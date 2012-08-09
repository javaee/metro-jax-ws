package fromwsdl.schema_validation.basic.server;

import com.sun.xml.ws.developer.ValidationErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

/**
 * @author Jitendra Kotamraju
 */
public class MyErrorHandler extends ValidationErrorHandler {
    public void warning(SAXParseException e) throws SAXException {
        throw e;
    }

    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    public void fatalError(SAXParseException e) throws SAXException {
        ; // noop
    }

}

