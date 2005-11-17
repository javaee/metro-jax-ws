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

package com.sun.tools.ws.wsdl.framework;

/**
 * An entity that can be defined in a target namespace.
 *
 * @author WS Development Team
 */
public abstract class GlobalEntity extends Entity implements GloballyKnown {

    public GlobalEntity(Defining defining) {
        _defining = defining;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public abstract Kind getKind();

    public Defining getDefining() {
        return _defining;
    }

    private Defining _defining;
    private String _name;
}
