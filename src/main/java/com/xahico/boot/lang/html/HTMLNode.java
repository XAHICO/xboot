/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.html;

import com.xahico.boot.util.ArrayUtilities;
import com.xahico.boot.dev.Helper;
import com.xahico.boot.dev.Untested;
import com.xahico.boot.util.Filter;
import com.xahico.boot.util.StringUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class HTMLNode extends HTMLElement {
	private final Map<String, String> attributes = new HashMap<>();
	private final List<HTMLElement>   children = new ArrayList<>() {
		@Override
		public void add (final int index, final HTMLElement element){
			super.add(index, element);
			
			if (null != element) {
				if (element instanceof HTMLNode) {
					final HTMLNode node;

					node = (HTMLNode)(element);
					node.setParent(HTMLNode.this);
				}
			}
		}
		
		@Override
		public boolean add (final HTMLElement element){
			final boolean added;
			
			added = super.add(element);
			
			if (added && (null != element)) {
				if (element instanceof HTMLNode) {
					final HTMLNode node;
					
					node = (HTMLNode)(element);
					node.setParent(HTMLNode.this);
				}
			}
			
			return added;
		}
		
		@Override
		public HTMLElement remove (final int index){
			final HTMLElement removedElement;
			
			removedElement = super.remove(index);
			
			if (null != removedElement) {
				if (removedElement instanceof HTMLNode) {
					final HTMLNode removedNode;
					
					removedNode = (HTMLNode)(removedElement);
					removedNode.setParent(null);
				}
			}
			
			return removedElement;
		}
		
		@Override
		public boolean remove (final Object obj){
			final boolean removed;
			
			removed = super.remove(obj);
			
			if (removed) {
				if (obj instanceof HTMLNode) {
					final HTMLNode node;
					
					node = (HTMLNode)(obj);
					node.setParent(null);
				}
			}
			
			return removed;
		}
		
		@Override
		public HTMLElement set (final int index, final HTMLElement newElement){
			final HTMLElement oldElement;
			
			oldElement = super.set(index, newElement);
			
			if (null != oldElement) {
				if (oldElement instanceof HTMLNode) {
					final HTMLNode oldNode;
					
					oldNode = (HTMLNode)(oldElement);
					oldNode.setParent(null);
				}
			}
			
			if (null != newElement) {
				if (newElement instanceof HTMLNode) {
					final HTMLNode newNode;
					
					newNode = (HTMLNode)(newElement);
					newNode.setParent(HTMLNode.this);
				}
			}
			
			return oldElement;
		}
	};
	private String                    name = null;
	
	
	
	public HTMLNode (){
		super();
	}
	
	public HTMLNode (final HTMLNode parent){
		super(parent);
	}
	
	public HTMLNode (final HTMLStandardType type){
		super();
		
		this.name = type.toTypeString();
	}
	
	
	
	public HTMLNode addChild (final HTMLElement element){
		this.getChildren().add(element);
		
		return HTMLNode.this;
	}
	
	@Untested
	public HTMLNode addChildren (final HTMLElement... elements){
		this.getChildren().addAll(Arrays.asList(elements));
		
		return HTMLNode.this;
	}
	
	public HTMLNode addChildren (final List<HTMLElement> elements){
		this.getChildren().addAll(elements);
		
		return HTMLNode.this;
	}
	
	public HTMLNode addAttribute (final String key, final String value){
		final String attribute;
		
		attribute = this.getAttribute(key);
		
		if (null == attribute) 
			this.getAttributes().put(key, value);
		else {
			this.getAttributes().put(key, (attribute + " " + value));
		}
		
		return HTMLNode.this;
	}
	
	public HTMLNode addAttributes (final Map<String, String> attributes){
		for (final var key : attributes.keySet()) {
			this.addAttribute(key, attributes.get(key));
		}
		
		return HTMLNode.this;
	}
	
	public List<HTMLNode> collect (final List<HTMLNode> collection, final String key, final int depth){
		for (final HTMLElement element : this.getChildren()) {
			if (element instanceof HTMLNode) {
				final HTMLNode node;
				
				node = (HTMLNode)(element);
				
				if (key.equalsIgnoreCase(node.getName())) {
					collection.add(node);
				}
				
				if ((depth > 0) || (depth == -1)) {
					node.collect(collection, key, ((depth == -1) ? -1 : (depth - 1)));
				}
			}
		}
		
		return collection;
	}
	
	@Override
	public HTMLNode duplicate (){
		final HTMLNode clone;
		
		clone = new HTMLNode();
		clone.setAttributes(this.getAttributes());
		clone.setContent(this.getContent());
		clone.setName(this.getName());
		
		for (final var childElement : this.children) {
			final HTMLElement childElementClone;
			
			childElementClone = childElement.duplicate();
			childElementClone.setParent(clone);
			
			clone.addChild(childElementClone);
		}
		
		return clone;
	}
	
	public String getAttribute (final String key){
		return this.getAttributes().get(key);
	}
	
	public Map<String, String> getAttributes (){
		return this.attributes;
	}
	
	public HTMLElement getChild (final int index){
		return this.getChildren().get(index);
	}
	
	public List<HTMLElement> getChildren (){
		return this.children;
	}
	
	public String getName (){
		return this.name;
	}
	
	public HTMLNode getRoot (){
		if (null != this.getParent()) {
			return this.getParent().getRoot();
		}
		
		return this;
	}
	
	public boolean hasAttributes (){
		return !this.getAttributes().isEmpty();
	}
	
	public boolean hasChildren (){
		return !this.getChildren().isEmpty();
	}
	
	@Untested
	public int indexOf (final HTMLElement lookupElement){
		if (null != lookupElement) {
			for (var i = 0; i < this.getChildren().size(); i++) {
				final HTMLElement element;
				
				element = this.getChildren().get(i);
				
				if (element == lookupElement) {
					return i;
				}
			}
		}
		
		return -1;
	}
	
	public boolean isCustomType (){
		return !this.isStandardType();
	}
	
	public boolean isDocumentTag (){
		if (null == this.name) 
			return false;
		else {
			return this.name.equalsIgnoreCase(HTMLDocument.DOCUMENT_TAG);
		}
	}
	
	public boolean isRoot (){
		return !this.hasParent();
	}
	
	public boolean isStandardType (){
		for (final HTMLStandardType type : HTMLStandardType.values()) {
			if (this.isType(type)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isType (final HTMLStandardType type){
		if (null == this.name) 
			return false;
		else {
			return this.name.equalsIgnoreCase(type.name());
		}
	}
	
	public boolean isWithLanguageContent (){
		final HTMLStandardType type;
		
		type = HTMLStandardType.parseString(this.getName());
		
		if (null == type) 
			return false;
		
		return type.withLanguageContent();
	}
	
	@Helper
	public List<HTMLNode> lookup (final String key){
		return this.lookup(key, 0);
	}
	
	public List<HTMLNode> lookup (final String key, final int depth){
		final List<HTMLNode> collection;
		
		collection = new ArrayList<>();
		
		this.collect(collection, key, depth);
		
		return collection;
	}
	
	public HTMLNode lookupFirst (final HTMLStandardType type){
		return this.lookupFirst(type.toTypeString());
	}
	
	public HTMLNode lookupFirst (final HTMLStandardType type, final int depth){
		return this.lookupFirst(type.toTypeString(), depth);
	}
	
	/**
	 * Helper method to perform shallow lookup key.
	 * 
	 * Syntactic sugar for {@link #lookupFirst(java.lang.String, int) 
	 * lookupFirst(key, 0)}.
	 * 
	 * @param key 
	 * Element name to look up.
	 * 
	 * @return 
	 * Element object if found; {@code null} otherwise.
	 */
	@Helper
	public HTMLNode lookupFirst (final String key){
		return this.lookupFirst(key, 0);
	}
	
	public HTMLNode lookupFirst (final String key, final int depth){
		for (final HTMLElement element : this.getChildren()) {
			if (element instanceof HTMLNode) {
				final HTMLNode node;
				final HTMLNode nodeLookup;
				
				node = (HTMLNode)(element);
				
				if (key.equalsIgnoreCase(node.getName())) {
					return node;
				}
				
				if ((depth > 0) || (depth == -1)) {
					nodeLookup = node.lookupFirst(key, ((depth == -1) ? -1 : (depth - 1)));
					
					if (null != nodeLookup) {
						return nodeLookup;
					}
				}
			}
		}
		
		return null;
	}
	
	public HTMLNode lookupLast (final HTMLStandardType type){
		return this.lookupLast(type.toTypeString());
	}
	
	public HTMLNode lookupLast (final HTMLStandardType type, final int depth){
		return this.lookupLast(type.toTypeString(), depth);
	}
	
	@Helper
	@Untested
	public HTMLNode lookupLast (final String key){
		return this.lookupLast(key, 0);
	}
	
	@Untested
	public HTMLNode lookupLast (final String key, final int depth){
		for (var i = (this.getChildren().size() - 1); i > -1; i--) {
			final HTMLElement element;
			
			element = this.getChildren().get(i);
			
			if (element instanceof HTMLNode) {
				final HTMLNode node;
				final HTMLNode nodeLookup;
				
				node = (HTMLNode)(element);
				
				if (key.equalsIgnoreCase(node.getName())) {
					return node;
				}
				
				if ((depth > 0) || (depth == -1)) {
					nodeLookup = node.lookupFirst(key, ((depth == -1) ? -1 : (depth - 1)));
					
					if (null != nodeLookup) {
						return nodeLookup;
					}
				}
			}
		}
		
		return null;
	}
	
	@Untested
	public HTMLNode removeAllAttributes (){
		this.getAttributes().clear();
		
		return HTMLNode.this;
	}
	
	@Untested
	public HTMLNode removeAllChildren (){
		this.getChildren().clear();
		
		return HTMLNode.this;
	}
	
	public HTMLNode removeAttribute (final String key){
		this.getAttributes().remove(key);
		
		return HTMLNode.this;
	}
	
	@Untested
	public HTMLNode removeAttributes (final Collection<String> removeKeys){
		final Iterator<String> it;
		
		it = this.getAttributes().keySet().iterator();
		
		while (it.hasNext()) {
			final String key;
			
			key = it.next();
			
			for (final var removeKey : removeKeys) {
				if (removeKey.equalsIgnoreCase(key)) {
					it.remove();
					
					break;
				}
			}
		}
		
		return HTMLNode.this;
	}
	
	@Untested
	public HTMLNode removeAttributes (final String... keys){
		final Iterator<String> it;
		
		it = this.getAttributes().keySet().iterator();
		
		while (it.hasNext()) {
			final String key;
			
			key = it.next();
			
			if (ArrayUtilities.containsStringIgnoreCase(keys, key)) {
				it.remove();
			}
		}
		
		return HTMLNode.this;
	}
	
	@Untested
	public HTMLNode removeChild (final int elementIndex){
		this.getChildren().remove(elementIndex);
		
		return HTMLNode.this;
	}
	
	public HTMLNode removeChild (final HTMLElement element){
		this.getChildren().remove(element);
		
		return HTMLNode.this;
	}
	
	public HTMLNode removeChildren (final Collection<HTMLElement> removeElements){
		final Iterator<HTMLElement> it;
		
		it = this.getChildren().iterator();
		
		while (it.hasNext()) {
			final HTMLElement element;
			
			element = it.next();
			
			for (final var removeElement : removeElements) {
				if (removeElement == element) {
					it.remove();
					
					break;
				}
			}
		}
		
		return HTMLNode.this;
	}
	
	public HTMLNode removeChildren (final HTMLElement... removeElements){
		final Iterator<HTMLElement> it;
		
		it = this.getChildren().iterator();
		
		while (it.hasNext()) {
			final HTMLElement element;
			
			element = it.next();
			
			for (final var removeElement : removeElements) {
				if (removeElement == element) {
					it.remove();
					
					break;
				}
			}
		}
		
		return HTMLNode.this;
	}
	
	public HTMLNode removeComments (){
		final Iterator<HTMLElement> it;
		
		it = this.getChildren().iterator();
		
		while (it.hasNext()) {
			final HTMLElement element;
			
			element = it.next();
			
			if (element instanceof HTMLComment) {
				it.remove();
			} else if (element instanceof HTMLNode) {
				final HTMLNode node;
				
				node = (HTMLNode)(element);
				node.removeComments();
			}
		}
		
		return HTMLNode.this;
	}
	
	public HTMLElement seek (final Filter<HTMLElement> filter, final int depth){
		for (final HTMLElement element : this.getChildren()) {
			if (filter.accept(element)) {
				return element;
			}
			
			if (element instanceof HTMLNode) {
				final HTMLNode node;
				
				node = (HTMLNode)(element);
				
				if ((depth > 0) || (depth == -1)) {
					final HTMLElement seekElement;
					
					seekElement = node.seek(filter, ((depth == -1) ? -1 : (depth - 1)));
					
					if (null != seekElement) {
						return seekElement;
					}
				}
			}
		}
		
		return null;
	}
	
	public HTMLNode setAttribute (final String key, final String value){
		this.getAttributes().put(key, value);
		
		return HTMLNode.this;
	}
	
	public HTMLNode setAttributes (final Map<String, String> attributes){
		this.getAttributes().putAll(attributes);
		
		return HTMLNode.this;
	}
	
	@Override
	public HTMLNode setContent (final String content){
		return (HTMLNode) super.setContent(content);
	}
	
	public HTMLNode setName (final String name){
		this.name = name;
		
		return HTMLNode.this;
	}
	
	public HTMLNode swap (final HTMLElement element){
		this.removeAllAttributes();
		this.setContent(element.getContent());
		this.removeAllChildren();
		
		if (element instanceof HTMLNode) {
			final HTMLNode node;
			
			node = (HTMLNode)(element);
			
			this.addChildren(node.getChildren());
			this.setAttributes(node.getAttributes());
			this.setName(node.getName());
		}
		
		return HTMLNode.this;
	}
	
	@Override
	public String toHTMLString (final int depth, final boolean safe){
		final StringBuilder sb;
		
		sb = new StringBuilder();

		if (depth > 0) {
			sb.append(LTAB.repeat(depth));
		}
		
		sb.append("<");
		sb.append(this.getName());

		if (this.hasAttributes()) {
			for (final var key : this.getAttributes().keySet()) {
				sb.append(" ");
				sb.append(key);
				sb.append("=");
				sb.append(StringUtilities.quote(this.getAttributes().get(key)));
			}
		}
		
		if (this.getChildren().isEmpty() && !this.hasContent()) {
			if (safe) {
				sb.append(">");
				sb.append("<");
				sb.append("/");
				sb.append(this.getName());
				sb.append(">");
			} else {
				sb.append("/");
				sb.append(">");
			}
		} else {
			sb.append(">");
			
			if (depth != -1) {
				sb.append(LINE);
			}
			
			if (this.hasContent()) {
				sb.append(super.toHTMLString((depth == -1) ? -1 : (depth + 1), safe));
				
				if (depth != -1) {
					sb.append(LINE);
				}
			}
			
			for (final var childElement : this.getChildren()) {
				if (null == childElement) 
					continue;
				
				sb.append(childElement.toHTMLString((depth == -1) ? -1 : (depth + 1), safe));
				
				if (depth != -1) {
					sb.append(LINE);
				}
			}
			
			if (depth > 0) {
				sb.append(LTAB.repeat(depth));
			}
			
			sb.append("<");
			sb.append("/");
			sb.append(this.getName());
			sb.append(">");
		}
		
		return sb.toString();
	}
	
	public boolean unlink (){
		if (null == this.getParent()) 
			return false;
		else {
			this.getParent().removeChild(HTMLNode.this);
			
			return true;
		}
	}
	
	public void walk (final Consumer<HTMLElement> consumer, final int depth){
		for (final HTMLElement element : this.getChildren()) {
			consumer.accept(element);
			
			if (element instanceof HTMLNode) {
				final HTMLNode node;
				
				node = (HTMLNode)(element);
				
				if (((depth > 0) || (depth == -1)) && node.hasChildren()) {
					node.walk(consumer, ((depth == -1) ? -1 : (depth - 1)));
				}
			}
		}
	}
}