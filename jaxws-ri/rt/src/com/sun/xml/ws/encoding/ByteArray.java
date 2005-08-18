/**
 * $Id: ByteArray.java,v 1.1 2005-08-18 00:58:59 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding;


public class ByteArray {
    public final byte[] bytes;
    public final int offset;
    public final int length;

    public ByteArray(byte[] bytes, int offset, int len) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = len;
    }
}
