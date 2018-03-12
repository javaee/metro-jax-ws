/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2006 Guillaume Nodet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jvnet.jax_ws_commons.jaxws;

import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Reads a JAX-WS service endpoint implementation class
 * and generates all of the portable artifacts for a JAX-WS web service
 * (into the generate test source directory).
 *
 * <p>
 * <code>${maven.test.skip}</code> property is honored. If it is set, code generation is skipped.
 * </p>
 *
 */
@Mojo(name = "wsgen-test", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, requiresDependencyResolution = ResolutionScope.TEST)
public class TestWsGenMojo extends AbstractWsGenMojo {
    
    /**
     * Specify where to place output generated classes. Use <code>xnocompile</code>
     * to turn this off.
     */
    @Parameter(defaultValue = "${project.build.testOutputDirectory}")
    private File destDir;

    /**
     * Specify where to place generated source files, keep is turned on with this option.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/test-wsgen")
    private File sourceDestDir;

    /**
     * Directory containing the generated wsdl files.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/test-wsdl")
    private File resourceDestDir;

    /**
     * Set this to "true" to bypass code generation.
     */
    @Parameter(property = "maven.test.skip")
    private boolean skip;

    /**
     * Either ${build.outputDirectory} or ${build.testOutputDirectory}.
     */
    @Override
    protected File getDestDir() {
        return destDir;
    }

    /**
     * ${project.build.directory}/generated-sources/test-wsgen.
     */
    @Override
    protected File getSourceDestDir() {
        return sourceDestDir;
    }

    @Override
    protected void addSourceRoot(String sourceDir) {
        if (!project.getTestCompileSourceRoots().contains(sourceDir)) {
            getLog().debug("adding test src root: " + sourceDir);
            project.addTestCompileSourceRoot(sourceDir);
        } else {
            getLog().debug("existing test src root: " + sourceDir);
        }
    }

    @Override
    protected File getResourceDestDir() {
        return resourceDestDir;
    }

    @Override
    protected File getDefaultSrcOut() {
        return new File(project.getBuild().getDirectory(), "generated-sources/test-wsgen");
    }

    @Override
    protected File getClassesDir() {
        return new File(project.getBuild().getTestOutputDirectory());
    }

    @Override
    protected String getExtraClasspath() {
        String cp = super.getExtraClasspath();
        StringBuilder buf = new StringBuilder();
        int i = cp.indexOf(File.pathSeparatorChar);
        buf.append(i > 0 ? cp.substring(0, i) : cp);
        buf.append(File.pathSeparatorChar);
        buf.append(project.getBuild().getOutputDirectory());
        if (i > 0 && cp.substring(i).length() > 0) {
            buf.append(cp.substring(i));
        }
        return buf.toString();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //if maven.test.skip is set test compilation is not called, so
        //no need to generate sources/classes
        if (skip) {
            getLog().info("Skipping tests, nothing to do.");
        } else {
            super.execute();
        }
    }
}
