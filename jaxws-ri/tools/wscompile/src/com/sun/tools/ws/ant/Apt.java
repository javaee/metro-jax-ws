package com.sun.tools.ws.ant;

import com.sun.tools.apt.Main;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * OLD apt task for use with the JAXWS project.
 * <p/>
 * In case factories is set - old APT functionality will be called
 * After this new javax.annotation processing will be run
 */
@Deprecated
public class Apt extends Task {

    /**
     * -classpath option
     */
    protected Path compileClasspath = null;

    public Path getClasspath() {
        return compileClasspath;
    }

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

    /**
     * -d option: directory to output processor and javac generated class files
     */
    private File destDir = null;

    public File getDestdir() {
        return this.destDir;
    }

    public void setDestdir(File base) {
        this.destDir = base;
    }

    /**
     * -s option: directory to place processor generated source files
     */
    private File sourceDestDir;

    public void setSourcedestdir(File sourceBase) {
        this.sourceDestDir = sourceBase;
    }

    public File getSourcedestdir() {
        return sourceDestDir;
    }

    /**
     * -A option
     */
    protected List<Option> options = new ArrayList<Option>();

    public List<Option> getOptions() {
        return options;
    }

    public Option createOption() {
        Option option = new Option();
        options.add(option);
        return option;
    }

    /**
     * -J<flag> option: Pass <flag> directly to the runtime
     */
    protected List<Jvmarg> jvmargs = new ArrayList<Jvmarg>();

    public List<Jvmarg> getJvmargs() {
        return jvmargs;
    }

    public Jvmarg createJvmarg() {
        Jvmarg jvmarg = new Jvmarg();
        jvmargs.add(jvmarg);
        return jvmarg;
    }

    /**
     * -nocompile option
     */
    private boolean noCompile = false;

    public boolean isNocompile() {
        return noCompile;
    }

    public void setNocompile(boolean noCompile) {
        this.noCompile = noCompile;
    }

    /**
     * ***************** -print option *********************
     */
    private boolean print = false;

    public boolean isPrint() {
        return print;
    }

    public void setPrint(boolean print) {
        this.print = print;
    }

    /**
     * ***************** -factorypath option *********************
     */
    private File factoryPath = null;

    public File getFactorypath() {
        return factoryPath;
    }

    public void setFactorypath(File factoryPath) {
        this.factoryPath = factoryPath;
    }

    /**
     * ***************** -factory option *********************
     */
    private String factory = null;

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    /**
     * ***************** -XListAnnotationTypes option *********************
     */
    private boolean xListAnnotationTypes = false;

    public boolean isXlistannotationtypes() {
        return xListAnnotationTypes;
    }

    public void setXlistannotationtypes(boolean xListAnnotationTypes) {
        this.xListAnnotationTypes = xListAnnotationTypes;
    }

    /**
     * ***************** -XListDeclarations option *********************
     */
    private boolean xListDeclarations = false;

    public boolean isXlistdeclarations() {
        return xListDeclarations;
    }

    public void setXlistdeclarations(boolean xListDeclarations) {
        this.xListDeclarations = xListDeclarations;
    }

    /**
     * ***************** -XPrintAptRounds option *********************
     */
    private boolean xPrintAptRounds = false;

    public boolean isXprintaptrounds() {
        return xPrintAptRounds;
    }

    public void setXprintaptrounds(boolean xPrintAptRounds) {
        this.xPrintAptRounds = xPrintAptRounds;
    }

    /**
     * ***************** -XPrintFactoryInfo option *********************
     */
    private boolean xPrintFactoryInfo = false;

    public boolean isXprintfactoryinfo() {
        return xPrintFactoryInfo;
    }

    public void setXprintfactoryinfo(boolean xPrintFactoryInfo) {
        this.xPrintFactoryInfo = xPrintFactoryInfo;
    }

