/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.test.xbeandoc;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com.sun.xml.ws.test.xbeandoc package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sun.xml.ws.test.xbeandoc
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetCountryInfoResponse }
     *
     */
    public GetCountryInfoResponse createGetCountryInfoResponse() {
        return new GetCountryInfoResponse();
    }

    /**
     * Create an instance of {@link CountryInfoType }
     *
     */
    public CountryInfoType createCountryInfoType() {
        return new CountryInfoType();
    }

    /**
     * Create an instance of {@link GetCountryName }
     *
     */
    public GetCountryName createGetCountryName() {
        return new GetCountryName();
    }

    /**
     * Create an instance of {@link Countries }
     *
     */
    public Countries createCountries() {
        return new Countries();
    }

    /**
     * Create an instance of {@link GetCountryNameXml }
     *
     */
    public GetCountryNameXml createGetCountryNameXml() {
        return new GetCountryNameXml();
    }

    /**
     * Create an instance of {@link GetCountryInfo }
     *
     */
    public GetCountryInfo createGetCountryInfo() {
        return new GetCountryInfo();
    }

    /**
     * Create an instance of {@link AddCountry }
     *
     */
    public AddCountry createAddCountry() {
        return new AddCountry();
    }

    /**
     * Create an instance of {@link GetCountryNameResponse }
     *
     */
    public GetCountryNameResponse createGetCountryNameResponse() {
        return new GetCountryNameResponse();
    }

    /**
     * Create an instance of {@link AddCountryResponse }
     *
     */
    public AddCountryResponse createAddCountryResponse() {
        return new AddCountryResponse();
    }

    /**
     * Create an instance of {@link GetCountryNameXmlResponse }
     *
     */
    public GetCountryNameXmlResponse createGetCountryNameXmlResponse() {
        return new GetCountryNameXmlResponse();
    }

}
