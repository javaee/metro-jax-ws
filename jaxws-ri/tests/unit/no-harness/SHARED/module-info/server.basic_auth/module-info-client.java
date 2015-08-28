module client {
    requires java.xml.ws;
    requires java.logging;

    exports server.basic_auth.client;
}
