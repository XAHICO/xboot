/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.accessibility.search;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class QueryRating {
	private final Accessible target;
	
	private double freshness = 0.0;
	private double popularity = 0.0;
	private double prominence = 0.0;
	private double relevance = 0.0;
	
	
	
	public QueryRating (final Accessible target){
		super();
		
		this.target = target;
	}
	
	
	
	public double calculateAggregate (){
		return (((this.prominence + this.relevance) / 2.0) + ((this.freshness + this.popularity) / 2.0));
	}
	
	public double getFreshness (){
		return this.freshness;
	}
	
	public double getPopularity (){
		return this.popularity;
	}
	
	public double getProminence (){
		return this.prominence;
	}
	
	public double getRelevance (){
		return this.relevance;
	}
	
	public void setFreshness (final double value){
		this.freshness = value;
	}
	
	public void setPopularity (final double value){
		this.popularity = value;
	}
	
	public void setProminence (final double value){
		this.prominence = value;
	}
	
	public void setRelevance (final double value){
		this.relevance = value;
	}
	
	public Accessible target (){
		return this.target;
	}
	
	@Override
	public String toString (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append(QueryRating.class.getSimpleName());
		sb.append(" ");
		sb.append("{");
		sb.append("freshness").append(":").append(this.freshness);
		sb.append(",");
		sb.append(" ");
		sb.append("popularity").append(":").append(this.popularity);
		sb.append(",");
		sb.append(" ");
		sb.append("prominence").append(":").append(this.prominence);
		sb.append(",");
		sb.append(" ");
		sb.append("relevance").append(":").append(this.relevance);
		sb.append(",");
		sb.append(" ");
		sb.append("aggregate").append(":").append(this.calculateAggregate());
		sb.append("}");
		
		return sb.toString();
	}
}