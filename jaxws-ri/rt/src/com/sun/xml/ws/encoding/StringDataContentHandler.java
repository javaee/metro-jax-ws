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
package com.sun.xml.ws.encoding;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataSource;
import javax.activation.DataContentHandler;
import java.awt.datatransfer.DataFlavor;
import java.io.*;

/**
 * JAF data content handler for text/plain --> String
 *
 * @author Anil Vijendran
 */
public class StringDataContentHandler implements DataContentHandler {

    /**
     * return the DataFlavors for this <code>DataContentHandler</code>
     * @return The DataFlavors.
     */
    public DataFlavor[] getTransferDataFlavors() { // throws Exception;
        DataFlavor flavors[] = new DataFlavor[2];
        flavors[0] =  new ActivationDataFlavor(String.class, "text/plain", "text string");
        flavors[1] = new DataFlavor("text/plain", "Plain Text");
        return flavors;
    }

    /**
     * return the Transfer Data of type DataFlavor from InputStream
     * @param df The DataFlavor.
     * @param ds The InputStream corresponding to the data.
     * @return The constructed Object.
     */
    public Object getTransferData(DataFlavor df, DataSource ds) {

        // this is sort of hacky, but will work for the
        // sake of testing...
        if (df.getMimeType().startsWith("text/plain")) {
            if (df
                .getRepresentationClass()
                .getName()
                .equals("java.lang.String")) {
                // spit out String
                StringBuffer buf = new StringBuffer();
                char data[] = new char[1024];
                // InputStream is = null;
                InputStreamReader isr = null;
                int bytes_read = 0;
                int total_bytes = 0;

                try {
                    isr = new InputStreamReader(ds.getInputStream());

                    while (true) {
                        bytes_read = isr.read(data);
                        if (bytes_read > 0)
                            buf.append(data, 0, bytes_read);
                        else
                            break;
                        total_bytes += bytes_read;
                    }
                } catch (Exception e) {
                }

                return buf.toString();

            } else if (
                df.getRepresentationClass().getName().equals(
                    "java.io.InputStream")) {
                // spit out InputStream
                try {
                    return ds.getInputStream();
                } catch (Exception e) {
                }
            }

        }
        return null;
    }

    /**
     *
     */
    public Object getContent(DataSource ds) { // throws Exception;
        StringBuffer buf = new StringBuffer();
        char data[] = new char[1024];
        // InputStream is = null;
        InputStreamReader isr = null;
        int bytes_read = 0;
        int total_bytes = 0;

        try {
            isr = new InputStreamReader(ds.getInputStream());

            while (true) {
                bytes_read = isr.read(data);
                if (bytes_read > 0)
                    buf.append(data, 0, bytes_read);
                else
                    break;
                total_bytes += bytes_read;
            }
        } catch (Exception e) {
        }

        return buf.toString();
    }
    /**
     * construct an object from a byte stream
     * (similar semantically to previous method, we are deciding
     *  which one to support)
     */
    public void writeTo(Object obj, String mimeType, OutputStream os)
        throws IOException {
        if (!mimeType.startsWith("text/plain"))
            throw new IOException(
                "Invalid type \"" + mimeType + "\" on StringDCH");

        Writer out = new OutputStreamWriter(os);
        out.write((String) obj);
        out.flush();
    }

}
