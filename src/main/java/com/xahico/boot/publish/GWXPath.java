/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.util.StringUtilities;
import java.util.Arrays;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXPath {
	private static final char   EXT_SEPARATOR = '.';
	private static final String WORD_SEPARATOR = "/";
	
	
	
	public static boolean compare (final String s1, final String s2){
		final int p1 = (s1.startsWith("/") ? 1 : 0);
		final int p2 = (s2.startsWith("/") ? 1 : 0);
		
		final int len1 = (s1.length() - p1);
		final int len2 = (s2.length() - p2);
		
		return ((len1 == len2) && s1.regionMatches(p1, s2, p2, len1));
	}
	
	private static boolean compareWords (final String word1, final String word2){
		final int    word1Delimiter;
		final int    word2Delimiter;
		final String word1EventName;
		final String word1EventTarget;
		final String word2EventName;
		final String word2EventTarget;
		
		word1Delimiter = word1.lastIndexOf(EXT_SEPARATOR);
		
		if (word1Delimiter == -1) {
			return word1.equals(word2);
		}
		
		word2Delimiter = word2.lastIndexOf(EXT_SEPARATOR);
		
		if (word2Delimiter == -1) {
			return false;
		}
		
		word1EventTarget = word1.substring(word1Delimiter);
		word1EventName = word1.substring(word1Delimiter + 1);
		
		word2EventTarget = word2.substring(word2Delimiter);
		word2EventName = word2.substring(word2Delimiter + 1);
		
		if (!word1EventTarget.equals("*") && !word2EventTarget.equals("*") && !word1EventTarget.equals(word2EventTarget)) 
			return false;
		
		if (!word1EventName.equals("*") && !word2EventName.equals("*") && !word1EventName.equals(word2EventName)) 
			return false;
		
		return true;
	}
	
	public static GWXPath create (final String path){
		final String[] words;
		
		words = StringUtilities.splitString(path, WORD_SEPARATOR, true);
				
		return new GWXPath(words);
	}
	
	public static GWXPath create (final String path, final String extension){
		final int delimiter;
		
		delimiter = path.indexOf(EXT_SEPARATOR);
		
		if (delimiter == -1) 
			return GWXPath.create(path + EXT_SEPARATOR + extension);
		else {
			return GWXPath.create(path.substring(0, delimiter), extension);
		}
	}
	
	
	
	private final String[] words;
	
	
	
	private GWXPath (final String[] words){
		super();
		
		this.words = words;
	}
	
	
	
	public int count (){
		return this.words.length;
	}
	
	@Override
	public boolean equals (final Object obj){
		final GWXPath other;
		
		if (this == obj) 
			return true;
		
		if (null == obj) 
			return false;
		
		other = (GWXPath)(obj);
		
		if (this.words.length != other.words.length) 
			return false;
		
		return Arrays.deepEquals(this.words, other.words);
	}
	
	public GWXPath from (final int index){
		final StringBuilder sb;
		
		if (index == 0) {
			return this;
		}
		
		sb = new StringBuilder();
		
		for (var i = index; i < this.words.length; i++) {
			final String word;
			
			word = this.words[i];
			
			sb.append("/");
			sb.append(word);
		}
		
		return GWXPath.create(sb.toString());
	}
	
	public Pattern generic (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < words.length; i++) {
			final String word;
			
			word = words[i];
			
			sb.append(WORD_SEPARATOR);
			
			if (this.isParameter(i)) {
				sb.append("?");
			} else {
				sb.append(word);
			}
		}
		
		return GWXPath.create(sb.toString()).pattern();
	}
	
	public String get (final int index){
		return this.words[index];
	}
	
	public String get (final String key, final GWXPath.Pattern pattern){
		final int keyPosition;
		
		if (this.words.length < pattern.index.length) 
			return null;
		
		if (pattern.keys.length == 0) 
			return null;
		
		keyPosition = pattern.find(key);
		
		if (keyPosition == -1) 
			return null;
		
		return this.words[keyPosition];
	}
	
	public String getExtension (){
		final int    delimiter;
		final String name;
		
		name = this.getName();
		
		if (name.equals("")) 
			return null;
		
		delimiter = name.indexOf(EXT_SEPARATOR);
		
		if (delimiter == -1) 
			return null;
		
		return name.substring(delimiter + 1);
	}
	
	public String getName (){
		if (this.words.length == 0) 
			return "";
		else {
			return this.words[this.words.length - 1];
		}
	}
	
	@Override
	public int hashCode (){
		int hash = 7;
		hash = 67 * hash + Arrays.deepHashCode(this.words);
		return hash;
	}
	
	public boolean hasExtension (){
		final String name;
		
		name = this.getName();
		
		if (name.equals("")) 
			return false;
		
		return (name.indexOf(EXT_SEPARATOR) != -1);
	}
	
	public boolean isParameter (final int index){
		if (index < this.words.length) {
			final String value;
			
			value = this.words[index];
			
			if (value.startsWith("$(") && value.endsWith(")")) {
				return true;
			}
		}
		
		return false;
	}
	
	public String root (){
		if (this.words.length > 0)
			return this.words[0];
		else {
			return "";
		}
	}
	
	public Pattern pattern (){
		int             cursor;
		int             count;
		final boolean[] index;
		final String[]  keys;
		
		index = new boolean[words.length];
		
		count = 0;
		
		for (var i = 0; i < words.length; i++) {
			final String word;
			
			word = words[i];
			
			if (word.startsWith("$(") && word.endsWith(")")) {
				index[i] = true;
				
				count++;
			} else {
				index[i] = false;
			}
		}
		
		keys = new String[count];
		
		cursor = 0;
		
		for (var i = 0; i < words.length; i++) {
			final String word;
			
			word = words[i];
			
			if (word.startsWith("$(") && word.endsWith(")")) {
				keys[cursor] = word.substring(2, (word.length() - 1));
				
				cursor++;
				
				if (cursor == count) {
					break;
				}
			}
		}
		
		return new Pattern(index, keys);
	}
	
	@Override
	public String toString (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (final var word : this.words) {
			sb.append(WORD_SEPARATOR);
			sb.append(word);
		}
		
		if (sb.isEmpty()) 
			return "";
		else {
			return sb.substring(1);
		}
	}
	
	public String toString (final int wordCount){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i <= wordCount; i++) {
			final String word;
			
			if (i >= this.words.length) {
				break;
			}
			
			word = this.words[i];
			
			sb.append(WORD_SEPARATOR);
			sb.append(word);
		}
		
		if (sb.isEmpty()) 
			return "";
		else {
			return sb.substring(1);
		}
	}
	
	public GWXPath withExtension (final String extension){
		return GWXPath.create(this.withoutExtension() + EXT_SEPARATOR + extension);
	}
	
	public String withoutExtension (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < this.words.length; i++) {
			String word;
			
			word = this.words[i];
			
			if ((i + 1) == this.words.length) {
				final int delimiter;
				
				delimiter = word.indexOf(EXT_SEPARATOR);
				
				if (delimiter != -1) {
					word = word.substring(0, delimiter);
				}
			}
			
			sb.append(WORD_SEPARATOR);
			sb.append(word);
		}
		
		if (sb.isEmpty()) 
			return "";
		else {
			return sb.substring(1);
		}
	}
	
	
	
	public final class Pattern {
		private final boolean[] index;
		private final String[]  keys;
		
		
		
		private Pattern (final boolean[] index, final String[] keys){
			super();
			
			this.index = index;
			this.keys = keys;
		}
		
		
		
		@Override
		public boolean equals (final Object obj){
			final Pattern other;
			
			if (this == obj) 
				return true;
			
			if (null == obj) 
				return false;
			
			if (!(obj instanceof Pattern)) 
				return false;
			
			other = (Pattern)(obj);
			
			if (this.index.length != other.index.length) 
				return false;
			
			for (var i = 0; i < words.length; i++) {
				final String  word1;
				final String  word2;
				final boolean wordIsKey;
				
				wordIsKey = this.index[i];
				
				if (wordIsKey && other.isKey(i)) {
					continue;
				}
				
				word1 = words[i];
				word2 = other.get(i);
				
				if (! word1.equals(word2)) {
					return false;
				}
			}
			
			return true;
		}
		
		public int find (final String key){
			if (this.keys.length > 0) {
				int passed;
				
				passed = 0;
				
				for (var i = 0; i < words.length; i++) {
					final String  word;
					final boolean wordIsKey;
					
					wordIsKey = this.index[i];
					
					if (! wordIsKey) {
						continue;
					}
					
					word = words[i];
					
					if (word.regionMatches(true, 2, key, 0, key.length())) {
						return i;
					} else {
						passed++;
					}
					
					if (passed == this.keys.length) {
						break;
					}
				}
			}
			
			return -1;
		}
		
		public String get (final int index){
			return words[index];
		}
		
		@Override
		public int hashCode (){
			int hash = 7;
			hash = 59 * hash + Arrays.deepHashCode(words);
			return hash;
		}
		
		public boolean isKey (final int index){
			return this.index[index];
		}
		
		public String[] keys (){
			return this.keys;
		}
		
		public boolean matches (final GWXPath path){
			if (words.length != path.words.length) 
				return false;
			
			for (var i = 0; i < words.length; i++) {
				final String  word1;
				final String  word2;
				final boolean wordIsKey;
				
				wordIsKey = this.index[i];
				
				if (wordIsKey) 
					continue;
				
				word1 = words[i];
				
				if (word1.equals("*")) {
					continue;
				}
				
				word2 = path.get(i);
				
				if (word2.equals("*")) {
					continue;
				}
				
				if (! GWXPath.compareWords(word1, word2)) {
					return false;
				}
			}
			
			return true;
		}
		
		@Override
		public String toString (){
			final StringBuilder sb;
			
			sb = new StringBuilder();
			
			for (var i = 0; i < words.length; i++) {
				final String  word;
				final boolean wordIsKey;
				
				wordIsKey = this.index[i];
				
				word = words[i];
				
				sb.append(WORD_SEPARATOR);
				
				if (wordIsKey) 
					sb.append("?");
				else {
					sb.append(word);
				}
			}
			
			return sb.toString();
		}
	}
}