/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import com.sun.tools.ws.wscompile.Options;
import com.sun.tools.ws.wscompile.WsimportTool;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.XMLCatalog;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * wscompile task for use with the JAXWS project.
 *
 */
public class WsImport2 extends MatchingTask {

    private final CommandlineJava cmd = new CommandlineJava();
    /** -d option. */
    private File destDir = null;

    /** Additional command line arguments for XJC. The equivalent of the -B option. */
    private final Commandline xjcCmdLine = new Commandline();

    /** Enable/disable debug messages - stack trace **/
    private boolean xdebug = false;

    public boolean isXdebug() {
        return xdebug;
    }


    public void setXdebug(boolean xdebug) {
        this.xdebug = xdebug;
    }


    public boolean isXnocompile() {
        return xnocompile;
    }

    public void setXnocompile(boolean xnocompile) {
        this.xnocompile = xnocompile;
    }

    /** do not compile generated classes **/
    private boolean xnocompile = false;

    /**
     * Provides a way to directly pass wsimport commandline options through nested arg
     * instead of a separate attribute for each option.
     * @return
     */
    public Commandline.Argument createArg() {
        return cmd.createArgument();
    }

    /**
     * -XadditionalHeaders - maps headers not bound to req/resp messages to Java parameters
     */
    private boolean xadditionalHeaders = false;

    public boolean isXadditionalHeaders() {
        return xadditionalHeaders;
    }

    public void setXadditionalHeaders(boolean xadditionalHeaders) {
        this.xadditionalHeaders = xadditionalHeaders;
    }

    /** Gets the base directory to output generated class. **/
    public File getDestdir() {
        return this.destDir;
    }

    /** Sets the base directory to output generated class. **/
    public void setDestdir(File base) {
        this.destDir = base;
    }

    /** wsdllocation - set @WebService.wsdlLocation and @WebServiceClient.wsdlLocation values */

    private String wsdlLocation;

    public String getWsdllocation() {
        return wsdlLocation;
    }

    public void setWsdllocation(String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }

    public void addConfiguredXMLCatalog(XMLCatalog entityResolver) {
        if(this.xmlCatalog==null){
            this.xmlCatalog = new XMLCatalog();
            xmlCatalog.setProject(getProject());
        }
        this.xmlCatalog.addConfiguredXMLCatalog(entityResolver);
    }

    private XMLCatalog xmlCatalog;

    private String pkg;
    public void setPackage(String pkg){
        this.pkg = pkg;
    }

