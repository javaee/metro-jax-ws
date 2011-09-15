/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.tools.ws.wscompile;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.writer.ProgressCodeWriter;
import com.sun.tools.ws.ToolVersion;
import com.sun.tools.ws.api.TJavaGeneratorExtension;
import com.sun.tools.ws.processor.generator.CustomExceptionGenerator;
import com.sun.tools.ws.processor.generator.SeiGenerator;
import com.sun.tools.ws.processor.generator.ServiceGenerator;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.modeler.wsdl.ConsoleErrorReporter;
import com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler;
import com.sun.tools.ws.processor.util.DirectoryUtil;
import com.sun.tools.ws.resources.WscompileMessages;
import com.sun.tools.ws.resources.WsdlMessages;
import com.sun.tools.ws.util.WSDLFetcher;
import com.sun.tools.ws.wsdl.parser.MetadataFinder;
import com.sun.tools.ws.wsdl.parser.WSDLInternalizationLogic;
import com.sun.tools.xjc.util.NullStream;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.istack.tools.ParallelWorldClassLoader;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBPermission;
import javax.xml.stream.*;
import javax.xml.ws.EndpointContext;
import java.io.*;
import java.util.*;
import java.net.Authenticator;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * @author Vivek Pandey
 */
public class WsimportTool {
    private static final String WSIMPORT = "wsimport";
    private final PrintStream out;
    private final Container container;

    /**
     * Wsimport specific options
     */
    private final WsimportOptions options = new WsimportOptions();

    public WsimportTool(OutputStream out) {
        this(out, null);
    }

    public WsimportTool(OutputStream logStream, Container container) {
        this.out = (logStream instanceof PrintStream)?(PrintStream)logStream:new PrintStream(logStream);
        this.container = container;
    }

    public boolean run(String[] args) {
        class Listener extends WsimportListener {
            ConsoleErrorReporter cer = new ConsoleErrorReporter(out == null ? new PrintStream(new NullStream()) : out);

            @Override
            public void generatedFile(String fileName) {
                message(fileName);
            }

            @Override
            public void message(String msg) {
                out.println(msg);
            }

            @Override
            public void error(SAXParseException exception) {
                cer.error(exception);
            }

            @Override
            public void fatalError(SAXParseException exception) {
                cer.fatalError(exception);
            }

            @Override
            public void warning(SAXParseException exception) {
                cer.warning(exception);
            }

            @Override
            public void debug(SAXParseException exception) {
                cer.debug(exception);
            }

            @Override
            public void info(SAXParseException exception) {
                cer.info(exception);
            }

            public void enableDebugging(){
                cer.enableDebugging();
            }
        }
        final Listener listener = new Listener();
        ErrorReceiverFilter receiver = new ErrorReceiverFilter(listener) {
            public void info(SAXParseException exception) {
                if (options.verbose)
                    super.info(exception);
            }

            public void warning(SAXParseException exception) {
                if (!options.quiet)
                    super.warning(exception);
            }

            @Override
            public void pollAbort() throws AbortException {
                if (listener.isCanceled())
                    throw new AbortException();
            }

            @Override
            public void debug(SAXParseException exception){
                if(options.debugMode){
                    listener.debug(exception);
                }
            }
        };

        for (String arg : args) {
            if (arg.equals("-version")) {
                listener.message(ToolVersion.VERSION.toString());
                return true;
            }
        }
        
        Authenticator orig = null;
        try {
            options.parseArguments(args);
            options.validate();
            if(options.debugMode)
                listener.enableDebugging();
            options.parseBindings(receiver);

            try {
                if( !options.quiet )
                    listener.message(WscompileMessages.WSIMPORT_PARSING_WSDL());

                //set auth info
                //if(options.authFile != null)
                orig = DefaultAuthenticator.getCurrentAuthenticator();
                    Authenticator.setDefault(new DefaultAuthenticator(receiver, options.authFile));

                MetadataFinder forest = new MetadataFinder(new WSDLInternalizationLogic(), options, receiver);
                forest.parseWSDL();
                if (forest.isMexMetadata)
                    receiver.reset();

                WSDLModeler wsdlModeler = new WSDLModeler(options, receiver,forest);
                Model wsdlModel = wsdlModeler.buildModel();
                if (wsdlModel == null) {
                    listener.message(WsdlMessages.PARSING_PARSE_FAILED());
                    return false;
                }

                if(options.clientjar != null) {
                    if( !options.quiet )
                        listener.message(WscompileMessages.WSIMPORT_FETCHING_METADATA());
                    options.wsdlLocation = new WSDLFetcher(options,listener).fetchWsdls(forest);
                }

                //generated code
                if( !options.quiet )
                    listener.message(WscompileMessages.WSIMPORT_GENERATING_CODE());
                
                TJavaGeneratorExtension[] genExtn = ServiceFinder.find(TJavaGeneratorExtension.class).toArray();
                CustomExceptionGenerator.generate(wsdlModel,  options, receiver);
                SeiGenerator.generate(wsdlModel, options, receiver, genExtn);
                if(receiver.hadError()){
                    throw new AbortException();
                }
                ServiceGenerator.generate(wsdlModel, options, receiver);
                CodeWriter cw = new WSCodeWriter(options.sourceDir, options);
                if (options.verbose)
                    cw = new ProgressCodeWriter(cw, out);
                options.getCodeModel().build(cw);
            } catch(AbortException e){
                //error might have been reported
                return false;
            }catch (IOException e) {
                receiver.error(e);
                return false;
            }catch (XMLStreamException e) {
                receiver.error(e);
                return false;
            }

            if (!options.nocompile){
                if(!compileGeneratedClasses(receiver, listener)){
                    listener.message(WscompileMessages.WSCOMPILE_COMPILATION_FAILED());
                    return false;
                }
            }
            try {
                if (options.clientjar != null) {
                    //add all the generated class files to the list of generated files
                    addClassesToGeneratedFiles();
                    jarArtifacts(listener);

                }
            } catch (IOException e) {
                receiver.error(e);
                return false;
            }
        } catch (Options.WeAreDone done) {
            usage(done.getOptions());
        } catch (BadCommandLineException e) {
            if (e.getMessage() != null) {
                System.out.println(e.getMessage());
                System.out.println();
            }
            usage(e.getOptions());
            return false;
        } finally{
            deleteGeneratedFiles();
            Authenticator.setDefault(orig);

        }
        if(receiver.hadError()) {
            return false;
        }
        return true;
    }

