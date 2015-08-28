module client {
    requires java.xml.ws;
    requires java.logging;

    exports server.http_only.client;
}
