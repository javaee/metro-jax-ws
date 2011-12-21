package org.jvnet.ws;

import javax.xml.ws.WebServiceFeature;

public class EnvelopingFeature extends WebServiceFeature {
    
    private Enveloping.Style[] styles;
    
    public EnvelopingFeature(Enveloping.Style... s) {
        styles = s;
    }
    
    public Enveloping.Style[] getStyles() {
        return styles;
    }
    
    public String getID() {
        return EnvelopingFeature.class.getName();
    }
}
