package mtom.tcktest.server;

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
@WebService(endpointInterface = "mtom.tcktest.server.Hello")

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

}
