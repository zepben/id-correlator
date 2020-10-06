/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.idcorrelator;

import com.zepben.collections.CompactHashMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * Contains code to benchmark {@link IdCorrelator} implementations.
 * <p>
 * To run the benchmark take out the @Ignore annotation and use these VM options:
 * To allows the use of 8G of memory:
 * -Xms8g -Xmx8g
 * To print the garbage collector activity:
 * -XX:+PrintGC
 * </p>
 */
public class IdCorrelatorBenchMark {

    private static final Path ID_CORRELATOR_FILE_PATH = Paths.get("src/test/data/id_correlations.json");

    @BeforeEach
    public void deleteIdCorrelatorFileBefore() throws IOException {
        Files.deleteIfExists(ID_CORRELATOR_FILE_PATH);
    }

    @AfterAll
    public static void deleteIdCorrelatorFileAfter() throws IOException {
        Files.deleteIfExists(ID_CORRELATOR_FILE_PATH);
    }

    /*
    Benchmarks:

    date: Thu Apr 20 09:00:07 AEST 2017
    runsPerBenchMark: 10
    numCoreAssets: 100000
    map: CompactHashMap
    set: CompactHashSet
    ______________________________________________
    || Run ||  Put Time[ms]   ||   Get Time[ms]  ||
    ||  *  || Min | Avg | Max || Min | Avg | Max ||
    -----------------------------------------------
    ||  0  || 274 | 303 | 371 || 237 | 264 | 438 ||
    ||  1  || 284 | 305 | 353 || 232 | 269 | 395 ||
    ||  2  || 271 | 292 | 315 || 236 | 269 | 328 ||
    ||  3  || 280 | 301 | 342 || 233 | 254 | 318 ||
    ||  4  || 280 | 314 | 363 || 235 | 281 | 339 ||
    ||  5  || 277 | 303 | 379 || 233 | 264 | 377 ||
    ||  6  || 284 | 315 | 361 || 234 | 278 | 351 ||
    ||  7  || 274 | 294 | 338 || 231 | 260 | 326 ||
    ||  8  || 279 | 313 | 403 || 234 | 274 | 326 ||
    ||  9  || 277 | 295 | 356 || 232 | 274 | 363 ||
    -----------------------------------------------
     */
    @Test
    @Disabled
    public void benchmarkCorrelators() {

        // Benchmark Parameters
        String mapName = "CompactHashMap";
        String setName = "CompactHashSet";
        Date date = new Date();
        int numCoreAssets = 100000;
        int numBenchmarks = 10;
        int runsPerBenchMark = 10;

        /* WARM UP STARTS*/
        // initializing core and string arrays
        int numWarmUpAssets = 10;
        int numCalls = 1000000;
        String[] warmUpCoreArray = new String[numWarmUpAssets];
        String[] warmUpArray0 = new String[numWarmUpAssets];
        String[] warmUpArray1 = new String[numWarmUpAssets * 2];
        String[] warmUpArray2 = new String[numWarmUpAssets / 2];

        for (int i = 0; i < numWarmUpAssets; i++)
            warmUpCoreArray[i] = "core_" + i;

        for (int i = 0; i < numWarmUpAssets; i++)
            warmUpArray0[i] = ("s0_id" + i);

        for (int i = 0; i < numWarmUpAssets * 2; i++)
            warmUpArray1[i] = ("s1_id" + i);

        for (int i = 0; i < numWarmUpAssets / 2; i++)
            warmUpArray2[i] = ("s2_id" + i);

        System.out.println("Warm Up Starts");
        for (int i = 0; i < numCalls; i++)
            benchmarkCorrelators(CompactHashMap::new, HashSet::new, numWarmUpAssets, warmUpCoreArray, warmUpArray0, warmUpArray1, warmUpArray2);
        System.out.println("Warm Up Ends");

        long[][] runResults = new long[10][2];

        // Printing benchmark parameters
        System.out.printf("date: %s\n" +
                "runsPerBenchmark: %d\n" +
                "numCoreAssets: %d\n" +
                "map: %s\n" +
                "set: %s\n",
            date.toString(),
            runsPerBenchMark,
            numCoreAssets,
            mapName,
            setName);

        // Printing start of table
        System.out.println(
            "______________________________________________\n" +
                "|| Run ||  Put Time[ms]   ||   Get Time[ms]  ||\n" +
                "||  *  || Min | Avg | Max || Min | Avg | Max ||\n" +
                "-----------------------------------------------");

        // Creating data set for benchmarking
        // numCoreAssets must be an even number
        //noinspection ConstantConditions
        if (numCoreAssets % 2 != 0)
            throw new IllegalArgumentException("numCoreAssets must be an even number.");

        // initializing core and string arrays
        String[] coreArray = new String[numCoreAssets];
        String[] array0 = new String[numCoreAssets];
        String[] array1 = new String[numCoreAssets * 2];
        String[] array2 = new String[numCoreAssets / 2];

        for (int i = 0; i < numCoreAssets; i++)
            coreArray[i] = "core_" + i;

        for (int i = 0; i < numCoreAssets; i++)
            array0[i] = ("s0_id" + i);

        for (int i = 0; i < numCoreAssets * 2; i++)
            array1[i] = ("s1_id" + i);

        for (int i = 0; i < numCoreAssets / 2; i++)
            array2[i] = ("s2_id" + i);

        for (int i = 0; i < numBenchmarks; i++) {
            for (int j = 0; j < runsPerBenchMark; j++) {
                runResults[j] = benchmarkCorrelators(CompactHashMap::new, HashSet::new, numCoreAssets, coreArray, array0, array1, array2);
            }

            long[] writeArray = new long[runsPerBenchMark];
            long[] readArray = new long[runsPerBenchMark];

            for (int j = 0; j < runsPerBenchMark; j++) {
                writeArray[j] = runResults[j][0];
                readArray[j] = runResults[j][1];
            }

            long writeMin = min(writeArray);
            long writeMax = max(writeArray);
            long writeAvg = avg(writeArray);
            long readMin = min(readArray);
            long readMax = max(readArray);
            long readAvg = avg(readArray);

            // Printing benchmark results for run
            System.out.printf("||  %d  || %d | %d | %d || %d | %d | %d ||\n",
                i,
                writeMin,
                writeAvg,
                writeMax,
                readMin,
                readAvg,
                readMax);
        }
        // Printing end of table
        System.out.println("-----------------------------------------------");
    }

