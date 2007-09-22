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

package fromjava.wsa.custom_action.server;

import testutil.WsaBaseSOAPHandler;
import com.sun.xml.ws.addressing.model.ActionNotSupportedException;
import fromjava.wsa.custom_action.common.TestConstants;

/**
 * @author Arun Gupta
 */
public class ServerSOAPHandler extends WsaBaseSOAPHandler {
    protected void checkInboundActions(String oper, String action) {
        if (oper.equals("addNumbersNoAction")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_IN_NOACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbersEmptyAction")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_IN_EMPTYACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbers")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_IN_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbers2")) {
            if (!action.equals(TestConstants.ADD_NUMBERS2_IN_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbers3")) {
            if (!action.equals(TestConstants.ADD_NUMBERS3_IN_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbersFault1")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT1_IN_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbersFault2")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT2_IN_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbersFault3")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT3_IN_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbersFault4")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT4_IN_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbersFault5")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT5_IN_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbersFault6")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT6_IN_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        } else if (oper.equals("addNumbersFault7")) {
            if (!action.equals(TestConstants.ADD_NUMBERS_FAULT7_IN_ACTION)) {
                throw new ActionNotSupportedException(action);
            }
        }
    }
}
