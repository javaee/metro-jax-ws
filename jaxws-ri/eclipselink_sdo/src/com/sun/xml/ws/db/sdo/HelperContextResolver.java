package com.sun.xml.ws.db.sdo;

import java.util.Map;

import javax.xml.namespace.QName;

import commonj.sdo.helper.HelperContext;

public interface HelperContextResolver {
    HelperContext getHelperContext(boolean isClient, QName serviceName,
            Map<String, Object> properties);
}
