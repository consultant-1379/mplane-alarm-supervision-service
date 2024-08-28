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

package com.ericsson.oss.mediation.fm.oradio.dps;

import java.util.List;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;

public interface DpsAccess {

    List<ManagedObject> createRestrictionTypeQuery(final String namespace, final String type, final String restrictionType, final String nodeType);
}
