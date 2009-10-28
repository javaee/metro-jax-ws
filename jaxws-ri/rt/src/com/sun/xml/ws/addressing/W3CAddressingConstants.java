/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.ws.addressing;

import javax.xml.namespace.QName;

import com.sun.xml.ws.api.addressing.AddressingVersion;

/**
 * Constants for W3C WS-Addressing version
 *
 * @author Arun Gupta
 */
public interface W3CAddressingConstants {
    public static final String WSA_NAMESPACE_NAME = "http://www.w3.org/2005/08/addressing";
    public static final String WSA_NAMESPACE_WSDL_NAME = "http://www.w3.org/2006/05/addressing/wsdl";

    public static final String WSAW_SERVICENAME_NAME = "ServiceName";
    public static final String WSAW_INTERFACENAME_NAME = "InterfaceName";
    public static final String WSAW_ENDPOINTNAME_NAME = "EndpointName";

    public static final String WSA_REFERENCEPROPERTIES_NAME = "ReferenceParameters";
    public static final QName WSA_REFERENCEPROPERTIES_QNAME = new QName(WSA_NAMESPACE_NAME, WSA_REFERENCEPROPERTIES_NAME);

    public static final String WSA_REFERENCEPARAMETERS_NAME = "ReferenceParameters";
    public static final QName WSA_REFERENCEPARAMETERS_QNAME = new QName(WSA_NAMESPACE_NAME, WSA_REFERENCEPARAMETERS_NAME);

    public static final String WSA_METADATA_NAME = "Metadata";
    public static final QName WSA_METADATA_QNAME = new QName(WSA_NAMESPACE_NAME, WSA_METADATA_NAME);

    public static final String WSA_ADDRESS_NAME = "Address";
    public static final QName WSA_ADDRESS_QNAME = new QName(WSA_NAMESPACE_NAME, WSA_ADDRESS_NAME);

    public static final String WSA_ANONYMOUS_ADDRESS = WSA_NAMESPACE_NAME + "/anonymous";
    public static final String WSA_NONE_ADDRESS = WSA_NAMESPACE_NAME + "/none";

    public static final String WSA_DEFAULT_FAULT_ACTION = WSA_NAMESPACE_NAME + "/fault";

    public static final String WSA_EPR_NAME = "EndpointReference";
    public static final QName WSA_EPR_QNAME = new QName(WSA_NAMESPACE_NAME, WSA_EPR_NAME);


    public static final String WSAW_USING_ADDRESSING_NAME = "UsingAddressing";
    public static final QName WSAW_USING_ADDRESSING_QNAME = new QName(WSA_NAMESPACE_WSDL_NAME, WSAW_USING_ADDRESSING_NAME);

    public static final QName INVALID_MAP_QNAME = new QName(WSA_NAMESPACE_NAME, "InvalidAddressingHeader");
    public static final QName MAP_REQUIRED_QNAME = new QName(WSA_NAMESPACE_NAME, "MessageAddressingHeaderRequired");
    public static final QName DESTINATION_UNREACHABLE_QNAME = new QName(WSA_NAMESPACE_NAME, "DestinationUnreachable");
    public static final QName ACTION_NOT_SUPPORTED_QNAME = new QName(WSA_NAMESPACE_NAME, "ActionNotSupported");
    public static final QName ENDPOINT_UNAVAILABLE_QNAME = new QName(WSA_NAMESPACE_NAME, "EndpointUnavailable");

    public static final String ACTION_NOT_SUPPORTED_TEXT = "The \"%s\" cannot be processed at the receiver";
    public static final String DESTINATION_UNREACHABLE_TEXT = "No route can be determined to reach %s";
    public static final String ENDPOINT_UNAVAILABLE_TEXT = "The endpoint is unable to process the message at this time";
    public static final String INVALID_MAP_TEXT = "A header representing a Message Addressing Property is not valid and the message cannot be processed";
    public static final String MAP_REQUIRED_TEXT = "A required header representing a Message Addressing Property is not present";

    public static final QName PROBLEM_ACTION_QNAME = new QName(WSA_NAMESPACE_NAME, "ProblemAction");
    public static final QName PROBLEM_HEADER_QNAME_QNAME = new QName(WSA_NAMESPACE_NAME, "ProblemHeaderQName");
    public static final QName FAULT_DETAIL_QNAME = new QName(WSA_NAMESPACE_NAME, "FaultDetail");

    // Fault subsubcode when an invalid address is specified.
    public static final QName INVALID_ADDRESS_SUBCODE = new QName(WSA_NAMESPACE_NAME, "InvalidAddress",
                                                                  AddressingVersion.W3C.getPrefix());

    // Fault subsubcode when an invalid header was expected to be EndpointReference but was not valid.
    public static final QName INVALID_EPR = new QName(WSA_NAMESPACE_NAME, "InvalidEPR", AddressingVersion.W3C.getPrefix());

    // Fault subsubcode when greater than expected number of the specified header is received.
    public static final QName INVALID_CARDINALITY = new QName(WSA_NAMESPACE_NAME, "InvalidCardinality",
                                                              AddressingVersion.W3C.getPrefix());

    // Fault subsubcode when an invalid header was expected to be EndpointReference but did not contain address.
    public static final QName MISSING_ADDRESS_IN_EPR = new QName(WSA_NAMESPACE_NAME, "MissingAddressInEPR",
                                                                 AddressingVersion.W3C.getPrefix());

    // Fault subsubcode when a header contains a message id that was a duplicate of one already received.
    public static final QName DUPLICATE_MESSAGEID = new QName(WSA_NAMESPACE_NAME, "DuplicateMessageID",
                                                              AddressingVersion.W3C.getPrefix());

    // Fault subsubcode when <code>Action</code> and <code>SOAPAction</code> for the mesage did not match.
    public static final QName ACTION_MISMATCH = new QName(WSA_NAMESPACE_NAME, "ActionMismatch",
                                                          AddressingVersion.W3C.getPrefix());

    // Fault subsubcode when the only address supported is the anonymous address.
    public static final QName ONLY_ANONYMOUS_ADDRESS_SUPPORTED = new QName(WSA_NAMESPACE_NAME, "OnlyAnonymousAddressSupported",
                                                                           AddressingVersion.W3C.getPrefix());

    //Fault subsubcode when anonymous address is not supported.
    public static final QName ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED = new QName(WSA_NAMESPACE_NAME, "OnlyNonAnonymousAddressSupported",
                                                                               AddressingVersion.W3C.getPrefix());

    public static final String ANONYMOUS_EPR = "<EndpointReference xmlns=\"http://www.w3.org/2005/08/addressing\">\n" +
            "    <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>\n" +
            "</EndpointReference>";
}
