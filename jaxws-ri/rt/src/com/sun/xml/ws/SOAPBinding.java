/**
 * $Id: SOAPBinding.java,v 1.2 2005-07-12 23:33:42 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws;
import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;



/**
 * Replacement annotation for <code>the javax.jws.soap.SOAPBinding</code> annotation.  
 * This annotation can be placed on methods as well as classes.  This annotation should
 * go away with 181.next
 */
@Target({TYPE, METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SOAPBinding {
    
    /**
     * specifies the style of messages
     */
    public enum Style {
       /**
        * used to specify DOCUMENT style
        */
       DOCUMENT,
       /**
        * used to specify RPC style
        */
       RPC,
    };
   
    /**
     * specifies the use of messages.
     */
    public enum Use {
   	/**
   	 * specifies <code>LITERAL</code> use.
   	 */
   	LITERAL,
	/**
	 * specifies <code>ENCODED</code> use.
	 */
	ENCODED,
    };

    /**
     * determines whether method parameters represent the entire message body,
     * or whether the parameters are elements wrapped inside a top-level element
     * named after the operation.  Default <code>WRAPPED</code>.
     */
    public enum ParameterStyle {
   	/**
   	 * specifies <code>BARE</code> parameterStyle in other words, parameters represent
   	 * the entire message body.
   	 */
   	BARE,
	/**
	 * specifies <code>WRAPPED</code> parameterStyle, in other words,
	 * the paramters are wrapped inside a top-level element named after
	 * the operation.
	 */
	WRAPPED,
    };

    /**
     * gets the encoding style for messages.  One of <code>DOCUMENT</code> or
     * <code>RPC</code>.  Default <code>DOCUMENT</code>.
     */
    Style style() default Style.DOCUMENT;
    /**
     * gets the formatting style for messages.  One of <code>Literal</code> or
     * <code>ENCODED</code>. Default <code>LITERAL</code>.
     */
    Use use() default Use.LITERAL;
    /**
     * gets the parameterStyle of messages
     */
    ParameterStyle parameterStyle() default ParameterStyle.WRAPPED;

    /**
     * Implementation of <code>com.sun.xml.ws.SOAPBinding</code>
     */
    public static class MySOAPBinding implements javax.jws.soap.SOAPBinding {
        javax.jws.soap.SOAPBinding.Style style;
        javax.jws.soap.SOAPBinding.Use use;
        javax.jws.soap.SOAPBinding.ParameterStyle paramStyle;
        
        /**
         * intatiates a <code>MySOAPBinding</code> object from a <code>javax.jws.soap.SOAPBinding</code> object.
         * @param binding the binding to initialize the new instance from
         */
        public MySOAPBinding(SOAPBinding binding) {
            this.style = binding.style().equals(SOAPBinding.Style.DOCUMENT) ?
                         javax.jws.soap.SOAPBinding.Style.DOCUMENT : javax.jws.soap.SOAPBinding.Style.RPC;
            this.use = binding.use().equals(SOAPBinding.Use.LITERAL) ?
                       javax.jws.soap.SOAPBinding.Use.LITERAL : javax.jws.soap.SOAPBinding.Use.ENCODED;
            this.paramStyle = binding.parameterStyle().equals(SOAPBinding.ParameterStyle.WRAPPED) ?
                              javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED : javax.jws.soap.SOAPBinding.ParameterStyle.BARE;
        }
        /**
         * 
         */
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
