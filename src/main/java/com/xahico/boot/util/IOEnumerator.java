/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.util;

import java.io.IOException;

/**
 * TBD. 
 * 
 * @param <T> 
 * TBD
 * 
 * @author KARBAROTTA
'*/
@FunctionalInterface
public interface IOEnumerator <T> {
	void next (final T element) throws IOException;
}