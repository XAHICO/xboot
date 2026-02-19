/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.platform;

import com.xahico.boot.platform.PlatformFamily;
import com.xahico.boot.platform.PlatformModel;
import com.xahico.boot.platform.ProcessorArchitecture;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class PlatformUtilities {
	private static final PlatformFamily PLATFORM = detectPlatform();
	
	
	
	private static PlatformFamily detectPlatform (){
		final String pam;
		final String platformName;
		
		platformName = System.getProperty("os.name");
		
		pam = platformName.toLowerCase();
		
		if (pam.contains("linux")) 
			return PlatformFamily.LINUX;
		
		if (pam.contains("nix")) 
			return PlatformFamily.UNIX;
		
		if (pam.startsWith("win")) 
			return PlatformFamily.WINDOWS;
		
		throw new RuntimeException("Failed to determine platform");
	}
	
	public static String formatPlatformString (final PlatformFamily pf, final PlatformModel pm){
		final StringBuilder sb;
		
		if ((null == pf) && (null == pm)) 
			return "Unknown";
		
		sb = new StringBuilder();
		
		if (null != pf) {
			sb.append(pf.toString());
		}
		
		if (null != pm) {
			if (! sb.isEmpty()) {
				sb.append(',');
				sb.append(' ');
			}
			
			sb.append(pm.toString());
		}
		
		return sb.toString();
	}
	
	public static String getNativeExecutableExtension (){
		switch (getPlatform()) {
			case WINDOWS: {
				return ".exe";
			}
			default: {
				throw new Error();
			}
		}
	}
	
	public static PlatformFamily getPlatform (){
		return PLATFORM;
	}
	
	public static PlatformModel getPlatformModel (){
		final String pam;
		final String platformName;
		
		platformName = System.getProperty("os.name");
		
		pam = platformName.toLowerCase();
		
		if (pam.contains("win")) {
			if (pam.contains("vista")) 
				return PlatformModel.WINDOWS_VISTA;
			else if (pam.contains("xp")) 
				return PlatformModel.WINDOWS_XP;
			else if (pam.contains("10") && !pam.contains("2010")) 
				return PlatformModel.WINDOWS_10;
			else if (pam.contains("8.1")) 
				return PlatformModel.WINDOWS_8_1;
			else if (pam.contains("8.0")) 
				return PlatformModel.WINDOWS_8;
			else if (pam.contains("7")) 
				return PlatformModel.WINDOWS_7;
			else {
				return PlatformModel.WINDOWS_NT;
			}
		}
		
		return PlatformModel.UNKNOWN;
	}
	
	public static String getPlatformName (){
		try {
			final InetAddress localAddress;
			
			localAddress = InetAddress.getLocalHost();
			
			return localAddress.getHostName();
		} catch (final UnknownHostException ex) {
			final Map<String, String> systemEnvironment;
			
			systemEnvironment = System.getenv();
			
			if (systemEnvironment.containsKey("COMPUTERNAME"))
			    return systemEnvironment.get("COMPUTERNAME");
			else if (systemEnvironment.containsKey("HOSTNAME"))
			    return systemEnvironment.get("HOSTNAME");
			else {
			    return "Unknown";
			}
		}
	}
	
	public static ProcessorArchitecture getProcessorArchitecture (){
		final String par;
		final String processorArchitectur;
		
		processorArchitectur = System.getProperty("os.arch");
		
		par = processorArchitectur.toLowerCase();
		
		if (par.contains("amd64")) 
			return ProcessorArchitecture.AMD64;
		
		System.out.println("unknown: " + par);
		return ProcessorArchitecture.UNKNOWN;
	}
	
	/**
	 * Retrieves, in bytes, the total physical memory available on the 
	 * current machine. 
	 * 
	 * This is the total amount of RAM (Random Access Memory) and is not 
	 * to be confused for physical disk space.
	 * 
	 * @return 
	 * Total RAM on the current machine in bytes.
	**/
	public static long getTotalPhysicalMemory (){
		try {
			final Object      attribute;
			final ObjectName  attributeName;
			final Long        attributeValue;
			final MBeanServer mbs;
			
			mbs = ManagementFactory.getPlatformMBeanServer();
			
			attributeName = new ObjectName("java.lang", "type", "OperatingSystem");
			
			attribute = mbs.getAttribute(attributeName, "TotalPhysicalMemorySize");
			
			if (attribute instanceof Long) 
				attributeValue = (Long)(attribute);
			else {
				throw new InternalError();
			}
			
			return attributeValue;
		} catch (final AttributeNotFoundException | InstanceNotFoundException | MalformedObjectNameException | MBeanException | ReflectionException ex) {
			throw new InternalError(ex);
		}
	}
	
	
	
	private PlatformUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}