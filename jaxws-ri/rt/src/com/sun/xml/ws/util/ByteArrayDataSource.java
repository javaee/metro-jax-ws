/**
 * $Id: ByteArrayDataSource.java,v 1.1 2005-09-23 23:14:05 kohlert Exp $
 */

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * {@link DataSource} backed by a byte buffer.
 * 
 * @author Kohsuke Kawaguchi
 */
public final class ByteArrayDataSource implements DataSource {

    private final String contentType;
    private final byte[] buf;
    private final int len;

    public ByteArrayDataSource(byte[] buf, String contentType) {
        this(buf,buf.length,contentType);
    }
    public ByteArrayDataSource(byte[] buf, int length, String contentType) {
        this.buf = buf;
        this.len = length;
        this.contentType = contentType;
    }

    public String getContentType() {
        if(contentType==null)
            return "application/octet-stream";
        return contentType;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(buf,0,len);
    }

    public String getName() {
        return null;
    }

    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }
}
