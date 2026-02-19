/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXRoute {
	public static GWXRoute parseFullString (final String s, final String separator) throws GWXInvalidRouteException {
		int          cursor = 0;
		final String path;
		final String root;
		final int    rootDelimiter;
		final int    version;
		final int    versionDelimiter;
		
		if (s.charAt(0) != '/') {
			throw new GWXInvalidRouteException("not relative");
		}
		
		rootDelimiter = s.indexOf('/', (cursor + 1));
		
		if (rootDelimiter == -1) {
			throw new GWXInvalidRouteException("no root delimiter");
		}
		
		cursor = rootDelimiter;
		
		versionDelimiter = s.indexOf('/', (cursor + 1));
		
		if (versionDelimiter == -1) {
			throw new GWXInvalidRouteException("no version delimiter");
		}
		
		if (! s.regionMatches(true, rootDelimiter, "/v", 0, 2)) {
			throw new GWXInvalidRouteException("invalid version");
		}
		
		cursor = versionDelimiter;
		
		version = Integer.parseInt(s.substring((rootDelimiter + 2), versionDelimiter));
		
		path = s.substring((cursor + 1), s.length());
		
		root = s.substring(0, rootDelimiter);
		
		return new GWXRoute(root, version, GWXPath.create(path));
	}
	
	public static GWXRoute parseMiniString (final String s, final String separator, final int version) throws GWXInvalidRouteException {
		return new GWXRoute(null, version, GWXPath.create(s));
	}
	
	public static GWXRoute parseSemiString (final String s, final String separator, final int version) throws GWXInvalidRouteException {
		int          cursor = 0;
		final String path;
		final String root;
		final int    rootDelimiter;
		
		if (s.charAt(0) != '/') {
			throw new GWXInvalidRouteException("not relative");
		}
		
		rootDelimiter = s.indexOf('/', (cursor + 1));
		
		if (rootDelimiter == -1) {
			throw new GWXInvalidRouteException("no root delimiter");
		}
		
		root = s.substring(0, rootDelimiter);
		
		cursor = rootDelimiter;
		
		path = s.substring((cursor + 1), s.length());
		
		return new GWXRoute(root, version, GWXPath.create(path));
	}
	
	
	
	public final GWXPath path;
	public final String  root;
	public final int     version;
	
	
	
	GWXRoute (final String root, final int version, final GWXPath path){
		super();
		
		this.root = root;
		this.version = version;
		this.path = path;
	}
	
	
	
	@Override
	public String toString (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append(this.root);
		sb.append("/");
		sb.append("v");
		sb.append(this.version);
		sb.append("/");
		sb.append(this.path);
		
		return sb.toString();
	}
}