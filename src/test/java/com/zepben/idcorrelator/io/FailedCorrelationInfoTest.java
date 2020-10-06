/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.idcorrelator.io;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class FailedCorrelationInfoTest {

    private static final FailedCorrelationInfo testFailedCorrelationInfo = new FailedCorrelationInfo("testCoreId", "testSystemTag", "testSystemId", "testDetails");

    @Test
    public void coreIdTest() {
        assertThat(testFailedCorrelationInfo.coreId(), equalTo("testCoreId"));

    }

    @Test
    public void systemTagTest() {
        assertThat(testFailedCorrelationInfo.systemTag(), equalTo("testSystemTag"));
    }


    @Test
    public void systemIdTest() {
        assertThat(testFailedCorrelationInfo.systemId(), equalTo("testSystemId"));
    }

    @Test
    public void detailsTest() {
        assertThat(testFailedCorrelationInfo.details(), equalTo("testDetails"));
    }

}