    /**
     * ***************** -XclassesAsDecls option *********************
     */
    private boolean xClassesAsDecls = false;

    public boolean isXclassesasdecls() {
        return xClassesAsDecls;
    }

    public void setXclassesasdecls(boolean xClassesAsDecls) {
        this.xClassesAsDecls = xClassesAsDecls;
    }

    /** Inherited from javac */

    /**
     * -g option: debugging info
     */
    protected boolean debug = false;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * debug level
     */
    protected String debugLevel = null;

    public String getDebuglevel() {
        return debugLevel;
    }

    public void setDebuglevel(String debugLevel) {
        this.debugLevel = debugLevel;
    }

    /**
     * -nowarn option: generate no warnings
     */
    protected boolean nowarn = false;

    public boolean isNowarn() {
        return nowarn;
    }

    public void setNowarn(boolean nowarn) {
        this.nowarn = nowarn;
    }

    /**
     * -deprecation option: output source locations where deprecated APIs are used
     */
    protected boolean deprecation = false;

    public boolean isDeprecation() {
        return deprecation;
    }

    public void setDeprecation(boolean deprecation) {
        this.deprecation = deprecation;
    }

    /**
     * -bootclasspath option: override location of bootstrap class files
     */
    protected Path bootclassPath = null;

    public Path getBootclasspath() {
        return bootclassPath;
    }

    public void setBootclasspath(Path bootclassPath) {
        this.bootclassPath = bootclassPath;
    }

    /**
     * -extdirs option: override location of installed extensions
     */
    protected String extdirs = null;

    public String getExtdirs() {
        return extdirs;
    }

    public void setExtdirs(String extdirs) {
        this.extdirs = extdirs;
    }

    /**
     * -endorseddirs option: override location of endorsed standards path
     */
    protected String endorseddirs = null;

    public String getEndorseddirs() {
        return endorseddirs;
    }

    public void setEndorseddirs(String endorseddirs) {
        this.endorseddirs = endorseddirs;
    }

    /**
     * -verbose option: output messages about what the compiler is doing
     */
    protected boolean verbose = false;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * -sourcepath option: Specify where to find input source files
     */
    protected Path sourcePath = null;

    public Path getSourcepath() {
        return sourcePath;
    }

    public void setSourcepath(Path sourcePath) {
        this.sourcePath = sourcePath;
    }

    /**
     * -encoding option: character encoding used by the source files
     */
    protected String encoding = null;

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * -target option: generate class files for specific VM version
     */
    protected String targetVM = null;

    public String getTarget() {
        return targetVM;
    }

    public void setTarget(String target) {
        this.targetVM = target;
    }

    /** Others */

    /**
     * -fork option:
     */
    protected boolean fork = false;

    public boolean isFork() {
        return fork;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }

    protected List<FileSet> sourceFileset = new ArrayList<FileSet>();

    public void addConfiguredSource(FileSet fileset) {
        sourceFileset.add(fileset);
    }

    private Container setupAptCommand() {
        Container c = setupAptArgs();

        // classpath option (cp option just uses classpath option)
        Path classpath = getClasspath();

        if (classpath != null && !classpath.toString().equals("")) {
            addValue(c.cmd, c.ap, "-classpath", classpath);
        }
        return c;
    }

    private Container setupAptForkCommand() {
        CommandlineJava forkCmd = new CommandlineJava();

        Path classpath = getClasspath();
        forkCmd.createClasspath(getProject()).append(classpath);
        forkCmd.setClassname("com.sun.tools.apt.Main");
        if (null != getJvmargs()) {
            for (Jvmarg jvmarg : jvmargs) {
                forkCmd.createVmArgument().setLine(jvmarg.getValue());
            }
        }

        Container c = setupAptArgs();
        c.cmd.createArgument(true).setLine(forkCmd.toString());

        forkCmd = new CommandlineJava();
        forkCmd.createClasspath(getProject()).append(classpath);
        forkCmd.setClassname("com.sun.tools.javac.Main");
        if (null != getJvmargs()) {
            for (Jvmarg jvmarg : jvmargs) {
                forkCmd.createVmArgument().setLine(jvmarg.getValue());
            }
        }

        c.ap.createArgument(true).setLine(forkCmd.toString());
        return c;
    }

