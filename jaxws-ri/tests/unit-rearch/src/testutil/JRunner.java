/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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

package testutil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * @author JAX-WSA SI Development Team
 */
public class JRunner extends Task {

    /** whether to run the tests in local or HTTP mode */
    private boolean local;

    public boolean getLocal() {
        return this.local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    /** list of targets to invoke */
    private String targets;

    public String getTargets() {
        return targets;
    }

    public void setTargets(String targets) {
        this.targets = targets;
    }
    
    /** Content negotiation for FI */
    private String contentNegotiation;

    public String getContentNegotiation() {
        return contentNegotiation;
    }

    public void setContentNegotiation(String contentNegotiation) {
        this.contentNegotiation = contentNegotiation;
    }

    /** verbose option */
    protected boolean verbose = false;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /** failonerror option */
    protected boolean failonerror = false;

    public boolean isFailonerror() {
        return failonerror;
    }

    public void setFailonerror(boolean failonerror) {
        this.failonerror = failonerror;
    }

    protected List<FileSet> buildFileFileset = new ArrayList<FileSet>();

    public void addConfiguredBuildFile(FileSet fileset) {
        buildFileFileset.add(fileset);
    }

    void prepareBuildFileList(Set<File> buildFileFiles) throws BuildException {
        if (buildFileFileset != null) {
            for (FileSet fileset : buildFileFileset) {
                DirectoryScanner ds = fileset.getDirectoryScanner(getProject());
                String[] includedFiles = ds.getIncludedFiles();
                File baseDir = ds.getBasedir();
                for (int i = 0; i < includedFiles.length; ++i) {
                    buildFileFiles.add(new File(baseDir, includedFiles[i]));
                }
            }
        }
    }

    /** Called by the project to let the task do it's work * */
    public void execute() throws BuildException {
        Set<File> buildFileFiles = new HashSet<File>();
        prepareBuildFileList(buildFileFiles);

        int count = 0;
        for (File buildFile : buildFileFiles) {
            log("Build file: " + ++count + buildFile.getAbsolutePath());

            // initialize the project
            Project project = new Project();
            initializeProject(project, buildFile);

            project.setProperty("uselocal", String.valueOf(getLocal()));
            
            if (contentNegotiation != null && contentNegotiation.length() > 0) {
                project.setProperty("contentNegotiation", getContentNegotiation());
            }

            // parse the list of tokens
            StringTokenizer tokens = new StringTokenizer(getTargets(), ",");
            while (tokens.hasMoreTokens()) {
                String target = tokens.nextToken().trim();

                if (verbose) {
                    log("Invoking ... " + target);
                }

                try {
                    project.executeTarget(target);
                } catch (BuildException ex) {
                    if (failonerror)
                        throw ex;
                }
            }
        }
    }
    
    private void initializeProject(Project project, File buildFile) {
        project.init();
        
        DefaultLogger consoleLogger = new DefaultLogger();
        consoleLogger.setErrorPrintStream(System.err);
        consoleLogger.setOutputPrintStream(System.out);
        consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
        project.addBuildListener(consoleLogger);
        
        project.setUserProperty("ant.file", buildFile.getAbsolutePath());
        ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
        projectHelper.parse(project, buildFile);        
    }
}
