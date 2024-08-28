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

package com.ericsson.oss.mediation.fm.oradio;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.notification.event.AttributeChangeData;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsAttributeChangedEvent;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectDeletedEvent;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.util.StringUtils;
import com.ericsson.oss.itpf.sdk.eventbus.annotation.Consumes;
import com.ericsson.oss.mediation.fm.oradio.dps.DpsAccess;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Startup
@Slf4j
public class SupervisionStateManager {

    private static final String FM_ALARM_SUPERVISION_EVENT_ENDPOINT = "jms:/topic/FmSupervisionStatusTopic";

    //Clustered-dps-notification-event
    private static final String DPS_NOTIFICATION_EVENT_ENDPOINT = "jms:/topic/dps-notification-event";
    private static final String FM_ALARM_SUPERVISION_FILTER = "((bucketName IS NOT NULL) AND (bucketName = 'Live')) AND ((namespace IS NOT NULL) " +
            "AND (namespace = 'OSS_NE_FM_DEF')) AND ((type IS NOT NULL) AND (type='FmAlarmSupervision'))";

    private static final String OSS_NE_DEF_NS = "OSS_NE_DEF";
    private static final String ORADIO_NE_TYPE = "ORadio";
    private static final String NE_TYPE_ATTR = "neType";
    private static final String NETWORK_ELEMENT_TYPE = "NetworkElement";
    private static final String FM_ALARM_SUPERVISION_TYPE = "FmAlarmSupervision";
    private static final String FM_ALARM_SUPERVISION_RDN = FM_ALARM_SUPERVISION_TYPE + "=1";

    private static final String ACTIVE_ATTR = "active";

    @Inject
    private DpsAccess dpsAccess;

    private Map<String, SupervisionState> supervisionStateCache = new ConcurrentHashMap<>();

    @PostConstruct
    void initialiseSupervisionStateCache() {
        try {
            log.debug("Initialising SupervisionStateCache");

            final List<ManagedObject> networkElementMoList = dpsAccess.createRestrictionTypeQuery(OSS_NE_DEF_NS, NETWORK_ELEMENT_TYPE,
                    NE_TYPE_ATTR, ORADIO_NE_TYPE);

            log.debug("Found potential NetworkElement MOs to add to cache {}", networkElementMoList);

            for (final ManagedObject networkElementMo : networkElementMoList) {
                final String networkElementFdn = networkElementMo.getFdn();
                log.debug("Found NetworkElement MO with fdn {}", networkElementFdn);
                final ManagedObject fmSupervisionMo = networkElementMo.getChild(FM_ALARM_SUPERVISION_RDN);

                if (fmSupervisionMo == null) {
                    log.debug("Node with fdn {} does not have an FmAlarmSupervision child so not adding to cache.", networkElementFdn);
                    continue;
                }

                log.debug("Found FmAlarmSupervision MO with fdn {}", fmSupervisionMo.getFdn());

                final boolean activeAttrValue = Boolean.TRUE.equals(fmSupervisionMo.getAttribute(ACTIVE_ATTR));
                final SupervisionState state = SupervisionState.fromActiveAttrValue(activeAttrValue);

                log.debug("Adding fdn {} with state {} to cache.", fmSupervisionMo.getFdn(), state);
                supervisionStateCache.put(networkElementFdn, SupervisionState.fromActiveAttrValue(activeAttrValue));
            }
            log.debug("Initialised SupervisionStateCache with {} entries.", supervisionStateCache.size());
        } catch (final Exception e) {
            // TODO catch proper exception and decide what to do with it
            log.error("Failed to initialise supervision state cache", e);
        }
    }

    @PreDestroy
    void onServiceStopping() {
        supervisionStateCache = null;
        log.info("Stopping SupervisionStateManager");
    }

    public void onNodeDeletedEvent(@Observes @Consumes(endpoint = DPS_NOTIFICATION_EVENT_ENDPOINT, filter = "type = 'NetworkElement'")//NETWORKELEMENT_DELETE_FILTER)
                                   final DpsObjectDeletedEvent event) {
        log.info("Received DpsObjectDeletedEvent {}. Removing node from cache if it exists.", event);
        supervisionStateCache.remove(event.getFdn());
    }

    /**
     * Consumes events from dps-notification-event JMS topic. The purpose of this method is to keep a local cache update with the FmAlarmSupervision
     * state for a managed node.
     *
     * @param dpsAttributeChangedEvent
     *          attribute changed event triggered by DPS
     */
    public void onSupervisionStateChange(@Observes @Consumes(endpoint = FM_ALARM_SUPERVISION_EVENT_ENDPOINT, filter = FM_ALARM_SUPERVISION_FILTER)
                                         final DpsAttributeChangedEvent dpsAttributeChangedEvent) {

        if (dpsAttributeChangedEvent == null) {
            log.debug("Invalid notification received by SupervisionStateManager");
            return;
        }

        log.debug("SupervisionStateManager.updateSupervisionStateCache() with event {}", dpsAttributeChangedEvent);

        final String fmAlarmSupervisionFdn = dpsAttributeChangedEvent.getFdn();
        final Set<AttributeChangeData> changedAttributes = dpsAttributeChangedEvent.getChangedAttributes();

        if (StringUtils.isEmpty(fmAlarmSupervisionFdn) || changedAttributes == null || changedAttributes.isEmpty()) {
            log.warn("Event {} missing FDN or changed attributes.", dpsAttributeChangedEvent);
            return;
        }

        final String networkElementFdn = fmAlarmSupervisionFdn.substring(0, fmAlarmSupervisionFdn.indexOf(','));

        for (final AttributeChangeData changedAttribute : changedAttributes) {
            if (ACTIVE_ATTR.equals(changedAttribute.getName())) {
                final boolean activeAttrValue = Boolean.TRUE.equals(changedAttribute.getNewValue());
                final SupervisionState supervisionState = SupervisionState.fromActiveAttrValue(activeAttrValue);
                supervisionStateCache.put(networkElementFdn, supervisionState);
                log.info("Updated supervision state for FDN {} to {}", networkElementFdn, supervisionState);
            }
        }
    }

    public boolean isSupervisionActive(final String networkElementFdn) {
        if (supervisionStateCache.containsKey(networkElementFdn)) {
            final SupervisionState state = supervisionStateCache.get(networkElementFdn);
            return state.equals(SupervisionState.ACTIVE);
        }
        return false;
    }

    enum SupervisionState {
        ACTIVE(true),
        INACTIVE(false);

        private final boolean stateValue;

        SupervisionState(final boolean stateValue) {
            this.stateValue = stateValue;
        }

        public static SupervisionState fromActiveAttrValue(final boolean stateValue) {
            for (SupervisionState b : SupervisionState.values()) {
                if (b.stateValue == stateValue) {
                    return b;
                }
            }
            return null;
        }
    }

}