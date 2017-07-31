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

package com.sun.xml.ws.api.server;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;

/**
 * Provides a way to filter {@link SDDocument} infoset while writing it. These
 * filter objects can be added to {@link ServiceDefinition} using
 * {@link ServiceDefinition#addFilter(SDDocumentFilter)}
 * 
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
public interface SDDocumentFilter {
    /**
     * Returns a wrapped XMLStreamWriter on top of passed-in XMLStreamWriter.
     * It works like any filtering API for e.g. {@link java.io.FilterOutputStream}.
     * The method returns a XMLStreamWriter that calls the same methods on original
     * XMLStreamWriter with some modified events. The end result is some infoset
     * is filtered before it reaches the original writer and the infoset writer
     * doesn't have to change any code to incorporate this filter.
     *
     * @param doc gives context for the filter. This should only be used to query
     *  read-only information. Calling doc.writeTo() may result in infinite loop.
     * @param w Original XMLStreamWriter
     * @return Filtering {@link XMLStreamWriter}
     */
    XMLStreamWriter filter(SDDocument doc, XMLStreamWriter w) throws XMLStreamException, IOException;
}
