/*
 * Compact hash map
 *
 * Copyright (c) 2017 Project Nayuki
 * https://www.nayuki.io/page/compact-hash-map-java
 *
 * (MIT License)
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */

package com.zepben.collections;

import com.zepben.annotations.EverythingIsNonnullByDefault;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

/**
 * A hash table based map that stores data as arrays of Objects.
 * <p>
 * Pros:
 * - Less memory usage for underlying storage than java's default HashMap
 * implementation.
 * <p>
 * Cons:
 * - The maps entrySet is created on demand and is not cached.
 * This means calls to entrySet() can produce a lot of short lived objects
 * which will make the GC work harder.
 * - Small performance hit (needs proper benchmarking).
 * <p>
 * TODO: THIS CLASS DOESN'T REALLY IMPLEMENT SERIALIZABLE BECAUSE TWO OF ITS FIELDS IS NOT SERIALIZABLE, NAMELY keyTable and valueTable. BECAUSE THE Object CLASS DOES NOT IMPLEMENT SERIALIZABLE.
 */
@EverythingIsNonnullByDefault
public final class CompactHashMap<K, V> extends AbstractMap<K, V> implements Serializable {

    /*---- Fields ----*/

    private Object[] keyTable = new Object[0];  // Length is always a power of 2. Each element is either null, tombstone, or data. At least one element must be null.
    private Object[] valueTable = new Object[0];  // Length is always a power of 2. Each element is either null, tombstone, or data. At least one element must be null.
    private int lengthBits;  // Equal to log2(table.length)
    private int size;        // Number of items stored in hash table
    private int filled;      // Items plus tombstones; 0 <= size <= filled < table.length
    private int version;
    private final double loadFactor = 0.5;  // 0 < loadFactor < 1

    /*---- Constructors ----*/

    public CompactHashMap() {
        version = -1;
        clear();
    }

    /*---- Basic methods ----*/

    public void clear() {
        size = 0;
        keyTable = new Object[0];
        valueTable = new Object[0];
        resize(1);
    }

    public int size() {
        return size;
    }

