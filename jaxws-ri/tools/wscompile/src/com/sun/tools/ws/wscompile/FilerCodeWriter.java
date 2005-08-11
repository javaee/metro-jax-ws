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
import java.io.PrintWriter;

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
    
    private OutputStream out;
    
    public FilerCodeWriter(File outDir, ProcessorEnvironment env ) throws IOException {
        super(outDir, env);
        this.filer = env.getFiler();
    }
    
    
    public OutputStream open(JPackage pkg, String fileName) throws IOException {
        File file = getFile(pkg, fileName);
        String tmp = file.getName().substring(0, file.getName().length()-5);
        PrintWriter pw = filer.createSourceFile(pkg.name()+"."+tmp);
        pw.close();
        out = new FileOutputStream(file);
        return out;
    }
    

    public void close() throws IOException {
        super.close();
        if (out != null)
            out.close();
        out = null;
    }
}
