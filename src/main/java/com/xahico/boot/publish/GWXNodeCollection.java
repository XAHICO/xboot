/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXArray;
import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.publish.collections.GWXProxyNodeCollection;
import com.xahico.boot.util.Filter;
import com.xahico.boot.util.OrderedConsumer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class GWXNodeCollection <T extends GWXObject> extends GWXObject implements GWXSerializableCollection {
	protected GWXNodeCollection (){
		super();
	}
	
	protected GWXNodeCollection (final GWXObject owner){
		super();
		
		this.link(owner);
	}
	
	
	
	public abstract void add (final T node);
	
	public final void addAll (final T[] nodes){
		this.addAll(Arrays.asList(nodes));
	}
	
	public void addAll (final Collection<T> nodes){
		nodes.forEach(node -> this.add(node));
	}
	
	public void addAll (final GWXNodeCollection<T> other){
		other.walk((element) -> {
			this.add(element);
		});
	}
	
	public final void bind (final T node){
		node.link(this);
	}
	
	@Override
	protected void cleanup (){
		this.walk((node) -> {
			node.cleanup();
		});
		
		super.cleanup();
	}
	
	public abstract void clear ();
	
	public final List<T> collect (final Filter<T> filter){
		final List<T> collection;
		
		collection = new ArrayList<>();
		
		this.walk((element) -> {
			if (filter.accept(element)) {
				collection.add(element);
			}
		});
		
		return collection;
	}
	
	public abstract boolean contains (final T node);
	
	public GWXNodeCollection<T> exclude (final T node){
		return this.filtered((element) -> {
			if (element == node) {
				return false;
			} else {
				return true;
			}
		});
	}
	
	public GWXNodeCollection<T> exclude (final List<T> nodes){
		return this.filtered((element) -> {
			if (nodes.contains(element)) {
				return false;
			} else {
				return true;
			}
		});
	}
	
	public GWXNodeCollection<T> filtered (final Filter<T>... filters){
		return this.filtered(Arrays.asList(filters));
	}
	
	public GWXNodeCollection<T> filtered (final List<Filter<T>> filters){
		return new GWXProxyNodeCollection<>(this) {
			@Override
			public boolean contains (final T node){
				for (final var filter : filters) {
					if (! filter.accept(node)) {
						return false;
					}
				}
				
				return GWXNodeCollection.this.contains(node);
			}
			
			@Override
			public T lookup (final Object key){
				final T result;
				
				result = GWXNodeCollection.this.lookup(key);
				
				for (final var filter : filters) {
					if (! filter.accept(result)) {
						return null;
					}
				}
				
				return result;
			}
			
			@Override
			public void walk (final OrderedConsumer<T> consumer) {
				GWXNodeCollection.this.walk((node) -> {
					for (final var filter : filters) {
						if (! filter.accept(node)) {
							return;
						}
					}
					
					consumer.accept(node);
				});
			}
		};
	}
	
	public GWXNodeCollection<T> flip (){
		return new GWXProxyNodeCollection<>(this) {
			@Override
			public void walk (final OrderedConsumer<T> consumer) {
				GWXNodeCollection.this.walkReversed(consumer);
			}
			
			@Override
			public void walkReversed (final OrderedConsumer<T> consumer) {
				GWXNodeCollection.this.walk(consumer);
			}
		};
	}
	
	public final GWXNodeCollection<T> from (final int position){
		return this.position(position);
	}
	
	public boolean isEmpty (){
		return (this.size() == 0);
	}
	
	public final GWXNodeCollection<T> limited (final int max){
		return this.max(max);
	}
	
	public abstract T lookup (final Object key);
	
	public GWXNodeCollection<T> max (final int max){
		return new GWXProxyNodeCollection<>(this) {
			@Override
			public void walk (final OrderedConsumer<T> consumer) {
				GWXNodeCollection.this.walk(new OrderedConsumer<>() {
					int count = -1;
					
					@Override
					public boolean accept (final T node){
						count++;
						
						if (count < max) {
							return consumer.accept(node);
						} else {
							return false;
						}
					}
				});
			}
		};
	}
	
	public GWXNodeCollection<T> ordered (final Comparator<T> comparator){
		return new GWXProxyNodeCollection<>(this) {
			@Override
			public void walk (final OrderedConsumer<T> consumer){
				final List<T> nodes;
				
				nodes = new ArrayList<>();
				
				GWXNodeCollection.this.walk((node) -> {
					nodes.add(node);
					
					return true;
				});

				nodes.sort(comparator);
				
				for (final var node : nodes) {
					if (! consumer.accept(node)) {
						break;
					}
				}
			}
		};
	}
	
	public GWXNodeCollection<T> position (final int position){
		return new GWXProxyNodeCollection<>(this) {
			@Override
			public void walk (final OrderedConsumer<T> consumer) {
				GWXNodeCollection.this.walk(new OrderedConsumer<>() {
					int cursor = -1;
					
					@Override
					public boolean accept (final T node){
						cursor++;
						
						if (cursor >= position) {
							return consumer.accept(node);
						} else {
							return true;
						}
					}
				});
			}
		};
	}
	
	public abstract void remove (final T node);
	
	public final void removeAll (final T[] nodes){
		this.removeAll(Arrays.asList(nodes));
	}
	
	public void removeAll (final Collection<T> nodes){
		nodes.forEach(node -> this.remove(node));
	}
	
	public final T seek (final Filter<T> filter){
		final AtomicReference<T> atom;
		
		atom = new AtomicReference<>(null);
		
		this.walk((element) -> {
			if (filter.accept(element)) {
				atom.set(element);
				
				return false;
			} else {
				return true; // continue it
			}
		});
		
		return atom.get();
	}
	
	@Override
	final void select (final String path, final String from, final GWXSelector consumer, final GWXPathResolveFallbackHandler fallback){
		final int           delimiter;
		final String        key;
		final boolean       last;
		final String        next;
		final AtomicBoolean select;

		System.out.println("@ncoll select " + path + " from " + from);
		select = new AtomicBoolean(true);

		super.select(path, from, (p, o) -> {
			final boolean call;

			call = consumer.call(p, o);

			if (! call) {
				select.set(false);

				return false;
			}

			return call;
		}, fallback);

		if (select.get() == false) {
			return;
		}

		delimiter = path.indexOf('/');

		if (delimiter == -1) {
			key = path;
			next = null;
			last = true;
		} else {
			key = path.substring(0, delimiter);
			next = path.substring(delimiter + 1);
			last = next.isEmpty();
		}

		if (key.equals("**") && last) {
			consumer.call(from, this);

			// FIXED: Don't use recursive walk with consumer - just iterate direct children
			this.walk(new Consumer<>() {
				boolean acceptNext = true;

				@Override
				public void accept (final T object){
					if (acceptNext) {
						// Only call consumer on direct children - do NOT recursively select on them
						acceptNext = consumer.call((from + "/" + object.getId()), object);

						// If object is a collection, walk its direct children too (one level only)
						if (acceptNext && object instanceof GWXNodeCollection) {
							((GWXNodeCollection<?>) object).walk((child) -> {
								consumer.call((from + "/" + object.getId() + "/" + child.getId()), child);
							});
						}
					}
				}
			});
		} else if (key.equals("*")) {
			if (last) {
				this.walk((object) -> {
					return consumer.call((from + "/" + object.getId()), object);
				});
			} else {
				this.walk((object) -> {
					object.select(next, (from + "/" + object.getId()), consumer, fallback);
				});
			}
		} else {
			final GWXObject object;

			object = this.lookup(key);

			if (null == object) {
				fallback.fallback(path);
			} else {
				if (last) {
					consumer.call((from + "/" + key), object);
				} else {
					object.select(next, (from + "/" + key), consumer, fallback);
				}
			}
		}
	}
	
	@Override
	public byte[] serialize (final boolean internal){
		final JSOXArray serialized;
		
		serialized = new JSOXArray();
		
		this.walk((object) -> {
			serialized.append(object.serialize(internal));
			
			return true;
		});
		
		return serialized.toJSONStringCompact().getBytes(StandardCharsets.UTF_8);
	}
	
	public abstract int size ();
	
	@Override
	public JSOXVariant snapshot (){
		final JSOXVariant image;
		
		image = new JSOXVariant();
		
		this.walk((element) -> {
			image.put(Objects.toString(element.getId()), element.snapshot());
		});
		
		return image;
	}
	
	public Object[] toArray (){
		final Object[] array;
		
		array = new Object[this.size()];
		
		return this.toArray(array);
	}
	
	public <E> E[] toArray (final E[] array){
		this.walk(new Consumer<>() {
			int index = 0;
			
			@Override
			public void accept (final T object){
				if (index < array.length) {
					array[index] = ((E)object);
					
					index++;
				}
			}
		});
		
		return array;
	}
	
	@Override
	public String toString (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append("[");
		
		if (! this.isEmpty()) {
			this.walk((element) -> {
				sb.append(element);
				sb.append(", ");
			});
			
			if (sb.length() >= 3) {
				sb.delete((sb.length() - 2), sb.length());
			}
		}
		
		sb.append("]");
		
		return sb.toString();
	}
	
	public final void unbind (final T node){
		node.unlink();
	}
	
	public final GWXNodeCollection<T> unpack (){
		return new GWXProxyNodeCollection<>(this) {
			@Override
			public void add (final T element){
				throw new UnsupportedOperationException("Not supported.");
			}
			
			@Override
			public T lookup (final Object key){
				final AtomicReference<T> atom;
				
				atom = new AtomicReference<>(null);
				
				GWXNodeCollection.this.walk((element) -> {
					if (element instanceof GWXNodeCollection collection) {
						final T result;
						
						result = (T) collection.lookup(key);
						
						if (null != result) {
							atom.set(result);
							
							return false;
						}
					}
					
					return true;
				});
				
				return atom.get();
			}
			
			@Override
			public void remove (final T element){
				throw new UnsupportedOperationException("Not supported.");
			}
			
			@Override
			public void walk (final OrderedConsumer<T> consumer) {
				final AtomicBoolean atom;
				
				atom = new AtomicBoolean(false);
				
				GWXNodeCollection.this.walk((element) -> {
					if (element instanceof GWXNodeCollection collection) {
						collection.walk((e2) -> {
							final boolean state;
							
							state = consumer.accept((T)e2);
							
							if (state == false) {
								atom.set(true);
								
								return false;
							}
							
							return true;
						});
						
						return atom.get();
					} else {
						return true;
					}
				});
			}
		};
	}
	
	public final void walk (final Consumer<T> consumer){
		this.walk((object) -> {
			consumer.accept(object);
			
			return true;
		});
	}
	
	public final void walk (final Consumer<T> consumer, final Runnable callback){
		try {
			this.walk(consumer);
		} finally {
			callback.run();
		}
	}
	
	public abstract void walk (final OrderedConsumer<T> consumer);
	
	public final void walk (final OrderedConsumer<T> consumer, final Runnable callback){
		try {
			this.walk(consumer);
		} finally {
			callback.run();
		}
	}
	
	public final void walkReversed (final Consumer<T> consumer){
		this.walkReversed((object) -> {
			consumer.accept(object);
			
			return true;
		});
	}
	
	public final void walkReversed (final Consumer<T> consumer, final Runnable callback){
		try {
			this.walkReversed(consumer);
		} finally {
			callback.run();
		}
	}
	
	public abstract void walkReversed (final OrderedConsumer<T> consumer);
	
	public final void walkReversed (final OrderedConsumer<T> consumer, final Runnable callback){
		try {
			this.walkReversed(consumer);
		} finally {
			callback.run();
		}
	}
}