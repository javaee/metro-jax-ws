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

package com.sun.xml.ws.developer;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;

import javax.xml.transform.Source;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents additional data to be added to EPRs
 * created from {@link StatefulWebServiceManager} (for advanced users).
 *
 * <p>
 * Occasionally it is convenient to be able to control the data to be
 * present on {@link EndpointReference}s created by {@link StatefulWebServiceManager}.
 * You can do so by using this class like this:
 *
 * <pre>
 * statefulWebServiceManager.export({@link W3CEndpointReference}.class,myObject,
 *   new EPRRecipe().addReferenceParameter({@link Headers}.create(...))
 *                  .addReferenceParameter({@link Headers}.create(...)));
 * </pre>
 *
 * <p>
 * The methods on this class follows <a href="http://www.martinfowler.com/bliki/FluentInterface.html">
 * the fluent interface design</a> to allow construction without using a variable.
 *
 *
 * <p>
 * See <a href="http://www.w3.org/TR/2006/REC-ws-addr-core-20060509/#eprinfomodel">
 * WS-Addressing EPR information model</a> for more details.
 *
 * @author Kohsuke Kawaguchi
 * @since 2.1.1
 * @see StatefulWebServiceManager
 * @see Headers
 */
public final class EPRRecipe {
    private final List<Header> referenceParameters = new ArrayList<Header>();
    private final List<Source> metadata = new ArrayList<Source>();

    /**
     * Gets all the reference parameters added so far.
     */
    public @NotNull List<Header> getReferenceParameters() {
        return referenceParameters;
    }

    /**
     * Gets all the metadata added so far.
     */
    public @NotNull List<Source> getMetadata() {
        return metadata;
    }

    /**
     * Adds a new reference parameter.
     */
    public EPRRecipe addReferenceParameter(Header h) {
        if(h==null) throw new IllegalArgumentException();
        referenceParameters.add(h);
        return this;
    }

    /**
     * Adds all the headers as reference parameters.
     */
    public EPRRecipe addReferenceParameters(Header... headers) {
        for (Header h : headers)
            addReferenceParameter(h);
        return this;
    }

    /**
     * Adds all the headers as reference parameters.
     */
    public EPRRecipe addReferenceParameters(Iterable<? extends Header> headers) {
        for (Header h : headers)
            addReferenceParameter(h);
        return this;
    }

    /**
     * Adds a new metadata.
     */
    public EPRRecipe addMetadata(Source source) {
        if(source==null)    throw new IllegalArgumentException();
        metadata.add(source);
        return this;
    }

    public EPRRecipe addMetadata(Source... sources) {
        for (Source s : sources)
            addMetadata(s);
        return this;
    }

    public EPRRecipe addMetadata(Iterable<? extends Source> sources) {
        for (Source s : sources)
            addMetadata(s);
        return this;
    }
}
