package nl.scholten.crypto.cryptobox;

import java.lang.reflect.Field;

public class Test {

	public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String text = "Valentijn";
		Field field = text.getClass().getDeclaredField("value");
		field.setAccessible(true);
//		field.set(bean, "Hello");
		System.out.println((char[]) field.get(text));
		char[] internal = (char[]) field.get(text);
		
		internal[5] = 'X';
		
		System.out.println(text);
		
		String data = "TEST";
		String data2 = new String(data.toCharArray());
		String orgData2 = data2;
		
		
		String dataI = data.intern();
		String dataI2 = data2.intern();
		
		System.out.println(dataI == data);
		
		System.out.println(data2.intern() == data2);
				
	}

}
