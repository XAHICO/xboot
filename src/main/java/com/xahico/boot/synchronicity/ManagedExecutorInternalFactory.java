/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.synchronicity;

import java.util.concurrent.ExecutorService;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
interface ManagedExecutorInternalFactory {
	ExecutorService newInstance (final ManagedExecutor executor);
}