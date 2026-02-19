/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.javascript;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class JSCode extends JSObject {
	private static final char SYM_QUOTE_ESC= '\\';
	private static final char SYM_QUOTE1 = '\"';
	private static final char SYM_QUOTE2 = '\'';
	
	
	
	private String content;
	
	
	
	public JSCode (){
		this(null);
	}
	
	public JSCode (final String content){
		super();
		
		this.content = content;
	}
	
	
	
	public JSCode setContent (final String content){
		this.content = content;
		
		return this;
	}
	
	@Override
	public String toJavaScript (final int depthBase){
		int                 bracketDepth = 0;
		int                 depthQuote1 = 0;
		int                 depthQuote2 = 0;
		final StringBuilder lb;
		int                 lineDefinedBrackets = 0;
		final StringBuilder sb;
		boolean             withinCommentMultiLine = false;
		boolean             withinCommentSingleLine = false;
		boolean             withinQuote1 = false;
		boolean             withinQuote2 = false;
		
		lb = new StringBuilder();
		
		sb = new StringBuilder();
		
		for (var cursor = 0; cursor < this.content.length(); cursor++) {
			final char    c;
			final boolean withinComment;
			final boolean withinQuote;
			
			c = this.content.charAt(cursor);
			
			if (!withinQuote1 && !withinQuote2) {
				// Detect if within single-line comment: // ...
				if ((c == '/') && (cursor > 0) && (this.content.charAt(cursor - 1) == '/')) {
					withinCommentSingleLine = true;
				}

				if (withinCommentSingleLine) {
					if (c == '\n') {
						withinCommentSingleLine = false;
					}
				}
				
				// Detect if within multi-line comment: /* ... */
				if (! withinCommentSingleLine) {
					if ((c == '*') && (cursor > 0) && (this.content.charAt(cursor - 1) == '/')) {
						withinCommentMultiLine = true;
					}

					if (withinCommentMultiLine) {
						if ((c == '/') && (this.content.charAt(cursor - 1) == '*')) {
							withinCommentMultiLine = false;
						}
					}
				}
			}
			
			withinComment = (withinCommentSingleLine || withinCommentMultiLine);
			
			if (! withinComment) {
				// Detect if within QUOT1-Type String ("")
				if (c == SYM_QUOTE1) {
					int depth;

					if (withinQuote2) {
						continue;
					}

					depth = 0;

					for (var i = (cursor - 1); i > -1; i--) {
						if (this.content.charAt(i) == SYM_QUOTE_ESC) 
							depth++;
						else {
							break;
						}
					}

					if (withinQuote1) {
						if (depth == depthQuote1) {
							withinQuote1 = false;
							depthQuote1 = 0;
						}
					} else {
						withinQuote1 = true;
						depthQuote1 = depth;
					}
				}

				// Detect if within QUOT2-Type String ('')
				if (c == SYM_QUOTE2) {
					int depth;

					if (withinQuote1) {
						continue;
					}

					depth = 0;

					for (var i = (cursor - 1); i > -1; i--) {
						if (this.content.charAt(i) == SYM_QUOTE_ESC) 
							depth++;
						else {
							break;
						}
					}

					if (withinQuote2) {
						if (depth == depthQuote2) {
							withinQuote2 = false;
							depthQuote2 = 0;
						}
					} else {
						withinQuote2 = true;
						depthQuote2 = depth;
					}
				}
			}
			
			withinQuote = (withinQuote1 || withinQuote2);
			
			if (!withinComment && !withinQuote) {
				if (c == '\n') {
					sb.append(LTAB.repeat(depthBase + (bracketDepth - lineDefinedBrackets)));
					sb.append(lb.toString().strip());
					
					if ((cursor + 1) < this.content.length()) {
						sb.append(LINE);
					}
					
					lb.delete(0, lb.length());
					
					lineDefinedBrackets = 0;
					
					continue;
				}
				
				if (c == '{') {
					bracketDepth++;
					
					lineDefinedBrackets++;
				}

				if (c == '}') {
					bracketDepth--;
					
					if (lineDefinedBrackets > 0) {
						lineDefinedBrackets--;
					}
				}
				
				if (Character.isWhitespace(c) && lb.isEmpty()) {
					continue;
				}
			}
			
			lb.append(c);
		}
		
		return sb.toString();
	}
}