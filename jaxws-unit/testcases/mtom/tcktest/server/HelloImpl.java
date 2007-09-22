package mtom.tcktest.server;


import com.sun.xml.ws.developer.JAXWSProperties;

import javax.xml.ws.WebServiceException;
import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

/**
 * @author Jitendra Kotamraju
 */
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

}
