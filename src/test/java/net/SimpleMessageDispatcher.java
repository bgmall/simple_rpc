package net;

import org.springframework.stereotype.Component;
import simple.net.handler.MessageDispatcher;
import simple.net.handler.MessageHandlerDesc;
import simple.net.protocol.NetMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component
public class SimpleMessageDispatcher implements MessageDispatcher {
    @Override
    public void dispatch(NetMessage message, MessageHandlerDesc messageHandlerDesc) {
        Method method = messageHandlerDesc.getMethod();
        try {
            Object[] objects = convertParams(messageHandlerDesc, message);
            method.invoke(messageHandlerDesc.getHandler(), objects);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Object[] convertParams(MessageHandlerDesc messageHandlerDesc, NetMessage message) {
        Class<?>[] paramClass = messageHandlerDesc.getParamClass();
        Object[] params = new Object[paramClass.length];
        for (int i = 0; i < params.length; i++) {
            Class<?> clazz = paramClass[i];
            if (clazz == null) {
                continue;
            }
            if (NetMessage.class.isAssignableFrom(clazz)) {
                params[i] = message;
            }
        }
        return params;
    }
}
