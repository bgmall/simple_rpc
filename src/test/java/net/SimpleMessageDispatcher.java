package net;

import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.net.handler.MessageDispatcher;
import simple.net.handler.MessageHandlerDesc;
import simple.net.protocol.message.NetMessage;

import java.lang.reflect.Method;

public class SimpleMessageDispatcher implements MessageDispatcher {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SimpleMessageDispatcher.class);

    @Override
    public void messageReceived(Channel channel, NetMessage message, MessageHandlerDesc messageHandlerDesc) {
        Method method = messageHandlerDesc.getMethod();
        try {
            Object[] objects = convertParams(channel, messageHandlerDesc, message);
            method.invoke(messageHandlerDesc.getHandler(), objects);
        } catch (Throwable e) {
            logger.error(e);
        }
    }

    private Object[] convertParams(Channel channel, MessageHandlerDesc messageHandlerDesc, NetMessage message) {
        Class<?>[] paramClass = messageHandlerDesc.getParamClass();
        Object[] params = new Object[paramClass == null ? 0 : paramClass.length];
        for (int i = 0; i < params.length; i++) {
            Class<?> clazz = paramClass[i];
            if (clazz == null) {
                continue;
            }
            if (NetMessage.class.isAssignableFrom(clazz)) {
                params[i] = message;
            } else if (Channel.class.isAssignableFrom(clazz)) {
                params[i] = channel;
            }
        }
        return params;
    }

    @Override
    public void channelOpened(Channel channel) throws Exception {

    }

    @Override
    public void channelClosed(Channel channel) throws Exception {

    }

    @Override
    public void exceptionCaught(Channel channel, Throwable cause) throws Exception {
    }

}
