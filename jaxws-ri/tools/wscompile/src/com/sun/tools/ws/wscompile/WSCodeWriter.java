/*
 * $Id: WSCodeWriter.java,v 1.1 2005-07-24 01:35:14 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.wscompile;

import java.io.File;
import java.io.IOException;

import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.tools.ws.processor.util.GeneratedFileInfo;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;

/**
 * {@link FileCodeWriter} implementation that notifies
 * JAX-WS about newly created files.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class WSCodeWriter extends FileCodeWriter {
    private final ProcessorEnvironment env;

    public WSCodeWriter( File outDir, ProcessorEnvironment _env ) throws IOException {
        super(outDir);
        this.env = _env;
    }

    protected File getFile(JPackage pkg, String fileName ) throws IOException {
        File f = super.getFile(pkg, fileName);

        // notify JAX-WS RI
        GeneratedFileInfo fi = new GeneratedFileInfo();
        fi.setType("JAXB"/*GeneratorConstants.FILE_TYPE_VALUETYPE*/);
        fi.setFile(f);
        env.addGeneratedFile(fi);
        // we can't really tell the file type, for we don't know
        // what this file is used for. Fortunately,
        // FILE_TYPE doesn't seem to be used, so it doesn't really
        // matter what we set.

        return f;
    }
}
