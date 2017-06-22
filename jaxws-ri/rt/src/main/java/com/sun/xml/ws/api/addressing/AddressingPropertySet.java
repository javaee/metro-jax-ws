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

package com.sun.xml.ws.api.addressing;

import com.sun.xml.ws.api.message.Packet;
import com.oracle.webservices.api.message.BasePropertySet;
import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;

/**
 * <p>This property set exists so the upper stack can SET addressing info
 * on a PER-REQUEST basis (instead of a per proxy/dispatch basis via OneWayFeature).</p>
 *
 * <p>This class is NOT used for reading addressing header values.</p>
 */
public class AddressingPropertySet extends BasePropertySet {

    // NOTE: Setting ACTION on client side is covered by standard BindingProvider.

    public static final String ADDRESSING_FAULT_TO = "com.sun.xml.ws.api.addressing.fault.to";
    private String faultTo;
    @Property(ADDRESSING_FAULT_TO)
    public String getFaultTo()                 { return faultTo; }
    public void   setFaultTo(final String x)   { faultTo = x;    }

    public static final String ADDRESSING_MESSAGE_ID = "com.sun.xml.ws.api.addressing.message.id";
    private String messageId;
    public String getMessageId()               { return messageId; }
    public void   setMessageId(final String x) { messageId = x;    }

    public static final String ADDRESSING_RELATES_TO = "com.sun.xml.ws.api.addressing.relates.to";
    @Property(ADDRESSING_RELATES_TO)
    private String relatesTo;
    public String getRelatesTo()               { return relatesTo; }
    public void   setRelatesTo(final String x) { relatesTo = x;    }

    public static final String ADDRESSING_REPLY_TO = "com.sun.xml.ws.api.addressing.reply.to";
    @Property(ADDRESSING_REPLY_TO)
    private String replyTo;
    public String getReplyTo()                 { return replyTo; }
    public void   setReplyTo(final String x)   { replyTo = x;    }

    ////////////////////////////////////////////////////
    //
    // PropertySet boilerplate
    //

    private static final PropertyMap model;

    static {
        model = parse(AddressingPropertySet.class);
    }

    @Override
    protected PropertyMap getPropertyMap() {
        return model;
    }
}
