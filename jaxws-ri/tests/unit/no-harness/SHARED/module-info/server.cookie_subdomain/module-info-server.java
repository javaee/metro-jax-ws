module server {
    requires java.xml.ws;
     requires jdk.httpserver;
      requires java.logging; 

    // generated by WebServiceWrapperGenerator
    exports server.cookie_subdomain.server.jaxws;
    exports server.cookie_subdomain.server;
}
