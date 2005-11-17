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

package com.sun.tools.ws.wsdl.document;

import com.sun.tools.ws.wsdl.framework.Kind;

/**
 * Enumeration of the kind of entities that can be defined in a WSDL "definitions" element.
 *
 * @author WS Development Team
 */
public class Kinds {
    public static final Kind BINDING = new Kind("wsdl:binding");
    public static final Kind MESSAGE = new Kind("wsdl:message");
    public static final Kind PORT = new Kind("wsdl:port");
    public static final Kind PORT_TYPE = new Kind("wsdl:portType");
    public static final Kind SERVICE = new Kind("wsdl:service");

    private Kinds() {
    }
}
