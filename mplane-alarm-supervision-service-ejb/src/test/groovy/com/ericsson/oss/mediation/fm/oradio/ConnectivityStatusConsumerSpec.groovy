/*
 * ------------------------------------------------------------------------------
 * ******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *******************************************************************************
 * -------------------------------------------------------------------------------
 */

package com.ericsson.oss.mediation.fm.oradio

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.mediation.fm.oradio.api.MPlaneAlarmService
import com.ericsson.oss.mediation.fm.oradio.models.ConnectionStatus
import com.ericsson.oss.mediation.fm.oradio.models.NodeConnectivityStatus

import javax.inject.Inject

class ConnectivityStatusConsumerSpec extends CdiSpecification {

    @MockedImplementation
    SupervisionStateManager supervisionStateManager

    @Inject
    MPlaneAlarmService mPlaneAlarmService


    @ObjectUnderTest
    ConnectivityStatusConsumer consumer


    def "Should drop the message if node is not under supervision"() {
        given: "Node with CONNECTED connectivity status and supervision inactive"
            NodeConnectivityStatus connectivityMessage = new NodeConnectivityStatus("NE123", ConnectionStatus.CONNECTED, "msmplane-1")
            supervisionStateManager.isSupervisionActive("NE123") >> false

        when: "onMessage triggered with connectivity message"
            consumer.onMessage(connectivityMessage)

        then: "No methods from mplaneAlarmService should be called"
            0 * mPlaneAlarmService._() // No interaction with mPlaneAlarmService should occur
    }

    def "Should clear heartbeat alarm when a connected node is under supervision"() {
        given: "Node with CONNECTED connectivity status and supervision active"
            String networkElementFdn = "NE123"
            NodeConnectivityStatus connectivityMessage = new NodeConnectivityStatus("NE123", ConnectionStatus.CONNECTED, "msmplane-1")

            supervisionStateManager.isSupervisionActive(networkElementFdn) >> true

        when: "onMessage triggered with connectivity message"
            consumer.onMessage(connectivityMessage)

        then: "one clearHeartBeat message should be sent"
            1 * mPlaneAlarmService.clearHeartbeatAlarm(networkElementFdn)
    }

    def "Should raise heartbeat alarm when a disconnected node is under supervision"() {
        given: "Node with DISCONNECTED connectivity status and supervision active"
            String networkElementFdn = "NE123"
            NodeConnectivityStatus connectivityMessage = new NodeConnectivityStatus("NE123", ConnectionStatus.DISCONNECTED, "msmplane-1")

            supervisionStateManager.isSupervisionActive(networkElementFdn) >> true

        when: "onMessage triggered with connectivity message"
            consumer.onMessage(connectivityMessage)

        then: "one raiseHeartBeat message should be sent"
            1 * mPlaneAlarmService.raiseHeartbeatAlarm(networkElementFdn)
    }
}
