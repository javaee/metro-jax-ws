/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.ws.ant;

import com.sun.tools.ws.wscompile.WsgenTool;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 *
 * @author lukas
 */
class WsGenBase extends WsTask2 {

    /**
     * **********************  -classpath option ************************
     */
    protected Path compileClasspath = null;

    /**
     * List of external metadata files; those are necessary if it's impossible to use/modify
     * annotations in ws implementation (for example only binaries are available)
     */
    private final List<ExternalMetadata> externalMetadataFiles = new ArrayList<ExternalMetadata>();

    /**
     * Gets the classpath.
     *
     * @return user defined classpath.
     */
    public Path getClasspath() {
        return compileClasspath;
    }

    /**
     * Set the classpath to be used for this compilation.
     *
     * @param classpath user defined classpath.
     */
    public void setClasspath(Path classpath) {
        if (compileClasspath == null) {
            compileClasspath = classpath;
        } else {
            compileClasspath.append(classpath);
        }
    }

    /**
     * Creates a nested classpath element.
     *
     * @return classpath created.
     */
    public Path createClasspath() {
        if (compileClasspath == null) {
            compileClasspath = new Path(getProject());
        }
        return compileClasspath.createPath();
    }

    /**
     * Adds a reference to a CLASSPATH defined elsewhere.
     *
     * @param r classpath reference.
     */
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    /*************************  -cp option *************************/
    /**
     * Gets the classpath.
     *
     * @return user defined classpath.
     */
    public Path getCP() {
        return getClasspath();
    }

    /**
     * Set the classpath to be used for this compilation.
     *
     * @param classpath user defined classpath.
     */
    public void setCP(Path classpath) {
        setClasspath(classpath);
    }

    /**
     * *****************  -inlineSchemas option *********************
     */
    protected boolean inlineSchemas;

    /**
     * Gets the "inlineSchemas" flag.
     *
     * @return true if schema should be inlined in a WSDL.
     */
    public boolean getInlineSchemas() {
        return inlineSchemas;
    }

    /**
     * Sets the "inlineSchemas" flag.
     *
     * @param inlineSchemas true to inline schema in a WSDL.
     */
    public void setInlineSchemas(boolean inlineSchemas) {
        this.inlineSchemas = inlineSchemas;
    }

    /**
     * **********************  -r option ************************
     */
    private File resourceDestDir = null;

    /**
     * Gets the directory for non-class generated files.
     *
     * @return destination directory for generated resource(s).
     */
    public File getResourcedestdir() {
        return this.resourceDestDir;
    }

    /**
     * Sets the directory for non-class generated files.
     *
     * @param resourceDir destination directory for generated resource(s).
     */
    public void setResourcedestdir(File resourceDir) {
        this.resourceDestDir = resourceDir;
    }

    /**
     * **********************  -wsdl option ************************
     */
    private boolean genWsdl = false;

    /**
     * Gets the genWsdl flag.
     *
     * @return true if WSDL should be generated.
     */
    public boolean getGenwsdl() {
        return genWsdl;
    }

    /**
     * Sets the genWsdl flag.
     *
     * @param genWsdl true to generate a WSDL.
     */
    public void setGenwsdl(boolean genWsdl) {
        this.genWsdl = genWsdl;
    }

    /**
     * **********  protocol option used only with -wsdl option****************
     */
    private String protocol = "";

    /**
     * Gets the protocol.
     *
     * @return protocol.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the protocol.
     *
     * @param protocol protocol.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * **********  serviceName option used only with -wsdl option****************
     */
    private String serviceName = null;

    /**
     * Gets the serviceName.
     *
     * @return service name.
     */
    public String getServicename() {
        return serviceName;
    }

    /**
     * Sets the serviceName.
     *
     * @param name service name.
     */
    public void setServicename(String name) {
        this.serviceName = name;
    }

    /**
     * **********  portName option used only with -wsdl option****************
     */
    private String portName = null;

    /**
     * Gets the portName.
     *
     * @return port name.
     */
    public String getPortname() {
        return portName;
    }

    /**
     * Sets the serviceName.
     *
     * @param name port name.
     */
    public void setPortname(String name) {
        this.portName = name;
    }

    private String sei;

    /**
     * @return Returns the sei.
     */
    public String getSei() {
        return sei;
    }

    /**
     * Set SEI.
     * @param endpointImplementationClass SEI.
     */
    public void setSei(String endpointImplementationClass) {
        this.sei = endpointImplementationClass;
    }

    @Override
    protected CommandlineJava setupCommand() {
        CommandlineJava cmd = super.setupCommand();
        Path classpath = getClasspath();
        if (classpath != null && !classpath.toString().equals("")) {
            cmd.createArgument().setValue("-classpath");
            cmd.createArgument().setPath(classpath);
        }

        //-Xnocompile option
        if (isXnocompile()) {
            cmd.createArgument().setValue("-Xnocompile");
        }

        if (getGenwsdl()) {
            String tmp = "-wsdl";
            if (protocol.length() > 0) {
                tmp += ":" + protocol;
            }
            cmd.createArgument().setValue(tmp);

            if (serviceName != null && serviceName.length() > 0) {
                cmd.createArgument().setValue("-servicename");
                cmd.createArgument().setValue(serviceName);
            }

            if (portName != null && portName.length() > 0) {
                cmd.createArgument().setValue("-portname");
                cmd.createArgument().setValue(portName);
            }
            if (getInlineSchemas()) {
                cmd.createArgument().setValue("-inlineSchemas");
            }
        }

        // r option
        if (null != getResourcedestdir() && !getResourcedestdir().getName().equals("")) {
            cmd.createArgument().setValue("-r");
            cmd.createArgument().setFile(getResourcedestdir());
        }

        if (externalMetadataFiles != null) {
            for (ExternalMetadata file : externalMetadataFiles) {
                cmd.createArgument().setValue("-x");
                cmd.createArgument().setValue(file.file);
            }
        }

        for (String a : getJavacargs().getArguments()) {
            cmd.createArgument().setValue("-J" + a);
        }

        if (getSei() != null) {
            cmd.createArgument().setValue(getSei());
        }
        return cmd;
    }


    /**
     * Called by the project to let the task do it's work *
     */
    @Override
    public void execute() throws BuildException {
        execute("wsgen", "com.sun.tools.ws.WsGen");
    }

    @Override
    protected boolean runInVm(String[] arguments, OutputStream out) {
        WsgenTool compTool = new WsgenTool(out);
        return compTool.run(arguments);
    }

    public ExternalMetadata createExternalMetadata() {                                 // 3
        ExternalMetadata e = new ExternalMetadata();
        externalMetadataFiles.add(e);
        return e;
    }

    public static class ExternalMetadata {
        String file;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }
    }
}
