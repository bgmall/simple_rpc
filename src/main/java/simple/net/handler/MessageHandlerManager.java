package simple.net.handler;

import simple.net.handler.annotation.NetMessageInvoke;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MessageHandlerManager {

    private Map<Integer, MessageHandlerDesc> msgIdToHandler = new HashMap<>();

    public void register(Object handler) {
        Class<?> handlerClass = handler.getClass();
        Method[] declaredMethods = handlerClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            NetMessageInvoke annotation = method.getAnnotation(NetMessageInvoke.class);
            if (annotation != null) {
                int msgId = annotation.msgId();
                if (msgId <= 0) {
                    throw new RuntimeException();
                }

                MessageHandlerDesc messageHandler = new MessageHandlerDesc();
                messageHandler.handler = handler;
                messageHandler.method = method;
                messageHandler.paramClass = method.getParameterTypes();
                if (msgIdToHandler.putIfAbsent(msgId, messageHandler) != null) {
                    throw new RuntimeException();
                }
            }
        }
    }

    public MessageHandlerDesc getMessageHandler(int msgId) {
        return msgIdToHandler.get(msgId);
    }

}
