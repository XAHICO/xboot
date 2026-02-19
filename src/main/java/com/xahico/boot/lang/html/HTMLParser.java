/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.html;

import com.xahico.boot.io.Source;
import com.xahico.boot.util.Exceptions;
import com.xahico.boot.util.StringUtilities;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class HTMLParser {
	private static final char SYM_QUOTE1 = '\"';
	private static final char SYM_QUOTE2 = '\'';
	private static final char SYM_QUOTE_ESC = '\\';
	private static final String SYN_COMMENT_LEFT = "<!--";
	private static final String SYN_COMMENT_RIGHT = "-->";
	private static final String SYN_SPECIAL_LEFT = "<?";
	private static final String SYN_SPECIAL_RIGHT = "?>";
	
	
	
	private static boolean isElementAutoClosing (final String element){
		final String           elementName;
		final HTMLStandardType elementType;
		
		elementName = parseElementName(element);
		
		if (elementName.equalsIgnoreCase(HTMLDocument.DOCUMENT_TAG)) 
			return true;
		
		elementType = HTMLStandardType.parseString(element);
		
		if (null == elementType) 
			return false;
		
		return !elementType.canContainElements();
	}
	
	private static boolean isElementCloser (final String element){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < element.length(); i++) {
			final char c;
			
			c = element.charAt(i);
			
			if (Character.isWhitespace(c)) {
				if (sb.isEmpty()) 
					continue;
				else {
					break;
				}
			}
			
			sb.append(c);
		}
		
		if (sb.length() < 3) 
			return false;
		
		return ((sb.charAt(0) == '<') && (sb.charAt(1) == '/') && (sb.charAt(sb.length() - 1) == '>'));
	}
	
	private static boolean isElementCloserFor (final String element, final String master){
		if (! isElementCloser(element)) 
			return false;
		else {
			return parseElementName(element).equals(master);
		}
	}
	
	private static boolean isElementComment (final String element){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < element.length(); i++) {
			final char c;
			
			c = element.charAt(i);
			
			if (Character.isWhitespace(c) && sb.isEmpty()) 
				continue;
			
			if (sb.isEmpty() && (c != SYN_COMMENT_LEFT.charAt(0))) 
				return false;
			
			if (sb.length() > SYN_COMMENT_LEFT.length()) 
				return false;
			
			if ((sb.length() == SYN_COMMENT_LEFT.length()) && (sb.substring(0, SYN_COMMENT_LEFT.length()).equals(SYN_COMMENT_LEFT))) {
				sb.delete(0, sb.length());
				
				break;
			}
			
			sb.append(c);
		}
		
		for (var i = (element.length() - 1); i > -1; i--) {
			final char c;
			
			c = element.charAt(i);
			
			if (Character.isWhitespace(c) && sb.isEmpty()) 
				continue;
			
			if (sb.isEmpty() && (c != SYN_COMMENT_RIGHT.charAt(SYN_COMMENT_RIGHT.length() - 1))) 
				break;
			
			if (sb.length() > SYN_COMMENT_RIGHT.length()) 
				break;
			
			if ((sb.length() == SYN_COMMENT_RIGHT.length()) && (sb.reverse().substring(0, SYN_COMMENT_RIGHT.length()).equals(SYN_COMMENT_RIGHT))) {
				sb.delete(0, sb.length());
				
				return true;
			}
			
			sb.append(c);
		}
		
		return false;
	}
	
	private static boolean isElementSelfClosing (final String element){
		if (element.length() < 3) 
			return false;
		
		if (element.charAt(element.length() - 1) != '>') 
			return false;
		
		for (var i = (element.length() - 2); i != 0; i--) {
			final char c;
			
			c = element.charAt(i);
			
			if (Character.isWhitespace(c)) 
				continue;
			
			if (c == '/') {
				return true;
			} else {
				break;
			}
		}
		
		return isElementAutoClosing(element);
	}
	
	private static String parseElementAsComment (final String element){
		int                 commentBegin = -1;
		int                 commentEnd = -1;
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < element.length(); i++) {
			final char c;
			
			c = element.charAt(i);
			
			if (Character.isWhitespace(c) && sb.isEmpty()) 
				continue;
			
			if (sb.isEmpty() && (c != SYN_COMMENT_LEFT.charAt(0))) 
				return null;
			
			if (sb.length() > SYN_COMMENT_LEFT.length()) 
				return null;
			
			if ((sb.length() == SYN_COMMENT_LEFT.length()) && (sb.substring(0, SYN_COMMENT_LEFT.length()).equals(SYN_COMMENT_LEFT))) {
				sb.delete(0, sb.length());
				
				commentBegin = i;
				
				break;
			}
			
			sb.append(c);
		}
		
		for (var i = (element.length() - 1); i > -1; i--) {
			final char c;
			
			c = element.charAt(i);
			
			if (Character.isWhitespace(c) && sb.isEmpty()) 
				continue;
			
			if (sb.isEmpty() && (c != SYN_COMMENT_RIGHT.charAt(SYN_COMMENT_RIGHT.length() - 1))) 
				return null;
			
			if (sb.length() > SYN_COMMENT_RIGHT.length()) 
				return null;
			
			if ((sb.length() == SYN_COMMENT_RIGHT.length()) && (sb.reverse().substring(0, SYN_COMMENT_RIGHT.length()).equals(SYN_COMMENT_RIGHT))) {
				sb.delete(0, sb.length());
				
				commentEnd = (i + 1);
				
				break;
			}
			
			sb.append(c);
		}
		
		if ((commentBegin == -1) || (commentEnd == -1)) 
			return null;
		
		return element.substring(commentBegin, commentEnd);
	}
	
	private static String parseElementAsSpecial (final String element){
		int                 commentBegin = -1;
		int                 commentEnd = -1;
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < element.length(); i++) {
			final char c;
			
			c = element.charAt(i);
			
			if (Character.isWhitespace(c) && sb.isEmpty()) 
				continue;
			
			if (sb.isEmpty() && (c != SYN_SPECIAL_LEFT.charAt(0))) 
				return null;
			
			if (sb.length() > SYN_SPECIAL_LEFT.length()) 
				return null;
			
			if ((sb.length() == SYN_SPECIAL_LEFT.length()) && (sb.substring(0, SYN_SPECIAL_LEFT.length()).equals(SYN_SPECIAL_LEFT))) {
				sb.delete(0, sb.length());
				
				commentBegin = i;
				
				break;
			}
			
			sb.append(c);
		}
		
		for (var i = (element.length() - 1); i > -1; i--) {
			final char c;
			
			c = element.charAt(i);
			
			if (Character.isWhitespace(c) && sb.isEmpty()) 
				continue;
			
			if (sb.isEmpty() && (c != SYN_SPECIAL_RIGHT.charAt(SYN_SPECIAL_RIGHT.length() - 1))) 
				return null;
			
			if (sb.length() > SYN_SPECIAL_RIGHT.length()) 
				return null;
			
			if ((sb.length() == SYN_SPECIAL_RIGHT.length()) && (sb.reverse().substring(0, SYN_SPECIAL_RIGHT.length()).equals(SYN_SPECIAL_RIGHT))) {
				sb.delete(0, sb.length());
				
				commentEnd = (i + 1);
				
				break;
			}
			
			sb.append(c);
		}
		
		if ((commentBegin == -1) || (commentEnd == -1)) 
			return null;
		
		return element.substring(commentBegin, commentEnd);
	}
	
	private static void parseElementAttributes (final String element, final Map<String, String> attributes){
		boolean             acceptedKey = false;
		int                 cursor = 0;
		int                 depthQuote1 = 0;
		int                 depthQuote2 = 0;
		boolean             elementStarted = false;
		final StringBuilder keyBuilder;
		final StringBuilder valBuilder;
		boolean             withinQuote1 = false;
		boolean             withinQuote2 = false;
		
		// Skip element name
		for (; cursor < element.length(); cursor++) {
			final char c;
			
			c = element.charAt(cursor);
			
			if (Character.isWhitespace(c)) {
				if (elementStarted) {
					break;
				}
			} else if (! elementStarted) {
				elementStarted = true;
			}
		}
		
		keyBuilder = new StringBuilder();
		valBuilder = new StringBuilder();
		
		for (; cursor < element.length(); cursor++) {
			final char    c;
			final boolean withinQuote;
			
			c = element.charAt(cursor);
			
			if (! acceptedKey) {
				// Read key
				if (Character.isWhitespace(c) && keyBuilder.isEmpty()) 
					continue;
				
				if (c == '=') {
					acceptedKey = true;
					
					continue;
				}
				
				keyBuilder.append(c);
			} else {
				// Read value
				if (Character.isWhitespace(c) && valBuilder.isEmpty()) 
					continue;
				
				// Detect if within QUOT1-Type String ("")
				if (c == SYM_QUOTE1) {
					int depth;

					if (withinQuote2) {
						continue;
					}

					depth = 0;
					
					for (var i = (cursor - 1); i > -1; i--) {
						if (element.charAt(i) == SYM_QUOTE_ESC) 
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
						if (element.charAt(i) == SYM_QUOTE_ESC) 
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
				
				withinQuote = (withinQuote1 || withinQuote2);
				
				if (!withinQuote && (Character.isWhitespace(c) || (c == '/') || (c == '>'))) {
					attributes.put(keyBuilder.toString(), StringUtilities.unquote(valBuilder.toString()));
					
					acceptedKey = false;
					
					keyBuilder.delete(0, keyBuilder.length());
					valBuilder.delete(0, valBuilder.length());
					
					continue;
				}
				
				valBuilder.append(c);
			}
		}
	}
	
	private static String parseElementName (final String element){
		final int           elementBegin;
		final int           elementEnd;
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < element.length(); i++) {
			final char c;
			
			c = element.charAt(i);
			
			if (Character.isWhitespace(c)) {
				if (sb.isEmpty()) 
					continue;
				else {
					break;
				}
			}
			
			sb.append(c);
		}
		
		if (sb.length() < 2) 
			return null;
		
		if (sb.charAt(0) != '<') 
			return null;
		
		if (sb.charAt(1) == '/') {
			elementBegin = 2;
		} else {
			elementBegin = 1;
		}
		
		if (sb.charAt(sb.length() - 1) == '>') 
			elementEnd = (sb.length() - 1);
		else {
			elementEnd = sb.length();
		}
		
		return sb.substring(elementBegin, elementEnd);
	}
	
	public static HTMLDocument parseString (final String string) throws HTMLException, IOException {
		final HTMLParser parser;
		
		parser = new HTMLParser();
		parser.setSource(Source.wrapString(string));
		parser.setTolerateGarbage(true);
		
		return parser.parse();
	}
	
	
	
	private Source              source = null;
	private boolean             tolerateGarbage = false;
	
	private int                 cursor = 0;
	private final StringBuilder history = new StringBuilder();
	private InputStream         stream = null;
	
	private int                 depthQuote1 = 0;
	private int                 depthQuote2 = 0;
	private boolean             withinQuote1 = false;
	private boolean             withinQuote2 = false;
	
	
	
	public HTMLParser (){
		super();
	}
	
	
	
	private void compact (final boolean retain){
		if (retain) {
			history.delete(0, (history.length() - 1));
			cursor = 1;
		} else {
			history.delete(0, history.length());
			cursor = 0;
		}
	}
	
	private void ensureReady (){
		if (null == this.source) {
			throw new Error("not mounted");
		}
	}
	
	public HTMLDocument parse () throws HTMLException, IOException {
		final HTMLDocument document;
		
		this.ensureReady();
		
		document = new HTMLDocument();
		
		try {
			this.source.open();
			
			this.stream = this.source.stream();
			
			cursor = 0;
			history.delete(0, history.length());
			
			try {
				parseNextElement(document, false);
			} catch (final EndOfDocumentException ex) {
				Exceptions.ignore(ex);
			}
			
			return document;
		} finally {
			this.source.close();
		}
	}
	
	private int parseContents (final HTMLNode master) throws HTMLException, IOException {
		String  elementBody = null;
		int     elementBodyBegin = -1;
		int     elementBodyEnd = -1;
		int     pointer = -1;
		
		for (;;) {
			final char c;
			boolean    enterQuote = false;
			boolean    withinQuote = false;
			
			c = this.read();
			
			history.append(c);

			// Detect if within QUOT1-Type String ("")
			if (c == SYM_QUOTE1) {
				int depth;

				if (withinQuote2) {
					cursor++;
					
					continue;
				}
				
				depth = 0;

				for (var i = (history.length() - 1); i > -1; i--) {
					if (history.charAt(i) == SYM_QUOTE_ESC) 
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
					enterQuote = true;
					withinQuote1 = true;
					depthQuote1 = depth;
				}
			}

			// Detect if within QUOT2-Type String ('')
			if (c == SYM_QUOTE2) {
				int depth;

				if (withinQuote1) {
					cursor++;
					
					continue;
				}

				depth = 0;

				for (var i = (history.length() - 1); i > -1; i--) {
					if (history.charAt(i) == SYM_QUOTE_ESC) 
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
					enterQuote = true;
					withinQuote2 = true;
					depthQuote2 = depth;
				}
			}

			if (! enterQuote) {
				withinQuote = (withinQuote1 || withinQuote2);
			}
			
			if (! withinQuote) {
				if (c == '<') {
					elementBodyBegin = cursor;
				} else if (c == '>') {
					if (elementBodyEnd == -1) {
						elementBodyEnd = (cursor + 1);
					}
				}
			}
			
			cursor++;
			
			if ((elementBodyBegin != -1) && (elementBodyEnd != -1)) {
				if (elementBodyBegin > elementBodyEnd) {
					elementBodyEnd = -1;
					
					continue;
				}
				
				elementBody = history.substring(elementBodyBegin, elementBodyEnd);
				
				if (isElementCloserFor(elementBody, master.getName())) {
					pointer = elementBodyBegin;
					
					break;
				} else {
					elementBody = null;
					elementBodyBegin = elementBodyEnd = -1;
				}
			}
		}
		
		return pointer;
	}
	
	private void parseNextElement (final HTMLNode master, boolean checkHistory) throws HTMLException, IOException {
		String  elementBody = null;
		int     elementBodyBegin = -1;
		int     elementBodyEnd = -1;
		boolean elementHasChildren = false;
		boolean elementIsCloser = false;
		boolean elementIsSelfClosing = false;
		String  elementName = null;
		boolean elementIsComment = false;
		boolean elementIsSpecial = false;
		boolean elementIsString = false;
		
		if (master.isWithLanguageContent()) {
			elementBodyBegin = cursor;
			
			elementBodyEnd = parseContents(master);
			
			elementBody = history.substring(elementBodyBegin, elementBodyEnd);
			
			compact(false);
			
			master.setContent(elementBody.isBlank() ? null : elementBody);
			
			return;
		}
		
		for (;;) {
			final char c;
			boolean    enterQuote = false;
			boolean    withinQuote = false;

			if (checkHistory) {
				checkHistory = false;
				
				c = history.charAt(history.length() - 1);
			} else {
				c = this.read();
			}
			
			history.append(c);

			// Detect if within QUOT1-Type String ("")
			if (!elementIsComment && (c == SYM_QUOTE1)) {
				int depth;

				if (withinQuote2) {
					cursor++;
					
					continue;
				}

				depth = 0;

				for (var i = (history.length() - 1); i > -1; i--) {
					if (history.charAt(i) == SYM_QUOTE_ESC) 
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
					enterQuote = true;
					withinQuote1 = true;
					depthQuote1 = depth;
				}
			}

			// Detect if within QUOT2-Type String ('')
			if (!elementIsComment && (c == SYM_QUOTE2)) {
				int depth;

				if (withinQuote1) {
					cursor++;
					
					continue;
				}

				depth = 0;

				for (var i = (history.length() - 1); i > -1; i--) {
					if (history.charAt(i) == SYM_QUOTE_ESC) 
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
					enterQuote = true;
					withinQuote2 = true;
					depthQuote2 = depth;
				}
			}

			if (! enterQuote) {
				withinQuote = (withinQuote1 || withinQuote2);
			}

			if (! withinQuote) {
				if (c == '<') {
					if (elementIsComment) {
						continue;
					} else if (elementIsString) {
						elementBodyEnd = cursor;
					} else if ((elementBodyBegin != -1)) {
						throw new Error();
					} else {
						elementBodyBegin = cursor;
					}
				} else if (c == '>') {
					if (elementBodyEnd != -1) {
						throw new Error();
					} else if (elementIsComment) {
						if ((history.length() >= SYN_COMMENT_RIGHT.length()) && history.substring(history.length() - SYN_COMMENT_RIGHT.length()).equals(SYN_COMMENT_RIGHT)) {
							elementBodyEnd = (cursor + 1);
						}
					} else if (elementIsSpecial) {
						if ((history.length() >= SYN_SPECIAL_RIGHT.length()) && history.substring(history.length() - SYN_SPECIAL_RIGHT.length()).equals(SYN_SPECIAL_RIGHT)) {
							elementBodyEnd = (cursor + 1);
						}
					} else {
						elementBodyEnd = (cursor + 1);
					}
				} else if ((c == SYN_COMMENT_LEFT.charAt(SYN_COMMENT_LEFT.length() - 1)) && !elementIsString) {
					if ((history.length() >= SYN_COMMENT_LEFT.length()) && history.substring(history.length() - SYN_COMMENT_LEFT.length()).equals(SYN_COMMENT_LEFT)) {
						elementIsComment = true;
					}
				} else if ((c == SYN_SPECIAL_LEFT.charAt(SYN_SPECIAL_LEFT.length() - 1)) && !elementIsString) {
					if ((history.length() >= SYN_SPECIAL_LEFT.length()) && history.substring(history.length() - SYN_SPECIAL_LEFT.length()).equals(SYN_SPECIAL_LEFT)) {
						elementIsSpecial = true;
					}
				} else {
					if ((elementBodyBegin == -1) && !Character.isWhitespace(c) && !master.isRoot()) {
						elementBodyBegin = cursor;
						elementIsString = true;
					}
				}
			}
			
			cursor++;

			if ((elementBodyBegin != -1) && (elementBodyEnd != -1)) {
				elementBody = history.substring(elementBodyBegin, elementBodyEnd);
				
				if (elementIsComment) {
					final HTMLComment element;
					
					compact(false);
					
					element = new HTMLComment();
					element.setContent(parseElementAsComment(elementBody));
					
					master.addChild(element);
					
					parseNextElement(master, false);
				} else if (elementIsSpecial) {
					final HTMLSpecialElement element;
					
					compact(false);
					
					element = new HTMLSpecialElement();
					element.setContent(parseElementAsSpecial(elementBody));
					
					master.addChild(element);
					
					parseNextElement(master, false);
				} else if (elementIsString) {
					final HTMLString element;
					
					compact(true);
					
					element = new HTMLString();
					element.setContent(elementBody);
					
					master.addChild(element);
					
					parseNextElement(master, true);
				} else {
					compact(false);
					
					elementIsCloser = isElementCloser(elementBody);
					elementIsSelfClosing = isElementSelfClosing(elementBody);
					elementName = parseElementName(elementBody);
					
					if (elementIsSelfClosing || !elementIsCloser) {
						final HTMLNode element;
						
						element = new HTMLNode();
						element.setName((elementIsSelfClosing && elementName.endsWith("/")) ? elementName.substring(0, elementName.length() - 1) : elementName);
						
						parseElementAttributes(elementBody, element.getAttributes());
						
						if (! element.isDocumentTag()) {
							master.addChild(element);
						}

						if (!elementIsCloser && !elementIsSelfClosing) {
							elementHasChildren = true;
						}

						if (elementHasChildren) {
							parseNextElement(element, elementIsString);
						}
					} else {
						if (elementIsCloser) {
							if (master.getName().equals(elementName)) {
								break;
							}
						}
					}
					
					parseNextElement(master, false);
				}
				
				break;
			}
		}
	}
	
	private char read () throws EndOfDocumentException, IOException {
		final int  b;
		final char c;
		
		b = this.stream.read();
		
		if (b == -1) {
			throw new EndOfDocumentException();
		}
		
		c = (char)(b);
		
		return c;
	}
	
	public HTMLParser setSource (final Source source){
		this.source = source;
		
		return this;
	}
	
	public HTMLParser setTolerateGarbage (final boolean tolerateGarbage){
		this.tolerateGarbage = tolerateGarbage;
		
		return this;
	}
}