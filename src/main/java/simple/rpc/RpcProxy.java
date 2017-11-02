package simple.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RpcProxy implements InvocationHandler {

    private static final Map<Class<?>, Object> interfaceClassToProxyInstance = new HashMap<>();

    public synchronized <T> T proxyInstance(Class<?> interfaceClass) {
        Object instance = interfaceClassToProxyInstance.get(interfaceClass);
        if (instance == null) {
            return null;
        }

        return (T) instance;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
