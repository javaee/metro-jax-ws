/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
import com.sun.tools.ws.resources.WscompileMessages;
import com.sun.tools.ws.resources.WsdlMessages;
import com.sun.tools.xjc.util.NullStream;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.util.ServiceFinder;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXParseException;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.EndpointReference;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

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
        };

        for (String arg : args) {
            if (arg.equals("-version")) {
                listener.message(ToolVersion.VERSION.BUILD_VERSION);
                return true;
            }
        }
        try {
            options.parseArguments(args);
            options.validate();
            if(options.debugMode)
                listener.enableDebugging();
            options.parseBindings(receiver);

            try {
                if( !options.quiet )
                    listener.message(WscompileMessages.WSIMPORT_PARSING_WSDL());

                WSDLModeler wsdlModeler = new WSDLModeler(options, receiver);
                Model wsdlModel = wsdlModeler.buildModel();
                if (wsdlModel == null) {
                    listener.message(WsdlMessages.PARSING_PARSE_FAILED());
                    return false;
                }

                //generated code
                if( !options.quiet )
                    listener.message(WscompileMessages.WSIMPORT_GENERATING_CODE());
                
                TJavaGeneratorExtension[] genExtn = ServiceFinder.find(TJavaGeneratorExtension.class).toArray();
                CustomExceptionGenerator.generate(wsdlModel,  options, receiver);
                SeiGenerator.generate(wsdlModel, options, receiver, genExtn);
                ServiceGenerator.generate(wsdlModel, options, receiver);
                CodeWriter cw = new WSCodeWriter(options.sourceDir, options);
                if (options.verbose)
                    cw = new ProgressCodeWriter(cw, System.out);
                options.getCodeModel().build(cw);
            } catch(AbortException e){
                //error might have been reported
            }catch (IOException e) {
                receiver.error(e);
            }

            if (!options.nocompile){
                if(!compileGeneratedClasses(receiver, listener)){
                    listener.message(WscompileMessages.WSCOMPILE_COMPILATION_FAILED());
                    return false;
                }
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
            if(!options.keep){
                options.removeGeneratedFiles();
            }
        }
        return true;
    }

    public void setEntityResolver(EntityResolver resolver){
        this.options.entityResolver = resolver;
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
            String[] args = new String[5 + (options.debug ? 1 : 0)
                    + sourceFiles.size()];
            args[0] = "-d";
            args[1] = classDir;
            args[2] = "-classpath";
            args[3] = classpathString;
            args[4] = "-Xbootclasspath/p:"+JavaCompilerHelper.getJarFile(EndpointReference.class)+File.pathSeparator+JavaCompilerHelper.getJarFile(XmlSeeAlso.class);
            int baseIndex = 5;
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
        return System.getProperty("java.class.path");
    }

    protected void usage(Options options) {
        System.out.println(WscompileMessages.WSIMPORT_HELP(WSIMPORT));
        System.out.println(WscompileMessages.WSIMPORT_USAGE_EXAMPLES());
    }
}
