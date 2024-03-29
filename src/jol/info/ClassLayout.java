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
package jol.info;

import jol.layouters.CurrentLayouter;
import jol.layouters.Layouter;
import jol.util.VMSupport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.SortedSet;

/**
 * Handles the class data *with* the layout information.
 */
public class ClassLayout {

	/**
	 * Produce the class layout for the given class.
	 * This produces the layout for the current VM.
	 *
	 * @param klass class to work on
	 * @return class layout
	 */
	public static ClassLayout parseClass(Class<?> klass)
	{
		return parseClass(klass, new CurrentLayouter());
	}

	/**
	 * Produce the class layout for the given class, and given layouter.
	 *
	 * @param klass    class to work on
	 * @param layouter class layouter
	 * @return class layout
	 */
	public static ClassLayout parseClass(Class<?> klass, Layouter layouter)
	{
		return layouter.layout(ClassData.parseClass(klass));
	}

	private final ClassData classData;
	private final SortedSet<FieldLayout> fields;
	private final int headerSize;
	private final int size;

	/**
	 * Builds the class layout.
	 *
	 * @param classData    class data
	 * @param fields       field layouts
	 * @param headerSize   header size
	 * @param instanceSize instance size
	 * @param check        whether to check important invariants
	 */
	public ClassLayout(ClassData classData, SortedSet<FieldLayout> fields, int headerSize, int instanceSize, boolean check)
	{
		this.classData = classData;
		this.fields = fields;
		this.headerSize = headerSize;
		this.size = instanceSize;
		if (check)
		{
			checkInvariants();
		}
	}

	private void checkInvariants()
	{
		long lastOffset = 0;
		for (FieldLayout f : fields)
		{
			if (f.offset() % f.size() != 0)
			{
				throw new IllegalStateException("Field " + f + " is not aligned");
			}
			if (f.offset() + f.size() > instanceSize())
			{
				throw new IllegalStateException("Field " + f + " is overflowing the object of size " + instanceSize());
			}
			if (f.offset() < lastOffset)
			{
				throw new IllegalStateException("Field " + f + " overlaps with the previous field");
			}
			lastOffset = f.offset() + f.size();
		}
	}

	/**
	 * Answer the set of fields, including those in superclasses
	 *
	 * @return sorted set of fields
	 */
	public SortedSet<FieldLayout> fields()
	{
		return fields;
	}

	/**
	 * Answer instance size
	 *
	 * @return instance size
	 */
	public int instanceSize()
	{
		return size;
	}

	/**
	 * Answer header size
	 *
	 * @return header size
	 */
	public int headerSize()
	{
		return headerSize;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (FieldLayout f : fields())
		{
			sb.append(f).append("\n");
		}
		sb.append("size = ").append(size).append("\n");
		return sb.toString();
	}

	/**
	 * Produce printable stringly representation of class layout.
	 * This method does not require alive instance, just the class.
	 *
	 * @return human-readable layout info
	 */
	public String toPrintable()
	{
		return toPrintable(null);
	}

	/**
	 * Produce printable stringly representation of class layout.
	 * This method accepts instance to read the actual data from.
	 *
	 * @param instance instance to work on
	 * @return human-readable layout info
	 */
	public String toPrintable(Object instance)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		int maxTypeLen = 5;
		for (FieldLayout f : fields())
		{
			maxTypeLen = Math.max(f.typeClass().length(), maxTypeLen);
		}

		int maxDescrLen = 30;
		for (FieldLayout f : fields())
		{
			maxDescrLen = Math.max((f.hostClass() + "" + f.name()).length(), maxDescrLen);
		}

		pw.println(classData.name() + " object internals:");
		pw.printf(" %6s %5s %" + maxTypeLen + "s %-" + maxDescrLen + "s %s%n", "OFFSET", "SIZE", "TYPE", "DESCRIPTION", "VALUE");
		if (instance != null)
		{
			for (long off = 0; off < headerSize(); off += 4)
			{
				pw.printf(" %6d %5d %" + maxTypeLen + "s %-" + maxDescrLen + "s %s%n", off, 4, "", "(object header)",
						toHex(VMSupport.U.getByte(instance, off + 0) & 0xFF) + " " +
								toHex(VMSupport.U.getByte(instance, off + 1) & 0xFF) + " " +
								toHex(VMSupport.U.getByte(instance, off + 2) & 0xFF) + " " +
								toHex(VMSupport.U.getByte(instance, off + 3) & 0xFF) + " " +
								"(" +
								toBinary(VMSupport.U.getByte(instance, off + 0) & 0xFF) + " " +
								toBinary(VMSupport.U.getByte(instance, off + 1) & 0xFF) + " " +
								toBinary(VMSupport.U.getByte(instance, off + 2) & 0xFF) + " " +
								toBinary(VMSupport.U.getByte(instance, off + 3) & 0xFF) + ")"
				);
			}
		}
		else
		{
			pw.printf(" %6d %5d %" + maxTypeLen + "s %-" + maxDescrLen + "s %s%n", 0, headerSize(), "", "(object header)", "N/A");
		}

		int nextFree = headerSize();

		int interLoss = 0;
		int exterLoss = 0;

		for (FieldLayout f : fields())
		{
			if (f.offset() > nextFree)
			{
				pw.printf(" %6d %5d %" + maxTypeLen + "s %-" + maxDescrLen + "s %s%n", nextFree, (f.offset() - nextFree), "", "(alignment/padding gap)", "N/A");
				interLoss += (f.offset() - nextFree);
			}
			pw.printf(" %6d %5d %" + maxTypeLen + "s %-" + maxDescrLen + "s %s%n",
					f.offset(),
					f.size(),
					f.typeClass(),
					f.hostClass() + "." + f.name(),
					(instance != null) ? f.safeValue(instance) : "N/A"
			);

			nextFree = f.offset() + f.size();
		}

		VMSupport.SizeInfo info = VMSupport.tryExactObjectSize(instance, this);

		if (info.instanceSize() != nextFree)
		{
			exterLoss = info.instanceSize() - nextFree;
			pw.printf(" %6d %5s %" + maxTypeLen + "s %s%n", nextFree, exterLoss, "", "(loss due to the next object alignment)");
		}

		if (info.exactSize())
		{
			pw.printf("Instance size: %d bytes (reported by VM agent)%n", info.instanceSize());
		}
		else
		{
			if (instance != null)
			{
				pw.printf("Instance size: %d bytes (estimated, add this JAR via -javaagent: to get accurate result)%n", info.instanceSize());
			}
			else
			{
				pw.printf("Instance size: %d bytes (estimated, the sample instance is not available)%n", info.instanceSize());
			}
		}

		pw.printf("Space losses: %d bytes internal + %d bytes external = %d bytes total%n", interLoss, exterLoss, interLoss + exterLoss);

		pw.close();

		return sw.toString();
	}

	// very ineffective, so what?
	private static String toBinary(int x)
	{
		String s = Integer.toBinaryString(x);
		int deficit = 8 - s.length();
		for (int c = 0; c < deficit; c++)
		{
			s = "0" + s;
		}

		return s.substring(0, 4) + " " + s.substring(4);
	}

	// very ineffective, so what?
	private static String toHex(int x)
	{
		String s = Integer.toHexString(x);
		int deficit = 2 - s.length();
		for (int c = 0; c < deficit; c++)
		{
			s = "0" + s;
		}
		return s;
	}

}
