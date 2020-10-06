/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.collections;

import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

public class CompactHashSetTest {

    // TODO: NEED TO ADD SERIALIZE TEST WHEN IT GETS IMPLEMENTED
        /* ---- Set functionality test ----
        Methods Tested:
        - forEach();
        - clear();
        - size();
        - equals();
        - remove();
        - removeAll();
        - iterator();
        - contains();
        - containsAll();
        - isEmpty();
        - addAll();
        - add();
        */

    @Test
    public void addAndContainsTest() {
        CompactHashSet<String> hashSet = new CompactHashSet<>();
        ArrayList<String> arrayList = new ArrayList<>();

        // isEmpty Test
        checkIsEmpty(hashSet);

        // Add Test
        add(hashSet, arrayList);

        // Contains Test
        contains(hashSet, arrayList);
    }

    private void checkIsEmpty(CompactHashSet<String> hashSet) {
        assertThat(hashSet.size(), is(0));
    }

    @Test
    public void iteratorTest() {
        CompactHashSet<String> hashSet = new CompactHashSet<>();
        ArrayList<String> arrayList = new ArrayList<>();

        add(hashSet, arrayList);

        // Testing Iterator
        iterator(hashSet);
    }

    @Test
    public void removeTest() {
        CompactHashSet<String> hashSet = new CompactHashSet<>();
        ArrayList<String> arrayList = new ArrayList<>();

        add(hashSet, arrayList);

        // Remove Test
        remove(hashSet, arrayList);
    }

    @Test
    public void equalsTest() {
        CompactHashSet<String> hashSet = new CompactHashSet<>();
        ArrayList<String> arrayList = new ArrayList<>();

        add(hashSet, arrayList);

        // Clear test
        clear(hashSet);
    }

    @Test
    public void clearTest() {
        CompactHashSet<String> hashSet = new CompactHashSet<>();
        ArrayList<String> arrayList = new ArrayList<>();

        add(hashSet, arrayList);

        // Equals Test
        equalsSet(hashSet);
    }

    @Test
    public void obscureEdgeCaseCoverage() {
        CompactHashSet<Integer> ints = new CompactHashSet<>();
        CompactHashSet<Double> doubles = new CompactHashSet<>();

        // Should not be adding nulls but they are handled.
        assertThat(ints.add(null), equalTo(true));
        assertThat(doubles.add(null), equalTo(true));

        // Removing items that do not exist.
        assertThat(ints.remove(1), equalTo(false));

        // Adding items twice
        assertThat(ints.add(1), equalTo(true));
        assertThat(ints.add(1), equalTo(false));

        // Add an item that has already been removed.
        assertThat(ints.remove(1), equalTo(true));
        assertThat(ints.add(1), equalTo(true));

        // NOTE: val1 and val2 have colliding hashes in the CompactHasSet.
        double val1 = 4.9E-324;
        double val2 = 40.61106112799864;

        assertThat(doubles.add(val1), equalTo(true));
        assertThat(doubles.add(val2), equalTo(true));
        assertThat(doubles.remove(val2), equalTo(true));
        assertThat(doubles.add(val2), equalTo(true));
        assertThat(doubles.add(val2), equalTo(false));


        IntStream.range(0, 1000).forEach(i -> {
            ints.add(i);
            doubles.add(i * 1.0);
        });

        IntStream.range(0, 1000).forEach(i -> {
            ints.remove(i);
            doubles.remove(i * 1.0);
        });
    }

    static private int hash(@Nullable Object obj) {
        int h;
        return (obj == null) ? 0 : (h = obj.hashCode()) ^ (h >>> 16);
    }

    private void clear(CompactHashSet<String> hashSet) {
        hashSet.clear();
        checkIsEmpty(hashSet);
        assertThat(hashSet.size(), equalTo(0));
    }

    private void equalsSet(CompactHashSet<String> hashSet) {
        CompactHashSet<String> hashSet2 = new CompactHashSet<>();
        hashSet2.add("6");
        hashSet2.add("5");
        hashSet2.add("4");
        hashSet2.add("3");
        hashSet2.add("2");
        hashSet2.add("1");
        hashSet2.add("0");
        hashSet2.add("7");
        hashSet2.add("8");
        hashSet2.add("9");

        assertThat(hashSet, equalTo(hashSet2));
        hashSet2.add("other");
        assertThat(hashSet, not(equalTo(hashSet2)));

        hashSet.forEach(s ->
            assertThat((s.equals("0") ||
                s.equals("1") ||
                s.equals("2") ||
                s.equals("3") ||
                s.equals("4") ||
                s.equals("5") ||
                s.equals("6") ||
                s.equals("7") ||
                s.equals("8") ||
                s.equals("9")), equalTo(true)));
    }

