import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Pointer {

	private static Unsafe unsafe;

	static
	{
		try
		{
			Field theUnsafeField = Unsafe.class.getDeclaredFields()[0];
			theUnsafeField.setAccessible(true);
			unsafe = (Unsafe) theUnsafeField.get(null);
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
			unsafe = null;
		}
	}

	private final Object object;

	public Pointer(Object object)
	{
		this.object = object;
	}

	public Object getObject()
	{
		return this.object;
	}

	public long getAddress()
	{
		Object[] pointerGetter = new Object[]{object};
		return unsafe.getLong(pointerGetter, unsafe.arrayBaseOffset(Object[].class));
	}

	public String getAddressString()
	{
		return Long.toHexString(getAddress());
	}

	public static void putAddress(long address1, long address2)
	{
		unsafe.putLong(address1, address2);
	}
}
