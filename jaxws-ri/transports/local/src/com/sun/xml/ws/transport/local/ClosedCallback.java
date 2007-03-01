/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.transport.local;

import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.net.URI;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;

/**
 *
 * @author Jitendra Kotamraju
 */
interface ClosedCallback {
    void onClosed();
}
