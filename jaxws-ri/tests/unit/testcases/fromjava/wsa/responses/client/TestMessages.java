/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package fromjava.wsa.responses.client;

import testutil.W3CAddressingConstants;
import testutil.WsaW3CSOAPMessages;

import java.util.UUID;

import com.sun.xml.ws.api.addressing.AddressingVersion;

/**
 * @author Rama Pulavarthi
 */
public class TestMessages {
    public static final AddressingVersion ADDRESSING_VERSION = AddressingVersion.W3C;
    public static final WsaW3CSOAPMessages MESSAGES = new WsaW3CSOAPMessages(ADDRESSING_VERSION);
    public static final long PROVIDER_MAX_TIMEOUT = 20L;
    public static final long CLIENT_MAX_TIMEOUT = 40L;

    private static String ANONYMOUS_REPLY_TO_HEADER = "<ReplyTo xmlns=\"" + W3CAddressingConstants.WSA_NAMESPACE_NAME + "\">" +
            "<Address>" + W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS + "</Address>" +
            "</ReplyTo>";
    private static String ANONYMOUS_FAULT_TO_HEADER = "<FaultTo xmlns=\"" + W3CAddressingConstants.WSA_NAMESPACE_NAME + "\">" +
            "<Address>" + W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS + "</Address>" +
            "</FaultTo>";
    private static String NON_ANONYMOUS_FAULT_TO_HEADER = "<wsa:FaultTo>" +
            "<wsa:Address>%s</wsa:Address>" +
            "</wsa:FaultTo>";
    private static String NON_ANONYMOUS_REPLY_TO_HEADER = "<wsa:ReplyTo>" +
            "<wsa:Address>%s</wsa:Address>" +
            "</wsa:ReplyTo>";

    static final String ANONYMOUS_FAULT_TO_MESSAGE = "<S:Envelope xmlns:S=\"%s\" " +
            "xmlns:wsa=\"" + W3CAddressingConstants.WSA_NAMESPACE_NAME + "\">\n" +
            "<S:Header>\n" +
            ANONYMOUS_FAULT_TO_HEADER +
            "</S:Header>\n" +
            "<S:Body>\n" +
            "<ns1:addNumbers xmlns:ns1=\"http://server.responses.wsa.fromjava/\">\n" +
            "  <number1>10</number1>\n" +
            "  <number2>10</number2>\n" +
            "  <testname>%s</testname>\n" +
            "</ns1:addNumbers>\n" +
            "</S:Body></S:Envelope>";

    static final String ANONYMOUS_FAULT_TO_COMPLETE_MESSAGE = "<S:Envelope xmlns:S=\"%s\" " +
            "xmlns:wsa=\"" + W3CAddressingConstants.WSA_NAMESPACE_NAME + "\">\n" +
            "<S:Header>\n" +
            "<wsa:Action>%s</wsa:Action>" +
            "<wsa:MessageID>uuid:" + UUID.randomUUID() + "</wsa:MessageID>" +
            "<wsa:To>%s</wsa:To>" +
            ANONYMOUS_FAULT_TO_HEADER +
            "</S:Header>\n" +
            "<S:Body>\n" +
            "<ns1:addNumbers xmlns:ns1=\"http://server.responses.wsa.fromjava/\">\n" +
            "  <number1>10</number1>\n" +
            "  <number2>10</number2>\n" +
            "  <testname>%s</testname>\n" +
            "</ns1:addNumbers>\n" +
            "</S:Body></S:Envelope>";

    static final String NON_ANONYMOUS_FAULT_TO_MESSAGE = "<S:Envelope xmlns:S=\"%s\" " +
            "xmlns:wsa=\"" + W3CAddressingConstants.WSA_NAMESPACE_NAME + "\">\n" +
            "<S:Header>\n" +
            NON_ANONYMOUS_FAULT_TO_HEADER +
            "</S:Header>\n" +
            "<S:Body>\n" +
            "<ns1:addNumbers xmlns:ns1=\"http://server.responses.wsa.fromjava/\">\n" +
            "  <number1>10</number1>\n" +
            "  <number2>10</number2>\n" +
            "  <testname>%s</testname>\n" +
            "</ns1:addNumbers>\n" +
            "</S:Body></S:Envelope>";

    static final String NON_ANONYMOUS_FAULT_TO_COMPLETE_MESSAGE = "<S:Envelope xmlns:S=\"%s\" " +
            "xmlns:wsa=\"" + W3CAddressingConstants.WSA_NAMESPACE_NAME + "\">\n" +
            "<S:Header>\n" +
            NON_ANONYMOUS_FAULT_TO_HEADER +
            "<wsa:Action>%s</wsa:Action>" +
            "<wsa:To>%s</wsa:To>" +
            "<wsa:MessageID>uuid:" + UUID.randomUUID() + "</wsa:MessageID>" +
            "</S:Header>\n" +
            "<S:Body>\n" +
            "<ns1:addNumbers xmlns:ns1=\"http://server.responses.wsa.fromjava/\">\n" +
            "  <number1>10</number1>\n" +
            "  <number2>10</number2>\n" +
            "  <testname>%s</testname>\n" +
            "</ns1:addNumbers>\n" +
            "</S:Body></S:Envelope>";

