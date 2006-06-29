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
package com.sun.tools.ws.wsdl.framework;

/**
 *
 * Maintains wsdl:location context. This is used with
 * ParserContext, where one each WSDL being imported its location is pushed, this will be used
 * latter to resolve relative imports of schema in SchemaParser.
 *
 * @author WS Development Team
 */
public class WSDLLocation {
    WSDLLocation() {
        reset();
    }

    public void push() {
        int max = contexts.length;
        idPos++;
        if (idPos >= max) {
            LocationContext newContexts[] = new LocationContext[max * 2];
            System.arraycopy(contexts, 0, newContexts, 0, max);
            max *= 2;
            contexts = newContexts;
        }
        currentContext = contexts[idPos];
        if (currentContext == null) {
            contexts[idPos] = currentContext = new LocationContext();
        }
        if (idPos > 0) {
            currentContext.setParent(contexts[idPos - 1]);
        }

    }

    public void pop() {
        idPos--;
        if (idPos >= 0) {
            currentContext = contexts[idPos];
        }
    }

    public void reset() {
        contexts = new LocationContext[32];
        idPos = 0;
        contexts[idPos] = currentContext = new LocationContext();
    }

    public String getLocation() {
        return currentContext.getLocation();
    }

    public void setLocation(String loc) {
        currentContext.setLocation(loc);
    }

    private LocationContext[] contexts;
    private int idPos;
    private LocationContext currentContext;

    // LocationContext - inner class
    private static class LocationContext {
        void setLocation(String loc) {
            location = loc;
        }

        String getLocation() {
            return location;
        }

        void setParent(LocationContext parent) {
            parentLocation = parent;
        }

        private String location;
        private LocationContext parentLocation;
    }
}
