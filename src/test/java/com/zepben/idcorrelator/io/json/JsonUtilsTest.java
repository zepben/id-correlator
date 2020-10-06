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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.zepben.idcorrelator.TestUtils.compareCorrelations;
import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class JsonUtilsTest {

    @Test
    @Disabled
    public void testToJson() {
        assertThat(testIdCorrelatorJson(), equalTo(JsonUtils.toJson(testIdCorrelator())));
    }

    @Test
    public void testToIdCorrelator() {
        compareCorrelations(testIdCorrelator(), JsonUtils.toIdCorrelator(testIdCorrelatorJson()));
    }

    @Test
    public void testBadJsonInput() {
        expect(() -> JsonUtils.toIdCorrelator(new JsonObject()))
            .toThrow(IllegalArgumentException.class)
            .withMessage("Required key 'systems' must be specified");
    }

    private IdCorrelator testIdCorrelator() {
        IdCorrelator idCorrelator = MapBackedIdCorrelator.newCorrelator();

        // Add Systems
        idCorrelator.addSystem("sys1");
        idCorrelator.addSystem("sys2");
        idCorrelator.addSystem("sys3");

        // Add Cores
        idCorrelator.addCore("core1");
        idCorrelator.addCore("core2");
        idCorrelator.addCore("core3");

        // Add Correlations
        idCorrelator.addCorrelation("core1", "sys1", "core1_sys1_id1");
        idCorrelator.addCorrelation("core1", "sys1", "core1_sys1_id2");
        idCorrelator.addCorrelation("core1", "sys1", "core1_sys1_id3");
        idCorrelator.addCorrelation("core1", "sys2", "core1_sys2_id1");
        idCorrelator.addCorrelation("core1", "sys2", "core1_sys2_id2");
        idCorrelator.addCorrelation("core1", "sys3", "core1_sys3_id1");

        idCorrelator.addCorrelation("core2", "sys3", "core1_sys3_id1");

        idCorrelator.addCorrelation("core3", "sys2", "core3_sys2_id1");

        return idCorrelator;
    }

    private JsonObject testIdCorrelatorJson() {
        JsonObject idCorrelatorJson = new JsonObject();

        // -- Adding Systems --
        JsonArray systems = new JsonArray();
        systems.add("sys3");
        systems.add("sys1");
        systems.add("sys2");
        idCorrelatorJson.put("systems", systems);

        // -- Core 1 Correlations --
        JsonObject core1 = new JsonObject();
        core1.put("id", "core1");

        // Correlation sys1
        JsonArray ids1 = new JsonArray();
        ids1.add("core1_sys1_id1");
        ids1.add("core1_sys1_id2");
        ids1.add("core1_sys1_id3");
        core1.put("sys1", ids1);

        // Correlation sys2
        JsonArray ids2 = new JsonArray();
        ids2.add("core1_sys2_id2");
        ids2.add("core1_sys2_id1");
        core1.put("sys2", ids2);

        // Correlation sys3
        JsonArray ids3 = new JsonArray();
        ids3.add("core1_sys3_id1");
        core1.put("sys3", ids3);

        // -- Core 2 Correlations --
        JsonObject core2 = new JsonObject();
        core2.put("id", "core2");

        // Correlation sys3
        JsonArray ids23 = new JsonArray();
        ids23.add("core1_sys3_id1");
        core2.put("sys3", ids23);

        // -- Core 3 Correlations --
        JsonObject core3 = new JsonObject();
        core3.put("id", "core3");

        // Correlation sys2
        JsonArray ids32 = new JsonArray();
        ids32.add("core3_sys2_id1");
        core3.put("sys2", ids32);

        // Adding Cores
        JsonArray cores = new JsonArray();
        cores.add(core3);
        cores.add(core2);
        cores.add(core1);
        idCorrelatorJson.put("cores", cores);

        return idCorrelatorJson;
    }

}
