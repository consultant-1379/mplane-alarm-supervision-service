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

import com.ericsson.cds.cdi.support.rule.ImplementationInstance
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.SharedCdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.notification.event.AttributeChangeData
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsAttributeChangedEvent
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.mediation.fm.oradio.dps.DpsAccess
import spock.lang.Shared

class SupervisionStateManagerSpec extends SharedCdiSpecification {

    private static final def NETWORK_ELEMENT_ID = "TestORadio"
    private static final def NETWORK_ELEMENT_FDN = "NetworkElement=" + NETWORK_ELEMENT_ID
    private static final def FM_ALARM_SUPERVISION_FDN = NETWORK_ELEMENT_FDN + ",FmAlarmSupervision=1"

    private static final def INVALID_NETWORK_ELEMENT_FDN = "NetworkElement=SomeInvalidFdn"

    private static final def NS_OSS_NE_FM_DEF = "OSS_NE_FM_DEF"
    private static final def TYPE_FMALARMSUPERVISION = "FmAlarmSupervision"
    private static final def VERSION_1_0_0 = "1.0.0"

    private static final def TRUE = true
    private static final def FALSE = false


    // Create a stub for Supervision - this is because you can't use a mock for a class if there is a @PostConstruct method
    @ImplementationInstance
    @Shared
    private ManagedObject fmAlarmSupervisionMo = [ getFdn: { FM_ALARM_SUPERVISION_FDN }, getAttribute: { String attributeName -> true } ] as ManagedObject

    // Create a stub for  NetworkElement - this is because you can't use a mock for a class if there is a @PostConstruct method
    @ImplementationInstance
    @Shared
    private ManagedObject networkElementMo = [ getFdn: { NETWORK_ELEMENT_FDN }, getChild: { String childRdn -> fmAlarmSupervisionMo } ] as ManagedObject

    // Create a stub for DpsAccess - this is because you can't use a mock for a class if there is a @PostConstruct method
    @ImplementationInstance
    @Shared
    private DpsAccess dpsAccess = new DpsAccess() {
        @Override
        List<ManagedObject> createRestrictionTypeQuery(String namespace, String type, String restrictionType, String nodeType) {
            return [networkElementMo]
        }
    }

    @ObjectUnderTest
    @Shared
    private SupervisionStateManager supervisionStateManager

    void setup() {
        // Nothing to do
    }

    /*
        TESTS
     */

    def "initialiseCache should populate the cache with ACTIVE supervision state"() {

        // Note: No need to call initialiseCache because the cdi framework calls @PostConstruct on creation

        expect: "cache is populated and supervision is active"
            supervisionStateManager.isSupervisionActive(NETWORK_ELEMENT_FDN)

    }

    def "Update cache with INACTIVE when a FmAlarmSupervision active=false notification received"() {

        // Note: No need to call initialiseCache because the cdi framework calls @PostConstruct on creation

        given: "Supervision state is active"
            def startingSupervisionState = supervisionStateManager.isSupervisionActive(NETWORK_ELEMENT_FDN)
            assert startingSupervisionState

        and: "DpsAttributeChangedEvent where FmAlarmSupervision.active=false"
            def attrChangeEvent = createDpsAttributeChangedEvent(TRUE, FALSE, FM_ALARM_SUPERVISION_FDN);

        when: "updateSupervisionStateCache is called with DISCONNECT event"
            supervisionStateManager.onSupervisionStateChange(attrChangeEvent)

        then: "Supervision should be inactive after update"
            startingSupervisionState != supervisionStateManager.isSupervisionActive(NETWORK_ELEMENT_FDN)
            !supervisionStateManager.isSupervisionActive(NETWORK_ELEMENT_FDN)
    }

    def "Update cache with ACTIVE when a FmAlarmSupervision active=true notification received"() {

        // Note: No need to call initialiseCache because the cdi framework calls @PostConstruct on creation

        given: "Supervision state is inactive"
            def startingSupervisionState = supervisionStateManager.isSupervisionActive(NETWORK_ELEMENT_FDN)
            assert !startingSupervisionState

        and: "DpsAttributeChangedEvent where FmAlarmSupervision.active=true"
            def attrChangeEvent = createDpsAttributeChangedEvent(FALSE, TRUE, FM_ALARM_SUPERVISION_FDN);

        when: "updateSupervisionStateCache is called with DISCONNECT event"
            supervisionStateManager.onSupervisionStateChange(attrChangeEvent)

        then: "Supervision should be active after update"
            startingSupervisionState != supervisionStateManager.isSupervisionActive(NETWORK_ELEMENT_FDN)
            supervisionStateManager.isSupervisionActive(NETWORK_ELEMENT_FDN)
    }

    def "Cache not updated when a NULL notification is received"() {

        // Note: No need to call initialiseCache because the cdi framework calls @PostConstruct on creation

        given: "Supervision state is active"
            def startingSupervisionState = supervisionStateManager.isSupervisionActive(NETWORK_ELEMENT_FDN)
            assert startingSupervisionState

        and: "DpsAttributeChangedEvent where event is NULL"
            def attrChangeEvent = null;

        when: "updateSupervisionStateCache is called with DISCONNECT event"
            supervisionStateManager.onSupervisionStateChange(attrChangeEvent)

        then: "Supervision should still be active after update"
            startingSupervisionState == supervisionStateManager.isSupervisionActive(NETWORK_ELEMENT_FDN)
            supervisionStateManager.isSupervisionActive(NETWORK_ELEMENT_FDN)
    }

    def "Cache not updated when notification contains no FDN value"() {

        // Note: No need to call initialiseCache because the cdi framework calls @PostConstruct on creation

        given: "Supervision state is active"
            def startingSupervisionState = supervisionStateManager.isSupervisionActive(NETWORK_ELEMENT_FDN)
            assert startingSupervisionState

        and: "DpsAttributeChangedEvent where event fdn is NULL"
            def attrChangeEvent = createDpsAttributeChangedEvent(FALSE, TRUE, null)

        when: "updateSupervisionStateCache is called with DISCONNECT event"
            supervisionStateManager.onSupervisionStateChange(attrChangeEvent)

        then: "Supervision should still be active after update"
            startingSupervisionState == supervisionStateManager.isSupervisionActive(NETWORK_ELEMENT_FDN)
            supervisionStateManager.isSupervisionActive(NETWORK_ELEMENT_FDN)
    }

    def "isSuperActive returns false when fdn does not exist"() {

        // Note: No need to call initialiseCache because the cdi framework calls @PostConstruct on creation

        expect: "False returned indicating supervision not active"
            !supervisionStateManager.isSupervisionActive(INVALID_NETWORK_ELEMENT_FDN)
    }

    def createDpsAttributeChangedEvent(def oldValue, def newValue, def fdn) {
        def changeData = new AttributeChangeData("active", oldValue, newValue, null, null)
        return new DpsAttributeChangedEvent(NS_OSS_NE_FM_DEF, TYPE_FMALARMSUPERVISION, VERSION_1_0_0, 1L, fdn, "Live", [changeData])

    }
}