/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.wscompile;

import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import java.io.BufferedOutputStream;

import com.sun.mirror.apt.Filer;
        
/**
 * Writes all the source files using the specified Filer.
 * 
 * @author WS Development Team
 */
public class FilerCodeWriter extends WSCodeWriter {

    /** The Filer used to create files. */
    private final Filer filer;
    
    private Writer w;
    
    public FilerCodeWriter(File outDir, ProcessorEnvironment env ) throws IOException {
        super(outDir, env);
        this.filer = env.getFiler();
    }
    
    
    public Writer openSource(JPackage pkg, String fileName) throws IOException {
        String tmp = fileName.substring(0, fileName.length()-5);
        w = filer.createSourceFile(pkg.name()+"."+tmp);
        return w;
    }
    

    public void close() throws IOException {
        super.close();
        if (w != null)
            w.close();
        w = null;
    }
}
