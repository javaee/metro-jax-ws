/*
 * $Id: WSDLGenResolver.java,v 1.5 2005-08-26 22:25:52 jitu Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.server;

import com.sun.xml.ws.wsdl.parser.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import com.sun.xml.ws.wsdl.writer.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WS Development Team
 */

public class WSDLGenResolver implements WSDLOutputResolver {
    
    private Map<String, DocInfo> docs;
    private String wsdlFile;
    
    public WSDLGenResolver() {
        docs = new HashMap<String, DocInfo>();
    }
    
    public String getWSDLFile() {
        return wsdlFile;
    }
    
    public Map<String, DocInfo> getDocs() {
        return docs;
    }
    
    public Result getWSDLOutput(String suggestedFileName) {       
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        StreamDocInfo docInfo = new StreamDocInfo(suggestedFileName, bout);
        
        if (wsdlFile == null) {
            docInfo.setQueryString("wsdl");
            wsdlFile = suggestedFileName;
        } else {
            docInfo.setQueryString("wsdl="+suggestedFileName);
        }
        docs.put(docInfo.getPath(),  docInfo);
   
        StreamResult result = new StreamResult();
        result.setOutputStream(bout);
        result.setSystemId(suggestedFileName);
        return result;
    }
    
    public Result getSchemaOutput(String namespaceUri, String suggestedFileName) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        StreamDocInfo docInfo = new StreamDocInfo(suggestedFileName, bout);
        docInfo.setQueryString("xsd="+suggestedFileName);
        docs.put(docInfo.getPath(),  docInfo);
   
        StreamResult result = new StreamResult();
        result.setOutputStream(bout);
        result.setSystemId(suggestedFileName);
        return result;
    }
    
    public class StreamDocInfo implements DocInfo {
        private ByteArrayOutputStream bout;
        private String resource;
        private String queryString;
        
        public StreamDocInfo(String resource, ByteArrayOutputStream bout) {
            this.resource = "/WEB-INF/wsdl/"+resource;
            this.bout = bout;
        }

        public InputStream getDoc() {
            try {
                bout.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
            return new ByteArrayInputStream(bout.toByteArray());
        }

        public String getPath() {
            return resource;
        }

        public String getQueryString() {
            return queryString;
        }
        
        public DocContext getDocContext() {
            return new GenDocContext();
        }

        public void setQueryString(String queryString) {
            this.queryString = queryString;
        }
        
        public void setDocType(DOC_TYPE docType) {

        }

        public DOC_TYPE getDocType() {
            return null;
        }

        public void setTargetNamespace(String ns) {

        }

        public String getTargetNamespace() {
            return null;
        }

        public void setService(Service service) {

        }

        public Service getService() {
            return null;
        }

        public void setPortType(boolean portType) {

        }

        public boolean hasPortType() {
            return false;
        }

    }
    
    public static class GenDocContext implements DocContext {
    
        public String getAbsolutePath(String abs, String rel) {
            int index = abs.lastIndexOf("/");       
            return abs.substring(0, index+1)+rel;
        }
    
    }


}
