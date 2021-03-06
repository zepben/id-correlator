/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.idcorrelator.io;

import com.zepben.annotations.EverythingIsNonnullByDefault;

/**
 * Exception thrown in the IdCorrelatorWriter interface.
 */
@EverythingIsNonnullByDefault
public class IdCorrelatorWriteException extends Exception {

    public IdCorrelatorWriteException() {
    }

    public IdCorrelatorWriteException(String message) {
        super(message);
    }

    public IdCorrelatorWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdCorrelatorWriteException(Throwable cause) {
        super(cause);
    }

    public IdCorrelatorWriteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