    private void remove(CompactHashSet<String> hashSet, ArrayList<String> arrayList) {
        hashSet.removeAll(arrayList);
        assertThat(hashSet.size(), is(7));
        assertThat(hashSet, containsInAnyOrder("0", "1", "2", "3", "4", "5", "6"));
        hashSet.remove("0");
        assertThat(hashSet.size(), is(6));
        assertThat(hashSet, containsInAnyOrder("1", "2", "3", "4", "5", "6"));
    }

    private void iterator(CompactHashSet<String> hashSet) {
        Iterator<String> iteratorHashSet = hashSet.iterator();

        while (iteratorHashSet.hasNext()) {
            String setElement = iteratorHashSet.next();

            switch (setElement) {
                case "0":
                case "1":
                case "2":
                case "3":
                case "4":
                case "5":
                case "6":
                case "7":
                case "8":
                    break;
                case "9":
                    iteratorHashSet.remove();
                    break;
                default:
                    fail();
                    break;
            }
        }
        assertThat(hashSet.size(), is(9));
        assertThat(hashSet, containsInAnyOrder("0", "1", "2", "3", "4", "5", "6", "7", "8"));
    }

    private void contains(CompactHashSet<String> hashSet, ArrayList<String> arrayList) {
        assertThat(hashSet.containsAll(arrayList), is(true));
        arrayList.add("else");
        assertThat(hashSet.containsAll(arrayList), is(false));

        assertThat(hashSet, hasItem("0"));
        assertThat(hashSet, hasItem("1"));
        assertThat(hashSet, hasItem("2"));
        assertThat(hashSet, hasItem("3"));
        assertThat(hashSet, hasItem("4"));
        assertThat(hashSet, hasItem("5"));
        assertThat(hashSet, hasItem("6"));
        assertThat(hashSet, hasItem("7"));
        assertThat(hashSet, hasItem("8"));
        assertThat(hashSet, hasItem("9"));
        assertThat(hashSet, not(hasItem("10")));

    }

    private void add(CompactHashSet<String> hashSet, ArrayList<String> arrayList) {
        hashSet.add("0");
        hashSet.add("1");
        hashSet.add("2");
        hashSet.add("3");
        hashSet.add("4");
        hashSet.add("5");
        hashSet.add("6");
        arrayList.add("7");
        arrayList.add("8");
        arrayList.add("9");
        hashSet.addAll(arrayList);

        assertThat(hashSet.size(), is(10));
        assertThat(hashSet, containsInAnyOrder("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
        assertThat(hashSet.size(), not(equalTo(0)));
    }

    // This method can be used to compare performance between HashSet and CompactHashSetV2
    private void compareSets() {

        /* ---- Java Implementation ---- */
//        HashSet<String> set1 = new HashSet<>();
//        HashSet<String> set2 = new HashSet<>();
//        HashSet<String> set3 = new HashSet<>();
//        HashSet<String> set4 = new HashSet<>();
//        HashSet<String> set5 = new HashSet<>();

        /* ---- Compact Implementation ---- */
        CompactHashSet<String> set1 = new CompactHashSet<>();
        CompactHashSet<String> set2 = new CompactHashSet<>();
        CompactHashSet<String> set3 = new CompactHashSet<>();
        CompactHashSet<String> set4 = new CompactHashSet<>();
        CompactHashSet<String> set5 = new CompactHashSet<>();

        int numberOfStrings = 1000;

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfStrings; i++) {
            String str = "test" + i;

            set1.add(str);
            set2.add(str);
            set3.add(str);
            set4.add(str);
            set5.add(str);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Adding strings time: " + (endTime - startTime));

        startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfStrings; i++) {
            String str = "test" + i;

            assertThat(set1, hasItem(str));
            assertThat(set2, hasItem(str));
            assertThat(set3, hasItem(str));
            assertThat(set4, hasItem(str));
            assertThat(set5, hasItem(str));

        }
        endTime = System.currentTimeMillis();
        System.out.println("Getting strings time: " + (endTime - startTime));

        // Uncomment this section to make the thread sleep (Used to see memory consumption on VisualVM)
//        try {
//            System.out.println("sleeping.");
//            Thread.sleep(Long.MAX_VALUE);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        System.out.println("Done.");
    }

}
