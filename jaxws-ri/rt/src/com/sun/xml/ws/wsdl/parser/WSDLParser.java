/**
 * $Id: WSDLParser.java,v 1.1 2005-05-23 23:07:16 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import com.sun.xml.ws.streaming.XMLReader;
import com.sun.xml.ws.streaming.XMLReaderException;
import com.sun.xml.ws.streaming.XMLReaderFactory;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.wsdl.WSDLContext;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static com.sun.xml.ws.streaming.XMLReader.START;
import static com.sun.xml.ws.streaming.XMLReader.END;
import static com.sun.xml.ws.streaming.XMLReader.EOF;


/**
 * @author JAX-RPC Development Team
 */
public class WSDLParser {
    private String serviceName;
    private String location;
    private String systemId;
    private String bindingId;
    private WSDLContext wsdlContext;

    //todo:rename
    private LocalizableMessageFactory messageFactory =
        new LocalizableMessageFactory("com.sun.xml.ws.resources.wsdl");

    private static XMLReader reader;

    //used for debugging for the most part
    //do not remove
    private boolean isImport;
    private boolean importsDone;
    private boolean bindingDone;
    private boolean locationDone;
    private boolean portDone;
    private boolean serviceDone;

    public WSDLParser() {
        //portsLocationMap = new HashMap<String, QName>();
    }

    public WSDLContext getWSDLContext() {
        return wsdlContext;
    }

    /**
     * @param source
     * @param location
     * @return
     */

    public boolean isDone() {
        return (bindingDone() && locationDone());
    }

    public boolean isImport() {
        return isImport;
    }

    public void setIsImport(boolean imp) {
        isImport = imp;
    }

    public boolean importDone() {
        return importsDone;
    }

    public void setImportsDone(boolean imp) {
        importsDone = imp;
    }

    private boolean bindingDone() {
        return bindingDone;
    }

    private boolean locationDone() {
        return locationDone;
    }


    public WSDLContext parse(InputStream is, WSDLContext wsdlcontext)
        throws Exception {
        //wsdlContext = new WSDLContext();
        InputSource source = new InputSource(is);
        systemId = source.getSystemId();
        //System.out.println("Systemid = " + systemId);

        try {
            reader =
                XMLReaderFactory.newInstance().createXMLReader(is);
            return parseWSDL(reader, wsdlcontext);
        } catch (XMLReaderException e) {
            throw new Exception("wsdl.xmlReader", e);
        }
    }
    //todo: wsdl imports
    /*  public WSDLContext parseWSDL(XMLReader reader) {

           int state = BOF;
           do {
               //state = reader.next();
               switch (state) {
                   case START:
                       QName name = reader.getName();
                       if (WSDLConstants.QNAME_DEFINITIONS.equals(name)) {
                           reader.nextElementContent();

                       } else if (WSDLConstants.QNAME_BINDING.equals(name)) {
                           parseWSDLBinding(reader);

                       } else if (WSDLConstants.QNAME_SERVICE.equals(name)) {
                           parseWSDLService(reader);
                       } else {
                           reader.skipElement();
                           reader.nextElementContent();
                       }
                       break;
                   case END:
                       reader.nextElementContent();
                       break;
                       //case CHARS:
                       //writer.writeChars(reader.getValue());
               }
               if (isDone())
                   break;
           } while (state != EOF);


           while (reader.getState() != XMLReader.EOF)
               reader.next();

           reader.close();

           return wsdlContext;
       }
      */

    public WSDLContext parseWSDL(XMLReader reader, WSDLContext wsdlcontext) {

        int state = START;
        state = reader.next();
        do {

            switch (reader.getState()) {
                case START:
                    QName name = reader.getName();
                    //System.out.println("This is the current name " + name.toString());
                    if (WSDLConstants.QNAME_DEFINITIONS.equals(name)) {
                        //reader.next();
                        reader.nextElementContent();
                        //System.out.println("State " + ParserUtil.getStateName(reader));
                    } else if (WSDLConstants.QNAME_IMPORT.equals(name)) {
                        parseWSDLImport(reader, wsdlcontext);
                        reader.next();
                        reader.nextElementContent();
                        //System.out.println("State " + ParserUtil.getStateName(reader));
                    } else if (WSDLConstants.QNAME_DOCUMENTATION.equals(name)) {
                        //parseWSDLBinding(reader, wsdlcontext);
                        reader.skipElement();
                        reader.nextElementContent();
                        //System.out.println("State " + ParserUtil.getStateName(reader));
                    } else if (WSDLConstants.QNAME_BINDING.equals(name)) {
                        parseWSDLBinding(reader, wsdlcontext);
                        //System.out.println("State " + ParserUtil.getStateName(reader));
                    } else if (WSDLConstants.QNAME_SERVICE.equals(name)) {
                        parseWSDLService(reader, wsdlcontext);
                        //System.out.println("State " + ParserUtil.getStateName(reader));
                    } else {
                        reader.skipElement();
                        reader.nextElementContent();
                        //System.out.println("State " + ParserUtil.getStateName(reader));
                    }
                    break;
                case END:
                    reader.nextElementContent();
                    //System.out.println("State " + ParserUtil.getStateName(reader));
                    break;
                    //case CHARS:
                    //writer.writeChars(reader.getValue());
            }
            if (isDone()) //|| reader.getName().equals(WSDLConstants.QNAME_DEFINITIONS))
                break;
        } while (reader.getState() != EOF);


        while (reader.getState() != XMLReader.EOF)
            reader.next();

        reader.close();

        return (wsdlContext = wsdlcontext);
    }


