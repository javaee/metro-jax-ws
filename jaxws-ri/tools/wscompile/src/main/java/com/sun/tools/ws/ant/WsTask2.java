/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.tools.ws.ToolVersion;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
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

/**
 *  Base class for WS Tools Ant Task implementations.
 *
 * @author lukas
 */
abstract class WsTask2 extends MatchingTask {
    private Path modulepath = null;
    public void setModulepath(Path mp) {
        this.modulepath = mp;
    }
    public Path getModulepath() {
        return this.modulepath;
    }

    private Path upgrademodulepath = null;
    public void setUpgrademodulepath(Path ump) {
        this.upgrademodulepath = ump;
    }
    public Path getUpgrademodulepath() {
        return this.upgrademodulepath;
    }

    private String addmodules = null;
    public void setAddmodules(String ams) {
        this.addmodules = ams;
    }
    public String getAddmodules() {
        return this.addmodules;
    }

    private String limitmodules = null;
    public void setLimitmodules(String lms) {
        this.limitmodules = lms;
    }
    public String getLimitmodules() {
        return this.limitmodules;
    }

    private String addreads = null;
    public void setAddreads(String ars) {
        this.addreads = ars;
    }
    public String getAddreads() {
        return this.addreads;
    }

    private String addexports = null;
    public void setAddexports(String aes) {
        this.addexports = aes;
    }
    public String getAddexports() {
        return this.addexports;
    }

    private String patchmodule = null;
    public void setPatchmodule(String pms) {
        this.patchmodule = pms;
    }
    public String getPatchmodule() {
        return this.patchmodule;
    }

    private String addopens = null;
    public void setAddopens(String aos) {
        this.addopens = aos;
    }
    public String getAddopens() {
        return this.addopens;
    }

    private final CommandlineJava cmd = new CommandlineJava();

    CommandlineJava getCommandline() {
        return cmd;
    }

    /* *********************** -d option ************************ */
    private File destDir = null;

    /**
     * Gets the base directory to output generated class.
     *
     * @return destination directory for generated class(es).
     */
    public File getDestdir() {
        return this.destDir;
    }

    /**
     * Sets the base directory to output generated class.
     *
     * @param base destination directory for generated class(es).
     */
    public void setDestdir(File base) {
        this.destDir = base;
    }


    /* ****************** -extensions option ********************* */
    private boolean extension;

    /**
     * Gets the "extension" flag.
     *
     * @return true if extension mode is on, false otherwise.
     */
    public boolean getExtension() {
        return extension;
    }

    /**
     * Sets the "extension" flag.
     *
     * @param extension true to set extension mode on, false otherwise.
     */
    public void setExtension(boolean extension) {
        this.extension = extension;
    }


    /* *********************** -keep option ************************ */
    private boolean keep = false;

    /**
     * Gets the "keep" flag.
     *
     * @return Whether to keep generated sources.
     */
    public boolean getKeep() {
        return keep;
    }

    /**
     * Sets the "keep" flag.
     *
     * @param keep keep generated sources.
     */
    public void setKeep(boolean keep) {
        this.keep = keep;
    }


    /* *********************** -fork option ************************ */
    private boolean fork = false;

    /**
     * Gets the "fork" flag.
     *
     * @return true if execution should be done in forked JVM, false otherwise.
     */
    public boolean getFork() {
        return fork;
    }

    /**
     * Sets the "fork" flag.
     *
     * @param fork true to run execution in a forked JVM.
     */
    public void setFork(boolean fork) {
        this.fork = fork;
    }


    /* *********************** -s option ************************ */
    private File sourcedestdir;

    /**
     * Gets the directory to place generated source java files.
     *
     * @return destination directory for generated source(s).
     */
    public File getSourcedestdir() {
        return sourcedestdir;
    }

    /**
     * Sets the directory to place generated source java files.
     *
     * @param sourceBase destination directory for generated source(s).
     */
    public void setSourcedestdir(File sourceBase) {
        keep = true;
        this.sourcedestdir = sourceBase;
    }


    /* *********************** -encoding option ************************ */
    private String encoding;

