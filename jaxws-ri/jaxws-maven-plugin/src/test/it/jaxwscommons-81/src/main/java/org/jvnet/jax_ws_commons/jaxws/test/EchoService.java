/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jvnet.jax_ws_commons.jaxws.test;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;

/**
 *
 * @author Lukas Jungmann
 */
@WebService(serviceName = "EchoService")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class EchoService {

    /** This is a sample web service operation */
    @WebMethod(operationName = "echo")
    public String echo(@WebParam(name = "echo_arg") String txt) {
        return txt;
    }
}
