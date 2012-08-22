/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.message;

import com.sun.xml.ws.api.message.Message;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.transform.sax.SAXSource;

/**
 * @author Kohsuke Kawaguchi
 */
final class XMLReaderImpl extends XMLFilterImpl {

    private final Message msg;

    XMLReaderImpl(Message msg) {
        this.msg = msg;
    }

    public void parse(String systemId) {
        reportError();
    }

    private void reportError() {
        // TODO: i18n
        throw new IllegalStateException(
            "This is a special XMLReader implementation that only works with the InputSource given in SAXSource.");
    }

    public void parse(InputSource input) throws SAXException {
        if(input!=THE_SOURCE)
            reportError();
        msg.writeTo(this,this);
    }

    @Override
    public ContentHandler getContentHandler() {
        if(super.getContentHandler()==DUMMY)   return null;
        return super.getContentHandler();
    }

    @Override
    public void setContentHandler(ContentHandler contentHandler) {
        if(contentHandler==null)    contentHandler = DUMMY;
        super.setContentHandler(contentHandler);
    }

    private static final ContentHandler DUMMY = new DefaultHandler();

    /**
     * Special {@link InputSource} instance that we use to pass to {@link SAXSource}.
     */
    protected static final InputSource THE_SOURCE = new InputSource();
}
