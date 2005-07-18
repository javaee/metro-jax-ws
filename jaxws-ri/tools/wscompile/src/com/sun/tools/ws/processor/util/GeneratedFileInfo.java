/*
 * $Id: GeneratedFileInfo.java,v 1.2 2005-07-18 18:14:05 kohlert Exp $
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
     * @param instance of the file to be added
     * @return void
     */
    public void setFile( File file ) {
        this.file = file;
    }

    /**
     * Adds the type of file it is the container
     *
     * @param Type string which specifices the type
     * @return void
     */
    public void setType( String type ) {
        this.type = type;
    }

    /**
     * Gets the file that got added
     *
     * @param none
     * @return File instance
     */
    public File getFile() {
        return( file );
    }

    /**
     * Get the file type that got added
     *
     * @param none
     * @return File type of datatype String
     */
    public String getType() {
        return ( type );
    }
}
