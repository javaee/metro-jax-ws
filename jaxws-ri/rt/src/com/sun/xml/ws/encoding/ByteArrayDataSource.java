/**
 * $Id: ByteArrayDataSource.java,v 1.1 2005-05-24 17:48:13 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding;

import javax.activation.DataSource;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * DataSource for byte[]
 * @author Vivek Pandey
 */
public final class ByteArrayDataSource implements DataSource {

    private final String contentType;
    private final InputStream stream;

    public ByteArrayDataSource(InputStream is, String contentType) {
        this.stream = is;
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getInputStream() {
        return stream;
    }

    public String getName() {
        return null;
    }

    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }
}
