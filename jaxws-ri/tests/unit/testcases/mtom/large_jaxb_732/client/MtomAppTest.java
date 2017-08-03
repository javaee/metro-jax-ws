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

package mtom.large_jaxb_732.client;

import junit.framework.TestCase;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.MTOMFeature;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Closeable;
import java.util.Map;

/**
 * @author Jitendra Kotamraju
 */
public class MtomAppTest extends TestCase {

    public void testUpload() throws Exception {
        int size = 123*1000 *1000;
        Hello port = new HelloService().getHelloPort(new MTOMFeature());
        Map<String, Object> ctxt = ((BindingProvider)port).getRequestContext();
        // At present, JDK internal property - not supported
        ctxt.put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", 8192); 
        // Add this one to run with standalone RI bits
        ctxt.put("com.sun.xml.ws.transport.http.client.streaming.chunk.size", 8192); 

        Holder<Integer> total = new Holder<Integer>(size);
        Holder<String> name = new Holder<String>("huge");
        Holder<DataHandler> dh = new Holder<DataHandler>(getDataHandler(total.value));
        port.upload(total, name, dh);
        if (!"hugehuge".equals(name.value)) {
           fail("FAIL: Expecting: hugehuge Got: "+name.value);
        }
        if (!total.value.equals(size+1)) {
           fail("FAIL: Expecting size: "+(size+1)+" Got: "+total.value);
        }
        System.out.println("SUCCESS: Got: "+name.value);
        System.out.println("Going to verify DataHandler. This would take some time");
        validateDataHandler(total.value, dh.value);
        System.out.println("SUCCESS: DataHandler is verified");
        if (dh.value instanceof Closeable) {
            System.out.println("Client:Received DH is closeable");
            ((Closeable)dh.value).close();
        }
    }

    private DataHandler getDataHandler(final int total)  {
        return new DataHandler(new DataSource() {
            public InputStream getInputStream() throws IOException {
                return new InputStream() {
                    int i;

                    @Override
                    public int read() throws IOException {
                        return i<total ? i++%256 : -1;
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

    private void validateDataHandler(int expTotal, DataHandler dh)
		throws IOException {
        InputStream in = dh.getInputStream();
        int ch;
        int total = 0;
        while((ch=in.read()) != -1) {
            if (total++%256 != ch) {
                fail("Client:FAIL: DataHandler data is different");
            }
            if (total%(10*1000*1000) == 0) {
                System.out.println("Client: Received="+total); 
            }
        }
        in.close();
        if (total != expTotal) {
            fail("Client:FAIL: DataHandler data is different. Expecting "+expTotal+" but got "+total+" bytes");
        }
    }

}
