/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.mediation.fm.test.jee.model;

import com.ericsson.oss.itpf.modeling.annotation.DefaultValue;
import com.ericsson.oss.itpf.modeling.annotation.DefaultValues;
import com.ericsson.oss.itpf.modeling.annotation.EModel;
import com.ericsson.oss.itpf.modeling.annotation.configparam.ConfParamDefinition;
import com.ericsson.oss.itpf.modeling.annotation.configparam.ConfParamDefinitions;
import com.ericsson.oss.itpf.modeling.annotation.configparam.Scope;

/**
 * A example of configuration parameter modeling, which is used by this sample project test.
 */
@EModel(namespace = "ModeledConfigurationExample", description = "example of modeled configuration")
@ConfParamDefinitions
public class SampleModeledConfiguration {

    @ConfParamDefinition(description = "testNumber")
    @DefaultValue("123456")
    public int intProp;

    @ConfParamDefinition(description = "intArrayProp")
    @DefaultValues({ @DefaultValue("11"), @DefaultValue("12"), @DefaultValue("13"), @DefaultValue("14") })
    public int[] intArrayProp;

    @ConfParamDefinition(description = "boolProp", scope = Scope.GLOBAL)
    @DefaultValue("true")
    public boolean boolProp;

    @ConfParamDefinition(description = "stringArrayProp")
    @DefaultValues({ @DefaultValue("orange"), @DefaultValue("pear"), @DefaultValue("mango"), @DefaultValue("chicken"), @DefaultValue("duck") })
    public String[] stringArrayProp;

    @ConfParamDefinition(description = "stringProp", scope = Scope.GLOBAL)
    @DefaultValue("apple")
    public String stringProp;

}
