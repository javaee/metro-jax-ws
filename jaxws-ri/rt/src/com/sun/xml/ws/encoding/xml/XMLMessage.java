/*
 * $Id: XMLMessage.java,v 1.10 2005-08-10 02:45:08 jitu Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.encoding.xml;
import javax.xml.soap.MimeHeaders;
import com.sun.xml.messaging.saaj.packaging.mime.MessagingException;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ContentType;
import com.sun.xml.messaging.saaj.packaging.mime.internet.InternetHeaders;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ParseException;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
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

    private static final int PLAIN_XML_FLAG = 1;
    private static final int MIME_MULTIPART_FLAG = 2;
    
    protected MimeHeaders headers;
    protected XMLDataSource xmlDataSource;
    protected XMLSource xmlSource;
    protected Exception err;
    private static final String DEFAULT_SERVER_ERROR =
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<err>Internal Server Error: Unknown content in XMLMessage</err>";


    /**
     * Construct a message from an input stream. When messages are
     * received, there's two parts -- the transport headers and the
     * message content in a transport specific stream.
     */
    public XMLMessage(MimeHeaders headers, final InputStream in)
        throws IOException {
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
                xmlSource = new XMLSource(in);
            } else if ((contentTypeId & MIME_MULTIPART_FLAG) != 0) {
                xmlDataSource = new XMLDataSource(ct, in);
            } else {
                throw new XMLMessageException("xml.unknown.Content-Type");
            }
        } catch (Exception ex) {
            throw new XMLMessageException("xml.cannot.internalize.message",
                    new LocalizableExceptionAdapter(ex));
        }
    }
    
    public XMLMessage(Source source) {
        this.xmlSource = new XMLSource(source);
        this.headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
    }
    
    public XMLMessage(Exception err) {
        this.err = err;
        this.headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
    }
    
    public XMLMessage(DataSource dataSource) {
        this.xmlDataSource = new XMLDataSource(dataSource);
        this.headers = new MimeHeaders();
        headers.addHeader("Content-Type", dataSource.getContentType());
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
                } else {
                    throw new XMLMessageException(
                            "xml.content-type.mustbe.multipart");
                }
            } else {
                throw new XMLMessageException("xml.invalid.content-type",
                        new Object[] { primary+"/"+sub } );
            }
        } else if (isXMLType(primary, sub)) {
            return PLAIN_XML_FLAG;
        } else {
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

    private ContentType ContentType() {
        try {
            return new ContentType(getContentType());
        } catch (ParseException e) {
            throw new XMLMessageException("xml.Content-Type.parse.err",
                    new LocalizableExceptionAdapter(e));
        }
    }
    
    public WSConnection.STATUS getStatus() {
        if (err != null) {     
            if (err instanceof HTTPException) {
                return WSConnection.STATUS.OTHER;
            }
            return WSConnection.STATUS.INTERNAL_ERR;
        }
        return WSConnection.STATUS.OK;
    }
    
    public int getStatusCode() {
        if (err != null && err instanceof HTTPException) {
            return ((HTTPException)err).getStatusCode();
        }
        return -1;
    }

    public void writeTo(OutputStream out)
    throws MessagingException, IOException, TransformerException {
        if (xmlDataSource != null) {
            xmlDataSource.writeTo(out);
        } else if (xmlSource != null) {
            xmlSource.writeTo(out);

        } else if (err != null) {
            String msg = err.getMessage();
            if (msg == null) {
                msg = err.toString();
            }
            msg = "<err>"+msg+"</err>";
            out.write(msg.getBytes());
        } else {
            out.write(DEFAULT_SERVER_ERROR.getBytes());
        }
    }
    
    public Source getPayload() {
        if (xmlDataSource != null) {
            return xmlDataSource.getPayload();
        } else if (xmlSource != null) {
            return xmlSource.getPayload();
        } else {
            return null;
        } 
    }

    public void setPayload(Source payload) {
        if (xmlDataSource != null) {
            xmlDataSource.setPayload(payload);
        } else if (xmlSource != null) {
            xmlSource.setPayload(payload);
        }
    }

    public Object getPayload(JAXBContext context) {
        if (xmlDataSource != null) {
            return xmlDataSource.getPayload(context);
        } else if (xmlSource != null) {
            return xmlSource.getPayload(context);
        } else {
            return null;
        }
    }

    public void setPayload(Object jaxbObj, JAXBContext context) {
        if (xmlDataSource != null) {
            xmlDataSource.setPayload(jaxbObj, context);
        } else if (xmlSource != null) {
            xmlSource.setPayload(jaxbObj, context);
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
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            multipart.writeTo(bos);
                            bos.close();
                            return new ByteArrayInputStream(bos.toByteArray());
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
                        return multipart.getContentType();
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
                ContentType contentType = new ContentType(multipart.getContentType());
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
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                xmlSource.writeTo(baos);
                baos.close();
                InternetHeaders headers = new InternetHeaders();
                headers.addHeader("Content-Type", ctype);
                sourcePart = new MimeBodyPart(headers, baos.toByteArray());
                multipart.addBodyPart(sourcePart, 0);
            } catch (MessagingException ex) {
                throw new XMLMessageException("xml.get.source.err",
                        new LocalizableExceptionAdapter(ex));
            } catch (IOException ioe) {
                throw new XMLMessageException("xml.get.source.err",
                        new LocalizableExceptionAdapter(ioe));
            }
        }
        
        private void convertToMultipart() {
            if (dataSource != null) {
                try {
                    multipart = new MimeMultipart(dataSource);
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
        
        public XMLSource(InputStream in) {
            this.source = new StreamSource(in);
        }

        public XMLSource(Source source) {
            this.source = source;
        }
        
        /*
         * If there is a StreamSource with ByteInputStream, write the underlying
         * buffer to stream. Otherwise, use Transformer to write Source to the
         * ouput stream.
         */
        public void writeTo(OutputStream out) {
            try {
                if (source instanceof StreamSource) {
                    InputStream is = ((StreamSource)source).getInputStream();
                    if (is != null && is instanceof ByteInputStream) {
                        ByteInputStream bis = (ByteInputStream)is;
                        bis.close();                // Reset the stream
                        byte[] buf = bis.getBytes();
                        out.write(buf);
                        return;
                    }
                }
                Transformer transformer = XmlUtil.newTransformer();
                transformer.transform(source, new StreamResult(out));
            } catch(Exception e) {
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
                } else if (source instanceof StreamSource) {
                    InputStream is = ((StreamSource)source).getInputStream();
                    if (is != null && is instanceof ByteInputStream) {
                        ByteInputStream bis = (ByteInputStream)is;
                        bis.close();                // Reset the stream
                        byte[] buf = bis.getBytes();
                        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                        return new StreamSource(bais);
                    }
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Transformer transformer = XmlUtil.newTransformer();
                transformer.transform(source, new StreamResult(baos));
                baos.close();
                byte[] buf = baos.toByteArray();
                source = new StreamSource(new ByteInputStream(buf, buf.length));
                ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                return new StreamSource(bais);
            } catch(Exception e) {
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
         * Convert to StreamSource. In the process of converting, if there is a
         * JAXB exception, propagate it
         */
        public void setPayload(Object jaxbObj, JAXBContext ctxt) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                JAXBTypeSerializer.getInstance().serialize(jaxbObj, baos, ctxt);
                baos.close();
                byte[] buf = baos.toByteArray();
                source = new StreamSource(new ByteInputStream(buf, buf.length));
            } catch(Exception e) {
                throw new WebServiceException(e);
            }
        }
        
    }

    
}
