package com.sun.xml.ws.sandbox;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;

/**
 * {@link XMLStreamWriter} extended to support XOP.
 *
 * <p>
 * For more intuitive design, this interface would have extended {@link XMLStreamWriter},
 * but that would require delegation, which introduces unnecessary overhead.
 *
 * TODO
 * - Add methods to write other primitive types, such as hex and integers 
 *   (and arrays of).
 *   A textual implementation would write characters in accordance
 *   to the canonical lexical definitions specified in W3C XML Schema: datatypes.
 *   A MTOM implementation would write characters except for the case where octets
 *   that would otherwise be base64 encoded when using the textual implementation.
 *   A Fast Infoset implementation would encoded binary data the primitive types in
 *   binary form.
 * - Consider renaming writeBinary to writeBytesAsBase64 to be consistent with
 *   infoset abstraction.
 * - Consider including the ability to write an Object. The JAXB marshaller can
 *   be used.
 * - Add the ability to writeStart and writeEnd on attributes so that the same
 *   methods for writing primitive types (and characters, which will require new methods) 
 *   can be used for writing attribute values as well as element content.
 * @author Kohsuke Kawaguchi
 */
public interface XMLStreamWriterEx {

    /**
     * Gets the base {@link XMLStreamWriter}.
     */
    XMLStreamWriter getBase();

    /**
     * Write the binary data.
     *
     * <p>
     * Conceptually (infoset-wise), this produces the base64-encoded binary data on the
     * output. But this allows implementations like FastInfoset or XOP to do the smart
     * thing.
     *
     * <p>
     * The use of this method has some restriction to support XOP. Namely, this method
     * must be invoked as a sole content of an element.
     */
    void writeBinary(byte[] data, int start, int len) throws XMLStreamException;
}
