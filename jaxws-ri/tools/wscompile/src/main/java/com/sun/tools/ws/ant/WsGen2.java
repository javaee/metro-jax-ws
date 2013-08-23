/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

import com.sun.tools.ws.ToolVersion;
import com.sun.tools.ws.wscompile.WsgenTool;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * wsgen task for use with the JAXWS project.
 */
public class WsGen2 extends MatchingTask {

    private CommandlineJava cmd = new CommandlineJava();
    /** Additional command line arguments for Javac. The equivalent of the -J option. */
    private final Commandline javacCmdLine = new Commandline();

    /**
     * **********************  -classpath option ************************
     */
    protected Path compileClasspath = null;

    /**
     * List of external metadata files; those are necessary if it's impossible to use/modify
     * annotations in ws implementation (for example only binaries are available)
     */
    private List<ExternalMetadata> externalMetadataFiles = new ArrayList<ExternalMetadata>();

    /**
     * Gets the classpath.
     */
    public Path getClasspath() {
        return compileClasspath;
    }

    /**
     * Set the classpath to be used for this compilation.
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
     */
    public Path createClasspath() {
        if (compileClasspath == null) {
            compileClasspath = new Path(getProject());
        }
        return compileClasspath.createPath();
    }

    /**
     * Adds a reference to a CLASSPATH defined elsewhere.
     */
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    /*************************  -cp option *************************/
    /**
     * Gets the classpath.
     */
    public Path getCP() {
        return getClasspath();
    }

    /**
     * Set the classpath to be used for this compilation.
     */
    public void setCP(Path classpath) {
        setClasspath(classpath);
    }

    /**
     * **********************  -d option ************************
     */
    private File destDir = null;

    /**
     * Gets the base directory to output generated class. *
     */
    public File getDestdir() {
        return this.destDir;
    }

    /**
     * Sets the base directory to output generated class. *
     */
    public void setDestdir(File base) {
        this.destDir = base;
    }

    /********************* failonerror option  ***********************/
    /**
     * False to continue the build even if the compilation fails.
     */
    private boolean failonerror = true;

    /**
     * Mostly for our SQE teams and not to be advertized.
     */
    public void setFailonerror(boolean value) {
        failonerror = value;
    }

    /**
     * Adds a JVM argument.
     *
     * @return JVM argument created
     */
    public Commandline.Argument createJvmarg() {
        return cmd.createVmArgument();
    }

    /**
     * Adds Javac argument.
     *
     * @since 2.2.9
     * @return Javac argument created
     */
    public Commandline.Argument createJavacarg() {
        return javacCmdLine.createArgument();
    }

    /********************  -Xendorsed option **********************/

    /**
     * Set to true to perform the endorsed directory override so that
     * Ant tasks can run on JavaSE 6.
     * This is used only when fork is true. With fork=false which is default, it is handled way before in the WrapperTask.
     */
    private boolean xendorsed = false;

    public void setXendorsed(boolean xendorsed) {
        this.xendorsed = xendorsed;
    }

    public boolean isXendorsed() {
        return xendorsed;
    }

    /**
     * *****************  -extensions option *********************
     */
    protected boolean extension;

    /**
     * Gets the "extension" flag. *
     */
    public boolean getExtension() {
        return extension;
    }

    /**
     * Sets the "extension" flag. *
     */
    public void setExtension(boolean extension) {
        this.extension = extension;
    }

    /**
     * *****************  -inlineSchemas option *********************
     */
    protected boolean inlineSchemas;

    /**
     * Gets the "inlineSchemas" flag. *
     */
    public boolean getInlineSchemas() {
        return inlineSchemas;
    }

    /**
     * Sets the "inlineSchemas" flag. *
     */
    public void setInlineSchemas(boolean inlineSchemas) {
        this.inlineSchemas = inlineSchemas;
    }

    /**
     * **********************  -keep option ************************
     */
    private boolean keep = false;

    /**
     * Gets the "keep" flag. *
     */
    public boolean getKeep() {
        return keep;
    }

