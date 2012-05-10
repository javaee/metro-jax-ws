/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)SwaTestImpl2.java	1.10 06/03/25
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.cts.dl_swa;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;

@WebService(
    portName="SwaTestTwoPort",
    serviceName="WSIDLSwaTestService",
    targetNamespace="http://SwaTestService.org/wsdl",
    wsdlLocation="WEB-INF/wsdl/SwaTestService.wsdl",
    endpointInterface="com.sun.xml.ws.cts.dl_swa.SwaTest2"
)

public class SwaTestImpl2 implements SwaTest2 {
    public OutputResponseString putMultipleAttachments(InputRequestPut request, DataHandler attach1, DataHandler attach2)  {
	try {
	    OutputResponseString theResponse = new OutputResponseString();
 	    theResponse.setMyString("ok");
	    System.out.println("Enter putMultipleAttachments() ......");
	    if(attach1 == null) {
		System.err.println("attach1 is null (unexpected)");
		theResponse.setMyString("not ok");
	    }
	    if(attach2 == null) {
		System.err.println("attach2 is null (unexpected)");
		theResponse.setMyString("not ok");
	    }
	    System.out.println("Leave putMultipleAttachments() ......");
	    return theResponse;
	} catch (Exception e) {
	    throw new WebServiceException(e.getMessage());
	}
    }

    public OutputResponseString echoNoAttachments(InputRequestString request)  {
	try {
	    System.out.println("Enter echoNoAttachments() ......");
	    OutputResponseString theResponse = new OutputResponseString();
 	    theResponse.setMyString(request.getMyString());
	    System.out.println("Leave echoNoAttachments() ......");
	    return theResponse;
	} catch (Exception e) {
	    throw new WebServiceException(e.getMessage());
	}
    }
}
