/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import com.sun.istack.NotNull;
import com.sun.tools.ws.resources.WscompileMessages;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class DefaultAuthenticator extends Authenticator {

    private final List<AuthInfo> authInfo = new ArrayList<AuthInfo>();
    private final ErrorReceiver errReceiver;
    private final String proxyUser;
    private final String proxyPasswd;

    //can user.home value be null?
    public static final String defaultAuthfile = System.getProperty("user.home")+ System.getProperty("file.separator")+".metro"+System.getProperty("file.separator")+"auth";

    public DefaultAuthenticator(@NotNull ErrorReceiver receiver, @NotNull File authfile) throws BadCommandLineException {
        this.errReceiver = receiver;
        this.proxyUser = System.getProperty("http.proxyUser");
        this.proxyPasswd = System.getProperty("http.proxyPassword");

        if(authfile != null)
            parseAuth(authfile);
        else
            parseAuth();
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        //If user sets proxy user and passwd and the RequestType is from proxy server then create
        // PasswordAuthentication using proxyUser and proxyClass;
        if((getRequestorType() == RequestorType.PROXY) && proxyUser != null && proxyPasswd != null){
            return new PasswordAuthentication(proxyUser, proxyPasswd.toCharArray());
        }
        for(AuthInfo auth:authInfo){
            if(auth.matchingHost(getRequestingURL())){
                return new PasswordAuthentication(auth.getUser(), auth.getPassword().toCharArray());
            }
        }
        return null;
    }

    private void parseAuth(File authfile) {
        errReceiver.info(new SAXParseException(WscompileMessages.WSIMPORT_READING_AUTH_FILE(authfile), null));

        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(authfile), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            errReceiver.error(new SAXParseException(e.getMessage(), null));
            return;
        } catch (FileNotFoundException e) {
            errReceiver.error(new SAXParseException(WscompileMessages.WSIMPORT_AUTH_FILE_NOT_FOUND(authfile, defaultAuthfile), null, e));
            return;
        }
        String text;
        LocatorImpl locator = new LocatorImpl();
        try {
            int lineno = 1;

            locator.setSystemId(authfile.getCanonicalPath());

            while ((text = in.readLine()) != null) {
                locator.setLineNumber(lineno);
                try {
                    URL url = new URL(text);
                    String authinfo = url.getUserInfo();

                    if (authinfo != null) {
                        int i = authinfo.indexOf(':');

                        if (i >= 0) {
                            String user = authinfo.substring(0, i);
                            String password = authinfo.substring(i + 1);
                            authInfo.add(new AuthInfo(new URL(text), user, password));
                        } else {
                            errReceiver.error(new SAXParseException(WscompileMessages.WSIMPORT_ILLEGAL_AUTH_INFO(url), locator));
                        }
                    } else {
                        errReceiver.error(new SAXParseException(WscompileMessages.WSIMPORT_ILLEGAL_AUTH_INFO(url), locator));
                    }

                } catch (NumberFormatException e) {
                    errReceiver.error(new SAXParseException(WscompileMessages.WSIMPORT_ILLEGAL_AUTH_INFO(text), locator));
                }
            }
            in.close();
        } catch (IOException e) {
            errReceiver.error(new SAXParseException(WscompileMessages.WSIMPORT_FAILED_TO_PARSE(authfile,e.getMessage()), locator));
        }
    }

    private void parseAuth() {
        File auth = new File(defaultAuthfile);

        if(!auth.exists()){
            errReceiver.debug(new SAXParseException(WscompileMessages.WSIMPORT_AUTH_FILE_NOT_FOUND(defaultAuthfile, defaultAuthfile), null));
            return;
        }

        if(!auth.canRead()){
            errReceiver.debug(new SAXParseException("Authorization file: "+auth + " does not have read permission!", null));
            return;
        }
        parseAuth(auth);
    }
}
