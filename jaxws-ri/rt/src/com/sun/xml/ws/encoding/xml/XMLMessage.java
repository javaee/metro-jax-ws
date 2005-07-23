/*
 * $Id: XMLMessage.java,v 1.2 2005-07-23 00:21:27 jitu Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.encoding.xml;
import com.sun.xml.messaging.saaj.packaging.mime.MessagingException;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ContentType;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart;
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

/**
 *
 * @author WS Developement Team
 */
public class XMLMessage {
    
    private static final Logger log = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".encoding.xml");

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
        if (headers != null)
            ct = getContentType(headers);
        else {
            log.severe("xml.null.headers");
            throw new WebServiceException("Cannot create message: " +
                                        "Headers can't be null");
        }

        if (ct == null) {
            log.severe("xml.no.Content-Type");
            throw new WebServiceException("Absent Content-Type");
        }

        try {
            ContentType contentType = new ContentType(ct);
      
            // In the absence of type attribute we assume it to be text/xml.
            // That would mean we're easy on accepting the message and 
            // generate the correct thing (as the SWA spec also specifies
            // that the type parameter should always be text/xml)
            if(contentType.getParameter("type") == null)
                contentType.setParameter("type", "text/xml");

            // In the absence of type attribute we assume it to be text/xml.
            // That would mean we're easy on accepting the message and
            // generate the correct thing (as the SWA spec also specifies
            // that the type parameter should always be text/xml)
            if (contentType.getParameter("type") == null)
                contentType.setParameter("type", "text/xml");

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
                log.severe("xml.unknown.Content-Type");
                throw new WebServiceException("Unrecognized Content-Type");
            }
        } catch (Throwable ex) {
            log.severe("xml.cannot.internalize.message");
            throw new WebServiceException("Unable to internalize message", ex);
        }
    }
    
    public XMLMessage(Source source) {
        this.source = source;
    }
    
    public XMLMessage(DataSource dataSource) {
        this.dataSource = dataSource;
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
                log.log(Level.SEVERE, 
                        "xml.root.part.invalid.Content-Type",
                        new Object[] {baseType});
                throw new WebServiceException(
                        "Bad Content-Type for Root Part : " +
                        baseType);
            }
            return new StreamSource(sourcePart.getInputStream());
        } catch (MessagingException ex) {
            throw new WebServiceException("Cannot return source", ex);
        } catch (IOException ioe) {
            throw new WebServiceException("cannot return source", ioe);
        }
    }
    
    public void setPayloadSource(StreamSource source) {
        try {
            if (dataSource != null) {
                multipart = new MimeMultipart(dataSource);
                dataSource = null;
            }
            if (multipart != null) {
                MimeBodyPart sourcePart = new MimeBodyPart(source.getInputStream());
                multipart.addBodyPart(sourcePart, 0);
            } else {
                this.source = source;
            }
        } catch (MessagingException ex) {
            throw new WebServiceException("Cannot set payload source", ex);
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
                        throw new WebServiceException("Cannot give DataSource", me);
                    } catch(IOException ioe) {
                        throw new WebServiceException("Cannot give DataSource", ioe);
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
        // TBD
        //    Is there anything else we need to verify here?

        String primary = contentType.getPrimaryType();
        String sub = contentType.getSubType();

        if (primary.equalsIgnoreCase("multipart")) {
            if (sub.equalsIgnoreCase("related")) {
                String type = contentType.getParameter("type");
                if (isXMLType(type)) {
                    return MIME_MULTIPART_FLAG;
                } else {
                    log.severe("xml.content-type.mustbe.multipart");
                    throw new WebServiceException(
                        "Content-Type needs to be Multipart/Related "
                            + "and with \"type=text/xml\" "
                            + "or \"type=application/soap+xml\"");
                }
            } else {
                log.severe("xml.invalid.content-type");
                throw new WebServiceException(
                    "Invalid Content-Type: " + primary + "/" + sub);
            }
        } else if (isXMLType(primary)) {
            return PLAIN_XML_FLAG;
        } else {
            log.severe("xml.invalid.content-type");
            throw new WebServiceException(
                "Invalid Content-Type:"
                    + primary
                    + "/"
                    + sub);
        }
    }
    
    protected static boolean isXMLType(String type) {
        return type.toLowerCase().startsWith("text/xml");
    }

    public MimeHeaders getMimeHeaders() {
        return this.headers;
    }

    final static String getContentType(MimeHeaders headers) {
        String[] values = headers.getHeader("Content-Type");
        if (values == null)
            return null;
        else
            return values[0];
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
        ContentType ct = null;
        try {
            ct = new ContentType(getContentType());
        } catch (Exception e) {
            // what to do here?
        }
        return ct;
    }

    /*
     * Return the MIME type string, without the parameters.
     */
    public String getBaseType() {
        return ContentType().getBaseType();
    }

    public void setBaseType(String type) {
        ContentType ct = ContentType();
        ct.setParameter("type", type);
        headers.setHeader("Content-Type", ct.toString());
    }

    public String getAction() {
        return ContentType().getParameter("action");
    }

    public void setAction(String action) {
        ContentType ct = ContentType();
        ct.setParameter("action", action);
        headers.setHeader("Content-Type", ct.toString());
    }

    public String getCharset() {
        return ContentType().getParameter("charset");
    }

    public void setCharset(String charset) {
        ContentType ct = ContentType();
        ct.setParameter("charset", charset);
        headers.setHeader("Content-Type", ct.toString());
    }


    public void writeTo(OutputStream out) throws MessagingException, IOException {
        if (dataSource != null) {
            MimeMultipart multipart = new MimeMultipart(dataSource);
            multipart.writeTo(out);
        } else {
            // Write the Source object to stream
            if (source instanceof StreamSource) {
                StreamSource src = (StreamSource)source;
                InputStream is = src.getInputStream();
                byte[] buf = new byte[1024];
                int num = 0;
                while ((num = is.read(buf)) != -1) {
                    out.write(buf, 0, num);
                }
            } else {
                // TODO
            }

        }
    }

    
}
