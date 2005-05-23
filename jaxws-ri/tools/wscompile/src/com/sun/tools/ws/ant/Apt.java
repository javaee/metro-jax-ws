/**
 * $Id: Apt.java,v 1.1 2005-05-23 23:11:23 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.ant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * apt task for use with the JAXRPC project.
 */
public class Apt extends Task {
    
    /** -classpath option */
    protected Path compileClasspath = null;
    
    public Path getClasspath() { return compileClasspath; }
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
    
    /** -d option: directory to output processor and javac generated class files */
    private File baseDir = null;
    public File getBase() { return this.baseDir; }
    public void setBase(File base) { this.baseDir = base; }
    
    /** -s option: directory to place processor generated source files */
    private File sourceBase;
    public void setSourceBase(File sourceBase) { this.sourceBase = sourceBase; }
    public File getSourceBase() { return sourceBase; }
    
//    /** -source option: Provide source compatibility with specified release */
//    protected String source = null;
//    public String getSource() { return source; }
//    public void setSource(String source) { this.source = source; }
    
    /** -A option */
    protected List<Option> options = new ArrayList<Option>();
    public List<Option> getOptions() { return options; }

    public Option createOption() {
            Option option = new Option();
            options.add(option);
            return option;
    }

    /** -J<flag> option: Pass <flag> directly to the runtime */
    protected List<Jvmarg> jvmargs = new ArrayList<Jvmarg>();
    public List<Jvmarg> getJvmargs() { return jvmargs; }

    public Jvmarg createJvmarg() {
        Jvmarg jvmarg = new Jvmarg();
        jvmargs.add(jvmarg);
        return jvmarg;
    }
	
    /** -nocompile option */
    private boolean noCompile = false;
    public boolean isNoCompile() { return noCompile; }
    public void setNoCompile(boolean noCompile) { this.noCompile = noCompile; }

    /******************** -print option **********************/
    private boolean print = false;
    public boolean isPrint() { return print; }
    public void setPrint(boolean print) { this.print = print; }

    /******************** -factorypath option **********************/
    private File factoryPath = null;
    public File getFactoryPath() { return factoryPath; }
    public void setFactoryPath(File factoryPath) { this.factoryPath = factoryPath; }

    /******************** -factory option **********************/
    private String factory = null;
    public String getFactory() { return factory; }
    public void setFactory(String factory) { this.factory = factory; }
	
	/******************** -XListAnnotationTypes option **********************/
    private boolean xListAnnotationTypes = false;
    public boolean isXListAnnotationTypes() { return xListAnnotationTypes; }
    public void setXListAnnotationTypes(boolean xListAnnotationTypes) { this.xListAnnotationTypes = xListAnnotationTypes; }

	/******************** -XListDeclarations option **********************/
    private boolean xListDeclarations = false;
    public boolean isXListDeclarations() { return xListDeclarations; }
    public void setXListDeclarations(boolean xListDeclarations) { this.xListDeclarations = xListDeclarations; }

	/******************** -XPrintAptRounds option **********************/
    private boolean xPrintAptRounds = false;
    public boolean isXPrintAptRounds() { return xPrintAptRounds; }
    public void setXPrintAptRounds(boolean xPrintAptRounds) { this.xPrintAptRounds = xPrintAptRounds; }

    /******************** -XPrintFactoryInfo option **********************/
    private boolean xPrintFactoryInfo = false;
    public boolean isXPrintFactoryInfo() { return xPrintFactoryInfo; }
    public void setXPrintFactoryInfo(boolean xPrintFactoryInfo) { this.xPrintFactoryInfo = xPrintFactoryInfo; }

	/******************** -XclassesAsDecls option **********************/
    private boolean xClassesAsDecls = false;
    public boolean isXClassesAsDecls() { return xClassesAsDecls; }
    public void setXClassesAsDecls(boolean xClassesAsDecls) { this.xClassesAsDecls = xClassesAsDecls; }
	
    /** Inherited from javac */

