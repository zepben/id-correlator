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
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class SynchronisedIdCorrelator implements IdCorrelator {

    private final IdCorrelator idCorrelator;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public SynchronisedIdCorrelator(IdCorrelator idCorrelator) {
        this.idCorrelator = idCorrelator;
    }

    @Override
    public void addBlockedSystem(String blockedSystem) {
        lock.writeLock().lock();
        try {
            idCorrelator.addBlockedSystem(blockedSystem);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeBlockedSystem(String blockedSystem) {
        lock.readLock().lock();
        try {
            idCorrelator.removeBlockedSystem(blockedSystem);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Set<String> systems() {
        lock.readLock().lock();
        try {
            return idCorrelator.systems();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Set<String> cores() {
        lock.readLock().lock();
        try {
            return idCorrelator.cores();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean addSystem(String systemTag) {
        lock.writeLock().lock();
        try {
            return idCorrelator.addSystem(systemTag);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeSystem(String systemTag) {
        lock.writeLock().lock();
        try {
            idCorrelator.removeSystem(systemTag);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean addCore(String coreId) {
        lock.writeLock().lock();
        try {
            return idCorrelator.addCore(coreId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeCore(String coreId) {
        lock.writeLock().lock();
        try {
            idCorrelator.removeCore(coreId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Set<String> getCores(String systemTag, String systemId) {
        lock.readLock().lock();
        try {
            return idCorrelator.getCores(systemTag, systemId);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void addCorrelation(String coreId, String systemTag, String systemId) {
        lock.writeLock().lock();
        try {
            idCorrelator.addCorrelation(coreId, systemTag, systemId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeId(String systemTag, String systemId) {
        lock.writeLock().lock();
        try {
            idCorrelator.removeId(systemTag, systemId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Set<String> getIds(String coreId, String systemTag) {
        lock.readLock().lock();
        try {
            return idCorrelator.getIds(coreId, systemTag);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void write(IdCorrelatorWriter idCorrelatorWriter) throws IdCorrelatorWriteException {
        lock.readLock().lock();
        try {
            idCorrelator.write(idCorrelatorWriter);
        } finally {
            lock.readLock().unlock();
        }
    }

    int getReadHoldCount() {
        return lock.getReadHoldCount();
    }

    int getWriteHoldCount() {
        return lock.getWriteHoldCount();
    }

}