    private void deleteGeneratedFiles() {
        Set<File> trackedRootPackages = new HashSet<File>();

        if (options.clientjar != null) {
            //remove all non-java artifacts as they will packaged in jar.
            Iterable<File> generatedFiles = options.getGeneratedFiles();
            synchronized (generatedFiles) {
                for (File file : generatedFiles) {
                    if (!file.getName().endsWith(".java")) {
                        file.delete();
                        trackedRootPackages.add(file.getParentFile());

                    }

                }
            }
            //remove empty package dirs 
            for(File pkg:trackedRootPackages) {

                while(pkg.list() != null && pkg.list().length ==0 && !pkg.equals(options.destDir)) {
                    File parentPkg = pkg.getParentFile();
                    pkg.delete();
                    pkg = parentPkg;
                }
            }
        }
        if(!options.keep) {
            options.removeGeneratedFiles();
        }

    }

    private void addClassesToGeneratedFiles() throws IOException {
        Iterable<File> generatedFiles = options.getGeneratedFiles();
        final List<File> trackedClassFiles = new ArrayList<File>();
        for(File f: generatedFiles) {
            if(f.getName().endsWith(".java")) {
                String relativeDir = DirectoryUtil.getRelativePathfromCommonBase(f.getParentFile(),options.sourceDir);
                final String className = f.getName().substring(0,f.getName().indexOf(".java"));
                File classDir = new File(options.destDir,relativeDir);
                if(classDir.exists()) {
                    classDir.listFiles(new FilenameFilter() {

                        public boolean accept(File dir, String name) {
                            if(name.equals(className+".class") || (name.startsWith(className+"$") && name.endsWith(".class"))) {
                                trackedClassFiles.add(new File(dir,name));
                                return true;
                            }
                            return false;
                        }
                    });
                }
            }
        }
        for(File f: trackedClassFiles) {
            options.addGeneratedFile(f);
        }
    }

