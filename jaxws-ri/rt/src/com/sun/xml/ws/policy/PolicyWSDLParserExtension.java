/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.policy;

import com.sun.xml.ws.api.model.wsdl.*;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtensionContext;
import com.sun.xml.ws.api.policy.PolicyResolver;
import com.sun.xml.ws.resources.PolicyMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelUnmarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModelContext;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken;
import com.sun.xml.ws.model.wsdl.WSDLModelImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.ws.WebServiceException;

/**
 * This class parses the Policy Attachments in the WSDL and creates a PolicyMap thaty captures the policies configured on
 * different PolicySubjects in the wsdl.
 *
 * After, it is finished it sets the PolicyMap on the WSDLModel.
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 * @author Fabian Ritzmann
 * @author Rama Pulavarthi
 */
final public class PolicyWSDLParserExtension extends WSDLParserExtension {
    
    enum HandlerType {
        PolicyUri, AnonymousPolicyId
    }
    
    final static class PolicyRecordHandler {
        String handler;
        HandlerType type;
        
        PolicyRecordHandler(HandlerType type, String handler) {
            this.type = type;
            this.handler = handler;
        }
        
        HandlerType getType() {
            return type;
        }
        
        String getHandler() {
            return handler;
        }
    }
    
    final static class PolicyRecord {
        PolicyRecord next;
        String uri;
        PolicySourceModel policyModel;
        Set<String> unresolvedURIs;
        
        PolicyRecord() {
            // nothing to initialize
        }
        
        PolicyRecord insert(final PolicyRecord insertedRec) {
            if (null==insertedRec.unresolvedURIs || insertedRec.unresolvedURIs.isEmpty()) {
                insertedRec.next = this;
                return insertedRec;
            }
            final PolicyRecord head = this;
            PolicyRecord oneBeforeCurrent = null;
            PolicyRecord current;
            for (current = head ; null != current.next ; ) {
                if ((null != current.unresolvedURIs) && current.unresolvedURIs.contains(insertedRec.uri)) {
                    if (null == oneBeforeCurrent) {
                        insertedRec.next = current;
                        return insertedRec;
                    } else { // oneBeforeCurrent != null
                        oneBeforeCurrent.next = insertedRec;
                        insertedRec.next = current;
                        return head;
                    } // end-if-else oneBeforeCurrent == null
                }// end-if current record depends on inserted one
                if (insertedRec.unresolvedURIs.remove(current.uri) && (insertedRec.unresolvedURIs.isEmpty())) {
                    insertedRec.next = current.next;
                    current.next = insertedRec;
                    return head;
                } // end-if one of unresolved URIs resolved by current record and thus unresolvedURIs empty
                oneBeforeCurrent = current;
                current = current.next;
            } // end for (current = head; null!=current.next; )
            insertedRec.next = null;
            current.next = insertedRec;
            return head;
        }
        