    /**
     * Sets the encoding for generated source java files.
     *
     * @param encoding encoding to use in generated sources.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Gets the encoding for generated source java files.
     *
     * @return encoding to use in generated sources.
     */
    public String getEncoding() {
        return encoding;
    }


    /* *********************** -verbose option ************************ */
    private boolean verbose = false;

    /**
     * Gets the "verbose" flag.
     *
     * @return true if messages about what the compiler is doing should be
     * printed out.
     */
    public boolean getVerbose() {
        return verbose;
    }

    /**
     * Sets the "verbose" flag.
     *
     * @param verbose whether to output messages about what the compiler is
     * doing.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }


    /* *********************** -g option ************************ */
    private boolean debug = false;

    /**
     * Gets the debug flag.
     *
     * @return  true if all debugging info should be generated.
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Sets the debug flag.
     *
     * @param debug generate all debugging info.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }


    /* *********************** -J option ************************ */
    /**
     * Additional command line arguments for Javac. The equivalent of the -J
     * option.
     */
    private final Commandline javacCmdLine = new Commandline();

    /**
     * Adds Javac argument.
     *
     * @return Javac argument created.
     * @since 2.2.9
     */
    public Commandline.Argument createJavacarg() {
        return javacCmdLine.createArgument();
    }

    public Commandline getJavacargs() {
        return javacCmdLine;
    }


    /* *********************** -Xendorsed option ************************ */
    /**
     * Set to true to perform the endorsed directory override so that Ant tasks
     * can run on JavaSE 6. This is used only when fork is true. With fork=false
     * which is default, it is handled way before in the WrapperTask.
     */
    private boolean xendorsed = false;

    public void setXendorsed(boolean xendorsed) {
        this.xendorsed = xendorsed;
    }

    public boolean isXendorsed() {
        return xendorsed;
    }


    /* *********************** -Xnocompile option ************************ */
    /**
     * do not compile generated classes *
     */
    private boolean xnocompile = false;

    public boolean isXnocompile() {
        return xnocompile;
    }

    public void setXnocompile(boolean xnocompile) {
        this.xnocompile = xnocompile;
    }


    /* ******************* failonerror option  ********************** */
    /**
     * False to continue the build even if the compilation fails.
     */
    private boolean failonerror = true;

    /**
     * Mostly for our SQE teams and not to be advertised.
     *
     * @param value a boolean value
     */
    public void setFailonerror(boolean value) {
        failonerror = value;
    }

    /**
     *
     * @return true if the task should fail on error.
     */
    public boolean isFailonerror() {
        return failonerror;
    }


    /* ********************* include ant runtime ********************* */
    /**
     * not sure if these methods are needed
     */
    private boolean includeAntRuntime = false;

    /**
     * Include ant's own classpath in this task's classpath?
     *
     * @param include a boolean value.
     */
    public void setIncludeantruntime(boolean include) {
        includeAntRuntime = include;
    }

    /**
     * Gets whether or not the ant classpath is to be included in the task's
     * classpath.
     *
     * @return true if Ant classpath should be included in the task's classpath.
     */
    public boolean getIncludeantruntime() {
        return includeAntRuntime;
    }


    /* ********************* include java runtime ********************* */
    /**
     * not sure if these methods are needed
     */
    private boolean includeJavaRuntime = false;

    /**
     * Sets whether or not to include the java runtime libraries to this task's
     * classpath.
     *
     * @param include a boolean value.
     */
    public void setIncludejavaruntime(boolean include) {
        includeJavaRuntime = include;
    }

