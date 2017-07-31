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

package com.sun.tools.ws.processor.modeler.wsdl;

import com.sun.tools.ws.resources.WscompileMessages;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import org.xml.sax.SAXParseException;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;

public class ConsoleErrorReporter extends ErrorReceiver {

    private boolean hasError;
    private PrintStream output;
    private boolean debug;

    public ConsoleErrorReporter(PrintStream stream) {
        this.output = stream;
    }

    public ConsoleErrorReporter(OutputStream outputStream) {
        this.output = new PrintStream(outputStream);
    }

    public boolean hasError() {
        return hasError;
    }

    public void error(SAXParseException e) {
        if(debug)
            e.printStackTrace();
        hasError = true;
        if((e.getSystemId() == null && e.getPublicId() == null) && (e.getCause() instanceof UnknownHostException)) {
            print(WscompileMessages.WSIMPORT_ERROR_MESSAGE(e.toString()), e);
        } else {
            print(WscompileMessages.WSIMPORT_ERROR_MESSAGE(e.getMessage()), e);
        }
    }



    public void fatalError(SAXParseException e) {
        if(debug)
            e.printStackTrace();
        hasError = true;
        print(WscompileMessages.WSIMPORT_ERROR_MESSAGE(e.getMessage()), e);
    }

    public void warning(SAXParseException e) {
        print(WscompileMessages.WSIMPORT_WARNING_MESSAGE(e.getMessage()), e);
    }
    
    /**
     * Used to report possibly verbose information that
     * can be safely ignored.
     */
    public void info(SAXParseException e) {
        print(WscompileMessages.WSIMPORT_INFO_MESSAGE(e.getMessage()), e);
    }

    public void debug(SAXParseException e){
        print(WscompileMessages.WSIMPORT_DEBUG_MESSAGE(e.getMessage()), e);
    }


    private void print(String message, SAXParseException e) {
        output.println(message);
        output.println(getLocationString(e));
        output.println();
    }

    public void enableDebugging(){
        this.debug = true;
    }

}