    private void jarArtifacts(WsimportListener listener) throws IOException {
        File zipFile = new File(options.clientjar);
        if(!zipFile.isAbsolute()) {
            zipFile = new File(options.destDir, options.clientjar);
        }

        if (zipFile.exists()) {
            //TODO
        }
        FileOutputStream fos = null;
        if( !options.quiet )
            listener.message(WscompileMessages.WSIMPORT_ARCHIVING_ARTIFACTS(zipFile));


        fos = new FileOutputStream(zipFile);
        JarOutputStream jos = new JarOutputStream(fos);

        String base = options.destDir.getCanonicalPath();
        for(File f: options.getGeneratedFiles()) {
            //exclude packaging the java files in the jar
            if(f.getName().endsWith(".java")) {
                continue;
            }
            if(options.verbose) {
                listener.message(WscompileMessages.WSIMPORT_ARCHIVE_ARTIFACT(f, options.clientjar));    
            }
            String entry = f.getCanonicalPath().substring(base.length()+1);
            BufferedInputStream bis = new BufferedInputStream(
                            new FileInputStream(f));
            JarEntry jarEntry = new JarEntry(entry);
            jos.putNextEntry(jarEntry);
            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = bis.read(buffer)) != -1) {
                jos.write(buffer, 0, bytesRead);
            }
            bis.close();

        }

        jos.close();

    }

    /*
    private void jarArtifacts() throws IOException {
        File zipFile = new File(options.clientjar);
        if(!zipFile.isAbsolute()) {
            zipFile = new File(options.destDir, options.clientjar);
        }

        if (zipFile.exists()) {
            //TODO
        }
        FileOutputStream fos = null;

        fos = new FileOutputStream(zipFile);
        JarOutputStream jos = new JarOutputStream(fos);
        addFileToJar(jos, options.destDir, "");
        jos.close();
    }

    private void addFileToJar(JarOutputStream jos, File file, String name) throws IOException {
        if(file.isDirectory()) {
           for(File f: file.listFiles()) {
               String entryName = name.equals("")?f.getName():name+"/"+f.getName();
               addFileToJar(jos,f,entryName);
           }
        } else {
            if(name.equals(options.clientjar)) {
                return;
            }
            BufferedInputStream bis = new BufferedInputStream(
                            new FileInputStream(file));
            JarEntry entry = new JarEntry(name);
            jos.putNextEntry(entry);
            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = bis.read(buffer)) != -1) {
                jos.write(buffer, 0, bytesRead);
            }
            bis.close();
        }
    }
    */
    public void setEntityResolver(EntityResolver resolver){
        this.options.entityResolver = resolver;
    }

    /*
     * To take care of JDK6-JDK6u3, where 2.1 API classes are not there
     */
    private static boolean useBootClasspath(Class clazz) {
        try {
            ParallelWorldClassLoader.toJarUrl(clazz.getResource('/'+clazz.getName().replace('.','/')+".class"));
            return true;
        } catch(Exception e) {
            return false;
        }
    }


    protected boolean compileGeneratedClasses(ErrorReceiver receiver, WsimportListener listener){
        List<String> sourceFiles = new ArrayList<String>();

        for (File f : options.getGeneratedFiles()) {
            if (f.exists() && f.getName().endsWith(".java")) {
                sourceFiles.add(f.getAbsolutePath());
            }
        }

        if (sourceFiles.size() > 0) {
            String classDir = options.destDir.getAbsolutePath();
            String classpathString = createClasspathString();
            boolean bootCP = useBootClasspath(EndpointContext.class) || useBootClasspath(JAXBPermission.class);
            String[] args = new String[4 + (bootCP ? 1 : 0) + (options.debug ? 1 : 0)
                    + sourceFiles.size()];
            args[0] = "-d";
            args[1] = classDir;
            args[2] = "-classpath";
            args[3] = classpathString;
            int baseIndex = 4;
            //javac is not working in osgi as the url starts with a bundle
            if (bootCP) {
                args[baseIndex++] = "-Xbootclasspath/p:"+JavaCompilerHelper.getJarFile(EndpointContext.class)+File.pathSeparator+JavaCompilerHelper.getJarFile(JAXBPermission.class);
            }

            if (options.debug) {
                args[baseIndex++] = "-g";
            }
            for (int i = 0; i < sourceFiles.size(); ++i) {
                args[baseIndex + i] = sourceFiles.get(i);
            }
            
            listener.message(WscompileMessages.WSIMPORT_COMPILING_CODE());
            if(options.verbose){
                StringBuffer argstr = new StringBuffer();
                for(String arg:args){
                    argstr.append(arg).append(" ");                    
                }
                listener.message("javac "+ argstr.toString());
            }

            return JavaCompilerHelper.compile(args, out, receiver);
        }
        //there are no files to compile, so return true?
        return true;
    }

    private String createClasspathString() {
        String classpathStr = System.getProperty("java.class.path");
        for(String s: options.cmdlineJars) {
            classpathStr = classpathStr+File.pathSeparator+new File(s);
        }
        return classpathStr;
    }

    protected void usage(Options options) {
        System.out.println(WscompileMessages.WSIMPORT_HELP(WSIMPORT));
        System.out.println(WscompileMessages.WSIMPORT_USAGE_EXTENSIONS());
        System.out.println(WscompileMessages.WSIMPORT_USAGE_EXAMPLES());
    }
}