    protected WSDLContext parseWSDLImport(XMLReader reader, WSDLContext wsdlcontext) {
        if (WSDLConstants.QNAME_IMPORT.equals(reader.getName())) {
            isImport = true;
            URL importURL = null;
            String importLocation =
                ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_LOCATION);
            URI temp = null;
            try {

                temp = new URI(importLocation);
                System.out.println("temp " + temp.toString());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            if (temp.isAbsolute()) {
                //System.out.println("temp is absolute");
                //System.out.println("isAbsolute");
                try {
                    importURL = temp.toURL();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }  else {

            String origWsdlPath = wsdlcontext.getOrigURLPath();
            //System.out.println("Orig path " + origWsdlPath);
            int index = origWsdlPath.lastIndexOf("/");
            String base = origWsdlPath.substring(0, index);
            //System.out.println("base = " + base);
            //is orig wsdl a file
            URI owsdlLoc = null;

            String host = null;
            int port = 0;
            try {
                owsdlLoc = new URI(wsdlcontext.getOrigWSDLLocation().toExternalForm());
                host = owsdlLoc.getHost();
                port = owsdlLoc.getPort();

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            if (owsdlLoc != null) {
                File file = null;
                if (owsdlLoc.getScheme().equals("file")) {
                    String path = owsdlLoc.getPath();
                    if (path != null) {
                        file = new File(path);
                        File parent = file.getParentFile();
                        if (parent.isDirectory()) {
                            try {

                                String absPath = parent.getCanonicalPath();
                                String importString = "file:/" +
                                    absPath + "/" + importLocation;
                                System.out.println("This is it");
                                importURL = new URL(importString);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                    //todo://needs to be update for http imports
                } else if (owsdlLoc.getScheme().equals("http"))
                    try {
                        importURL = new URL("http://" + host + ":" + port + base + "/" + importLocation);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
            }


            try {
                parse(importURL.openStream(), wsdlcontext);
                isImport = false;
            } catch (Exception e) {
                e.printStackTrace();
            }

            }
        }

        return null;
    }

    protected void parseWSDLBinding(XMLReader reader, WSDLContext wsdlcontext) {

        if (WSDLConstants.QNAME_BINDING.equals(reader.getName())) {
            reader.nextElementContent();

            int state = reader.getState();
            do {
                switch (reader.getState()) {
                    case START:
                        QName name = reader.getName();
                        //System.out.println("This is the current name " + name.toString());
                        if (WSDLConstants.NS_SOAP_BINDING.equals(reader.getName())) {
                            wsdlcontext.setBindingID(ParserUtil.getMandatoryAttribute(reader, WSDLConstants.ATTR_TRANSPORT));
                            reader.next();
                            reader.nextElementContent();
                        } else if (WSDLConstants.QNAME_DOCUMENTATION.equals(name)) {
                            //parseWSDLBinding(reader, wsdlcontext);
                            reader.skipElement();
                            reader.nextElementContent();
                            //System.out.println("State " + ParserUtil.getStateName(reader));

                        } else if (WSDLConstants.QNAME_OPERATION.equals(name)) {
                            //parseWSDLService(reader, wsdlcontext);
                            reader.skipElement();
                            reader.nextElementContent();
                            //System.out.println("State " + ParserUtil.getStateName(reader));
                        } else {
                            reader.skipElement();
                            reader.nextElementContent();
                            //System.out.println("State " + ParserUtil.getStateName(reader));
                        }
                        break;

                        //case CHARS:
                        //writer.writeChars(reader.getValue());
                }
                if (reader.getName().equals(WSDLConstants.QNAME_BINDING))
                    break;
            } while (reader.getState() != EOF);
            //System.out.println("end of import EOF");
            //System.out.println("State " + ParserUtil.getStateName(reader));
            reader.next();
            reader.nextElementContent();
        }
        //need to take into account http binding
        // }
    }

    protected void parseWSDLService(XMLReader reader, WSDLContext wsdlcontext) {

        if (WSDLConstants.QNAME_SERVICE.equals(reader.getName())) {
            //wsdlContext.setServiceName(ParserUtil.getMandatoryAttribute(reader, WSDLConstants.ATTR_NAME));
            reader.nextElementContent();

            //int state = reader.getState();
            do {
                switch (reader.getState()) {
                    case START:
                        QName name = reader.getName();
                        //System.out.println("This is the current name " + name.toString());

                        if (WSDLConstants.QNAME_PORT.equals(reader.getName())) {
                            parseWSDLPort(reader, wsdlcontext);
                            reader.next();
                            //System.out.println(ParserUtil.getStateName(reader));
                            reader.nextElementContent();
                            //System.out.println(ParserUtil.getStateName(reader));
                            //System.out.println("reader.getName end of port");
                            //reader.next(); //skip end of service
                        } else if (WSDLConstants.QNAME_DOCUMENTATION.equals(reader.getName())){
                            reader.skipElement();
                            reader.nextElementContent();
                        } else {
                            reader.skipElement();
                            reader.nextElementContent();
                        }
                        break;
                      case END:
                        reader.nextElementContent();
                        //case CHARS:
                        //writer.writeChars(reader.getValue());
                }
                //if (reader.getName().equals(WSDLConstants.QNAME_SERVICE))
               //     break;
            } while (reader.getState() != EOF);
            serviceDone = true;
            //System.out.println("end of import EOF");
            //System.out.println("State " + ParserUtil.getStateName(reader));
            reader.next();
            reader.nextElementContent();
            //System.out.println("End of Service " + reader.getName());
            //System.out.println("State at end of servervie" + ParserUtil.getStateName(reader));
            //if (reader.getName().equals(WSDLConstants.QNAME_DEFINITIONS))
            //    return;
        }
    }

    protected void parseWSDLPort(XMLReader reader, WSDLContext wsdlcontext) {

        if (WSDLConstants.QNAME_PORT.equals(reader.getName())) {
            String portName = ParserUtil.getMandatoryAttribute(reader, WSDLConstants.ATTR_NAME);
            reader.nextElementContent();

            //int state = reader.getState();
            do {
                switch (reader.getState()) {
                    case START:
                        QName name = reader.getName();
                        //System.out.println("This is the current name " + name.toString());
                        if (WSDLConstants.NS_SOAP_BINDING_ADDRESS.equals(reader.getName())) {
                            String endpoint = ParserUtil.getMandatoryAttribute(reader, WSDLConstants.ATTR_LOCATION);
                            wsdlcontext.addPort(new QName("", portName), endpoint);
                            locationDone = true;
                            reader.next();
                            reader.nextElementContent();
                            //System.out.println(ParserUtil.getStateName(reader));
                            QName ename = reader.getName();
                            //System.out.println("name in get port" + ename);
                            //System.out.println("");
                        } else {
                            reader.skipElement();
                            reader.nextElementContent();
                        }

                        break;
                    case END:
                        reader.nextElementContent();
                }
                if (reader.getName().equals(WSDLConstants.QNAME_PORT)) {
                    break;
                }

            } while (reader.getState() != EOF);
            portDone = true;
            //System.out.println("end of import EOF");
            //System.out.println("State " + ParserUtil.getStateName(reader));
            reader.next();
            reader.nextElementContent();
            //System.out.println("End of Service " + reader.getName());
            //System.out.println("State at end of servervie" + ParserUtil.getStateName(reader));

        }

    }


    /*{
    if (location != null) {
                // NOTE - here we would really benefit from a URI class!
                String adjustedLocation =
                    source.getSystemId() == null
                        ? (context.getDocument().getSystemId() == null
                            ? location
                            : Util.processSystemIdWithBase(
                                context.getDocument().getSystemId(),
                                location))
                        : Util.processSystemIdWithBase(
                            source.getSystemId(),
                            location);

                try {
                    if (!context
                        .getDocument()
                        .isImportedDocument(adjustedLocation)) {
                        context.getDocument().addImportedEntity(
                            parseDefinitions(
                                context,
                                new InputSource(adjustedLocation),
                                i.getNamespace()));
                        context.getDocument().addImportedDocument(
                            adjustedLocation);
                    }
                } catch (ParseException e) {
                    // hardly the cleanest solution, but it should
                }
    }
    */
}