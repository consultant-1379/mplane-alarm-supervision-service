/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.mediation.fm.rest.model;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConnectionNotification implements Serializable {

    public enum ConnectionStatus {
        CONNECTED, DISCONNECTED
    }

    private String fdn;
    private ConnectionStatus connectionStatus;
    private String msmplaneInstance;

    public String getFdn() {
        return fdn;
    }

    public void setFdn(String fdn) {
        this.fdn = fdn;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getMsmplaneInstance() {
        return msmplaneInstance;
    }

    public void setMsmplaneInstance(String msmplaneInstance) {
        this.msmplaneInstance = msmplaneInstance;
    }


    @Override
    public String toString() {
        return "ConnectionNotification{" + "fdn='" + fdn + '\'' + ", connectionStatus=" + connectionStatus + '\'' + ", msmplaneInstance=" + msmplaneInstance + '}';
    }
}
