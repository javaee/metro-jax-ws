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
package bugs.wsit1533.client;

import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.StreamingDataHandler;
import com.sun.xml.ws.encoding.DataSourceStreamingDataHandler;
import junit.framework.TestCase;
import org.apache.tools.ant.util.ReaderInputStream;

import javax.activation.DataSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Map;

/**
 * Testcase created from issue WSIT-1533; Issue already fixed at the time of testing
 *
 * @author Miroslav Kos (miroslav.kos at oracle.com)
 */
public class UploadTest extends TestCase {

     private UploadServicePortType upload;

     public void setUp() throws Exception {
          upload = new UploadService().getUpload(new MTOMFeature());
          Map<String, Object> ctxt = ((BindingProvider) upload).getRequestContext();
          // Enable HTTP chunking mode, otherwise HttpURLConnection buffers
          ctxt.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
     }

     public void doBigUpload() throws Exception {

         StringBuilder sb = new StringBuilder();
         for(int i = 0; i < 100000; i++) {
             sb.append("some HUGE data ");
         }

         doUpload(sb.toString());
     }

    private void doUpload(String singleLineMsg) {
        StreamingDataHandler data = wrapStringData(singleLineMsg);
        final String line = upload.uploadDataTest(data);
        assertEquals(line, singleLineMsg);
    }

    public void testUploads() throws Exception {

         //this should be under the MTOM-treshold of 512
        doUpload("Small");

        doBigUpload();

        doUpload("Small");
        doUpload("Small");

        doBigUpload();
    }

     private StreamingDataHandler wrapStringData(final String msg) {

          return new DataSourceStreamingDataHandler(
                  new DataSource() {

                       @Override
                       public OutputStream getOutputStream() throws IOException {
                            throw new IOException("no outputstream provided");
                       }

                       @Override
                       public String getName() {
                            return "testdata";
                       }

                       @Override
                       public InputStream getInputStream() throws IOException {
                            return new ReaderInputStream(new StringReader(msg));
                       }

                       @Override
                       public String getContentType() {
                            return "text/plain; charset=utf-8";
                       }
                  }
          );
     }
}
