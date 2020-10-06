/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.idcorrelator;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TestUtils {

    public static void compareCorrelations(IdCorrelator idCorrelator1, IdCorrelator idCorrelator2) {
        Set<String> cores1 = idCorrelator1.cores();
        Set<String> cores2 = idCorrelator2.cores();

        assertThat(cores1.size(), equalTo(cores2.size()));
        assertThat(cores1.containsAll(cores2), equalTo(true));

        Set<String> systems1 = idCorrelator1.systems();
        Set<String> systems2 = idCorrelator2.systems();

        assertThat(systems1.size(), equalTo(systems2.size()));
        assertThat(systems1.containsAll(systems2), equalTo(true));

        cores1.forEach(core -> systems1.forEach(system -> {
            Set<String> ids1 = idCorrelator1.getIds(core, system);
            Set<String> ids2 = idCorrelator2.getIds(core, system);

            assertThat(ids1.size(), equalTo(ids2.size()));
            assertThat(ids1.containsAll(ids2), equalTo(true));

            ids1.forEach(id -> {
                Set<String> mappedCores1 = idCorrelator1.getCores(system, id);
                Set<String> mappedCores2 = idCorrelator2.getCores(system, id);

                assertThat(mappedCores1.size(), equalTo(mappedCores2.size()));
                assertThat(mappedCores1.containsAll(mappedCores2), equalTo(true));
            });
        }));
    }

}
