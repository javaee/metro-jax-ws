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

package com.sun.xml.ws.message.source;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.message.AttachmentSetImpl;
import com.sun.xml.ws.message.stream.PayloadStreamReaderMessage;
import com.sun.xml.ws.streaming.SourceReaderFactory;

import javax.xml.transform.Source;

/**
 * {@link Message} backed by {@link Source} as payload
 *
 * @author Vivek Pandey
 */
public class PayloadSourceMessage extends PayloadStreamReaderMessage {

    public PayloadSourceMessage(@Nullable HeaderList headers,
        @NotNull Source payload, @NotNull AttachmentSet attSet,
        @NotNull SOAPVersion soapVersion) {
        
        super(headers, SourceReaderFactory.createSourceReader(payload, true),
                attSet, soapVersion);
    }

    public PayloadSourceMessage(Source s, SOAPVersion soapVer) {
        this(null, s, new AttachmentSetImpl(), soapVer);
    }

}