    public boolean containsKey(Object key) {
        return probe(key) >= 0;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public V get(Object key) {
        int index = probe(key);
        if (index >= 0)
            return (V) valueTable[index];
        else
            return null;
    }

    @SuppressWarnings("unchecked")
    public V put(K key, @Nullable V value) {

        int index = probe(key);
        boolean isNew = index < 0;
        Object result = isNew ? null : valueTable[index];
        if (isNew) {
            if (size == MAX_TABLE_LEN - 1)  // Because table.length is a power of 2, and at least one slot must be free
                throw new IllegalStateException("Maximum size reached");
            index = ~index;
            if (keyTable[index] != TOMBSTONE) {
                filled++;
            }
        }
        keyTable[index] = key;
        valueTable[index] = value;
        if (isNew) {
            incrementSize();
            if (filled == MAX_TABLE_LEN)
                resize(keyTable.length);
        }
        return (V) result;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public V remove(Object key) {

        int index = probe(key);
        if (index >= 0) {
            Object result = valueTable[index];
            keyTable[index] = TOMBSTONE;
            valueTable[index] = TOMBSTONE;
            decrementSize();
            return (V) result;
        } else
            return null;
    }

    /*---- Helper methods ----*/

    private int hash(Object key) {
        int h;
        return (h = key.hashCode()) ^ (h >>> 16);
    }

    // Returns either a match index (non-negative) or the bitwise complement of the first empty slot index (negative).
    private int probe(Object key) {
        final int lengthMask = keyTable.length - 1;
        final int hash = hash(key);
        final int initIndex = hash & lengthMask;

        int emptyIndex = -1;
        Object item = keyTable[initIndex];
        if (item == null)
            return ~initIndex;
        else if (item == TOMBSTONE)
            emptyIndex = initIndex;
        else if (key.equals(item))
            return initIndex;

        int increment = Math.max((hash >>> lengthBits) & lengthMask, 1);
        int index = (initIndex + increment) & lengthMask;
        int start = index;
        while (true) {
            item = keyTable[index];
            if (item == null) {
                if (emptyIndex != -1)
                    return ~emptyIndex;
                else
                    return ~index;
            } else if (item == TOMBSTONE) {
                if (emptyIndex == -1)
                    emptyIndex = index;
            } else if (key.equals(item))
                return index;
            index = (index + 1) & lengthMask;
            if (index == start)
                throw new AssertionError();
        }
    }

    private void incrementSize() {
        size++;
        if (keyTable.length < MAX_TABLE_LEN && (double) filled / keyTable.length > loadFactor) {  // Refresh or expand hash keyTable
            int newLen = keyTable.length;
            while (newLen < MAX_TABLE_LEN && (double) size / newLen > loadFactor)
                newLen *= 2;
            resize(newLen);
        }
    }

    private void decrementSize() {
        size--;
        int newLen = keyTable.length;
        while (newLen >= 2 && (double) size / newLen < loadFactor / 4 && size < newLen / 2)
            newLen /= 2;
        if (newLen < keyTable.length)
            resize(newLen);
    }

    private void resize(int newLen) {
        version++;

        if (newLen <= size)
            throw new AssertionError();
        Object[] oldKeyTable = keyTable;
        Object[] oldValueTable = valueTable;

        keyTable = new Object[newLen];
        valueTable = new Object[newLen];

        lengthBits = Integer.bitCount(newLen - 1);
        filled = size;

        for (int i = 0; i < oldKeyTable.length; i++) {
            Object key = oldKeyTable[i];

            if (key != null && key != TOMBSTONE) {
                int index = probe(key);
                if (index >= 0)
                    throw new AssertionError();
                keyTable[~index] = key;
                valueTable[~index] = oldValueTable[i];
            }
        }
    }

    /*---- Advanced methods ----*/

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    // Special placeholder reference for deleted slots.
    private static final Object TOMBSTONE = new Object();

    private static final int MAX_TABLE_LEN = 0x40000000;  // Largest power of 2 that fits in an int

    /*---- Helper classes ----*/

    // Clone not supported.
    // http://stackoverflow.com/questions/2356809/shallow-copy-of-a-map-in-java
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    // For the entrySet() method.
    @EverythingIsNonnullByDefault
    private final class EntrySet extends AbstractSet<Map.Entry<K, V>> implements Serializable {

        public int size() {
            return size;
        }

        public boolean contains(Object obj) {
            if (!(obj instanceof Map.Entry))
                throw new NullPointerException();
            @SuppressWarnings("unchecked")
            Map.Entry<K, V> entry = (Map.Entry<K, V>) obj;
            K key = entry.getKey();
            if (key == null)
                throw new NullPointerException();
            if (!CompactHashMap.this.containsKey(key))
                return false;
            V val0 = entry.getValue();
            V val1 = CompactHashMap.this.get(key);
            return val0 == null && val1 == null || val0 != null && val0.equals(val1);
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new Iter();
        }

        @EverythingIsNonnullByDefault
        private final class Iter implements Iterator<Map.Entry<K, V>>, Serializable {

            private final int myVersion;
            private int currentIndex;
            private int nextIndex;

            Iter() {
                myVersion = version;
                currentIndex = -1;
                nextIndex = 0;
            }

            // Iterator methods
            public boolean hasNext() {
                while (true) {
                    if (nextIndex >= keyTable.length)
                        return false;
                    else if (keyTable[nextIndex] != null && keyTable[nextIndex] != TOMBSTONE)
                        return true;
                    else
                        nextIndex++;
                }
            }

            public Map.Entry<K, V> next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                currentIndex = nextIndex;
                nextIndex++;

                return new Map.Entry<K, V>() {
                    // Map.Entry methods
                    private int idx = currentIndex;
                    private int ver = myVersion;
                    @SuppressWarnings("unchecked") private final K key = (K) keyTable[idx];

                    @Nullable
                    @Override
                    public K getKey() {
                        updateDueToResize();
                        if (idx >= keyTable.length || idx < 0)
                            return null;

                        if (!Objects.equals(keyTable[idx], key))
                            return null;

                        return key;
                    }

                    @SuppressWarnings("unchecked")
                    @Nullable
                    @Override
                    public V getValue() {
                        updateDueToResize();
                        if (idx >= keyTable.length || idx < 0)
                            return null;

                        if (!Objects.equals(keyTable[idx], key))
                            return null;

                        return (V) valueTable[idx];
                    }

                    @SuppressWarnings("unchecked")
                    @Nullable
                    @Override
                    public V setValue(V value) {
                        updateDueToResize();

                        if (idx >= keyTable.length || idx < 0)
                            return null;

                        if (keyTable[idx] == TOMBSTONE || !Objects.equals(keyTable[idx], key))
                            return null;

                        V oldValue = (V) valueTable[idx];
                        valueTable[idx] = value;
                        return oldValue;
                    }

                    private void updateDueToResize() {
                        if (ver != version) {
                            idx = probe(key);
                            ver = version;
                        }
                    }

                    @Override
                    public final int hashCode() {
                        return Objects.hashCode(key) ^ Objects.hashCode(getValue());
                    }

                    @Override
                    public final boolean equals(Object o) {
                        if (o == this)
                            return true;
                        if (o instanceof Map.Entry) {
                            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                            return Objects.equals(key, e.getKey()) &&
                                Objects.equals(getValue(), e.getValue());
                        }
                        return false;
                    }
                };
            }

            @Override
            public void remove() {
                if (currentIndex == -1 || keyTable[currentIndex] == TOMBSTONE)
                    throw new IllegalStateException();
                keyTable[currentIndex] = TOMBSTONE;
                valueTable[currentIndex] = null;
                size--;  // Note: Do not use decrementSize() because a table resize will screw up the iterator's indexing
            }

        }

    }

}
