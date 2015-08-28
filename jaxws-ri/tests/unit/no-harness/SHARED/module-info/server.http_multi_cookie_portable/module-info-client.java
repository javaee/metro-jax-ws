module client {
    requires java.xml.ws;
    requires java.logging;

    exports server.http_multi_cookie_portable.client;
}
