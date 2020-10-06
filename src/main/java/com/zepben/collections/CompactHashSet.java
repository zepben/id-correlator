/*
 * Compact hash set
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
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A hash table based set that stores data as arrays of Objects.
 * <p>
 * * Pros:
 * - Less memory usage for underlying storage than java's default HashSet
 * implementation.
 * <p>
 * Cons:
 * - Small performance hit (needs proper benchmarking).
 * <p>
 * TODO: THIS CLASS DOESN'T REALLY IMPLEMENT SERIALIZABLE BECAUSE ONE OF ITS FIELDS IS NOT SERIALIZABLE, NAMELY THE objTable. BECAUSE THE Object CLASS DOES NOT IMPLEMENT SERIALIZABLE.
 */
@EverythingIsNonnullByDefault
public final class CompactHashSet<E> extends AbstractSet<E> implements Serializable {

    /*---- Fields ----*/

    private Object[] objTable = new Object[0];  // Length is always a power of 2. Each element is either null, tombstone, or data. At least one element must be null.
    private int lengthBits;  // Equal to log2(table.length)
    private int size;        // Number of items stored in hash table
    private int filled;      // Items plus tombstones; 0 <= size <= filled < table.length
    private int version;
    private final double loadFactor = 0.5;  // 0 < loadFactor < 1

    /*---- Constructors ----*/

    public CompactHashSet() {
        version = -1;
        clear();
    }

    /*---- Basic methods ----*/

    public void clear() {
        size = 0;
        objTable = new Object[size];
        version++;
        resize(1);
    }

    public int size() {
        return size;
    }

    public boolean add(E obj) {

        int index = probe(obj);
        if (index >= 0)
            return false;
        if (size == MAX_TABLE_LEN - 1)  // Because table.length is a power of 2, and at least one slot must be free
            throw new IllegalStateException("Maximum size reached");
        version++;
        index = ~index;
        if (objTable[index] != TOMBSTONE)
            filled++;
        objTable[index] = obj;
        incrementSize();
        if (filled == MAX_TABLE_LEN)
            resize(objTable.length);
        return true;
    }

    public boolean remove(Object obj) {

        int index = probe(obj);
        if (index >= 0) {
            version++;
            objTable[index] = TOMBSTONE;
            decrementSize();
            return true;
        } else
            return false;
    }

    /*---- Helper methods ----*/

    static private int hash(@Nullable Object obj) {
        int h;
        return (obj == null) ? 0 : (h = obj.hashCode()) ^ (h >>> 16);
    }

    // Returns either a match index (non-negative) or the bitwise complement of the first empty slot index (negative).
    private int probe(Object obj) {
        final int lengthMask = objTable.length - 1;
        final int hash = hash(obj);
        final int initIndex = hash & lengthMask;

        int emptyIndex = -1;
        Object item = objTable[initIndex];
        if (item == null)
            return ~initIndex;
        else if (item == TOMBSTONE)
            emptyIndex = initIndex;
        else if (obj.equals(item))
            return initIndex;

        int increment = Math.max((hash >>> lengthBits) & lengthMask, 1);
        int index = (initIndex + increment) & lengthMask;
        int start = index;
        while (true) {
            item = objTable[index];
            if (item == null) {
                if (emptyIndex != -1)
                    return ~emptyIndex;
                else
                    return ~index;
            } else if (item == TOMBSTONE) {
                if (emptyIndex == -1)
                    emptyIndex = index;
            } else if (obj.equals(item))
                return index;
            index = (index + 1) & lengthMask;
            if (index == start)
                throw new AssertionError();
        }
    }

    private void incrementSize() {
        size++;
        if (objTable.length < MAX_TABLE_LEN && (double) filled / objTable.length > loadFactor) {  // Refresh or expand hash table
            int newLen = objTable.length;
            while (newLen < MAX_TABLE_LEN && (double) size / newLen > loadFactor)
                newLen *= 2;
            resize(newLen);
        }
    }

    private void decrementSize() {
        size--;
        int newLen = objTable.length;
        while (newLen >= 2 && (double) size / newLen < loadFactor / 4 && size < newLen / 2)
            newLen /= 2;
        if (newLen < objTable.length)
            resize(newLen);
    }

    private void resize(int newLen) {
        if (newLen <= size)
            throw new AssertionError();
        Object[] oldObjTable = objTable;
        objTable = new Object[newLen];

        lengthBits = Integer.bitCount(newLen - 1);
        filled = size;

        for (Object obj : oldObjTable) {
            if (obj != null && obj != TOMBSTONE) {
                int index = probe(obj);
                if (index >= 0)
                    throw new AssertionError();
                objTable[~index] = obj;
            }
        }
    }

    /*---- Advanced methods ----*/

    @Override
    public Iterator<E> iterator() {
        return new Iter();
    }

    // Special placeholder reference for deleted slots.
    private static final byte[] TOMBSTONE = new byte[0];

    private static final int MAX_TABLE_LEN = 0x40000000;  // Largest power of 2 that fits in an int

    /*---- Helper classes ----*/

    @EverythingIsNonnullByDefault
    private final class Iter implements Iterator<E>, Serializable {

        private final int myVersion;
        private int currentIndex;
        private int nextIndex;

        Iter() {
            myVersion = version;
            currentIndex = -1;
            nextIndex = 0;
        }

        // Iterator methods

        @Override
        public boolean hasNext() {
            if (myVersion != version)
                throw new ConcurrentModificationException();
            while (true) {
                if (nextIndex >= objTable.length)
                    return false;
                else if (objTable[nextIndex] != null && objTable[nextIndex] != TOMBSTONE)
                    return true;
                else
                    nextIndex++;
            }
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public E next() {
            if (myVersion != version)
                throw new ConcurrentModificationException();
            if (!hasNext())
                throw new NoSuchElementException();
            currentIndex = nextIndex;
            nextIndex++;
            return (E) objTable[currentIndex];
        }

        public void remove() {
            if (myVersion != version)
                throw new ConcurrentModificationException();
            if (currentIndex == -1 || objTable[currentIndex] == TOMBSTONE)
                throw new IllegalStateException();
            objTable[currentIndex] = TOMBSTONE;
            size--;  // Note: Do not use decrementSize() because a table resize will screw up the iterator's indexing
        }

    }

}