    /**
     * Sets the "keep" flag. *
     */
    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    /**
     * **********************  -fork option ************************
     */
    private boolean fork = false;

    /**
     * Gets the "fork" flag. *
     */
    public boolean getFork() {
        return fork;
    }

    /**
     * Sets the "fork" flag. *
     */
    public void setFork(boolean fork) {
        this.fork = fork;
    }

    /**
     * **********************  -r option ************************
     */
    private File resourceDestDir = null;

    /**
     * Gets the directory for non-class generated files. *
     */
    public File getResourcedestdir() {
        return this.resourceDestDir;
    }

    /**
     * Sets the directory for non-class generated files. *
     */
    public void setResourcedestdir(File resourceDir) {
        this.resourceDestDir = resourceDir;
    }

    /**
     * **********************  -O option ************************
     */
    private boolean optimize = false;

    /**
     * Gets the optimize flag. *
     */
    public boolean getOptimize() {
        return optimize;
    }

    /**
     * Sets the optimize flag. *
     */
    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    /**
     * **********************  -s option ************************
     */
    private File sourceDestDir;

    /**
     * Sets the directory to place generated source java files. *
     */
    public void setSourcedestdir(File sourceBase) {
        keep = true;
        this.sourceDestDir = sourceBase;
    }

    /**
     * Gets the directory to place generated source java files. *
     */
    public File getSourcedestdir() {
        return sourceDestDir;
    }

    /**
     * **********************  -encoding option ************************
     */
    private String encoding;

    /**
     * Sets the encoding for generated source java files. *
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Gets the encoding for generated source java files. *
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * **********************  -verbose option ************************
     */
    protected boolean verbose = false;

    /**
     * Gets the "verbose" flag. *
     */
    public boolean getVerbose() {
        return verbose;
    }

    /**
     * Sets the "verbose" flag. *
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * **********************  -g option ************************
     */
    private boolean debug = false;

    /**
     * Gets the debug flag. *
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Sets the debug flag. *
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isXnocompile() {
        return xnocompile;
    }

    public void setXnocompile(boolean xnocompile) {
        this.xnocompile = xnocompile;
    }

    /**
     * do not compile generated classes *
     */
    private boolean xnocompile = false;

    /**
     * **********************  -wsdl option ************************
     */
    private boolean genWsdl = false;

    /**
     * Gets the genWsdl flag. *
     */
    public boolean getGenwsdl() {
        return genWsdl;
    }

    /**
     * Sets the genWsdl flag. *
     */
    public void setGenwsdl(boolean genWsdl) {
        this.genWsdl = genWsdl;
    }

    /**
     * **********  protocol option used only with -wsdl option****************
     */
    private String protocol = "";

    /**
     * Gets the protocol. *
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the protocol. *
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * **********  serviceName option used only with -wsdl option****************
     */
    private String serviceName = null;

    /**
     * Gets the serviceName. *
     */
    public String getServicename() {
        return serviceName;
    }

    /**
     * Sets the serviceName. *
     */
    public void setServicename(String name) {
        this.serviceName = name;
    }

    /**
     * **********  portName option used only with -wsdl option****************
     */
    private String portName = null;

    /**
     * Gets the portName. *
     */
    public String getPortname() {
        return portName;
    }

    /**
     * Sets the serviceName. *
     */
    public void setPortname(String name) {
        this.portName = name;
    }

    /***********************  include ant runtime **********************/
    /**
     * not sure if these methods are needed
     */
    private boolean includeAntRuntime = false;

    /**
     * Include ant's own classpath in this task's classpath?
     */
    public void setIncludeantruntime(boolean include) {
        includeAntRuntime = include;
    }

    /**
     * Gets whether or not the ant classpath is to be included in the
     * task's classpath.
     */
    public boolean getIncludeantruntime() {
        return includeAntRuntime;
    }

    /***********************  include java runtime **********************/
    /**
     * not sure if these methods are needed
     */
    private boolean includeJavaRuntime = false;

    /**
     * Sets whether or not to include the java runtime libraries to this
     * task's classpath.
     */
    public void setIncludejavaruntime(boolean include) {
        includeJavaRuntime = include;
    }

