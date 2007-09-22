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

package fromjava.wsa.custom_action.client;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;

import com.sun.xml.ws.addressing.model.ActionNotSupportedException;
import testutil.WsaBaseSOAPHandler;
import fromjava.wsa.custom_action.common.TestConstants;

/**
 * @author Arun Gupta
 */
public class ClientSOAPHandler extends WsaBaseSOAPHandler {
    @Override
    protected void checkInboundActions(String oper, String action) {
        if (oper.equals("addNumbersNoActionResponse")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_OUT_NOACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbersEmptyActionResponse")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_OUT_EMPTYACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbersResponse")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_OUT_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbers2Response")) {
            if (!action.equals(TestConstants.ADD_NUMBERS2_OUT_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbers3Response")) {
            if (!action.equals(TestConstants.ADD_NUMBERS3_OUT_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        }
    }

    @Override
    protected void checkFaultActions(String requestName, String detailName, String action) {
        if (requestName.equals("addNumbersFault1") && detailName.equals("AddNumbersException")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT1_ADDNUMBERS_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (requestName.equals("addNumbersFault2") && detailName.equals("AddNumbersException")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT2_ADDNUMBERS_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (requestName.equals("addNumbersFault2") && detailName.equals("TooBigNumbersException")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT2_TOOBIGNUMBERS_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (requestName.equals("addNumbersFault3") && detailName.equals("AddNumbersException")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT3_ADDNUMBERS_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (requestName.equals("addNumbersFault3") && detailName.equals("TooBigNumbersException")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT3_TOOBIGNUMBERS_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (requestName.equals("addNumbersFault4") && detailName.equals("AddNumbersException")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT4_ADDNUMBERS_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (requestName.equals("addNumbersFault4") && detailName.equals("TooBigNumbersException")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT4_TOOBIGNUMBERS_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (requestName.equals("addNumbersFault5") && detailName.equals("AddNumbersException")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT5_ADDNUMBERS_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (requestName.equals("addNumbersFault5") && detailName.equals("TooBigNumbersException")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT5_TOOBIGNUMBERS_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (requestName.equals("addNumbersFault6") && detailName.equals("AddNumbersException")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT6_ADDNUMBERS_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (requestName.equals("addNumbersFault6") && detailName.equals("TooBigNumbersException")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT6_TOOBIGNUMBERS_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (requestName.equals("addNumbersFault7") && detailName.equals("AddNumbersException")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT7_ADDNUMBERS_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (requestName.equals("addNumbersFault7") && detailName.equals("TooBigNumbersException")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT7_TOOBIGNUMBERS_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        }
        super.checkFaultActions(requestName, detailName, action);
    }

    @Override
    protected String getOperationName(SOAPBody soapBody) throws SOAPException {
        String opName = super.getOperationName(soapBody);
        if (!opName.startsWith("addNumbersFault"))
            return opName;

        if (opName.equals("addNumbersFault1"))
            return opName;

        if (opName.equals("addNumbersFault2")) {
            soapBody.getFirstChild().getFirstChild().getNodeValue();
        }
        return opName;
    }
}
