package com.sun.xml.ws.encoding;

import com.sun.xml.ws.util.xml.XmlUtil;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.OutputStream;


/**
 * JAF data handler for XML content
 *
 * @author Anil Vijendran
 */
public class XmlDataContentHandler implements DataContentHandler {

    private final DataFlavor[] flavors;

    public XmlDataContentHandler() throws ClassNotFoundException {
        flavors = new DataFlavor[2];
        flavors[0] = new ActivationDataFlavor(StreamSource.class, "text/xml", "XML");
        flavors[1] = new ActivationDataFlavor(StreamSource.class, "application/xml", "XML");
    }

    /**
     * return the DataFlavors for this <code>DataContentHandler</code>
     * @return The DataFlavors.
     */
    public DataFlavor[] getTransferDataFlavors() { // throws Exception;
        return flavors;
    }

    /**
     * return the Transfer Data of type DataFlavor from InputStream
     * @param df The DataFlavor.
     * @param ds The InputStream corresponding to the data.
     * @return The constructed Object.
     */
    public Object getTransferData(DataFlavor df, DataSource ds)
        throws IOException {

        for (DataFlavor aFlavor : flavors) {
            if (aFlavor.equals(df)) {
                return getContent(ds);
            }
        }
        return null;
    }

    /**
     *
     */
    public Object getContent(DataSource dataSource) throws IOException {
        return new StreamSource(dataSource.getInputStream());
    }

    /**
     * construct an object from a byte stream
     * (similar semantically to previous method, we are deciding
     *  which one to support)
     */
    public void writeTo(Object obj, String mimeType, OutputStream os)
        throws IOException {
        if (!mimeType.equals("text/xml") && !mimeType.equals("application/xml"))
            throw new IOException(
                "Invalid content type \"" + mimeType + "\" for XmlDCH");

        try {
            Transformer transformer = XmlUtil.newTransformer();
            StreamResult result = new StreamResult(os);
            if (obj instanceof DataSource) {
                // Streaming transform applies only to javax.xml.transform.StreamSource
                transformer.transform((Source) getContent((DataSource)obj), result);
            } else {
                transformer.transform((Source) obj, result);
            }
        } catch (Exception ex) {
            throw new IOException(
                "Unable to run the JAXP transformer on a stream "
                    + ex.getMessage());
        }
    }
}

