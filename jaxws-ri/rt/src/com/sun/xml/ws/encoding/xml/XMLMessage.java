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

package com.sun.xml.ws.encoding.xml;

import com.sun.xml.messaging.saaj.packaging.mime.MessagingException;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ContentType;
import com.sun.xml.messaging.saaj.packaging.mime.internet.InternetHeaders;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.messaging.saaj.util.ByteOutputStream;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.protocol.xml.XMLMessageException;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.FastInfosetReflection;
import com.sun.xml.ws.util.FastInfosetUtil;
import com.sun.xml.ws.util.xml.XmlUtil;

import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.soap.MimeHeaders;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 *
 * @author WS Developement Team
 */
public final class XMLMessage {

    private static final Logger log = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".protocol.xml");

    private static final int PLAIN_XML_FLAG      = 1;       // 00001
    private static final int MIME_MULTIPART_FLAG = 2;       // 00010
    private static final int FI_ENCODED_FLAG     = 16;      // 10000

    protected MimeHeaders headers;

    private final DataRepresentation data;

    /**
     * Indicates when Fast Infoset should be used to serialize
     * this message.
     */
    protected boolean useFastInfoset = false;

    protected boolean noData = false;

    /**
     * Construct a message from an input stream. When messages are
     * received, there's two parts -- the transport headers and the
     * message content in a transport specific stream.
     */
    public XMLMessage(MimeHeaders headers, final InputStream in) {
        this.headers = headers;
        final String ct;

        if (headers != null) {
            ct = getContentType(headers);
        }
        else {
            throw new XMLMessageException("xml.null.headers");
        }

        if (ct == null) {
            throw new XMLMessageException("xml.no.Content-Type");
        }

        try {
            ContentType contentType = new ContentType(ct);

            // In the absence of type attribute we assume it to be text/xml.
            // That would mean we're easy on accepting the message and
            // generate the correct thing
            if (contentType.getParameter("type") == null) {
                contentType.setParameter("type", "text/xml");
            }

            int contentTypeId = identifyContentType(contentType);
            boolean isFastInfoset = (contentTypeId & FI_ENCODED_FLAG) > 0;

            if ((contentTypeId & PLAIN_XML_FLAG) != 0) {
                data = new XMLSource(in, isFastInfoset);
            }
            else if ((contentTypeId & MIME_MULTIPART_FLAG) != 0) {
                data = new XMLDataSource(ct, in, isFastInfoset);
            }
            else {
                throw new XMLMessageException("xml.unknown.Content-Type");
            }
        } catch (Exception ex) {
            throw new XMLMessageException("xml.cannot.internalize.message",ex);
        }
    }

    public XMLMessage(Source source, boolean useFastInfoset) {
        if (source == null)
           this.noData = true;
        this.data = new XMLSource(source);
        this.headers = new MimeHeaders();
        this.useFastInfoset = useFastInfoset;
        headers.addHeader("Content-Type",
            useFastInfoset ? "application/fastinfoset" : "text/xml");
    }

    public XMLMessage(Exception err, boolean useFastInfoset) {
        this.data = new XMLErr(err);
        this.headers = new MimeHeaders();
        this.useFastInfoset = useFastInfoset;
        headers.addHeader("Content-Type",
            useFastInfoset ? "application/fastinfoset" : "text/xml");
    }

    public XMLMessage(DataSource dataSource, boolean useFastInfoset) {
        if (dataSource == null)
            this.noData = true;

        String contentType = dataSource.getContentType();
        this.data = new XMLDataSource(dataSource,
            contentType.indexOf("application/fastinfoset") > 0);
        this.headers = new MimeHeaders();
        this.useFastInfoset = useFastInfoset;
        headers.addHeader("Content-Type",
            !useFastInfoset ? contentType
                : contentType.replaceFirst("text/xml", "application/fastinfoset"));
    }

    public XMLMessage(Object object, JAXBContext context, boolean useFastInfoset) {
        if (object == null)
            this.noData = true;

        this.data = new XMLJaxb(object, context);
        this.headers = new MimeHeaders();
        this.useFastInfoset = useFastInfoset;
        headers.addHeader("Content-Type",
            useFastInfoset ? "application/fastinfoset" : "text/xml");
    }

    /**
     * Returns true if the underlying encoding of this message is FI.
     */
    public boolean isFastInfoset() {
        return data.isFastInfoset();
    }

     /**
     * Returns true if the underlying encoding of this message is FI.
     */
    public boolean hasNoData() {
        return this.noData;
    }
    /**
     * Returns true if the sender of this message accepts FI. Slow, but
     * should only be called once.
     */
    public boolean acceptFastInfoset() {
        return FastInfosetUtil.isFastInfosetAccepted(headers.getHeader("Accept"));
    }

    public Source getSource() {
        return data.getSource();
    }

    public DataSource getDataSource() {
        return data.getDataSource();
    }

    /**
     * Verify a contentType.
     *
     * @return PLAIN_XML_CODE for plain XML and MIME_MULTIPART_CODE for MIME multipart
     */
    private static int identifyContentType(ContentType contentType) {

        String primary = contentType.getPrimaryType();
        String sub = contentType.getSubType();

        if (primary.equalsIgnoreCase("multipart")) {
            if (sub.equalsIgnoreCase("related")) {
                String type = contentType.getParameter("type");
                if (isXMLType(type)) {
                    return MIME_MULTIPART_FLAG;
                }
                else if (isFastInfosetType(type)) {
                    return MIME_MULTIPART_FLAG | FI_ENCODED_FLAG;
                }
                else {
                    throw new XMLMessageException(
                            "xml.content-type.mustbe.multipart");
                }
            }
            else {
                throw new XMLMessageException("xml.invalid.content-type",
                        new Object[] { primary+"/"+sub } );
            }
        }
        else if (isXMLType(primary, sub)) {
            return PLAIN_XML_FLAG;
        }
        else if (isFastInfosetType(primary, sub)) {
            return PLAIN_XML_FLAG | FI_ENCODED_FLAG;
        }
        else {
            throw new XMLMessageException("xml.invalid.content-type",
                    new Object[] { primary+"/"+sub } );
        }
    }

    protected static boolean isXMLType(String primary, String sub) {
        return primary.equalsIgnoreCase("text") && sub.equalsIgnoreCase("xml");
    }

    protected static boolean isXMLType(String type) {
        return type.toLowerCase().startsWith("text/xml");
    }

    protected static boolean isFastInfosetType(String primary, String sub) {
        return primary.equalsIgnoreCase("application") && sub.equalsIgnoreCase("fastinfoset");
    }

    protected static boolean isFastInfosetType(String type) {
        return type.toLowerCase().startsWith("application/fastinfoset");
    }

    public MimeHeaders getMimeHeaders() {
        return this.headers;
    }

    private static String getContentType(MimeHeaders headers) {
        String[] values = headers.getHeader("Content-Type");
        return (values == null) ? null : values[0];
    }

    /*
     * Get the complete ContentType value along with optional parameters.
     */
    public String getContentType() {
        return getContentType(this.headers);
    }

    public void setContentType(String type) {
        headers.setHeader("Content-Type", type);
    }

