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

import javax.xml.soap.MimeHeaders;
import com.sun.xml.messaging.saaj.packaging.mime.MessagingException;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ContentType;
import com.sun.xml.messaging.saaj.packaging.mime.internet.InternetHeaders;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.messaging.saaj.util.ByteOutputStream;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.util.FastInfosetReflection;
import com.sun.xml.ws.util.FastInfosetUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.activation.DataSource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.protocol.xml.XMLMessageException;
import com.sun.xml.ws.spi.runtime.WSConnection;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPException;

/**
 *
 * @author WS Developement Team
 */
public class XMLMessage {
    
    private static final Logger log = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".protocol.xml");

    private static final int PLAIN_XML_FLAG      = 1;       // 00001
    private static final int MIME_MULTIPART_FLAG = 2;       // 00010
    private static final int FI_ENCODED_FLAG     = 16;      // 10000
    
    protected MimeHeaders headers;
    protected XMLDataSource xmlDataSource;
    protected XMLSource xmlSource;
    protected XMLJaxb jaxbObject;
    protected Exception err;
    
    /**
     * Indicates when Fast Infoset should be used to serialize
     * this message.
     */
    protected boolean useFastInfoset = false;
    
    private static final String DEFAULT_SERVER_ERROR =
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<err>Internal Server Error: Unknown content in XMLMessage</err>";


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
        } else {
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
            if(contentType.getParameter("type") == null) {
                contentType.setParameter("type", "text/xml");
            }

            int contentTypeId = identifyContentType(contentType);

            if ((contentTypeId & PLAIN_XML_FLAG) != 0) {
                xmlSource = new XMLSource(in, (contentTypeId & FI_ENCODED_FLAG) > 0);
            } 
            else if ((contentTypeId & MIME_MULTIPART_FLAG) != 0) {
                xmlDataSource = new XMLDataSource(ct, in);      // TODO: Fast Infoset
            } 
            else {
                throw new XMLMessageException("xml.unknown.Content-Type");
            }
        } catch (Exception ex) {
            throw new XMLMessageException("xml.cannot.internalize.message",
                    new LocalizableExceptionAdapter(ex));
        }
    }
    
    public XMLMessage(Source source, boolean useFastInfoset) {
        this.xmlSource = new XMLSource(source);
        this.headers = new MimeHeaders();
        this.useFastInfoset = useFastInfoset;
        headers.addHeader("Content-Type", 
            useFastInfoset ? "application/fastinfoset" : "text/xml");
    }
    
    public XMLMessage(Exception err, boolean useFastInfoset) {
        this.err = err;
        this.headers = new MimeHeaders();
        this.useFastInfoset = useFastInfoset;
        headers.addHeader("Content-Type", 
            useFastInfoset ? "application/fastinfoset" : "text/xml");
    }
    
    public XMLMessage(DataSource dataSource, boolean useFastInfoset) {
        this.xmlDataSource = new XMLDataSource(dataSource);
        this.headers = new MimeHeaders();
        this.useFastInfoset = useFastInfoset;
        headers.addHeader("Content-Type", dataSource.getContentType());     // TODO FI
    }
    
    public XMLMessage(Object object, JAXBContext context, boolean useFastInfoset) {
        this.jaxbObject = new XMLJaxb(object, context);
        this.headers = new MimeHeaders();
        this.useFastInfoset = useFastInfoset;
        headers.addHeader("Content-Type", 
            useFastInfoset ? "application/fastinfoset" : "text/xml");
    }

    /**
     * Returns true if the underlying encoding of this message is FI.
     */
    public boolean isFastInfoset() {
        if (xmlSource != null) {
            return xmlSource.isFastInfoset();
        } 
        else if (xmlDataSource != null) {
            return xmlDataSource.isFastInfoset();
        } 
        else {
            return false;
        }
    }

    /**
     * Returns true if the sender of this message accepts FI. Slow, but
     * should only be called once.
     */
    public boolean acceptFastInfoset() {
        return FastInfosetUtil.isFastInfosetAccepted(headers.getHeader("Accept"));
    }
    
    public Source getSource() {
        if (xmlSource != null) {
            return xmlSource.getSource();
        } else if (xmlDataSource != null) {
            return xmlDataSource.getSource();
        } else {
            return null;
        }
    }
    
    public DataSource getDataSource() {
        return xmlDataSource.getDataSource();
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

    private static final String getContentType(MimeHeaders headers) {
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
        if (err != null) {     
            if (err instanceof HTTPException) {
                return ((HTTPException)err).getStatusCode();
            }
            return WSConnection.INTERNAL_ERR;
        }
        return WSConnection.OK;
    }
    
    public void writeTo(OutputStream out)
        throws MessagingException, IOException, TransformerException 
    {
        if (xmlDataSource != null) {
            xmlDataSource.writeTo(out); // TODO: useFastInfoset
        } 
        else if (xmlSource != null) {
            xmlSource.writeTo(out, useFastInfoset);
        } 
        else if (jaxbObject != null) {
            jaxbObject.writeTo(out, useFastInfoset);
        } 
        else if (err != null) {
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
        else {
            if (useFastInfoset) {
                FastInfosetUtil.transcodeXMLStringToFI(DEFAULT_SERVER_ERROR, out);
            }
            else {
                out.write(DEFAULT_SERVER_ERROR.getBytes());
            }
        }
    }
    
    public Source getPayload() {
        if (xmlDataSource != null) {
            return xmlDataSource.getPayload();
        } else if (xmlSource != null) {
            return xmlSource.getPayload();
        } else if (jaxbObject != null) {
            return jaxbObject.getSource();
        } else {
            return null;
        }
    }

    public void setPayload(Source payload) {
        if (xmlDataSource != null) {
            xmlDataSource.setPayload(payload);
        } else if (xmlSource != null) {
            xmlSource.setPayload(payload);
        } else if (jaxbObject != null) {
            jaxbObject.setPayload(payload);
        }
    }

    public Object getPayload(JAXBContext context) {
        if (xmlDataSource != null) {
            return xmlDataSource.getPayload(context);
        } else if (xmlSource != null) {
            return xmlSource.getPayload(context);
        } else if (jaxbObject != null) {
            return jaxbObject.getPayload(context);
        } else {
            return null;
        }
    }

    public void setPayload(Object jaxbObj, JAXBContext context) {
        if (xmlDataSource != null) {
            xmlDataSource.setPayload(jaxbObj, context);
        } else if (xmlSource != null) {
            xmlSource.setPayload(jaxbObj, context);
        } else if (jaxbObject != null) {
            jaxbObject.setPayload(jaxbObj, context);
        }
    }
    
    public static class XMLDataSource {
        private DataSource dataSource;
        private MimeMultipart multipart;
        private XMLSource xmlSource;
        
        public XMLDataSource(final String contentType, final InputStream is) {
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
        
        public XMLDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
        }
        
        public boolean isFastInfoset() {
            return false;       // TODO
        }
        
        public DataSource getDataSource() {
            if (dataSource != null) {
                return dataSource;
            } else if (multipart != null) {
                return new DataSource() {
                    public InputStream getInputStream() {
                        try {
                            if (xmlSource != null) {
                                replaceRootPart();
                            }
                            ByteOutputStream bos = new ByteOutputStream();
                            multipart.writeTo(bos);
                            return bos.newInputStream();
                        } catch(MessagingException me) {
                            throw new XMLMessageException("xml.get.ds.err",
                                    new LocalizableExceptionAdapter(me));
                        } catch(IOException ioe) {
                            throw new XMLMessageException("xml.get.ds.err",
                                    new LocalizableExceptionAdapter(ioe));
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
            } catch (MessagingException ex) {
                throw new XMLMessageException("xml.get.source.err",
                        new LocalizableExceptionAdapter(ex));
            }
        }
        
        private void replaceRootPart() {
            if (xmlSource == null) {
                return;
            }
            try {
                MimeBodyPart sourcePart = getRootPart();
                String ctype = sourcePart.getContentType();
                multipart.removeBodyPart(sourcePart);
                
                ByteOutputStream bos = new ByteOutputStream();
                xmlSource.writeTo(bos, false);
                InternetHeaders headers = new InternetHeaders();
                headers.addHeader("Content-Type", ctype);
                sourcePart = new MimeBodyPart(headers, bos.getBytes(),bos.getCount());
                multipart.addBodyPart(sourcePart, 0);
            } catch (MessagingException ex) {
                throw new XMLMessageException("xml.get.source.err",
                        new LocalizableExceptionAdapter(ex));
            }
        }
        
        private void convertToMultipart() {
            if (dataSource != null) {
                try {
                    multipart = new MimeMultipart(dataSource,null);
                    dataSource = null;
                } catch (MessagingException ex) {
                    throw new XMLMessageException("xml.get.source.err",
                        new LocalizableExceptionAdapter(ex));
                }
            }
        }
        
        /*
         *
         * Returns root part of the MIME message
         */
        public Source getSource() {
            try {
                if (xmlSource != null) {
                    return xmlSource.getPayload();
                }
                convertToMultipart();
                MimeBodyPart sourcePart = getRootPart();
                ContentType ctype = new ContentType(sourcePart.getContentType());
                String baseType = ctype.getBaseType();
                if (!(isXMLType(baseType))) {
                    throw new XMLMessageException(
                            "xml.root.part.invalid.Content-Type",
                            new Object[] {baseType});
                }
                return new StreamSource(sourcePart.getInputStream());
            } catch (MessagingException ex) {
                throw new XMLMessageException("xml.get.source.err",
                        new LocalizableExceptionAdapter(ex));
            } catch (IOException ioe) {
                throw new XMLMessageException("xml.get.source.err",
                        new LocalizableExceptionAdapter(ioe));
            }
        }
        
        public Source getPayload() {
            return getSource();
        }
        
        public void setPayload(Source source) {
            xmlSource = new XMLSource(source);
        }
        
        public Object getPayload(JAXBContext ctxt) {
            return JAXBTypeSerializer.getInstance().deserialize(getPayload(),
                ctxt);
        }
        
        public void setPayload(Object jaxbObj, JAXBContext ctxt) {
            xmlSource = new XMLSource((Source)null);
            xmlSource.setPayload(jaxbObj, ctxt);
        }
        
        public void writeTo(OutputStream out) {
            try {
                if (xmlSource != null) {
                    convertToMultipart();
                }
                if (dataSource != null) {
                    InputStream is = dataSource.getInputStream();
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = is.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                } else {
                    replaceRootPart();
                    multipart.writeTo(out);
                }
            } catch(Exception e) {
                throw new WebServiceException(e);
            }
        }

    }
    
    public static class XMLSource {
        
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
                (source.getClass() == FastInfosetReflection.fiFastInfosetSource);
        }
        
        /**
         * Returns true whenever the underlying representation of this message
         * is a Fast Infoset stream.
         */
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
                    bis.close();                // Reset the stream
                    byte[] buf = bis.getBytes();
                    out.write(buf);
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
                    bis.close();                // Reset the stream
                    byte[] buf = bis.getBytes();
                    ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                    return isFastInfoset ? 
                        FastInfosetReflection.FastInfosetSource_new(is)
                        : new StreamSource(bais);
                }
                
                // Copy source to result respecting desired encoding
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Transformer transformer = XmlUtil.newTransformer();
                transformer.transform(source, isFastInfoset ? 
                    FastInfosetReflection.FastInfosetResult_new(baos)
                    : new StreamResult(baos));
                baos.close();
                
                // Set internal source 
                byte[] buf = baos.toByteArray();
                ByteInputStream bis = new ByteInputStream(buf, buf.length);
                source = isFastInfoset ?
                    FastInfosetReflection.FastInfosetSource_new(bis) 
                    : new StreamSource(bis);
                
                // Return fresh source back to handler
                ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                return isFastInfoset ? 
                    FastInfosetReflection.FastInfosetSource_new(bais)
                    : new StreamSource(bais);
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
            return JAXBTypeSerializer.getInstance().deserialize(getPayload(), 
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
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                // If old source was FI, re-encode using FI
                if (isFastInfoset) {
                    JAXBTypeSerializer.getInstance().serialize(jaxbObj, 
                        XMLStreamWriterFactory.createFIStreamWriter(baos),
                        ctxt);                    
                }
                else {
                    JAXBTypeSerializer.getInstance().serialize(jaxbObj, baos, ctxt);
                }
                
                // Return XML or FI source
                baos.close();                
                byte[] buf = baos.toByteArray();
                ByteInputStream bis = new ByteInputStream(buf, buf.length);
                source = isFastInfoset ?
                    FastInfosetReflection.FastInfosetSource_new(bis)
                    : new StreamSource(bis);
            } 
            catch (Exception e) {
                throw new WebServiceException(e);
            }
        }
    }

    public static class XMLJaxb {
        private Object object;
        private JAXBContext jaxbContext;
        private ByteArrayOutputStream baos;
        
        public XMLJaxb(Object object, JAXBContext jaxbContext) {
            this.object = object;
            this.jaxbContext = jaxbContext;
        }
        
        public void writeTo(OutputStream out, boolean useFastInfoset) {
            try {
                JAXBBeanInfo beanInfo = new JAXBBeanInfo(object, jaxbContext);
                baos = new ByteArrayOutputStream();
                
                if (useFastInfoset) {
                    JAXBTypeSerializer.getInstance().serialize(beanInfo.getBean(),
                        XMLStreamWriterFactory.createFIStreamWriter(out), 
                        beanInfo.getJAXBContext());
                }
                else {
                    JAXBTypeSerializer.getInstance().serialize(beanInfo.getBean(),
                        baos, beanInfo.getJAXBContext());
                }
                
                out.write(baos.toByteArray());
            } 
            catch(Exception e) {
                throw new WebServiceException(e);
            }
        }
        
        public Source getSource() {
            return JAXBTypeSerializer.getInstance().serialize(
                object, jaxbContext);
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
            object = JAXBTypeSerializer.getInstance().deserialize(
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
}