    /**
     * Gets whether or not the java runtime should be included in this
     * task's classpath.
     */
    public boolean getIncludejavaruntime() {
        return includeJavaRuntime;
    }

    private String sei;

    /**
     * @return Returns the sei.
     */
    public String getSei() {
        return sei;
    }

    public void setSei(String endpointImplementationClass) {
        this.sei = endpointImplementationClass;
    }

    private void setupWscompileCommand() {
        Path classpath = getClasspath();
        if (classpath != null && !classpath.toString().equals("")) {
            cmd.createArgument().setValue("-classpath");
            cmd.createArgument().setPath(classpath);
        }
        setupWscompileArgs();

    }

    private void setupWscompileForkCommand() {

        ClassLoader loader = this.getClass().getClassLoader();
        while (loader != null && !(loader instanceof AntClassLoader)) {
            loader = loader.getParent();
        }

        String antcp = loader != null
                //taskedef cp
                ? ((AntClassLoader) loader).getClasspath()
                //system classloader, ie. env CLASSPATH=...
                : System.getProperty("java.class.path");
        // try to find tools.jar and add it to the cp
        // so the behaviour on all JDKs is the same
        // (avoid creating MaskingClassLoader on non-Mac JDKs)
        File jreHome = new File(System.getProperty("java.home"));
        File toolsJar = new File(jreHome.getParent(), "lib/tools.jar");
        if (toolsJar.exists()) {
            antcp += File.pathSeparatorChar + toolsJar.getAbsolutePath();
        }
        cmd.createClasspath(getProject()).append(new Path(getProject(), antcp));
        String apiCp = getApiClassPath(this.getClass().getClassLoader());
        if (apiCp != null) {
            //TODO: jigsaw - Xbootclaspath may get deprecated/removed
            //and replaced with '-L' or '-m' options
            //see also: http://mail.openjdk.java.net/pipermail/jigsaw-dev/2010-April/000778.html
            cmd.createVmArgument().setLine("-Xbootclasspath/p:" + apiCp);
        }

        cmd.createClasspath(getProject()).append(getClasspath());
        cmd.setClassname("com.sun.tools.ws.WsGen");
        setupWscompileArgs();
        //cmd.createArgument(true).setLine(forkCmd.toString());

    }

