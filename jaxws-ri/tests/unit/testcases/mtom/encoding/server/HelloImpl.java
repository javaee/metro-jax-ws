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

package mtom.encoding.server;

import javax.xml.ws.WebServiceException;
import javax.activation.*;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.ws.soap.MTOM;

/**
 * @author Jitendra Kotamraju
 */
@MTOM
@WebService(endpointInterface = "mtom.encoding.server.Hello")
public class HelloImpl {

    public String mtomIn(DataType data) {
        Source source1 = data.getDoc1();
        try {
            getStringFromSource(source1);
        } catch(Exception e) {
            throw new WebServiceException("Incorrect Source source1", e);
        }

        Source source2 = data.getDoc2();
        try {
            getStringFromSource(source2);
        } catch(Exception e) {
            throw new WebServiceException("Incorrect Source source1", e);
        }

        DataHandler dh = data.getDoc3();

        Image image = data.getDoc4();
        if (image == null) {
            throw new WebServiceException("Received Image is null");
        }

        return "works";
    }

    public void mtomInOut(
        Holder<DataHandler> doc1,
        Holder<DataHandler> doc2,
        Holder<DataHandler> doc3,
        Holder<Image> doc4,
        Holder<Image> doc5) {

        try {
            DataHandler dh = getDataHandler(doc1.value);
        	closeDataHandler(doc1.value);
            doc1.value = dh;

            dh = getDataHandler(doc2.value);
        	closeDataHandler(doc2.value);
            doc2.value = dh;

            dh = getDataHandler(doc3.value);
        	closeDataHandler(doc3.value);
            doc3.value = dh;
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }

    private String getStringFromSource(Source source) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult sr = new StreamResult(bos );
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        Properties oprops = new Properties();
        oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperties(oprops);
        trans.transform(source, sr);
        bos.flush();
        return bos.toString();
    }

    private void closeDataHandler(DataHandler dh) throws Exception {
        if (dh instanceof Closeable) {
            ((Closeable)dh).close();
        }
    }

    private DataHandler getDataHandler(DataHandler dh) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buf[] = new byte[4096];
        int len;
        InputStream in = dh.getInputStream();
        while((len=in.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        in.close();
        final String ct = dh.getContentType();
        final InputStream bin = new ByteArrayInputStream(baos.toByteArray());
        return new DataHandler(new DataSource() {
            public String getContentType() {
                return ct;
            }

            public InputStream getInputStream() {
                return bin;
            }

            public String getName() {
                return null;
            }

            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException();
            }
        });
    }

/*
    private void verify() {
        MessageContext ctxt = wsc.getMessageContext();
        Map<String, List<String>> hdrs = (Map<String, List<String>>)ctxt.get(MessageContext.HTTP_REQUEST_HEADERS);
        List<String> ctList = hdrs.get("Content-Type");
        if (ctList == null || ctList.size() != 1) {
            throw new WebServiceException("Invalid Content-Type header="+ctList);
        }
        String ct = ctList.get(0);
        if (!(ct.contains("UTF-16") || ct.contains("utf-16"))) {
            throw new WebServiceException("Invalid Encoding for request. Content-Type header="+ctList);
        }
    }
*/

}
