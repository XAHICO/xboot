/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.accessibility.search;

import com.xahico.boot.util.CollectionUtilities;
import com.xahico.boot.util.Exceptions;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @param <CTX> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class AccessibleSearchEngine <T extends Accessible, CTX> {
	private AccessibleDataSource<T>       dataSource = null;
	private final List<SearchFilter<CTX>> filterList = new ArrayList<>();
	private double                        minimumRating = Score.ACCEPTABLE.minimumRating();
	
	
	
	public AccessibleSearchEngine (){
		super();
	}
	
	
	
	protected boolean accept (final T accssible, final CTX context){
		return true;
	}
	
	public void addFilter (final SearchFilter filter){
		this.filterList.add(filter);
	}
	
	protected double evaluateFreshness (final T accessible, final CTX context, final AccessibleData data){
		return 1.0;
	}
	
	protected double evaluatePopularity (final T accessible, final CTX context, final AccessibleData data){
		return 1.0;
	}
	
	protected double evaluateProminence (final T accessible, final CTX context, final AccessibleData data){
		return 1.0;
	}
	
	protected double evaluateRelevance (final T accessible, final CTX context, final AccessibleData data){
		double totalScore = 0.0;
		
		for (final var matcher : data.matcherList) try {
			double matcherScore;
			
			if (matcher.hasKey()) {
				final AccessibleProperty property;
				
				property = accessible.getAccessibleProperties().get(matcher.key());
				
				if (null == property) 
					continue;
				
				matcherScore = property.match(matcher);
				
				if (matcher.isRequired() && (matcherScore < this.minimumRating)) {
					return 0.0;
				}
				
				matcherScore *= property.scoreMultiplier().value();
			} else {
				matcherScore = 0.0;
					
				for (final var propertyKey : accessible.getAccessibleProperties().keys()) {
					final AccessibleProperty property;
					final double             propertyScore;
					
					property = accessible.getAccessibleProperties().get(propertyKey);
					
					if (null == property) 
						continue;
					
					propertyScore = property.match(matcher);
					
					if (matcher.isRequired() && (propertyScore < this.minimumRating)) {
						return 0.0;
					}
					
					matcherScore += (propertyScore * property.scoreMultiplier().value());
				}
				
				matcherScore /= ((double)accessible.getAccessibleProperties().keys().length);
			}
			
			totalScore += matcherScore;
		} catch (final NoSuchPropertyException ex) {
			Exceptions.ignore(ex);
		}
		
		return (totalScore / ((double)data.matcherList.size()));
	}
	
	public final QueryResults executeQuery (final Query query, final CTX context){
		final AccessibleData    data;
		final QueryParser       parser;
		final QueryResults      queryResults;
		final List<QueryRating> ratingList;
		double                  relevanceStandard;
		final long              whenBegin;
		final long              whenEnd;
		
		System.out.println("Executing Query '%s'".formatted(query.searchString));
		
		whenBegin = System.currentTimeMillis();
		
		data = new AccessibleData();
		
		parser = new QueryParser();
		parser.parse(query.searchString, data.matcherList);
		
		ratingList = new ArrayList<>();
		
		System.out.println("Matchers: " + data.matcherList);
		System.out.println("-".repeat(64));
		
		searchBegin();
		
		/*
		 * Collect all matches that are of relevance to the query.
		 */
		try {
			this.dataSource.open();

			this.dataSource.forEach((accessible) -> {
				final QueryRating rating;
				final double      score;

				if (this.accept(accessible, context)) {
					if (data.matcherList.isEmpty()) {
						score = 100.0;
					} else {
						score = evaluateRelevance(accessible, context, data);
					}

					System.out.println("Evaluated Revelance of '%s' -> %g".formatted(accessible, score));

					if (score > 0.0) {
						rating = new QueryRating(accessible);
						rating.setRelevance(score);

						ratingList.add(rating);
					}
				}
			});
		} finally {
			this.dataSource.close();
		}
		
		relevanceStandard = 0.0;
		
		for (final var rating : ratingList) {
			if (rating.getRelevance() > relevanceStandard) {
				relevanceStandard = rating.getRelevance();
			}
		}
		
		for (final var rating : ratingList) {
			rating.setRelevance(rating.getRelevance() / relevanceStandard);
		}
		
		CollectionUtilities.removeFiltered(ratingList, (rating) -> {
			if (rating.getRelevance() < this.minimumRating) {
				return true;
			} else {
				processItem((T)rating.target(), context);
				
				return false;
			}
		});
		
		/*
		 * Evaluate Freshness, Popularity & Prominence of each match, 
		 * respectively.
		 */
		for (final var rating : ratingList) {
			rating.setFreshness(evaluateFreshness((T)rating.target(), context, data));
			rating.setPopularity(evaluatePopularity((T)rating.target(), context, data));
			rating.setProminence(evaluateProminence((T)rating.target(), context, data));
		}
		
		searchEnd();
		
		/*
		 * Sort collection according to sorting policy.
		 */
		switch (query.sortingPolicy) {
			case AGGREGATE: {
				ratingList.sort((o1, o2) -> Double.compare(o2.calculateAggregate(), o1.calculateAggregate()));
				
				break;
			}
			case FRESHNESS: {
				ratingList.sort((o1, o2) -> Double.compare(o2.getFreshness(), o1.getFreshness()));
				
				break;
			}
			case POPULARITY: {
				ratingList.sort((o1, o2) -> Double.compare(o2.getPopularity(), o1.getPopularity()));
				
				break;
			}
			case PROMINENCE: {
				ratingList.sort((o1, o2) -> Double.compare(o2.getProminence(), o1.getProminence()));
				
				break;
			}
			case RELEVANCE: {
				ratingList.sort((o1, o2) -> Double.compare(o2.getRelevance(), o1.getRelevance()));
				
				break;
			}
		}
		
		/*
		 * Build & fill results.
		 */
		queryResults = new QueryResults();
		queryResults.collection = new ArrayList<>();
		
		for (final var rating : ratingList) {
			if (filter((T)rating.target(), context)) {
				queryResults.collection.add(rating.target());
			}
		}
		
		queryResults.totalResults = queryResults.collection.size();
		
		whenEnd = System.currentTimeMillis();
		
		System.out.println("Search Finished with %d results (took %d milliseconds)".formatted(queryResults.totalResults, whenEnd - whenBegin));
		System.out.println("-".repeat(64));
		
		return queryResults;
	}
	
	private boolean filter (final T accessible, final CTX context){
		if (this.filterList.isEmpty())
			return true;
		else {
			for (final var filter : this.filterList) {
				if (! filter.accept(accessible, context)) {
					return false;
				}
			}
			
			return true;
		}
	}
	
	protected void processItem (final T accessible, final CTX context){
		
	}
	
	protected void searchBegin (){
		
	}
	
	protected void searchEnd (){
		
	}
	
	public final void setDataSource (final AccessibleDataSource<T> dataSource){
		this.dataSource = dataSource;
	}
	
	public final void setDataSource (final List<T> accessibleList){
		this.setDataSource(new AccessibleDataSource<T>() {
			@Override
			public void close (){
				
			}
			
			@Override
			public void forEach (final Consumer<T> consumer){
				accessibleList.forEach(consumer);
			}
			
			@Override
			public void open (){
				
			}
		});
	}
	
	public final void setMinimumRating (final double minimumRating){
		this.minimumRating = minimumRating;
	}
}