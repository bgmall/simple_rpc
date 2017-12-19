package simple.net.protocol.message;

import simple.net.protocol.annotation.NetProtocol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private static class SingletonHolder {
        private static final MessageManager INSTANCE = new MessageManager();
    }

    private MessageManager() {
    }

    public static final MessageManager getInstance() {
        return MessageManager.SingletonHolder.INSTANCE;
    }

    private Map<Integer, Class<?>> msgIdToClass = new HashMap<>();

    private Map<Integer, Integer> msgIdToCodecType = new HashMap<>();

    private Map<Integer, Integer> msgIdToCompressType = new HashMap<>();

    private Map<Integer, Integer> msgIdToProtocolType = new HashMap<>();

    private Map<Integer, Integer> msgIdToCompressRequiredLength = new HashMap<>();

    public void register(Class<?> msgClass) {
        NetProtocol annotation = msgClass.getAnnotation(NetProtocol.class);
        if (annotation == null) {
            throw new IllegalArgumentException(msgClass + " don't have NetProtocol annotation");
        }

        int msgId = annotation.msgId();
        if (!HeartBeatMessage.class.isAssignableFrom(msgClass)) {
            if (annotation.msgId() <= 0) {
                throw new IllegalArgumentException("illegal message id");
            }
        }

        if (msgIdToClass.putIfAbsent(msgId, msgClass) != null) {
            throw new IllegalStateException(msgId + " already exist, duplicate msgId define");
        }

        msgIdToCodecType.putIfAbsent(msgId, annotation.codecType());
        msgIdToCompressType.putIfAbsent(msgId, annotation.compressType());
        msgIdToProtocolType.putIfAbsent(msgId, annotation.protocolType());
        msgIdToCompressRequiredLength.putIfAbsent(msgId, annotation.compressRequiredLength());
    }


    public Class<?> getMessageClass(int msgId) {
        return msgIdToClass.get(msgId);
    }

    public Collection<Class<?>> getMessageClasses() {
        return msgIdToClass.values();
    }

    public int getCompressType(int msgId) {
        return msgIdToCompressType.get(msgId);
    }

    public int getCodecType(int msgId) {
        return msgIdToCodecType.get(msgId);
    }

    public int getProtocolType(int msgId) {
        return msgIdToProtocolType.get(msgId);
    }

    public int getCompressRequiredLength(int msgId) {
        return msgIdToCompressRequiredLength.get(msgId);
    }
}
