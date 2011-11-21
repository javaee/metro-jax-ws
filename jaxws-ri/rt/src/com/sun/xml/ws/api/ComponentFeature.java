package com.sun.xml.ws.api;

import javax.xml.ws.WebServiceFeature;

/**
 * Allows registration of a {@link Component} against the {@link ComponentRegistry} implementations
 * of the {@link Container}, {@link WSEndpoint}, {@link WSService}, or {@link Stub}.  The
 * registration is guaranteed to occur early in the initialization of these objects prior to tubeline creation
 * (applicable to endpoint and stub only).
 * <p>
 * Because the Container is shared among all Stubs created from a common WSService object, this feature must 
 * be passed during WSService initialization in order to register a Component against the client-side Container.
 * <p>
 * IllegalArgumentException will be thrown if the feature is used with an inappropriate target, e.g. stub target
 * used during WSEndpoint initialization.
 */
public class ComponentFeature extends WebServiceFeature {
    /**
     * Targets the object on which the Component will be registered
     * 
     */
    public static enum Target {
        CONTAINER, ENDPOINT, SERVICE, STUB
    }
        
    private final Component component;
    private final Target target;

    /**
     * Constructs ComponentFeature with indicated component and that is targeted at the Container.
     * @param component component
     */
    public ComponentFeature(Component component) {
        this(component, Target.CONTAINER);
    }
    
    /**
     * Constructs ComponentFeature with indicated component and target
     * @param component component
     * @param target target
     */
    public ComponentFeature(Component component, Target target) {
        this.enabled = true;
        this.component = component;
        this.target = target;
    }

    @Override
    public String getID() {
        return ComponentFeature.class.getName();
    }

    /**
     * Retrieves component
     * @return component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Retrieves target
     * @return target
     */
    public Target getTarget() {
        return target;
    }
}