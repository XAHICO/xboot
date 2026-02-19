/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.html;

import java.util.function.Consumer;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class HTMLElement implements Cloneable {
	protected static final String LINE = "\n";
	protected static final String LTAB = "\t";
	
	
	
	private String   content = null;
	private HTMLNode parent;
	
	
	
	protected HTMLElement (){
		super();
		
		this.parent = null;
	}
	
	protected HTMLElement (final HTMLNode parent){
		super();
		
		this.parent = parent;
	}
	
	
	
	@Override
	@SuppressWarnings("CloneDeclaresCloneNotSupported")
	public HTMLElement clone (){
		try {
			return (HTMLElement)(super.clone());
		} catch (final CloneNotSupportedException ex) {
			throw new InternalError(ex);
		}
	}
	
	public abstract HTMLElement duplicate ();
	
	public String getContent (){
		return this.content;
	}
	
	public HTMLNode getParent (){
		return this.parent;
	}
	
	public boolean hasContent (){
		return (null != this.content);
	}
	
	public boolean hasParent (){
		return (null != this.parent);
	}
	
	public HTMLElement setContent (final String content){
		this.content = content;
		
		return HTMLElement.this;
	}
	
	public void setParent (final HTMLNode node){
		this.parent = node;
	}
	
	public final String toHTMLString (){
		return this.toHTMLString(0, true);
	}
	
	public final String toHTMLStringAbstracted (){
		return this.toHTMLString(-1, true);
	}
	
	public String toHTMLString (final int depth, final boolean safe){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		this.getContent().lines().forEach(new Consumer<>() {
			int     baseline = -1;
			boolean firstAdded = false;
			
			@Override
			public void accept (final String line){
				final int tabs;
				
				tabs = HTMLUtilities.calculatePaddingTabs(line);
				
				if ((baseline == -1) && !line.isBlank()) {
					baseline = tabs;
				}
				
				if (baseline != -1) {
					if (depth != -1) {
						if (! firstAdded) 
							firstAdded = true;
						else {
							sb.append(LINE);
						}
						
						sb.append(LTAB.repeat(depth + ((tabs > baseline) ? (tabs - baseline) : 0)));
						sb.append(line.strip());
					} else {
						if (! line.isBlank()) {
							sb.append(line.strip());
							sb.append(" ");
						}
					}
				}
			}
		});
		
		for (var i = (sb.length() - 1); i > -1; i--) {
			final char c;
			
			c = sb.charAt(i);
			
			if (! Character.isWhitespace(c)) {
				sb.delete((i + 1), sb.length());
				
				break;
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString (){
		return this.toHTMLString();
	}
}