    private long max(long[] array) {
        long max = array[0];
        for (int i = 1; i < array.length; i++)
            if (array[i] > max) {
                max = array[i];
            }
        return max;
    }

    private long min(long[] array) {
        long min = array[0];
        for (int i = 1; i < array.length; i++)
            if (array[i] < min) {
                min = array[i];
            }
        return min;
    }

    private long avg(long[] array) {
        long avg = array[0];
        for (int i = 1; i < array.length; i++)
            avg += array[i];
        avg /= array.length;
        return avg;
    }

    private long[] benchmarkCorrelators(MapBackedIdCorrelator.MapFactory mapFactory, MapBackedIdCorrelator.SetFactory setFactory, int numCoreAssets, String[] coreArray, String[] array0, String[] array1, String[] array2) {
        long[] putGetTime = new long[2];
        IdCorrelator idCorrelator = new MapBackedIdCorrelator(mapFactory, setFactory);
        for (int i = 0; i < numCoreAssets; i++)
            idCorrelator.addCore(coreArray[i]);

        // Functional interfaces for putting and getting the ids
        BiConsumer<Integer, IdCorrelator> correlatorPutter = (myIndex, inIdCorrelator) -> {
            String systemTag = ("system_" + myIndex);
            inIdCorrelator.addSystem(systemTag);

            if (myIndex % 3 == 0)
                for (int i = 0; i < numCoreAssets; i++)
                    inIdCorrelator.addCorrelation(coreArray[i], systemTag, array0[i]);
            else if (myIndex % 3 == 1)
                for (int i = 0; i < numCoreAssets * 2; i++)
                    inIdCorrelator.addCorrelation(coreArray[i / 2], systemTag, array1[i]);
            else if (myIndex % 3 == 2) {
                int coreCounter = 0;
                for (int i = 0; i < numCoreAssets / 2; i++) {
                    inIdCorrelator.addCorrelation(coreArray[coreCounter++], systemTag, array2[i]);
                    inIdCorrelator.addCorrelation(coreArray[coreCounter++], systemTag, array2[i]);
                }
            }
        };

        BiConsumer<Integer, IdCorrelator> correlatorGetter = (myIndex, inIdCorrelator) -> {
            String systemTag = ("system_" + myIndex);
            Set<String> cores;

            if (myIndex % 3 == 0)
                for (int i = 0; i < numCoreAssets; i++) {
                    cores = inIdCorrelator.getCores(systemTag, array0[i]);
                    assertThat(cores, containsInAnyOrder(coreArray[i]));
                }
            else if (myIndex % 3 == 1)
                for (int i = 0; i < numCoreAssets * 2; i++) {
                    cores = inIdCorrelator.getCores(systemTag, array1[i]);
                    assertThat(cores, containsInAnyOrder(coreArray[i / 2]));
                }
            else if (myIndex % 3 == 2) {
                int coreCounter = 0;
                for (int i = 0; i < numCoreAssets / 2; i++) {
                    cores = inIdCorrelator.getCores(systemTag, array2[i]);
                    assertThat(cores, containsInAnyOrder(coreArray[coreCounter], coreArray[(coreCounter + 1)]));
                    coreCounter++;
                    coreCounter++;
                }
            }
        };

        /* ---- Benchmarking Starts ---- */

        // Putting correlations into the IdCorrelator
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numCoreAssets; i++)
            idCorrelator.addCore(coreArray[i]);
        correlatorPutter.accept(0, idCorrelator);
        correlatorPutter.accept(1, idCorrelator);
        correlatorPutter.accept(2, idCorrelator);
        long endTime = System.currentTimeMillis();
        putGetTime[0] = (endTime - startTime);

        // Getting correlations from the IdCorrelator
        startTime = System.currentTimeMillis();
        correlatorGetter.accept(0, idCorrelator);
        correlatorGetter.accept(1, idCorrelator);
        correlatorGetter.accept(2, idCorrelator);
        endTime = System.currentTimeMillis();
        putGetTime[1] = (endTime - startTime);

        // Uncomment this section to make the thread sleep (Used to see memory consumption on VisualVM)
//        System.out.println("sleeping");
//        try {
//            Thread.sleep(Long.MAX_VALUE);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        return putGetTime;
    }

}
