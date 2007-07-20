package com.sun.xml.ws.client.sei;

import com.sun.xml.ws.model.ParameterImpl;

import javax.xml.ws.WebServiceException;

/**
 * {@link ValueSetterFactory} is used to create {@link ValueSetter}.
 *
 * @author Jitendra Kotamraju
 */
abstract class ValueSetterFactory {

    abstract ValueSetter get(ParameterImpl p);

    static final ValueSetterFactory SYNC = new ValueSetterFactory() {
        ValueSetter get(ParameterImpl p) {
            return ValueSetter.getSync(p);
        }
    };

    static final ValueSetterFactory NONE = new ValueSetterFactory() {
        ValueSetter get(ParameterImpl p) {
            throw new WebServiceException("This shouldn't happen. No response parameters.");
        }
    };

    static final ValueSetterFactory SINGLE = new ValueSetterFactory() {
        ValueSetter get(ParameterImpl p) {
            return ValueSetter.SINGLE_VALUE;
        }
    };

    static final class AsyncBeanValueSetterFactory extends ValueSetterFactory {
        private Class asyncBean;

        AsyncBeanValueSetterFactory(Class asyncBean) {
            this.asyncBean = asyncBean;
        }

        ValueSetter get(ParameterImpl p) {
            return new ValueSetter.AsyncBeanValueSetter(p, asyncBean);
        }
    }

}
