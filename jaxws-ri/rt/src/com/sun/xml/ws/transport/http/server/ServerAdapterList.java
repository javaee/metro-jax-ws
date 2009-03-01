package com.sun.xml.ws.transport.http.server;

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.HttpAdapterList;

public class ServerAdapterList extends HttpAdapterList<ServerAdapter> {
    @Override
    protected ServerAdapter createHttpAdapter(String name, String urlPattern, WSEndpoint<?> endpoint) {
        return new ServerAdapter(name, urlPattern, endpoint, this);
    }
}