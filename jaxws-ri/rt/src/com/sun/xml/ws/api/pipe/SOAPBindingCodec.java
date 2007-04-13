package com.sun.xml.ws.api.pipe;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Message;

import javax.xml.stream.XMLStreamReader;

/**
 *
 *
 * @see com.sun.xml.ws.api.pipe.Codecs
 * @author Jitendra Kotamraju
 */
public interface SOAPBindingCodec extends Codec {
    StreamSOAPCodec getXMLCodec();
}
