/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.collections;

import org.junit.jupiter.api.Test;

import java.util.*;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

// TODO: NEED TO ADD SERIALIZE TEST WHEN IT GETS IMPLEMENTED
        /* ---- Map functionality test ----
        Methods Tested:
        - merge()
        - replace()
        - replaceAll()
        - forEach()
        - getOrDefault()
        - compute()
        - computeIfAbsent()
        - computeIfPresent()
        - clone()
        - isEmpty()
        - size()
        - put()
        - putAll()
        - containsKey()
        - containsValue()
        - clear()
        - entrySet()
        - keySet()
        - values()
        - get()
        */

public class CompactHashMapTest {

    @Test
    public void overrideReplaceTest() {
        CompactHashMap<String, String> mapTest = new CompactHashMap<>();

        // Put test
        put(mapTest);

        // Override Entry
        overrideEntry(mapTest);

        // Replace Test
        replace(mapTest);
    }

    @Test
    public void removeTest() {
        CompactHashMap<String, String> mapTest = new CompactHashMap<>();

        // Put test
        put(mapTest);

        // Remove Test
        remove(mapTest);
    }

    @Test
    public void setTest() {
        CompactHashMap<String, String> mapTest = new CompactHashMap<>();

        put(mapTest);

        // EntrySet Test
        entrySet(mapTest);

        // KeySet Test
        keySet(mapTest);

        // ValueSet Test
        valueSet(mapTest);
    }

    @Test
    public void equalsTest() {
        CompactHashMap<String, String> mapTest = new CompactHashMap<>();

        put(mapTest);

        // Equals test
        equalsMap(mapTest);
    }

    @Test
    public void forEachTest() {
        CompactHashMap<String, String> mapTest = new CompactHashMap<>();
        CompactHashMap<String, String> mapEqualsTest = new CompactHashMap<>();

        put(mapTest);
        put(mapEqualsTest);

        // ForEachTest
        forEach(mapTest, mapEqualsTest);
    }

    @Test
    public void clearTest() {
        CompactHashMap<String, String> mapTest = new CompactHashMap<>();

        put(mapTest);

        // Clear Test
        clear(mapTest);
    }

    @Test
    public void cloneNotSupportedTest() {
        // Clone Not Supported Test
        CompactHashMap<String, String> mapTest = new CompactHashMap<>();
        cloneNotSupported(mapTest);
    }

    @Test
    public void replaceAllTest() {
        CompactHashMap<String, String> mapTest = new CompactHashMap<>();

        // Replace All
        replaceAll(mapTest);
    }

    @Test
    public void mergeTest() {
        CompactHashMap<String, String> mapTest = new CompactHashMap<>();

        put(mapTest);

        // Merge Test
        merge(mapTest);
    }

    @Test
    public void putAndGetTest() {
        CompactHashMap<String, String> mapTest = new CompactHashMap<>();

        // isEmpty Test
        checkIsEmpty(mapTest);

        // Put test
        put(mapTest);

        // Get Test
        get(mapTest);

        // Contains Test
        contains(mapTest);
    }

