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

import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor;
import com.ericsson.oss.itpf.datalayer.dps.query.Restriction;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;

@ApplicationScoped
public class CdiDpsAccessBean implements DpsAccess {

    @EServiceRef
    private DataPersistenceService dps;

    @Override
    public List<ManagedObject> createRestrictionTypeQuery(final String namespace, final String type, final String restrictionType,
                                                          final String nodeType) {
        final QueryBuilder queryBuilder = dps.getQueryBuilder();
        final Query<TypeRestrictionBuilder> query = queryBuilder.createTypeQuery(namespace, type);
        final Restriction restriction = query.getRestrictionBuilder().equalTo(restrictionType, nodeType);
        query.setRestriction(restriction);
        final DataBucket liveBucket = dps.getLiveBucket();
        final QueryExecutor queryExecutor = liveBucket.getQueryExecutor();
        return queryExecutor.getResultList(query);
    }
}