    /**
     * Gets whether or not the java runtime should be included in this task's
     * classpath.
     *
     * @return true if java runtime classpath should be included in the task's
     * classpath.
     */
    public boolean getIncludejavaruntime() {
        return includeJavaRuntime;
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
     * Let the task do it's work.
     *
     * @param tool string to use in logged messages
     * @param toolClass class name to invoke
     * @throws BuildException if invocation fails
     */
    protected final void execute(String tool, String toolClass) throws BuildException {
        setupCommand();
        LogOutputStream logstr = null;
        boolean ok = false;
        try {
            if (getVerbose()) {
                log(ToolVersion.VERSION.BUILD_VERSION);
                log("command line: " + tool
                        + (getFork() ? getCommandline().getJavaCommand() : getCommandline()).toString());
            }
            if (getFork()) {
                setupForkCommand(toolClass);
                int status = run(getCommandline().getCommandline());
                ok = (status == 0);
            } else {
                if (getCommandline().getVmCommand().size() > 1) {
                    log("JVM args ignored when same JVM is used.", Project.MSG_WARN);
                }
                logstr = new LogOutputStream(this, Project.MSG_WARN);

                ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                try {
                    ok = runInVm(getCommandline().getJavaCommand().getArguments(), logstr);
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
            if (!ok) {
                if (!getVerbose()) {
                    log("Command invoked: " + tool + getCommandline().toString());
                }
                throw new BuildException(tool + " failed", getLocation());
            }
        } catch (Exception ex) {
            if (isFailonerror()) {
                if (ex instanceof BuildException) {
                    throw (BuildException) ex;
                } else {
                    throw new BuildException("Error starting " + tool + ": " + ex.getMessage(), ex,
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
     * Set up command line to invoke.
     * 
     * @return ready to run command line
     */
    protected CommandlineJava setupCommand() {
        // d option
        if (null != getDestdir() && !getDestdir().getName().equals("")) {
            getCommandline().createArgument().setValue("-d");
            getCommandline().createArgument().setFile(getDestdir());
        }
        // extension flag
        if (getExtension()) {
            getCommandline().createArgument().setValue("-extension");
        }
        //-Xendorsed option
        /* TODO JDK9 */
        if (isXendorsed()) {
            getCommandline().createArgument().setValue("-Xendorsed");
        }
        // keep option
        if (getKeep()) {
            getCommandline().createArgument().setValue("-keep");
        }
        // s option
        if (null != getSourcedestdir() && !getSourcedestdir().getName().equals("")) {
            getCommandline().createArgument().setValue("-s");
            getCommandline().createArgument().setFile(getSourcedestdir());
        }
        // encoding option
        if (getEncoding() != null) {
            getCommandline().createArgument().setValue("-encoding");
            getCommandline().createArgument().setValue(getEncoding());
        }
        // g option
        if (getDebug()) {
            getCommandline().createArgument().setValue("-g");
        }
        // verbose option
        if (getVerbose()) {
            getCommandline().createArgument().setValue("-verbose");
        }
        return getCommandline();
    }

    /**
     * Used to call the tool directly using API.
     *
     * @param arguments arguments to be passed to the tool
     * @param out output for the tool
     * @return true if tool succeed
     */
    protected abstract boolean runInVm(String[] arguments, OutputStream out);

    void setupForkCommand(String className) {
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
        getCommandline().createClasspath(getProject()).append(new Path(getProject(), antcp));
        /*
        * TODO JDK9 */
        String apiCp = getApiClassPath(this.getClass().getClassLoader());
        if (apiCp != null) {
            //TODO: jigsaw - Xbootclaspath may get deprecated/removed
            //and replaced with '-L' or '-m' options
            //see also: http://mail.openjdk.java.net/pipermail/jigsaw-dev/2010-April/000778.html
            getCommandline().createVmArgument().setLine("-Xbootclasspath/p:" + apiCp);
        }
        getCommandline().setClassname(className);
    }

    /**
     * Executes the given class name with the given arguments in a separate VM.
     *
     * @param command arguments.
     * @return return value from the executed process.
     */
    private int run(String[] command) throws BuildException {
        Execute exe;
        LogStreamHandler logstr = new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN);
        exe = new Execute(logstr);
        exe.setAntRun(getProject());
        exe.setCommandline(command);
        try {
            int rc = exe.execute();
            if (exe.killedProcess()) {
                log("Timeout: killed the sub-process", Project.MSG_WARN);
            }
            return rc;
        } catch (IOException e) {
            throw new BuildException(e, getLocation());
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
        s = s.substring(s.lastIndexOf(':') + 1);
        return s.indexOf('!') < 0 ? s : s.substring(0, s.indexOf('!'));
    }
}