    private void checkIsEmpty(CompactHashMap<String, String> mapTest) {
        assertThat(mapTest.size(), is(0));
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void exceptionTesting() {
        CompactHashMap<String, String> hashMap = new CompactHashMap<>();
        Set<Map.Entry<String, String>> entrySetTest = hashMap.entrySet();

        expect(() -> entrySetTest.contains(null)).toThrow(NullPointerException.class);

        Iterator<Map.Entry<String, String>> iteratorTest = entrySetTest.iterator();
        expect(iteratorTest::next).toThrow(NoSuchElementException.class);
    }

    @Test
    @SuppressWarnings({"EqualsWithItself", "EqualsBetweenInconvertibleTypes"})
    public void iterNextTest() {
        CompactHashMap<String, String> hashMap = new CompactHashMap<>();
        hashMap.put("keyTest", "valueTest");
        Set<Map.Entry<String, String>> entrySetTest = hashMap.entrySet();
        Iterator<Map.Entry<String, String>> iteratorTest = entrySetTest.iterator();
        Map.Entry<String, String> entryTest = iteratorTest.next();
        assertThat(entryTest.setValue("newSetValue"), is("valueTest"));
        assertThat(entryTest.hashCode(), is(Objects.hashCode(entryTest.getKey()) ^ Objects.hashCode(entryTest.getValue())));
        assertThat(entryTest.equals(entryTest), equalTo(true));
        assertThat(entryTest.equals("entryTest"), equalTo(false));
    }

    @Test
    public void entryFunctionalityTest() {
        CompactHashMap<String, String> hashMap = new CompactHashMap<>();

        // Put and get Entry
        hashMap.put("keyTest", "valueTest");
        Set<Map.Entry<String, String>> entrySetTest = hashMap.entrySet();
        Iterator<Map.Entry<String, String>> iteratorTest = entrySetTest.iterator();
        Map.Entry<String, String> entryTest = iteratorTest.next();
        assertThat(entrySetTest, hasItem(entryTest));

        // Replace value in map and check Entry
        hashMap.replace("keyTest", "valueTest", "newValueTest");
        assertThat(entryTest.getValue(), equalTo("newValueTest"));

        // Use put with same key in map and check Entry
        hashMap.put("keyTest", "newValueTest2");
        assertThat(entryTest.getValue(), equalTo("newValueTest2"));

        // Use the iterator to remove the entry and check map
        iteratorTest.remove();
        assertThat(hashMap.keySet(), not(hasItem("keyTest")));

        CompactHashMap<String, String> newHashMap = new CompactHashMap<>();
        newHashMap.put("newKeyTest", "newValueTest");
        Set<Map.Entry<String, String>> newEntrySetTest = newHashMap.entrySet();
        Iterator<Map.Entry<String, String>> newIteratorTest = newEntrySetTest.iterator();
        Map.Entry<String, String> newEntryTest = newIteratorTest.next();
        assertThat(entrySetTest, not(hasItem(newEntryTest)));

        // Check entry returns null for everything after removal in map
        assertThat(entryTest.getKey(), nullValue());
        assertThat(entryTest.getValue(), nullValue());
        assertThat(entryTest.setValue("newSetValue"), nullValue());

        // Check entry still maps to the same key after putting it back in the map
        hashMap.put("keyTest", "newValueTest3");
        assertThat(entryTest.getValue(), equalTo("newValueTest3"));

        // Entry equality test
        Map.Entry<String, String> entryTest2 = hashMap.entrySet().iterator().next();
        assertThat(entryTest, equalTo(entryTest2));
        hashMap.remove("keyTest");
    }

    private void clear(CompactHashMap<String, String> mapTest) {
        mapTest.clear();
        assertThat(mapTest.size(), is(0));
        checkIsEmpty(mapTest);
    }

    private void cloneNotSupported(CompactHashMap<String, String> mapTest) {
        expect(mapTest::clone).toThrow(CloneNotSupportedException.class);
    }

    private void merge(CompactHashMap<String, String> mapEqualsTest) {
        mapEqualsTest.merge("key2", "_value2", String::concat);
        assertThat(mapEqualsTest.get("key2"), equalTo("value2_value2"));
    }

    private void replaceAll(CompactHashMap<String, String> mapEqualsTest) {
        mapEqualsTest.replaceAll((k, v) -> ("replaced"));
        mapEqualsTest.forEach((key, value) -> assertThat(value, equalTo("replaced")));
    }

    private void forEach(CompactHashMap<String, String> mapTest, CompactHashMap<String, String> mapEqualsTest) {
        mapEqualsTest.forEach((key, value) -> assertThat(mapTest.get(key), equalTo(value)));
    }

    private void equalsMap(CompactHashMap<String, String> mapTest) {

        CompactHashMap<String, String> mapEqualsTest = new CompactHashMap<>();

        mapEqualsTest.put("key0", "0");

        mapEqualsTest.compute("key0", (key, value) -> "value0");
        mapEqualsTest.putIfAbsent("key1", "value1");
        mapEqualsTest.putIfAbsent("key1", "value1");
        mapEqualsTest.computeIfPresent("key1", (key, value) -> value);
        mapEqualsTest.putIfAbsent("key2", "value2");
        mapEqualsTest.putIfAbsent("key3", "value3");
        mapEqualsTest.computeIfPresent("key3", (key, value) -> value);
        mapEqualsTest.putIfAbsent("key4", "value4");
        mapEqualsTest.putIfAbsent("key5", "value5");
        mapEqualsTest.computeIfPresent("key5", (key, value) -> value);
        mapEqualsTest.putIfAbsent("key6", "value6");
        mapEqualsTest.putIfAbsent("key7", "value7");
        mapEqualsTest.computeIfPresent("key7", (key, value) -> value);
        mapEqualsTest.putIfAbsent("key8", "value8");
        mapEqualsTest.putIfAbsent("key9", "value9");
        mapEqualsTest.computeIfPresent("key9", (key, value) -> value);

        assertThat(mapEqualsTest, equalTo(mapTest));
    }

    private void valueSet(CompactHashMap<String, String> mapTest) {
        assertThat(mapTest.values(), containsInAnyOrder("value0", "value1", "value2", "value3", "value4", "value5", "value6", "value7", "value8", "value9"));
    }

    private void keySet(CompactHashMap<String, String> mapTest) {
        assertThat(mapTest.keySet(), containsInAnyOrder("key0", "key1", "key2", "key3", "key4", "key5", "key6", "key7", "key8", "key9"));
    }

    private void replace(CompactHashMap<String, String> mapTest) {
        mapTest.replace("key1", "first_replace");
        assertThat(mapTest.get("key1"), equalTo("first_replace"));
        mapTest.replace("key1", "first_replace", "replaced_value1");
        assertThat(mapTest.get("key1"), equalTo("replaced_value1"));
    }

    private void entrySet(CompactHashMap<String, String> mapTest) {
        Set<Map.Entry<String, String>> set = mapTest.entrySet();
        assertThat(mapTest.size(), is(10));
        assertThat(set.size(), is(10));

        for (Map.Entry<String, String> entry : mapTest.entrySet()) {
            switch (entry.getKey()) {
                case "key0":
                    assertThat(entry.getValue(), equalTo("value0"));
                    break;
                case "key1":
                    assertThat(entry.getValue(), equalTo("value1"));
                    break;
                case "key2":
                    assertThat(entry.getValue(), equalTo("value2"));
                    break;
                case "key3":
                    assertThat(entry.getValue(), equalTo("value3"));
                    break;
                case "key4":
                    assertThat(entry.getValue(), equalTo("value4"));
                    break;
                case "key5":
                    assertThat(entry.getValue(), equalTo("value5"));
                    break;
                case "key6":
                    assertThat(entry.getValue(), equalTo("value6"));
                    break;
                case "key7":
                    assertThat(entry.getValue(), equalTo("value7"));
                    break;
                case "key8":
                    assertThat(entry.getValue(), equalTo("value8"));
                    break;
                case "key9":
                    assertThat(entry.getValue(), equalTo("value9"));
                    break;
                default:
                    fail();
                    break;
            }
        }
    }

    private void remove(CompactHashMap<String, String> mapTest) {
        mapTest.remove("key0");
        assertThat(mapTest.keySet(), not(hasItem("key0")));
        assertThat(mapTest.values(), not(hasItem("value0")));
        assertThat(mapTest.get("key0"), nullValue());
        assertThat(mapTest.size(), is(9));

        mapTest.put("post_key0", "post_value0");
        assertThat(mapTest.keySet(), hasItem("post_key0"));
        assertThat(mapTest.values(), hasItem("post_value0"));
        assertThat(mapTest.get("post_key0"), equalTo("post_value0"));
    }

    private void overrideEntry(CompactHashMap<String, String> mapTest) {
        mapTest.put("key9", "new_value9");
        mapTest.put("key7", "new_value7");
        mapTest.put("key5", "new_value5");
        mapTest.put("key3", "new_value3");
        mapTest.put("key1", "new_value1");

        assertThat(mapTest.get("key1"), equalTo("new_value1"));
        assertThat(mapTest.get("key3"), equalTo("new_value3"));
        assertThat(mapTest.get("key5"), equalTo("new_value5"));
        assertThat(mapTest.get("key7"), equalTo("new_value7"));
        assertThat(mapTest.get("key9"), equalTo("new_value9"));
    }

    private void contains(CompactHashMap<String, String> mapTest) {

        assertThat(mapTest.keySet(), hasItem("key0"));
        assertThat(mapTest.keySet(), hasItem("key1"));
        assertThat(mapTest.keySet(), hasItem("key2"));
        assertThat(mapTest.keySet(), hasItem("key3"));
        assertThat(mapTest.keySet(), hasItem("key4"));
        assertThat(mapTest.keySet(), hasItem("key5"));
        assertThat(mapTest.keySet(), hasItem("key6"));
        assertThat(mapTest.keySet(), hasItem("key7"));
        assertThat(mapTest.keySet(), hasItem("key8"));
        assertThat(mapTest.keySet(), hasItem("key9"));
        assertThat(mapTest.keySet(), not(hasItem("key10")));

        assertThat(mapTest.values(), hasItem("value0"));
        assertThat(mapTest.values(), hasItem("value1"));
        assertThat(mapTest.values(), hasItem("value2"));
        assertThat(mapTest.values(), hasItem("value3"));
        assertThat(mapTest.values(), hasItem("value4"));
        assertThat(mapTest.values(), hasItem("value5"));
        assertThat(mapTest.values(), hasItem("value6"));
        assertThat(mapTest.values(), hasItem("value7"));
        assertThat(mapTest.values(), hasItem("value8"));
        assertThat(mapTest.values(), hasItem("value9"));
        assertThat(mapTest.values(), not(hasItem("value10")));
    }

    private void get(CompactHashMap<String, String> mapTest) {
        assertThat(mapTest.get("key0"), equalTo("value0"));
        assertThat(mapTest.get("key1"), equalTo("value1"));
        assertThat(mapTest.get("key2"), equalTo("value2"));
        assertThat(mapTest.get("key3"), equalTo("value3"));
        assertThat(mapTest.get("key4"), equalTo("value4"));
        assertThat(mapTest.get("key5"), equalTo("value5"));
        assertThat(mapTest.get("key6"), equalTo("value6"));
        assertThat(mapTest.get("key7"), equalTo("value7"));
        assertThat(mapTest.get("key8"), equalTo("value8"));
        assertThat(mapTest.get("key9"), equalTo("value9"));
        assertThat(mapTest.getOrDefault("key9", ""), equalTo("value9"));
        assertThat(mapTest.getOrDefault("key10", ""), equalTo(""));
    }

    private void put(CompactHashMap<String, String> mapTest) {

        CompactHashMap<String, String> mapEqualsTest = new CompactHashMap<>();

        mapTest.put("key0", "value0");
        mapTest.put("key1", "value1");
        mapTest.put("key2", "value2");
        mapTest.put("key3", "value3");
        mapTest.put("key4", "value4");
        mapTest.put("key5", "value5");
        mapTest.put("key6", "value6");
        mapTest.put("key7", "value7");
        mapTest.put("key8", "value8");
        mapTest.put("key9", "value9");

        assertThat(mapTest.size(), not(is(0)));

        mapEqualsTest.putAll(mapTest);
        assertThat(mapEqualsTest, equalTo(mapTest));
    }

    // This method can be used to compare performance between HashMap and CompactHashMapV2
    private void compareMaps() {

        /* ---- Java Implementation ---- */
//        HashMap<String, byte[]> map1 = new HashMap<>();
//        HashMap<String, byte[]> map2 = new HashMap<>();
//        HashMap<String, byte[]> map3 = new HashMap<>();
//        HashMap<String, byte[]> map4 = new HashMap<>();
//        HashMap<String, byte[]> map5 = new HashMap<>();

        /* ---- Compact Implementation ---- */

        CompactHashMap<String, byte[]> map1 = new CompactHashMap<>();
        CompactHashMap<String, byte[]> map2 = new CompactHashMap<>();
        CompactHashMap<String, byte[]> map3 = new CompactHashMap<>();
        CompactHashMap<String, byte[]> map4 = new CompactHashMap<>();
        CompactHashMap<String, byte[]> map5 = new CompactHashMap<>();
        CompactHashMap<String, byte[]> mapEqualsCheck1 = new CompactHashMap<>();

        int objectSize = 100;
        int numberOfObjects = 1000;
        byte[][] objectsArray = new byte[numberOfObjects][];

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfObjects; i++) {
            String id = "core" + i;
            objectsArray[i] = new byte[objectSize];
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Creating objects time: " + (endTime - startTime));

        startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfObjects; i++) {
            String key = "test" + i;
            map1.put(key, objectsArray[i]);
            map2.put(key, objectsArray[i]);
            map3.put(key, objectsArray[i]);
            map4.put(key, objectsArray[i]);
            map5.put(key, objectsArray[i]);
        }

        assertThat(map2.keySet(), hasItem("test2"));

        endTime = System.currentTimeMillis();
        System.out.println("Adding objects time: " + (endTime - startTime));

        startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfObjects; i++) {
            String key = "test" + i;

            assertThat(map1.get(key), equalTo(objectsArray[i]));
        }
        endTime = System.currentTimeMillis();
        System.out.println("Getting objects time: " + (endTime - startTime));

        // Equals Check
        for (int i = numberOfObjects - 1; i >= 0; i--) {
            String key = "test" + i;
            mapEqualsCheck1.put(key, objectsArray[i]);
        }

        assertThat(mapEqualsCheck1, equalTo(map1));

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
