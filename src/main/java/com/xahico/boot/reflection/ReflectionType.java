/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ReflectionType {
	public static ReflectionType genericType (final Class<?> jclass){
		return new ReflectionType(jclass);
	}
	
	public static ReflectionType[] genericTypes (final Class<?>[] jclassList){
		final ReflectionType[] array;
		
		array = new ReflectionType[jclassList.length];
		
		for (var i = 0; i < jclassList.length; i++) {
			array[i] = genericType(jclassList[i]);
		}
		
		return array;
	}
	
	
	
	private final Type type;
	
	
	
	ReflectionType (final Type type){
		super();
		
		this.type = type;
	}
	
	
	
	public ReflectionType getComponentType (){
		if (! this.isArray()) 
			return null;
		else {
			return ReflectionType.genericType(this.getTypeClass().getComponentType());
		}
	}
	
	public Class<?>[] getGenericTypeArgumentClasses (){
		final Class<?>[]       typeArgumentClassList;
		final ReflectionType[] typeArgumentObjectList;
		
		if (! this.isParameterized()) 
			return null;
		
		typeArgumentObjectList = this.getGenericTypeArguments();
		
		typeArgumentClassList = new Class[typeArgumentObjectList.length];
		
		for (var i = 0; i < typeArgumentObjectList.length; i++) {
			typeArgumentClassList[i] = typeArgumentObjectList[i].getTypeClass();
		}
		
		return typeArgumentClassList;
	}
	
	public ReflectionType[] getGenericTypeArguments (){
		final ParameterizedType parameterized;
		final Type[]            parameterTypeArrayNative;
		final ReflectionType[]  parameterTypeArraySuper;
		
		if (! this.isParameterized()) 
			return null;
		
		parameterized = (ParameterizedType)(this.type);
		
		parameterTypeArrayNative = parameterized.getActualTypeArguments();
		
		parameterTypeArraySuper = new ReflectionType[parameterTypeArrayNative.length];
		
		for (var i = 0; i < parameterTypeArrayNative.length; i++) {
			parameterTypeArraySuper[i] = new ReflectionType(parameterTypeArrayNative[i]);
		}
		
		return parameterTypeArraySuper;
	}
	
	public int getGenericTypeArgumentsCount (){
		final ParameterizedType parameterized;
		
		if (! this.isParameterized()) 
			return 0;
		
		parameterized = (ParameterizedType)(this.type);
		
		return parameterized.getActualTypeArguments().length;
	}
	
	public Class<?> getTypeClass (){
		Class<?>     typeClass;
		final String typeName;
		
		typeName = this.getTypeClassName();
		
		try {
			typeClass = Class.forName(typeName);
		} catch (final ClassNotFoundException ex) {
			switch (typeName) {
				case "boolean":
					typeClass = boolean.class;
					
					break;
				case "byte":
					typeClass = byte.class;
					
					break;
				case "char":
					typeClass = char.class;
					
					break;
				case "double":
					typeClass = double.class;
					
					break;
				case "float":
					typeClass = float.class;
					
					break;
				case "int":
					typeClass = int.class;
					
					break;
				case "long":
					typeClass = long.class;
					
					break;
				case "short":
					typeClass = short.class;
					
					break;
				default: {
					throw new InternalError(ex);
				}
			}
		}
		
		return (this.isArray() ? Array.newInstance(typeClass, 0).getClass() : typeClass);
	}
	
	public String getTypeClassName (){
		final int    paramsBegin;
		final String typeName;
		final String typeString;
		
		typeString = this.getTypeString();
		
		paramsBegin = typeString.indexOf('<');
		
		if (paramsBegin == -1) 
			typeName = typeString;
		else {
			typeName = typeString.substring(0, paramsBegin);
		}
		
		return (typeName.endsWith("[]") ? typeName.substring(0, (typeName.length() - 2)) : typeName);
	}
	
	public String getTypeString (){
		final int    firstDelimiter;
		final String fullString;
		
		fullString = this.type.getTypeName();
		
		firstDelimiter = fullString.indexOf('<');
		
		if (firstDelimiter != -1) 
			return fullString.substring(0, firstDelimiter);
		else {
			return fullString;
		}
	}
	
	public boolean isArray (){
		return this.type.getTypeName().endsWith("[]");
	}
	
	public boolean isParameterized (){
		return (this.type instanceof ParameterizedType);
	}
	
	@Override
	public String toString (){
		return this.type.getTypeName();
	}
}