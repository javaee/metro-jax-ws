/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.ws.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 *
 * @author Santiago.PericasGeertsen@sun.com
 * @author Paul.Sandoz@sun.com
 */
public class FastInfosetReflection {
    /**
     * 
     */
    public static ClassLoader fiClassLoader;
    
    /**
     * FI StAXDocumentParser constructor using reflection.
     */
    public static Constructor fiStAXDocumentParser_new;
    
    /**
     * FI <code>StAXDocumentParser.setInputStream()</code> method via reflection.
     */
    public static Method fiStAXDocumentParser_setInputStream;
    
    /**
     * FI <code>StAXDocumentParser.setStringInterning()</code> method via reflection.
     */
    public static Method fiStAXDocumentParser_setStringInterning;

    /**
     * FI StAXDocumentSerializer constructor using reflection.
     */
    public static Constructor fiStAXDocumentSerializer_new;
    
    /**
     * FI <code>StAXDocumentSerializer.setOutputStream()</code> method via reflection.
     */
    public static Method fiStAXDocumentSerializer_setOutputStream;
    
    /**
     * FI <code>StAXDocumentSerializer.setEncoding()</code> method via reflection.
     */
    public static Method fiStAXDocumentSerializer_setEncoding;
    
    /**
     * FI DOMDocumentParser constructor using reflection.
     */
    public static Constructor fiDOMDocumentParser_new;
    
    /**
     * FI <code>DOMDocumentParser.parse()</code> method via reflection.
     */
    public static Method fiDOMDocumentParser_parse;
    
    /**
     * FI DOMDocumentSerializer constructor using reflection.
     */
    public static Constructor fiDOMDocumentSerializer_new;
    
    /**
     * FI <code>FastInfosetSource.serialize(Document)</code> method via reflection.
     */
    public static Method fiDOMDocumentSerializer_serialize;
    
    /**
     * FI <code>FastInfosetSource.setOutputStream(OutputStream)</code> method via reflection.
     */
    public static Method fiDOMDocumentSerializer_setOutputStream;

    /**
     * FI FastInfosetSource class.
     */
    public static Class fiFastInfosetSource;

    /**
     * FI FastInfosetSource constructor using reflection.
     */
    public static Constructor fiFastInfosetSource_new;
    
    /**
     * FI <code>FastInfosetSource.getInputStream()</code> method via reflection.
     */
    public static Method fiFastInfosetSource_getInputStream;
       
    /**
     * FI <code>FastInfosetSource.setInputSTream()</code> method via reflection.
     */
    public static Method fiFastInfosetSource_setInputStream;
    
    /**
     * FI FastInfosetResult class using reflection.
     */
    public static Class fiFastInfosetResult;
    
    /**
     * FI FastInfosetResult constructor using reflection.
     */
    public static Constructor fiFastInfosetResult_new;
    
    /**
     * FI <code>FastInfosetResult.getOutputSTream()</code> method via reflection.
     */
    public static Method fiFastInfosetResult_getOutputStream;
    
    static {
        // Use reflection to avoid static dependency with FI jar
        try {
            Class clazz = null;
            clazz = Class.forName("com.sun.xml.fastinfoset.stax.StAXDocumentParser");
            fiStAXDocumentParser_new = clazz.getConstructor();
            fiStAXDocumentParser_setInputStream =
                clazz.getMethod("setInputStream", java.io.InputStream.class);
            fiStAXDocumentParser_setStringInterning =
                clazz.getMethod("setStringInterning", boolean.class);

            clazz =
                Class.forName("com.sun.xml.fastinfoset.stax.StAXDocumentSerializer");
            fiStAXDocumentSerializer_new = clazz.getConstructor();
            fiStAXDocumentSerializer_setOutputStream =
                clazz.getMethod("setOutputStream", java.io.OutputStream.class);
            fiStAXDocumentSerializer_setEncoding =
                clazz.getMethod("setEncoding", String.class);
            
            clazz =
                Class.forName("com.sun.xml.fastinfoset.dom.DOMDocumentParser");
            fiDOMDocumentParser_new = clazz.getConstructor();
            fiDOMDocumentParser_parse = clazz.getMethod("parse", 
                new Class[] { org.w3c.dom.Document.class, java.io.InputStream.class });
            
            clazz = Class.forName("com.sun.xml.fastinfoset.dom.DOMDocumentSerializer");
            fiDOMDocumentSerializer_new = clazz.getConstructor();
            fiDOMDocumentSerializer_serialize = clazz.getMethod("serialize", 
                new Class[] { org.w3c.dom.Node.class });
            fiDOMDocumentSerializer_setOutputStream = clazz.getMethod("setOutputStream",
                new Class[] { java.io.OutputStream.class });
            
            fiFastInfosetSource = clazz = Class.forName("org.jvnet.fastinfoset.FastInfosetSource");
            fiFastInfosetSource_new = clazz.getConstructor(
                new Class[] { java.io.InputStream.class });
            fiFastInfosetSource_getInputStream = clazz.getMethod("getInputStream");          
            fiFastInfosetSource_setInputStream = clazz.getMethod("setInputStream", 
                new Class[] { java.io.InputStream.class });          
            
            fiFastInfosetResult = clazz = Class.forName("org.jvnet.fastinfoset.FastInfosetResult");
            fiFastInfosetResult_new = clazz.getConstructor(
                new Class[] { java.io.OutputStream.class });
            fiFastInfosetResult_getOutputStream = clazz.getMethod("getOutputStream");           
        } 
        catch (Exception e) {
            // falls through
        }
    }

