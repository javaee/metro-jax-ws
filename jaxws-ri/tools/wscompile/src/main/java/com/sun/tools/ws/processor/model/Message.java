/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.tools.ws.processor.model;

import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wscompile.AbortException;
import com.sun.tools.ws.resources.ModelMessages;

import javax.xml.namespace.QName;
import java.util.*;

/**
 *
 * @author WS Development Team
 */
public abstract class Message extends ModelObject {
    protected Message(com.sun.tools.ws.wsdl.document.Message entity, ErrorReceiver receiver) {
        super(entity);
        setErrorReceiver(receiver);
    }

    public void addBodyBlock(Block b) {
        if (_bodyBlocks.containsKey(b.getName())) {
            errorReceiver.error(getEntity().getLocator(), ModelMessages.MODEL_PART_NOT_UNIQUE(((com.sun.tools.ws.wsdl.document.Message)getEntity()).getName(), b.getName()));
            throw new AbortException();
        }
        _bodyBlocks.put(b.getName(), b);
        b.setLocation(Block.BODY);
    }

    public Iterator<Block> getBodyBlocks() {
        return _bodyBlocks.values().iterator();
    }

    public int getBodyBlockCount() {
        return _bodyBlocks.size();
    }

    /* serialization */
    public Map<QName, Block> getBodyBlocksMap() {
        return _bodyBlocks;
    }

    /* serialization */
    public void setBodyBlocksMap(Map<QName, Block> m) {
        _bodyBlocks = m;
    }

    public boolean isBodyEmpty() {
        return getBodyBlocks().hasNext();
    }

    public boolean isBodyEncoded() {
        boolean isEncoded = false;
        for (Iterator iter = getBodyBlocks(); iter.hasNext();) {
            Block bodyBlock = (Block) iter.next();
            if (bodyBlock.getType().isSOAPType()) {
                isEncoded = true;
            }
        }
        return isEncoded;
    }

    public void addHeaderBlock(Block b) {
        if (_headerBlocks.containsKey(b.getName())) {
            errorReceiver.error(getEntity().getLocator(), ModelMessages.MODEL_PART_NOT_UNIQUE(((com.sun.tools.ws.wsdl.document.Message)getEntity()).getName(), b.getName()));
            throw new AbortException();
        }
        _headerBlocks.put(b.getName(), b);
        b.setLocation(Block.HEADER);
    }

    public Iterator<Block> getHeaderBlocks() {
        return _headerBlocks.values().iterator();
    }

    public Collection<Block> getHeaderBlockCollection() {
        return _headerBlocks.values();
    }

    public int getHeaderBlockCount() {
        return _headerBlocks.size();
    }

    /* serialization */
    public Map<QName, Block> getHeaderBlocksMap() {
        return _headerBlocks;
    }

    /* serialization */
    public void setHeaderBlocksMap(Map<QName, Block> m) {
        _headerBlocks = m;
    }

    /** attachment block */
    public void addAttachmentBlock(Block b) {
        if (_attachmentBlocks.containsKey(b.getName())) {
            errorReceiver.error(getEntity().getLocator(), ModelMessages.MODEL_PART_NOT_UNIQUE(((com.sun.tools.ws.wsdl.document.Message)getEntity()).getName(), b.getName()));
            throw new AbortException();
        }
        _attachmentBlocks.put(b.getName(), b);
        b.setLocation(Block.ATTACHMENT);
    }

    public void addUnboundBlock(Block b) {
        if (_unboundBlocks.containsKey(b.getName())) {
            return;
        }
        _unboundBlocks.put(b.getName(), b);
        b.setLocation(Block.UNBOUND);
    }

    public Iterator<Block> getUnboundBlocks() {
        return _unboundBlocks.values().iterator();
    }

    /* serialization */
    public Map<QName, Block> getUnboundBlocksMap() {
        return _unboundBlocks;
    }

    public int getUnboundBlocksCount() {
        return _unboundBlocks.size();
    }

    /* serialization */
    public void setUnboundBlocksMap(Map<QName, Block> m) {
        _unboundBlocks = m;
    }


    public Iterator<Block> getAttachmentBlocks() {
        return _attachmentBlocks.values().iterator();
    }

    public int getAttachmentBlockCount () {
        return _attachmentBlocks.size();
    }

        /* serialization */
    public Map<QName, Block> getAttachmentBlocksMap() {
        return _attachmentBlocks;
    }

    /* serialization */
    public void setAttachmentBlocksMap(Map<QName, Block> m) {
        _attachmentBlocks = m;
    }

    public void addParameter(Parameter p) {
        if (_parametersByName.containsKey(p.getName())) {            
            errorReceiver.error(getEntity().getLocator(), ModelMessages.MODEL_PARAMETER_NOTUNIQUE(p.getName(), p.getName()));
            throw new AbortException();
        }
        _parameters.add(p);
        String name = p.getCustomName() != null ? p.getCustomName() : p.getName();
        _parametersByName.put(name, p);
    }

    public Parameter getParameterByName(String name) {
        if (_parametersByName.size() != _parameters.size()) {
            initializeParametersByName();
        }
        return _parametersByName.get(name);
    }

    public Iterator<Parameter> getParameters() {
        return _parameters.iterator();
    }

    /* serialization */
    public List<Parameter> getParametersList() {
        return _parameters;
    }

    /* serialization */
    public void setParametersList(List<Parameter> l) {
        _parameters = l;
    }

    private void initializeParametersByName() {
        _parametersByName = new HashMap();
        if (_parameters != null) {
            for (Iterator iter = _parameters.iterator(); iter.hasNext();) {
                Parameter param = (Parameter) iter.next();
                if (param.getName() != null &&
                    _parametersByName.containsKey(param.getName())) {
                    errorReceiver.error(getEntity().getLocator(), ModelMessages.MODEL_PARAMETER_NOTUNIQUE(param.getName(), param.getName()));
                    throw new AbortException();
                }
                _parametersByName.put(param.getName(), param);
            }
        }
    }

    public Set<Block> getAllBlocks(){
        Set<Block> blocks = new HashSet<Block>();
        blocks.addAll(_bodyBlocks.values());
        blocks.addAll(_headerBlocks.values());
        blocks.addAll(_attachmentBlocks.values());
        return blocks;
    }

    private Map<QName, Block> _attachmentBlocks = new HashMap<QName, Block>();
    private Map<QName, Block> _bodyBlocks = new HashMap<QName, Block>();
    private Map<QName, Block> _headerBlocks = new HashMap<QName, Block>();
    private Map<QName, Block> _unboundBlocks = new HashMap<QName, Block>();
    private List<Parameter> _parameters = new ArrayList<Parameter>();
    private Map<String, Parameter> _parametersByName = new HashMap<String, Parameter>();
}
