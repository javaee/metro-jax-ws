/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.util;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads a input stream completely and creates a new stream
 * by keeping some data in memory and the rest on the file system.
 *
 * @author Jitendra Kotamraju
 */
public class ReadAllStream extends InputStream {

    private final @NotNull MemoryStream memStream;
    private final @NotNull FileStream fileStream;

    private boolean readAll;
    private boolean closed;

    private static final Logger LOGGER = Logger.getLogger(ReadAllStream.class.getName());
    
    public ReadAllStream() {
        memStream = new MemoryStream();
        fileStream = new FileStream();
    }

    /**
     * Reads the data from input stream completely. It keeps
     * inMemory size in the memory, and the rest on the file
     * system.
     *
     * Caller's responsibility to close the InputStream. This
     * method can be called only once.
     *
     * @param in from which to be read
     * @param inMemory this much data is kept in the memory
     * @throws IOException in case of exception
     */
    public void readAll(InputStream in, long inMemory) throws IOException {
        assert !readAll;
        readAll = true;

        boolean eof = memStream.readAll(in, inMemory);
        if (!eof) {
            fileStream.readAll(in);
        }
    }

    @Override
    public int read() throws IOException {
        int ch = memStream.read();
        if (ch == -1) {
            ch = fileStream.read();
        }
        return ch;
    }
    
    @Override
    public int read(byte b[], int off, int sz) throws IOException {
        int len = memStream.read(b, off, sz);
        if (len == -1) {
            len = fileStream.read(b, off, sz);
        }
        return len;
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            memStream.close();
            fileStream.close();
            closed = true;
        }
    }

    // Keeps the rest of the data on the file system
    private static class FileStream extends InputStream {
        private @Nullable File tempFile;
        private @Nullable FileInputStream fin;

        void readAll(InputStream in) throws IOException {
            tempFile = File.createTempFile("jaxws",".bin");
            FileOutputStream fileOut = new FileOutputStream(tempFile);
            try {
                byte[] buf = new byte[8192];
                int len;
                while((len=in.read(buf)) != -1) {
                    fileOut.write(buf, 0, len);
                }
            } finally {
                fileOut.close();
            }
            fin = new FileInputStream(tempFile);
        }

        @Override
        public int read() throws IOException {
            return (fin != null) ? fin.read() : -1;
        }

        @Override
        public int read(byte b[], int off, int sz) throws IOException {
            return (fin != null) ? fin.read(b, off, sz) : -1;
        }

        @Override
        public void close() throws IOException {
            if (fin != null) {
                fin.close();
            }
            if (tempFile != null) {
                boolean success = tempFile.delete();
                if (!success) {
                    LOGGER.log(Level.INFO, "File {0} could not be deleted", tempFile);
                }
            }
        }
    }

    // Keeps data in memory until certain size
    private static class MemoryStream extends InputStream {
        private Chunk head, tail;
        private int curOff;

        private void add(byte[] buf, int len) {
            if (tail != null) {
                tail = tail.createNext(buf, 0, len);
            } else {
                head = tail = new Chunk(buf, 0, len);
            }
        }

        /**
         * Reads until the size specified
         *
         * @param in stream from which to be read
         * @param inMemory reads until this size
         * @return true if eof
         *         false otherwise
         * @throws IOException in case of exception
         */
        boolean readAll(InputStream in, long inMemory) throws IOException {
            long total = 0;
            while(true) {
                byte[] buf = new byte[8192];
                int read = fill(in, buf);
                total += read;
                if (read != 0) {
                    add(buf, read);
                }
                if (read != buf.length) {
                    return true;
                }        // EOF
                if (total > inMemory) {
                    return false; // Reached in-memory size
                }       
            }
        }

        private int fill(InputStream in, byte[] buf) throws IOException {
            int read;
            int total = 0;
            while(total < buf.length && (read=in.read(buf, total, buf.length-total)) != -1) {
                total += read;
            }
            return total;
        }

        @Override
        public int read() throws IOException {
            if (!fetch()) {
                return -1;
            }
            return (head.buf[curOff++] & 0xff);
        }

        @Override
        public int read(byte b[], int off, int sz) throws IOException {
            if (!fetch()) {
                return -1;
            }
            sz = Math.min(sz, head.len-(curOff-head.off));
            System.arraycopy(head.buf,curOff,b,off,sz);
            curOff += sz;
            return sz;
        }

        // if eof, return false else true
        private boolean fetch() {
            if (head == null) {
                return false;
            }
            if (curOff == head.off+head.len) {
                head = head.next;
                if (head == null) {
                    return false;
                }
                curOff = head.off;
            }
            return true;
        }

        private static final class Chunk {
            Chunk next;
            final byte[] buf;
            final int off;
            final int len;

            public Chunk(byte[] buf, int off, int len) {
                this.buf = buf;
                this.off = off;
                this.len = len;
            }

            public Chunk createNext(byte[] buf, int off, int len) {
                return next = new Chunk(buf, off, len);
            }
        }
    }
}
