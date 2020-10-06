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
 * Class that holds the information that produced a failed correlation.
 */
@EverythingIsNonnullByDefault
public class FailedCorrelationInfo {

    private String coreId;
    private String systemTag;
    private String systemId;
    private String details;

    public FailedCorrelationInfo(String coreId, String systemTag, String systemId, String details) {
        this.coreId = coreId;
        this.systemTag = systemTag;
        this.systemId = systemId;
        this.details = details;
    }

    /**
     * @return returns the coreId used in the failed correlation.
     */
    public String coreId() {
        return coreId;
    }

    /**
     * @return returns the systemTag used in the failed correlation.
     */
    public String systemTag() {
        return systemTag;
    }

    /**
     * @return returns the systemTag used in the failed correlation.
     */
    public String systemId() {
        return systemId;
    }

    /**
     * Provides a brief explanation of why the correlation failed.
     *
     * @return returns the details field of the instance.
     */
    public String details() {
        return details;
    }

}