    private void setupWscompileArgs() {

        // d option
        if (null != getDestdir() && !getDestdir().getName().equals("")) {
            cmd.createArgument().setValue("-d");
            cmd.createArgument().setFile(getDestdir());
        }

        // g option
        if (getDebug()) {
            cmd.createArgument().setValue("-g");
        }

        // extension flag
        if (getExtension()) {
            cmd.createArgument().setValue("-extension");
        }

        //-Xendorsed option
        if (isXendorsed()) {
            cmd.createArgument().setValue("-Xendorsed");
        }

        // keep option
        if (getKeep()) {
            cmd.createArgument().setValue("-keep");
        }

        //-Xnocompile option
        if (isXnocompile()) {
            cmd.createArgument().setValue("-Xnocompile");
        }

        if (getGenwsdl()) {
            String tmp = "-wsdl";
            if (protocol.length() > 0)
                tmp += ":" + protocol;
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

        // optimize option
        if (getOptimize()) {
            cmd.createArgument().setValue("-O");
        }

        // s option
        if (null != getSourcedestdir() && !getSourcedestdir().getName().equals("")) {
            cmd.createArgument().setValue("-s");
            cmd.createArgument().setFile(getSourcedestdir());
        }

        if (getEncoding() != null) {
            cmd.createArgument().setValue("-encoding");
            cmd.createArgument().setValue(getEncoding());
        }

        // verbose option
        if (getVerbose()) {
            cmd.createArgument().setValue("-verbose");
        }

        if (externalMetadataFiles != null) {
            for (ExternalMetadata file : externalMetadataFiles) {
                cmd.createArgument().setValue("-x");
                cmd.createArgument().setValue(file.file);
            }
        }

        for (String a : javacCmdLine.getArguments()) {
            cmd.createArgument().setValue("-J" + a);
        }

        if (getSei() != null) {
            cmd.createArgument().setValue(getSei());
        }

    }


    /**
     * Called by the project to let the task do it's work *
     */
    public void execute() throws BuildException {
        /* Create an instance of the rmic, redirecting output to
         * the project log
         */
        LogOutputStream logstr = null;
        boolean ok = false;
        try {
            if (fork) {
                setupWscompileForkCommand();
            } else {
                if (cmd.getVmCommand().size() > 1) {
                    log("JVM args ignored when same JVM is used.", Project.MSG_WARN);
                }
                setupWscompileCommand();
            }
            if (fork) {
                if (verbose) {       // Fix for CR 6444561
                    log(ToolVersion.VERSION.BUILD_VERSION);
                    log("command line: " + "wsgen " + cmd.toString());
                }
                int status = run(cmd.getCommandline());
                ok = (status == 0);
            } else {
                if (verbose) {                // Fix for CR 6444561
                    log(ToolVersion.VERSION.BUILD_VERSION);
                    log("command line: " + "wsgen " + cmd.getJavaCommand().toString());
                }
                logstr = new LogOutputStream(this, Project.MSG_WARN);

                ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                try {
                    WsgenTool compTool = new WsgenTool(logstr);
                    ok = compTool.run(cmd.getJavaCommand().getArguments());
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
            if (!ok) {
                if (!verbose) {
                    log("Command invoked: " + "wsgen " + cmd.toString());
                }
                throw new BuildException("wsgen failed", location);
            }
        } catch (Exception ex) {
            if (failonerror) {
                if (ex instanceof BuildException) {
                    throw (BuildException) ex;
                } else {
                    throw new BuildException("Error starting wsgen: " + ex.getMessage(), ex,
                            getLocation());
                }
            } else {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                getProject().log(sw.toString(), Project.MSG_WARN);
                // continue
            }
        } finally {
            try {
                if (logstr != null) {
                    logstr.close();
                }
            } catch (IOException e) {
                throw new BuildException(e);
            }
        }
    }

    /**
     * Executes the given classname with the given arguments in a separate VM.
     */
    private int run(String[] command) throws BuildException {
        Execute exe;
        LogStreamHandler logstr = new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN);
        exe = new Execute(logstr);
        exe.setAntRun(project);
        exe.setCommandline(command);
        try {
            int rc = exe.execute();
            if (exe.killedProcess()) {
                log("Timeout: killed the sub-process", Project.MSG_WARN);
            }
            return rc;
        } catch (IOException e) {
            throw new BuildException(e, location);
        }
    }

    private String getApiClassPath(ClassLoader cl) {
        StringBuilder sb = new StringBuilder();
        URL wsAPI = getResourceFromCP(cl, "javax/xml/ws/EndpointContext.class");
        if (wsAPI != null) {
            sb.append(jarToPath(wsAPI));
            URL jaxbAPI = getResourceFromCP(cl, "javax/xml/bind/JAXBPermission.class");
            if (jaxbAPI != null) {
                String s = jarToPath(jaxbAPI);
                if (sb.indexOf(s) < 0) {
                    sb.append(File.pathSeparator);
                    sb.append(s);
                }
            }
        }
        return sb.length() != 0 ? sb.toString() : null;
    }

    private URL getResourceFromCP(ClassLoader cl, String resource) {
        try {
            Enumeration<URL> res = cl.getResources(resource);
            while (res.hasMoreElements()) {
                URL u = res.nextElement();
                String s = u.toExternalForm();
                if (!s.contains("rt.jar") && !s.contains("classes.jar")) {
                    return u;
                }
            }
        } catch (IOException ex) {
            log(ex.getMessage(), Project.MSG_WARN);
        }
        return null;
    }

    private String jarToPath(URL u) {
        String s = u.toExternalForm();
        s = s.substring(s.lastIndexOf(":") + 1);
        return s.indexOf('!') < 0 ? s : s.substring(0, s.indexOf('!'));
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
