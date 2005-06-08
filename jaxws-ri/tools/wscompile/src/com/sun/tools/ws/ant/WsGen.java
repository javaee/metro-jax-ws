/**
 * $Id: WsGen.java,v 1.3 2005-06-08 06:13:20 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

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
import org.apache.tools.ant.types.Reference;

import com.sun.tools.ws.wscompile.CompileTool;
import com.sun.xml.ws.util.VersionUtil;

/**
 * wscompile task for use with the JAXRPC project.
 *
 */
public class WsGen extends MatchingTask {

    /*************************  -classpath option *************************/
    protected Path compileClasspath = null;

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
            compileClasspath = new Path(project);
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

    /*************************  -d option *************************/
    private File baseDir = null;

    /** Gets the base directory to output generated class. **/
    public File getBase() {
        return this.baseDir;
    }

    /** Sets the base directory to output generated class. **/
    public void setBase(File base) {
        this.baseDir = base;
    }

    /********************  -jvmargs option **********************/
    protected String jvmargs;

    /** Gets the Java VM options. **/
    public String getJvmargs() {
        return jvmargs;
    }

    /** Sets the Java VM options. **/
    public void setJvmargs(String jvmargs) {
        this.jvmargs = jvmargs;
    }

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

    /*************************  -fork option *************************/
    private boolean fork = false;

    /** Gets the "keep" flag. **/
    public boolean getFork() {
        return fork;
    }

    /** Sets the "fork" flag. **/
    public void setFork(boolean fork) {
        this.fork = fork;
    }

    /*************************  -nd option *************************/
    private File nonClassDir = null;

    /** Gets the directory for non-class generated files. **/
    public File getNonClassDir() {
        return this.nonClassDir;
    }

    /** Sets the directory for non-class generated files. **/
    public void setNonClassDir(File nonClassDir) {
        this.nonClassDir = nonClassDir;
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
    private File sourceBase;

    /** Sets the directory to place generated source java files. **/
    public void setSourceBase(File sourceBase) {
        keep = true;
        this.sourceBase = sourceBase;
    }

    /** Gets the directory to place generated source java files. **/
    public File getSourceBase() {
        return sourceBase;
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

    /*************************  -version option *************************/
    protected boolean version = false;

    /** Gets the "version" flag. **/
    public boolean getVersion() {
        return version;
    }

    /** Sets the "version" flag. **/
    public void setVersion(boolean version) {
        this.version = version;
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
     
     /*************************  -wsdl option *************************/
     private boolean genWsdl = false;

     /** Gets the genWsdl flag. **/
     public boolean getgenWsdl() {
         return genWsdl;
     }

     /** Sets the genWsdl flag. **/
     public void setgenWsdl(boolean genWsdl) {
         this.genWsdl = genWsdl;
     }
     
     /*************  protocol option used only with -wsdl option*****************/
     private String protocol = "";

     /** Gets the protocol. **/
     public String getProtocol() {
         return protocol;
     }

     /** Sets the protocol. **/
     public void setProtocol(String protocol) {
         this.protocol = protocol;
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

    private String endpointImplementationClass;
    /**
     * @return Returns the endpointImplementationClass.
     */
    public String getEndpointImplementationClass() {
        return endpointImplementationClass;
    }
    public void setEndpointImplementationClass(String endpointImplementationClass) {
        this.endpointImplementationClass = endpointImplementationClass;
    }

    private Commandline setupWscompileCommand() {
        Commandline cmd = setupWscompileArgs();

        Path classpath = getClasspath();

        if (classpath != null && !classpath.toString().equals("")) {
            cmd.createArgument().setValue("-classpath");
            cmd.createArgument().setPath(classpath);
        }
        return cmd;
    }

    private Commandline setupWscompileForkCommand() {
        CommandlineJava forkCmd = new CommandlineJava();

        Path classpath = getClasspath();
        forkCmd.createClasspath(getProject()).append(classpath);
        forkCmd.setClassname("com.sun.tools.ws.WsGen");
        if (null != getJvmargs()) {
            forkCmd.createVmArgument().setLine(getJvmargs());
        }

        Commandline cmd = setupWscompileArgs();
        cmd.createArgument(true).setLine(forkCmd.toString());
        return cmd;
    }

    private Commandline setupWscompileArgs() {
        Commandline cmd = new Commandline();

        // d option
        if (null != getBase() && !getBase().equals("")) {
            cmd.createArgument().setValue("-d");
            cmd.createArgument().setFile(getBase());
        }

        // g option
        if (getDebug()) {
            cmd.createArgument().setValue("-g");
        }

        // keep option
        if (getKeep()) {
            cmd.createArgument().setValue("-keep");
        }

        if (getgenWsdl()) {
            String tmp = "-wsdl";
            if (protocol.length() > 0)
                tmp += ":"+protocol;
            cmd.createArgument().setValue(tmp);
        }
        
        // nd option
        if (null != getNonClassDir() && !getNonClassDir().equals("")) {
            cmd.createArgument().setValue("-nd");
            cmd.createArgument().setFile(getNonClassDir());
        }

        // optimize option
        if (getOptimize()) {
            cmd.createArgument().setValue("-O");
        }

        // s option
        if (null != getSourceBase() && !getSourceBase().equals("")) {
            cmd.createArgument().setValue("-s");
            cmd.createArgument().setFile(getSourceBase());
        }

        // verbose option
        if (getVerbose()) {
            cmd.createArgument().setValue("-verbose");
        }

        // version option
        if (getVersion()) {
            cmd.createArgument().setValue("-version");
        }

        if (getEndpointImplementationClass() != null) {
            cmd.createArgument().setValue(getEndpointImplementationClass());
        }

        return cmd;
    }


    /** Called by the project to let the task do it's work **/
    public void execute() throws BuildException {
        /* Create an instance of the rmic, redirecting output to
         * the project log
         */
        LogOutputStream logstr = null;
        boolean ok = false;
        try {
            Commandline cmd = fork ?
                setupWscompileForkCommand() : setupWscompileCommand();
            if (verbose) {
                log("command line: "+"wsgen "+cmd.toString());
            }
            if (fork) {
                int status = run(cmd.getCommandline());
                ok = (status == 0) ? true : false;
            } else {
                logstr = new LogOutputStream(this, Project.MSG_WARN);

                ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                try {
                    CompileTool compTool = new CompileTool(logstr, "wsgen");
                    ok = compTool.run(cmd.getArguments());
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
            if (!ok) {
                if (!verbose) {
                    log("Command invoked: "+"wsgen "+cmd.toString());
                }
                throw new BuildException("wsgen failed", location);
            }
        } catch (Exception ex) {
            if (ex instanceof BuildException) {
                throw (BuildException)ex;
            } else {
                throw new BuildException("Error starting wsgen: ", ex,
                getLocation());
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
        FileOutputStream fos = null;
        Execute exe = null;
        LogStreamHandler logstr = new LogStreamHandler(this,
            Project.MSG_INFO, Project.MSG_WARN);
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
}
