package fromwsdl.xmime_501.server;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.jws.WebService;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * @author Jitendra Kotamraju
 */
@WebService(endpointInterface = "fromwsdl.xmime_501.server.Hello")
public class HelloImpl {

    public DataHandler mtomInOut(Source doc1) {
        try {
            return getDataHandlerFromSource(doc1);
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    private DataHandler getDataHandlerFromSource(Source source) throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult sr = new StreamResult(bos );
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        Properties oprops = new Properties();
        oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperties(oprops);
        trans.transform(source, sr);
        return new DataHandler(new DataSource() {
            public String getContentType() {
                return "text/xml";
            }

            public InputStream getInputStream() {
                return new ByteArrayInputStream(bos.toByteArray());
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
