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

package com.sun.xml.ws.client;

/**
 * Content negotiation enum.
 * <p>
 * A value of {@link #none} means no content negotation at level of the 
 * client transport will be performed to negotiate the encoding of XML infoset.
 * The default encoding will always be used.
 * <p>
 * A value of {@link #pessimistic} means the client transport will assume
 * the default encoding of XML infoset for an outbound message unless informed
 * otherwise by a previously received inbound message.
 * (The client transport initially and pessimistically assumes that a service
 * does not support anything other than the default encoding of XML infoset.)
 * <p>
 * A value of {@link #optimistic} means the client transport will assume
 * a non-default encoding of XML infoset for an outbound message.
 * (The client transport optimistically assumes that a service
 * supports the non-default encoding of XML infoset.)
 *
 * @author Paul.Sandoz@Sun.Com
 */
public enum ContentNegotiation {
    none,
    pessimistic,
    optimistic;
  
    /**
     * Property name for content negotiation on {@link RequestContext}.
     */
    public static final String PROPERTY = "com.sun.xml.ws.client.ContentNegotiation";
    
    /**
     * Obtain the content negotiation value from a system property.
     * <p>
     * This method will never throw a runtime exception.
     *
     * @return the content negotiation value.
     */
    public static ContentNegotiation obtainFromSystemProperty() {
        try {
            String value = System.getProperty(PROPERTY);
            
            if (value == null) {
                return none;
            }
            
            return valueOf(value);
        } catch (Exception e) {
            // Default to none for any unrecognized value or any other
            // runtime exception thrown
            return none;
        }
    }
}
