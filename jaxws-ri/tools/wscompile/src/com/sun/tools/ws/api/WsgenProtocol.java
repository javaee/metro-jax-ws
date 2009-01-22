package com.sun.tools.ws.api;

import java.lang.annotation.*;

/**
 * @author Jitendra Kotamraju
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WsgenProtocol {
    String token();
    String lexical();
}