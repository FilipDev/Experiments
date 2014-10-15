import jol.util.VMSupport;
import memoryfile.MemoryFile;
import memoryfile.MemoryFileManager;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

public class Main {

	public String a = "test123";
	public B b = new B();

	private static MemoryFile memoryFile = new MemoryFile("test/asdf/", new File("C:\\Users\\Administrator\\Desktop\\motd.txt"));

	private static BufferedReader reader = memoryFile.getReader();

	public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException
	{
		Class unsafeClass = Unsafe.class;
		Field field = unsafeClass.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		Unsafe unsafe = (Unsafe) field.get(null);

		Main main = new Main();

		Scanner scanner = new Scanner(System.in);

		try
		{
			while (true)
			{
				scanner.nextLine();
				main.doStuff(unsafe);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void a()
	{
		b();
	}

	public void b()
	{
		a();
	}

	public class TestClass {

	}

	public void doOtherOtherStuff(Unsafe unsafe)
	{
		TestClass testClass = new TestClass();
		doOtherStuff(testClass, unsafe);

		A a = new A();
		doOtherStuff(a, unsafe);
	}

	public void doOtherStuff(Object object, Unsafe unsafe)
	{
		System.out.println("Reading: " + object.getClass().getName());
		//for (long x = 0; x <= 128; x++)
		//{
		//	if (x % 1 == 0)
		//	{
		//		//System.out.println(x + " " + unsafe.getInt(object, x));
		//	}
		//}
		System.out.println(UnsafeImpl.Object2Trace(object));
		System.out.println("--------------------------------------------------");
	}

	public void doStuff(Unsafe unsafe) throws InstantiationException, InterruptedException, IOException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		Object object = new Object();
		F f = new F();
		B b = new B();
		A a = new A();
		C c = new C();
		D d = new D();
		E e = new E();
		NotA notA = new NotA();
		TestB testB = new TestB();

		//doOtherStuff(a, unsafe);
		//doOtherStuff(f, unsafe);
		//doOtherStuff(object, unsafe);

		//a.test();

		//unsafe.putLong(a, 80, unsafe.getLong(e, 8L));
		//unsafe.putLong(a, 128, unsafe.getLong(e, 8L));

		//a.test();

		int debug = 0;

		//doOtherStuff(a, unsafe);

		/*for (long x = 0; x <= 512; x += 4)
		{
			for (long y = 0; y <= 512; y += 4)
			{
				if ((unsafe.getInt(g, x) == unsafe.getInt(new char[]{'t', 'e', 's', '4'}, y)) && unsafe.getInt(g, x) != 0 && unsafe.getInt(g, x) != 1)
				{
					unsafe.putInt(g, x, unsafe.getInt(new char[]{}, y));
					System.out.println(x + " " + unsafe.getInt(g, x));
				}
			}
		}*/

		//AN EXAMPLE STRING
		/*String g = "test";

		long chararray = 0;

		//SETS CHAR ARRAY TO THE OFFSET WHERE THE CHAR ARRAY IS STORED (44L)
		for (long y = 0; y <= 128; y += 4)
		{
			if (unsafe.getInt(g, y) == unsafe.getInt(new char[]{'t', 'e', 's', 't'}, 20L))
				System.out.println("found char array at " + (chararray = y));
		}

		//SETS THE LOCATION WHERE THE OFFSET IN THE STRING WAS FOUND FOR THE CHAR ARRAY (44L)
		unsafe.putInt(g, chararray, unsafe.getInt(new char[]{'t', 'e', 's', '7'}, 20L));

		//PRINTS 'tes3'
		System.out.println(g);
		*/

		//int objAsInt = ByteBuffer.wrap(toByteArray(a.a)).getInt();

		/*for (long x = 0; x <= 512; x += 4)
		{
				if (unsafe.getInt(a, x) == unsafe.getInt(g, 8L))
				{
					System.out.println(x + " " + unsafe.getInt(a, x));
					unsafe.putInt(a, x, unsafe.getInt(h, 8L));
					System.out.println(x + " " + unsafe.getInt(a, x));
				}
		}*/

		/*System.out.println(a.test);
		System.out.println("testa" + a.testa);
		System.out.println("test" + a.a);
		unsafe.putInt(a, 12L, 0/*unsafe.getLong(8L));
		System.out.println(a.test);
		System.out.println("testa" + a.testa);
		System.out.println("test" + a.a);
		unsafe.putInt(a, 16L, 0/*unsafe.getLong(8L));
		System.out.println(a.test);
		System.out.println("testa" + a.testa);
		System.out.println("test" + a.a);
		unsafe.putInt(a, 24L, 0/*unsafe.getLong(8L));
		System.out.println(a.test);
		System.out.println("testa" + a.testa);
		System.out.println("test" + a.a);*/

		//long objKlassOOP = unsafe.getLong(b, 72L);

		//for (long x = 0; x <= 512; x += 8)
		//{
		//	if (unsafe.getLong(c, x) == objKlassOOP)
		//		System.out.println(x);
		//}

		/*doOtherStuff(a, unsafe);

		Object obj = new Object();

		a.a = "asdffff";
		a.test = obj;

		doOtherStuff(a, unsafe);

		//a.a = "asdf";

		int string = 0;

		//System.out.println(unsafe.getInt(a, 16L));

		for (long x = 0; x <= 512; x += 4)
		{
			for (long y = 0; x <= 512; x += 4)
			{
				if (unsafe.getInt("asdffff", x) == unsafe.getInt(a, y))
				{
					System.out.println(x + " " + unsafe.getInt("asdffff", x));
				}
			}
		}

		System.out.println("-----------------------------------");

		/*for (long x = 0; x <= 512; x += 4)
		{
			for (long y = 0; y <= 512; y += 4)
			{
				boolean z = unsafe.getInt(obj, x) == unsafe.getInt(a, y);
				if (z && unsafe.getInt(obj, x) != 0 && unsafe.getInt(obj, x) != 1)
				{
					System.out.println(x + " " + y + " " + unsafe.getInt(obj, x));
				}
			}
		}

		System.out.println(a.a);
		System.out.println(obj);
		//System.out.println(unsafe.getInt("test", 8L));

		doOtherStuff(a, unsafe);

		//doOtherStuff(a, unsafe);

		System.out.println(debug += 1);




		*/

		//G g = new G("test");

		//int address = obj2Address(unsafe, TestEnum.TEST1);

		//Object message = address2Obj(unsafe, address);

		//System.out.println(TestEnum.TEST2);

		//unsafe.putInt(TestEnum.TEST2, 16L, unsafe.getInt(TestEnum.TEST1, 16L));

		/*unsafe.putInt(E.class, 8L, unsafe.getInt(F.class, 8L));
		unsafe.putInt(E.class, 12L, unsafe.getInt(F.class, 12L));
		unsafe.putInt(E.class, 16L, unsafe.getInt(F.class, 16L));
		unsafe.putInt(E.class, 20L, unsafe.getInt(F.class, 16L));*/
		//new E().test("asdf");

		//unsafe.putInt(E.class, 84L, unsafe.getInt(F.class, 84L));

		//new E().test("asdf");

		//System.out.println(E.class.getName());

		//System.out.println(String.format("%1$03d", 0));

		//ConstantPool constantPool = SharedSecrets.getJavaLangAccess().getConstantPool(TestEnum.class);

		//System.out.println(obj2Address(unsafe, TestEnum.TEST1));
		//System.out.println(
		// obj2Address(unsafe, TestEnum.TEST2));
		//System.out.println(obj2Address(unsafe, TestEnum.TEST3));
		//System.out.println(obj2Address(unsafe, TestEnum.TEST4));

		//System.out.println(address2Obj(unsafe, unsafe.getInt(E.class.getMethod("test", String.class), 28L)));

		reader = MemoryFileManager.getMemoryFile("test/asdf/", "motd.txt").getReader();

		String line;
		while ((line = reader.readLine()) != null)
			System.out.println(line);

		reader.close();

		/*Integer s = 0;
		Float t = 1F;

		long someInt = VMSupport.toNativeAddress(normalize(unsafe.getInt(s, 8L)));
		long someInt2 = VMSupport.toNativeAddress(normalize(unsafe.getInt(t, 8L)));

		System.out.println(someInt);

		for (long x = 0; x <= 64; System.out.println(x + " " + unsafe.getAddress(someInt + x++)));

		System.out.println("--------------------------");

		for (long x = 0; x <= 64; System.out.println(x + " " + unsafe.getAddress(someInt2 + x++)));

		unsafe.putInt(t, 8L, unsafe.getInt(s, 8L));

		for (long x = 0; x <= 64; System.out.println(x + " " + unsafe.getAddress(someInt2 + x++)));

		someInt2 = VMSupport.toNativeAddress(normalize(unsafe.getInt(t, 8L)));

		for (long x = 0; x <= 64; System.out.println(x + " " + unsafe.getAddress(someInt2 + x++)));

		System.out.println(t);*/

		/*for (long x = 0; x <= 50000; x += 4)
		{
			System.out.println(x + " " + unsafe.getInt(address + x));
			String s;
			if ((s = String.valueOf(unsafe.getInt(address + x))).startsWith("-88") || String.valueOf(unsafe.getInt(address + x)).startsWith("-89"))
			{
				System.out.println(int2Obj(unsafe, unsafe.getInt(address + x)));
				if (int2Obj(unsafe, unsafe.getInt(address + x)).toString().startsWith("[C"))
					System.out.println(new String((char[]) int2Obj(unsafe, unsafe.getInt(address + x))));
			}
		}*/

		//long address2 = normalize(unsafe.getLong(f, 8L));

		//System.out.println(unsafe.getAddress(address1 + 12));

		//unsafe.putAddress(address1 + 12, address2);

		//for (long x = 0; x <= 256; x += 4)
		//{
		//	System.out.println(x + " " + address);
		//	if (String.valueOf(address).startsWith("420609"))
		//		System.out.println(address2Obj(unsafe, address));
		//}

		//e.getClass().getMethod("test", String.class).invoke(e, "asdf");

		//System.out.println(TestEnum.TEST2.a);

		/*for (long x = 16; x <= 512; x += 4)
		{
			//if (unsafe.getLong(a, x) == objKlassOOP)
			//	System.out.println(x);
			System.out.println(x);
			//System.out.println(a.test);
			System.out.println(a.asdf());
			try
			{
				//System.out.println("a" + a.testa);
			}
			catch (NullPointerException ex)
			{

			}
			try
			{
				//System.out.println("b" + a.testb);
			}
			catch (NullPointerException ex)
			{

			}
			//System.out.println("test" + a.a);
			unsafe.putInt(a, x, 0/*unsafe.getLong(8L));
			Thread.sleep(50L);
		}/*
		//unsafe.putLong(a, 0L, 1L);

		//System.out.println(a.getSuper());

		/*System.out.println(debug += 1);

		doOtherStuff(a, unsafe);

		//System.out.println(a.getSuper());

		System.out.println(debug += 1);

		doOtherStuff(a, unsafe);

		//System.out.println(a.asdf());
		System.out.println(a.test);
	}
		/*

		unsafe.putLong(object, 8L, unsafe.getLong(a, 8L));

		doOtherStuff(object, unsafe);

		doOtherStuff(a, unsafe);

		//ChangeListener changeListener = new ChangeListener(b, 0);
		//changeListener.start();

		for (long i = 0; i <= 256; i++)
		{
			//if (i != 6L)
			unsafe.putByte(b, i, unsafe.getByte(testB, i));
			//if (i > 8 || i < 5)
			//	System.out.println(i + " " + b.asdf());
			//for (Method method : b.getClass().getMethods())
			//	System.out.println(method.getName());
		}

		/**
		  * DOES NOT REPLACE CLASS, INT (4 BYTES) TOO SMALL TO COVER 8L
		  * unsafe.putInt(b, 4L, unsafe.getInt(a, 4L));
		  * DOES REPLACE CLASS, LONG (8 BYTES) LARGE ENOUGH TO COVER 8L TO 12L
		  * unsafe.putLong(b, 4L, unsafe.getLong(a, 4L));
		  * ALSO WORKS
		  * unsafe.putLong(b, 1L, unsafe.getLong(a, 1L));
		  * DOES NOT
		  * unsafe.putLong(b, 0L, unsafe.getLong(a, 0L));
		 **/
/*
		doOtherStuff(b, unsafe);
		TestClass testClass = new TestClass();
		//unsafe.putLong(testClass, 8L, unsafe.getLong(a, 8L));
		//System.out.println(testClass.getClass().getName());
		System.out.println(unsafe.getInt(testClass, 8L));
		doOtherStuff(testClass, unsafe);

		b.test();
		System.out.println(unsafe.getInt(b, 68L) == unsafe.getInt(a, 72L));
		unsafe.putInt(b, 68L, unsafe.getInt(testClass, 82L));
		b.test();
		//SAME CLASS: java.lang.Object class == A super class.
		System.out.println(unsafe.getInt(new Object(), 68L) == unsafe.getInt(a, 68L));
		System.out.println(unsafe.getInt(new Object(), 68L) == unsafe.getInt(b, 68L));

		System.out.println(unsafe.getInt(new Object(), 68L));
		System.out.println(unsafe.getInt(new Object(), 72L));
		System.out.println("------");
		System.out.println(unsafe.getInt(a, 12L));
		System.out.println(unsafe.getInt(b, 12L));
		System.out.println("------");
		unsafe.putInt(b, 8L, unsafe.getInt(a, 8L));
		//b.test();
		System.out.println(b.getClass().getSuperclass().getName());
		System.out.println("------");
		//unsafe.putInt(b, 68L, unsafe.getInt(notA, 8L));
		b.test();
		System.out.println(b.getClass().getSuperclass().getName());

		for (long x = 1; x <= 256; x++)
		{
			try
			{
				//System.out.println(x + " " + unsafe.getInt(d, x));
			}
			catch (NullPointerException ignored)
			{}*/
	}

	private static long normalize(int value) {
		if(value >= 0) return value;
		return (~0L >>> 32) & value;
	}

	public byte[] toByteArray(Object object) throws IOException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

		objectOutputStream.writeObject(object);
		objectOutputStream.flush();
		objectOutputStream.close();
		byteArrayOutputStream.close();

		return byteArrayOutputStream.toByteArray();

	}

	public class A {

		Object test = new Object();
		String a = "test";
		Object testa = new Object();
		Object testb = new Object();

		public void test()
		{
			System.out.println(this.getClass().getSuperclass().getName());
		}

		public String asdf()
		{
			return test.getClass().getName();
		}

		public String getSuper()
		{
			return this.toString();
		}

	}

	private class NotA {

		public void test()
		{
			System.out.println("Not A.");
		}

		public String asdf()
		{
			return "NOTA";
		}

	}

	public class B extends A {

		public void test()
		{
			System.out.println("B = test");

			super.test();
		}

		@Override
		public String asdf()
		{
			return super.asdf();
		}

	}

	public class TestB extends NotA {

		public void test()
		{
			System.out.println("TestB = test");

			super.test();
		}

	}

	public class C extends B {

		public void test()
		{
			System.out.println(this.getClass().getName());
			System.out.println("C = test");
		}

	}

	public class D extends C {

		public void test()
		{
			System.out.println(this.getClass().getName());
			System.out.println("D = test");
		}

	}

	public class E {

		String testa;
		String test;

		public void test(String test)
		{
			this.test = test;
			this.testa = test;
		}

	}

	public class F {

		String test;
		String b;

		public void test(String test)
		{
			this.test = test;

			System.out.println("test");
		}

	}

	public class G {

		public void test()
		{
			System.out.println("test");
		}

	}

	public long obj2Address(Unsafe unsafe, Object object)
	{
		Object[] objArray = new Object[]{object};
		return VMSupport.toNativeAddress(unsafe.getLong(objArray, unsafe.arrayBaseOffset(Object[].class)));
	}

	public Object address2Obj(Unsafe unsafe, long address)
	{
		Object[] objArray = new Object[1];
		unsafe.putLong(objArray, (long) unsafe.arrayBaseOffset(Object[].class), VMSupport.revertNativeAddress(address));
		return objArray[0];
	}

	public Object int2Obj(Unsafe unsafe, int address)
	{
		Object[] objArray = new Object[1];
		unsafe.putInt(objArray, (long) unsafe.arrayBaseOffset(Object[].class), address);
		return objArray[0];
	}

	public void changeStringValue(Unsafe unsafe, String a, String b)
	{
		//AN EXAMPLE STRING
		long chararray = 0;

		for (long y = 0; y <= 128; y += 4)
		{
			if (unsafe.getInt(a, y) == unsafe.getInt(a.toCharArray(), 20L))
			{
				chararray = y;
				break;
			}
		}

		unsafe.putInt(a, chararray, unsafe.getInt(b.toCharArray(), 20L));

		/**
		//GETS THE OFFSET WHERE THE VALUE OF THE CHAR ARRAY IS STORED IN MEMORY (20L)
		for (long y = 0; y <= 32; y += 4)
		{
			System.out.println(y + " " + unsafe.getInt(new char[]{'t', 'e', 's', 't'}, y));
		}
		*/
	}

	

}

