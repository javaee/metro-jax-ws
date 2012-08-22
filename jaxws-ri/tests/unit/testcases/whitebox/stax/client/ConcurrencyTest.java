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

package whitebox.stax.client;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;

import junit.framework.TestCase;

import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;

/**
 * @author Jitendra Kotamraju
 */
public class ConcurrencyTest extends TestCase {

    private static final int NO_THREADS = 10;
    private static final int PER_THREAD = 5000;

    public void test() throws Exception {
        Thread[] threads = new Thread[NO_THREADS];
        MyRunnable[] run = new MyRunnable[NO_THREADS];
        for (int i = 0; i < NO_THREADS; i++) {
            run[i] = new MyRunnable(i);
            threads[i] = new Thread(run[i]);
        }
        for (int i = 0; i < NO_THREADS; i++) {
            threads[i].start();
        }
        for (int i = 0; i < NO_THREADS; i++) {
            threads[i].join();
        }
        for (int i = 0; i < NO_THREADS; i++) {
            Exception e = run[i].getException();
            if (e != null) {
                e.printStackTrace();
                fail("Failed with exception."+e);
            }
        }
    }

    private static class MyRunnable implements Runnable {
        final int no;
        volatile Exception e;

        MyRunnable(int no) {
            this.no = no;
        }

        public void run() {
            for(int req=0; req < PER_THREAD && e == null; req++) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    XMLStreamWriter w = getWriter(bos);
                    //System.out.println("Writer="+w+" Thread="+Thread.currentThread());
                    w.writeStartDocument();
                    w.writeStartElement("hello");
                    for (int j = 0; j < 50; j++) {
                        w.writeStartElement("a" + j);
                        w.writeEndElement();
                    }
                    w.writeEndElement();
                    w.writeEndDocument();
                    w.close();
                    bos.close();

                    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                    XMLStreamReader r = getReader(bis);
                    while (r.hasNext()) {
                        r.next();
                    }
                    r.close();
                    bis.close();
                } catch (Exception e) {
                    this.e = e;
                }
            }
        }

        public Exception getException() {
            return e;
        }
    }

    private static XMLStreamReader getReader(InputStream is)
            throws Exception {
        return XMLStreamReaderFactory.create(null, is, true);
    }

    private static XMLStreamWriter getWriter(OutputStream os)
            throws Exception {
        return XMLStreamWriterFactory.create(os);
    }

}
