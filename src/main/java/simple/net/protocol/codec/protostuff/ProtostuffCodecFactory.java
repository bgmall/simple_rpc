package simple.net.protocol.codec.protostuff;

import simple.net.protocol.codec.CodecFactory;
import simple.net.protocol.codec.CodecType;
import simple.net.protocol.message.MessageManager;
import simple.net.protocol.message.NetMessage;

import java.util.Collection;

public class ProtostuffCodecFactory implements CodecFactory {

    public ProtostuffCodecFactory() {
        preProcess();
    }

    @Override
    public int getCodecType() {
        return CodecType.PROTOSTUFF;
    }

    @Override
    public byte[] encode(NetMessage msg) {
        return ProtostuffUtil.serialize(msg);
    }

    @Override
    public Object decode(int msgId, byte[] data) {
        Class<?> messageClass = MessageManager.getInstance().getMessageClass(msgId);
        return messageClass == null ? null : ProtostuffUtil.deserialize(data, messageClass);
    }

    private void preProcess() {
        Collection<Class<?>> messageClasses = MessageManager.getInstance().getMessageClasses();
        for (Class<?> clazz : messageClasses) {
            ProtostuffUtil.getSchema(clazz);
        }
    }
}
