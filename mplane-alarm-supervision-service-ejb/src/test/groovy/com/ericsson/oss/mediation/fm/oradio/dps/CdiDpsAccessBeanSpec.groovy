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

package com.ericsson.oss.mediation.fm.oradio.dps

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.SharedCdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import spock.lang.Shared

class CdiDpsAccessBeanSpec extends SharedCdiSpecification {

    private static final def VERSION_1_0_0 = "1.0.0"
    private static final def ORADIO_NE_TYPE = "ORadio"
    private static final def NODE = "NODE"
    private static final def TYPE = "type"
    private static final def CATEGORY = "category"
    private static final def DPS = "dps"
    private static final def TARGET = "target"
    private static final def NAME = "name"

    @Shared
    private RuntimeConfigurableDps configurableDps

    @ObjectUnderTest
    @Shared
    private CdiDpsAccessBean cdiDpsAccessBean

    void setupSpec() {
        configurableDps = super.cdiInjector.getService(RuntimeConfigurableDps)
        createExistingMosInDb()
    }

    def "createTypeQuery returns correct ManagedObjects based on namespace and type"() {
        given:
            def namespace = "OSS_NE_DEF"
            def type= "NetworkElement"
            def neType = "ORadio"

        when: "createTypeQuery is called with a specific namespace and type"
            def result = cdiDpsAccessBean.createRestrictionTypeQuery(namespace, type,"neType", neType)

        then:
            result.namespace == ["OSS_NE_DEF"]
            result.type == ["NetworkElement"]
            result[0].getAttribute("neType") == ORADIO_NE_TYPE
    }

    def createExistingMosInDb() {
        // Add the CI tree for an existing node
        def existingNetworkElement = configurableDps.addManagedObject()
                .withFdn("NetworkElement=TestORadio")
                .namespace("OSS_NE_DEF")
                .type("NetworkElement")
                .addAttribute("neType","ORadio")
                .build()
    }

    def createTarget(name) {
        return configurableDps.addPersistenceObject()
                .namespace(DPS)
                .type(TARGET)
                .version(VERSION_1_0_0)
                .addAttributes(getTargetAttributesMap(name))
                .create();
    }

    def getTargetAttributesMap(name) {
        final Map<String, Object> targetMap = new HashMap<>();
        targetMap.put(TYPE, ORADIO_NE_TYPE);
        targetMap.put(CATEGORY, NODE);
        targetMap.put(NAME, name);
        return targetMap;
    }
}