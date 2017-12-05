package simple.net.handler;

import java.lang.reflect.Method;

public class MessageHandlerDesc {

    Class<?> handlerClass;

    Method method;

    Class<?>[] paramClass;

    public Class<?> getHandlerClass() {
        return handlerClass;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?>[] getParamClass() {
        return paramClass;
    }
}
