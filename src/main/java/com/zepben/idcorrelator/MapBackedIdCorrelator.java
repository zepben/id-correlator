/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.idcorrelator;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.collections.CompactHashMap;
import com.zepben.collections.CompactHashSet;
import com.zepben.idcorrelator.io.IdCorrelatorWriteException;
import com.zepben.idcorrelator.io.IdCorrelatorWriter;

import java.io.Serializable;
import java.util.*;

/**
 * An IdCorrelator that uses Maps and Sets in its implementation.
 * <p>
 * A static factory method is provided with the recommended Map and Set implementation, namely the CompactHashMap and
 * CompactHashSet implementation for Maps and Sets.
 */

@EverythingIsNonnullByDefault
public class MapBackedIdCorrelator implements IdCorrelator {

    private MapFactory mapFactory;
    private SetFactory setFactory;

    private Map<String, IdObject> coresMap;
    private Set<String> blockedSystems = new HashSet<>();
    private Map<String, Map<String, Set<IdObject>>> systemTagToIdMaps;

    /**
     * Functional Interface used by id correlator to instantiate maps
     */
    interface MapFactory {

        <K, V> Map<K, V> newMap();

    }

    /**
     * Functional Interface used by id correlator to instantiate sets
     */
    interface SetFactory {

        <T> Set<T> newSet();

    }

    /**
     * Constructor for the id correlator.
     *
     * @param mapFactory method reference for a map constructor.
     * @param setFactory method reference for a set constructor.
     */
    @SuppressWarnings("WeakerAccess")
    public MapBackedIdCorrelator(MapFactory mapFactory, SetFactory setFactory) {
        this.mapFactory = mapFactory;
        this.setFactory = setFactory;

        this.coresMap = mapFactory.newMap();
        this.systemTagToIdMaps = mapFactory.newMap();
    }

    /**
     * Factory method for the IdCorrelator which uses the CompactHashMap/Set implementation
     *
     * @return An id correlator that uses CompactHashMap/Set.
     */
    public static IdCorrelator newCorrelator() {
        return new MapBackedIdCorrelator(CompactHashMap::new, CompactHashSet::new);
    }

    @Override
    public void addBlockedSystem(String blockedSystem) {
        this.blockedSystems.add(blockedSystem);
    }

    @Override
    public void removeBlockedSystem(String blockedSystem) {
        this.blockedSystems.remove(blockedSystem);
    }

    @Override
    public Set<String> systems() {
        return readOnlyKeySetCopy(systemTagToIdMaps);
    }

    @Override
    public Set<String> cores() {
        return readOnlyKeySetCopy(coresMap);
    }

    @Override
    public boolean addSystem(String systemTag) {
        if (!blockedSystems.contains(systemTag) && !systemTag.equals("id")) {
            systemTagToIdMaps.putIfAbsent(systemTag, mapFactory.newMap());
            return true;
        }
        return false;
    }

    @Override
    public void removeSystem(String systemTag) {
        coresMap.values().forEach(core -> core.removeMap(systemTag));
        systemTagToIdMaps.remove(systemTag);
    }

    @Override
    public boolean addCore(String coreId) {
        return (coresMap.putIfAbsent(coreId, new IdObject(coreId)) == null);
    }

    @Override
    public void removeCore(String coreId) {
        if (coresMap.containsKey(coreId)) {

            for (String system : coresMap.get(coreId).systemTagToIds.keySet()) {
                Map<String, Set<IdObject>> locatedSystem = systemTagToIdMaps.get(system);

                for (String systemId : coresMap.get(coreId).systemTagToIds.get(system)) {
                    Set<IdObject> locatedId = locatedSystem.get(systemId);
                    locatedId.removeIf(core -> core.equals(coresMap.get(coreId)));

                    if (locatedId.size() == 0) {
                        locatedSystem.remove(systemId);
                    }
                }

                if (locatedSystem.size() == 0) {
                    systemTagToIdMaps.remove(system);
                }
            }
        }
        coresMap.remove(coreId);
    }

    @Override
    public Set<String> getCores(String systemTag, String systemId) {
        if (!systemTagToIdMaps.keySet().contains(systemTag))
            return Collections.emptySet();

        Set<String> ids = setFactory.newSet();
        systemTagToIdMaps.get(systemTag).getOrDefault(systemId, Collections.emptySet()).forEach(core -> ids.add(core.id));

        return Collections.unmodifiableSet(ids);
    }

    @Override
    public void addCorrelation(String coreId, String systemTag, String systemId) {
        Map<String, Set<IdObject>> systemIds = systemTagToIdMaps.get(systemTag);
        IdObject core = coresMap.get(coreId);

        if (systemIds == null)
            throw new IllegalArgumentException(String.format("INTERNAL ERROR: Unknown system tag '%s'. Why do I not know about it!", systemTag));

        if (core == null)
            throw new IllegalArgumentException(String.format("INTERNAL ERROR: Unknown core id '%s'. Why do I not know about it!", coreId));

        Set<IdObject> matchedCoreSet = systemIds.computeIfAbsent(systemId, (id) -> setFactory.newSet());
        matchedCoreSet.add(core);
        core.addMap(systemTag, systemId);
    }

    @Override
    public void removeId(String systemTag, String systemId) {
        coresMap.values().forEach(core -> core.removeId(systemTag, systemId));
        systemTagToIdMaps.getOrDefault(systemTag, Collections.emptyMap()).remove(systemId);
    }

    @Override
    public Set<String> getIds(String coreId, String systemTag) {
        if (!systemTagToIdMaps.keySet().contains(systemTag) || !coresMap.containsKey(coreId))
            return Collections.emptySet();

        Set<String> ids = setFactory.newSet();
        ids.addAll(coresMap.get(coreId).getMap(systemTag));

        return Collections.unmodifiableSet(ids);
    }

    @Override
    public void write(IdCorrelatorWriter idCorrelatorWriter) throws IdCorrelatorWriteException {
        idCorrelatorWriter.write(this);
    }

    private Set<String> readOnlyKeySetCopy(Map<String, ?> map) {
        Set<String> systemTags = setFactory.newSet();
        systemTags.addAll(map.keySet());

        return Collections.unmodifiableSet(systemTags);
    }

    /**
     * A class that represent a core element in the central system.
     * <p>
     * This class holds the correlations between a core and the peripheral systems it is correlated to.
     * </p>
     */
    @EverythingIsNonnullByDefault
    private class IdObject implements Serializable {

        private String id;
        private Map<String, Set<String>> systemTagToIds = mapFactory.newMap();

        private IdObject(String id) {
            this.id = id;
        }

        private void addMap(String systemTag, String systemId) {
            systemTagToIds.computeIfAbsent(systemTag, index -> setFactory.newSet()).add(systemId);
        }

        private Set<String> getMap(String systemTag) {
            return Collections.unmodifiableSet(systemTagToIds.getOrDefault(systemTag, Collections.emptySet()));
        }

        private void removeMap(String systemTag) {
            systemTagToIds.remove(systemTag);
        }

        private void removeId(String systemTag, String id) {
            systemTagToIds.getOrDefault(systemTag, Collections.emptySet()).remove(id);
            if (systemTagToIds.getOrDefault(systemTag, Collections.emptySet()).size() == 0)
                systemTagToIds.remove(systemTag);
        }

    }

}
