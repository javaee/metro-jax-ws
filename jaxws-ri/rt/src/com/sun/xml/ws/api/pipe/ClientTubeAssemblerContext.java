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
import com.sun.istack.Nullable;
import com.sun.xml.ws.addressing.WsaClientTube;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.client.ClientPipelineHook;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.helper.PipeAdapter;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.client.ClientSchemaValidationTube;
import com.sun.xml.ws.developer.SchemaValidationFeature;
import com.sun.xml.ws.handler.ClientLogicalHandlerTube;
import com.sun.xml.ws.handler.ClientMessageHandlerTube;
import com.sun.xml.ws.handler.ClientSOAPHandlerTube;
import com.sun.xml.ws.handler.HandlerTube;
import com.sun.xml.ws.protocol.soap.ClientMUTube;
import com.sun.xml.ws.transport.DeferredTransportPipe;
import com.sun.xml.ws.util.pipe.DumpTube;

import javax.xml.ws.soap.SOAPBinding;
import java.io.PrintStream;

/**
 * Factory for well-known {@link Tube} implementations
 * that the {@link TubelineAssembler} needs to use
 * to satisfy JAX-WS requirements.
 *
 * @author Jitendra Kotamraju
 */
public class ClientTubeAssemblerContext {

    private final @NotNull EndpointAddress address;
    private final @NotNull WSDLPort wsdlModel;
    private final @Nullable SEIModel seiModel;
    private final @NotNull WSService rootOwner;
    private final @NotNull WSBinding binding;
    private final @NotNull Container container;
    private @NotNull Codec codec;

    public ClientTubeAssemblerContext(@NotNull EndpointAddress address, @NotNull WSDLPort wsdlModel, @NotNull WSService rootOwner, @NotNull WSBinding binding) {
        this(address, wsdlModel, rootOwner, binding, Container.NONE);
    }

    public ClientTubeAssemblerContext(@NotNull EndpointAddress address, @NotNull WSDLPort wsdlModel,
                                      @NotNull WSService rootOwner, @NotNull WSBinding binding,
                                      @NotNull Container container) {
        // WSBinding is actually BindingImpl
        this(address, wsdlModel, rootOwner, binding, container, ((BindingImpl)binding).createCodec() );
    }

    public ClientTubeAssemblerContext(@NotNull EndpointAddress address, @NotNull WSDLPort wsdlModel,
                                      @NotNull WSService rootOwner, @NotNull WSBinding binding,
                                      @NotNull Container container, Codec codec) {
        this(address, wsdlModel, rootOwner, binding, container, codec, null);
    }

    public ClientTubeAssemblerContext(@NotNull EndpointAddress address, @NotNull WSDLPort wsdlModel,
                                      @NotNull WSService rootOwner, @NotNull WSBinding binding,
                                      @NotNull Container container, Codec codec, SEIModel seiModel) {
        this.address = address;
        this.wsdlModel = wsdlModel;
        this.rootOwner = rootOwner;
        this.binding = binding;
        this.container = container;
        this.codec = codec;
        this.seiModel = seiModel;
    }


    /**
     * The endpoint address. Always non-null. This parameter is taken separately
     * from {@link com.sun.xml.ws.api.model.wsdl.WSDLPort} (even though there's {@link com.sun.xml.ws.api.model.wsdl.WSDLPort#getAddress()})
     * because sometimes WSDL is not available.
     */
    public @NotNull EndpointAddress getAddress() {
        return address;
    }

    /**
     * The created pipeline will be used to serve this port.
     * Null if the service isn't associated with any port definition in WSDL,
     * and otherwise non-null.
     */
    public @Nullable WSDLPort getWsdlModel() {
        return wsdlModel;
    }

    /**
     * The pipeline is created for this {@link com.sun.xml.ws.api.WSService}.
     * Always non-null. (To be precise, the newly created pipeline
     * is owned by a proxy or a dispatch created from thsi {@link com.sun.xml.ws.api.WSService}.)
     */
    public @NotNull WSService getService() {
        return rootOwner;
    }

    /**
     * The binding of the new pipeline to be created.
     */
    public @NotNull WSBinding getBinding() {
        return binding;
    }

    /**
     * The created pipeline will use seiModel to get java concepts for the endpoint
     *
     * @return Null if the service doesn't have SEI model e.g. Dispatch,
     *         and otherwise non-null.
     */
    public @Nullable SEIModel getSEIModel() {
        return seiModel;
    }

