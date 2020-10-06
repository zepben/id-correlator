/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.idcorrelator.io.json;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.idcorrelator.IdCorrelator;
import com.zepben.idcorrelator.MapBackedIdCorrelator;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Utility class to convert instances of {@link IdCorrelator} into
 * instances of {@link JsonObject} and back.
 */
@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public final class JsonUtils {

    /**
     * Private constructor to prevent instantiation of a utility class.
     */
    private JsonUtils() {
    }

    /**
     * Converts an instance of {@link IdCorrelator} into an instance of {@link JsonObject}
     *
     * @param idCorrelator The idCorrelator to be converted.
     * @return The jsonObject instance that represents the provided idCorrelator.
     */
    public static JsonObject toJson(IdCorrelator idCorrelator) {
        JsonObject idCorrelatorJson = new JsonObject();
        addSystemsToJson(idCorrelator, idCorrelatorJson);
        addCoresToJson(idCorrelator, idCorrelatorJson);
        return idCorrelatorJson;
    }

    static private void addSystemsToJson(IdCorrelator idCorrelator, JsonObject idCorrelatorJson) {
        JsonArray systems = new JsonArray();
        idCorrelator.systems().forEach(systems::add);
        idCorrelatorJson.put("systems", systems);
    }

    static private void addCoresToJson(IdCorrelator idCorrelator, JsonObject idCorrelatorJson) {
        JsonArray cores = new JsonArray();
        idCorrelator.cores().forEach(coreId -> {
            JsonObject correlations = new JsonObject();
            idCorrelator.systems().forEach(system -> {
                JsonArray ids = new JsonArray();
                idCorrelator.getIds(coreId, system).forEach(ids::add);
                if (!ids.isEmpty())
                    correlations.put(system, ids);
            });

            if (!correlations.isEmpty()) {
                JsonObject core = new JsonObject()
                    .put("id", coreId)
                    .mergeIn(correlations);
                cores.add(core);
            }
        });
        idCorrelatorJson.put("cores", cores);
    }

    /**
     * Converts an instance of {@link JsonObject} into an instance of {@link IdCorrelator}.
     * The concrete class used to instantiate the idCorrelator is {@link MapBackedIdCorrelator}
     *
     * @param idCorrelatorJson The jsonObject that represents the idCorrelator to be converted.
     * @return The instance of MapBackedIdCorrelator represented by the provided JsonObject.
     */
    public static IdCorrelator toIdCorrelator(JsonObject idCorrelatorJson) {

        // Instantiating idCorrelator
        IdCorrelator idCorrelator = MapBackedIdCorrelator.newCorrelator();

        // Populating idCorrelator
        intoIdCorrelator(idCorrelatorJson, idCorrelator);

        return idCorrelator;
    }

    /**
     * Adds the correlations represented by a {@link JsonObject} into an instance of {@link IdCorrelator}.
     *
     * @param idCorrelatorJson Json representation of an IdCorrelator, serves as the source of the correlations to be added.
     * @param idCorrelator     Instance of an IdCorrelator to which the correlations will be added.
     */
    public static void intoIdCorrelator(JsonObject idCorrelatorJson, IdCorrelator idCorrelator) {
        addSystemsToIdCorrelator(idCorrelator, idCorrelatorJson);
        addCoresToIdCorrelator(idCorrelator, idCorrelatorJson);
    }

    static private void addSystemsToIdCorrelator(IdCorrelator idCorrelator, JsonObject idCorrelatorJson) {
        extract(idCorrelatorJson, "systems", JsonObject::getJsonArray)
            .stream()
            .map(String.class::cast)
            .forEach(idCorrelator::addSystem);
    }

    static private void addCoresToIdCorrelator(IdCorrelator idCorrelator, JsonObject idCorrelatorJson) {
        Set<String> systems = idCorrelator.systems();
        extract(idCorrelatorJson, "cores", JsonObject::getJsonArray)
            .stream()
            .map(JsonObject.class::cast)
            .forEach(core -> {
                String coreId = extract(core, "id", JsonObject::getString);
                idCorrelator.addCore(coreId);
                systems.forEach(system -> {
                    if (core.containsKey(system)) {
                        core.getJsonArray(system)
                            .stream()
                            .map(String.class::cast)
                            .forEach(id -> idCorrelator.addCorrelation(coreId, system, id));
                    }
                });
            });
    }

    @FunctionalInterface
    interface GetValue<T> {

        @Nullable
        T get(JsonObject json, String key);

    }

    static <T> T extract(JsonObject json, String key, JsonUtils.GetValue<T> valueSupplier) throws IllegalArgumentException {
        try {
            @Nullable T value = valueSupplier.get(json, key);
            if (value == null)
                throw new IllegalArgumentException(String.format("Required key '%s' must be specified", key));
            return value;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(String.format("Error reading required key '%s'", key), ex);
        }
    }

}
