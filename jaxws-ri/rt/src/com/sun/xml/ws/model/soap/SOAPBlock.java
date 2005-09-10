/**
 * $Id: SOAPBlock.java,v 1.5 2005-09-10 19:47:50 kohsuke Exp $
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
package com.sun.xml.ws.model.soap;

/**
 * Denotes the soap block
 *
 * @author Vivek Pandey
 */
public enum SOAPBlock {
    BODY(0), HEADER(1), MIME(2), UNBOUND(3);
    
    SOAPBlock(int type){
        this.type = type;
    }

    public int value() {
        return type;
    }

    public boolean isHeader(){
        return type == HEADER.value();
    }

    public boolean isBody(){
        return type == BODY.value();
    }

    public boolean isAttachment(){
        return type == MIME.value();
    }

    public boolean isUnbound(){
        return type == UNBOUND.value();
    }

    private final int type;
}
