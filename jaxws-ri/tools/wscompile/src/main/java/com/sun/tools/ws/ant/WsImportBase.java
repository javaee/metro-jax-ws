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

import com.sun.tools.ws.wscompile.Options;
import com.sun.tools.ws.wscompile.WsimportTool;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.XMLCatalog;

/**
 *
 * @author lukas
 */
public class WsImportBase extends WsTask2 {

    private final Commandline extraArgs = new Commandline();

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

    private boolean xuseBaseResourceAndURLToLoadWSDL = false;
    public void setXUseBaseResourceAndURLToLoadWSDL(boolean xuseBaseResourceAndURLToLoadWSDL) {
    	this.xuseBaseResourceAndURLToLoadWSDL = xuseBaseResourceAndURLToLoadWSDL;
    }

    public boolean isXUseBaseResourceAndURLToLoadWSDL() {
    	return xuseBaseResourceAndURLToLoadWSDL;
    }

    /**
     * -generateJWS option.
     */
    private boolean isGenerateJWS = false;

    public boolean isGenerateJWS() {
        return this.isGenerateJWS;
    }

    public void setGenerateJWS(boolean isGenerateJWS) {
        this.isGenerateJWS = isGenerateJWS;
    }

    /**
     * -implDestDir option.
     */
    private File implDestDir = null;

    public File getImplDestDir() {
        return this.implDestDir;
    }

    public void setImplDestDir(File base) {
        this.implDestDir = base;
    }

    /**
     * -implServiceName option.
     */
    private String implServiceName = null;

    public String getImplServiceName() {
        return this.implServiceName;
    }

    public void setImplServiceName(String base) {
        this.implServiceName = base;
    }

    /**
     * -implPortName option.
     */
    private String implPortName = null;

    public String getImplPortName() {
        return this.implPortName;
    }

    public void setImplPortName(String base) {
        this.implPortName = base;
    }