    /**
     * Returns the Container in which the client is running
     *
     * @return Container in which client is running
     */
    public Container getContainer() {
        return container;
    }

    /**
     * creates a {@link Tube} that dumps messages that pass through.
     */
    public Tube createDumpTube(String name, PrintStream out, Tube next) {
        return new DumpTube(name, out, next);
    }

    /**
     * Creates a {@link Tube} that adds container specific security
     */
    public @NotNull Tube createSecurityTube(@NotNull Tube next) {
        ClientPipelineHook hook = container.getSPI(ClientPipelineHook.class);
        if (hook != null) {
            ClientPipeAssemblerContext ctxt = new ClientPipeAssemblerContext(address, wsdlModel,
                                      rootOwner, binding, container);
            return PipeAdapter.adapt(hook.createSecurityPipe(ctxt, PipeAdapter.adapt(next)));
        }
        return next;
    }

    /**
     * Creates a {@link Tube} that invokes protocol and logical handlers.
     */
    public Tube createWsaTube(Tube next) {
        if (binding instanceof SOAPBinding && AddressingVersion.isEnabled(binding) && wsdlModel!=null)
            return new WsaClientTube(wsdlModel, binding, next);
        else
            return next;
    }

    /**
     * Creates a {@link Tube} that invokes protocol and logical handlers.
     */
    public Tube createHandlerTube(Tube next) {
        HandlerTube cousinHandlerTube = null;
        //XML/HTTP Binding can have only LogicalHandlerPipe
        if (binding instanceof SOAPBinding) {
            //Add MessageHandlerTube
            HandlerTube messageHandlerTube = new ClientMessageHandlerTube(seiModel, binding, wsdlModel, next);
            next = cousinHandlerTube = messageHandlerTube;

            //Add SOAPHandlerTuber
            HandlerTube soapHandlerTube = new ClientSOAPHandlerTube(binding, next, cousinHandlerTube);            
            next = cousinHandlerTube = soapHandlerTube;
        }
        return new ClientLogicalHandlerTube(binding, next, cousinHandlerTube);
    }

    /**
     * Creates a {@link Tube} that performs SOAP mustUnderstand processing.
     * This pipe should be before HandlerPipes.
     */
    public Tube createClientMUTube(Tube next) {
        if(binding instanceof SOAPBinding)
            return new ClientMUTube(binding,next);
        else
            return next;
    }

    /**
     * creates a {@link Tube} that validates messages against schema
     */
    public Tube createValidationTube(Tube next) {
        if (binding instanceof SOAPBinding && binding.isFeatureEnabled(SchemaValidationFeature.class) && wsdlModel!=null)
            return new ClientSchemaValidationTube(binding, wsdlModel, next);
        else
            return next;
    }

    /**
     * Creates a transport pipe (for client), which becomes the terminal pipe.
     */
    public Tube createTransportTube() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        // wsgen generates a WSDL with the address attribute that says "REPLACE_WITH_ACTUAL_URL".
        // while it's technically correct to reject such address (since there's no transport registered
        // with it), it's desirable to allow the user a benefit of doubt, and wait until the runtime
        // to see if the user configures the endpoint address through request context.
        // DeferredTransportPipe is used for this purpose.
        //
        // Ideally, we shouldn't have @address at all for such cases, but due to the backward
        // compatibility and the fact that this attribute is mandatory, we have no option but
        // to check for REPLACE_WITH_ACTUAL_URL.
        if(address.toString().equals("") || address.toString().equals("REPLACE_WITH_ACTUAL_URL"))
            return new DeferredTransportPipe(cl,this);

        return TransportTubeFactory.create(cl, this);
    }

    /**
     * Gets the {@link Codec} that is set by {@link #setCodec} or the default codec
     * based on the binding.
     *
     * @return codec to be used for web service requests
     */
    public @NotNull Codec getCodec() {
        return codec;
    }

    /**
     * Interception point to change {@link Codec} during {@link Tube}line assembly. The
     * new codec will be used by jax-ws client runtime for encoding/decoding web service
     * request/response messages. The new codec should be used by the transport tubes.
     *
     * <p>
     * the codec should correctly implement {@link Codec#copy} since it is used while
     * serving requests concurrently.
     *
     * @param codec codec to be used for web service requests
     */
    public void setCodec(@NotNull Codec codec) {
        this.codec = codec;
    }

}
