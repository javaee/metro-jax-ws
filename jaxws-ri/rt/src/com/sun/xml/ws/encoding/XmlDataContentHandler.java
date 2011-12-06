/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.encoding;

import com.sun.xml.ws.util.xml.XmlUtil;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * JAF data handler for XML content
 *
 * @author Jitendra Kotamraju
 */
public class XmlDataContentHandler implements DataContentHandler {

    private final DataFlavor[] flavors;

    public XmlDataContentHandler() throws ClassNotFoundException {
        flavors = new DataFlavor[3];
        flavors[0] = new ActivationDataFlavor(StreamSource.class, "text/xml", "XML");
        flavors[1] = new ActivationDataFlavor(StreamSource.class, "application/xml", "XML");
        flavors[2] = new ActivationDataFlavor(String.class, "text/xml", "XML String");
    }

    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public Object getTransferData(DataFlavor df, DataSource ds)
        throws IOException {

        for (DataFlavor aFlavor : flavors) {
            if (aFlavor.equals(df)) {
                return getContent(ds);
            }
        }
        return null;
    }

    /**
     * Create an object from the input stream
     */
    public Object getContent(DataSource ds) throws IOException {
        String ctStr = ds.getContentType();
        String charset = null;
        if (ctStr != null) {
            ContentType ct = new ContentType(ctStr);
            if (!isXml(ct)) {
                throw new IOException(
                    "Cannot convert DataSource with content type \""
                            + ctStr + "\" to object in XmlDataContentHandler");
            }
            charset = ct.getParameter("charset");
        }
        return (charset != null)
                ? new StreamSource(new InputStreamReader(ds.getInputStream()), charset)
                : new StreamSource(ds.getInputStream());
    }

    /**
     * Convert the object to a byte stream
     */
    public void writeTo(Object obj, String mimeType, OutputStream os)
        throws IOException {

        if (!(obj instanceof DataSource || obj instanceof Source || obj instanceof String)) {
             throw new IOException("Invalid Object type = "+obj.getClass()+
                ". XmlDataContentHandler can only convert DataSource|Source|String to XML.");
        }

        ContentType ct = new ContentType(mimeType);
        if (!isXml(ct)) {
            throw new IOException(
                "Invalid content type \"" + mimeType + "\" for XmlDataContentHandler");
        }

        String charset = ct.getParameter("charset");
        if (obj instanceof String) {
            String s = (String) obj;
            if (charset == null) {
                charset = "utf-8";
            }
            OutputStreamWriter osw = new OutputStreamWriter(os, charset);
            osw.write(s, 0, s.length());
            osw.flush();
            return;
        }

        Source source = (obj instanceof DataSource)
                ? (Source)getContent((DataSource)obj) : (Source)obj;
        try {
            Transformer transformer = XmlUtil.newTransformer();
            if (charset != null) {
                transformer.setOutputProperty(OutputKeys.ENCODING, charset);
            }
            StreamResult result = new StreamResult(os);
            transformer.transform(source, result);
        } catch (Exception ex) {
            throw new IOException(
                "Unable to run the JAXP transformer in XmlDataContentHandler "
                    + ex.getMessage());
        }
    }

    private boolean isXml(ContentType ct) {
        return ct.getSubType().equals("xml") &&
                    (ct.getPrimaryType().equals("text") || ct.getPrimaryType().equals("application"));
    }

}

