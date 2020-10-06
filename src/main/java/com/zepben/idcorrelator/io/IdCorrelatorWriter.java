/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.idcorrelator.io;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.idcorrelator.IdCorrelator;

/**
 * Interface that writes an {@link IdCorrelator} into a persistent data store.
 */

@EverythingIsNonnullByDefault
public interface IdCorrelatorWriter {

    /**
     * Writes the {@link IdCorrelator} correlations into a persistent data store.
     * <p>
     * Warning: Do not use this method directly, if you are concerned with thread safety.
     * The {@link IdCorrelator} provides the write() method if you want to save an
     * {@link IdCorrelator} while it is being used concurrently.
     * </p>
     *
     * @param idCorrelator the {@link IdCorrelator} object that holds the correlations to be written.
     * @throws IdCorrelatorWriteException if any exception is thrown while writing.
     */
    void write(IdCorrelator idCorrelator) throws IdCorrelatorWriteException;

}
