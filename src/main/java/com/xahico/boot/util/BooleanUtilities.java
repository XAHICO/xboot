/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 *
 * Copyright (C) 2022 Tuomas Kontiainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xahico.boot.util;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class BooleanUtilities {
	public static boolean fromNumber (final int n){
		return (n != 0);
	}
	
	public static boolean fromString (final String s){
		assert(null != s);
		
		return Boolean.parseBoolean(s);
	}
	
	public static int toNumber (final boolean b){
		return (b ? 1 : 0);
	}
	
	
	
	private BooleanUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}