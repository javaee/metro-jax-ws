package com.sun.xml.ws.db.sdo;

import org.eclipse.persistence.oxm.attachment.XMLAttachmentUnmarshaller;

import javax.activation.DataHandler;

/**
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: May 13, 2009
 * Time: 3:52:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class SDOAttachmentUnmarshaller implements XMLAttachmentUnmarshaller {
    private javax.xml.bind.attachment.AttachmentUnmarshaller jbu;



    public SDOAttachmentUnmarshaller(javax.xml.bind.attachment.AttachmentUnmarshaller jbu) {
        this.jbu = jbu;
    }

    public byte[] getAttachmentAsByteArray(String cid) {
        return jbu.getAttachmentAsByteArray(cid);
    }

    public DataHandler getAttachmentAsDataHandler(String cid) {
       return jbu.getAttachmentAsDataHandler(cid);
    }

    public boolean isXOPPackage() {
        return jbu.isXOPPackage();
    }
}
