/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.synchronicity;

/**
 *
 * @author root-user
 */
import java.util.*;
import java.util.concurrent.*;

public final class ConcurrentFallbackMap<K,V>
        implements ConcurrentMap<K,V> {

    private final ConcurrentMap<K,V> primary = new ConcurrentHashMap<>();
    private final ConcurrentMap<K,V> fallback;

    public ConcurrentFallbackMap(ConcurrentMap<K,V> fallback) {
        this.fallback = fallback;
    }

    // ---- lookup with fallback ----
    @Override
    public V get(Object k) {
        V v = primary.get(k);
        return (v != null || primary.containsKey(k)) ? v : fallback.get(k);
    }

    @Override
    public boolean containsKey(Object k) {
        return primary.containsKey(k) || fallback.containsKey(k);
    }

    @Override
    public boolean containsValue(Object v) {
        return primary.containsValue(v) || fallback.containsValue(v);
    }

    // ---- write only to primary ----
    @Override public V put(K k, V v) { return primary.put(k, v); }
    @Override public V remove(Object k) { return primary.remove(k); }
    @Override public void clear() { primary.clear(); }
    @Override public V putIfAbsent(K k, V v) { return primary.putIfAbsent(k, v); }
    @Override public boolean remove(Object k, Object v) { return primary.remove(k, v); }
    @Override public boolean replace(K k, V oldV, V newV) { return primary.replace(k, oldV, newV); }
    @Override public V replace(K k, V v) { return primary.replace(k, v); }

    // ---- combined views ----
    @Override
    public int size() {
        return (int) entrySet().size();
    }

    @Override public void putAll(Map<? extends K,? extends V> m) {
        primary.putAll(m);
    }
	
    @Override
    public boolean isEmpty() {
        return primary.isEmpty() && fallback.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        Set<K> ks = new HashSet<>(fallback.keySet());
        ks.addAll(primary.keySet());
        return ks;
    }

    @Override
    public Collection<V> values() {
        List<V> vs = new ArrayList<>();
        for (K k : keySet()) vs.add(get(k));
        return vs;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Map<K,V> merged = new HashMap<>(fallback);
        merged.putAll(primary);
        return merged.entrySet();
    }
}
