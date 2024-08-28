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

package com.ericsson.oss.mediation.fm.rest.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ericsson.oss.itpf.sdk.eventbus.Channel;
import com.ericsson.oss.itpf.sdk.eventbus.ChannelLocator;
import com.ericsson.oss.itpf.sdk.eventbus.annotation.Endpoint;
import com.ericsson.oss.mediation.fm.oradio.models.ConnectionStatus;
import com.ericsson.oss.mediation.fm.oradio.models.NodeConnectivityStatus;
import com.ericsson.oss.mediation.fm.rest.model.ConnectionNotification;
import lombok.extern.slf4j.Slf4j;

@Path("mplaneAlarmSupervision/")
@Slf4j
public class MPlaneQueueTestResource {
    private static final String MPLANE_ALARM_SUPERVISION_CHANNEL_URI = "jms:/queue/MplaneAlarmSupervisionQueue";

    @Inject
    @Endpoint(MPLANE_ALARM_SUPERVISION_CHANNEL_URI)
    private Channel channel;

    @Inject
    private ChannelLocator channelLocator;

    @GET
    public Response getMessage() {
        log.info("You've reached getMessage in MPlaneQueueTestResource.");
        return Response.ok("It's all good").build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response populate(final List<ConnectionNotification> notifications) {
        log.info("you are in populate method");
        for (final ConnectionNotification notification : notifications) {
            log.info(notification.toString());
            final String fdn = notification.getFdn();
            final String status = notification.getConnectionStatus().name();
            final NodeConnectivityStatus nodeConnectivityStatus = new NodeConnectivityStatus(fdn, ConnectionStatus.valueOf(status), notification.getMsmplaneInstance()) ;
            channel.send(nodeConnectivityStatus);
        }
        return Response.ok(notifications).build();
    }

}
