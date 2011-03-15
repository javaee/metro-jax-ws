package com.sun.xml.ws;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import javax.jws.WebService;

import com.sun.xml.ws.api.databinding.MetadataReader;

public class DummyAnnotations implements MetadataReader {
	static class DummyAnnotation implements InvocationHandler {
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Class<?> type = method.getReturnType();
			return String.class.equals(type)? "" : null;
		}		
	}
	public Annotation[] getAnnotations(Method m) {
		return new Annotation[0];
	}
	public Annotation[][] getParameterAnnotations(Method method) {
		return new Annotation[method.getParameterTypes().length][0];
	}
	public <A extends Annotation> A getAnnotation(Class<A> annType, Method m) {
		return null;
	}
	public <A extends Annotation> A getAnnotation(Class<A> annType, Class<?> cls) {
		if (Object.class.equals(cls)) return null;
		if (WebService.class.equals(annType)) {
			Class[] intf = { annType };
			Object dummy = Proxy.newProxyInstance(annType.getClassLoader(), intf, new DummyAnnotation());
			return annType.cast(dummy);
		}
		return null;
	}
	public Annotation[] getAnnotations(Class<?> c) {
		return new Annotation[0];
	}
    public void getProperties(final Map<String, Object> prop, final Class<?> cls){}	    
    public void getProperties(final Map<String, Object> prop, final Method method){} 	    
    public void getProperties(final Map<String, Object> prop, final Method method, int pos){}		
}