/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.idcorrelator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static com.zepben.idcorrelator.TestUtils.compareCorrelations;
import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class IdCorrelatorTest {

    /**
     * Tests the functionality of the class {@link MapBackedIdCorrelator}
     * <p>
     * Systems correlated:
     * CORE -> {GIS,DMS, OMS, CIS}
     */
    private static final Path ID_CORRELATOR_FILE_PATH = Paths.get("src/test/data/id_correlations.json");

    @BeforeEach
    public void deleteIdCorrelatorFileBefore() throws IOException {
        Files.deleteIfExists(ID_CORRELATOR_FILE_PATH);
    }

    @AfterAll
    public static void deleteIdCorrelatorFileAfter() throws IOException {
        Files.deleteIfExists(ID_CORRELATOR_FILE_PATH);
    }

    @Test
    public void correlationsTest() {
        IdCorrelator idCorrelator = newTestCorrelator();

        Set<String> coreIds;
        Iterator<String> codeIdIterator;

        // ---- CIS to Everything ---
        //GIS
        coreIds = idCorrelator.getCores("CIS", "C1");
        codeIdIterator = coreIds.iterator();
        assertThat(coreIds, containsInAnyOrder("1", "2", "3"));
        assertThat(idCorrelator.getIds(codeIdIterator.next(), "GIS"), containsInAnyOrder(Collections.emptySet()));
        assertThat(idCorrelator.getIds(codeIdIterator.next(), "GIS"), containsInAnyOrder("G2"));
        assertThat(idCorrelator.getIds(codeIdIterator.next(), "GIS"), containsInAnyOrder("G1"));

        //DMS
        coreIds = idCorrelator.getCores("CIS", "C1");
        codeIdIterator = coreIds.iterator();
        assertThat(coreIds, containsInAnyOrder("1", "2", "3"));
        assertThat(idCorrelator.getIds(codeIdIterator.next(), "DMS"), containsInAnyOrder(Collections.emptySet()));
        assertThat(idCorrelator.getIds(codeIdIterator.next(), "DMS"), containsInAnyOrder("D4", "D3"));
        assertThat(idCorrelator.getIds(codeIdIterator.next(), "DMS"), containsInAnyOrder("D2", "D6"));

        //OMS
        coreIds = idCorrelator.getCores("CIS", "C1");
        codeIdIterator = coreIds.iterator();
        assertThat(coreIds, containsInAnyOrder("1", "2", "3"));
        assertThat(idCorrelator.getIds(codeIdIterator.next(), "OMS"), containsInAnyOrder(Collections.emptySet()));
        assertThat(idCorrelator.getIds(codeIdIterator.next(), "OMS"), containsInAnyOrder("O5", "O6"));
        assertThat(idCorrelator.getIds(codeIdIterator.next(), "OMS"), containsInAnyOrder("O1", "O2"));

        // ---- DMS to Core ----
        // D4
        coreIds = idCorrelator.getCores("DMS", "D4");
        assertThat(coreIds, containsInAnyOrder("2"));

        // D3
        coreIds = idCorrelator.getCores("DMS", "D3");
        assertThat(coreIds, containsInAnyOrder("2"));

        // D6
        coreIds = idCorrelator.getCores("DMS", "D6");
        assertThat(coreIds, containsInAnyOrder("3"));

        // D2
        coreIds = idCorrelator.getCores("DMS", "D2");
        assertThat(coreIds, containsInAnyOrder("3"));

        // ---- OMS to Core -----
        // O1
        coreIds = idCorrelator.getCores("OMS", "O1");
        assertThat(coreIds, containsInAnyOrder("3"));

        // O2
        coreIds = idCorrelator.getCores("OMS", "O2");
        assertThat(coreIds, containsInAnyOrder("3"));

        // O5
        coreIds = idCorrelator.getCores("OMS", "O5");
        assertThat(coreIds, containsInAnyOrder("2"));

        // O6
        coreIds = idCorrelator.getCores("OMS", "O6");
        assertThat(coreIds, containsInAnyOrder("2"));

        // ---- GIS to Core ----
        // G1
        coreIds = idCorrelator.getCores("GIS", "G1");
        assertThat(coreIds, containsInAnyOrder("3"));

        // G2
        coreIds = idCorrelator.getCores("GIS", "G2");
        assertThat(coreIds, containsInAnyOrder("2"));
    }

    @Test
    public void equalsTest() {
        IdCorrelator idCorrelator1 = newTestCorrelator();
        IdCorrelator idCorrelator2 = newTestCorrelator();

        compareCorrelations(idCorrelator1, idCorrelator1);
        compareCorrelations(idCorrelator1, idCorrelator2);

        idCorrelator1.removeCore("4");

        expect(() -> compareCorrelations(idCorrelator1, idCorrelator2)).toThrow(AssertionError.class);
    }

    @Test
    public void removingSystemTest() {
        IdCorrelator idCorrelator = newTestCorrelator();

        idCorrelator.removeSystem("OTHER");
        assertThat(idCorrelator.systems().size(), is(4));
        assertThat(idCorrelator.systems(), containsInAnyOrder("GIS", "DMS", "OMS", "CIS"));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getIds("4", "OTHER")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getCores("OTHER", "otherId1")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getCores("OTHER", "otherId2")));
    }

    @Test
    public void removingCoreTest() {
        IdCorrelator idCorrelator = newTestCorrelator();

        idCorrelator.removeCore("4");

        assertThat(idCorrelator.cores(), not(hasItem("4")));

        assertThat(Collections.emptySet(), equalTo(idCorrelator.getCores("DMS", "D9")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getCores("OMS", "O9")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getCores("GIS", "G9")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getCores("OTHER", "otherId1")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getCores("OTHER", "otherId2")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getCores("4", "DMS")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getCores("4", "OMS")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getCores("4", "GIS")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getCores("4", "OTHER")));

        idCorrelator.removeCore("3");

        assertThat(idCorrelator.cores(), not(hasItem("3")));
        assertThat(idCorrelator.getCores("CIS", "C1"), not(hasItem("3")));
        assertThat(idCorrelator.getCores("GIS", "G1"), not(hasItem("3")));
        assertThat(idCorrelator.getCores("OMS", "O2"), not(hasItem("3")));
        assertThat(idCorrelator.getCores("OMS", "O1"), not(hasItem("3")));
        assertThat(idCorrelator.getCores("DMS", "D2"), not(hasItem("3")));
        assertThat(idCorrelator.getCores("DMS", "D6"), not(hasItem("3")));

        assertThat(Collections.emptySet(), equalTo(idCorrelator.getIds("3", "CIS")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getIds("3", "GIS")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getIds("3", "OMS")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getIds("3", "DMS")));

        idCorrelator.removeCore("2");

        assertThat(idCorrelator.cores(), not(hasItem("2")));
        assertThat(idCorrelator.getCores("CIS", "C1"), not(hasItem("2")));
        assertThat(idCorrelator.getCores("GIS", "G2"), not(hasItem("2")));
        assertThat(idCorrelator.getCores("OMS", "O6"), not(hasItem("2")));
        assertThat(idCorrelator.getCores("OMS", "O5"), not(hasItem("2")));
        assertThat(idCorrelator.getCores("DMS", "D4"), not(hasItem("2")));
        assertThat(idCorrelator.getCores("DMS", "D3"), not(hasItem("2")));

        assertThat(Collections.emptySet(), equalTo(idCorrelator.getIds("2", "CIS")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getIds("2", "GIS")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getIds("2", "OMS")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getIds("2", "DMS")));

        idCorrelator.removeCore("1");

        assertThat(idCorrelator.cores(), not(hasItem("1")));
        assertThat(idCorrelator.getCores("CIS", "C1"), not(hasItem("1")));
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getIds("1", "CIS")));

    }

    @Test
    public void removingIdTest() {
        IdCorrelator idCorrelator = newTestCorrelator();

        idCorrelator.removeId("OTHER", "otherId1");
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getCores("OTHER", "otherId1")));
        assertThat(idCorrelator.getIds("4", "OTHER"), containsInAnyOrder("otherId2"));
    }

    @Test
    public void removingInvalidCoreTest() {
        IdCorrelator idCorrelator1 = newTestCorrelator();
        IdCorrelator idCorrelator2 = newTestCorrelator();

        idCorrelator1.removeCore("I DON'T EXIST");

        compareCorrelations(idCorrelator1, idCorrelator2);
    }

    @Test
    public void removingInvalidIdTest() {
        IdCorrelator idCorrelator1 = newTestCorrelator();
        IdCorrelator idCorrelator2 = newTestCorrelator();

        idCorrelator1.removeId("INVALID SYSTEM", "I DON'T EXIST");

        compareCorrelations(idCorrelator1, idCorrelator2);
    }

    @Test
    public void gettingIdsUsingCoresTest() {
        IdCorrelator idCorrelator = newTestCorrelator();

        assertThat(idCorrelator.getIds("2", "OMS"), containsInAnyOrder("O6", "O5"));
        assertThat(idCorrelator.getIds("3", "DMS"), containsInAnyOrder("D2", "D6"));
        assertThat(idCorrelator.getIds("4", "DMS"), containsInAnyOrder("D9"));
        assertThat(idCorrelator.getIds("4", "OMS"), containsInAnyOrder("O9"));
        assertThat(idCorrelator.getIds("4", "GIS"), containsInAnyOrder("G9"));
        assertThat(idCorrelator.getIds("4", "OTHER"), containsInAnyOrder("otherId1", "otherId2"));
    }

    @Test
    public void gettingCoresUsingIDsTest() {
        IdCorrelator idCorrelator = newTestCorrelator();

        assertThat(idCorrelator.getCores("CIS", "C1"), containsInAnyOrder("1", "2", "3"));
        assertThat(idCorrelator.getCores("DMS", "D2"), containsInAnyOrder("3"));
        assertThat(idCorrelator.getCores("DMS", "D9"), containsInAnyOrder("4"));
        assertThat(idCorrelator.getCores("OMS", "O9"), containsInAnyOrder("4"));
        assertThat(idCorrelator.getCores("GIS", "G9"), containsInAnyOrder("4"));
        assertThat(idCorrelator.getCores("OTHER", "otherId1"), containsInAnyOrder("4"));
        assertThat(idCorrelator.getCores("OTHER", "otherId2"), containsInAnyOrder("4"));
    }

    @Test
    public void addingCorrelationsTest() {
        IdCorrelator idCorrelator = MapBackedIdCorrelator.newCorrelator();
        idCorrelator.addSystem("GIS");
        idCorrelator.addSystem("DMS");
        idCorrelator.addSystem("OMS");
        idCorrelator.addSystem("CIS");
        idCorrelator.addSystem("OTHER");
        idCorrelator.addCore("1");
        idCorrelator.addCore("2");
        idCorrelator.addCore("3");
        idCorrelator.addCore("4");

        idCorrelator.addCorrelation("1", "CIS", "C1");
        idCorrelator.addCorrelation("2", "CIS", "C1");
        idCorrelator.addCorrelation("3", "CIS", "C1");
        idCorrelator.addCorrelation("2", "GIS", "G2");
        idCorrelator.addCorrelation("3", "GIS", "G1");
        idCorrelator.addCorrelation("2", "OMS", "O6");
        idCorrelator.addCorrelation("2", "OMS", "O5");
        idCorrelator.addCorrelation("3", "OMS", "O2");
        idCorrelator.addCorrelation("3", "OMS", "O1");
        idCorrelator.addCorrelation("3", "DMS", "D2");
        idCorrelator.addCorrelation("3", "DMS", "D6");
        idCorrelator.addCorrelation("2", "DMS", "D4");
        idCorrelator.addCorrelation("2", "DMS", "D3");
        idCorrelator.addCorrelation("4", "GIS", "G9");
        idCorrelator.addCorrelation("4", "DMS", "D9");
        idCorrelator.addCorrelation("4", "OMS", "O9");
        idCorrelator.addCorrelation("4", "OTHER", "otherId1");
        idCorrelator.addCorrelation("4", "OTHER", "otherId2");

        // Cant add correlations to non-existent system
        expect(() -> idCorrelator.addCorrelation("2", "fake_system", "fakeId"))
            .toThrow(IllegalArgumentException.class)
            .withMessage("INTERNAL ERROR: Unknown system tag 'fake_system'. Why do I not know about it!");
        assertThat(Collections.emptySet(), equalTo(idCorrelator.getCores("fake_system", "fakeId")));

        // Cant add correlations to non-existent coreId
        expect(() -> idCorrelator.addCorrelation("fakeCoreId", "OTHER", "otherId2"))
            .toThrow(IllegalArgumentException.class)
            .withMessage("INTERNAL ERROR: Unknown core id 'fakeCoreId'. Why do I not know about it!");

        // Can't add correlations that produce many-to-many relationships
        idCorrelator.addCorrelation("2", "GIS", "G1");
        idCorrelator.addCorrelation("3", "OMS", "O5");
    }

    @Test
    public void addCoresTest() {
        IdCorrelator idCorrelator = MapBackedIdCorrelator.newCorrelator();
        idCorrelator.addCore("1");
        idCorrelator.addCore("2");
        idCorrelator.addCore("3");
        idCorrelator.addCore("4");

        assertThat(idCorrelator.addCore("4"), equalTo(false));
        assertThat(idCorrelator.cores(), containsInAnyOrder("1", "2", "3", "4"));
    }

    @Test
    public void addSystemTest() {
        IdCorrelator idCorrelator = MapBackedIdCorrelator.newCorrelator();
        assertThat(idCorrelator.addSystem("GIS"), equalTo(true));
        assertThat(idCorrelator.addSystem("DMS"), equalTo(true));
        assertThat(idCorrelator.addSystem("OMS"), equalTo(true));
        assertThat(idCorrelator.addSystem("CIS"), equalTo(true));
        assertThat(idCorrelator.addSystem("OTHER"), equalTo(true));

        assertThat(idCorrelator.addSystem("id"), equalTo(false));
        assertThat(idCorrelator.addSystem("ID"), equalTo(true));

        idCorrelator.addBlockedSystem("ID");
        assertThat(idCorrelator.addSystem("ID"), equalTo(false));

        idCorrelator.removeBlockedSystem("ID");
        assertThat(idCorrelator.addSystem("ID"), equalTo(true));
    }

    @Test
    public void addSystemsToIdCorrelatorTest() {
        IdCorrelator idCorrelator = MapBackedIdCorrelator.newCorrelator();
        idCorrelator.addSystem("GIS");
        idCorrelator.addSystem("DMS");
        idCorrelator.addSystem("OMS");
        idCorrelator.addSystem("CIS");
        idCorrelator.addSystem("OTHER");

        assertThat(idCorrelator.systems().size(), is(5));
        assertThat(idCorrelator.systems(), containsInAnyOrder("GIS", "DMS", "OMS", "CIS", "OTHER"));
    }

    private IdCorrelator newTestCorrelator() {
        IdCorrelator idCorrelator = MapBackedIdCorrelator.newCorrelator();
        idCorrelator.addSystem("GIS");
        idCorrelator.addSystem("DMS");
        idCorrelator.addSystem("OMS");
        idCorrelator.addSystem("CIS");
        idCorrelator.addSystem("OTHER");
        idCorrelator.addCore("1");
        idCorrelator.addCore("2");
        idCorrelator.addCore("3");
        idCorrelator.addCore("4");
        idCorrelator.addCorrelation("1", "CIS", "C1");
        idCorrelator.addCorrelation("2", "CIS", "C1");
        idCorrelator.addCorrelation("3", "CIS", "C1");
        idCorrelator.addCorrelation("2", "GIS", "G2");
        idCorrelator.addCorrelation("3", "GIS", "G1");
        idCorrelator.addCorrelation("2", "OMS", "O6");
        idCorrelator.addCorrelation("2", "OMS", "O5");
        idCorrelator.addCorrelation("3", "OMS", "O2");
        idCorrelator.addCorrelation("3", "OMS", "O1");
        idCorrelator.addCorrelation("3", "DMS", "D2");
        idCorrelator.addCorrelation("3", "DMS", "D6");
        idCorrelator.addCorrelation("2", "DMS", "D4");
        idCorrelator.addCorrelation("2", "DMS", "D3");
        idCorrelator.addCorrelation("4", "GIS", "G9");
        idCorrelator.addCorrelation("4", "DMS", "D9");
        idCorrelator.addCorrelation("4", "OMS", "O9");
        idCorrelator.addCorrelation("4", "OTHER", "otherId1");
        idCorrelator.addCorrelation("4", "OTHER", "otherId2");

        return idCorrelator;
    }

}
