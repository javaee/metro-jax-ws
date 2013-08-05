/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.streaming;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.api.WstxInputProperties;

public class XMLInputFactoryPropertiesConfig {
	private static final Logger LOGGER = Logger.getLogger(XMLInputFactoryPropertiesConfig.class.getName());

    // XML size limitation

    public final static String PROPERTY_MAX_ATTRIBUTES_PER_ELEMENT = "xml.ws.maximum.AttributesPerElement";
    public final static String PROPERTY_MAX_ATTRIBUTE_SIZE = "xml.ws.maximum.AttributeSize";
    public final static String PROPERTY_MAX_CHILDREN_PER_ELEMENT = "xml.ws.maximum.ChildrenPerElement";
    public final static String PROPERTY_MAX_ELEMENT_COUNT = "xml.ws.maximum.ElementCount";
    public final static String PROPERTY_MAX_ELEMENT_DEPTH = "xml.ws.maximum.ElementDepth";
    public final static String PROPERTY_MAX_CHARACTERS = "xml.ws.maximum.Characters";


    private static final int DEFAULT_MAX_ATTRIBUTES_PER_ELEMENT = 500;
    private static final int DEFAULT_MAX_ATTRIBUTE_SIZE = 65536 * 8;
    private static final int DEFAULT_MAX_CHILDREN_PER_ELEMENT = Integer.MAX_VALUE;
    private static final int DEFAULT_MAX_ELEMENT_DEPTH = 500;
    private static final long DEFAULT_MAX_ELEMENT_COUNT = Integer.MAX_VALUE;
    private static final long DEFAULT_MAX_CHARACTERS = Long.MAX_VALUE;

    /* Woodstox default setting:
     int mMaxAttributesPerElement = 1000;
     int mMaxAttributeSize = 65536 * 8;
     int mMaxChildrenPerElement = Integer.MAX_VALUE;
     int mMaxElementDepth = 1000;
     long mMaxElementCount = Long.MAX_VALUE;
     long mMaxCharacters = Long.MAX_VALUE;
     */

    private int maxAttributesPerElement = DEFAULT_MAX_ATTRIBUTES_PER_ELEMENT;
    private int maxAttributeSize = DEFAULT_MAX_ATTRIBUTE_SIZE;
    private int maxChildrenPerElement = DEFAULT_MAX_CHILDREN_PER_ELEMENT;
    private int maxElementDepth = DEFAULT_MAX_ELEMENT_DEPTH;
    private long maxElementCount = DEFAULT_MAX_ELEMENT_COUNT;
    private long maxCharacters = DEFAULT_MAX_CHARACTERS;

    public static void configXMLInputFactory(XMLInputFactory xif ) {
        if (xif instanceof WstxInputFactory) {
             configWstxInputFactory((WstxInputFactory) xif);
        }
    }

    private static void configWstxInputFactory(WstxInputFactory xif) {
        XMLInputFactoryPropertiesConfig propertiesConfig = XMLInputFactoryPropertiesConfig.builder().build();
        
        Integer maxAttributesPerElement = Integer.valueOf(propertiesConfig.getMaxAttributesPerElement());
		xif.setProperty(WstxInputProperties.P_MAX_ATTRIBUTES_PER_ELEMENT,
				maxAttributesPerElement);
        
        Integer maxAttributeSize = Integer.valueOf(propertiesConfig.getMaxAttributeSize());
		xif.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE,
				maxAttributeSize);
        
        Integer maxChildrenPerElement = Integer.valueOf(propertiesConfig.getMaxChildrenPerElement());
		xif.setProperty(WstxInputProperties.P_MAX_CHILDREN_PER_ELEMENT,
                maxChildrenPerElement);
        
        Integer maxElementDepth = Integer.valueOf(propertiesConfig.getMaxElementDepth());
		xif.setProperty(WstxInputProperties.P_MAX_ELEMENT_DEPTH,
                maxElementDepth);
        
        Long maxElementCount = Long.valueOf(propertiesConfig.getMaxElementCount());
		xif.setProperty(WstxInputProperties.P_MAX_ELEMENT_COUNT,
                maxElementCount);
        
        Long maxCharacters = Long.valueOf(propertiesConfig.getMaxCharacters());
		xif.setProperty(WstxInputProperties.P_MAX_CHARACTERS,
                maxCharacters);
		
