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
