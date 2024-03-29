/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jol.util;

import jol.info.ClassData;
import jol.info.ClassLayout;
import jol.layouters.CurrentLayouter;
import sun.misc.Unsafe;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import javax.management.openmbean.CompositeDataSupport;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VM support doorway.
 * Contains all the special tricks and methods to poll VM about it's secrets.
 *
 * @author Aleksey Shipilev
 */
public class VMSupport {

	private static Instrumentation INSTRUMENTATION;

	public static final Unsafe U;

	public static final String VM_NAME;
	public static final int ADDRESS_SIZE;
	public static final int OBJ_ALIGNMENT;
	public static final int OBJ_HEADER_SIZE;
	public static final boolean USE_COMPRESSED_REFS;
	public static final int COMPRESSED_REF_SHIFT;

	public static final int REF_SIZE;
	public static final int BOOLEAN_SIZE;
	public static final int BYTE_SIZE;
	public static final int CHAR_SIZE;
	public static final int DOUBLE_SIZE;
	public static final int FLOAT_SIZE;
	public static final int INT_SIZE;
	public static final int LONG_SIZE;
	public static final int SHORT_SIZE;

	private static final ThreadLocal<Object[]> BUFFERS;
	private static final long OBJECT_ARRAY_BASE;

	static
	{
		U = AccessController.doPrivileged(
				new PrivilegedAction<Unsafe>() {
					public Unsafe run()
					{
						try
						{
							Field unsafe = Unsafe.class.getDeclaredField("theUnsafe");
							unsafe.setAccessible(true);
							return (Unsafe) unsafe.get(null);
						} catch (NoSuchFieldException e)
						{
							throw new IllegalStateException(e);
						} catch (IllegalAccessException e)
						{
							throw new IllegalStateException(e);
						}
					}
				}
		);

		OBJECT_ARRAY_BASE = U.arrayBaseOffset(Object[].class);
		BUFFERS = new ThreadLocal<Object[]>() {
			@Override
			protected Object[] initialValue()
			{
				return new Object[1];
			}
		};

		int headerSize;
		try
		{
			long off1 = U.objectFieldOffset(HeaderClass.class.getField("b1"));
			headerSize = (int) off1;
		} catch (NoSuchFieldException e)
		{
			headerSize = -1;
		}

		VMOptions opts = VMOptions.getOptions();

		ADDRESS_SIZE = U.addressSize();
		OBJ_HEADER_SIZE = headerSize;

		VM_NAME = opts.name;
		USE_COMPRESSED_REFS = opts.compressedRef;
		COMPRESSED_REF_SHIFT = opts.compressRefShift;
		OBJ_ALIGNMENT = opts.objectAlignment;
		REF_SIZE = opts.sizeReference;
		BOOLEAN_SIZE = opts.sizeBoolean;
		BYTE_SIZE = opts.sizeByte;
		CHAR_SIZE = opts.sizeChar;
		DOUBLE_SIZE = opts.sizeDouble;
		FLOAT_SIZE = opts.sizeFloat;
		INT_SIZE = opts.sizeInt;
		LONG_SIZE = opts.sizeLong;
		SHORT_SIZE = opts.sizeShort;
	}

	public static long toNativeAddress(long address)
	{
		if (USE_COMPRESSED_REFS)
		{
			return address << COMPRESSED_REF_SHIFT;
		}
		else
		{
			return address;
		}
	}

	public static long revertNativeAddress(long address)
	{
		if (USE_COMPRESSED_REFS)
		{
			return address >> COMPRESSED_REF_SHIFT;
		}
		else
		{
			return address;
		}
	}

	public static int align(int addr)
	{
		return align(addr, OBJ_ALIGNMENT);
	}

	public static int align(int addr, int align)
	{
		if ((addr % align) == 0)
		{
			return addr;
		}
		else
		{
			return ((addr / align) + 1) * align;
		}
	}

	public static String vmDetails()
	{
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);

		out.println("Running " + (ADDRESS_SIZE * 8) + "-bit " + VM_NAME + " VM.");
		if (USE_COMPRESSED_REFS)
			out.println("Using compressed references with " + COMPRESSED_REF_SHIFT + "-bit shift.");
		out.println("Objects are " + OBJ_ALIGNMENT + " bytes aligned.");

