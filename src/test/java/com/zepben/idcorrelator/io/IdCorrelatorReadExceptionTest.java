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


public class IdCorrelatorReadExceptionTest {

    @Test
    public void constructorCoverageTest() {
        String message = "messageAndCauseTest";
        Throwable cause = new Throwable();

        expect(() -> {
            throw new IdCorrelatorReadException();
        })
            .toThrow(IdCorrelatorReadException.class);

        expect(() -> {
            throw new IdCorrelatorReadException(cause);
        })
            .toThrow(IdCorrelatorReadException.class)
            .withCause(cause.getClass());

        expect(() -> {
            throw new IdCorrelatorReadException(message);
        })
            .toThrow(IdCorrelatorReadException.class)
            .withMessage(message)
            .withoutCause();

        expect(() -> {
            throw new IdCorrelatorReadException(message, cause);
        })
            .toThrow(IdCorrelatorReadException.class)
            .withMessage(message)
            .withCause(cause.getClass());

        expect(() -> {
            throw new IdCorrelatorReadException(message, cause, true, true);
        })
            .toThrow(IdCorrelatorReadException.class)
            .withMessage(message)
            .withCause(cause.getClass());
    }


}