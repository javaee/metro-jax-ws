/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.transport.http.server;

import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.util.xml.XmlUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author WS Developement Team
 */
public class EndpointEntityResolver implements EntityResolver {

    private EntityResolver catalogResolver;
    private Map<String, DocInfo> metadata;
    
    /*
     * Assumes Source objects can be reused multiple times
     */ 
    public EndpointEntityResolver(Map<String, DocInfo> metadata) {
        this.metadata = metadata;
        catalogResolver = XmlUtil.createDefaultCatalogResolver(); 
    }
    
    /*
     * It resolves the systemId in the metadata first. If it is not found, then
     * it tries to resolve in catalog
     */
    public InputSource resolveEntity (String publicId, String systemId)
	throws SAXException, IOException {
        if (systemId != null) {
            DocInfo docInfo = metadata.get(systemId);
            if (docInfo != null) {
                InputStream in = docInfo.getDoc();
                InputSource is = new InputSource(in);
                is.setSystemId(systemId);
                return is;
            }
        }
        return catalogResolver.resolveEntity(publicId, systemId);
    }
    
}
