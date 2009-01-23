package com.sun.tools.ws.api;

/**
 * Allows to customize wsgen behaviour using this extension.
 * The extension implementations are found using service
 * discovery mechanism i.e. JAX-WS tooltime locates
 * {@link WsgenExtension}s through the
 * <tt>META-INF/services/com.sun.tools.ws.api.WsgenExtension</tt>
 * files.
 *
 * {@link WsgenProtocol} annotation can be specified on the
 * extensions to extend -wsdl[:protocol] behaviour.
 * 
 * @author Jitendra Kotamraju
 * @since JAX-WS RI 2.1.6
 * @see WsgenProtocol
 */
public abstract class WsgenExtension {
}
