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

import java.util.Collection;

/**
 * Interface that reads the correlations for an {@link IdCorrelator} from a persistent data store.
 */

@EverythingIsNonnullByDefault
public interface IdCorrelatorReader {

    /**
     * Takes in an {@link IdCorrelator} and populates it with the correlations present in a persistent data store.
     *
     * @param idCorrelator the {@link IdCorrelator} instance to be populated with the correlations read.
     * @throws IdCorrelatorReadException if any exception is thrown while reading.
     */
    Collection<FailedCorrelationInfo> read(IdCorrelator idCorrelator) throws IdCorrelatorReadException;

}
