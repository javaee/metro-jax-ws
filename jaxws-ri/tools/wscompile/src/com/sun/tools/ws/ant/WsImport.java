/**
 * $Id: WsImport.java,v 1.7 2005-08-12 21:34:28 kohlert Exp $
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
import org.apache.tools.ant.AntClassLoader;

/**
 * wscompile task for use with the JAXWS project.
 *
 */
public class WsImport extends MatchingTask {

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

    /********************  -httpproxy option **********************/
    private String HTTPProxyURL = null;
    private URL proxyURL = null;

    /** Gets the String "httpproxy" flag. **/
    public String getHTTPProxy() {
        return HTTPProxyURL;
    }

    /** Sets the String "httpproxy" flag.
     * This value can either specify the http protocol or not.
     **/
    public void setHTTPProxy(String HTTPProxy) {
        if (HTTPProxy != null && !HTTPProxy.equals("")) {
            if (HTTPProxy.startsWith("http://")) {
                this.HTTPProxyURL = HTTPProxy;
            } else {
                this.HTTPProxyURL = "http://" + HTTPProxy;
            }

            try {
                URL proxyServer = new URL(this.HTTPProxyURL);
                setProxyServer(proxyServer);
            } catch (MalformedURLException e) {
                throw new Error("Invalid HTTP URL specified: " +
                    this.HTTPProxyURL);
            }
        }
    }

    /** Gets the URL for "httpproxy" flag. **/
    public URL getProxyServer() {
        return proxyURL;
    }

    /** Sets the URL for "httpproxy" flag. **/
    public void setProxyServer(URL proxyURL) {
        this.proxyURL = proxyURL;
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

    /** Gets the "fork" flag. **/
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

    private String binding;
    /**
     * @return Returns the binding.
     */
    public String getBinding() {
        return binding;
    }
    /**
     * @param binding The binding to set.
     */
    public void setBinding(String binding) {
        this.binding = binding;
    }

    private String wsdlFile;
    /**
     * @return Returns the wsdlFile.
     */
    public String getWsdlFile() {
        return wsdlFile;
    }
    /**
     * @param wsdlFile The wsdlFile to set.
     */
    public void setWsdlFile(String wsdlFile) {
        this.wsdlFile = wsdlFile;
    }

    public void addConfiguredBinding( FileSet fs ) {
        DirectoryScanner ds = fs.getDirectoryScanner(project);
        String[] includedFiles = ds.getIncludedFiles();
        File baseDir = ds.getBasedir();
        for (int i = 0; i < includedFiles.length; ++i) {
            bindingFiles.add(new File(baseDir, includedFiles[i]));
        }
    }

    private Commandline setupWsimportCommand() {
        Commandline cmd = setupWsimportArgs();
        return cmd;
    }

    private Commandline setupWsimportForkCommand() {
        CommandlineJava forkCmd = new CommandlineJava();
        ClassLoader loader = this.getClass().getClassLoader();

        Path classpath = new Path(project);
        if (loader instanceof AntClassLoader) {
           classpath = new Path(project, ((AntClassLoader)loader).getClasspath());
        }
        forkCmd.createClasspath(getProject()).append(classpath);
        forkCmd.setClassname("com.sun.tools.ws.WsImport");
        if (null != getJvmargs()) {
            forkCmd.createVmArgument().setLine(getJvmargs());
        }

        Commandline cmd = setupWsimportArgs();
        cmd.createArgument(true).setLine(forkCmd.toString());
        return cmd;
    }

    private Commandline setupWsimportArgs() {
        Commandline cmd = new Commandline();

        // d option
        if (null != getBase() && !getBase().getName().equals("")) {
            cmd.createArgument().setValue("-d");
            cmd.createArgument().setFile(getBase());
        }

        // extension flag
        if (getExtension()) {
            cmd.createArgument().setValue("-extension");
        }
        
        // g option
        if (getDebug()) {
            cmd.createArgument().setValue("-g");
        }

        // httpproxy option
        if (getProxyServer() != null) {
            String host = getProxyServer().getHost();
            if (host != null && !host.equals("")) {
                String proxyVal = "-httpproxy:" + host;
                if (getProxyServer().getPort() != -1) {
                    proxyVal += ":" + getProxyServer().getPort();
                }

                cmd.createArgument().setValue(proxyVal);
            }
        }

        // keep option
        if (getKeep()) {
            cmd.createArgument().setValue("-keep");
        }

        // nd option
        if (null != getNonClassDir() && !getNonClassDir().getName().equals("")) {
            cmd.createArgument().setValue("-nd");
            cmd.createArgument().setFile(getNonClassDir());
        }

        // optimize option
        if (getOptimize()) {
            cmd.createArgument().setValue("-O");
        }

        // s option
        if (null != getSourceBase() && !getSourceBase().getName().equals("")) {
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

        //wsdlFile
        if(getWsdlFile() != null){
            cmd.createArgument().setValue(getWsdlFile());
        }

        if(getBinding() != null){
            cmd.createArgument().setValue("-b");
            cmd.createArgument().setFile(new File (getBinding()));
        }

        if(!bindingFiles.isEmpty()){
            for(File binding : bindingFiles){
                cmd.createArgument().setValue("-b");
                cmd.createArgument().setFile(binding);
            }
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
                setupWsimportForkCommand() : setupWsimportCommand();
            if (verbose) {
                log("command line: "+"wsimport "+cmd.toString());
            }
            if (fork) {
                int status = run(cmd.getCommandline());
                ok = (status == 0) ? true : false;
            } else {
                logstr = new LogOutputStream(this, Project.MSG_WARN);

                ClassLoader old = Thread.currentThread().getContextClassLoader();
                ClassLoader loader = this.getClass().getClassLoader();
                Thread.currentThread().setContextClassLoader(loader);
                String sysPath = System.getProperty("java.class.path");
                try {
                    CompileTool compTool = new CompileTool(logstr, "wsimport");
                    if (loader instanceof AntClassLoader) {
                        System.setProperty("java.class.path", ((AntClassLoader)loader).getClasspath());
                    }
                    ok = compTool.run(cmd.getArguments());
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
            if (ex instanceof BuildException) {
                throw (BuildException)ex;
            } else {
                throw new BuildException("Error starting wsimport: ", ex,
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

    private Set<File> bindingFiles = new HashSet<File>();

}
