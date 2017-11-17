package simple.net.protocol;

import simple.net.protocol.annotation.Protocol;

public abstract class AnnotationMessage implements Message {

    @Override
    public int getMsgId() {
        Protocol annotation = this.getClass().getAnnotation(Protocol.class);
        if (annotation != null) {
            return annotation.msgId();
        }
        return 0;
    }

    @Override
    public byte getProtocolCode() {
        Protocol annotation = this.getClass().getAnnotation(Protocol.class);
        if (annotation != null) {
            return annotation.codec();
        }
        return 0;
    }
}
