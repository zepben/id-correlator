/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.idcorrelator.io.json;

import com.zepben.idcorrelator.IdCorrelator;
import com.zepben.idcorrelator.MapBackedIdCorrelator;
import com.zepben.idcorrelator.io.FailedCorrelationInfo;
import com.zepben.idcorrelator.io.IdCorrelatorWriteException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;

import static com.zepben.idcorrelator.TestUtils.compareCorrelations;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JSONDataPersistenceTest {

    /**
     * Tests functionality of the class {@link IdCorrelatorJSONReaderWriter}.
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
    public void testMain() {
        IdCorrelator idCorrelator = populateIdCorrelator();

        // Save as JSON file
        IdCorrelatorJSONReaderWriter jsonReadWriter = new IdCorrelatorJSONReaderWriter(ID_CORRELATOR_FILE_PATH, false);
        try {
            idCorrelator.write(jsonReadWriter);
        } catch (IdCorrelatorWriteException e) {
            e.printStackTrace();
        }

        // Load from JSON file
        IdCorrelator newIdCorrelator = MapBackedIdCorrelator.newCorrelator();
        Collection<FailedCorrelationInfo> failedInfo = null;
        try {
            failedInfo = jsonReadWriter.read(newIdCorrelator);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertThat(failedInfo, not(nullValue()));
        assertThat(failedInfo.size(), is(0));

        // Check IdCorrelators are Equal
        assertThat(newIdCorrelator, not(nullValue()));
        compareCorrelations(idCorrelator, newIdCorrelator);

        // Test correlations
        testCorrelations(idCorrelator);
        testCorrelations(newIdCorrelator);
    }

    @Test
    public void testSettersAndGetters() {
        IdCorrelatorJSONReaderWriter jsonReadWriter = new IdCorrelatorJSONReaderWriter(Paths.get("src/test/data/fileName1"), false);

        assertThat(MessageFormat.format("src{0}test{0}data{0}fileName1", File.separator), equalTo(jsonReadWriter.fileName()));
        assertThat(jsonReadWriter.prettyPrint(), equalTo(false));

        jsonReadWriter.prettyPrint(true);
        assertThat(jsonReadWriter.prettyPrint(), equalTo(true));
    }

    private IdCorrelator populateIdCorrelator() {
        IdCorrelator idCorrelator = MapBackedIdCorrelator.newCorrelator();
        idCorrelator.addSystem("GIS");
        idCorrelator.addSystem("DMS");
        idCorrelator.addSystem("OMS");
        idCorrelator.addSystem("CIS");

        idCorrelator.addCore("1");
        idCorrelator.addCore("2");
        idCorrelator.addCore("3");

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

        // --- Testing being able to add a correlation that creates a Many-To-Many relationship ----
        idCorrelator.addCorrelation("2", "GIS", "G1");
        idCorrelator.addCorrelation("3", "OMS", "O5");

        return idCorrelator;
    }

    private void testCorrelations(IdCorrelator idCorrelator) {

        // Testing correlations
        Set<String> coreIds;

        // ---- CIS to Everything ---
        //GIS
        List<String> expectedCores = Arrays.asList("1", "2", "3");
        coreIds = idCorrelator.getCores("CIS", "C1");
        assertThat(coreIds, containsInAnyOrder(expectedCores.toArray()));
        assertThat(idCorrelator.getIds(expectedCores.get(0), "GIS"), containsInAnyOrder(Collections.emptySet()));
        assertThat(idCorrelator.getIds(expectedCores.get(1), "GIS"), containsInAnyOrder("G1", "G2"));
        assertThat(idCorrelator.getIds(expectedCores.get(2), "GIS"), containsInAnyOrder("G1"));

        //DMS
        coreIds = idCorrelator.getCores("CIS", "C1");
        assertThat(coreIds, containsInAnyOrder(expectedCores.toArray()));
        assertThat(idCorrelator.getIds(expectedCores.get(0), "DMS"), containsInAnyOrder(Collections.emptySet()));
        assertThat(idCorrelator.getIds(expectedCores.get(1), "DMS"), containsInAnyOrder("D4", "D3"));
        assertThat(idCorrelator.getIds(expectedCores.get(2), "DMS"), containsInAnyOrder("D2", "D6"));

        //OMS
        coreIds = idCorrelator.getCores("CIS", "C1");
        assertThat(coreIds, containsInAnyOrder(expectedCores.toArray()));
        assertThat(idCorrelator.getIds(expectedCores.get(0), "OMS"), containsInAnyOrder(Collections.emptySet()));
        assertThat(idCorrelator.getIds(expectedCores.get(1), "OMS"), containsInAnyOrder("O5", "O6"));
        assertThat(idCorrelator.getIds(expectedCores.get(2), "OMS"), containsInAnyOrder("O1", "O2", "O5"));

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
        assertThat(coreIds, containsInAnyOrder("2", "3"));

        // O6
        coreIds = idCorrelator.getCores("OMS", "O6");
        assertThat(coreIds, containsInAnyOrder("2"));

        // ---- GIS to Core ----
        // G1
        coreIds = idCorrelator.getCores("GIS", "G1");
        assertThat(coreIds, containsInAnyOrder("2", "3"));

        // G2
        coreIds = idCorrelator.getCores("GIS", "G2");
        assertThat(coreIds, containsInAnyOrder("2"));
    }

}
