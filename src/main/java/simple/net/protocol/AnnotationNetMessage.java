package simple.net.protocol;

import simple.net.protocol.annotation.NetProtocol;

public abstract class AnnotationNetMessage implements NetMessage {

    @Override
    public int getMsgId() {
        NetProtocol annotation = this.getClass().getAnnotation(NetProtocol.class);
        if (annotation != null) {
            return annotation.msgId();
        }
        return 0;
    }

    @Override
    public byte getProtocolCode() {
        NetProtocol annotation = this.getClass().getAnnotation(NetProtocol.class);
        if (annotation != null) {
            return annotation.codec();
        }
        return 0;
    }
}
