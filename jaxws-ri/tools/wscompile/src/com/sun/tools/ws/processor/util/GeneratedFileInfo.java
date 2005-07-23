/*
 * $Id: GeneratedFileInfo.java,v 1.3 2005-07-23 04:11:01 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.util;

import java.io.File;

/**
 * A container to hold info on the files that get
 * generated.
 *
 * @author WS Development Team
 */
public class GeneratedFileInfo {

    /**
     * local variables
     */
    private File file = null;
    private String type = null;

    /* constructor */
    public GeneratedFileInfo() {}

    /**
     * Adds the file object to the container
     *
     * @param file instance of the file to be added
     */
    public void setFile( File file ) {
        this.file = file;
    }

    /**
     * Adds the type of file it is the container
     *
     * @param type string which specifices the type
     */
    public void setType( String type ) {
        this.type = type;
    }

    /**
     * Gets the file that got added
     *
     * @return File that got added
     */
    public File getFile() {
        return( file );
    }

    /**
     * Get the file type that got added
     *
     * @return File type of datatype String
     */
    public String getType() {
        return ( type );
    }
}
