/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.tools.ws.processor.modeler.annotation.WebServiceAp;
import com.sun.tools.ws.resources.JavacompilerMessages;
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
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Reincarnation of old APT task
 * The task has the same API as old one, with only difference that it isn't possible to set additional annotation factories
 *
 * Ant task which will process JAX-WS annotations
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
     * @return 
     */
    public Path createClasspath() {
        if (compileClasspath == null) {
            compileClasspath = new Path(project);
        }
        return compileClasspath.createPath();
    }

    /**
     * Adds a reference to a CLASSPATH defined elsewhere.
     * @param r
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
    protected List<Option> options = new ArrayList<>();

    public List<Option> getOptions() {
        return options;
    }

    public Option createOption() {
        Option option = new Option();
        options.add(option);
        return option;
    }

    /**
     * -J&lt;flag&gt; option: Pass &lt;flag&gt; directly to the runtime
     */
    protected List<Jvmarg> jvmargs = new ArrayList<>();

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

    protected List<FileSet> sourceFileset = new ArrayList<>();

    public void addConfiguredSource(FileSet fileset) {
        sourceFileset.add(fileset);
    }

    private Commandline setupAptCommand() {
        Commandline cmd = setupAptArgs();

        // classpath option (cp option just uses classpath option)
        Path classpath = getClasspath();

        if (classpath != null && !classpath.toString().equals("")) {
            addValue(cmd, "-classpath", classpath);
        }
        return cmd;
    }

    private Commandline setupAptForkCommand() {
        CommandlineJava forkCmd = new CommandlineJava();

        Path classpath = getClasspath();
        forkCmd.createClasspath(getProject()).append(classpath);
        forkCmd.setClassname("com.sun.tools.javac.Main");
        if (getJvmargs() != null) {
            for (Jvmarg jvmarg : jvmargs) {
                forkCmd.createVmArgument().setLine(jvmarg.getValue());
            }
        }

        Commandline cmd = setupAptArgs();
        cmd.createArgument(true).setLine(forkCmd.toString());

        return cmd;
    }

    private Commandline setupAptArgs() {
        Commandline ap = new Commandline();

        if (getDestdir() != null && !getDestdir().getName().equals("")) {
            addValue(ap, "-d", getDestdir());
        }

        if (getSourcedestdir() != null && !getSourcedestdir().getName().equals("")) {
            addValue(ap, "-s", getSourcedestdir());
        }

        if (getSourcepath() == null)
            throw new BuildException("\"sourcePath\" attribute must be set.");

        if (getSourcepath() != null && !getSourcepath().toString().equals("")) {
            addValue(ap, "-sourcepath", getSourcepath().toString());
        }

        if (getBootclasspath() != null && !getBootclasspath().toString().equals("")) {
            addValue(ap, "-bootclasspath", getBootclasspath().toString());
        }

        if (getExtdirs() != null && !getExtdirs().equals("")) {
            addValue(ap, "-extdirs", getExtdirs());
        }

        if (getEndorseddirs() != null && !getEndorseddirs().equals("")) {
            addValue(ap, "-endorseddirs", getEndorseddirs());
        }

        if (isDebug()) {
            String debugOption = "-g";
            if (getDebuglevel() != null && !getDebuglevel().equals(""))
                debugOption += ":" + getDebuglevel();
            addValue(ap, debugOption);
        } else
            addValue(ap, "-g:none");

        if (isVerbose())
            addValue(ap, "-verbose");

        if (getEncoding() != null && !getEncoding().equals("")) {
            addValue(ap, "-encoding", getEncoding());
        }

        if (getTarget() != null && !getTarget().equals("")) {
            addValue(ap, "-target", getTarget());
        }

        //if fork, these arguments will appear twice
        if (!fork) {
            for (Jvmarg jvmarg : jvmargs) {
                addValue(ap, "-J" + jvmarg.getValue());
            }
        }

        for (Option option : options) {
            addValue(ap, "-A" + option.getKey() + "=" + option.getValue());
        }

        if (isNowarn()) {
            addValue(ap, "-nowarn");
        }

        if (isNocompile()) {
            addValue(ap, "-proc:only");
        }

        if (isDeprecation()) {
            addValue(ap, "-deprecation");
        }

        if (isPrint()) {
            addValue(ap, "-Xprint");
        }

        if (isXprintaptrounds())
            addValue(ap, "-XprintRounds");

        if (isXprintfactoryinfo())
            addValue(ap, "-XprintProcessorInfo");

        Set<File> sourceFiles = new HashSet<>();
        prepareSourceList(sourceFiles);

        if (!sourceFiles.isEmpty()) {
            for (File source : sourceFiles) {
                ap.createArgument().setFile(source);
            }
        }

        addValue(ap, "-processor", WebServiceAp.class.getName());

        return ap;
    }

    private static void addValue(Commandline ap, String name) {
        addValue(ap, name, (String) null);
    }

    private static void addValue(Commandline ap, String name, String value) {
        ap.createArgument().setValue(name);
        if (value != null) {
            ap.createArgument().setValue(value);
        }
    }

    private static void addValue(Commandline ap, String name, Path value) {
        ap.createArgument().setValue(name);
        if (value != null) {
            ap.createArgument().setPath(value);
        }
    }

    private static void addValue(Commandline ap, String name, File value) {
        ap.createArgument().setValue(name);
        if (value != null) {
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
    @Override
    public void execute() throws BuildException {

        PrintWriter writer = null;
        try {
            Commandline cmd = fork ? setupAptForkCommand() : setupAptCommand();

            if (verbose) {
                log("command line: apt " + cmd.toString());
            }
            int status = 0;
            if (fork)
                status = run(cmd.getCommandline());
            else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                writer = new PrintWriter(baos);

                ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                try {
                    JavaCompiler comp = ToolProvider.getSystemJavaCompiler();
                    if (comp == null) {
                        writer.println(JavacompilerMessages.NO_JAVACOMPILER_ERROR());
                        status = -1;
                    } else {
                        status = comp.run(null, baos, baos, cmd.getArguments());
                    }

                    writer.flush();
                    if (verbose || baos.size() != 0) {
                        log(baos.toString());
                    }
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
            if (status != 0) {
                if (!verbose) {
                    log("Command invoked: apt " + cmd.toString());
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
}
