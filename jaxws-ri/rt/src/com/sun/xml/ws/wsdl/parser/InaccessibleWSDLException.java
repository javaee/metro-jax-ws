package com.sun.xml.ws.wsdl.parser;

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

import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A list of {@link InaccessibleWSDLException} wrapped in one exception.
 *
 * <p>
 * This exception is used to report all the errors during WSDL parsing from {@link RuntimeWSDLParser#parse(java.net.URL, org.xml.sax.EntityResolver, boolean, com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension[])}
 *
 * @author Vivek Pandey
 */
public class InaccessibleWSDLException extends WebServiceException {

    private final List<Throwable> errors;

    private static final long serialVersionUID = 1L;

    public InaccessibleWSDLException(List<Throwable> errors) {
        super(errors.size()+" counts of InaccessibleWSDLException.\n");
        assert !errors.isEmpty() : "there must be at least one error";
        this.errors = Collections.unmodifiableList(new ArrayList<Throwable>(errors));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append('\n');

        for( Throwable error : errors )
            sb.append(error.toString()).append('\n');

        return sb.toString();
    }

    /**
     * Returns a read-only list of {@link InaccessibleWSDLException}s
     * wrapped in this exception.
     *
     * @return
     *      a non-null list.
     */
    public List<Throwable> getErrors() {
        return errors;
    }

    public static class Builder implements ErrorHandler {
        private final List<Throwable> list = new ArrayList<Throwable>();
        public void error(Throwable e) {
            list.add(e);
        }
        /**
         * If an error was reported, throw the exception.
         * Otherwise exit normally.
         */
        public void check() throws InaccessibleWSDLException {
            if(list.isEmpty())
                return;
            throw new InaccessibleWSDLException(list);
        }
    }

}