		if(LOGGER.isLoggable(Level.FINE)){
			LOGGER.log(Level.FINE, WstxInputProperties.P_MAX_ATTRIBUTES_PER_ELEMENT + " is " + maxAttributesPerElement);
			LOGGER.log(Level.FINE, WstxInputProperties.P_MAX_ATTRIBUTE_SIZE+" is " + maxAttributeSize);
			LOGGER.log(Level.FINE, WstxInputProperties.P_MAX_CHILDREN_PER_ELEMENT + " is " + maxChildrenPerElement);
			LOGGER.log(Level.FINE, WstxInputProperties.P_MAX_ELEMENT_DEPTH +" is " + maxElementDepth);
			LOGGER.log(Level.FINE, WstxInputProperties.P_MAX_ELEMENT_COUNT+" is " + maxElementCount);
			LOGGER.log(Level.FINE, WstxInputProperties.P_MAX_CHARACTERS+" is " + maxCharacters);
		}
    }

    private int getMaxAttributesPerElement() {
        return maxAttributesPerElement;
    }

    private int getMaxAttributeSize() {
        return maxAttributeSize;
    }

    private int getMaxChildrenPerElement() {
        return maxChildrenPerElement;
    }

    private int getMaxElementDepth() {
        return maxElementDepth;
    }

    private long getMaxElementCount() {
        return maxElementCount;
    }

    private long getMaxCharacters() {
        return maxCharacters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private XMLInputFactoryPropertiesConfig configValues = null;

        Builder() {
            this.buildXMLInputFactoryPropertiesConfig();
        }

        public XMLInputFactoryPropertiesConfig build() {
            if (this.configValues == null) {
                this.buildXMLInputFactoryPropertiesConfig();
            }
            return this.configValues;
        }

        private void buildXMLInputFactoryPropertiesConfig() {
            this.configValues = new XMLInputFactoryPropertiesConfig();
            this.buildMaxAttributesPerElement();
            this.buildMaxAttributeSize();
            this.buildMaxCharacters();
            this.buildMaxChildrenPerElement();
            this.buildMaxElementCount();
            this.buildMaxElementDepth();
        }

        private void buildMaxAttributesPerElement() {
            configValues.maxAttributesPerElement = this.buildIntegerValue(
                    XMLInputFactoryPropertiesConfig.PROPERTY_MAX_ATTRIBUTES_PER_ELEMENT,
                    XMLInputFactoryPropertiesConfig.DEFAULT_MAX_ATTRIBUTES_PER_ELEMENT);
        }

        private void buildMaxAttributeSize() {
            configValues.maxAttributeSize = this.buildIntegerValue(
                    XMLInputFactoryPropertiesConfig.PROPERTY_MAX_ATTRIBUTE_SIZE,
                    XMLInputFactoryPropertiesConfig.DEFAULT_MAX_ATTRIBUTE_SIZE);
        }

        private void buildMaxChildrenPerElement() {
            configValues.maxChildrenPerElement = this.buildIntegerValue(
                    XMLInputFactoryPropertiesConfig.PROPERTY_MAX_CHILDREN_PER_ELEMENT,
                    XMLInputFactoryPropertiesConfig.DEFAULT_MAX_CHILDREN_PER_ELEMENT);
        }

        private void buildMaxElementDepth() {
            configValues.maxElementDepth = this.buildIntegerValue(
                    XMLInputFactoryPropertiesConfig.PROPERTY_MAX_ELEMENT_DEPTH,
                    XMLInputFactoryPropertiesConfig.DEFAULT_MAX_ELEMENT_DEPTH);
        }

        private void buildMaxElementCount() {
            configValues.maxElementCount = this.buildLongValue(
                    XMLInputFactoryPropertiesConfig.PROPERTY_MAX_ELEMENT_COUNT,
                    XMLInputFactoryPropertiesConfig.DEFAULT_MAX_ELEMENT_COUNT);
        }

        private void buildMaxCharacters() {
            configValues.maxCharacters = this.buildLongValue(
                    XMLInputFactoryPropertiesConfig.PROPERTY_MAX_CHARACTERS,
                    XMLInputFactoryPropertiesConfig.DEFAULT_MAX_CHARACTERS);
        }

        private int buildIntegerValue(String propertyName, int defaultValue) {
            String propVal = System.getProperty(propertyName);
            if (propVal != null && propVal.length() > 0) {
                try {
                    Integer value = Integer.parseInt(propVal);
                    if (value > 0) {
                        // return with the value in System property
                        return value;
                    }
                } catch (NumberFormatException nfe) {
                	if(LOGGER.isLoggable(Level.WARNING))
                		LOGGER.log(Level.WARNING, "Ignoring error when building integer value", nfe);
                }
            }
            // return with the default value
            return defaultValue;
        }

        private long buildLongValue(String propertyName, long defaultValue) {
            String propVal = System.getProperty(propertyName);
            if (propVal != null && propVal.length() > 0) {
                try {
                    long value = Long.parseLong(propVal);
                    if (value > 0L) {
                        // return with the value in System property
                        return value;
                    }
                } catch (NumberFormatException nfe) {
                    // defult will be returned
                	if(LOGGER.isLoggable(Level.WARNING))
                		LOGGER.log(Level.WARNING, "Ignoring error when building long value", nfe);
                }
            }
            // return with the default value
            return defaultValue;
        }
    }
}