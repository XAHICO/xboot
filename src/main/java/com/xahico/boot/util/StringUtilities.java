/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.util;

import com.xahico.boot.util.transformer.StringObjectConstructor;
import com.xahico.boot.util.transformer.ObjectStringifier;
import com.xahico.boot.util.transformer.StringTransformer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class StringUtilities {
	public static final String GENERATOR_ASCII_ALPHABET;
	public static final String GENERATOR_ASCII_ALPHABET_LOWER;
	public static final String GENERATOR_ASCII_ALPHABET_UPPER;
	public static final String GENERATOR_ASCII_ALPHANUMERIC;
	public static final String GENERATOR_ASCII_DIGIT;
	
	private static final int INTEGER_MAX_LENGTH;
	private static final int LONG_MAX_LENGTH;
	
	private static final char   SYM_QUOTE_ALT_CH = '\'';
	private static final String SYM_QUOTE_ALT_STR = "\'";
	private static final char   SYM_QUOTE_ESC_CH = '\\';
	private static final String SYM_QUOTE_ESC_STR = "\\";
	private static final char   SYM_QUOTE_REG_CH = '\"';
	private static final String SYM_QUOTE_REG_STR = "\"";
	
	
	
	static {
		int imaxmax;
		int imixmax;
		
		GENERATOR_ASCII_ALPHABET_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		GENERATOR_ASCII_ALPHABET_LOWER = "abcdefghijklmnopqrstuvwxyz";
		GENERATOR_ASCII_ALPHABET = (GENERATOR_ASCII_ALPHABET_UPPER + GENERATOR_ASCII_ALPHABET_LOWER);
		GENERATOR_ASCII_DIGIT = "0123456789";
		GENERATOR_ASCII_ALPHANUMERIC = (GENERATOR_ASCII_ALPHABET + GENERATOR_ASCII_DIGIT);

		imaxmax = Integer.toString(Integer.MAX_VALUE).length();
		imixmax = Integer.toString(Integer.MIN_VALUE).length();
		
		INTEGER_MAX_LENGTH = ((imaxmax > imixmax) ? imaxmax : imixmax);
		
		imaxmax = Long.toString(Long.MAX_VALUE).length();
		imixmax = Long.toString(Long.MIN_VALUE).length();
		
		LONG_MAX_LENGTH = ((imaxmax > imixmax) ? imaxmax : imixmax);
	}
	
	
	
	public static String buildString (final List<? extends Object> objectList, final String separator){
		final Iterator<? extends Object> it;
		final StringBuilder              sb;
		
		sb = new StringBuilder();
		
		it = objectList.iterator();
		
		while (it.hasNext()) {
			final Object item;
			
			item = it.next();
			
			sb.append(item);
			
			if (it.hasNext()) {
				sb.append(separator);
			}
		}
		
		return sb.toString();
	}
	
	public static String buildString (final List<? extends Object> objectList, final String separator, final ObjectStringifier transformer){
		final Iterator<? extends Object> it;
		final StringBuilder              sb;
		
		sb = new StringBuilder();
		
		it = objectList.iterator();
		
		while (it.hasNext()) {
			final Object item;
			
			item = transformer.call(it.next());
			
			sb.append(item);
			
			if (it.hasNext()) {
				sb.append(separator);
			}
		}
		
		return sb.toString();
	}
	
	public static byte[] bytes (final String string, final Charset charset, final boolean nullTerminated){
		final byte[] bytes;
		final byte[] bytesOriginal;
		final int    charSize;
		
		if (! nullTerminated) 
			return string.getBytes(charset);
		
		charSize = StringUtilities.charSize(charset);
		
		bytesOriginal = string.getBytes(charset);
		bytes = Arrays.copyOf(bytesOriginal, (bytesOriginal.length + charSize));
		
		for (var i = 0; i < charSize; i++) {
			bytes[bytesOriginal.length + i] = 0x0;
		}
		
		return bytes;
	}
	
	public static double calculateContainsInordinal (final String string2, final String string1, final double awardIgnoreCase){
		double ldiff;
		double multi = 1.0;
		double score = 0.0;
		
		if (string2.length() > string1.length()) 
			ldiff = (((double)string1.length()) / ((double)string2.length()));
		else if (string1.length() > string2.length()) 
			ldiff = (((double)string2.length()) / ((double)string1.length()));
		else {
			ldiff = 1.0;
		}
		
		for (int i1 = 0, i2 = 0;; i2++) {
			final char c1;
			final char c2;
			
			if (!(i1 < string1.length())) {
				break;
			}
			
			if (!(i2 < string2.length())) {
				break;
			}
			
			c1 = string1.charAt(i1);
			c2 = string2.charAt(i2);
			
			if (c1 == c2) {
				score += (1.0 * multi);
				
				i1++;
				
				multi = 1.0;
			} else if (Character.toLowerCase(c1) == Character.toLowerCase(c2)) {
				score += ((awardIgnoreCase != 0.0) ? (awardIgnoreCase * multi) : 0.0);
				
				i1++;
				
				multi = awardIgnoreCase;
			} else {
				multi /= (1.0 + (((double)(i2 + 1)) / ((double)string2.length())));
			}
		}
		
		return ((score / ((double)string1.length())) * ldiff);
	}
	
	public static double calculateEquals (final String string1, final String string2){
		return calculateEquals(string1, string2, false);
	}
	
	public static double calculateEquals (final String string1, final String string2, final boolean awardIgnoreCase){
		final double clen;
		double       score = 0.0;
		
		for (var i = 0; i < string1.length(); i++) {
			final char c1;
			final char c2;
			
			c1 = string1.charAt(i);
			
			if (!(i < string2.length())) 
				break;
			
			c2 = string2.charAt(i);
			
			if (c1 == c2) 
				score += 1.0;
			else if (awardIgnoreCase && (Character.toLowerCase(c1) == Character.toLowerCase(c2))) 
				score += 0.5;
			else {
				break;
			}
		}
		
		clen = ((((double)string1.length()) + ((double)string2.length())) / 2.0);
		
		return (score / clen);
	}
	
	public static double calculateEqualsAnagram (final String word, final String anagram, final double awardIgnoreCase){
		int    match = 0;
		String wordx = word;
		
		for (var i = 0; i < anagram.length(); i++) {
			char c;
			int  p;
			
			c = anagram.charAt(i);
			
			p = wordx.indexOf(c);
			
			if (p != -1) {
				match += 1;
				
				wordx = removeFirst(wordx, c, false);
				
				continue;
			}
			
			p = StringUtilities.findIndexOf(wordx, c, 0, true);
			
			if (p != -1) {
				match += awardIgnoreCase;
				
				wordx = removeFirst(wordx, c, true);
				
				continue;
			}
		}
		
		return (((double)match) / ((double)word.length()));
	}
	
	public static double calculateEqualsIgnoreCase (final String string1, final String string2){
		final double clen;
		double       score = 0.0;
		
		for (var i = 0; i < string1.length(); i++) {
			final char c1;
			final char c2;
			
			c1 = string1.charAt(i);
			
			if (!(i < string2.length())) 
				break;
			
			c2 = string2.charAt(i);
			
			if (Character.toLowerCase(c1) == Character.toLowerCase(c2)) 
				score += 1.0;
			else {
				break;
			}
		}
		
		clen = ((((double)string1.length()) + ((double)string2.length())) / 2.0);
		
		return (score / clen);
	}
	
	public static double calculateEqualsInordinal (final String word, final String other, final double awardIgnoreCase){
		int    lhigh;
		double match = 0.0;
		
		for (int i1 = 0, i2 = 0;; i1++, i2++) {
			char c1, c2;
			
			if (!(i1 < other.length())) 
				break;
			
			if (!(i1 < word.length())) 
				break;
			
			c1 = word.charAt(i1);
			c2 = other.charAt(i2);
			
			if (c1 == c2) {
				match += 1.0;
				
				continue;
			}
			
			if (Character.toLowerCase(c1) == Character.toLowerCase(c2)) {
				match += awardIgnoreCase;
				
				continue;
			}
			
			for (var j = i1; j < other.length(); j++) {
				c2 = other.charAt(j);
				
				if (!Character.isAlphabetic(c1) && (Character.isAlphabetic(c2) || Character.isDigit(c2))) {
					if ((i1 + 1) < word.length()) {
						i1++;
						
						c1 = word.charAt(i1);
					} else {
						break;
					}
				}
				
				if (c1 == c2) {
					match += (1.0 / Math.pow(2.0, ((double)(1 + (j - i1)))));
					
					break;
				}
				
				if (Character.toLowerCase(c1) == Character.toLowerCase(c2)) {
					match += (awardIgnoreCase / Math.pow(2.0, ((double)(1 + (j - i1)))));
					
					break;
				}
			}
		}
		
		if (word.length() > other.length()) 
			lhigh = word.length();
		else {
			lhigh = other.length();
		}
		
		return (((double)match) / ((double)lhigh));
	}
	
	public static double calculateEqualsInordinalDuplex (final String word, final String other, final double awardIgnoreCase){
		final double finax;
		final double highs;
		final double ldiff;
		final double means;
		final double score1;
		final double score2;
		
		score1 = calculateEqualsInordinal(word, other, awardIgnoreCase);
		
		if (score1 == 1.0) 
			return score1;
		
		score2 = calculateEqualsInordinal(other, word, awardIgnoreCase);
		
		if (score2 == 1.0) 
			return score2;
		
		if (score2 > score1) 
			highs = (score2 - score1);
		else {
			highs = (score1 - score2);
		}
		
		if (word.length() > other.length()) 
			ldiff = ((double)other.length()) / ((double)word.length());
		else {
			ldiff = ((double)word.length()) / ((double)other.length());
		}
		
		means = ((score1 + score2) / 2.0);
		
		finax = ((means + (highs / 2.0)) * ldiff);
		
		return finax;
	}
	
	public static String capitalize (final String string){
		return capitalize(string, true);
	}
	
	public static String capitalize (final String string, final boolean lowerOther){
		if (string.length() == 0) 
			return string;
		else if (string.length() == 1) 
			return string.toUpperCase();
		else {
			if (lowerOther) 
				return (Character.toUpperCase(string.charAt(0)) + string.toLowerCase().substring(1));
			else {
				return (Character.toUpperCase(string.charAt(0)) + string.substring(1));
			}
		}
	}
	
	public static String capitalizeWords (final String string, final boolean lowerOther){
		final Iterator<String> it;
		final StringBuilder    sb;
		
		sb = new StringBuilder();
		
		it = splitStringIntoWordIterator(string);
		
		while (it.hasNext()) {
			final String word;
			
			word = capitalize(it.next(), lowerOther);
			
			sb.append(word);
			
			if (it.hasNext()) {
				sb.append(" ");
			}
		}
		
		return sb.toString();
	}
	
	public static int charSize (final Charset charset){
		if (charset == StandardCharsets.UTF_8) 
			return 1;
		else if ((charset == StandardCharsets.UTF_16) || (charset == StandardCharsets.UTF_16BE) || (charset == StandardCharsets.UTF_16LE)) 
			return 2;
		else {
			throw new UnsupportedOperationException("not supported yet: char conversion to %s not implemented".formatted(charset));
		}
	}
	
	public static boolean containsAnagram (final String word, final String anagram, final boolean ignoreCase){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static boolean endsWithIgnoreCase (final String string1, final String string2){
		if (string2.length() > string1.length()) 
			return false;
		else {
			return string1.regionMatches(true, (string1.length() - string2.length()), string2, 0, string2.length());
		}
	}
	
	public static int findIndexOf (final String string, final char c, final int fromIndex, final boolean ignoreCase){
		for (var i = fromIndex; i < string.length(); i++) {
			if ((string.charAt(i) == c) || (ignoreCase && (Character.toLowerCase(string.charAt(i)) == Character.toLowerCase(c)))) {
				return i;
			}
		}
		
		return -1;
	}
	
	public static char getFirstSharedCharacter (final String s1, final String s2, final int fromIndex, final boolean ignoreCase){
		for (var i = 0; i < s1.length(); i++) {
			final char c;
			
			c = s1.charAt(i);
			
			if (s2.indexOf(c, fromIndex) != -1) {
				return c;
			}
			
			if (ignoreCase) {
				if (Character.isUpperCase(c)) {
					if (s2.indexOf(Character.toLowerCase(c), fromIndex) != -1) {
						return Character.toLowerCase(c);
					}
				} else {
					if (s2.indexOf(Character.toUpperCase(c), fromIndex) != -1) {
						return Character.toUpperCase(c);
					}
				}
			}
		}
		
		return '\0';
	}
	
	public static char getLastSharedCharacter (final String s1, final String s2, final int fromIndex, final boolean ignoreCase){
		for (var i = 0; i < s1.length(); i++) {
			final char c;
			
			c = s1.charAt(i);
			
			if (s2.lastIndexOf(c, fromIndex) != -1) {
				return c;
			}
			
			if (ignoreCase) {
				if (Character.isUpperCase(c)) {
					if (s2.lastIndexOf(Character.toLowerCase(c), fromIndex) != -1) {
						return Character.toLowerCase(c);
					}
				} else {
					if (s2.lastIndexOf(Character.toUpperCase(c), fromIndex) != -1) {
						return Character.toUpperCase(c);
					}
				}
			}
		}
		
		return '\0';
	}
	
	public static boolean isAnagramOf (final String anagram, String ofWord, final boolean ignoreCase){
		if (anagram.length() != ofWord.length()) 
			return false;
		
		if (ignoreCase) 
			ofWord = ofWord.toLowerCase();
		
		for (var i = 0; i < anagram.length(); i++) {
			final char c;
			final int  p;
			
			if (ignoreCase) 
				c = Character.toLowerCase(anagram.charAt(i));
			else {
				c = anagram.charAt(i);
			}
			
			p = ofWord.indexOf(c);
			
			if (p == -1) 
				return false;
			
			ofWord = removeFirst(ofWord, c, ignoreCase);
		}
		
		return true;
	}
	
	public static boolean isBoolean (final String string){
		return (string.equalsIgnoreCase("true") || string.equalsIgnoreCase("false"));
	}
	
	public static boolean isBooleanInteger (final String string){
		return (string.equals("1") || string.equals("0"));
	}
	
	public static boolean isDouble(String s) {
		if (s == null || s.isEmpty()) return false;
		s = s.trim();
		if (s.equalsIgnoreCase("NaN") || s.equalsIgnoreCase("Infinity") || s.equalsIgnoreCase("-Infinity") || s.equalsIgnoreCase("+Infinity"))
		    return true;
		int dotCount = 0;
		int eCount = 0;
		int start = 0;
		if (s.charAt(0) == '+' || s.charAt(0) == '-') start = 1;
		boolean digitSeen = false;
		for (int i = start; i < s.length(); i++) {
		    char c = s.charAt(i);
		    if (Character.isDigit(c)) digitSeen = true;
		    else if (c == '.') {
			  if (dotCount++ > 0) return false;
		    } else if (c == 'e' || c == 'E') {
			  if (eCount++ > 0 || !digitSeen) return false;
			  i++;
			  if (i < s.length() && (s.charAt(i) == '+' || s.charAt(i) == '-')) i++;
			  if (i >= s.length()) return false;
			  for (; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return false;
			  return true;
		    } else return false;
		}
		
		return digitSeen;
	}
	
	public static boolean isInteger (final String s){
		int i = 0;
		
		if ((s == null) || s.isEmpty()) 
			return false;
		
		if (s.length() > INTEGER_MAX_LENGTH) 
			return false;
		
		return isNumber(s);
	}
	
	public static boolean isLong (final String s){
		int i = 0;
		
		if ((s == null) || s.isEmpty()) 
			return false;
		
		if (s.length() > LONG_MAX_LENGTH) 
			return false;
		
		return isNumber(s);
	}
	
	public static boolean isMultiWord (final String string){
		boolean firstWordFound = false;
		boolean secondWordFound = false;
		boolean separatorFound = false;
		
		for (var i = 0; i < string.length(); i++) {
			final char c;
			
			c = string.charAt(i);
			
			if (!firstWordFound && !Character.isWhitespace(c)) {
				firstWordFound = true;
				
				continue;
			}
			
			if (firstWordFound && !separatorFound && Character.isWhitespace(c)) {
				separatorFound = true;
				
				continue;
			}
			
			if (!secondWordFound && separatorFound && !Character.isWhitespace(c)) {
				secondWordFound = true;
				
				continue;
			}
		}
		
		return (firstWordFound && secondWordFound);
	}
	
	public static boolean isNumber (final String s){
		int i = 0;
		
		if ((s == null) || s.isEmpty()) 
			return false;
		
		if ((s.charAt(0) == '-') || (s.charAt(0) == '+')) 
			i = 1;
		
		if (i == s.length()) 
			return false;
		
		for (; i < s.length(); i++) {
			if (! Character.isDigit(s.charAt(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isQuoted (final String string){
		return (string.startsWith(SYM_QUOTE_REG_STR) && string.endsWith(SYM_QUOTE_REG_STR));
	}
	
	public static int lengthOf (final String string){
		if (null != string) 
			return string.length();
		else {
			return 0;
		}
	}
	
	public static List<String> parseConnectedWords (final String name, final int minLength){
		final List<String>  collection;
		final StringBuilder sb;
		
		collection = new ArrayList<>();
		
		sb = new StringBuilder();
		
		if (name.length() > 0) {
			for (var i = 0; i < minLength; i++) {
				sb.append(name.charAt(i));
			}
			
			for (var i = minLength; i < name.length(); i++) {
				final char cc;
				final char cl;
				
				cc = name.charAt(i);
				cl = name.charAt(i - 1);
				
				if (Character.isUpperCase(cc) == Character.isUpperCase(cl)) {
					sb.append(cc);
				} else {
					if (sb.length() < minLength) {
						sb.append(cc);
						
						continue;
					}
					
					if (sb.toString().equals(sb.toString().toUpperCase()) || sb.toString().equals(sb.toString().toLowerCase())) {
						collection.add(sb.substring(0, (sb.length() - 1)));
						
						sb.delete(0, sb.length() - 1);
						
						sb.append(cc);
						
						continue;
					}
					
					collection.add(sb.toString());
					
					sb.delete(0, sb.length());
					
					sb.append(cc);
				}
			}
			
			collection.add(sb.toString());
		}
		
		return collection;
	}
	
	public static String quote (final String string){
		return quote(string, true);
	}
	
	public static String quote (final String string, final boolean strict){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		if (strict) {
			sb.append(SYM_QUOTE_REG_CH);
		}
		
		for (var i = 0; i < string.length(); i++) {
			final char c;
			
			c = string.charAt(i);
			
			if (c == SYM_QUOTE_REG_CH) {
				sb.append(SYM_QUOTE_ESC_CH);
			}
			
			sb.append(c);
		}
		
		if (strict) {
			sb.append(SYM_QUOTE_REG_CH);
		}
		
		return sb.toString();
	}
	
	public static String random (final String generator, final int length){
		final SecureRandom  random;
		final StringBuilder sb;
		
		random = new SecureRandom();
		
		sb = new StringBuilder();
		
		for (var i = 0; i < length; i++) {
			final char c;
			final int  p;
			
			p = random.nextInt(generator.length());
			
			c = generator.charAt(p);
			
			sb.append(c);
		}
		
		return sb.toString();
	}
	
	public static String randomAlphabetic (final int length){
		final Random        rand;
		final StringBuilder sb;
		
		rand = new Random(System.currentTimeMillis());
		
		sb = new StringBuilder();
		
		for (var i = 0; i < length; i++) {
			final char c;
			
			c = (char)(rand.nextInt('z' - 'A') + 'A');
			
			if (Character.isAlphabetic(c)) 
				sb.append(c);
			else {
				i--;
			}
		}
		
		return sb.toString();
	}
	
	public static String removeFirst (final String string, final char removeChar, final boolean ignoreCase){
		final int           position;
		final StringBuilder sb;
		
		position = StringUtilities.findIndexOf(string, removeChar, 0, ignoreCase);
		
		if (position == -1) 
			return string;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < string.length(); i++) {
			final char c;
			
			c = string.charAt(i);
			
			if (i != position) {
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	public static String reverse (final String string){
		return new StringBuilder(string).reverse().toString();
	}
	
	public static void splitString (final String string, final String separator, final boolean ignoreWhitespace, final Consumer<String> callback){
		int cursor = 0;
		int itemBegin = 0;
		int itemEnd;
		
		for (;;) {
			String    item;
			final int separatorIndex;
			
			cursor = separatorIndex = string.indexOf(separator, cursor);
			cursor++;
			
			if (separatorIndex == -1) 
				itemEnd = string.length();
			else {
				itemEnd = separatorIndex;
			}
			
			item = string.substring(itemBegin, itemEnd);
			
			if (ignoreWhitespace) {
				item = item.strip();
			}
			
			if (! item.isEmpty()) {
				callback.accept(item);
			}
			
			if (itemEnd == string.length()) {
				break;
			} else {
				itemBegin = (itemEnd + 1);
			}
		}
	}
	
	public static String[] splitString (final String string, final String separator, final boolean ignoreWhitespace){
		final List<String> collection;
		
		collection = new LinkedList<>();
		
		splitString(string, separator, ignoreWhitespace, (substring) -> collection.add(substring));
		
		return collection.toArray(new String[collection.size()]);
	}
	
	public static void splitStringByWhitespace (final String string, final Consumer<String> callback){
		for (final var word : string.split("\\s+")) {
			callback.accept(word);
		}
	}
	
	public static Iterator<String> splitStringByWhitespaceIntoIterator (final String string){
		return new Iterator<>() {
			int                 cursor = 0;
			final StringBuilder sb = new StringBuilder();
			
			@Override
			public boolean hasNext (){
				while (cursor < string.length()) {
					final char c;
					
					c = string.charAt(cursor);
					
					if (Character.isWhitespace(c)) {
						if (! sb.isEmpty()) {
							break;
						}
					} else {
						sb.append(c);
					}
					
					cursor++;
				}
				
				return !sb.isEmpty();
			}
			
			@Override
			public String next (){
				final String element;
				
				element = sb.toString();
				
				sb.delete(0, sb.length());
				
				cursor++;
				
				return element;
			}
		};
	}
	
	public static List<String> splitStringByWhitespaceIntoList (final String string){
		final List<String> collection;
		
		collection = new ArrayList<>();
		
		splitStringByWhitespace(string, (word) -> collection.add(word));
		
		return collection;
	}
	
	public static List<String> splitStringByWhitespaceIntoList (final String string, final StringTransformer transformer){
		final List<String> collection;
		
		collection = new ArrayList<>();
		
		splitStringByWhitespace(string, (word) -> collection.add(transformer.call(word)));
		
		return collection;
	}
	
	public static Iterator<String> splitStringIntoItemIterator (final String string, final char separator){
		return new Iterator<>() {
			int     cursor = 0;
			int     ownDepth = 0;
			boolean withinQuote = false;
			int     wordBegin = 0;
			int     wordEnd = -1;
			
			@Override
			public boolean hasNext (){
				if (cursor > string.length()) 
					return false;
				
				for (; cursor < string.length(); cursor++) {
					final char c;
					
					c = string.charAt(cursor);
					
					if (!withinQuote && (c == separator)) {
						if ((cursor - wordBegin) > 0) 
							break;
						else {
							wordBegin++;
						}
					} else if (c == SYM_QUOTE_REG_CH) {
						int depth;
						
						depth = 0;

						for (var i = (cursor - 1); i > -1; i--) {
							if (string.charAt(i) == SYM_QUOTE_ESC_CH) 
								depth++;
							else {
								break;
							}
						}
						
						if (withinQuote) {
							if (depth == ownDepth) {
								withinQuote = false;
								ownDepth = 0;
//								cursor++;
							}
						} else {
							withinQuote = true;
							ownDepth = depth;
						}
					}
				}
				
				wordEnd = cursor;
				
				return true;
			}
			
			@Override
			public String next (){
				final String element;
				
				cursor++;
				
				element = string.substring(wordBegin, wordEnd).strip();
				
				wordBegin = wordEnd + 1;
				wordEnd = -1;
				
				return element;
			}
		};
	}
	
	public static List<String> splitStringIntoItemList (final String string, final char separator){
		final List<String> collection;
		
		collection = new ArrayList<>();
		
		splitStringIntoItems(string, separator, (item) -> collection.add(item));
		
		return collection;
	}
	
	public static List<String> splitStringIntoItemList (final String string, final char separator, final StringTransformer transformer){
		final List<String> collection;
		
		collection = new ArrayList<>();
		
		splitStringIntoItems(string, separator, (item) -> collection.add(transformer.call(item)));
		
		return collection;
	}
	
	public static void splitStringIntoItems (final String string, final char separator, final Consumer<String> callback){
		final Iterator<String> it;
		
		it = splitStringIntoItemIterator(string, separator);
		
		while (it.hasNext()) {
			callback.accept(it.next());
		}
	}
	
	public static Iterator<String> splitStringIntoIterator (final String string, final String separator, final boolean ignoreWhitespace){
		return new Iterator<>() {
			int    cursor = 0;
			String item = null;
			int    itemBegin = 0;
			int    itemEnd;
			
			@Override
			public boolean hasNext (){
				final int separatorIndex;
				
				if (cursor > string.length()) {
					return false;
				}
				
				separatorIndex = string.indexOf(separator, cursor);
				
				if (cursor == separatorIndex) 
					return true;
				
				cursor = separatorIndex;
				
				if (separatorIndex == -1) 
					itemEnd = string.length();
				else {
					itemEnd = separatorIndex;
				}
				
				item = string.substring(itemBegin, itemEnd);
				
				if (ignoreWhitespace) {
					item = item.strip();
				}
				
				if (itemEnd < string.length()) {
					itemBegin = (itemEnd + 1);
				}
				
				cursor = itemEnd;
				
				return true;
			}
			
			@Override
			public String next (){
				cursor++;
				
				return item;
			}
		};
	}
	
	public static List<String> splitStringIntoList (final String string, final String separator, final boolean ignoreWhitespace){
		final List<String> collection;
		
		collection = new ArrayList<>();
		
		splitString(string, separator, ignoreWhitespace, (word) -> collection.add(word));
		
		return collection;
	}
	
	public static List<String> splitStringIntoList (final String string, final String separator, final boolean ignoreWhitespace, final StringTransformer transformer){
		final List<String> collection;
		
		collection = new ArrayList<>();
		
		splitString(string, separator, ignoreWhitespace, (word) -> collection.add(transformer.call(word)));
		
		return collection;
	}
	
	public static <T> List<T> splitStringIntoObjectsList (final String string, final String separator, final boolean ignoreWhitespace, final StringObjectConstructor<T> constructor){
		final List<T> collection;
		
		collection = new ArrayList<>();
		
		splitString(string, separator, ignoreWhitespace, (word) -> collection.add(constructor.newInstance(string)));
		
		return collection;
	}
	
	public static Iterator<String> splitStringIntoWordIterator (final String string){
		return new Iterator<>() {
			int     cursor = 0;
			int     ownDepth = 0;
			boolean withinQuote = false;
			int     wordBegin = 0;
			int     wordEnd = -1;
			
			@Override
			public boolean hasNext (){
				if (cursor > string.length()) 
					return false;
				
				for (; cursor < string.length(); cursor++) {
					final char c;
					
					c = string.charAt(cursor);
					
					if (!withinQuote && Character.isWhitespace(c)) {
						if ((cursor - wordBegin) > 0) 
							break;
						else {
							wordBegin++;
						}
					} else if (c == SYM_QUOTE_REG_CH) {
						int depth;
						
						depth = 0;
						
						for (var i = (cursor - 1); i > -1; i--) {
							if (string.charAt(i) == SYM_QUOTE_ESC_CH) 
								depth++;
							else {
								break;
							}
						}
						
						if (withinQuote) {
							if (depth == ownDepth) {
								withinQuote = false;
								ownDepth = 0;
//								cursor++;
							}
						} else {
							withinQuote = true;
							ownDepth = depth;
						}
					}
				}
				
				wordEnd = cursor;
				
				return true;
			}
			
			@Override
			public String next (){
				final String element;
				
				cursor++;
				
				element = string.substring(wordBegin, wordEnd);
				
				wordBegin = wordEnd + 1;
				wordEnd = -1;
				
				return element;
			}
		};
	}
	
	public static List<String> splitStringIntoWordList (final String string){
		final List<String> collection;
		
		collection = new ArrayList<>();
		
		splitStringIntoWords(string, (word) -> collection.add(word));
		
		return collection;
	}
	
	public static void splitStringIntoWords (final String string, final Consumer<String> callback){
		final Iterator<String> it;
		
		it = splitStringIntoWordIterator(string);
		
		while (it.hasNext()) {
			callback.accept(it.next());
		}
	}
	
	public static boolean startsWithIgnoreCase (final String string1, final String string2){
		return string1.regionMatches(true, 0, string2, 0, string2.length());
	}
	
	public static String strip (final String string, final char removeChar){
		return strip(string, removeChar, false);
	}
	
	public static String strip (final String string, final char removeChar, final boolean ignoreCase){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < string.length(); i++) {
			final char c;
			
			c = string.charAt(i);
			
			if ((c == removeChar) || (ignoreCase && (Character.toLowerCase(c) == Character.toLowerCase(removeChar)))) {
				continue;
			}
			
			sb.append(c);
		}
		
		return sb.toString();
	}
	
	public static String strip (final String string, final char... removeChars){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < string.length(); i++) {
			final char c;
			boolean    include = true;
			
			c = string.charAt(i);
			
			for (final var removeChar : removeChars) {
				if (c == removeChar) {
					include = false;
					
					break;
				}
			}
			
			if (include) {
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	public static String unquote (final String string){
		int                 cursor;
		int                 length;
		final StringBuilder sb;
		
		if (isQuoted(string)) {
			cursor = 1;
			length = (string.length() - 1);
		} else {
			cursor = 0;
			length = string.length();
		}
		
		sb = new StringBuilder();
		
		for (; cursor < length; cursor++) {
			int        depth;
			final char c;
			
			c = string.charAt(cursor);
			
			if (c == SYM_QUOTE_ESC_CH) 
				continue;
			
			if (c == SYM_QUOTE_REG_CH) {
				depth = 0;
				
				for (var i = (cursor - 1); i > -1; i--) {
					if (string.charAt(i) == SYM_QUOTE_ESC_CH) 
						depth++;
					else {
						break;
					}
				}
				
				if (depth > 1) {
					sb.append(SYM_QUOTE_ESC_STR.repeat(depth - 1));
				}
			}
			
			sb.append(c);
		}
		
		return sb.toString();
	}
	
	
	
	private StringUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}