//    private ContentType ContentType() {
//        try {
//            return new ContentType(getContentType());
//        } catch (ParseException e) {
//            throw new XMLMessageException("xml.Content-Type.parse.err",
//                    new LocalizableExceptionAdapter(e));
//        }
//    }

    public int getStatus() {
        return data.getStatus();
    }

    public void writeTo(OutputStream out) throws IOException {
        if (!hasNoData())
            data.writeTo(out,useFastInfoset);
    }

    public Source getPayload() {
        return data.getPayload();
    }

    public void setPayload(Source payload) {
        data.setPayload(payload);
    }

    public Object getPayload(JAXBContext context) {
        return data.getPayload(context);
    }

    public void setPayload(Object jaxbObj, JAXBContext context) {
        data.setPayload(jaxbObj,context);
    }


    /**
     * Defines operations available regardless of the actual in-memory data representation.
     */
    private static abstract class DataRepresentation {
        abstract Source getPayload();
        abstract Object getPayload(JAXBContext context);
        abstract void setPayload(Source payload);
        abstract void setPayload(Object jaxbObject, JAXBContext context);
        abstract void writeTo(OutputStream out,boolean useFastInfoset) throws IOException;
        /**
         * Returns true whenever the underlying representation of this message
         * is a Fast Infoset stream.
         */
        abstract boolean isFastInfoset();
        abstract Source getSource();
        abstract DataSource getDataSource();
        int getStatus() {
            return WSConnection.OK;
        }
    }

    /**
     * Data represented as a multi-part MIME message.
     *
     * This class parses {@link MimeMultipart} lazily.
     */
    private static final class XMLDataSource extends DataRepresentation {
        private DataSource dataSource;
        private MimeMultipart multipart;
        private XMLSource xmlSource;
        private boolean isFastInfoset;

        public XMLDataSource(final String contentType, final InputStream is, boolean isFastInfoset) {
            this.isFastInfoset = isFastInfoset;
            dataSource = new DataSource() {
                public InputStream getInputStream() {
                    return is;
                }

                public OutputStream getOutputStream() {
                    return null;
                }

                public String getContentType() {
                    return contentType;
                }

                public String getName() {
                    return "";
                }
            };
        }

        public XMLDataSource(DataSource dataSource, boolean isFastInfoset) {
            this.dataSource = dataSource;
            this.isFastInfoset = isFastInfoset;
        }

        public boolean isFastInfoset() {
            return isFastInfoset;
        }

        public DataSource getDataSource() {
            if (dataSource != null) {
                return dataSource;
            }
            else if (multipart != null) {
                return new DataSource() {
                    public InputStream getInputStream() {
                        try {
                            if (xmlSource != null) {
                                replaceRootPart(false);
                            }
                            ByteOutputStream bos = new ByteOutputStream();
                            multipart.writeTo(bos);
                            return bos.newInputStream();
                        } catch(MessagingException me) {
                            throw new XMLMessageException("xml.get.ds.err",me);
                        } catch(IOException ioe) {
                            throw new XMLMessageException("xml.get.ds.err",ioe);
                        }
                    }

                    public OutputStream getOutputStream() {
                        return null;
                    }

                    public String getContentType() {
                        return multipart.getContentType().toString();
                    }

                    public String getName() {
                        return "";
                    }
                };
            }
            return null;
        }

        private MimeBodyPart getRootPart() {
            try {
                ContentType contentType = multipart.getContentType();
                String startParam = contentType.getParameter("start");
                MimeBodyPart sourcePart = (startParam == null)
                    ? (MimeBodyPart)multipart.getBodyPart(0)
                    : (MimeBodyPart)multipart.getBodyPart(startParam);
                return sourcePart;
            }
            catch (MessagingException ex) {
                throw new XMLMessageException("xml.get.source.err",ex);
            }
        }

        private void replaceRootPart(boolean useFastInfoset) {
            if (xmlSource == null) {
                return;
            }
            try {
                MimeBodyPart sourcePart = getRootPart();
                String ctype = sourcePart.getContentType();
                multipart.removeBodyPart(sourcePart);

                ByteOutputStream bos = new ByteOutputStream();
                xmlSource.writeTo(bos, useFastInfoset);
                InternetHeaders headers = new InternetHeaders();
                headers.addHeader("Content-Type",
                    useFastInfoset ? "application/fastinfoset" : ctype);

                sourcePart = new MimeBodyPart(headers, bos.getBytes(),bos.getCount());
                multipart.addBodyPart(sourcePart, 0);
            }
            catch (MessagingException ex) {
                throw new XMLMessageException("xml.get.source.err",ex);
            }
        }

        private void convertToMultipart() {
            if (dataSource != null) {
                try {
                    multipart = new MimeMultipart(dataSource,null);
                    dataSource = null;
                } catch (MessagingException ex) {
                    throw new XMLMessageException("xml.get.source.err",ex);
                }
            }
        }

        /**
         * Returns root part of the MIME message
         */
        public Source getSource() {
            try {
                // If there is an XMLSource, return that
                if (xmlSource != null) {
                    return xmlSource.getPayload();
                }

                // Otherwise, parse MIME package and find root part
                convertToMultipart();
                MimeBodyPart sourcePart = getRootPart();
                ContentType ctype = new ContentType(sourcePart.getContentType());
                String baseType = ctype.getBaseType();

                // Return a StreamSource or FastInfosetSource depending on type
                if (isXMLType(baseType)) {
                    return new StreamSource(sourcePart.getInputStream());
                }
                else if (isFastInfosetType(baseType)) {
                    return FastInfosetReflection.FastInfosetSource_new(
                        sourcePart.getInputStream());
                }
                else {
                    throw new XMLMessageException(
                            "xml.root.part.invalid.Content-Type",
                            new Object[] {baseType});
                }
            } catch (MessagingException ex) {
                throw new XMLMessageException("xml.get.source.err",ex);
            } catch (Exception ioe) {
                throw new XMLMessageException("xml.get.source.err",ioe);
            }
        }

        public Source getPayload() {
            return getSource();
        }

        public void setPayload(Source source) {
            xmlSource = new XMLSource(source);
        }

        public Object getPayload(JAXBContext ctxt) {
            return JAXBTypeSerializer.deserialize(getPayload(),ctxt);
        }

        public void setPayload(Object jaxbObj, JAXBContext ctxt) {
            xmlSource = new XMLSource(null);
            xmlSource.setPayload(jaxbObj, ctxt);
        }

        public void writeTo(OutputStream out, boolean useFastInfoset) {
            try {
                // If a source has been set, ensure MIME parsing
                if (xmlSource != null) {
                    convertToMultipart();
                }

                // Try to use dataSource whenever possible
                if (dataSource != null) {
                    // If already encoded correctly, just copy the bytes
                    if (isFastInfoset == useFastInfoset) {
                        InputStream is = dataSource.getInputStream();
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = is.read(buf)) != -1) {
                            out.write(buf, 0, len);
                        }
                        return;     // we're done
                    }
                    else {
                        // Parse MIME and create source for root part
                        xmlSource = new XMLSource(getSource());
                    }
                }

                // Finally, possibly re-encode root part and write it out
                replaceRootPart(useFastInfoset);
                multipart.writeTo(out);
            }
            catch(Exception e) {
                throw new WebServiceException(e);
            }
        }

    }

    /**
     * Data represented as {@link Source}.
     */
    public static class XMLSource extends DataRepresentation {

        private Source source;
        private boolean isFastInfoset;

        public XMLSource(InputStream in, boolean isFastInfoset) throws Exception {
            this.source = isFastInfoset ?
                FastInfosetReflection.FastInfosetSource_new(in)
                : new StreamSource(in);
            this.isFastInfoset = isFastInfoset;
        }

        public XMLSource(Source source) {
            this.source = source;
            this.isFastInfoset =
                ((source != null)?(source.getClass() == FastInfosetReflection.fiFastInfosetSource):false);
        }

        public boolean isFastInfoset() {
           return isFastInfoset;
        }

        /*
         * If there is a ByteInputStream available, then write it to the output
         * stream. Otherwise, use Transformer to write Source to output stream.
         */
        public void writeTo(OutputStream out, boolean useFastInfoset) {
            try {
                InputStream is = null;
                boolean canAvoidTransform = false;
                if (source instanceof StreamSource) {
                    is = ((StreamSource)source).getInputStream();
                    ByteArrayInputStream tis = (ByteArrayInputStream)is;
                    // If use of FI is requested, need to transcode
                    canAvoidTransform = !useFastInfoset;
                }
                else if (source.getClass() == FastInfosetReflection.fiFastInfosetSource) {
                    is = FastInfosetReflection.FastInfosetSource_getInputStream(source);
                    // If use of FI is not requested, need to transcode
                    canAvoidTransform = useFastInfoset;
                }

                assert is != null;

                if (canAvoidTransform && is instanceof ByteInputStream) {
                    ByteInputStream bis = (ByteInputStream)is;
                    // Reset the stream
                    byte[] buf = bis.getBytes();
                    out.write(buf);
                    bis.close();
                    return;
                }

                // TODO: Use an efficient transformer from SAAJ that knows how to optimally
                // write to FI results
                Transformer transformer = XmlUtil.newTransformer();
                transformer.transform(source,
                    useFastInfoset ? FastInfosetReflection.FastInfosetResult_new(out)
                                   : new StreamResult(out));
            }
            catch (Exception e) {
                throw new WebServiceException(e);
            }
        }

        public Source getSource() {
            return source;
        }

        DataSource getDataSource() {
            return null;
            //throw new UnsupportedOperationException();
        }

        /*
        * Usually called from logical handler
        * If there is a DOMSource, return that. Otherwise, return a copy of
        * the existing source.
        */
        public Source getPayload() {
            try {



                if (source instanceof DOMSource) {
                    return source;
                }

                InputStream is = null;

                if (source instanceof StreamSource) {
                    is = ((StreamSource)source).getInputStream();
                }
                else if (source.getClass() == FastInfosetReflection.fiFastInfosetSource) {
                    is = FastInfosetReflection.FastInfosetSource_getInputStream(source);
                }

                assert is != null;

                if (is != null && is instanceof ByteInputStream) {
                    ByteInputStream bis = (ByteInputStream)is;
                                  // Reset the stream
                    byte[] buf = bis.getBytes();

                    ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                     bis.close();
                    return isFastInfoset ?
                        FastInfosetReflection.FastInfosetSource_new(is)
                    : new StreamSource(bais);
                    
                }

                // Copy source to result respecting desired encoding
                ByteArrayBuffer bab = new ByteArrayBuffer();
                Transformer transformer = XmlUtil.newTransformer();
                transformer.transform(source, isFastInfoset ?
                    FastInfosetReflection.FastInfosetResult_new(bab)
                    : new StreamResult(bab));
                bab.close();

                // Set internal source
                InputStream bis = bab.newInputStream();
                source = isFastInfoset ?
                    FastInfosetReflection.FastInfosetSource_new(bis)
                    : new StreamSource(bis);

                // Return fresh source back to handler
                bis = bab.newInputStream();
                return isFastInfoset ?
                    FastInfosetReflection.FastInfosetSource_new(bis)
                    : new StreamSource(bis);
            }
            catch (Exception e) {
                throw new WebServiceException(e);
            }
        }

        /*
         * Usually called from logical handler
         */
        public void setPayload(Source source) {
            this.source = source;
        }

        /*
         * Usually called from logical handler
         */
        public Object getPayload(JAXBContext ctxt) {
            // Get a copy of Source using getPayload() and use it to deserialize
            // to JAXB object
            return JAXBTypeSerializer.deserialize(getPayload(),
                ctxt);
        }

        /*
         * Usually called from logical handler
         *
         * Convert to StreamSource or FastInfosetSource. In the process of converting,
         * if there is a JAXB exception, propagate it
         */
        public void setPayload(Object jaxbObj, JAXBContext ctxt) {
            try {
                ByteArrayBuffer bab = new ByteArrayBuffer();

                // If old source was FI, re-encode using FI
                if (isFastInfoset) {
                    JAXBTypeSerializer.serializeDocument(jaxbObj,
                        XMLStreamWriterFactory.createFIStreamWriter(bab),
                        ctxt);
                } else {
                    JAXBTypeSerializer.serialize(jaxbObj, bab, ctxt);
                }

                // Return XML or FI source
                InputStream bis = bab.newInputStream();
                source = isFastInfoset ?
                    FastInfosetReflection.FastInfosetSource_new(bis)
                    : new StreamSource(bis);
            }
            catch (Exception e) {
                throw new WebServiceException(e);
            }
        }
    }

    /**
     * Data represented as a JAXB object.
     */
    public static class XMLJaxb extends DataRepresentation {
        private Object object;
        private JAXBContext jaxbContext;

        public XMLJaxb(Object object, JAXBContext jaxbContext) {
            this.object = object;
            this.jaxbContext = jaxbContext;
        }

        public void writeTo(OutputStream out, boolean useFastInfoset) {
            if (useFastInfoset) {
                JAXBTypeSerializer.serializeDocument(object,
                    XMLStreamWriterFactory.createFIStreamWriter(out),
                    jaxbContext);
            }
            else {
                JAXBTypeSerializer.serialize(object, out, jaxbContext);
            }
        }

        boolean isFastInfoset() {
            return false;
        }

        public Source getSource() {
            return JAXBTypeSerializer.serialize(
                object, jaxbContext);
        }

        DataSource getDataSource() {
            throw new UnsupportedOperationException();
        }

        /*
        * Usually called from logical handler
        * If there is a DOMSource, return that. Otherwise, return a copy of
        * the existing source.
        */
        public Source getPayload() {
            return getSource();
        }

        /*
         * Usually called from logical handler
         */
        public void setPayload(Source source) {
            object = JAXBTypeSerializer.deserialize(
                source, jaxbContext);
        }

        /*
         * Usually called from logical handler
         */
        public Object getPayload(JAXBContext ctxt) {
            return object;
        }

        /*
         * Usually called from logical handler
         *
         * Convert to StreamSource. In the process of converting, if there is a
         * JAXB exception, propagate it
         */
        public void setPayload(Object jaxbObj, JAXBContext ctxt) {
            this.object = jaxbObj;
            this.jaxbContext = ctxt;
        }
    }

    private static final class XMLErr extends DataRepresentation {
        private final Exception err;

        XMLErr(Exception err) {
            this.err = err;
        }

        public Source getPayload() {
            return null;
        }

        public Object getPayload(JAXBContext context) {
            return null;
        }

        public void setPayload(Source payload) {
            throw new UnsupportedOperationException();
        }

        void setPayload(Object jaxbObject, JAXBContext context) {
            throw new UnsupportedOperationException();
        }

        public void writeTo(OutputStream out, boolean useFastInfoset) throws IOException {
            String msg = err.getMessage();
            if (msg == null) {
                msg = err.toString();
            }
            msg = "<err>"+msg+"</err>";

            if (useFastInfoset) {
                FastInfosetUtil.transcodeXMLStringToFI(msg, out);
            }
            else {
                out.write(msg.getBytes());
            }
        }

        boolean isFastInfoset() {
            return false;
        }

        Source getSource() {
            return null;
        }

        DataSource getDataSource() {
            throw new UnsupportedOperationException();
        }

        @Override
        int getStatus() {
            if (err instanceof HTTPException) {
                return ((HTTPException)err).getStatusCode();
            }
            return WSConnection.INTERNAL_ERR;
        }
    }
}
