/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.ws.streaming;

import org.xml.sax.helpers.XMLReaderFactory;

import java.util.Iterator;

import javax.xml.namespace.QName;

/**
 * <p> XMLReader provides a high-level streaming parser interface
 * for reading XML documents. </p>
 *
 * <p> The {@link #next} method is used to read events from the XML document. </p>
 *
 * <p> Each time it is called, {@link #next} returns the new state of the reader. </p>
 *
 * <p> Possible states are: BOF, the initial state, START, denoting the start
 * tag of an element, END, denoting the end tag of an element, CHARS, denoting
 * the character content of an element, PI, denoting a processing instruction,
 * EOF, denoting the end of the document. </p>
 *
 * <p> Depending on the state the reader is in, one or more of the following
 * query methods will be meaningful: {@link #getName}, {@link #getURI},
 * {@link #getLocalName}, {@link #getAttributes}, {@link #getValue}. </p>
 *
 * <p> Elements visited by a XMLReader are tagged with unique IDs. The ID of the
 * current element can be found by calling {@link #getElementId}. </p>
 *
 * <p> A XMLReader is always namespace-aware, and keeps track of the namespace
 * declarations which are in scope at any time during streaming. The
 * {@link #getURI(java.lang.String)} method can be used to find the URI
 * associated to a given prefix in the current scope. </p>
 *
 * <p> XMLReaders can be created using a {@link XMLReaderFactory}. </p>
 *
 * <p> Some utility methods, {@link #nextContent} and {@link #nextElementContent}
 * make it possible to ignore whitespace and processing instructions with
 * minimum impact on the client code. </p>
 *
 * <p> Similarly, the {@link #skipElement} and {@link #skipElement(int elementId)}
 * methods allow to skip to the end tag of an element ignoring all its content. </p>
 *
 * <p> Finally, the {@link #recordElement} method can be invoked when the XMLReader
 * is positioned on the start tag of an element to record the element's contents
 * so that they can be played back later. </p>
 *
 * @see XMLReaderFactory
 *
 * @author WS Development Team
 */
public interface XMLReader {
    /**
     * The initial state of a XMLReader.
     */
    public static final int BOF = 0;

    /**
     * The state denoting the start tag of an element.
     */
    public static final int START = 1;

    /**
     * The state denoting the end tag of an element.
     */
    public static final int END = 2;

    /**
     * The state denoting the character content of an element.
     */
    public static final int CHARS = 3;

    /**
     * The state denoting a processing instruction.
     */
    public static final int PI = 4;

    /**
     * The state denoting that the end of the document has been reached.
     */
    public static final int EOF = 5;

    /**
     * Return the next state of the XMLReader.
     *
     * The return value is one of: START, END, CHARS, PI, EOF.
     */
    public int next();

    /*
    * Return the next state of the XMLReader.
    *
    * <p> Whitespace character content and processing instructions are ignored. </p>
    *
    * <p> The return value is one of: START, END, CHARS, EOF. </p>
    */
    public int nextContent();

    /**
     * Return the next state of the XMLReader.
     *
     * <p> Whitespace character content, processing instructions are ignored.
     * Non-whitespace character content triggers an exception. </p>
     *
     * <p> The return value is one of: START, END, EOF. </p>
     */
    public int nextElementContent();

    /**
     * Return the current state of the XMLReader.
     *
     */
    public int getState();

    /**
     * Return the current qualified name.
     *
     * <p> Meaningful only when the state is one of: START, END. </p>
     */
    public QName getName();

    /**
     * Return the current URI.
     *
     * <p> Meaningful only when the state is one of: START, END. </p>
     */
    public String getURI();

    /**
     * Return the current local name.
     *
     * <p> Meaningful only when the state is one of: START, END, PI. </p>
     */
    public String getLocalName();

    /**
     * Return the current attribute list. In the jaxws implementation,
     * this list also includes namespace declarations.
     *
     * <p> Meaningful only when the state is one of: START. </p>
     *
     * <p> The returned {@link Attributes} object belong to the XMLReader and is
     * only guaranteed to be valid until the {@link #next} method is called,
     * directly or indirectly.</p>
     */
    public Attributes getAttributes();

    /**
     * Return the current value.
     *
     * <p> Meaningful only when the state is one of: CHARS, PI. </p>
     */
    public String getValue();

    /**
     * Return the current element ID.
     */
    public int getElementId();

    /**
     * Return the current line number.
     *
     * <p> Due to aggressive parsing, this value may be off by a few lines. </p>
     */
    public int getLineNumber();

    /**
     * Return the URI for the given prefix.
     *
     * <p> If there is no namespace declaration in scope for the given
     * prefix, return null. </p>
     */
    public String getURI(String prefix);

    /**
     * Return an iterator on all prefixes in scope, except for the default prefix.
     *
     */
    public Iterator getPrefixes();

    /**
     * Records the current element and leaves the reader positioned on its end tag.
     *
     * <p> The XMLReader must be positioned on the start tag of the element.
     * The returned reader will play back all events starting with the
     * start tag of the element and ending with its end tag. </p>
     */
    public XMLReader recordElement();

    /**
     * Skip all nodes up to the end tag of the element with the current element ID.
     */
    public void skipElement();

    /**
     * Skip all nodes up to the end tag of the element with the given element ID.
     */
    public void skipElement(int elementId);

    /**
     * Close the XMLReader.
     *
     * <p> All subsequent calls to {@link #next} will return EOF. </p>
     */
    public void close();
}