    // -- DOMDocumentParser ----------------------------------------------

    public static Object DOMDocumentParser_new() throws Exception {
        if (fiDOMDocumentParser_new == null) {
            throw new RuntimeException("Unable to locate Fast Infoset implementation");
        }
        return fiDOMDocumentParser_new.newInstance();
    }
    
    public static void DOMDocumentParser_parse(Object parser, 
        Document d, InputStream s) throws Exception 
    {
        if (fiDOMDocumentParser_parse == null) {
            throw new RuntimeException("Unable to locate Fast Infoset implementation");
        }
        fiDOMDocumentParser_parse.invoke(parser, d, s);
    }
    
    // -- DOMDocumentSerializer-------------------------------------------
    
    public static Object DOMDocumentSerializer_new() throws Exception {
        if (fiDOMDocumentSerializer_new == null) {
            throw new RuntimeException("Unable to locate Fast Infoset implementation");
        }
        return fiDOMDocumentSerializer_new.newInstance();
    }
    
    public static void DOMDocumentSerializer_serialize(Object serializer, Node node)
        throws Exception
    {
        if (fiDOMDocumentSerializer_serialize == null) {
            throw new RuntimeException("Unable to locate Fast Infoset implementation");
        }
        fiDOMDocumentSerializer_serialize.invoke(serializer, node);
    }
    
    public static void DOMDocumentSerializer_setOutputStream(Object serializer,
        OutputStream os) throws Exception
    {
        if (fiDOMDocumentSerializer_setOutputStream == null) {
            throw new RuntimeException("Unable to locate Fast Infoset implementation");
        }
        fiDOMDocumentSerializer_setOutputStream.invoke(serializer, os);
    }
    
    // -- FastInfosetSource ----------------------------------------------
    
    public static boolean isFastInfosetSource(Source source) {
        return source.getClass() == fiFastInfosetSource; 
    }
    
    public static Source FastInfosetSource_new(InputStream is) 
        throws Exception 
    {
        if (fiFastInfosetSource_new == null) {
            throw new RuntimeException("Unable to locate Fast Infoset implementation");
        }
        return (Source) fiFastInfosetSource_new.newInstance(is);        
    }
    
    public static InputStream FastInfosetSource_getInputStream(Source source) 
        throws Exception 
    {
        if (fiFastInfosetSource_getInputStream == null) {
            throw new RuntimeException("Unable to locate Fast Infoset implementation");
        }
        return (InputStream) fiFastInfosetSource_getInputStream.invoke(source);
    }
    
    public static void FastInfosetSource_setInputStream(Source source,
        InputStream is) throws Exception
    {
        if (fiFastInfosetSource_setInputStream == null) {
            throw new RuntimeException("Unable to locate Fast Infoset implementation");
        }
        fiFastInfosetSource_setInputStream.invoke(source, is);
    }
    
    // -- FastInfosetResult ----------------------------------------------
    
    public static boolean isFastInfosetResult(Result result) {
        return result.getClass() == fiFastInfosetResult;
    }
    
    public static Result FastInfosetResult_new(OutputStream os) 
        throws Exception 
    {
        if (fiFastInfosetResult_new == null) {
            throw new RuntimeException("Unable to locate Fast Infoset implementation");
        }
        return (Result) fiFastInfosetResult_new.newInstance(os);                
    }
    
    public static OutputStream FastInfosetResult_getOutputStream(Result result) 
        throws Exception 
    {
        if (fiFastInfosetResult_getOutputStream == null) {
            throw new RuntimeException("Unable to locate Fast Infoset implementation");
        }
        return (OutputStream) fiFastInfosetResult_getOutputStream.invoke(result);
    }
}
