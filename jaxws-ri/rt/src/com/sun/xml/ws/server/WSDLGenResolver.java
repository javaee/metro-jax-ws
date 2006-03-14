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

package com.sun.xml.ws.server;

import com.sun.xml.ws.server.DocInfo.DOC_TYPE;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.wsdl.parser.Service;
import com.sun.xml.ws.wsdl.writer.WSDLOutputResolver;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Holder;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author WS Development Team
 */

public class WSDLGenResolver implements WSDLOutputResolver {
    
    private Map<String, DocInfo> docs;
    private DocInfo abstractWsdl;
    private DocInfo concreteWsdl;
    private Map<String, List<String>> nsMapping;    // targetNS -> system id list
    
    public WSDLGenResolver(Map<String, DocInfo> docs) {
        this.docs = docs;
        nsMapping = new HashMap<String, List<String>>();
        Set<Entry<String, DocInfo>> docEntries = docs.entrySet();
        for(Entry<String, DocInfo> entry : docEntries) {
            DocInfo docInfo = entry.getValue();
            if (docInfo.isHavingPortType()) {
                abstractWsdl = docInfo;
            }
            if (docInfo.getDocType() == DOC_TYPE.SCHEMA) {
                List<String> sysIds = nsMapping.get(docInfo.getTargetNamespace());
                if (sysIds == null) {
                    sysIds = new ArrayList<String>();
                    nsMapping.put(docInfo.getTargetNamespace(), sysIds);
                }
                sysIds.add(docInfo.getUrl().toString());
            }
        }
    }
    
    public String getWSDLFile() {
        return concreteWsdl.getUrl().toString();
    }
    
    public Map<String, DocInfo> getDocs() {
        return docs;
    }
    
    /*
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
     */
    
    public Result getSchemaOutput(String namespaceUri, String suggestedFileName) {
        ByteArrayBuffer bout = new ByteArrayBuffer();
        
        StreamDocInfo docInfo = new StreamDocInfo(suggestedFileName, bout);
        docInfo.setQueryString("xsd="+suggestedFileName);
        docInfo.setDocType(DOC_TYPE.SCHEMA);
        docs.put(docInfo.getUrl().toString(),  docInfo);
   
        StreamResult result = new StreamResult();
        result.setOutputStream(bout);
        result.setSystemId(docInfo.getUrl().toString());
        return result;
    }
    
    /*
     * return null if concrete WSDL need not be generated
     */
    public Result getWSDLOutput(String filename) {        
        ByteArrayBuffer bout = new ByteArrayBuffer();
        StreamDocInfo docInfo = new StreamDocInfo(filename, bout);
        docInfo.setDocType(DOC_TYPE.WSDL);
        docInfo.setQueryString("wsdl");
        concreteWsdl = docInfo;
        docs.put(docInfo.getUrl().toString(),  docInfo);
        StreamResult result = new StreamResult();
        result.setOutputStream(bout);
        result.setSystemId(docInfo.getUrl().toString());
        return result;
    }

    /*
     * Updates filename if the suggested filename need to be changed in
     * wsdl:import
     *
     * return null if abstract WSDL need not be generated
     */
    public Result getAbstractWSDLOutput(Holder<String> filename) {
        if (abstractWsdl != null) {
            filename.value = abstractWsdl.getUrl().toString();
            return null;                // Don't generate abstract WSDL
        }
        ByteArrayBuffer bout = new ByteArrayBuffer();
        StreamDocInfo abstractWsdl = new StreamDocInfo(filename.value, bout);
        abstractWsdl.setDocType(DOC_TYPE.WSDL);
        //abstractWsdl.setQueryString("wsdl="+filename.value);
        docs.put(abstractWsdl.getUrl().toString(),  abstractWsdl);
        StreamResult result = new StreamResult();
        result.setOutputStream(bout);
        result.setSystemId(abstractWsdl.getUrl().toString());
        return result;
    }

    /*
     * Updates filename if the suggested filename need to be changed in
     * xsd:import
     *
     * return null if schema need not be generated
     */
    public Result getSchemaOutput(String namespace, Holder<String> filename) {
        List<String> schemas = nsMapping.get(namespace);
        if (schemas != null) {
            if (schemas.size() > 1) {
                throw new ServerRtException("server.rt.err",
                    "More than one schema for the target namespace "+namespace);
            }
            filename.value = schemas.get(0);
            return null;            // Don't generate schema
        }
        ByteArrayBuffer bout = new ByteArrayBuffer();
        StreamDocInfo docInfo = new StreamDocInfo(filename.value, bout);
        docInfo.setDocType(DOC_TYPE.SCHEMA);
        //docInfo.setQueryString("xsd="+filename.value);
        docs.put(docInfo.getUrl().toString(),  docInfo);
        StreamResult result = new StreamResult();
        result.setOutputStream(bout);
        result.setSystemId(docInfo.getUrl().toString());
        return result;
    }
    
    private static class StreamDocInfo implements DocInfo {
        private ByteArrayBuffer bout;
        private String resource;
        private String queryString;
		private DOC_TYPE docType;
        
        public StreamDocInfo(String resource, ByteArrayBuffer bout) {
            this.resource = resource;
            this.bout = bout;
        }

        public InputStream getDoc() {
            bout.close();
            return bout.newInputStream();
        }

        public String getPath() {
            return resource;
        }
        
        public URL getUrl() {
            try {
                return new URL("file:///"+resource);
            } catch(Exception e) {
                
            }
            return null;
        }

        public String getQueryString() {
            return queryString;
        }

        public void setQueryString(String queryString) {
            this.queryString = queryString;
        }
        
        public void setDocType(DOC_TYPE docType) {
			this.docType = docType;
        }

        public DOC_TYPE getDocType() {
            return docType;
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

        public void setHavingPortType(boolean portType) {

        }

        public boolean isHavingPortType() {
            return false;
        }
    }

}
