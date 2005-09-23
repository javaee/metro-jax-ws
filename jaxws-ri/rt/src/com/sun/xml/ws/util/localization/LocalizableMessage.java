/*
 * $Id: LocalizableMessage.java,v 1.4 2005-09-23 22:05:38 kohsuke Exp $
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

package com.sun.xml.ws.util.localization;

/**
 * @author WS Development Team
 */
public class LocalizableMessage implements Localizable {

    protected String _bundlename;
    protected String _key;
    protected Object[] _args;

    public LocalizableMessage(
        String bundlename,
        String key,
        Object... args) {
        _bundlename = bundlename;
        _key = key;
        _args = args;
    }

    public String getKey() {
        return _key;
    }

    public Object[] getArguments() {
        return _args;
    }

    public String getResourceBundleName() {
        return _bundlename;
    }
}
