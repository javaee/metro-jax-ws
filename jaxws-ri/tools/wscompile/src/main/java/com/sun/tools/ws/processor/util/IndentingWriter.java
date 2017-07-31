/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.ws.processor.util;

import com.sun.tools.ws.processor.generator.GeneratorException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.MessageFormat;

/**
 *
 * @author WS Development Team
 */
public class IndentingWriter extends BufferedWriter {

    private boolean beginningOfLine = true;
    private int currentIndent = 0;
    private int indentStep = 4;

    public IndentingWriter(Writer out) {
        super(out);
    }

    public IndentingWriter(Writer out,int step) {
        this(out);

        if (indentStep < 0) {
            throw new IllegalArgumentException("negative indent step");
        }
        indentStep = step;
    }

    public void write(int c) throws IOException {
        checkWrite();
        super.write(c);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        if (len > 0) {
            checkWrite();
        }
        super.write(cbuf, off, len);
    }

    public void write(String s, int off, int len) throws IOException {
        if (len > 0) {
            checkWrite();
        }
        super.write(s, off, len);
    }

    public void newLine() throws IOException {
        super.newLine();
        beginningOfLine = true;
    }

    protected void checkWrite() throws IOException {
        if (beginningOfLine) {
            beginningOfLine = false;
            int i = currentIndent;
            while (i > 0) {
                super.write(' ');
                -- i;
            }
        }
    }

    protected void indentIn() {
        currentIndent += indentStep;
    }

    protected void indentOut() {
        currentIndent -= indentStep;
        if (currentIndent < 0) {
            currentIndent = 0;
        }
    }

    public void pI() {
        indentIn();
    }

    public void pO() {
        indentOut();
    }

    public void pI(int levels) {
        for (int i = 0; i < levels; ++i) {
            indentIn();
        }
    }

    public void pO(int levels) {
        for (int i = 0; i < levels; ++i) {
            indentOut();
        }
    }

    public void p(String s) throws IOException {
        /*
        int tabCount = 0;
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) == '\t') {
                ++tabCount;
                indentIn();
            }
        }

        String printStr = s.substring(tabCount);
         */
        boolean canEncode = true;

        //bug fix: 4839636
        try{
            if(!canEncode(s)) {
                canEncode = false;
            }
        } catch (Throwable t) {

            // there was some exception, what should we do?
            // lets ignore it for now and proceed with the code generation!
        }

        if(!canEncode) {
            throw new GeneratorException(
                "generator.indentingwriter.charset.cantencode", s);
        }
        write(s);
/*
        while (tabCount-- > 0) {
            indentOut();
        }
 */
    }

    /**
     * Check if encode can handle the chars in this string.
     *
     */
    protected boolean canEncode(String s) {
        final CharsetEncoder encoder =
            Charset.forName(System.getProperty("file.encoding")).newEncoder();
        char[] chars = s.toCharArray();
        for (int i=0; i<chars.length; i++) {
            if(!encoder.canEncode(chars[i])) {
                return false;
            }
        }
        return true;
    }

    public void p(String s1, String s2) throws IOException {
        p(s1);
        p(s2);
    }

    public void p(String s1, String s2, String s3) throws IOException {
        p(s1);
        p(s2);
        p(s3);
    }

    public void p(String s1, String s2, String s3, String s4) throws IOException {
        p(s1);
        p(s2);
        p(s3);
        p(s4);
    }

    public void p(String s1, String s2, String s3, String s4, String s5) throws IOException {
        p(s1);
        p(s2);
        p(s3);
        p(s4);
        p(s5);
    }

    public void pln() throws IOException {
        newLine();
    }

    public void pln(String s) throws IOException {
        p(s);
        pln();
    }

    public void pln(String s1, String s2) throws IOException {
        p(s1, s2);
        pln();
    }

    public void pln(String s1, String s2, String s3) throws IOException {
        p(s1, s2, s3);
        pln();
    }

    public void pln(String s1, String s2, String s3, String s4) throws IOException {
        p(s1, s2, s3, s4);
        pln();
    }

    public void pln(String s1, String s2, String s3, String s4, String s5) throws IOException {
        p(s1, s2, s3, s4, s5);
        pln();
    }

    public void plnI(String s) throws IOException {
        p(s);
        pln();
        pI();
    }

    public void pO(String s) throws IOException {
        pO();
        p(s);
    }

    public void pOln(String s) throws IOException {
        pO(s);
        pln();
    }

    public void pOlnI(String s) throws IOException {
        pO(s);
        pln();
        pI();
    }

    public void p(Object o) throws IOException {
        write(o.toString());
    }

    public void pln(Object o) throws IOException {
        p(o.toString());
        pln();
    }

    public void plnI(Object o) throws IOException {
        p(o.toString());
        pln();
        pI();
    }

    public void pO(Object o) throws IOException {
        pO();
        p(o.toString());
    }

    public void pOln(Object o) throws IOException {
        pO(o.toString());
        pln();
    }

    public void pOlnI(Object o) throws IOException {
        pO(o.toString());
        pln();
        pI();
    }

    public void pM(String s) throws IOException {
        int i = 0;
        while (i < s.length()) {
            int j = s.indexOf('\n', i);
            if (j == -1) {
                p(s.substring(i));
                break;
            } else {
                pln(s.substring(i, j));
                i = j + 1;
            }
        }
    }

    public void pMln(String s) throws IOException {
        pM(s);
        pln();
    }

    public void pMlnI(String s) throws IOException {
        pM(s);
        pln();
        pI();
    }

    public void pMO(String s) throws IOException {
        pO();
        pM(s);
    }

    public void pMOln(String s) throws IOException {
        pMO(s);
        pln();
    }

    public void pF(String pattern, Object[] arguments) throws IOException {
        pM(MessageFormat.format(pattern, arguments));
    }

    public void pFln(String pattern, Object[] arguments) throws IOException {
        pF(pattern, arguments);
        pln();
    }
}
