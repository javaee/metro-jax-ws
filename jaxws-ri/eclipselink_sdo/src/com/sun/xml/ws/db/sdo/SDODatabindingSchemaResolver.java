package com.sun.xml.ws.db.sdo;

import org.eclipse.persistence.sdo.helper.DefaultSchemaResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: May 13, 2009
 * Time: 1:15:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class SDODatabindingSchemaResolver extends DefaultSchemaResolver {

    public static final String CLASSPATH_SCHEME = "classpath";
    private List<Source> schemaList = null;
    private EntityResolver externalResolver = null;

    public SDODatabindingSchemaResolver(List<Source> schemas) {
        schemaList = schemas;
    }

    public SDODatabindingSchemaResolver(List<Source> schemas, EntityResolver resolver) {
        schemaList = schemas;
        externalResolver = resolver;
    }
    
    public Source resolveSchema(Source sourceXSD, String namespace,
                                String schemaLocation) {

        if (schemaLocation == null) {
            return null;
        }

        Source source = null;
        try {
            URI baseURI;
            if (sourceXSD != null && sourceXSD.getSystemId() != null) {
                baseURI = new URI(sourceXSD.getSystemId());

                String scheme = null;
                if (baseURI.isOpaque()) {
                    scheme = baseURI.getScheme();
                    baseURI = new URI(baseURI.getRawSchemeSpecificPart());
                }

                URI schemaLocationURI = baseURI.resolve(schemaLocation);
                if (scheme != null) {
                    schemaLocationURI = new URI(scheme + ":" + schemaLocationURI.toString());
                }
                source = loadSourceFromKnownSchemas(schemaList, schemaLocationURI);
                if (source != null) {
                    return source;
                }

                source = loadSourceFromURL(schemaLocationURI.toURL());

                if (source == null && externalResolver != null) {
                    InputSource inputSource = externalResolver.resolveEntity(schemaLocation, schemaLocationURI.toASCIIString());
                    if (inputSource != null) {
                        source = new SAXSource(inputSource);
                    }
                }
            } else {
                System.out.println("Base URI for " + schemaLocation + " can not be determined because source schema does not have systemID set");
            }
        }
        catch (Exception ee) {
            System.out.println("Exception trying to resolve schema for "
                    + schemaLocation + ": " + ee.getMessage());
        }

        if (source == null) {
            source = loadSourceFromClasspath(schemaLocation);
        }

        if (source == null) {
            System.out.println("Unable to resolve requested schema resource [" + schemaLocation + "]");
        }
        return source;


    }


    private Source loadSourceFromKnownSchemas(List<Source> schemas, URI schemaLocation) {
        if (schemas == null) {
            return null;
        }

        for (Source schema : schemas) {
            String sysId = schema.getSystemId();
            if (sysId != null  &&  sysId.equals(schemaLocation.toASCIIString())) {
                return schema;
            }
        }

        return null;
    }


    private Source loadSourceFromClasspath(String schemaLocation) {
        if (schemaLocation.startsWith(CLASSPATH_SCHEME)) {
            schemaLocation = schemaLocation.substring(CLASSPATH_SCHEME.length() + 1);
        }
        URL url = Thread.currentThread().getContextClassLoader().getResource(schemaLocation);
        if (url == null && schemaLocation.startsWith("/")) {
            url = Thread.currentThread().getContextClassLoader().getResource(schemaLocation.substring(1));
        }
        if (url != null) {
            return loadSourceFromURL(url);
        }
        return null;
    }


    private Source loadSourceFromURL(URL targetURL) {
        Source targetXSD = null;
        try {
            InputStream is = targetURL.openStream();
            targetXSD = new StreamSource(is);
            targetXSD.setSystemId(targetURL.toExternalForm());
        } catch (IOException e) {
            System.out.println("failed to load source from URL:"
                    + targetURL.toString() + ": " + e.getMessage());
            return null;
        }

        return targetXSD;
    }


}
