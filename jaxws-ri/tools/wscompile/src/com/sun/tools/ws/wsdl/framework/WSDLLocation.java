/*
 * $Id: WSDLLocation.java,v 1.2 2005-07-18 18:14:21 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
