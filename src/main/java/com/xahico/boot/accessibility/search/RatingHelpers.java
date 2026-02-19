/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.accessibility.search;

import com.xahico.boot.util.StringUtilities;
import java.util.List;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class RatingHelpers {
	private static final double CASE_INSENSITIVE_SCORE = 0.9;
	
	
	
	public static double eval (final double score, final boolean anti){
		return (anti ? (1.0 - score) : (0.0 + score));
	}
	
	public static double rate (final String inputs, final String text){
		final double       cmult;
		boolean            containsPerfectMatch = false;
		final List<String> inputList;
		double             overallRating = 0.0;
		int                position = 0;
		final List<String> textWordList;
		
		textWordList = StringUtilities.splitStringIntoWordList(simplify(text));
		
		inputList = StringUtilities.splitStringIntoWordList(simplify(inputs));
		
		if (inputList.size() > textWordList.size()) {
			cmult = (1.0 / (inputList.size() - textWordList.size()));
		} else if (textWordList.size() > inputList.size()) {
			cmult = (((double)textWordList.size()) / ((double)inputList.size()));
		} else {
			cmult = 1.0;
		}
		
		for (final var input : inputList) {
			double highestRating = 0.0;
			
			for (var i = 0; i < textWordList.size(); i++) {
				int    distance;
				double ratingA;
				double ratingB;
				double shared;
				String word;
				
				/*
				 * Calculate word distance from corresponding index.
				 */
				if (i < position) 
					distance = (position - i);
				else {
					distance = (i - position);
				}
				
				word = textWordList.get(i);
				
				ratingA = rateExpression(word, input);
				
				shared = StringUtilities.calculateEqualsAnagram(word, input, 1.0);
				
				/*
				 * If rating is below 'acceptable', attempt 
				 * reversed rating to account for backwards matching.
				 */
				if (ratingA < Score.ACCEPTABLE.minimumRating()) {
					if (shared > 0.5) {
						ratingB = rateExpression(StringUtilities.reverse(word), StringUtilities.reverse(input));
						
						if (ratingB > ratingA) {
							ratingA = ratingB;
						}
					}
				}
				
				/*
				 * If rating is "a new high" yet below 'good', 
				 * add a small portion of anagram match score to 
				 * adjust it.
				 */
				if ((highestRating < ratingA) && (ratingA < Score.GOOD.minimumRating())) {
					ratingA += (shared / 2.0);
					ratingA /= (2.0 + ((double)distance));
				}
				
				/*
				 * If rating out-of-index (and not perfect match) 
				 * then decrement score by distance relative to 
				 * input count.
				 */
				if ((distance > 0) && (ratingA < Score.PERFECT.minimumRating())) 
					ratingA /= ((((double)inputList.size()) / (((double)distance) + 1.0)) + 1.0);
				
				/*
				 * If rating is below 10% it is most likely an 
				 * inaccuracy and can be ignored.
				 */
				//if (ratingA < 0.1) 
				//	continue;
				
				if (ratingA > highestRating) 
					highestRating = ratingA;
				
				if (ratingA >= Score.PERFECT.minimumRating()) {
					containsPerfectMatch = true;
					
					break;
				}
			}
			
			overallRating += highestRating;
			
			position++;
		}
		
		overallRating /= textWordList.size();
		overallRating *= cmult;
		
		/*
		 * Ensure that if a perfect match is found the score can never be 
		 * below 'acceptable', and add 10% of the rating to minimum 
		 * acceptable rating as to distinguish it with precision while 
		 * indexing.
		 */
		if (containsPerfectMatch && (overallRating < Score.ACCEPTABLE.minimumRating())) {
			overallRating = (Score.ACCEPTABLE.minimumRating() + (overallRating * 0.1));
		}
		
		return overallRating;
	}
	
	private static double rateExpression (final String field, final String input){
		double ldiff;
		double rating;
		
		if (field.length() > input.length()) 
			ldiff = (((double)input.length()) / ((double)field.length()));
		else if (input.length() > field.length()) 
			ldiff = (((double)field.length()) / ((double)input.length()));
		else {
			ldiff = 0.0;
		}
		
		rating = StringUtilities.calculateContainsInordinal(field, input, CASE_INSENSITIVE_SCORE);
		
		if (rating < Score.ACCEPTABLE.minimumRating()) {
			double ratingReverse;
			
			ratingReverse = StringUtilities.calculateContainsInordinal(StringUtilities.reverse(field), StringUtilities.reverse(input), CASE_INSENSITIVE_SCORE);
			//ratingReverse /= 2.0;
			
			if (ratingReverse > rating) {
				rating = ratingReverse;
			}
		}
		
		return (rating * (1.0 + ldiff));
//		return (rating * ((ldiff != 0.0) ? ldiff : 1.0));
	}
	
	private static String simplify (final String input){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < input.length(); i++) {
			final char cc; // character, current
			final char ci; // character to insert
			final char cl; // character, last
			
			cc = input.charAt(i);
			
			if (i > 0) 
				cl = input.charAt(i - 1);
			else {
				cl = 0x0;
			}
			
			if (((cc == '-') || (cc == '_')) && Character.isAlphabetic(cl)) {
				ci = ' ';
			} else if (!Character.isAlphabetic(cc) && !Character.isDigit(cc)) {
				ci = ' ';
			} else {
				ci = cc;
			}
			
			if (Character.isWhitespace(ci)) {
				if (!sb.isEmpty() && !Character.isWhitespace(sb.charAt(sb.length() - 1))) {
					sb.append(ci);
				}
			} else if (Character.isAlphabetic(ci) || Character.isDigit(ci)) {
				sb.append(ci);
			}
		}
		
		return sb.toString();
	}
	
	
	
	private RatingHelpers (){
		throw new UnsupportedOperationException("Not supported.");
	}
}