    private Container setupAptArgs() {
        Commandline cmd = new Commandline();
        Commandline ap = new Commandline();

        if (null != getDestdir() && !getDestdir().getName().equals("")) {
            addValue(cmd, ap, "-d", getDestdir());
        }

        if (null != getSourcedestdir() && !getSourcedestdir().getName().equals("")) {
            addValue(cmd, ap, "-s", getSourcedestdir());
        }

        if (getSourcepath() == null)
            throw new BuildException("\"sourcePath\" attribute must be set.");

        if (getSourcepath() != null && !getSourcepath().toString().equals("")) {
            addValue(cmd, ap, "-sourcepath", getSourcepath().toString());
        }

        if (getBootclasspath() != null && !getBootclasspath().toString().equals("")) {
            addValue(cmd, ap, "-bootclasspath", getBootclasspath().toString());
        }

        if (getExtdirs() != null && !getExtdirs().equals("")) {
            addValue(cmd, ap, "-extdirs", getExtdirs());
        }

        if (getEndorseddirs() != null && !getEndorseddirs().equals("")) {
            addValue(cmd, ap, "-endorseddirs", getEndorseddirs());
        }

        if (isDebug()) {
            String debugOption = "-g";
            if (getDebuglevel() != null && !getDebuglevel().equals(""))
                debugOption += ":" + getDebuglevel();
            cmd.createArgument().setValue(debugOption);
            ap.createArgument().setValue(debugOption);
        } else
            addValue(cmd, ap, "-g:none");

        if (isVerbose())
            addValue(cmd, ap, "-verbose");

        if (getEncoding() != null && !getEncoding().equals("")) {
            addValue(cmd, ap, "-encoding", getEncoding());
        }

        if (getTarget() != null && !getTarget().equals("")) {
            addValue(cmd, ap, "-target", getTarget());
        }

        //if fork, these arguments will appear twice
        if (!fork) {
            for (Jvmarg jvmarg : jvmargs) {
                addValue(cmd, ap, "-J" + jvmarg.getValue());
            }
        }

        for (Option option : options) {
            addValue(cmd, ap, "-A" + option.getKey() + "=" + option.getValue());
        }

        if (isNowarn()) {
            addValue(cmd, ap, "-nowarn");
        }

        if (isNocompile()) {
            cmd.createArgument().setValue("-nocompile");
            ap.createArgument().setValue("-proc:only");
        }

        if (isDeprecation()) {
            addValue(cmd, ap, "-deprecation");
        }

        if (isPrint()) {
            cmd.createArgument().setValue("-print");
            ap.createArgument().setValue("-Xprint");
        }

        if (getFactorypath() != null) {
            cmd.createArgument().setValue("-factorypath");
            cmd.createArgument().setValue(getFactorypath().toString());
        }

        if (getFactory() != null) {
            cmd.createArgument().setValue("-factory");
            cmd.createArgument().setValue(getFactory());
        }

        if (isXlistannotationtypes()) {
            cmd.createArgument().setValue("-XListAnnotationTypes");
        }

        if (isXlistdeclarations()) {
            cmd.createArgument().setValue("-XListDeclarations");
        }

        if (isXprintaptrounds()) {
            cmd.createArgument().setValue("-XPrintAptRounds");
            ap.createArgument().setValue("-XprintRounds");
        }

        if (isXprintfactoryinfo()) {
            cmd.createArgument().setValue("-XPrintFactoryInfo");
            ap.createArgument().setValue("-XprintProcessorInfo");
        }

        if (isXprintfactoryinfo()) {
            cmd.createArgument().setValue("-XclassesAsDecls");
        }

        Set<File> sourceFiles = new HashSet<File>();
        prepareSourceList(sourceFiles);

        if (!sourceFiles.isEmpty()) {
            for (File source : sourceFiles) {
                cmd.createArgument().setFile(source);
                ap.createArgument().setFile(source);
            }
        }

        ap.createArgument().setValue("-processor");
        ap.createArgument().setValue("com.sun.tools.ws.processor.modeler.annotation.WebServiceAp");

        return new Container(cmd, ap);
    }

