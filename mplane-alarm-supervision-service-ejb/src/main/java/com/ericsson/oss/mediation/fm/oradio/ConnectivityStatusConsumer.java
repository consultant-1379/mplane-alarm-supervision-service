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

package com.ericsson.oss.mediation.fm.oradio;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.eventbus.annotation.Consumes;
import com.ericsson.oss.mediation.fm.oradio.api.MPlaneAlarmService;
import com.ericsson.oss.mediation.fm.oradio.models.ConnectionStatus;
import com.ericsson.oss.mediation.fm.oradio.models.NodeConnectivityStatus;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ConnectivityStatusConsumer {

    private static final String MPLANE_ALARM_SUPERVISION_CHANNEL_URI = "jms:/queue/MplaneAlarmSupervisionQueue";

    @EServiceRef
    private MPlaneAlarmService mPlaneAlarmService;

    @Inject
    private SupervisionStateManager supervisionStateManager;

    @PostConstruct
    void onServiceStarted() {
        log.info("Starting ConnectivityStatusConsumer in MASS");
    }

    @PreDestroy
    void onServiceStopping() {
        log.info("Stopping ConnectivityStatusConsumer in MASS");
    }


    public void onMessage(@Observes @Consumes(endpoint = MPLANE_ALARM_SUPERVISION_CHANNEL_URI) final NodeConnectivityStatus connectivityMessage) {

        if (connectivityMessage == null) {
            log.debug("Invalid notification received on MplaneAlarmSupervisionQueue.");
            return;
        }

        log.debug("ConnectivityStatusConsumer.onMessage() with message {}", connectivityMessage);

        final String networkElementFdn = connectivityMessage.getNetworkElementFdn();
        final ConnectionStatus status = connectivityMessage.getConnectionStatus();

        final boolean isSupervisionActive = supervisionStateManager.isSupervisionActive(networkElementFdn);

        if (!isSupervisionActive) {
            log.warn("Node with FDN {} is not under FM Supervision. ConnectivityStatus Message will be dropped.", networkElementFdn);
        } else {
            log.debug("ConnectionStatus of node with fdn {}: {}", networkElementFdn, status);
            if (status == ConnectionStatus.CONNECTED) {
                mPlaneAlarmService.clearHeartbeatAlarm(networkElementFdn);
            } else if (status == ConnectionStatus.DISCONNECTED) {
                mPlaneAlarmService.raiseHeartbeatAlarm(networkElementFdn);
            }
        }
    }
}