/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.tools.ws.util;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * {@link EntityResolver} that delegates to two {@link EntityResolver}s.
 *
 * @author WS Development Team
 */
public class ForkEntityResolver implements EntityResolver {
    private final EntityResolver lhs;
    private final EntityResolver rhs;

    public ForkEntityResolver(EntityResolver lhs, EntityResolver rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        InputSource is = lhs.resolveEntity(publicId, systemId);
        if(is!=null)
            return is;
        return rhs.resolveEntity(systemId,systemId);
    }
}
