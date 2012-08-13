/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.util.exception;

import com.sun.istack.NotNull;
import com.sun.xml.ws.resources.UtilMessages;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;
import java.util.Arrays;
import java.util.List;

/**
 * {@link WebServiceException} with source location informaiton.
 *
 * <p>
 * This exception should be used wherever the location information is available,
 * so that the location information is carried forward to users (to assist
 * error diagnostics.)
 *
 * @author Kohsuke Kawaguchi
 */
public class LocatableWebServiceException extends WebServiceException {
    /**
     * Locations related to error.
     */
    private final Locator[] location;

    public LocatableWebServiceException(String message, Locator... location) {
        this(message,null,location);
    }

    public LocatableWebServiceException(String message, Throwable cause, Locator... location) {
        super(appendLocationInfo(message,location), cause);
        this.location = location;
    }

    public LocatableWebServiceException(Throwable cause, Locator... location) {
        this(cause.toString(),cause,location);
    }

    public LocatableWebServiceException(String message, XMLStreamReader locationSource) {
        this(message,toLocation(locationSource));
    }

    public LocatableWebServiceException(String message, Throwable cause, XMLStreamReader locationSource) {
        this(message,cause,toLocation(locationSource));
    }

    public LocatableWebServiceException(Throwable cause, XMLStreamReader locationSource) {
        this(cause,toLocation(locationSource));
    }

    /**
     * Locations related to this exception.
     *
     * @return
     *      Can be empty but never null.
     */
    public @NotNull List<Locator> getLocation() {
        return Arrays.asList(location);
    }

    private static String appendLocationInfo(String message, Locator[] location) {
        StringBuilder buf = new StringBuilder(message);
        for( Locator loc : location )
            buf.append('\n').append(UtilMessages.UTIL_LOCATION( loc.getLineNumber(), loc.getSystemId() ));
        return buf.toString();
    }

    private static Locator toLocation(XMLStreamReader xsr) {
        LocatorImpl loc = new LocatorImpl();
        Location in = xsr.getLocation();
        loc.setSystemId(in.getSystemId());
        loc.setPublicId(in.getPublicId());
        loc.setLineNumber(in.getLineNumber());
        loc.setColumnNumber(in.getColumnNumber());
        return loc;
    }
}
