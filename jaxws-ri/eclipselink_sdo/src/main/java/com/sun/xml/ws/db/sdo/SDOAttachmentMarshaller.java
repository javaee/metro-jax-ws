/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
