/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.streaming;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.Location;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.io.Closeable;
import java.io.IOException;

/**
 * Wrapper over XMLStreamReader. It will be used primarily to cleaup the resources such as closure on InputStream/Reader.
 *
 * @author Vivek Pandey
 */
public class TidyXMLStreamReader implements XMLStreamReader{
    private final XMLStreamReader reader;
    private final Closeable closeableSource;

    public TidyXMLStreamReader(XMLStreamReader reader, Closeable closeableSource) {
        this.reader = reader;
        this.closeableSource = closeableSource;
    }

    public int getAttributeCount() {
        return reader.getAttributeCount();
    }

    public int getEventType() {
        return reader.getEventType();
    }

    public int getNamespaceCount() {
        return reader.getNamespaceCount();
    }

    public int getTextLength() {
        return reader.getTextLength();
    }

    public int getTextStart() {
        return reader.getTextStart();
    }

    public int next() throws XMLStreamException {
        return reader.next();
    }

    public int nextTag() throws XMLStreamException {
        return reader.nextTag();
    }

    public void close() throws XMLStreamException {
        reader.close();
        try {
            closeableSource.close();
        } catch (IOException e) {
            throw new WebServiceException(e);
        }
    }

    public boolean hasName() {
        return reader.hasName();
    }

    public boolean hasNext() throws XMLStreamException {
        return reader.hasNext();
    }

    public boolean hasText() {
        return reader.hasText();
    }

    public boolean isCharacters() {
        return reader.isCharacters();
    }

    public boolean isEndElement() {
        return reader.isEndElement();
    }

    public boolean isStandalone() {
        return reader.isStandalone();
    }

    public boolean isStartElement() {
        return reader.isStartElement();
    }

    public boolean isWhiteSpace() {
        return reader.isWhiteSpace();
    }

    public boolean standaloneSet() {
        return reader.standaloneSet();
    }

    public char[] getTextCharacters() {
        return reader.getTextCharacters();
    }

    public boolean isAttributeSpecified(int i) {
        return reader.isAttributeSpecified(i);
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2) throws XMLStreamException {
        return reader.getTextCharacters(i, chars, i1, i2);
    }

    public String getCharacterEncodingScheme() {
        return reader.getCharacterEncodingScheme();
    }

    public String getElementText() throws XMLStreamException {
        return reader.getElementText();
    }

    public String getEncoding() {
        return reader.getEncoding();
    }

    public String getLocalName() {
        return reader.getLocalName();
    }

    public String getNamespaceURI() {
        return reader.getNamespaceURI();
    }

    public String getPIData() {
        return reader.getPIData();
    }

    public String getPITarget() {
        return reader.getPITarget();
    }

    public String getPrefix() {
        return reader.getPrefix();
    }

    public String getText() {
        return reader.getText();
    }

    public String getVersion() {
        return reader.getVersion();
    }

    public String getAttributeLocalName(int i) {
        return reader.getAttributeLocalName(i);
    }

    public String getAttributeNamespace(int i) {
        return reader.getAttributeNamespace(i);
    }

    public String getAttributePrefix(int i) {
        return reader.getAttributePrefix(i);
    }

    public String getAttributeType(int i) {
        return reader.getAttributeType(i);
    }

    public String getAttributeValue(int i) {
        return reader.getAttributeValue(i);
    }

    public String getNamespacePrefix(int i) {
        return reader.getNamespacePrefix(i);
    }

    public String getNamespaceURI(int i) {
        return reader.getNamespaceURI(i);
    }

    public NamespaceContext getNamespaceContext() {
        return reader.getNamespaceContext();
    }

    public QName getName() {
        return reader.getName();
    }

    public QName getAttributeName(int i) {
        return reader.getAttributeName(i);
    }

    public Location getLocation() {
        return reader.getLocation();
    }

    public Object getProperty(String s) throws IllegalArgumentException {
        return reader.getProperty(s);
    }

    public void require(int i, String s, String s1) throws XMLStreamException {
        reader.require(i, s, s1);
    }

    public String getNamespaceURI(String s) {
        return reader.getNamespaceURI(s);
    }

    public String getAttributeValue(String s, String s1) {
        return reader.getAttributeValue(s, s1);
    }


}
