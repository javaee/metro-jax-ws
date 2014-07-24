package com.sun.xml.ws.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
//import org.apache.xmlbeans.SchemaType;

import com.sun.xml.ws.model.ReflectAnnotationReader;
import static com.sun.xml.ws.model.RuntimeModeler.erasure;

/**
 * JWSAnnotationReader extends the ReflectAnnotationReader to allow customizing the parameter names with dummy 
 * WebParam and WebResult.
 * 
 * @author shih-chang.chen@oracle.com
 */
@SuppressWarnings("unchecked")
public class JWSAnnotationReader extends ReflectAnnotationReader {    
    String defaultArgPrefix = "param";   
    String defaultResultName = "return";
    
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
        return anno;
    }

    public Annotation[][] getParameterAnnotations(final Method method) {
        return AccessController.doPrivileged(new PrivilegedAction<Annotation[][]>() {
           public Annotation[][] run() {
               Annotation[][] paramAnno = method.getParameterAnnotations();
               Class<?>[]     paramType = method.getParameterTypes();
               Type[]  genericParamType = method.getGenericParameterTypes();
               for(int pos = 0; pos < paramType.length; pos ++) {
                   Annotation[] anno = paramAnno[pos];
                   Class<?>     type = paramType[pos];
                   if(Holder.class.equals(type)) type = erasure(((ParameterizedType)genericParamType[pos]).getActualTypeArguments()[0]);
                   WebParam webParam = null;
                   int webParamPos = -1;
                   for(int i = 0; i < anno.length; i++) if (anno[i] instanceof WebParam){
                       webParam = (WebParam)anno[i];
                       webParamPos = i;
                   }
                   String pname = "param"+pos;
                   String ptns = "";
                   QName elemName = getGlobalElementName(type);
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
                    QName elemName = getGlobalElementName(m.getReturnType());
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
                return cls.getAnnotation(annType);
            }
        });
    }
    
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
    //This is the XMLBeans way ...
//    static QName getGlobalElementName(Class<?> cls) {
//        SchemaType schemaType = XMLBeansContext.type(cls);
//        return (schemaType != null && schemaType.isDocumentType()) ? schemaType.getDocumentElementName() : null;
//    }
    
    static QName getGlobalElementName(Class<?> cls) {
        XmlRootElement xre = (XmlRootElement) cls.getAnnotation(XmlRootElement.class);
        XmlType xt = (XmlType) cls.getAnnotation(XmlType.class);
        if (xt != null && xt.name() != null && !"".equals(xt.name())) return null;
        if (xre != null) {
            String lp = xre.name();
            String ns = xre.namespace();
            if (ns.equals("##default")) {
                XmlSchema xs = cls.getPackage().getAnnotation(XmlSchema.class);
                if (xs != null) ns = xs.namespace();
                else ns = "";
            }
            return new QName(ns, lp);
        }
      return null;
    }
}
