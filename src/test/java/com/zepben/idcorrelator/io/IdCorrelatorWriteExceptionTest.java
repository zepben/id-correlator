/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.idcorrelator.io;

import org.junit.jupiter.api.Test;

import static com.zepben.testutils.exception.ExpectException.expect;


public class IdCorrelatorWriteExceptionTest {

    @Test
    public void constructorCoverageTest() {
        String message = "messageAndCauseTest";
        Throwable cause = new Throwable();

        expect(() -> {
            throw new IdCorrelatorWriteException();
        })
            .toThrow(IdCorrelatorWriteException.class);

        expect(() -> {
            throw new IdCorrelatorWriteException(cause);
        })
            .toThrow(IdCorrelatorWriteException.class)
            .withCause(cause.getClass());

        expect(() -> {
            throw new IdCorrelatorWriteException(message);
        })
            .toThrow(IdCorrelatorWriteException.class)
            .withMessage(message)
            .withoutCause();

        expect(() -> {
            throw new IdCorrelatorWriteException(message, cause);
        })
            .toThrow(IdCorrelatorWriteException.class)
            .withMessage(message)
            .withCause(cause.getClass());

        expect(() -> {
            throw new IdCorrelatorWriteException(message, cause, true, true);
        })
            .toThrow(IdCorrelatorWriteException.class)
            .withMessage(message)
            .withCause(cause.getClass());
    }

}