package com.sun.xml.ws.db.xmlbeans;

import static com.sun.xml.ws.db.xmlbeans.XMLBeansContext.globalElementName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;
import javax.xml.ws.WebFault;

import com.sun.xml.ws.api.databinding.MetadataReader;
import com.sun.xml.ws.db.xmlbeans.FaultInfo.PropInfo;

/**
 * JWSAnnotationReader allows customizing the names by dummy annotations.
 * 
 * @author shih-chang.chen@oracle.com
 */
@SuppressWarnings("unchecked")
public class JWSAnnotationReader  implements MetadataReader {
    
    String defaultArgPrefix = "param";   
    String defaultResultName = "return";
    String wsdlNamespace;
    
    public Annotation[] getAnnotations(Method m) {
        Annotation[] anno =  m.getAnnotations();
        int webResultPos = -1;
        for(int i = 0; i < anno.length; i++) if (anno[i] instanceof WebParam){
            webResultPos = i;
        }
        WebResult webResult = getAnnotation(WebResult.class, m);
        if (webResultPos == -1) {
            anno = append(anno, webResult);
        } else {
            anno[webResultPos] = webResult;
        }
        if (m.getReturnType().isArray()) {
            //TODO XmlElementWrapper is NOT allowed on parameter
            anno = append(anno, new DummyXmlElementWrapper(webResult.name(), webResult.targetNamespace()));
        }
        return anno;
    }

    public Annotation[][] getParameterAnnotations(final Method method) {
        return AccessController.doPrivileged(new PrivilegedAction<Annotation[][]>() {
           public Annotation[][] run() {
               Annotation[][] paramAnno = method.getParameterAnnotations();
               Class<?>[]     paramType = method.getParameterTypes();
               for(int pos = 0; pos < paramType.length; pos ++) {
                   Annotation[] anno = paramAnno[pos];
                   Class<?>     type = paramType[pos];
                   WebParam webParam = null;
                   int webParamPos = -1;
                   for(int i = 0; i < anno.length; i++) if (anno[i] instanceof WebParam){
                       webParam = (WebParam)anno[i];
                       webParamPos = i;
                   }
                   String pname = "param"+pos;
                   String ptns = "";
                   String arrayWrapperName = pname;
                   QName elemName = globalElementName(type);
                   if (elemName != null) { 
                       ptns = elemName.getNamespaceURI();
                       pname = elemName.getLocalPart();
                   } 
                   webParam = new DummyWebParam(webParam, pname, ptns);
                   if (webParamPos == -1) {
                       paramAnno[pos] = append(anno, webParam);
                   } else {
                       anno[webParamPos] = webParam;
                   }
                   if (type.isArray()) {
                       //TODO XmlElementWrapper is NOT allowed on parameter
                       paramAnno[pos] = append(paramAnno[pos], new DummyXmlElementWrapper(arrayWrapperName, ptns));
                   }
               }
               return paramAnno;
           }
        });
    }


    static Annotation[] append(Annotation[] anno, Annotation oneMore) {
        Annotation[] newArray = new Annotation[anno.length+1];
        System.arraycopy(anno, 0, newArray, 0, anno.length);
        newArray[anno.length] = oneMore;
        return newArray;
    }
    
    public <A extends Annotation> A getAnnotation(final Class<A> annType, final Method m) {
        return AccessController.doPrivileged(new PrivilegedAction<A>() {
            public A run() {
                Annotation anno = m.getAnnotation(annType);
                if (WebResult.class.equals(annType)) {
                    String pname = defaultResultName;
                    String ptns = "";
                    QName elemName = globalElementName(m.getReturnType());
                    if (elemName != null) {
                        ptns = elemName.getNamespaceURI();
                        pname = elemName.getLocalPart();
                    }
                    anno = new DummyWebResult((WebResult)anno, pname, ptns);
                }
                return (A) anno;
            }
        });
    }
    
    public <A extends Annotation> A getAnnotation(final Class<A> annType, final Class<?> cls) {
        return AccessController.doPrivileged(new PrivilegedAction<A>() {
            public A run() {
                if (Exception.class.equals(cls) && XmlTransient.class.equals(annType)) {
                    return (A) new XmlTransient() {
                        public Class<? extends Annotation> annotationType() { return XmlTransient.class; }                        
                    };
                }
                if (Exception.class.isAssignableFrom(cls) &&  WebFault.class.equals(annType)) 
                    return (A) new DummyWebFault(cls.getAnnotation(WebFault.class), cls, wsdlNamespace);
                A ann = cls.getAnnotation(annType);
                if (ann instanceof WebService) wsdlNamespace = ((WebService)ann).targetNamespace();
                return ann;
            }
        });
    }
    
