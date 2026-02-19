/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.accessibility.search;

import com.xahico.boot.util.StringUtilities;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class QueryParser {
	private static final char SYM_ADD = '&';
	private static final char SYM_EQS = ':';
	private static final char SYM_NOT = '!';
	private static final char SYM_ORX = '+';
	private static final char SYM_REQ = '?';
	
	
	
	public QueryParser (){
		super();
	}
	
	
	
	private String formatString (final String searchString){
		final Iterator<String> it;
		final StringBuilder    sb;
		
		sb = new StringBuilder();
		
		it = StringUtilities.splitStringIntoWordIterator(searchString);
		
		while (it.hasNext()) {
			final String word;
			
			word = it.next();
			
			if (word.equals(Character.toString(SYM_ADD)) || word.equals(Character.toString(SYM_NOT)) || word.equals(Character.toString(SYM_EQS))) {
				if (word.equals(Character.toString(SYM_ADD)) && (!sb.isEmpty() && !Character.isWhitespace(sb.charAt(sb.length() - 1)))) {
					sb.append(' ');
				}
				
				sb.append(word);
				
				continue;
			}
			
			if (word.endsWith(Character.toString(SYM_ADD)) || word.endsWith(Character.toString(SYM_NOT))) {
				sb.append(word.substring(0, (word.length() - 1)));
				sb.append(' ');
				sb.append(word.charAt(word.length() - 1));
				
				continue;
			}
			
			if (word.startsWith(Character.toString(SYM_ADD)) || word.startsWith(Character.toString(SYM_NOT))) {
				if (word.startsWith(Character.toString(SYM_ADD)) && (!sb.isEmpty() && !Character.isWhitespace(sb.charAt(sb.length() - 1)))) {
					sb.append(' ');
				}
				
				sb.append(word.charAt(0));
				sb.append(word.substring(1));
				
				continue;
			}
			
			sb.append(word);
			
			if (it.hasNext()) {
				sb.append(' ');
			}
		}
		
		return sb.toString();
	}
	
	public List<Matcher> parse (final String searchString){
		return this.parse(searchString, new ArrayList<>());
	}
	
	public List<Matcher> parse (final String searchString, final List<Matcher> collection){
		final Iterator<String> it;
		
		it = StringUtilities.splitStringIntoWordIterator(formatString(searchString));
		
		while (it.hasNext()) {
			final Matcher matcher;
			final boolean required;
			final String  word;
			final String  wordFinal;
			
			word = it.next();
			
			if (word.isBlank()) 
				continue;
			
			required = word.endsWith(Character.toString(SYM_REQ));
			
			if (required) {
				wordFinal = word.substring(0, (word.length() - 1));
			} else {
				wordFinal = word;
			}
			
			if (wordFinal.startsWith(Character.toString(SYM_ADD))) {
				matcher = parseSpecifiedMatcher(wordFinal.substring(1), required);
			} else {
				matcher = parseWildcardMatcher(wordFinal, required);
			}
			
			collection.add(matcher);
		}
		
		return collection;
	}
	
	public String parseAsDebugString (final String searchString){
		final List<Matcher> matcherList;
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		matcherList = parse(searchString);
		
		for (var i = 0; i < matcherList.size(); i++) {
			final Matcher matcher;
			
			matcher = matcherList.get(i);
			
			sb.append(matcher);
			
			if ((i + 1) < matcherList.size()) {
				sb.append(' ');
				sb.append("WITH");
				sb.append(' ');
			}
		}
		
		return sb.toString();
	}
	
	private Matcher parseSpecifiedMatcher (final String word, final boolean required){
		final boolean      anti;
		int                cursor = 0;
		final int          delimiter;
		final String       key;
		final List<String> valueList;
		
		anti = word.startsWith(Character.toString(SYM_NOT));
		
		if (anti) {
			cursor++;
		}
		
		delimiter = word.indexOf(SYM_EQS);
		
		if (delimiter == -1) {
			key = word.substring(cursor);
			
			valueList = new ArrayList<>();
		} else {
			key = word.substring(cursor, delimiter);

			cursor = (delimiter + 1);
			
			valueList = new ArrayList<>();
			
			StringUtilities.splitStringIntoItems(word.substring(cursor), SYM_ORX, 
				(item) -> valueList.add(StringUtilities.isQuoted(item) ? StringUtilities.unquote(item) : item)
			);
		}
		
		return new SpecifiedMatcher(key, valueList, anti, required);
	}
	
	private Matcher parseWildcardMatcher (final String word, final boolean required){
		return new WildlycardMatcher(word, required);
	}
}