        @Override
        public String toString() {
            String result = uri;
            if (null!=next) {
                result += "->" + next.toString();
            }
            return result;
        }
    }
    
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyWSDLParserExtension.class);
    
    //anonymous policy id prefix
    private static final StringBuffer AnonymnousPolicyIdPrefix = new StringBuffer("#__anonymousPolicy__ID");
    
    // anonymous policies count
    private int anonymousPoliciesCount;
    
    // policy queue -- needed for evaluating the right order policy of policy models expansion
    private PolicyRecord expandQueueHead = null;
    
    // storage for policy models with an id passed by
    private Map<String,PolicyRecord> policyRecordsPassedBy = null;
    // storage for anonymous policies defined within given WSDL
    private Map<String,PolicySourceModel> anonymousPolicyModels = null;
    
    // container for URIs of policies referenced
    private List<String> unresolvedUris = null;
    
    // urls of xml docs policies were read from
    final Set<String> urlsRead = new HashSet<String>();
    
    // structures for policies really needed to build a map
    private final LinkedList<String> urisNeeded = new LinkedList<String>();
    private final Map<String, PolicySourceModel> modelsNeeded = new HashMap<String, PolicySourceModel>();
    
    // lookup tables for Policy attachments found
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4ServiceMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4PortMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4PortTypeMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4BindingMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4BoundOperationMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4OperationMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4MessageMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4InputMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4OutputMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4FaultMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4BindingInputOpMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4BindingOutputOpMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4BindingFaultOpMap = null;
    
    private PolicyMapBuilder policyBuilder = new PolicyMapBuilder();
    
    private boolean isPolicyProcessed(final String policyUri) {
        return modelsNeeded.containsKey(policyUri);
    }
    
    private void addNewPolicyNeeded(final String policyUri, final PolicySourceModel policyModel) {
        if (!modelsNeeded.containsKey(policyUri)) {
            modelsNeeded.put(policyUri, policyModel);
            urisNeeded.addFirst(policyUri);
        }
    }
    
    private Map<String, PolicySourceModel> getPolicyModels() {
        return modelsNeeded;
    }
    
    private Map<String,PolicyRecord> getPolicyRecordsPassedBy() {
        if (null==policyRecordsPassedBy) {
            policyRecordsPassedBy = new HashMap<String,PolicyRecord>();
        }
        return policyRecordsPassedBy;
    }
    
    private Map<String,PolicySourceModel> getAnonymousPolicyModels() {
        if (null==anonymousPolicyModels) {
            anonymousPolicyModels = new HashMap<String,PolicySourceModel>();
        }
        return anonymousPolicyModels;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4ServiceMap() {
        if (null==handlers4ServiceMap) {
            handlers4ServiceMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4ServiceMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4PortMap() {
        if (null==handlers4PortMap) {
            handlers4PortMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4PortMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4PortTypeMap() {
        if (null==handlers4PortTypeMap) {
            handlers4PortTypeMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4PortTypeMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4BindingMap() {
        if (null==handlers4BindingMap) {
            handlers4BindingMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4BindingMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4OperationMap() {
        if (null==handlers4OperationMap) {
            handlers4OperationMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4OperationMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4BoundOperationMap() {
        if (null==handlers4BoundOperationMap) {
            handlers4BoundOperationMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4BoundOperationMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4MessageMap() {
        if (null==handlers4MessageMap) {
            handlers4MessageMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4MessageMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4InputMap() {
        if (null==handlers4InputMap) {
            handlers4InputMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4InputMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4OutputMap() {
        if (null==handlers4OutputMap) {
            handlers4OutputMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4OutputMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4FaultMap() {
        if (null==handlers4FaultMap) {
            handlers4FaultMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4FaultMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4BindingInputOpMap() {
        if (null==handlers4BindingInputOpMap) {
            handlers4BindingInputOpMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4BindingInputOpMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4BindingOutputOpMap() {
        if (null==handlers4BindingOutputOpMap) {
            handlers4BindingOutputOpMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4BindingOutputOpMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4BindingFaultOpMap() {
        if (null==handlers4BindingFaultOpMap) {
            handlers4BindingFaultOpMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4BindingFaultOpMap;
    }
    
    private List<String> getUnresolvedUris(final boolean emptyListNeeded) {
        if ((null == unresolvedUris) || emptyListNeeded) {
            unresolvedUris = new LinkedList<String>();
        }
        return unresolvedUris;
    }
    
    
    
    private void policyRecToExpandQueue(final PolicyRecord policyRec) {
        if (null==expandQueueHead) {
            expandQueueHead = policyRec;
        } else {
            expandQueueHead = expandQueueHead.insert(policyRec);
        }
    }
    
    /**
     * Creates a new instance of PolicyWSDLParserExtension
     */
    public PolicyWSDLParserExtension() {

    }
    
    
    private PolicyRecordHandler readSinglePolicy(final PolicyRecord policyRec, final boolean inner) {
        PolicyRecordHandler handler = null;
        String policyId = policyRec.policyModel.getPolicyId();
        if (policyId == null) {
            policyId = policyRec.policyModel.getPolicyName();
        }
        if (policyId != null) {           // policy id defined, keep the policy
            handler = new PolicyRecordHandler(HandlerType.PolicyUri,policyRec.uri);
            getPolicyRecordsPassedBy().put(policyRec.uri, policyRec);
            policyRecToExpandQueue(policyRec);
        } else if (inner) { // no id given to the policy --> keep as an annonymous policy model
            final String anonymousId = AnonymnousPolicyIdPrefix.append(anonymousPoliciesCount++).toString();
            handler = new PolicyRecordHandler(HandlerType.AnonymousPolicyId,anonymousId);
            getAnonymousPolicyModels().put(anonymousId, policyRec.policyModel);
            if (null != policyRec.unresolvedURIs) {
                getUnresolvedUris(false).addAll(policyRec.unresolvedURIs);
            }
        }
        return handler;
    }
    
    
    private void addHandlerToMap(
            final Map<WSDLObject, Collection<PolicyRecordHandler>> map, final WSDLObject key, final PolicyRecordHandler handler) {
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            final Collection<PolicyRecordHandler> newSet = new LinkedList<PolicyRecordHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private String getBaseUrl(final String policyUri) {
        if (null == policyUri) {
            return null;
        }
        // TODO: encoded urls (escaped characters) might be a problem ?
        final int fragmentIdx = policyUri.indexOf('#');
        return (fragmentIdx == -1) ? policyUri : policyUri.substring(0, fragmentIdx);
    }
    
    private String relativeToAbsoluteUrl(final String relativeUri, final String baseUri) {
        if ('#' != relativeUri.charAt(0)) {  // TODO: escaped char could be an issue?
            return relativeUri; // absolute already
        }
        return (null == baseUri) ? relativeUri : baseUri + relativeUri;
    }
    
    // adding current url even to locally referenced policies
    // in order to distinguish imported policies
    private void processReferenceUri(
            final String policyUri,
            final WSDLObject element,
            final XMLStreamReader reader,
            final Map<WSDLObject, Collection<PolicyRecordHandler>> map) {
        
        if (null == policyUri || policyUri.length() == 0) {
            return;
        }
        if ('#' != policyUri.charAt(0)) { // external uri (already)
            getUnresolvedUris(false).add(policyUri);
        }
        
        addHandlerToMap(map, element,
                new PolicyRecordHandler(
                HandlerType.PolicyUri,
                relativeToAbsoluteUrl(policyUri, reader.getLocation().getSystemId())));
    }
    
    private boolean processSubelement(
            final WSDLObject element, final XMLStreamReader reader, final Map<WSDLObject, Collection<PolicyRecordHandler>> map) {
        if (NamespaceVersion.resolveAsToken(reader.getName()) == XmlToken.PolicyReference) {     // "PolicyReference" element interests us
            processReferenceUri(readPolicyReferenceElement(reader), element, reader, map);
            return true;
        } else if (NamespaceVersion.resolveAsToken(reader.getName()) == XmlToken.Policy) {   // policy could be defined here
            final PolicyRecordHandler handler =
                    readSinglePolicy(
                    skipPolicyElement(
                    reader,
                    (null == reader.getLocation().getSystemId()) ? // baseUrl
                        "" : reader.getLocation().getSystemId()),
                    true);
            if (null != handler) {           // only policies with an Id can work for us
                addHandlerToMap(map, element, handler);
            } // endif null != handler
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    private void processAttributes(final WSDLObject element, final XMLStreamReader reader, final Map<WSDLObject, Collection<PolicyRecordHandler>> map) {
        final String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null != uriArray) {
            for (String policyUri : uriArray) {
                processReferenceUri(policyUri, element, reader, map);
            }
        }
    }
    
    @Override
    public boolean portElements(final WSDLPort port, final XMLStreamReader reader) {
        LOGGER.entering();
        final boolean result = processSubelement(port, reader, getHandlers4PortMap());
        LOGGER.exiting();
        return result;
    }
    
    @Override
    public void portAttributes(final WSDLPort port, final XMLStreamReader reader) {
        LOGGER.entering();
        processAttributes(port, reader, getHandlers4PortMap());
        LOGGER.exiting();
    }
    
    @Override
    public boolean serviceElements(final WSDLService service, final XMLStreamReader reader) {
        LOGGER.entering();
        final boolean result = processSubelement(service, reader, getHandlers4ServiceMap());
        LOGGER.exiting();
        return result;
    }
    
    @Override
    public void serviceAttributes(final WSDLService service, final XMLStreamReader reader) {
        LOGGER.entering();
        processAttributes(service, reader, getHandlers4ServiceMap());
        LOGGER.exiting();
    }
    
    
    @Override
    public boolean definitionsElements(final XMLStreamReader reader){
        LOGGER.entering();
        if (NamespaceVersion.resolveAsToken(reader.getName()) == XmlToken.Policy) {     // Only "Policy" element interests me
            readSinglePolicy(
                    skipPolicyElement(
                    reader,
                    (null == reader.getLocation().getSystemId()) ? // baseUrl
                        "" : reader.getLocation().getSystemId()),
                    false);
            LOGGER.exiting();
            return true;
        }
        LOGGER.exiting();
        return false;
    }
    
    @Override
    public boolean bindingElements(final WSDLBoundPortType binding, final XMLStreamReader reader) {
        LOGGER.entering();
        final boolean result = processSubelement(binding, reader, getHandlers4BindingMap());
        LOGGER.exiting();
        return result;
    }
    
    @Override
    public void bindingAttributes(final WSDLBoundPortType binding, final XMLStreamReader reader) {
        LOGGER.entering();
        processAttributes(binding, reader, getHandlers4BindingMap());
        LOGGER.exiting();
    }
    
    @Override
    public boolean portTypeElements(final WSDLPortType portType, final XMLStreamReader reader) {
        LOGGER.entering();
        final boolean result = processSubelement(portType, reader, getHandlers4PortTypeMap());
        LOGGER.exiting();
        return result;
    }
    
    @Override
    public void portTypeAttributes(final WSDLPortType portType, final XMLStreamReader reader) {
        LOGGER.entering();
        processAttributes(portType, reader, getHandlers4PortTypeMap());
        LOGGER.exiting();
    }
    
    @Override
    public boolean portTypeOperationElements(final WSDLOperation operation, final XMLStreamReader reader) {
        LOGGER.entering();
        final boolean result = processSubelement(operation, reader, getHandlers4OperationMap());
        LOGGER.exiting();
        return result;
    }
    
    @Override
    public void portTypeOperationAttributes(final WSDLOperation operation, final XMLStreamReader reader) {
        LOGGER.entering();
        processAttributes(operation, reader, getHandlers4OperationMap());
        LOGGER.exiting();
    }
    
    @Override
    public boolean bindingOperationElements(final WSDLBoundOperation boundOperation, final XMLStreamReader reader) {
        LOGGER.entering();
        final boolean result = processSubelement(boundOperation, reader, getHandlers4BoundOperationMap());
        LOGGER.exiting();
        return result;
    }
    
    @Override
    public void bindingOperationAttributes(final WSDLBoundOperation boundOperation, final XMLStreamReader reader) {
        LOGGER.entering();
        processAttributes(boundOperation, reader, getHandlers4BoundOperationMap());
        LOGGER.exiting();
    }
    
    @Override
    public boolean messageElements(final WSDLMessage msg, final XMLStreamReader reader) {
        LOGGER.entering();
        final boolean result = processSubelement(msg, reader, getHandlers4MessageMap());
        LOGGER.exiting();
        return result;
    }
    
    @Override
    public void messageAttributes(final WSDLMessage msg, final XMLStreamReader reader) {
        LOGGER.entering();
        processAttributes(msg, reader, getHandlers4MessageMap());
        LOGGER.exiting();
    }
        
    @Override
    public boolean portTypeOperationInputElements(final WSDLInput input, final XMLStreamReader reader) {
        LOGGER.entering();
        final boolean result = processSubelement(input, reader, getHandlers4InputMap());
        LOGGER.exiting();
        return result;
    }
    
    @Override
    public void portTypeOperationInputAttributes(final WSDLInput input, final XMLStreamReader reader) {
        LOGGER.entering();
        processAttributes(input, reader, getHandlers4InputMap());
        LOGGER.exiting();
    }
    
    
    @Override
    public boolean portTypeOperationOutputElements(final WSDLOutput output, final XMLStreamReader reader) {
        LOGGER.entering();
        final boolean result = processSubelement(output, reader, getHandlers4OutputMap());
        LOGGER.exiting();
        return result;
    }
    
    @Override
    public void portTypeOperationOutputAttributes(final WSDLOutput output, final XMLStreamReader reader) {
        LOGGER.entering();
        processAttributes(output, reader, getHandlers4OutputMap());
        LOGGER.exiting();
    }
    
    
    @Override
    public boolean portTypeOperationFaultElements(final WSDLFault fault, final XMLStreamReader reader) {
        LOGGER.entering();
        final boolean result = processSubelement(fault, reader, getHandlers4FaultMap());
        LOGGER.exiting();
        return result;
    }
    
    @Override
    public void portTypeOperationFaultAttributes(final WSDLFault fault, final XMLStreamReader reader) {
        LOGGER.entering();
        processAttributes(fault, reader, getHandlers4FaultMap());
        LOGGER.exiting();
    }
    
    @Override
    public boolean bindingOperationInputElements(final WSDLBoundOperation operation, final XMLStreamReader reader) {
        LOGGER.entering();
        final boolean result = processSubelement(operation, reader, getHandlers4BindingInputOpMap());
        LOGGER.exiting();
        return result;
    }
    
    @Override
    public void bindingOperationInputAttributes(final WSDLBoundOperation operation, final XMLStreamReader reader) {
        LOGGER.entering();
        processAttributes(operation, reader, getHandlers4BindingInputOpMap());
        LOGGER.exiting();
    }
    
    
    @Override
    public boolean bindingOperationOutputElements(final WSDLBoundOperation operation, final XMLStreamReader reader) {
        LOGGER.entering();
        final boolean result = processSubelement(operation, reader, getHandlers4BindingOutputOpMap());
        LOGGER.exiting();
        return result;
    }
    
    @Override
    public void bindingOperationOutputAttributes(final WSDLBoundOperation operation, final XMLStreamReader reader) {
        LOGGER.entering();
        processAttributes(operation, reader, getHandlers4BindingOutputOpMap());
        LOGGER.exiting();
    }
    
    @Override
    public boolean bindingOperationFaultElements(final WSDLBoundFault fault, final XMLStreamReader reader) {
        LOGGER.entering();
        final boolean result = processSubelement(fault, reader, getHandlers4BindingFaultOpMap());
        LOGGER.exiting(result);
        return result;
    }
    
    @Override
    public void bindingOperationFaultAttributes(final WSDLBoundFault fault, final XMLStreamReader reader) {
        LOGGER.entering();
        processAttributes(fault, reader, getHandlers4BindingFaultOpMap());
        LOGGER.exiting();
    }
    
    
    private PolicyMapBuilder getPolicyMapBuilder() {
        if (null == policyBuilder) {
            policyBuilder = new PolicyMapBuilder();
        }
        return policyBuilder;
    }
    
    private Collection<String> getPolicyURIs(
            final Collection<PolicyRecordHandler> handlers, final PolicySourceModelContext modelContext) throws PolicyException{
        final Collection<String> result = new ArrayList<String>(handlers.size());
        String policyUri;
        for (PolicyRecordHandler handler : handlers) {
            policyUri = handler.handler;
            if (HandlerType.AnonymousPolicyId == handler.type) {
                final PolicySourceModel policyModel = getAnonymousPolicyModels().get(policyUri);
                policyModel.expand(modelContext);
                while (getPolicyModels().containsKey(policyUri)) {
                    policyUri = AnonymnousPolicyIdPrefix.append(anonymousPoliciesCount++).toString();
                }
                getPolicyModels().put(policyUri,policyModel);
            }
            result.add(policyUri);
        }
        return result;
    }
    
    private boolean readExternalFile(final String fileUrl) {
        InputStream ios = null;
        XMLStreamReader reader = null;
        try {
            final URL xmlURL = new URL(fileUrl);
            ios = xmlURL.openStream();
            reader = XMLInputFactory.newInstance().createXMLStreamReader(ios);
            while (reader.hasNext()) {
                if (reader.isStartElement() && NamespaceVersion.resolveAsToken(reader.getName()) == XmlToken.Policy) {
                    readSinglePolicy(skipPolicyElement(reader, fileUrl), false);
                }
                reader.next();
            }
            return true;
        } catch (IOException ioe) {
            return false;
        } catch (XMLStreamException xmlse) {
            return false;
        } finally {
            PolicyUtils.IO.closeResource(reader);
            PolicyUtils.IO.closeResource(ios);
        }
    }
    
    @Override
    public void finished(final WSDLParserExtensionContext context) {
        LOGGER.entering();
        // need to make sure proper beginning order of internal policies within unresolvedUris list
        if (null != expandQueueHead) { // any policies found
            final List<String> externalUris = getUnresolvedUris(false); // protect list of possible external policies
            getUnresolvedUris(true); // cleaning up the list only
            final LinkedList<String> baseUnresolvedUris = new LinkedList<String>();
            for (PolicyRecord currentRec = expandQueueHead ; null != currentRec ; currentRec = currentRec.next) {
                baseUnresolvedUris.addFirst(currentRec.uri);
            }
            getUnresolvedUris(false).addAll(baseUnresolvedUris);
            expandQueueHead = null; // cut the queue off
            getUnresolvedUris(false).addAll(externalUris);
        }
//        final Set<String> urlsRead = new HashSet<String>();
//        urlsRead.add("");
        while (!getUnresolvedUris(false).isEmpty()) {
            final List<String> urisToBeSolvedList = getUnresolvedUris(false);
            getUnresolvedUris(true); // just cleaning up the list
            for (String currentUri : urisToBeSolvedList) {
                if (!isPolicyProcessed(currentUri)) {
                    final PolicyRecord prefetchedRecord = getPolicyRecordsPassedBy().get(currentUri);
                    if (null == prefetchedRecord) {
                        if (urlsRead.contains(getBaseUrl(currentUri))) { // big problem --> unresolvable policy
                            LOGGER.logSevereException(new PolicyException(PolicyMessages.WSP_1042_CAN_NOT_FIND_POLICY(currentUri)));
                        } else {
                            if (readExternalFile(getBaseUrl(currentUri))) {
                                getUnresolvedUris(false).add(currentUri);
                            }
                        }
                    } else { // policy has not been yet passed by
                        if (null != prefetchedRecord.unresolvedURIs) {
                            getUnresolvedUris(false).addAll(prefetchedRecord.unresolvedURIs);
                        } // end-if null != prefetchedRecord.unresolvedURIs
                        addNewPolicyNeeded(currentUri, prefetchedRecord.policyModel);
                    }
                } // end-if policy already processed
            } // end-foreach unresolved uris
        }
        final PolicySourceModelContext modelContext = PolicySourceModelContext.createContext();
        for (String policyUri : urisNeeded) {
            final PolicySourceModel sourceModel = modelsNeeded.get(policyUri);
            try {
                sourceModel.expand(modelContext);
                modelContext.addModel(new URI(policyUri), sourceModel);
            } catch (URISyntaxException e) {
                LOGGER.logSevereException(e);
            } catch (PolicyException e) {
                LOGGER.logSevereException(e);
            }
        }
        
        // Start-preparation of policy map builder
        // iterating over all services and binding all the policies read before
        try {
            // messageSet holds the handlers for all wsdl:message elements. There
            // may otherwise be multiple entries for policies that are contained
            // by fault messages.
            HashSet<BuilderHandlerMessageScope> messageSet = new HashSet<BuilderHandlerMessageScope>();
            for (WSDLService service : context.getWSDLModel().getServices().values()) {
                if (getHandlers4ServiceMap().containsKey(service)) {
                    getPolicyMapBuilder().registerHandler(new BuilderHandlerServiceScope(
                            getPolicyURIs(getHandlers4ServiceMap().get(service),modelContext)
                            ,getPolicyModels()
                            ,service
                            ,service.getName()));
                }
                // end service scope
                
                for (WSDLPort port : service.getPorts()) {
                    if (getHandlers4PortMap().containsKey(port)) {
                        getPolicyMapBuilder().registerHandler(
                                new BuilderHandlerEndpointScope(
                                getPolicyURIs(getHandlers4PortMap().get(port),modelContext)
                                ,getPolicyModels()
                                ,port
                                ,port.getOwner().getName()
                                ,port.getName()));
                    }
                    if ( // port.getBinding may not be null, but in case ...
                            null != port.getBinding()) {
                        if ( // handler for binding
                                getHandlers4BindingMap().containsKey(port.getBinding())) {
                            getPolicyMapBuilder()
                            .registerHandler(
                                    new BuilderHandlerEndpointScope(
                                    getPolicyURIs(getHandlers4BindingMap().get(port.getBinding()),modelContext)
                                    ,getPolicyModels()
                                    ,port.getBinding()
                                    ,service.getName()
                                    ,port.getName()));
                        } // endif handler for binding
                        if ( // handler for port type
                                getHandlers4PortTypeMap().containsKey(port.getBinding().getPortType())) {
                            getPolicyMapBuilder()
                            .registerHandler(
                                    new BuilderHandlerEndpointScope(
                                    getPolicyURIs(getHandlers4PortTypeMap().get(port.getBinding().getPortType()),modelContext)
                                    ,getPolicyModels()
                                    ,port.getBinding().getPortType()
                                    ,service.getName()
                                    ,port.getName()));
                        } // endif handler for port type
                        // end endpoint scope
                        
                        for (WSDLBoundOperation boundOperation : port.getBinding().getBindingOperations()) {

                            final WSDLOperation operation = boundOperation.getOperation();
                            final QName operationName = new QName(boundOperation.getBoundPortType().getName().getNamespaceURI(), boundOperation.getName().getLocalPart());
                            // We store the message and portType/operation under the same namespace as the binding/operation so that we can match them up later
                            if ( // handler for operation scope -- by boundOperation
                                    getHandlers4BoundOperationMap().containsKey(boundOperation)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerOperationScope(
                                        getPolicyURIs(getHandlers4BoundOperationMap().get(boundOperation),modelContext)
                                        ,getPolicyModels()
                                        ,boundOperation
                                        ,service.getName()
                                        ,port.getName()
                                        ,operationName));
                            } // endif handler for binding:operation scope
                            if ( // handler for operation scope -- by operation map
                                    getHandlers4OperationMap().containsKey(operation)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerOperationScope(
                                        getPolicyURIs(getHandlers4OperationMap().get(operation),modelContext)
                                        ,getPolicyModels()
                                        ,operation
                                        ,service.getName()
                                        ,port.getName()
                                        ,operationName));
                            } // endif for portType:operation scope
                            // end operation scope

                            final WSDLInput input = operation.getInput();
                            if (null!=input) {
                                WSDLMessage inputMsg = input.getMessage();
                                if (inputMsg != null && getHandlers4MessageMap().containsKey(inputMsg)) {
                                    messageSet.add(new BuilderHandlerMessageScope(
                                        getPolicyURIs(
                                            getHandlers4MessageMap().get(inputMsg), modelContext)
                                            ,getPolicyModels()
                                            ,inputMsg
                                            ,BuilderHandlerMessageScope.Scope.InputMessageScope
                                            ,service.getName()
                                            ,port.getName()
                                            ,operationName
                                            ,null)
                                    );
                                }
                            }
                            if ( // binding op input msg
                                    getHandlers4BindingInputOpMap().containsKey(boundOperation)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerMessageScope(
                                        getPolicyURIs(getHandlers4BindingInputOpMap().get(boundOperation),modelContext)
                                        ,getPolicyModels()
                                        ,boundOperation
                                        ,BuilderHandlerMessageScope.Scope.InputMessageScope
                                        ,service.getName()
                                        ,port.getName()
                                        ,operationName
                                        ,null));
                            } // endif binding op input msg
                            if ( null != input    // portType op input msg
                                    && getHandlers4InputMap().containsKey(input)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerMessageScope(
                                        getPolicyURIs(getHandlers4InputMap().get(input),modelContext)
                                        ,getPolicyModels()
                                        ,input
                                        ,BuilderHandlerMessageScope.Scope.InputMessageScope
                                        ,service.getName()
                                        ,port.getName()
                                        ,operationName
                                        ,null));
                            } // endif portType op input msg
                            // end input message scope
                            
                            final WSDLOutput output = operation.getOutput();
                            if (null!=output) {
                                WSDLMessage outputMsg = output.getMessage();
                                if (outputMsg != null && getHandlers4MessageMap().containsKey(outputMsg)) {
                                    messageSet.add(new BuilderHandlerMessageScope(
                                        getPolicyURIs(
                                            getHandlers4MessageMap().get(outputMsg),modelContext)
                                            ,getPolicyModels()
                                            ,outputMsg
                                            ,BuilderHandlerMessageScope.Scope.OutputMessageScope
                                            ,service.getName()
                                            ,port.getName()
                                            ,operationName
                                            ,null)
                                    );
                                }
                            }
                            if ( // binding op output msg
                                    getHandlers4BindingOutputOpMap().containsKey(boundOperation)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerMessageScope(
                                        getPolicyURIs(getHandlers4BindingOutputOpMap().get(boundOperation),modelContext)
                                        ,getPolicyModels()
                                        ,boundOperation
                                        ,BuilderHandlerMessageScope.Scope.OutputMessageScope
                                        ,service.getName()
                                        ,port.getName()
                                        ,operationName
                                        ,null));
                            } // endif binding op output msg
                            if ( null != output // portType op output msg
                                    && getHandlers4OutputMap().containsKey(output)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerMessageScope(
                                        getPolicyURIs(getHandlers4OutputMap().get(output),modelContext)
                                        ,getPolicyModels()
                                        ,output
                                        ,BuilderHandlerMessageScope.Scope.OutputMessageScope
                                        ,service.getName()
                                        ,port.getName()
                                        ,operationName
                                        ,null));
                            } // endif portType op output msg
                            // end output message scope
                            
                            for (WSDLBoundFault boundFault : boundOperation.getFaults()) {
                                final WSDLFault fault = boundFault.getFault();
                                final WSDLMessage faultMessage = fault.getMessage();
                                final QName faultName = new QName(boundOperation.getBoundPortType().getName().getNamespaceURI(), boundFault.getName());
                                // We store the message and portType/fault under the same namespace as the binding/fault so that we can match them up later
                                if (faultMessage != null && getHandlers4MessageMap().containsKey(faultMessage)) {
                                    messageSet.add(
                                        new BuilderHandlerMessageScope(
                                            getPolicyURIs(getHandlers4MessageMap().get(faultMessage), modelContext)
                                            ,getPolicyModels()
                                            ,new WSDLBoundFaultContainer(boundFault, boundOperation)
                                            ,BuilderHandlerMessageScope.Scope.FaultMessageScope
                                            ,service.getName()
                                            ,port.getName()
                                            ,operationName
                                            ,faultName)
                                        );
                                }
                                if (getHandlers4FaultMap().containsKey(fault)) {
                                    messageSet.add(
                                        new BuilderHandlerMessageScope(
                                            getPolicyURIs(getHandlers4FaultMap().get(fault), modelContext)
                                            ,getPolicyModels()
                                            ,new WSDLBoundFaultContainer(boundFault, boundOperation)
                                            ,BuilderHandlerMessageScope.Scope.FaultMessageScope
                                            ,service.getName()
                                            ,port.getName()
                                            ,operationName
                                            ,faultName)
                                        );
                                }
                                if (getHandlers4BindingFaultOpMap().containsKey(boundFault)) {
                                    messageSet.add(
                                        new BuilderHandlerMessageScope(
                                            getPolicyURIs(getHandlers4BindingFaultOpMap().get(boundFault), modelContext)
                                            ,getPolicyModels()
                                            ,new WSDLBoundFaultContainer(boundFault, boundOperation)
                                            ,BuilderHandlerMessageScope.Scope.FaultMessageScope
                                            ,service.getName()
                                            ,port.getName()
                                            ,operationName
                                            ,faultName)
                                        );
                                }
                            } // end foreach binding operation fault msg
                            // end fault message scope
                            
                        } // end foreach boundOperation in port
                    } // endif port.getBinding() != null
                } // end foreach port in service
            } // end foreach service in wsdl
            // Add handlers for wsdl:message elements
            for (BuilderHandlerMessageScope scopeHandler : messageSet) {
                getPolicyMapBuilder().registerHandler(scopeHandler);
            }
        } catch(PolicyException e) {
            LOGGER.logSevereException(e);
        }
        // End-preparation of policy map builder

        LOGGER.exiting();
    }


    // time to read possible config file and do alternative selection (on client side)
    @Override
    public void postFinished(final WSDLParserExtensionContext context) {
        // finally register the PolicyMap on the WSDLModel
        WSDLModel wsdlModel = context.getWSDLModel();
        PolicyMap effectiveMap;
        try {
            if(context.isClientSide())
                effectiveMap = context.getPolicyResolver().resolve(new PolicyResolver.ClientContext(policyBuilder.getPolicyMap(),context.getContainer()));
            else
                effectiveMap = context.getPolicyResolver().resolve(new PolicyResolver.ServerContext(policyBuilder.getPolicyMap(), context.getContainer(),null));
            ((WSDLModelImpl) wsdlModel).setPolicyMap(effectiveMap);
        } catch (PolicyException e) {
            LOGGER.logSevereException(e);
            throw LOGGER.logSevereException(new WebServiceException(PolicyMessages.WSP_1018_POLICY_EXCEPTION_WHILE_FINISHING_PARSING_WSDL(), e));
        }
        try {
            PolicyUtil.configureModel(wsdlModel,effectiveMap);
        } catch (PolicyException e) {
            LOGGER.logSevereException(e);
            throw LOGGER.logSevereException(new WebServiceException(PolicyMessages.WSP_1032_FAILED_CONFIGURE_WSDL_MODEL(), e));
        }
        LOGGER.exiting();
    }

    /**
     * Reads policy reference element <wsp:PolicyReference/> and returns referenced policy URI as String
     */
    private String readPolicyReferenceElement(final XMLStreamReader reader) {
        try {
            if (NamespaceVersion.resolveAsToken(reader.getName()) == XmlToken.PolicyReference) {     // "PolicyReference" element interests me
                for (int i = 0; i < reader.getAttributeCount(); i++) {
                    if (XmlToken.resolveToken(reader.getAttributeName(i).getLocalPart()) == XmlToken.Uri) {
                        final String uriValue = reader.getAttributeValue(i);
                        reader.next();
                        return uriValue;
                    }
                }
            }
            reader.next();
            return null;
        } catch(XMLStreamException e) {
            throw LOGGER.logSevereException(new WebServiceException(PolicyMessages.WSP_1001_XML_EXCEPTION_WHEN_PROCESSING_POLICY_REFERENCE(), e));
        }
    }
    
    
    /**
     * Reads policy reference URIs from PolicyURIs attribute and returns them 
     * as a String array returns null if there is no such attribute. This method 
     * will attempt to check for the attribute in every supported policy namespace.
     * Resulting array of URIs is concatenation of URIs defined in all found 
     * PolicyURIs attribute version.
     */
    private String[] getPolicyURIsFromAttr(final XMLStreamReader reader) {
        StringBuffer policyUriBuffer = new StringBuffer();
        for (NamespaceVersion version : NamespaceVersion.values()) {
            final String value = reader.getAttributeValue(version.toString(), XmlToken.PolicyUris.toString());
            if (value != null) {
                policyUriBuffer.append(value).append(" ");
            }
        }
        return (policyUriBuffer.length() > 0) ? policyUriBuffer.toString().split("[\\n ]+") : null;
    }
    
    
    /**
     *  skips current element (should be in START_ELEMENT state) and returns its content as String
     */
    private PolicyRecord skipPolicyElement(final XMLStreamReader reader, final String baseUrl){
        if ((null == reader) || (!reader.isStartElement())) {
            return null;
        }
        final StringBuffer elementCode = new StringBuffer();
        final PolicyRecord policyRec = new PolicyRecord();
        final QName elementName = reader.getName();
        boolean insidePolicyReferenceAttr;
        int depth = 0;
        try{
            do {
                switch (reader.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:  // process start of next element
                        QName curName = reader.getName();
                        insidePolicyReferenceAttr = NamespaceVersion.resolveAsToken(curName) == XmlToken.PolicyReference;
                        if (elementName.equals(curName)) {  // it is our element !
                            depth++;                        // we are then deeper
                        }
                        final StringBuffer xmlnsCode = new StringBuffer();    // take care about namespaces as well
                        final Set<String> tmpNsSet = new HashSet<String>();
                        if ((null == curName.getPrefix()) || ("".equals(curName.getPrefix()))) {           // no prefix
                            elementCode
                                    .append('<')                     // start tag
                                    .append(curName.getLocalPart());
                            xmlnsCode
                                    .append(" xmlns=\"")
                                    .append(curName.getNamespaceURI())
                                    .append('"');
                            
                        } else {                                    // prefix presented
                            elementCode
                                    .append('<')                     // start tag
                                    .append(curName.getPrefix())
                                    .append(':')
                                    .append(curName.getLocalPart());
                            xmlnsCode
                                    .append(" xmlns:")
                                    .append(curName.getPrefix())
                                    .append("=\"")
                                    .append(curName.getNamespaceURI())
                                    .append('"');
                            tmpNsSet.add(curName.getPrefix());
                        }
                        final int attrCount = reader.getAttributeCount();     // process element attributes
                        final StringBuffer attrCode = new StringBuffer();
                        for (int i=0; i < attrCount; i++) {
                            boolean uriAttrFlg = false;
                            if (insidePolicyReferenceAttr && "URI".equals(
                                    reader.getAttributeName(i).getLocalPart())) { // PolicyReference found
                                uriAttrFlg = true;
                                if (null == policyRec.unresolvedURIs) { // first such URI found
                                    policyRec.unresolvedURIs = new HashSet<String>(); // initialize URIs set
                                }
                                policyRec.unresolvedURIs.add(  // add the URI
                                        relativeToAbsoluteUrl(reader.getAttributeValue(i), baseUrl));
                            } // end-if PolicyReference attribute found
                            if ("xmlns".equals(reader.getAttributePrefix(i)) && tmpNsSet.contains(reader.getAttributeLocalName(i))) {
                                continue; // do not append already defined ns
                            }
                            if ((null == reader.getAttributePrefix(i)) || ("".equals(reader.getAttributePrefix(i)))) {  // no attribute prefix
                                attrCode
                                        .append(' ')
                                        .append(reader.getAttributeLocalName(i))
                                        .append("=\"")
                                        .append(uriAttrFlg ? relativeToAbsoluteUrl(reader.getAttributeValue(i), baseUrl) : reader.getAttributeValue(i))
                                        .append('"');
                            } else {                                        // prefix`presented
                                attrCode
                                        .append(' ')
                                        .append(reader.getAttributePrefix(i))
                                        .append(':')
                                        .append(reader.getAttributeLocalName(i))
                                        .append("=\"")
                                        .append(uriAttrFlg ? relativeToAbsoluteUrl(reader.getAttributeValue(i), baseUrl) : reader.getAttributeValue(i))
                                        .append('"');
                                if (!tmpNsSet.contains(reader.getAttributePrefix(i))) {
                                    xmlnsCode
                                            .append(" xmlns:")
                                            .append(reader.getAttributePrefix(i))
                                            .append("=\"")
                                            .append(reader.getAttributeNamespace(i))
                                            .append('"');
                                    tmpNsSet.add(reader.getAttributePrefix(i));
                                } // end if prefix already processed
                            }
                        } // end foreach attr
                        elementCode
                                .append(xmlnsCode)          // complete the start element tag
                                .append(attrCode)
                                .append('>');
                        break;
                        //case XMLStreamConstants.ATTRIBUTE:   Unreachable (I hope ;-)
                        //    break;
                        //case XMLStreamConstants.NAMESPACE:   Unreachable (I hope ;-)
                        //    break;
                    case XMLStreamConstants.END_ELEMENT:
                        curName = reader.getName();
                        if (elementName.equals(curName)) {  // it is our element !
                            depth--;                        // go up
                        }
                        elementCode
                                .append("</")                     // append appropriate XML code
                                .append("".equals(curName.getPrefix())?"":curName.getPrefix()+':')
                                .append(curName.getLocalPart())
                                .append('>');                        // complete the end element tag
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        elementCode.append(reader.getText());           // append text data
                        break;
                    case XMLStreamConstants.CDATA:
                        elementCode
                                .append("<![CDATA[")                // append CDATA delimiters
                                .append(reader.getText())
                                .append("]]>");
                        break;
                    case XMLStreamConstants.COMMENT:    // Ignore any comments
                        break;
                    case XMLStreamConstants.SPACE:      // Ignore spaces as well
                        break;
                }
                if (reader.hasNext() && depth>0) {
                    reader.next();
                }
            } while (XMLStreamConstants.END_DOCUMENT!=reader.getEventType() && depth>0);
            policyRec.policyModel = PolicyModelUnmarshaller.getXmlUnmarshaller().unmarshalModel(
                    new StringReader(elementCode.toString()));
            if (null != policyRec.policyModel.getPolicyId()) {
                policyRec.uri = baseUrl + "#" + policyRec.policyModel.getPolicyId();
            } else if (policyRec.policyModel.getPolicyName() != null) {
                policyRec.uri = policyRec.policyModel.getPolicyName();
            }
        } catch(Exception e) {
            throw LOGGER.logSevereException(new WebServiceException(PolicyMessages.WSP_1033_EXCEPTION_WHEN_READING_POLICY_ELEMENT(elementCode.toString()), e));
        }
        urlsRead.add(baseUrl);
        return policyRec;
    }    
}