    public String getPackage(){
        return pkg;
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
     * Adds XJC argument.
     *
     * @since 2.1
     */
    public Commandline.Argument createXjcarg() {
        return xjcCmdLine.createArgument();
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

    /********************  -extensions option **********************/
    protected boolean extension;

    /** Gets the "extension" flag. **/
    public boolean getExtension() {
        return extension;
    }

    /** Sets the "extension" flag. **/
    public void setExtension(boolean extension) {
        this.extension = extension;
    }

    public boolean getxNoAddressingDatabinding() {
        return xNoAddressingDatabinding;
    }

    public void setxNoAddressingDatabinding(boolean xNoAddressingDatabinding) {
        this.xNoAddressingDatabinding = xNoAddressingDatabinding;
    }

    /**** -Xno-addressing-databinding ***/
    protected boolean xNoAddressingDatabinding;



    /*************************  -keep option *************************/
    private boolean keep = false;

    /** Gets the "keep" flag. **/
    public boolean getKeep() {
        return keep;
    }

    /** Sets the "keep" flag. **/
    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    /** -quiet switch **/
    private boolean quiet = false;


    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    /**
     * Sets the target version of the compilation
     */
    private String target;
    public void setTarget( String version ) {
        Options.Target targetVersion = Options.Target.parse(version);
        if(targetVersion==null)
            throw new BuildException(version+" is not a valid version number");
        target = targetVersion.getVersion();
    }




    /*************************  -fork option *************************/
    private boolean fork = false;

    /** Gets the "fork" flag. **/
    public boolean getFork() {
        return fork;
    }

    /** Sets the "fork" flag. **/
    public void setFork(boolean fork) {
        this.fork = fork;
    }

    /*************************  -O option *************************/
    private boolean optimize = false;

    /** Gets the optimize flag. **/
    public boolean getOptimize() {
        return optimize;
    }

    /** Sets the optimize flag. **/
    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    /*************************  -s option *************************/
    private File sourcedestdir;

    /** Sets the directory to place generated source java files. **/
    public void setSourcedestdir(File sourceBase) {
        keep = true;
        this.sourcedestdir = sourceBase;
    }

    /** Gets the directory to place generated source java files. **/
    public File getSourcedestdir() {
        return sourcedestdir;
    }

    /*************************  -verbose option *************************/
    protected boolean verbose = false;

    /** Gets the "verbose" flag. **/
    public boolean getVerbose() {
        return verbose;
    }

    /** Sets the "verbose" flag. **/
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

     /*************************  -g option *************************/
     private boolean debug = false;

     /** Gets the debug flag. **/
     public boolean getDebug() {
         return debug;
     }

     /** Sets the debug flag. **/
     public void setDebug(boolean debug) {
         this.debug = debug;
     }

    /***********************  include ant runtime **********************/
    /** not sure if these methods are needed */
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
    /** not sure if these methods are needed */
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

    /**
     * Files used to determine whether XJC should run or not.
     */
    private final ArrayList<File> dependsSet = new ArrayList<File>();
    private final ArrayList<File> producesSet = new ArrayList<File>();

    /**
     * Set to true once the &lt;produces> element is used.
     * This flag is used to issue a suggestion to users.
     */
    private boolean producesSpecified = false;

    /** Nested &lt;depends> element. */
    public void addConfiguredDepends( FileSet fs ) {
        addIndividualFilesTo( fs, dependsSet );
    }

    /** Nested &lt;produces> element. */
    public void addConfiguredProduces( FileSet fs ) {
        producesSpecified = true;
        if( !fs.getDir(getProject()).exists() ) {
            log(
                fs.getDir(getProject()).getAbsolutePath()+" is not found and thus excluded from the dependency check",
                Project.MSG_INFO );
        } else
            addIndividualFilesTo( fs, producesSet );
    }

    /**
     * Extracts {@link File} objects that the given {@link FileSet}
     * represents and adds them all to the given {@link java.util.List}.
     */
    private void addIndividualFilesTo( FileSet fs, List<File> lst ) {
        DirectoryScanner ds = fs.getDirectoryScanner(getProject());
        String[] includedFiles = ds.getIncludedFiles();
        File baseDir = ds.getBasedir();

        for (String value : includedFiles) {
            lst.add(new File(baseDir, value));
        }
    }

    /**
     * Determines the timestamp of the newest/oldest file in the given set.
     */
    private long computeTimestampFor( List<File> files, boolean findNewest ) {

        long lastModified = findNewest?Long.MIN_VALUE:Long.MAX_VALUE;

        for( File file : files ) {
            log("Checking timestamp of "+file.toString(), Project.MSG_VERBOSE );

            if( findNewest )
                lastModified = Math.max( lastModified, file.lastModified() );
            else
                lastModified = Math.min( lastModified, file.lastModified() );
        }

        if( lastModified == Long.MIN_VALUE ) // no file was found
            return Long.MAX_VALUE;  // force re-run

        if( lastModified == Long.MAX_VALUE ) // no file was found
            return Long.MIN_VALUE;  // force re-run

        return lastModified;
    }

    /**
     * @param binding The external binding to set.
     */
    public void setBinding(String binding) {
        File f = new File(binding);
        bindingFiles.add(f);
        dependsSet.add(f);
    }

    /**
     * Adds a new catalog file.
     */
    public void setCatalog( File catalog ) {
        this.catalog = catalog;
    }

    public File getCatalog(){
        return catalog;
    }

    private File catalog;



    private String wsdl;
    /**
     * @return Returns the wsdl.
     */
    public String getWsdl() {
        return wsdl;
    }
    /**
     * @param wsdl The wsdl to set.
     */
    public void setWsdl(String wsdl) {
        this.wsdl = wsdl;
        dependsSet.add(new File(wsdl));
    }


    /**
     * -Xauth
     */
    private File xauthfile;

    public File getXauthfile() {
        return xauthfile;
    }

    public void setXauthfile(File xauthfile) {
        this.xauthfile = xauthfile;
    }

    public void addConfiguredBinding( FileSet fs ) {
        DirectoryScanner ds = fs.getDirectoryScanner(getProject());
        String[] includedFiles = ds.getIncludedFiles();
        File baseDir = ds.getBasedir();
        for (String includedFile : includedFiles) {
            bindingFiles.add(new File(baseDir, includedFile));
        }
        addIndividualFilesTo( fs, dependsSet );
    }

    private void setupWsimportForkCommand() {
        ClassLoader loader = this.getClass().getClassLoader();
        while(loader!=null && !(loader instanceof AntClassLoader)) {
            loader = loader.getParent();
        }

        if(loader!=null) {
            Path classpath = new Path(project, ((AntClassLoader)loader).getClasspath());
            cmd.createClasspath(getProject()).append(classpath);
        }
        cmd.setClassname("com.sun.tools.ws.WsImport");
        //setupWsimportArgs();
        //cmd.createArgument(true).setLine(forkCmd.toString());
    }

    private void setupWsimportArgs() {
        // d option
        if (null != getDestdir() && !getDestdir().getName().equals("")) {
            cmd.createArgument().setValue("-d");
            cmd.createArgument().setFile(getDestdir());
        }

        // extension flag
        if (getExtension()) {
            cmd.createArgument().setValue("-extension");
        }

        if(getxNoAddressingDatabinding()){
            cmd.createArgument().setValue("-Xno-addressing-databinding");
        }

        // g option
        if (getDebug()) {
            cmd.createArgument().setValue("-g");
        }

        if(isXdebug()){
            cmd.createArgument().setValue("-Xdebug");
        }

        if(isXnocompile()){
            cmd.createArgument().setValue("-Xnocompile");
        }

        if(isXadditionalHeaders()){
            cmd.createArgument().setValue("-XadditionalHeaders");
        }

        // keep option
        if (getKeep()) {
            cmd.createArgument().setValue("-keep");
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

        //catalog
        if((getCatalog() != null) && (getCatalog().getName().length() > 0)){
            cmd.createArgument().setValue("-catalog");
            cmd.createArgument().setFile(getCatalog());
        }

        // verbose option
        if (getVerbose()) {
            cmd.createArgument().setValue("-verbose");
        }

        if(quiet){
            cmd.createArgument().setValue("-quiet");
        }

        if(target != null){
            cmd.createArgument().setValue("-target");
            cmd.createArgument().setValue(target);
        }

        //wsdl
        if(getWsdl() != null){
            cmd.createArgument().setValue(getWsdl());
        }

        if(getXauthfile() != null){
            cmd.createArgument().setValue("-Xauthfile");
            cmd.createArgument().setFile(getXauthfile());
        }

        //package
        if((getPackage() != null) && (getPackage().length() > 0)){
            cmd.createArgument().setValue("-p");
            cmd.createArgument().setValue(getPackage());
        }

        for( String a : xjcCmdLine.getArguments() ) {
            if(a.startsWith("-")) {
                cmd.createArgument().setValue("-B"+a);
            } else {
                cmd.createArgument().setValue(a);
            }
        }
        
        if(!bindingFiles.isEmpty()){
            for(File binding : bindingFiles){
                cmd.createArgument().setValue("-b");
                cmd.createArgument().setFile(binding);
            }
        }

        if((wsdlLocation != null) && (wsdlLocation.length() != 0)){
            cmd.createArgument().setValue("-wsdllocation");
            cmd.createArgument().setValue(wsdlLocation);
        }         
    }


    /** Called by the project to let the task do it's work **/
    public void execute() throws BuildException {
        /* Create an instance of the rmic, redirecting output to
         * the project log
         */
        LogOutputStream logstr = null;
        boolean ok = false;
        try {
            if( !producesSpecified ) {
                log("Consider using <depends>/<produces> so that wsimport won't do unnecessary compilation",Project.MSG_INFO);
            }

            // up to date check
            long srcTime = computeTimestampFor(dependsSet,true);
            long dstTime = computeTimestampFor(producesSet,false);
            log("the last modified time of the inputs is  "+srcTime, Project.MSG_VERBOSE);
            log("the last modified time of the outputs is "+dstTime, Project.MSG_VERBOSE);

            if( srcTime < dstTime ) {
                log("files are up to date");
                return;
            }


            if(fork){
                setupWsimportForkCommand();
            } else {
                if (cmd.getVmCommand().size() > 1) {
                    log("JVM args ignored when same JVM is used.",Project.MSG_WARN);
                }
            }
            setupWsimportArgs();
            if (fork) {
                if (verbose) {
                    log("command line: "+"wsimport "+cmd.toString());
                }
                int status = run(cmd.getCommandline());
                ok = (status == 0);
            } else {
                if (verbose) {
                    log("command line: "+"wsimport "+cmd.getJavaCommand().toString());
                }
                logstr = new LogOutputStream(this, Project.MSG_WARN);

                ClassLoader old = Thread.currentThread().getContextClassLoader();
                ClassLoader loader = this.getClass().getClassLoader();
                Thread.currentThread().setContextClassLoader(loader);
                String sysPath = System.getProperty("java.class.path");
                try {
                    WsimportTool compTool = new WsimportTool(logstr);
                    if(xmlCatalog != null){
                        compTool.setEntityResolver(xmlCatalog);
                    }
                    if (loader instanceof AntClassLoader) {
                        System.setProperty("java.class.path", ((AntClassLoader)loader).getClasspath());
                    }
                    ok = compTool.run(cmd.getJavaCommand().getArguments());
                } finally {
                    if (sysPath != null) {
                        System.setProperty("java.class.path", sysPath);
                    }
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
            if (!ok) {
                if (!verbose) {
                    log("Command invoked: "+"wsimport "+cmd.toString());
                }
                throw new BuildException("wsimport failed", location);
            }
        } catch (Exception ex) {
            if (failonerror) {
                if (ex instanceof BuildException) {
                    throw (BuildException) ex;
                } else {
                    throw new BuildException("Error starting wsimport: ", ex,
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
        LogStreamHandler logstr = new LogStreamHandler(this,
            Project.MSG_INFO, Project.MSG_WARN);
        Execute exe = new Execute(logstr);
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

    private Set<File> bindingFiles = new HashSet<File>();

}
