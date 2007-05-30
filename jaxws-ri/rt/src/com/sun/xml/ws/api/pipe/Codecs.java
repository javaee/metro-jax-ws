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

package com.sun.xml.ws.api.pipe;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;

/**
 * Factory methods for some of the {@link Codec} implementations.
 *
 * <p>
 * This class provides methods to create codecs for SOAP/HTTP binding.
 * It allows to replace default SOAP envelope(primary part in MIME message)
 * codec in the whole Codec.
 *
 * <p>
 * This is a part of the JAX-WS RI internal API so that
 * {@link Tube} and transport implementations can reuse the implementations
 * done inside the JAX-WS.
 *
 * @author Jitendra Kotamraju
 * @author Kohsuke Kawaguchi
 */
public abstract class Codecs {

    /**
     * This creates a full {@link Codec} for SOAP binding using the primary
     * XML codec argument. The codec argument is used to encode/decode SOAP envelopes
     * while the returned codec is responsible for encoding/decoding the whole
     * message.
     *
     * <p>
     * Creates codecs can be set during the {@link Tube}line assembly process.
     *
     * @see ServerTubeAssemblerContext#setCodec(Codec)
     * @see ClientTubeAssemblerContext#setCodec(Codec)
     * 
     * @param binding binding of the webservice
     * @param xmlEnvelopeCodec SOAP envelope codec
     * @return non null codec to parse entire SOAP message(including MIME parts)
     */
    public static @NotNull SOAPBindingCodec createSOAPBindingCodec(WSBinding binding, StreamSOAPCodec xmlEnvelopeCodec) {
        return new com.sun.xml.ws.encoding.SOAPBindingCodec(binding, xmlEnvelopeCodec);
    }

    /**
     * Creates a default {@link Codec} that can be used to used to
     * decode XML infoset in SOAP envelope(primary part in MIME message). New codecs
     * can be written using this codec as delegate.
     *
     * @param version SOAP version of the binding
     * @return non null default xml codec
     */
    public static @NotNull
    StreamSOAPCodec createSOAPEnvelopeXmlCodec(@NotNull SOAPVersion version) {
        return com.sun.xml.ws.encoding.StreamSOAPCodec.create(version);
    }
}
