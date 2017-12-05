package simple.net.protocol;

import simple.net.protocol.annotation.NetProtocol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private Map<Integer, Class<?>> msgIdToClass = new HashMap<>();

    public void register(Class<?> msgClass) {
        NetProtocol annotation = msgClass.getAnnotation(NetProtocol.class);
        if (annotation == null) {
            throw new IllegalArgumentException(msgClass + " don't have NetProtocol annotation");
        }

        int msgId = annotation.msgId();

        if (!NetMessage.class.isAssignableFrom(msgClass)) {
            throw new IllegalArgumentException("msgClass invalid, must be NetMessage");
        }

        if (msgIdToClass.putIfAbsent(msgId, msgClass) != null) {
            throw new IllegalStateException(msgId + " already exist, duplicate msgId define");
        }
    }

    public Class<?> getMessageClass(int msgId) {
        return msgIdToClass.get(msgId);
    }

    public Collection<Class<?>> getMessageClasses() {
        return msgIdToClass.values();
    }
}
