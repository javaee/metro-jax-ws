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

package wsa.submission.fromwsdl.issue564.server;

import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.Addressing;

import com.sun.xml.ws.developer.MemberSubmissionAddressing;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.SOAPVersion;


/**
 * @author Rama Pulavarthi
 */
@WebServiceProvider()
@ServiceMode(value= Service.Mode.MESSAGE)
@BindingType(value= SOAPBinding.SOAP12HTTP_BINDING)
@MemberSubmissionAddressing(required=false, validation=MemberSubmissionAddressing.Validation.STRICT)
//@Addressing
public class AddNumbersImpl implements Provider<Message> {


    public Message invoke(Message request) {
        Message m2 = Messages.create("Test Unsupported", AddressingVersion.W3C, SOAPVersion.SOAP_12);
        return m2;

    }
}