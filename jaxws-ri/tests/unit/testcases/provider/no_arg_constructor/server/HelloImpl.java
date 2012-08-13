/*
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package provider.no_arg_constructor.server;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;

/**
 * Service returns Source payload that is created using no-arg Source
 * constructors.
 *
 * @author Jitendra Kotamraju
 */
@WebServiceProvider
public class HelloImpl implements Provider<Source> {
    int source = 0;

    public Source invoke(Source msg) {
        int index = source++%3;
        if (index == 0)
            return new DOMSource();
        else if (index == 1)
            return new StreamSource();
        else
            return new SAXSource();
    }
}
