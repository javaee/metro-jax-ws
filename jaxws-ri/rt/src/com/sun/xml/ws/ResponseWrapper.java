/**
 * $Id: ResponseWrapper.java,v 1.1 2005-05-23 22:10:50 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author Vivek Pandey
 *
 * Response wrapper
 */
@Target({METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseWrapper {
    String name()default "";
    String namespace() default "";
    String type() default "";
}