    /** -g option: debugging info */
    protected boolean debug = false;
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }
	
    /** debug level */
    protected String debugLevel = null;
    public String getDebugLevel() { return debugLevel; }
    public void setDebugLevel(String debugLevel) { this.debugLevel = debugLevel; }
	
    /** -nowarn option: generate no warnings */
	protected boolean nowarn = false;
    public boolean isNowarn() { return nowarn; }
    public void setNowarn(boolean nowarn) { this.nowarn = nowarn; }

    /** -deprecation option: output source locations where deprecated APIs are used */
	protected boolean deprecation = false;
    public boolean isDeprecation() { return deprecation; }
    public void setDeprecation(boolean deprecation) { this.deprecation = deprecation; }

    /** -bootclasspath option: override location of bootstrap class files */
	protected Path bootclassPath = null;
    public Path getBootclassPath() { return bootclassPath; }
    public void setBootclassPath(Path bootclassPath) { this.bootclassPath = bootclassPath; }

    /** -extdirs option: override location of installed extensions */
	protected String extdirs = null;
    public String getExtdirs() { return extdirs; }
    public void setExtdirs(String extdirs) { this.extdirs = extdirs; }

    /** -endorseddirs option: override location of endorsed standards path */
	protected String endorseddirs = null;
    public String getEndorseddirs() { return endorseddirs; }
    public void setEndorseddirs(String endorseddirs) { this.endorseddirs = endorseddirs; }

	/** -verbose option: output messages about what the compiler is doing */
	protected boolean verbose = false;
    public boolean isVerbose() { return verbose; }
    public void setVerbose(boolean verbose) { this.verbose = verbose; }
	
    /** -sourcepath option: Specify where to find input source files */
	protected Path sourcePath = null;
    public Path getSourcePath() { return sourcePath; }
    public void setSourcePath(Path sourcePath) { this.sourcePath = sourcePath; }

    /** -encoding option: character encoding used by the source files */
	protected String encoding = null;
    public String getEncoding() { return encoding; }
    public void setEncoding(String encoding) { this.encoding = encoding; }

    /** -target option: generate class files for specific VM version */
	protected String target = null;
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }


//    /** -failonerror option */
//	protected String failonerror = null;
//    public String isFailonerror() { return failonerror; }
//    public void setFailonerror(String failonerror) { this.failonerror = failonerror; }

	/** Others */
	
    /** -fork option: */
	protected boolean fork = false;
    public boolean isFork() { return fork; }
    public void setFork(boolean fork) { this.fork = fork; }
	
    protected List<FileSet> sourceFileset = new ArrayList<FileSet>();
    public void addConfiguredSource(FileSet fileset) {
        sourceFileset.add(fileset);
    }
	
