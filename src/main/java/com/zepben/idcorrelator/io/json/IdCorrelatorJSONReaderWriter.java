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
import com.zepben.idcorrelator.io.*;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides the methods necessary to save/load an {@link IdCorrelator} into/from a Json file.
 */
@EverythingIsNonnullByDefault
public class IdCorrelatorJSONReaderWriter implements IdCorrelatorWriter, IdCorrelatorReader {

    private Path filePath;
    private boolean prettyPrint;

    /**
     * Class Constructor.
     *
     * @param prettyPrint pretty print flag.
     * @param filePath    {@link Path} object for the file to be written to or read from.
     */
    @SuppressWarnings("WeakerAccess")
    public IdCorrelatorJSONReaderWriter(Path filePath, boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        this.filePath = filePath;
    }

    /**
     * @return Returns the {@link Path} object of the file to be written to or read from.
     */
    public Path filePath() {
        return filePath;
    }

    /**
     * @return Returns the name of the file to be written to or read from.
     */
    @SuppressWarnings("WeakerAccess")
    public String fileName() {
        return filePath.toString();
    }

    /**
     * @return Return the flag indicating if pretty print enabled.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean prettyPrint() {
        return prettyPrint;
    }

    /**
     * Sets the pretty print flag.
     *
     * @param prettyPrint pretty print flag.
     */
    @SuppressWarnings("WeakerAccess")
    public void prettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    /**
     * Writes the correlations for an {@link IdCorrelator} into a JSON file.
     * This method should not be called directly if the idCorrelator is being used concurrently.
     * See the write() method in {@link IdCorrelator} for the concurrent access case.
     *
     * @param idCorrelator the {@link IdCorrelator} object to be saved.
     * @throws IdCorrelatorWriteException if an exception is thrown in the process of writing.
     */

    public void write(IdCorrelator idCorrelator) throws IdCorrelatorWriteException {
        try {
            if (!prettyPrint)
                Files.write(filePath, JsonUtils.toJson(idCorrelator).encode().getBytes(StandardCharsets.UTF_8));
            else
                Files.write(filePath, JsonUtils.toJson(idCorrelator).encodePrettily().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IdCorrelatorWriteException(e);
        }
    }

    /**
     * Reads the correlations for an {@link IdCorrelator} stored in an JSON file into an instance of {@link IdCorrelator}.
     *
     * @param idCorrelator the {@link IdCorrelator} instance to be populated with the correlations read.
     * @throws IdCorrelatorReadException if an exception is thrown in the process of reading.
     */
    public Collection<FailedCorrelationInfo> read(IdCorrelator idCorrelator) throws IdCorrelatorReadException {
        Set<FailedCorrelationInfo> failedCorrelationsSet = new HashSet<>();

        try {
            String jsonStr = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            JsonObject idCorrelatorJson = new JsonObject(jsonStr);
            JsonUtils.intoIdCorrelator(idCorrelatorJson, idCorrelator);
        } catch (IOException e) {
            throw new IdCorrelatorReadException(e);
        }

        return failedCorrelationsSet;
    }

}