    /**
     * Provides a way to directly pass wsimport commandline options through nested arg
     * instead of a separate attribute for each option.
     * @return argument created
     */
    public Commandline.Argument createArg() {
        return extraArgs.createArgument();
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

    /** -clientjar option. */
    private String clientjar = null;

    /**
     * Gets the clientjar to output generated artifacts into a jar.
     *
     * @return jar file where to put generated artifacts.
     */
    public String getClientjar() {
        return this.clientjar;
    }

    /**
     * Sets the base directory to output generated class.
     *
     * @param clientjar jar file where to put generated artifacts.
     */
    public void setClientjar(String clientjar) {
        this.clientjar = clientjar;
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

    /** -m <name> option. */
    private String module;

    /**
     * Sets Java module name.
     * @param module Java module name to set. {@code null} turns (@code module-info.java} generation off.
     */
    public void setModule(String module){
        this.module = module;
    }

    /**
     * Gets Java module name.
     * @return Java module name or {@code null} if (@code module-info.java} generation is turned off.
     */
    public String getModule(){
        return module;
    }

    /**
     * Adds XJC argument.
     *
     * @return XJC argument created.
     * @since 2.1
     */
    public Commandline.Argument createXjcarg() {
        return xjcCmdLine.createArgument();
    }

    public boolean getxNoAddressingDatabinding() {
        return xNoAddressingDatabinding;
    }

    public void setxNoAddressingDatabinding(boolean xNoAddressingDatabinding) {
        this.xNoAddressingDatabinding = xNoAddressingDatabinding;
    }

    /**** -Xno-addressing-databinding ***/
    protected boolean xNoAddressingDatabinding;

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
    private String specTarget;
    public void setTarget( String version ) {
        Options.Target targetVersion = Options.Target.parse(version);
        if(targetVersion==null)
            throw new BuildException(version+" is not a valid version number");
        specTarget = targetVersion.getVersion();
    }

    /**
     * Files used to determine whether XJC should run or not.
     */
    private final ArrayList<File> dependsSet = new ArrayList<>();
    private final ArrayList<File> producesSet = new ArrayList<>();

    /**
     * Set to true once the &lt;produces&gt; element is used.
     * This flag is used to issue a suggestion to users.
     */
    private boolean producesSpecified = false;

    /**
     * Nested &lt;depends&gt; element.
     *
     * @param fs FileSet to check for modifications.
     */
    public void addConfiguredDepends( FileSet fs ) {
        addIndividualFilesTo( fs, dependsSet );
    }

    /**
     * Nested &lt;produces&gt; element.
     *
     * @param fs FileSet to check for modifications.
     */
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
     * @param catalog catalog file to use.
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

    /**
     * -XdisableAuthenticator
     */
    private boolean disableAuthenticator;

    public boolean getXdisableAuthenticator() {
        return disableAuthenticator;
    }

    public void setdisableAuthenticator(boolean disableAuthenticator) {
        this.disableAuthenticator = disableAuthenticator;
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

    @Override
    protected CommandlineJava setupCommand() {
        CommandlineJava cmd = super.setupCommand();
        if(getxNoAddressingDatabinding()){
            cmd.createArgument().setValue("-Xno-addressing-databinding");
        }

        if(isXdebug()){
            cmd.createArgument().setValue("-Xdebug");
        }

        if(isXnocompile()){
            cmd.createArgument().setValue("-Xnocompile");
        } else {
            for (String a : getJavacargs().getArguments()) {
                cmd.createArgument().setValue("-J" + a);
            }
        }

        if(isXadditionalHeaders()){
            cmd.createArgument().setValue("-XadditionalHeaders");
        }

        if(isXUseBaseResourceAndURLToLoadWSDL()){
        	cmd.createArgument().setValue("-XuseBaseResourceAndURLToLoadWSDL");
        }

        //catalog
        if((getCatalog() != null) && (getCatalog().getName().length() > 0)){
            cmd.createArgument().setValue("-catalog");
            cmd.createArgument().setFile(getCatalog());
        }

        if(quiet){
            cmd.createArgument().setValue("-quiet");
        }

        if(specTarget != null){
            cmd.createArgument().setValue("-target");
            cmd.createArgument().setValue(specTarget);
        }

        //wsdl
        if(getWsdl() != null){
            cmd.createArgument().setValue(getWsdl());
        }

        if(getXauthfile() != null){
            cmd.createArgument().setValue("-Xauthfile");
            cmd.createArgument().setFile(getXauthfile());
        }

        if(getXdisableAuthenticator()){
            cmd.createArgument().setValue("-XdisableAuthenticator");
        }

        //package
        if((getPackage() != null) && (getPackage().length() > 0)){
            cmd.createArgument().setValue("-p");
            cmd.createArgument().setValue(getPackage());
        }

        //module
        if (module != null && !module.isEmpty()) {
            cmd.createArgument().setValue("-m");
            cmd.createArgument().setValue(module);
        }

        //clientjar
        if(getClientjar() != null){
            cmd.createArgument().setValue("-clientjar");
            cmd.createArgument().setValue(getClientjar());
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

		//Bug 10384615 - CLIENTGEN FAIL TO DEAL WITH BINGDING FILE USING SOFT LINK
                boolean isLink = false;
                try {
                	isLink = !binding.getCanonicalPath().equals(binding.getAbsolutePath())
                	  && !(binding.getAbsolutePath().contains("~1") &&
				binding.getCanonicalPath().indexOf(' ') >= 0);
		} catch (IOException e) {
					// do nothing
		}

		if(isLink){
			cmd.createArgument().setValue(binding.toURI().toString());
		}else
                	cmd.createArgument().setFile(binding);
            }
        }

        if((wsdlLocation != null) && (wsdlLocation.length() != 0)){
            cmd.createArgument().setValue("-wsdllocation");
            cmd.createArgument().setValue(wsdlLocation);
        }

        //implDestDir option
        if (isGenerateJWS()) {
            cmd.createArgument().setValue("-generateJWS");

            if (getImplDestDir() != null) {
                cmd.createArgument().setValue("-implDestDir");
                cmd.createArgument().setFile(getImplDestDir());
            }
            if (getImplServiceName() != null) {
                cmd.createArgument().setValue("-implServiceName");
                cmd.createArgument().setValue(getImplServiceName());
            }

            if (getImplPortName() != null) {
                cmd.createArgument().setValue("-implPortName");
                cmd.createArgument().setValue(getImplPortName());
            }
        }

        for (String a : extraArgs.getArguments()) {
            cmd.createArgument().setValue(a);
        }
        return cmd;
    }


    /** Called by the project to let the task do it's work **/
    @Override
    public void execute() throws BuildException {
        if (!producesSpecified) {
            log("Consider using <depends>/<produces> so that wsimport won't do unnecessary compilation", Project.MSG_INFO);
        }

        // up to date check
        long srcTime = computeTimestampFor(dependsSet, true);
        long dstTime = computeTimestampFor(producesSet, false);
        log("the last modified time of the inputs is  " + srcTime, Project.MSG_VERBOSE);
        log("the last modified time of the outputs is " + dstTime, Project.MSG_VERBOSE);

        if (srcTime < dstTime) {
            log("files are up to date");
            return;
        }

        execute("wsimport", "com.sun.tools.ws.WsImport");
    }

    private final Set<File> bindingFiles = new HashSet<>();

    @Override
    protected boolean runInVm(String[] arguments, OutputStream out) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String sysPath = System.getProperty("java.class.path");
        if (loader instanceof AntClassLoader) {
            System.setProperty("java.class.path", ((AntClassLoader) loader).getClasspath());
        }
        WsimportTool compTool = new WsimportTool(out);
        if (xmlCatalog != null) {
            compTool.setEntityResolver(xmlCatalog);
        }
        try {
            return compTool.run(arguments);
        } finally {
            if (sysPath != null) {
                System.setProperty("java.class.path", sysPath);
            }
        }
    }
}
