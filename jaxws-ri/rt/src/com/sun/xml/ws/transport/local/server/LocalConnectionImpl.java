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

package com.sun.xml.ws.transport.local.server;
import java.util.List;
import java.util.Map;
import com.sun.xml.ws.transport.WSConnectionImpl;
import com.sun.xml.ws.transport.local.LocalMessage;
import com.sun.xml.ws.util.ByteArrayBuffer;

import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author WS Development Team
 *
 * Server-side Local transport implementation
 */
public class LocalConnectionImpl extends WSConnectionImpl {
    private int status;
    private LocalMessage lm;
    
    public LocalConnectionImpl (LocalMessage localMessage) {
        this.lm = localMessage;
    }
    
    public Map<String,List<String>> getHeaders () {
        return lm.getHeaders ();
    }
    
    /**
     * sets response headers.
     */
    public void setHeaders (Map<String,List<String>> headers) {
        lm.setHeaders (headers);
    }
    
    public void setStatus (int status) {
        this.status = status;
    }
    
    public InputStream getInput () {
        return lm.getOutput().newInputStream();
    }
    
    public OutputStream getOutput () {
        ByteArrayBuffer bab = new ByteArrayBuffer();
        lm.setOutput(bab);
        return bab;
    }
}

