/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.api.policy;

import com.sun.xml.ws.config.management.policy.ManagementAssertionCreator;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelTranslator;
import com.sun.xml.ws.policy.spi.PolicyAssertionCreator;
import com.sun.xml.ws.resources.ManagementMessages;

import java.util.Arrays;

/**
 * This class provides a method for translating a PolicySourceModel structure to a
 * normalized Policy expression. The resulting Policy is disconnected from its model,
 * thus any additional changes in the model will have no effect on the Policy expression.
 *
 * @author Fabian Ritzmann
 */
public class ModelTranslator extends PolicyModelTranslator {

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(ModelTranslator.class);
    
    private static final PolicyAssertionCreator[] JAXWS_ASSERTION_CREATORS = {
        new ManagementAssertionCreator()
    };

    private static final ModelTranslator translator;
    private static final PolicyException creationException;

    static {
        ModelTranslator tempTranslator = null;
        PolicyException tempException = null;
        try {
            tempTranslator = new ModelTranslator();
        } catch (PolicyException e) {
            tempException = e;
            LOGGER.warning(ManagementMessages.WSM_1007_FAILED_MODEL_TRANSLATOR_INSTANTIATION(), e);
        } finally {
            translator = tempTranslator;
            creationException = tempException;
        }
    }

    private ModelTranslator() throws PolicyException {
        super(Arrays.asList(JAXWS_ASSERTION_CREATORS));
    }

    /**
     * Method returns thread-safe policy model translator instance.
     *
     * @return A policy model translator instance.
     * @throws PolicyException If instantiating a PolicyAssertionCreator failed.
     */
    public static ModelTranslator getTranslator() throws PolicyException {
        if (creationException != null) {
            throw LOGGER.logSevereException(creationException);
        }
        else {
            return translator;
        }
    }
    
}
