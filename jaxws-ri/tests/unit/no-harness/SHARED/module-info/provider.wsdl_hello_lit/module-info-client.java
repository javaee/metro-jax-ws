module client {
    requires java.xml.ws;
    requires java.logging;

    exports provider.wsdl_hello_lit.client;
}
