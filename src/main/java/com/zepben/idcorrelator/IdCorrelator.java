/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.idcorrelator;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.idcorrelator.io.IdCorrelatorWriteException;
import com.zepben.idcorrelator.io.IdCorrelatorWriter;

import java.util.Set;

/**
 * An object that correlates ids between peripheral systems and a central system.
 * <p>
 * The id correlator has two types of systems a central one whose ids are called coreIds and
 * peripheral systems whose ids are called systemIds.
 * </p>
 */

@EverythingIsNonnullByDefault
public interface IdCorrelator {

    /**
     * Returns the systemTags currently inside the id correlator
     *
     * @return A set that holds every systemTags in the id correlator.
     */
    Set<String> systems();

    /**
     * Returns the set of central system ids.
     *
     * @return A set that holds every coreId.
     */
    Set<String> cores();

    /**
     * Adds a system tag to the id correlator.
     *
     * @param systemTag The unique id for a peripheral system.
     */
    boolean addSystem(String systemTag);

    /**
     * Removes a system and all of its ids from the id correlator.
     *
     * @param systemTag The unique id for a peripheral system.
     */
    void removeSystem(String systemTag);

    /**
     * Adds a coreId to the central system.
     *
     * @param coreId An id in the central system.
     * @return False if the coreId was already present in the id correlator, true otherwise.
     */
    boolean addCore(String coreId);

    /**
     * Adds a blocked systemTag so that the id correlator will not accept it in the future;
     *
     * @param blockedSystem The unique id for a peripheral system.
     */
    void addBlockedSystem(String blockedSystem);

    /**
     * Removes a blocked systemTag so that the id correlator will accept it in the future;
     *
     * @param blockedSystem The unique id for a peripheral system.
     */
    void removeBlockedSystem(String blockedSystem);

    /**
     * Removes a coreId from the system.
     *
     * @param coreId An id in the central system.
     */
    void removeCore(String coreId);

    /**
     * Returns the coreIds correlated to a specific systemId.
     *
     * @param systemTag The unique id for a peripheral system.
     * @param systemId  An id in a peripheral system.
     * @return The set of 'coreIds' the 'systemId' (of 'systemTag') is correlated to.
     */
    Set<String> getCores(String systemTag, String systemId);

    /**
     * Adds a correlation between a 'coreId' and the 'systemId' located in system 'systemTag'.
     * <p>
     * <p>
     * Possible reasons for a correlation failing include:
     * - The correlation leads to a "many-to-many" relationship.
     * - systemTag did not exist.
     * - coreId did not exist.
     * </p>
     *
     * @param coreId    An id in the central system.
     * @param systemTag The unique id for a peripheral system.
     * @param systemId  An id in a peripheral system.
     */
    void addCorrelation(String coreId, String systemTag, String systemId);

    /**
     * Removes a systemId from the id correlator.
     *
     * @param systemTag The unique id for a peripheral system.
     * @param systemId  An id in a peripheral system.
     */
    void removeId(String systemTag, String systemId);

    /**
     * Returns the systemIds correlated to a coreId for a specific system.
     *
     * @param coreId    An id in the central system.
     * @param systemTag The unique id for a peripheral system.
     * @return The set of 'systemIds' the 'coreId' that is correlated to in the system with name 'systemTag'.
     */
    Set<String> getIds(String coreId, String systemTag);

    /**
     * Locks the id correlator and calls the write() method in a {@link IdCorrelatorWriter}.
     * If the id correlator is being used concurrently calling this method is the correct way to write its data to a persistent data store.
     *
     * @param idCorrelatorWriter An object that implements {@link IdCorrelatorWriter}.
     * @throws IdCorrelatorWriteException if any exception is thrown in the process.
     */
    void write(IdCorrelatorWriter idCorrelatorWriter) throws IdCorrelatorWriteException;

}