		out.printf("%-19s: %d, %d, %d, %d, %d, %d, %d, %d, %d [bytes]%n",
				"Field sizes by type",
				REF_SIZE,
				BOOLEAN_SIZE,
				BYTE_SIZE,
				CHAR_SIZE,
				SHORT_SIZE,
				INT_SIZE,
				FLOAT_SIZE,
				LONG_SIZE,
				DOUBLE_SIZE
		);

		out.printf("%-19s: %d, %d, %d, %d, %d, %d, %d, %d, %d [bytes]%n",
				"Array element sizes",
				U.arrayIndexScale(Object[].class),
				U.arrayIndexScale(boolean[].class),
				U.arrayIndexScale(byte[].class),
				U.arrayIndexScale(char[].class),
				U.arrayIndexScale(short[].class),
				U.arrayIndexScale(int[].class),
				U.arrayIndexScale(float[].class),
				U.arrayIndexScale(long[].class),
				U.arrayIndexScale(double[].class)
		);

		out.close();
		return sw.toString();
	}

	private static int guessAlignment(int oopSize)
	{
		final int COUNT = 10000;
		Object[] array = new Object[COUNT];
		long[] offsets = new long[COUNT];

		Random r = new Random();

		int min = -1;
		for (int t = 0; t < 100; t++)
		{
			for (int c = 0; c < COUNT; c++)
			{
				int s = r.nextInt(5);
				switch (s)
				{
					case 0:
						array[c] = new MyObject1();
						break;
					case 1:
						array[c] = new MyObject2();
						break;
					case 2:
						array[c] = new MyObject3();
						break;
					case 3:
						array[c] = new MyObject4();
						break;
					case 4:
						array[c] = new MyObject5();
						break;
					default:
						throw new IllegalStateException("Error while selecting the object type: type = " + s);
				}
			}

			System.gc();

			for (int c = 0; c < COUNT; c++)
			{
				offsets[c] = addressOf(array[c], oopSize);
			}

			Arrays.sort(offsets);

			List<Integer> sizes = new ArrayList<Integer>();
			for (int c = 1; c < COUNT; c++)
			{
				sizes.add((int) (offsets[c] - offsets[c - 1]));
			}

			for (int s : sizes)
			{
				if (s <= 0) continue;
				if (min == -1)
				{
					min = s;
				}
				else
				{
					min = MathUtil.gcd(min, s);
				}
			}
		}

		return min;
	}

	public static long addressOf(Object o)
	{
		return addressOf(o, REF_SIZE);
	}

	public static long addressOf(Object o, int oopSize)
	{
		Object[] array = BUFFERS.get();

		array[0] = o;

		long objectAddress;
		switch (oopSize)
		{
			case 4:
				objectAddress = U.getInt(array, OBJECT_ARRAY_BASE) & 0xFFFFFFFFL;
				break;
			case 8:
				objectAddress = U.getLong(array, OBJECT_ARRAY_BASE);
				break;
			default:
				throw new Error("unsupported address size: " + oopSize);
		}

		array[0] = null;

		return toNativeAddress(objectAddress);
	}

	public static void premain(String agentArgs, Instrumentation inst)
	{
		INSTRUMENTATION = inst;
	}

	public static SizeInfo tryExactObjectSize(Object o, ClassLayout layout)
	{
		return new SizeInfo(o, layout);
	}

	public static class SizeInfo {
		private final int size;
		private final boolean exactSizeAvail;

		public SizeInfo(Object o, ClassLayout layout)
		{
			exactSizeAvail = VMSupport.INSTRUMENTATION != null && o != null;
			size = exactSizeAvail ? (int) VMSupport.INSTRUMENTATION.getObjectSize(o) : layout.instanceSize();
		}

		public int instanceSize()
		{
			return size;
		}

		public boolean exactSize()
		{
			return exactSizeAvail;
		}
	}

	private static class VMOptions {
		private final String name;
		private final boolean compressedRef;
		private final int compressRefShift;
		private final int objectAlignment;

		private final int sizeReference;
		private final int sizeBoolean = getMinDiff(MyBooleans4.class);
		private final int sizeByte = getMinDiff(MyBytes4.class);
		private final int sizeShort = getMinDiff(MyShorts4.class);
		private final int sizeChar = getMinDiff(MyChars4.class);
		private final int sizeFloat = getMinDiff(MyFloats4.class);
		private final int sizeInt = getMinDiff(MyInts4.class);
		private final int sizeLong = getMinDiff(MyLongs4.class);
		private final int sizeDouble = getMinDiff(MyDoubles4.class);

		public static int getMinDiff(Class<?> klass)
		{
			try
			{
				int off1 = (int) U.objectFieldOffset(klass.getDeclaredField("f1"));
				int off2 = (int) U.objectFieldOffset(klass.getDeclaredField("f2"));
				int off3 = (int) U.objectFieldOffset(klass.getDeclaredField("f3"));
				int off4 = (int) U.objectFieldOffset(klass.getDeclaredField("f4"));
				return MathUtil.minDiff(off1, off2, off3, off4);
			} catch (NoSuchFieldException e)
			{
				throw new IllegalStateException("Infrastructure failure, klass = " + klass, e);
			}
		}

		public VMOptions(String name)
		{
			this.name = name;
			this.sizeReference = U.addressSize();
			this.objectAlignment = guessAlignment(this.sizeReference);
			this.compressedRef = false;
			this.compressRefShift = 1;
		}

		public VMOptions(String name, int align)
		{
			this.name = name;
			this.sizeReference = 4;
			this.objectAlignment = align;
			this.compressedRef = true;
			this.compressRefShift = MathUtil.log2p(align);
		}

		public VMOptions(String name, int align, int compRefShift)
		{
			this.name = name;
			this.sizeReference = 4;
			this.objectAlignment = align;
			this.compressedRef = true;
			this.compressRefShift = compRefShift;
		}

		private static VMOptions getOptions()
		{
			// try Hotspot
			VMOptions hsOpts = getHotspotSpecifics();
			if (hsOpts != null) return hsOpts;

			// try JRockit
			VMOptions jrOpts = getJRockitSpecifics();
			if (jrOpts != null) return jrOpts;

			// When running with CompressedOops on 64-bit platform, the address size
			// reported by Unsafe is still 8, while the real reference fields are 4 bytes long.
			// Try to guess the reference field size with this naive trick.
			int oopSize;
			try
			{
				long off1 = U.objectFieldOffset(CompressedOopsClass.class.getField("obj1"));
				long off2 = U.objectFieldOffset(CompressedOopsClass.class.getField("obj2"));
				oopSize = (int) Math.abs(off2 - off1);
			} catch (NoSuchFieldException e)
			{
				throw new IllegalStateException("Infrastructure failure", e);
			}

			if (oopSize != U.addressSize())
			{
				return new VMOptions("Auto-detected", 3); // assume compressed references have << 3 shift
			}
			else
			{
				return new VMOptions("Auto-detected");
			}
		}

		private static VMOptions getHotspotSpecifics()
		{
			String name = System.getProperty("java.vm.name");
			if (!name.contains("HotSpot") && !name.contains("OpenJDK"))
			{
				return null;
			}

			try
			{
				MBeanServer server = ManagementFactory.getPlatformMBeanServer();

				try
				{
					ObjectName mbean = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
					CompositeDataSupport compressedOopsValue = (CompositeDataSupport) server.invoke(mbean, "getVMOption", new Object[]{"UseCompressedOops"}, new String[]{"java.lang.String"});
					boolean compressedOops = Boolean.valueOf(compressedOopsValue.get("value").toString());
					if (compressedOops)
					{
						// if compressed oops are enabled, then this option is also accessible
						CompositeDataSupport alignmentValue = (CompositeDataSupport) server.invoke(mbean, "getVMOption", new Object[]{"ObjectAlignmentInBytes"}, new String[]{"java.lang.String"});
						int align = Integer.valueOf(alignmentValue.get("value").toString());
						return new VMOptions("HotSpot", align);
					}
					else
					{
						return new VMOptions("HotSpot");
					}

				} catch (RuntimeMBeanException iae)
				{
					return new VMOptions("HotSpot");
				}
			} catch (RuntimeException re)
			{
				System.err.println("Failed to read HotSpot-specific configuration properly, please report this as the bug");
				re.printStackTrace();
				return null;
			} catch (Exception exp)
			{
				System.err.println("Failed to read HotSpot-specific configuration properly, please report this as the bug");
				exp.printStackTrace();
				return null;
			}
		}

		private static VMOptions getJRockitSpecifics()
		{
			String name = System.getProperty("java.vm.name");
			if (!name.contains("JRockit"))
			{
				return null;
			}

			try
			{
				MBeanServer server = ManagementFactory.getPlatformMBeanServer();
				String str = (String) server.invoke(new ObjectName("oracle.jrockit.management:type=DiagnosticCommand"), "execute", new Object[]{"print_vm_state"}, new String[]{"java.lang.String"});
				String[] split = str.split("\n");
				for (String s : split)
				{
					if (s.contains("CompRefs"))
					{
						Pattern pattern = Pattern.compile("(.*?)References are compressed, with heap base (.*?) and shift (.*?)\\.");
						Matcher matcher = pattern.matcher(s);
						if (matcher.matches())
						{
							return new VMOptions("JRockit (experimental)", 8, Integer.valueOf(matcher.group(3)));
						}
						else
						{
							return new VMOptions("JRockit (experimental)");
						}
					}
				}
				return null;
			} catch (RuntimeException re)
			{
				System.err.println("Failed to read JRockit-specific configuration properly, please report this as the bug");
				re.printStackTrace();
				return null;
			} catch (Exception exp)
			{
				System.err.println("Failed to read JRockit-specific configuration properly, please report this as the bug");
				exp.printStackTrace();
				return null;
			}
		}

	}

	public static int sizeOf(Object o)
	{
		if (VMSupport.INSTRUMENTATION != null)
		{
			return VMSupport.align((int) VMSupport.INSTRUMENTATION.getObjectSize(o));
		}

		return new CurrentLayouter().layout(ClassData.parseInstance(o)).instanceSize();
	}

	/**
	 * Produces the toString string, only calling toString() on known types,
	 * which do not mutate the instance.
	 *
	 * @param o object to process
	 * @return toString
	 */
	public static String safeToString(Object o)
	{
		if (o == null) return "null";

		if (o.getClass().isArray())
		{
			Class<?> type = o.getClass().getComponentType();
			if (type == boolean.class) return Arrays.toString((boolean[]) o);
			if (type == byte.class) return Arrays.toString((byte[]) o);
			if (type == short.class) return Arrays.toString((short[]) o);
			if (type == char.class) return Arrays.toString((char[]) o);
			if (type == int.class) return Arrays.toString((int[]) o);
			if (type == float.class) return Arrays.toString((float[]) o);
			if (type == long.class) return Arrays.toString((long[]) o);
			if (type == double.class) return Arrays.toString((double[]) o);

			Object[] oos = (Object[]) o;
			String[] strs = new String[oos.length];
			for (int i = 0; i < oos.length; i++)
			{
				strs[i] = (oos[i] == null) ? "null" : safeToString(oos[i]);
			}
			return Arrays.toString(strs);
		}

		if (o.getClass().isPrimitive()) return o.toString();
		if (o.getClass() == Boolean.class) return o.toString();
		if (o.getClass() == Byte.class) return o.toString();
		if (o.getClass() == Short.class) return o.toString();
		if (o.getClass() == Character.class) return o.toString();
		if (o.getClass() == Integer.class) return o.toString();
		if (o.getClass() == Float.class) return o.toString();
		if (o.getClass() == Long.class) return o.toString();
		if (o.getClass() == Double.class) return o.toString();
		return "(object)";
	}


	static class CompressedOopsClass {
		public Object obj1;
		public Object obj2;
	}

	static class HeaderClass {
		public boolean b1;
	}

	static class MyObject1 {

	}

	static class MyObject2 {
		private boolean b;
	}

	static class MyObject3 {
		private int i;
	}

	static class MyObject4 {
		private long l;
	}

	static class MyObject5 {
		private Object o;
	}

	static class MyBooleans4 {
		private boolean f1, f2, f3, f4;
	}

	static class MyBytes4 {
		private byte f1, f2, f3, f4;
	}

	static class MyShorts4 {
		private short f1, f2, f3, f4;
	}

	static class MyChars4 {
		private char f1, f2, f3, f4;
	}

	static class MyInts4 {
		private int f1, f2, f3, f4;
	}

	static class MyFloats4 {
		private float f1, f2, f3, f4;
	}

	static class MyLongs4 {
		private long f1, f2, f3, f4;
	}

	static class MyDoubles4 {
		private double f1, f2, f3, f4;
	}

}
