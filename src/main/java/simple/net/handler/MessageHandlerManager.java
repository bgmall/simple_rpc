package simple.net.handler;

import simple.net.handler.annotation.NetMessageInvoke;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MessageHandlerManager {

    private Map<Integer, MessageHandler> msgIdToHandler = new HashMap<>();

    public void register(Class<?> handlerClass) {
        Method[] declaredMethods = handlerClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            NetMessageInvoke annotation = method.getAnnotation(NetMessageInvoke.class);
            if (annotation != null) {
                int msgId = annotation.msgId();
                if (msgId <= 0) {
                    throw new RuntimeException();
                }

                MessageHandler messageHandler = new MessageHandler();
                messageHandler.handlerClass = handlerClass;
                messageHandler.method = method;
                messageHandler.paramClass = method.getParameterTypes();
                if (msgIdToHandler.putIfAbsent(msgId, messageHandler) != null) {
                    throw new RuntimeException();
                }
            }
        }
    }

    public MessageHandler getMessageHandler(int msgId) {
        return msgIdToHandler.get(msgId);
    }

    public static class MessageHandler {

        public Class<?> handlerClass;

        public Method method;

        public Class<?>[] paramClass;
    }
}