//    /***********************  include ant runtime **********************/
//    /** not sure if these methods are needed */
//    private boolean includeAntRuntime = false;
//    
//    /**
//     * Include ant's own classpath in this task's classpath?
//     */
//    public void setIncludeantruntime(boolean include) {
//        includeAntRuntime = include;
//    }
//    
//    /**
//     * Gets whether or not the ant classpath is to be included in the
//     * task's classpath.
//     */
//    public boolean getIncludeantruntime() {
//        return includeAntRuntime;
//    }
//    
//    /***********************  include java runtime **********************/
//    /** not sure if these methods are needed */
//    private boolean includeJavaRuntime = false;
//    
//    /**
//     * Sets whether or not to include the java runtime libraries to this
//     * task's classpath.
//     */
//    public void setIncludejavaruntime(boolean include) {
//        includeJavaRuntime = include;
//    }
//    
//    /**
//     * Gets whether or not the java runtime should be included in this
//     * task's classpath.
//     */
//    public boolean getIncludejavaruntime() {
//        return includeJavaRuntime;
//    }
//    
//    /** not sure if this method is needed */
//    private Path generateCompileClasspath() {
//        Path classpath = new Path(getProject());
//        
//        if (getClasspath() == null) {
//            if (getIncludeantruntime()) {
//                classpath.addExisting(Path.systemClasspath);
//            }
//        } else {
//            if (getIncludeantruntime()) {
//                classpath.addExisting(
//                    getClasspath().concatSystemClasspath("last"));
//            } else {
//                classpath.addExisting(
//                    getClasspath().concatSystemClasspath("ignore"));
//            }
//        }
//        
//        if (getIncludejavaruntime()) {
//            
//            // JDK > 1.1 seems to set java.home to the JRE directory.
//            classpath.addExisting(new Path(null,
//                System.getProperty("java.home") +
//                File.separator + "lib" +
//                File.separator + "rt.jar"));
//            
//            /* Just keep the old version as well and let addExistingToPath
//             * sort it out.
//             */
//            classpath.addExisting(new Path(null,
//                System.getProperty("java.home") +
//                File.separator + "jre" +
//                File.separator + "lib" +
//                File.separator + "rt.jar"));
//        }
//        
//        return classpath;
//    }
    
    private Commandline setupAptCommand() {
        Commandline cmd = setupAptArgs();
        
        // classpath option (cp option just uses classpath option)
        // Path classpath = generateCompileClasspath();
        Path classpath = getClasspath();
        
        if (classpath != null && !classpath.toString().equals("")) {
            cmd.createArgument().setValue("-classpath");
            cmd.createArgument().setPath(classpath);
        }
        return cmd;
    }
    
    private Commandline setupAptForkCommand() {
        CommandlineJava forkCmd = new CommandlineJava();
        
        Path classpath = getClasspath();
        forkCmd.createClasspath(getProject()).append(classpath);
        forkCmd.setClassname("com.sun.tools.apt.Main");
        if (null != getJvmargs()) {
            for (Jvmarg jvmarg : jvmargs) {
                forkCmd.createVmArgument().setLine("-J" + jvmarg.getValue());
            }
        }
        
        Commandline cmd = setupAptArgs();
        cmd.createArgument(true).setLine(forkCmd.toString());
        return cmd;
    }
    
    private Commandline setupAptArgs() {
        Commandline cmd = new Commandline();
        
        if (null != getBase() && !getBase().equals("")) {
            cmd.createArgument().setValue("-d");
            cmd.createArgument().setFile(getBase());
        }
        
        if (null != getSourceBase() && !getSourceBase().equals("")) {
            cmd.createArgument().setValue("-s");
            cmd.createArgument().setFile(getSourceBase());
        }
		
//        if (getSource() != null && !getSource().equals("")) {
//            cmd.createArgument().setValue("-source");
//            cmd.createArgument().setValue(getSource());
//        }

        if (getSourcePath() == null)
            throw new BuildException("\"sourcePath\" attribute must be set.");
        
        if (getSourcePath() != null && !getSourcePath().equals("")) {
            cmd.createArgument().setValue("-sourcepath");
            cmd.createArgument().setValue(getSourcePath().toString());
        }
        
        if (getBootclassPath() != null && !getBootclassPath().equals("")) {
            cmd.createArgument().setValue("-bootclasspath");
            cmd.createArgument().setValue(getBootclassPath().toString());
        }
        
        if (getExtdirs() != null && !getExtdirs().equals("")) {
            cmd.createArgument().setValue("-extdirs");
            cmd.createArgument().setValue(getExtdirs());
        }
        
        if (getEndorseddirs() != null && !getEndorseddirs().equals("")) {
            cmd.createArgument().setValue("-endorseddirs");
            cmd.createArgument().setValue(getEndorseddirs());
        }
        
        if (isDebug()) {
            String debugOption = new String();
            debugOption = "-g";
            if (getDebugLevel() != null && !getDebugLevel().equals(""))
                    debugOption += ":" + getDebugLevel();
            cmd.createArgument().setValue(debugOption);
        } else
            cmd.createArgument().setValue("-g:none");
        
		if (isVerbose())
			cmd.createArgument().setValue("-verbose");
		
        if (getEncoding() != null && !getEncoding().equals("")) {
            cmd.createArgument().setValue("-encoding");
            cmd.createArgument().setValue(getEncoding().toString());
        }
        
        if (getTarget() != null && !getTarget().equals("")) {
            cmd.createArgument().setValue("-target");
            cmd.createArgument().setValue(getTarget());
        }
        
        for (Jvmarg jvmarg : jvmargs) {
            cmd.createArgument().setValue("-J" + jvmarg.getValue());
        }

        for (Option option : options) {
            cmd.createArgument().setValue("-A" + option.getKey() + "=" + option.getValue());
        }
        
        if(isNowarn()){
            cmd.createArgument().setValue("-nowarn");
        }

        if(isNoCompile()){
            cmd.createArgument().setValue("-nocompile");
        }
		
        if(isDeprecation()){
            cmd.createArgument().setValue("-deprecation");
        }

        if(isPrint()){
            cmd.createArgument().setValue("-print");
        }

        if(getFactoryPath() != null){
            cmd.createArgument().setValue("-factorypath");
			cmd.createArgument().setValue(getFactoryPath().toString());
        }
		
        if(getFactory() != null){
            cmd.createArgument().setValue("-factory");
			cmd.createArgument().setValue(getFactory());
        }
		
        if (isXListAnnotationTypes()) {
            cmd.createArgument().setValue("-XListAnnotationTypes");
        }
        
        if (isXListDeclarations()) {
            cmd.createArgument().setValue("-XListDeclarations");
        }
        
        if (isXPrintAptRounds()) {
            cmd.createArgument().setValue("-XPrintAptRounds");
        }
        
        if (isXPrintFactoryInfo()) {
            cmd.createArgument().setValue("-XPrintFactoryInfo");
        }
		
        if (isXPrintFactoryInfo()) {
            cmd.createArgument().setValue("-XclassesAsDecls");
        }
		
        Set<File> sourceFiles = new HashSet<File>();
        prepareSourceList(sourceFiles);
		
        if(!sourceFiles.isEmpty()){
            for(File source : sourceFiles){
                cmd.createArgument().setFile(source);                
            }
        }
        
        return cmd;
    }

    void prepareSourceList(Set<File> sourceFiles) throws BuildException {
        if (sourceFileset != null) {
            for (FileSet fileset : sourceFileset) {
                DirectoryScanner ds = fileset.getDirectoryScanner(getProject());
                String[] includedFiles = ds.getIncludedFiles();
                File baseDir = ds.getBasedir();
                for (int i = 0; i < includedFiles.length; ++i) {
                    sourceFiles.add(new File(baseDir, includedFiles[i]));
                }
            }
        }
    }
    
    /** Called by the project to let the task do it's work **/
    public void execute() throws BuildException {

        PrintWriter writer = null;
        boolean ok = false;
        try {
            Commandline cmd = fork ?
                setupAptForkCommand() : setupAptCommand();
			
//            Commandline cmd = setupAptCommand();
            if (verbose) {
                log("command line: " + "apt " + cmd.toString());
            }
			int status = 0;
            if (fork) {
                status = run(cmd.getCommandline());
            } else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
                writer = new PrintWriter(baos);

                ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                try {
					com.sun.tools.apt.Main aptTool = new com.sun.tools.apt.Main();
                    status = aptTool.process(writer, cmd.getArguments());
					writer.flush();
					if (verbose || !baos.toString().equals(""))
						log(baos.toString());
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
            ok = (status == 0) ? true : false;
            if (!ok) {
                if (!verbose) {
                    log("Command invoked: "+"apt "+cmd.toString());
                }
                throw new BuildException("apt failed", location);
            }
        } catch (Exception ex) {
            if (ex instanceof BuildException) {
                throw (BuildException)ex;
            } else {
                throw new BuildException("Error starting apt: ", ex,
                getLocation());
            }
        } finally {
            if (writer != null) {
				writer.close();
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
    
    public class Option {
        protected String key;
        protected String value;

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public class Jvmarg {
        protected String value;

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
