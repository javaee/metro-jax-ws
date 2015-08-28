module client {
    requires java.xml.ws;
    requires java.logging;

    exports provider.http_status_code_200_oneway.client;
}
