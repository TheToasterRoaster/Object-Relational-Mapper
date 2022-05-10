package com.room3.dao;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.room3.annotations.Column;
import com.room3.util.ColumnField;

public class Calculator {

	public String getColType(ColumnField column) {
		
		String t = column.getType().getSimpleName();
		
		if (t.equals("String")) {
			return "VARCHAR(50)";
		} else if (t.equals("int")) {
			return "Integer";
		}
	
		
		return null;
	}
	public static Object getNewInstance(Class<?> o) {
		Object[] j = null;
		try {
			return o.getDeclaredConstructor().newInstance(j);
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return o;
		
	}
}
