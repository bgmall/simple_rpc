package simple.net.handler;

import java.lang.reflect.Method;

public class MessageHandlerDesc {

    Object handler;

    Method method;

    Class<?>[] paramClass;

    public Object getHandler() {
        return handler;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?>[] getParamClass() {
        return paramClass;
    }
}
