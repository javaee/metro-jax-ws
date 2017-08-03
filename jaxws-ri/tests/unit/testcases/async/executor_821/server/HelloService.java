/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package async.executor_821.server;

import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

/**
 * @author Jitendra Kotamraju
 */
@WebService(endpointInterface="async.executor_821.server.Hello")
public class HelloService implements Hello {

    public HelloOutput hello(Hello_Type req){
        // Intentionally adding the sleep so that we can test client
        try {
            Thread.sleep(10000);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
        HelloOutput resp = new HelloOutput();
        resp.setArgument(req.getArgument());
        resp.setExtra(req.getExtra());
        return resp;
    }
    
    public void hello1(HelloType req, HelloType inHeader, 
            Holder<HelloOutput> resp, Holder<HelloType> outHeader){
        // Intentionally adding the sleep so that we can test client
        try {
            Thread.sleep(10000);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
        HelloOutput out = new HelloOutput();
        out.setArgument(req.getArgument());
        out.setExtra(req.getExtra());
        resp.value = out; 
        outHeader.value = inHeader;        
    }

    public int hello0(int param_in) {
        // Intentionally adding the sleep so that we can test client
        try {
            Thread.sleep(10000);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
        return param_in;
    }

}
