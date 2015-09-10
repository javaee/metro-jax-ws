module client {
    requires java.xml.ws;
    requires java.logging;

    exports server.endpoint1.client;
}