    public Annotation[] getAnnotations(final Class<?> cls) {
        return AccessController.doPrivileged(new PrivilegedAction<Annotation[]>() {
            public Annotation[] run() {
                return cls.getAnnotations();
            }
        });
    }

    public void getProperties(final Map<String, Object> prop, final Class<?> cls){}
    
    public void getProperties(final Map<String, Object> prop, final Method method){}  
    
    public void getProperties(final Map<String, Object> prop, final Method method, int pos){}
    
    static class DummyWebParam implements WebParam {        
        private WebParam delegate;     
        private String   defaultName;             
        private String   defaultNS;                

        public DummyWebParam(WebParam delegate, String defaultName, String defaultNS) {
            this.delegate = delegate;
            this.defaultName = defaultName;
            this.defaultNS = defaultNS;
        }
        @Override
        public Class<? extends Annotation> annotationType() {
            return (delegate != null)? delegate.annotationType() : WebParam.class;
        }
        @Override
        public boolean header() {
            return (delegate != null)? delegate.header() : false;
        }
        @Override
        public Mode mode() {
            return (delegate != null)? delegate.mode() : javax.jws.WebParam.Mode.IN;
        }
        @Override
        public String name() {
            String name = (delegate != null)? delegate.name() : "";
            if (name == null || "".equals(name)) return defaultName;
            return name;
        }
        @Override
        public String partName() {
            return (delegate != null)? delegate.partName() :  "";
        }
        @Override
        public String targetNamespace() {
            String ns = (delegate != null)? delegate.targetNamespace() :  "";
            if (ns == null || "".equals(ns)) return defaultNS;
            return ns;
        }        
    }
    
    static class DummyWebResult implements WebResult {        
        private WebResult delegate;     
        private String   defaultName;             
        private String   defaultNS;                

        public DummyWebResult(WebResult delegate, String defaultName, String defaultNS) {
            this.delegate = delegate;
            this.defaultName = defaultName;
            this.defaultNS = defaultNS;
        }
        @Override
        public Class<? extends Annotation> annotationType() {
            return (delegate != null)? delegate.annotationType() : WebResult.class;
        }
        @Override
        public boolean header() {
            return (delegate != null)? delegate.header() : false;
        }
        @Override
        public String name() {
            String name = (delegate != null)? delegate.name() : "";
            if (name == null || "".equals(name)) return defaultName;
            return name;
        }
        @Override
        public String partName() {
            return (delegate != null)? delegate.partName() :  "";
        }
        @Override
        public String targetNamespace() {
            String ns = (delegate != null)? delegate.targetNamespace() :  "";
            if (ns == null || "".equals(ns)) return defaultNS;
            return ns;
        }        
    }
    
    static class DummyXmlElementWrapper implements XmlElementWrapper {  
        private String name;             
        private String ns;    
        public DummyXmlElementWrapper(String name, String ns) {
            this.name = name;
            this.ns = ns;
        }
        @Override
        public Class<? extends Annotation> annotationType() {
            return XmlElementWrapper.class;
        }
        @Override
        public String name() {
            return name;
        }
        @Override
        public String namespace() {
            return ns;
        }
        @Override
        public boolean nillable() {
            return false;
        }
        @Override
        public boolean required() {
            return true;
        }        
    }

    static class DummyWebFault implements WebFault {     
        private WebFault delegate;     
        private Class cls;
        private PropInfo singleProp;
        public DummyWebFault(WebFault ann, Class c, String portTypeNS) {
            delegate = ann;
            cls = c;
            List<PropInfo> props = FaultInfo.model(cls, portTypeNS);
            singleProp = (props.size() ==1)? props.get(0):null;
        }
        @Override
        public Class<? extends Annotation> annotationType() {
            return (delegate != null)? delegate.annotationType() : WebFault.class;
        }
        @Override
        public String faultBean() {
            return (delegate != null)? delegate.faultBean() : "";
        }
        @Override
        public String messageName() {
            return (delegate != null)? delegate.messageName() : cls.getSimpleName();
        }
        @Override
        public String name() {
            return (delegate != null)? delegate.name() : (singleProp != null)? singleProp.typeInfo.tagName.getLocalPart(): cls.getSimpleName();
        }
        @Override
        public String targetNamespace() {
            return (delegate != null)? delegate.targetNamespace() : (singleProp != null)? singleProp.typeInfo.tagName.getNamespaceURI(): "java:"+cls.getPackage().getName();
        }       
    }
}
