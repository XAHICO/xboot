/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.util.configurable;

import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface Configuration {
	/**
	 * TBD.
	 * 
	 * @param key 
	 * TBD
	 * 
	 * @return 
	 * TBD
	 * 
	 * @throws NoSuchPropertyException 
	 * TBD
	**/
	public Property getProperty (final String key) throws NoSuchPropertyException;
	
	/**
	 * TBD.
	 * 
	 * @return 
	 * TBD
	**/
	public Set<String> properties ();
	
	/**
	 * TBD.
	 * 
	 * @param key 
	 * TBD
	 * 
	 * @param value 
	 * TBD
	 * 
	 * @throws NoSuchPropertyException 
	 * TBD
	**/
	public void setProperty (final String key, final String value) throws NoSuchPropertyException;
	
	
	
	/**
	 * TBD.
	 * 
	 * @author hat
	**/
	public final class Property {
		/** TBD. **/
		private final String key;
		
		/** TBD. **/
		private String value = null;
		
		
		
		public Property (final String key, final String value){
			this.key = key;
			this.set(value);
		}
		
		
		
		public boolean booleanValue (){
			return Boolean.parseBoolean(this.get());
		}
		
		public byte byteValue (){
			return Byte.parseByte(this.get());
		}
		
		public double doubleValue (){
			return Double.parseDouble(this.get());
		}
		
		public float floatValue (){
			return Float.parseFloat(this.get());
		}
		
		public String get (){
			final String sval;
			
			sval = this.value.stripTrailing();
			
			if (sval.startsWith("\"") && sval.endsWith("\"")) 
				return sval.substring(1, sval.length() - 1);
			else {
				return sval;
			}
		}
		
		/**
		 * TBD.
		 * 
		 * @return 
		 * TBD
		**/
		public String getKey (){
			return this.key;
		}
		
		public int intValue (){
			return Integer.parseInt(this.get());
		}
		
		/**
		 * TBD.
		 * 
		 * @return 
		 * TBD
		**/
		public boolean isNull (){
			return "null".equals(this.get());
		}
		
		public long longValue (){
			return Long.parseLong(this.get());
		}
		
		/**
		 * TBD.
		 * 
		 * @param newValue 
		 * TBD
		**/
		public synchronized void set (final String newValue){
			if (newValue.matches("^\".*\"$")) 
				this.value = newValue;
			else {
				this.value = newValue.toLowerCase();
			}
		}
		
		public void set (final boolean newValue){
			this.set(Boolean.toString(newValue));
		}
		
		public void set (final byte newValue){
			this.set(Byte.toString(newValue));
		}
		
		public void set (final double newValue){
			this.set(Double.toString(newValue));
		}
		
		public void set (final float newValue){
			this.set(Float.toString(newValue));
		}
		
		public void set (final int newValue){
			this.set(Integer.toString(newValue));
		}
		
		public void set (final long newValue){
			this.set(Long.toString(newValue));
		}
		
		public void set (final short newValue){
			this.set(Short.toString(newValue));
		}
		
		public short shortValue (){
			return Short.parseShort(this.get());
		}
	}
}