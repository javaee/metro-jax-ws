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

package mtom_soap12.server;


import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@MTOM
@WebService (endpointInterface = "mtom_soap12.server.MtomSample")

public class MtomSampleImpl implements MtomSample {

    public String upload(Image data) {
        if(data != null)
            return "Success";
        throw new WebServiceException("No image Received!");
    }

    /**
     * Send data as attachment in streaming fashion
     */
    public DataHandler download(int size) {
        return getDataHandler(size);
    }

    /**
     * Streaming data handler
     */
    private static DataHandler getDataHandler(final int total)  {
        return new DataHandler(new DataSource() {
            public InputStream getInputStream() throws IOException {
                return new InputStream() {
                    int i;

                    @Override
                    public int read() throws IOException {
                        return i<total ? 'A'+(i++%26) : -1;
                    }
                };
            }

            public OutputStream getOutputStream() throws IOException {
                return null;
            }

            public String getContentType() {
                return "application/octet-stream";
            }

            public String getName() {
                return "";
            }
        });
    }
}
