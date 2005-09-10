/*
 * $Id: ProcessorActionVersion.java,v 1.3 2005-09-10 19:49:31 kohsuke Exp $
 */

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
package com.sun.tools.ws.processor;

/**
 * @author WS Development Team
 *
 * Typesafe enum class to hold the ProcessorActionVersion
 */
public enum ProcessorActionVersion {
    PRE_20("1.1.2"), VERSION_20("2.0");

    ProcessorActionVersion(String version) {
        this.version = version;
    }

    public String value() {
        return version;
    }

    private final String version;
}
