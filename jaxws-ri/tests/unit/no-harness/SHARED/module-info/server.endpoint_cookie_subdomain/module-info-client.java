module client {
    requires java.xml.ws;
    requires java.logging;

    exports server.endpoint_cookie_subdomain.client;
}
