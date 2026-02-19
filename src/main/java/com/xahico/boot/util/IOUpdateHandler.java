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
 * @author hat
**/
@FunctionalInterface
public interface IOUpdateHandler <T> {
	public T handleUpdate (final T oldValue) throws IOException;
}