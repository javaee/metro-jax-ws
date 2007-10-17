package com.sun.xml.ws.util;

import com.sun.istack.NotNull;
import com.sun.xml.ws.server.SDDocumentImpl;

import java.util.*;

import org.xml.sax.EntityResolver;

/**
 * WSDL, schema document metadata utility class.
 *
 * @author Jitendra Kotamraju
 */
public class MetadataUtil {

    /**
     * Gets closure of all the referenced documents from the primary document(typically
     * the service WSDL). It traverses the WSDL and schema imports and builds a closure
     * set of documents.
     *
     * @param systemId primary wsdl or the any root document
     * @param resolver used to get SDDocumentImpl for a document
     * @param onlyTopLevelSchemas if true, the imported schemas from a schema would be ignored
     * @return all the documents
     */
    public static Map<String, SDDocumentImpl> getMetadataClosure(@NotNull String systemId,
            @NotNull MetadataResolver resolver, boolean onlyTopLevelSchemas) {
        Map <String, SDDocumentImpl> closureDocs = new HashMap<String, SDDocumentImpl>();
        Set<String> remaining = new HashSet<String>();
        remaining.add(systemId);

        while(!remaining.isEmpty()) {
            Iterator<String> it = remaining.iterator();
            String current = it.next();
            remaining.remove(current);

            SDDocumentImpl currentDoc = resolver.resolveEntity(current);
            SDDocumentImpl old = closureDocs.put(currentDoc.getSystemId().toExternalForm(), currentDoc);
            assert old == null;

            Set<String> imports =  currentDoc.getImports();
            if (!currentDoc.isSchema() || !onlyTopLevelSchemas) {
                for(String importedDoc : imports) {
                    if (closureDocs.get(importedDoc) == null) {
                        remaining.add(importedDoc);
                    }
                }
            }
        }

        return closureDocs;
    }

    public interface MetadataResolver {
        /**
         * returns {@link SDDocumentImpl} for the give systemId. It
         * parses the document and categorises as WSDL, schema etc.
         * The implementation could use a catlog resolver or an entity
         * resolver {@link EntityResolver} before parsing.
         *
         * @param systemId document's systemId
         * @return document for the systemId
         */
        @NotNull SDDocumentImpl resolveEntity(String systemId);
    }

}
