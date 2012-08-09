package fromjava.wsa.action_mapping.client;

import testutil.WsaUtils;

import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

import com.sun.xml.ws.addressing.W3CAddressingConstants;
import com.sun.xml.ws.addressing.v200408.MemberSubmissionAddressingConstants;

/**
 * @author Rama Pulavarthi
 */

public class NSContextImpl implements NamespaceContext {
    public NSContextImpl() {
        addToContext("wsdl", "http://schemas.xmlsoap.org/wsdl/");
        addToContext("tns", "http://foobar.org/");
        addToContext("wsaw", "http://www.w3.org/2006/05/addressing/wsdl");
        addToContext("wsam", "http://www.w3.org/2007/05/addressing/metadata");
        addToContext("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
        addToContext("wsp","http://www.w3.org/ns/ws-policy");
    }

    private static final Map<String, String> prefixToNSMap = new HashMap<String, String>();
    private static final Map<String, String> nsToPrefixMap = new HashMap<String, String>();

    public void addToContext(String prefix, String namespaceURI) {
        prefixToNSMap.put(prefix, namespaceURI);
        nsToPrefixMap.put(namespaceURI, prefix);
    }

    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException(
                    "NamespaceContextImpl#getNamespaceURI(String prefix) with prefix == null");
        }

        // constants
        if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
            return XMLConstants.XML_NS_URI;
        }
        if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }

        // default
        if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            if (prefixToNSMap.containsKey(prefix)) {
                return prefixToNSMap.get(prefix);
            } else {
                return XMLConstants.NULL_NS_URI;
            }
        }

        // bound
        if (prefixToNSMap.containsKey(prefix)) {
            return prefixToNSMap.get(prefix);
        }

        // unbound
        return XMLConstants.NULL_NS_URI;
    }

    public String getPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException(
                    "NamespaceContextImpl#getPrefix(String namespaceURI) with namespaceURI == null");
        }

        // constants
        if (namespaceURI.equals(XMLConstants.XML_NS_URI)) {
            return XMLConstants.XML_NS_PREFIX;
        }
        if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }

        // bound
        if (nsToPrefixMap.containsKey(namespaceURI)) {
            return nsToPrefixMap.get(namespaceURI);
        }

        // mimic "default Namespace URI"
        if (namespaceURI.equals(XMLConstants.NULL_NS_URI)) {
            return XMLConstants.DEFAULT_NS_PREFIX;
        }

        // unbound
        return null;
    }

    public Iterator getPrefixes(final String namespaceURI) {
       return new Iterator<String>() {
           String next = nsToPrefixMap.get(namespaceURI);
           public boolean hasNext() {
               return next != null;
           }

           public String next() {
               if(next != null) {
                   String tmp = next;
                   next = null;
                   return tmp;
               }
               throw new NoSuchElementException();
           }

           public void remove() {
               throw new UnsupportedOperationException();
           }
       };
    }
}