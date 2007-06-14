package com.sun.xml.ws.server.sei;

import com.sun.istack.Nullable;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.JavaMethodImpl;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link EndpointMethodDispatcher} that uses SOAPAction as the key for dispatching.
 * <p/>
 * A map of all SOAPAction on the port and the corresponding {@link EndpointMethodHandler}
 * is initialized in the constructor. The SOAPAction from the
 * request {@link Packet} is used as the key to return the correct handler.
 *
 * @author Jitendra Kotamraju
 */
final class SOAPActionBasedDispatcher implements EndpointMethodDispatcher {
    private final Map<String, EndpointMethodHandler> methodHandlers;

    public SOAPActionBasedDispatcher(AbstractSEIModelImpl model, WSBinding binding, SEIInvokerTube invokerTube) {
        // Find if any SOAPAction repeat for operations
        Map<String, Integer> unique = new HashMap<String, Integer>();
        for(JavaMethodImpl m : model.getJavaMethods()) {
            String soapAction = m.getOperation().getSOAPAction();
            Integer count = unique.get(soapAction);
            if (count == null) {
                unique.put(soapAction, 1);
            } else {
                unique.put(soapAction, ++count);
            }
        }
        methodHandlers = new HashMap<String, EndpointMethodHandler>();
        for( JavaMethodImpl m : model.getJavaMethods() ) {
            String soapAction = m.getOperation().getSOAPAction();
            // Set up method handlers only for unique SOAPAction values so
            // that dispatching happens consistently for a method
            if (unique.get(soapAction) == 1) {
                methodHandlers.put('"'+soapAction+'"', new EndpointMethodHandler(invokerTube,m,binding));
            }
        }
    }

    public @Nullable EndpointMethodHandler getEndpointMethodHandler(Packet request) {
        return request.soapAction == null ? null : methodHandlers.get(request.soapAction);
    }

}
