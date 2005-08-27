/*
 * $Id: EndpointEntityResolver.java,v 1.2 2005-08-27 02:54:28 jitu Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
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
