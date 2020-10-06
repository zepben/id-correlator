/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.idcorrelator;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.idcorrelator.io.IdCorrelatorWriter;
import com.zepben.testutils.junit.SystemLogExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import javax.annotation.Nullable;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@EverythingIsNonnullByDefault
public class SynchronisedIdCorrelatorTest {

    public SystemLogExtension systemErr = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess();

    @Mock
    @Nullable
    private IdCorrelator mockIdCorrelator;

    @Nullable
    private SynchronisedIdCorrelator synchronisedIdCorrelator;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        assertThat(mockIdCorrelator, not(nullValue()));

        synchronisedIdCorrelator = new SynchronisedIdCorrelator(mockIdCorrelator);

        AssertionError error = new AssertionError("Incorrectly called a mock that should not have been called");
        doThrow(error).when(mockIdCorrelator).systems();
        doThrow(error).when(mockIdCorrelator).cores();
        doThrow(error).when(mockIdCorrelator).addSystem(anyString());
        doThrow(error).when(mockIdCorrelator).removeSystem(anyString());
        doThrow(error).when(mockIdCorrelator).addCore(anyString());
        doThrow(error).when(mockIdCorrelator).removeCore(anyString());
        doThrow(error).when(mockIdCorrelator).getCores(anyString(), anyString());
        doThrow(error).when(mockIdCorrelator).addCorrelation(anyString(), anyString(), anyString());
        doThrow(error).when(mockIdCorrelator).removeId(anyString(), anyString());
        doThrow(error).when(mockIdCorrelator).getIds(anyString(), anyString());
        doThrow(error).when(mockIdCorrelator).write(any());
    }

    @Test
    public void systems() {

        assertThat(mockIdCorrelator, not(nullValue()));
        assertThat(synchronisedIdCorrelator, not(nullValue()));

        doAnswer(invocation -> {
            validateLocks(false);
            validateStringParams(invocation, 0);
            return Collections.emptySet();
        }).when(mockIdCorrelator).systems();

        synchronisedIdCorrelator.systems();
        verify(mockIdCorrelator, times(1)).systems();
    }

    @Test
    public void cores() {
        assertThat(mockIdCorrelator, not(nullValue()));
        assertThat(synchronisedIdCorrelator, not(nullValue()));

        doAnswer(invocation -> {
            validateLocks(false);
            validateStringParams(invocation, 0);
            return Collections.emptySet();
        }).when(mockIdCorrelator).cores();

        synchronisedIdCorrelator.cores();
        verify(mockIdCorrelator, times(1)).cores();
    }

    @Test
    public void addSystem() {
        assertThat(mockIdCorrelator, not(nullValue()));
        assertThat(synchronisedIdCorrelator, not(nullValue()));

        doAnswer(invocation -> {
            validateLocks(true);
            validateStringParams(invocation, 1);
            return null;
        }).when(mockIdCorrelator).addSystem(anyString());

        synchronisedIdCorrelator.addSystem("first");
        verify(mockIdCorrelator, times(1)).addSystem(anyString());
    }

    @Test
    public void removeSystem() {
        assertThat(mockIdCorrelator, not(nullValue()));
        assertThat(synchronisedIdCorrelator, not(nullValue()));

        doAnswer(invocation -> {
            validateLocks(true);
            validateStringParams(invocation, 1);
            return null;
        }).when(mockIdCorrelator).removeSystem(anyString());

        synchronisedIdCorrelator.removeSystem("first");
        verify(mockIdCorrelator, times(1)).removeSystem(anyString());
    }

    @Test
    public void addCore() {
        assertThat(mockIdCorrelator, not(nullValue()));
        assertThat(synchronisedIdCorrelator, not(nullValue()));

        doAnswer(invocation -> {
            validateLocks(true);
            validateStringParams(invocation, 1);
            return true;
        }).when(mockIdCorrelator).addCore(anyString());

        synchronisedIdCorrelator.addCore("first");
        verify(mockIdCorrelator, times(1)).addCore(anyString());
    }

    @Test
    public void removeCore() {
        assertThat(mockIdCorrelator, not(nullValue()));
        assertThat(synchronisedIdCorrelator, not(nullValue()));

        doAnswer(invocation -> {
            validateLocks(true);
            validateStringParams(invocation, 1);
            return null;
        }).when(mockIdCorrelator).removeCore(anyString());

        synchronisedIdCorrelator.removeCore("first");
        verify(mockIdCorrelator, times(1)).removeCore(anyString());
    }

    @Test
    public void getCores() {
        assertThat(mockIdCorrelator, not(nullValue()));
        assertThat(synchronisedIdCorrelator, not(nullValue()));

        doAnswer(invocation -> {
            validateLocks(false);
            validateStringParams(invocation, 2);
            return Collections.emptySet();
        }).when(mockIdCorrelator).getCores(anyString(), anyString());

        synchronisedIdCorrelator.getCores("first", "second");
        verify(mockIdCorrelator, times(1)).getCores(anyString(), anyString());
    }

    @Test
    public void addCorrelation() {
        assertThat(mockIdCorrelator, not(nullValue()));
        assertThat(synchronisedIdCorrelator, not(nullValue()));

        doAnswer(invocation -> {
            validateLocks(true);
            validateStringParams(invocation, 3);
            return null;
        }).when(mockIdCorrelator).addCorrelation(anyString(), anyString(), anyString());

        synchronisedIdCorrelator.addCorrelation("first", "second", "third");
        verify(mockIdCorrelator, times(1)).addCorrelation(anyString(), anyString(), anyString());
    }

    @Test
    public void removeId() {
        assertThat(mockIdCorrelator, not(nullValue()));
        assertThat(synchronisedIdCorrelator, not(nullValue()));

        doAnswer(invocation -> {
            validateLocks(true);
            validateStringParams(invocation, 2);
            return null;
        }).when(mockIdCorrelator).removeId(anyString(), anyString());

        synchronisedIdCorrelator.removeId("first", "second");
        verify(mockIdCorrelator, times(1)).removeId(anyString(), anyString());
    }

    @Test
    public void getIds() {
        assertThat(mockIdCorrelator, not(nullValue()));
        assertThat(synchronisedIdCorrelator, not(nullValue()));

        doAnswer(invocation -> {
            validateLocks(false);
            validateStringParams(invocation, 2);
            return Collections.emptySet();
        }).when(mockIdCorrelator).getIds(anyString(), anyString());

        synchronisedIdCorrelator.getIds("first", "second");
        verify(mockIdCorrelator, times(1)).getIds(anyString(), anyString());
    }

    @Test
    public void write() throws Exception {
        assertThat(mockIdCorrelator, not(nullValue()));
        assertThat(synchronisedIdCorrelator, not(nullValue()));

        IdCorrelatorWriter writer = mock(IdCorrelatorWriter.class);

        doAnswer(invocation -> {
            validateLocks(false);
            assertThat(invocation.getArgument(0), equalTo(writer));
            return null;
        }).when(mockIdCorrelator).write(any());

        synchronisedIdCorrelator.write(writer);
        verify(mockIdCorrelator, times(1)).write(any());
    }

    private void validateLocks(boolean expectWriteLock) {
        assertThat(synchronisedIdCorrelator, not(nullValue()));
        assertThat(synchronisedIdCorrelator.getReadHoldCount(), equalTo(expectWriteLock ? 0 : 1));
        assertThat(synchronisedIdCorrelator.getWriteHoldCount(), equalTo(expectWriteLock ? 1 : 0));
    }

    private void validateStringParams(InvocationOnMock invocation, int numExpectedParams) {
        String[] expectedParams = new String[]{"first", "second", "third"};

        assertThat(numExpectedParams, lessThanOrEqualTo(expectedParams.length));
        assertThat(numExpectedParams, greaterThanOrEqualTo(0));

        assertThat(invocation.getArguments().length, equalTo(numExpectedParams));
        for (int i = 0; i < numExpectedParams; ++i)
            assertThat(invocation.getArgument(i).toString(), equalTo(expectedParams[i]));
    }

}
