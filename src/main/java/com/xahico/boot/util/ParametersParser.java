/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ParametersParser {
	private List<String> args = null;
	private String       prefix = null;
	
	
	
	public ParametersParser (){
		super();
	}
	
	
	
	private void ensureInitialize (){
		if (null == this.args) {
			throw new Error();
		}
	}
	
	public void mount (final List<String> argList){
		this.args = argList;
	}
	
	public void mount (final String argString){
		this.mount(StringUtilities.splitStringByWhitespaceIntoList(argString));
	}
	
	public void mount (final String[] args){
		this.mount(Arrays.asList(args));
	}
	
	public Parameters parse (){
		final Parameters result;
		
		ensureInitialize();
		
		result = new Parameters();
		result.setPrefix(this.prefix);
		
		if (null != this.prefix) {
			String       key = null;
			List<String> valueList = null;
			
			for (var i = 0; i < this.args.size(); i++) {
				final String element;
				
				element = this.args.get(i);
				
				if (element.startsWith(this.prefix)) {
					if (null != key) {
						result.append(key, valueList);
					}
					
					key = element.substring(this.prefix.length());
					
					valueList = new LinkedList<>();
				} else if (null != valueList) {
					valueList.add(element);
				} else {
					result.append(element);
				}
			}
			
			if (null != key) {
				result.append(key, valueList);
			}
		} else {
			result.append(this.args);
		}
		
		return result;
	}
	
	public void setPrefix (final String prefix){
		this.prefix = prefix;
	}
}