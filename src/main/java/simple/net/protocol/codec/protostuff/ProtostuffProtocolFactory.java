package simple.net.protocol.codec.protostuff;

import simple.net.protocol.MessageManager;
import simple.net.protocol.NetMessage;
import simple.net.protocol.ProtocolFactory;

import java.util.Collection;

public class ProtostuffProtocolFactory implements ProtocolFactory {

    public static final byte PROTOCOL_CODE_ID = 1;

    private MessageManager messageManager;

    public ProtostuffProtocolFactory(MessageManager messageManager) {
        this.messageManager = messageManager;
        preProcess(messageManager);
    }

    @Override
    public byte getProtocolCode() {
        return PROTOCOL_CODE_ID;
    }

    @Override
    public byte[] encode(NetMessage msg) {
        return ProtostuffUtil.serialize(msg);
    }

    @Override
    public Object decode(int msgId, byte[] data) {
        Class<?> messageClass = messageManager.getMessageClass(msgId);
        return messageClass == null ? null : ProtostuffUtil.deserialize(data, messageClass);
    }

    private void preProcess(MessageManager messageManager) {
        Collection<Class<?>> messageClasses = messageManager.getMessageClasses();
        for (Class<?> clazz : messageClasses) {
            ProtostuffUtil.getSchema(clazz);
        }
    }
}
