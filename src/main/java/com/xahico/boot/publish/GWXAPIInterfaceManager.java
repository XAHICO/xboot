/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.platform.FileUtilities;
import com.xahico.boot.util.Exceptions;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXAPIInterfaceManager {
	private static final int    DISCARD_TIMEOUT_SECONDS = 5;
	private static final String INTERFACE_FILE_SUFFIX = ".py";
	private static final long   LV_CHECK_COOLDOWN_SECONDS = 5;
	
	
	
	private final Executor             executor;
	private final Set<GWXAPIInterface> interfaces = new CopyOnWriteArraySet<>();
	private final GWXResourceManager   resourceManager;
	private final File                 root;
	private final AtomicLong           versionCheck = new AtomicLong(-1);
	private final AtomicInteger        versionLatest = new AtomicInteger(-1);
	
	
	
	GWXAPIInterfaceManager (final GWXResourceManager resourceManager, final File root, final Executor executor){
		super();
		
		this.resourceManager = resourceManager;
		this.root = root;
		this.executor = executor;
	}
	
	
	
	public void destroy (){
		this.interfaces.forEach(iface -> iface.destroy());
		
		this.interfaces.clear();
	}
	
	public int detectLatestVersion (){
		try {
			final AtomicInteger lookupVersion;
			
			lookupVersion = new AtomicInteger(0);
			
			FileUtilities.walkDirectoryRaw(this.root, 1, (file) -> {
				final double version;
				final int    versionMajor;
				final String versionString;
				
				versionString = FileUtilities.getFileNameWithoutExtensions(file);
				
				if ((versionString.charAt(0) == 'v') && (versionString.length() > 1)) try {
					version = Double.parseDouble(versionString.substring(1));
					versionMajor = (int)(version);
					
					if (versionMajor > lookupVersion.get()) {
						lookupVersion.set(versionMajor);
					}
				} catch (final NumberFormatException ex) {
					Exceptions.ignore(ex);
				}
			});
			
			versionCheck.set(System.currentTimeMillis());
			
			if (lookupVersion.get() > versionLatest.get()) {
				versionLatest.set(lookupVersion.get());
			}
			
			return lookupVersion.get();
		} catch (final IOException ex) {
			return -1;
		}
	}
	
	private File getInterfacePath (final double version){
		return new File(this.root, "v%s%s".formatted((int)(version), INTERFACE_FILE_SUFFIX));
	}
	
	public int getLatestVersion (){
		final long callTime;
		
		callTime = System.currentTimeMillis();
		
		if ((versionLatest.get() != -1) && (callTime - versionCheck.get()) < (LV_CHECK_COOLDOWN_SECONDS * 1000)) {
			return versionLatest.get();
		} else {
			return this.detectLatestVersion();
		}
	}
	
	private GWXAPIInterface loadInterface (final double version){
		final GWXAPIInterface           iface;
		final GWXAPIInterface.Callbacks ifaceCallbacks;
		final File                      ifaceFile;
		
		ifaceCallbacks = new GWXAPIInterface.Callbacks() {
			@Override
			public void onRequire (final GWXAPIInterface iface){
				//System.out.println("Loaded Interface %d.%d".formatted((int)iface.version, iface.build));
			}
			
			@Override
			public void onRelease (final GWXAPIInterface iface){
				executor.execute(new Runnable() {
					final long dispatched = System.currentTimeMillis();
					
					@Override
					public void run (){
						if (iface.canDiscard()) {
							if ((System.currentTimeMillis() - dispatched) < (DISCARD_TIMEOUT_SECONDS * 1000)) {
								executor.execute(this);
							} else try {
								iface.destroy();
							} finally {
								interfaces.remove(iface);
								
								//System.out.println("Unloaded Interface %d.%d".formatted((int)iface.version, iface.build));
							}
						}
					}
				});
			}
		};
		
		ifaceFile = this.getInterfacePath(version);
		
		iface = new GWXAPIInterface(resourceManager, ifaceFile, version, ifaceCallbacks);
		
		interfaces.add(iface);
		
		return iface;
	}
	
	private GWXAPIInterface lookupInterface (final double version){
		GWXAPIInterface lookup = null;
		
		for (final var iface : this.interfaces) {
			if ((iface.version == version) && ((null == lookup) || (lookup.build < iface.build))) {
				lookup = iface;
			}
		}
		
		return lookup;
	}
	
	private long lookupInterfaceLatestBuild (final double version){
		final File ifaceFile;
		
		ifaceFile = this.getInterfacePath(version);
		
		if (! ifaceFile.exists()) 
			return 0;
		else {
			return ifaceFile.lastModified();
		}
	}
	
	public synchronized GWXAPIInterface requireInterface (final double version){
		GWXAPIInterface iface;
		
		iface = this.lookupInterface(version);
		
		if ((null == iface) || ((iface.build < this.lookupInterfaceLatestBuild(version)) || !iface.canRequire())) {
			iface = this.loadInterface(version);
			//System.out.println("requireInterface returned NEW INTERFACE");
		} else {
			//System.out.println("requireInterface returned OLD INTERFACE");
		}
		
		return iface;
	}
	
	public GWXAPIInterface requireInterface (final int version){
		return this.requireInterface((double)version);
	}
	
	public GWXAPIInterface requireInterfaceLatest (){
		return this.requireInterface(this.detectLatestVersion());
	}
}