    static final String NON_ANONYMOUS_FAULT_TO_COMPLETE_FAULTY_MESSAGE = "<S:Envelope xmlns:S=\"%s\" " +
            "xmlns:wsa=\"" + W3CAddressingConstants.WSA_NAMESPACE_NAME + "\">\n" +
            "<S:Header>\n" +
            NON_ANONYMOUS_FAULT_TO_HEADER +
            "<wsa:Action>%s</wsa:Action>" +
            "<wsa:To>%s</wsa:To>" +
            "<wsa:MessageID>uuid:" + UUID.randomUUID() + "</wsa:MessageID>" +
            "</S:Header>\n" +
            "<S:Body>\n" +
            "<ns1:addNumbers xmlns:ns1=\"http://server.responses.wsa.fromjava/\">\n" +
            "  <number1>10</number1>\n" +
            "  <number2>-1</number2>\n" +
            "  <testname>%s</testname>\n" +
            "</ns1:addNumbers>\n" +
            "</S:Body></S:Envelope>";

    static final String NON_ANONYMOUS_REPLY_TO_NON_ANONYMOUS_FAULT_TO_COMPLETE_FAULTY_MESSAGE = "<S:Envelope xmlns:S=\"%s\" " +
            "xmlns:wsa=\"" + W3CAddressingConstants.WSA_NAMESPACE_NAME + "\">\n" +
            "<S:Header>\n" +
            NON_ANONYMOUS_REPLY_TO_HEADER +
            NON_ANONYMOUS_FAULT_TO_HEADER +
            "<wsa:Action>%s</wsa:Action>" +
            "<wsa:To>%s</wsa:To>" +
            "<wsa:MessageID>uuid:" + UUID.randomUUID() + "</wsa:MessageID>" +
            "</S:Header>\n" +
            "<S:Body>\n" +
            "<ns1:addNumbers xmlns:ns1=\"http://server.responses.wsa.fromjava/\">\n" +
            "  <number1>10</number1>\n" +
            "  <number2>-1</number2>\n" +
            "  <testcase>%s</testcase>\n" +
            "</ns1:addNumbers>\n" +
            "</S:Body></S:Envelope>";

    static final String NON_ANONYMOUS_REPLY_TO_ANONYMOUS_FAULT_TO_COMPLETE_MESSAGE = "<S:Envelope xmlns:S=\"%s\" " +
            "xmlns:wsa=\"" + W3CAddressingConstants.WSA_NAMESPACE_NAME + "\">\n" +
            "<S:Header>\n" +
            NON_ANONYMOUS_REPLY_TO_HEADER +
            ANONYMOUS_FAULT_TO_HEADER +
            "<wsa:Action>%s</wsa:Action>" +
            "<wsa:To>%s</wsa:To>" +
            "<wsa:MessageID>uuid:" + UUID.randomUUID() + "</wsa:MessageID>" +
            "</S:Header>\n" +
            "<S:Body>\n" +
            "<ns1:addNumbers xmlns:ns1=\"http://server.responses.wsa.fromjava/\">\n" +
            "  <number1>10</number1>\n" +
            "  <number2>10</number2>\n" +
            "  <testcase>%s</testcase>\n" +
            "</ns1:addNumbers>\n" +
            "</S:Body></S:Envelope>";

    static final String ANONYMOUS_REPLY_TO_COMPLETE_MESSAGE = "<S:Envelope xmlns:S=\"%s\" " +
            "xmlns:wsa=\"" + W3CAddressingConstants.WSA_NAMESPACE_NAME + "\">\n" +
            "<S:Header>\n" +
            ANONYMOUS_REPLY_TO_HEADER +
            "<wsa:Action>%s</wsa:Action>" +
            "<wsa:To>%s</wsa:To>" +
            "<wsa:MessageID>uuid:" + UUID.randomUUID() + "</wsa:MessageID>" +
            "</S:Header>\n" +
            "<S:Body>\n" +
            "<ns1:addNumbers xmlns:ns1=\"http://server.responses.wsa.fromjava/\">\n" +
            "  <number1>10</number1>\n" +
            "  <number2>10</number2>\n" +
            "  <testname>%s</testname>\n" +
            "</ns1:addNumbers>\n" +
            "</S:Body></S:Envelope>";

    static final String NON_ANONYMOUS_REPLY_TO_COMPLETE_MESSAGE = "<S:Envelope xmlns:S=\"%s\" " +
            "xmlns:wsa=\"" + W3CAddressingConstants.WSA_NAMESPACE_NAME + "\">\n" +
            "<S:Header>\n" +
            NON_ANONYMOUS_REPLY_TO_HEADER +
            "<wsa:Action>%s</wsa:Action>" +
            "<wsa:To>%s</wsa:To>" +
            "<wsa:MessageID>uuid:" + UUID.randomUUID() + "</wsa:MessageID>" +
            "</S:Header>\n" +
            "<S:Body>\n" +
            "<ns1:addNumbers xmlns:ns1=\"http://server.responses.wsa.fromjava/\">\n" +
            "  <number1>10</number1>\n" +
            "  <number2>10</number2>\n" +
            "  <testname>%s</testname>\n" +
            "</ns1:addNumbers>\n" +
            "</S:Body></S:Envelope>";

}
