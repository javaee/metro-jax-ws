/*
 * $Id: XMLMessage.java,v 1.6 2005-07-27 01:31:22 jitu Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.encoding.xml;
import com.sun.xml.messaging.saaj.packaging.mime.MessagingException;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ContentType;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ParseException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataSource;
import javax.xml.soap.MimeHeaders;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import com.sun.xml.ws.util.xml.XmlUtil;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.protocol.xml.XMLMessageException;

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
    protected DataSource dataSource;
    protected MimeMultipart multipart;
    protected Source source;

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
                source = new StreamSource(in);
            } else if ((contentTypeId & MIME_MULTIPART_FLAG) != 0) {
                dataSource = new DataSource() {
                    public InputStream getInputStream() {
                        return in;
                    }

                    public OutputStream getOutputStream() {
                        return null;
                    }

                    public String getContentType() {
                        return ct;
                    }

                    public String getName() {
                        return "";
                    }
                };
            } else {
                throw new XMLMessageException("xml.unknown.Content-Type");
            }
        } catch (Exception ex) {
            throw new XMLMessageException("xml.cannot.internalize.message",
                    new LocalizableExceptionAdapter(ex));
        }
    }
    
    public XMLMessage(Source source) {
        this.source = source;
        this.headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
    }
    
    public XMLMessage(DataSource dataSource) {
        this.dataSource = dataSource;
        this.headers = new MimeHeaders();
        headers.addHeader("Content-Type", dataSource.getContentType());
    }
    
    public Source getSource() {
        if (source != null) {
            return source;
        }
        try {
            if (dataSource != null) {
                multipart = new MimeMultipart(dataSource);
                dataSource = null;
            }
            MimeBodyPart sourcePart = (MimeBodyPart)multipart.getBodyPart(0);
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
    
    public void setPayloadSource(StreamSource source) {
        try {
            if (dataSource != null) {
                multipart = new MimeMultipart(dataSource);
                dataSource = null;
            }
            if (multipart != null) {
                // TODO: some more work, setting headers 
                multipart.removeBodyPart(0);
                MimeBodyPart sourcePart = new MimeBodyPart(
                        source.getInputStream());
                multipart.addBodyPart(sourcePart, 0);
            } else {
                this.source = source;
            }
        } catch (MessagingException ex) {
            throw new XMLMessageException("xml.set.payload.err",
                    new LocalizableExceptionAdapter(ex));
        }
    }
    
    public DataSource getDataSource() {
        if (dataSource == null) {
            return new DataSource() {
                public InputStream getInputStream() {
                    try {
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
        return dataSource;
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

    public void writeTo(OutputStream out)
    throws MessagingException, IOException, TransformerException {
        if (dataSource != null) {
            MimeMultipart multipart = new MimeMultipart(dataSource);
            multipart.writeTo(out);
        } else {
            Transformer transformer = XmlUtil.newTransformer();
            transformer.transform(source, new StreamResult(out));
        }
    }

    
}
