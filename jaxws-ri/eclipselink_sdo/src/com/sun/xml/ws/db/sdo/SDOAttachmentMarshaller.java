package com.sun.xml.ws.db.sdo;

import org.eclipse.persistence.oxm.attachment.XMLAttachmentMarshaller;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: May 13, 2009
 * Time: 3:51:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class SDOAttachmentMarshaller implements XMLAttachmentMarshaller {
    private javax.xml.bind.attachment.AttachmentMarshaller jbm;

    public SDOAttachmentMarshaller(javax.xml.bind.attachment.AttachmentMarshaller jbm) {
       this.jbm = jbm;
    }
    public String addSwaRefAttachment(byte[] data, int start, int length) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(data, start, length);
        DataSource ds = new DataSource() {

            //@Override
            public String getContentType() {
                return "application/data";
            }

            //@Override
            public InputStream getInputStream() throws IOException {
                return bais;
            }

            //@Override
            public String getName() {
               return "";
            }

            //@Override
            public OutputStream getOutputStream() throws IOException {
                return null;
            }
            
        };
        return jbm.addSwaRefAttachment(new DataHandler(ds));
    }


    public String addMtomAttachment(DataHandler dh, String elementNamespace, String elementLocalName) {
        return jbm.addMtomAttachment(dh, elementNamespace, elementLocalName);
    }

    public String addMtomAttachment(byte[] data, int offset, int length,
                                    String mimeType, String elementNamespace, String elementLocalName) {
        return jbm.addMtomAttachment(data, offset, length, mimeType, elementNamespace, elementLocalName);
    }


    public String addSwaRefAttachment(DataHandler dh) {
        return jbm.addSwaRefAttachment(dh);
    }

    public boolean isXOPPackage() {
        return jbm.isXOPPackage();
    }
}
