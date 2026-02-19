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

package com.xahico.boot.net.inet;

import java.io.IOException;

/**
 * TBD.
 * 
 * @author tutomiko
**/
public class Internet6Address extends InternetAddress {
	public static final Internet6Address LOCALHOST = new Internet6Address();
	
	
	
	public static Internet6Address currentPublic () throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static Internet6Address forName (final String address) throws InvalidAddressException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static Internet6Address random (){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static boolean validate (final String addr){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	
	
	public Internet6Address (){
		super();
	}
	
	
	
	@Override
	public byte[] getBytes (){
		throw new UnsupportedOperationException("Not supported yet.");
	}
}