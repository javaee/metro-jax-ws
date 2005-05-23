/**
 * $Id: SOAPBinding.java,v 1.1 2005-05-23 22:10:50 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws;
import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;

@Target({TYPE, METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SOAPBinding {
    
    public enum Style {
       DOCUMENT,
       RPC,
    };
   
    public enum Use {
   	LITERAL,
	ENCODED,
    };

    public enum ParameterStyle {
   	BARE,
	WRAPPED,
    };

    Style style() default Style.DOCUMENT;
    Use use() default Use.LITERAL;
    ParameterStyle parameterStyle() default ParameterStyle.WRAPPED;

    public static class MySOAPBinding implements javax.jws.soap.SOAPBinding {
        javax.jws.soap.SOAPBinding.Style style;
        javax.jws.soap.SOAPBinding.Use use;
        javax.jws.soap.SOAPBinding.ParameterStyle paramStyle;
        
        public MySOAPBinding(SOAPBinding binding) {
            this.style = binding.style().equals(SOAPBinding.Style.DOCUMENT) ?
                         javax.jws.soap.SOAPBinding.Style.DOCUMENT : javax.jws.soap.SOAPBinding.Style.RPC;
            this.use = binding.use().equals(SOAPBinding.Use.LITERAL) ?
                       javax.jws.soap.SOAPBinding.Use.LITERAL : javax.jws.soap.SOAPBinding.Use.ENCODED;
            this.paramStyle = binding.parameterStyle().equals(SOAPBinding.ParameterStyle.WRAPPED) ?
                              javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED : javax.jws.soap.SOAPBinding.ParameterStyle.BARE;
        }
        public Style style() {
            return style;
        }
        public Use use() {
            return use;
        }
        public ParameterStyle parameterStyle() {
            return paramStyle;
        }
        public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return javax.jws.soap.SOAPBinding.class;
        }
    }
}
