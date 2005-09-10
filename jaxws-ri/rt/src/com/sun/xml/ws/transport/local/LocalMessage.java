/*
 * $Id: LocalMessage.java,v 1.2 2005-09-10 19:48:10 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.ws.transport.local;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author WS Development Team
 */
public class LocalMessage {
    ByteArrayOutputStream baos;
    Map<String, List<String>> headers;
    
    public LocalMessage () {
    }
    
    public ByteArrayOutputStream getOutput() {
        return baos;
    }
    
    public void setOutput(ByteArrayOutputStream baos) {
        this.baos = baos;
    }
    
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }
    
}
