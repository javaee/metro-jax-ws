/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.runtime.config;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.config.metro.dev.FeatureReader;
import com.sun.xml.ws.config.metro.util.ParserUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.ws.WebServiceException;
import java.util.Iterator;

/**
 *
 * @author Fabian Ritzmann
 */
public class TubelineFeatureReader implements FeatureReader {

    private static final Logger LOGGER = Logger.getLogger(TubelineFeatureReader.class);
    private static final QName NAME_ATTRIBUTE_NAME = new QName("name");

    // TODO implement
    public TubelineFeature parse(XMLEventReader reader) throws WebServiceException {
        try {
            final StartElement element = reader.nextEvent().asStartElement();
            boolean attributeEnabled = true;
            final Iterator iterator = element.getAttributes();
            while (iterator.hasNext()) {
                final Attribute nextAttribute = (Attribute) iterator.next();
                final QName attributeName = nextAttribute.getName();
                if (ENABLED_ATTRIBUTE_NAME.equals(attributeName)) {
                    attributeEnabled = ParserUtil.parseBooleanValue(nextAttribute.getValue());
                } else if (NAME_ATTRIBUTE_NAME.equals(attributeName)) {
                    // TODO use name attribute
                } else {
                    // TODO logging message
                    throw LOGGER.logSevereException(new WebServiceException("Unexpected attribute"));
                }
            }
            return parseFactories(attributeEnabled, element, reader);
        } catch (XMLStreamException e) {
            throw LOGGER.logSevereException(new WebServiceException("Failed to unmarshal XML document", e));
        }
    }

    private TubelineFeature parseFactories(final boolean enabled, final StartElement element, final XMLEventReader reader)
            throws WebServiceException {
        int elementRead = 0;
        loop:
        while (reader.hasNext()) {
            try {
                final XMLEvent event = reader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.COMMENT:
                        break; // skipping the comments and start document events
                    case XMLStreamConstants.CHARACTERS:
                        if (event.asCharacters().isWhiteSpace()) {
                            break;
                        }
                        else {
                            // TODO: logging message
                            throw LOGGER.logSevereException(new WebServiceException("No character data allowed, was " + event.asCharacters()));
                        }
                    case XMLStreamConstants.START_ELEMENT:
                        // TODO implement
                        elementRead++;
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        elementRead--;
                        if (elementRead < 0) {
                            final EndElement endElement = event.asEndElement();
                            if (!element.getName().equals(endElement.getName())) {
                                // TODO logging message
                                throw LOGGER.logSevereException(new WebServiceException("End element does not match " + endElement));
                            }
                            break loop;
                        }
                        else {
                            break;
                        }
                    default:
                        // TODO logging message
                        throw LOGGER.logSevereException(new WebServiceException("Unexpected event, was " + event));
                }
            } catch (XMLStreamException e) {
                // TODO logging message
                throw LOGGER.logSevereException(new WebServiceException("Failed to unmarshal XML document", e));
            }
        }

        // TODO implement
        return new TubelineFeature(enabled);
    }

}