    private static void addValue(Commandline cmd, Commandline ap, String name) {
        addValue(cmd, ap, name, (String) null);
    }

    private static void addValue(Commandline cmd, Commandline ap, String name, String value) {
        cmd.createArgument().setValue(name);
        ap.createArgument().setValue(name);
        if (value != null) {
            cmd.createArgument().setValue(value);
            ap.createArgument().setValue(value);
        }
    }

    private static void addValue(Commandline cmd, Commandline ap, String name, Path value) {
        cmd.createArgument().setValue(name);
        ap.createArgument().setValue(name);
        if (value != null) {
            cmd.createArgument().setPath(value);
            ap.createArgument().setPath(value);
        }
    }

    private static void addValue(Commandline cmd, Commandline ap, String name, File value) {
        cmd.createArgument().setValue(name);
        ap.createArgument().setValue(name);
        if (value != null) {
            cmd.createArgument().setFile(value);
            ap.createArgument().setFile(value);
        }
    }

    void prepareSourceList(Set<File> sourceFiles) throws BuildException {
        if (sourceFileset != null) {
            for (FileSet fileset : sourceFileset) {
                DirectoryScanner ds = fileset.getDirectoryScanner(getProject());
                String[] includedFiles = ds.getIncludedFiles();
                File baseDir = ds.getBasedir();
                for (String includedFile : includedFiles) {
                    sourceFiles.add(new File(baseDir, includedFile));
                }
            }
        }
    }

    /**
     * Called by the project to let the task do it's work *
     */
    public void execute() throws BuildException {

        PrintWriter writer = null;
        try {
            Container c = fork ? setupAptForkCommand() : setupAptCommand();

            if (verbose) {
                log("command line: " + "apt " + c.cmd.toString());
            }
            int status = 0;
            if (fork) {
                status = run(c.cmd.getCommandline());
                if (status == 0)
                    status = run(c.ap.getCommandline());
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                writer = new PrintWriter(baos);

                ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                try {
                    com.sun.tools.apt.Main aptTool = new com.sun.tools.apt.Main();
                    status = Main.process(writer, c.cmd.getArguments());
                    writer.flush();
                    if (verbose || baos.size() != 0)
                        log(baos.toString());
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
                if (status == 0) {
                    baos = new ByteArrayOutputStream();
                    writer = new PrintWriter(baos);

                    old = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                    try {
                        com.sun.tools.javac.Main mainTool = new com.sun.tools.javac.Main();
                        status = com.sun.tools.javac.Main.compile(c.ap.getArguments(), writer);
                        writer.flush();
                        if (verbose || baos.size() != 0)
                            log(baos.toString());
                    } finally {
                        Thread.currentThread().setContextClassLoader(old);
                    }
                }
            }
            if (status != 0) {
                if (!verbose) {
                    log("Command invoked: " + "apt " + c.cmd.toString());
                }
                throw new BuildException("apt failed", location);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex instanceof BuildException) {
                throw (BuildException) ex;
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
        LogStreamHandler logstr = new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN);
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

    public static class Option {

        protected String key;
        protected String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Jvmarg {

        protected String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class Container {

        private Commandline cmd;
        private Commandline ap;

        private Container(Commandline commandline, Commandline ap) {
            this.cmd = commandline;
            this.ap = ap;
        }
    }
}
