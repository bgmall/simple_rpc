package simple.net.protocol;

import simple.net.protocol.annotation.NetProtocol;

public interface NetMessage {

    default int getMsgId() {
        NetProtocol annotation = this.getClass().getAnnotation(NetProtocol.class);
        if (annotation != null) {
            return annotation.msgId();
        }
        return 0;
    }

    default byte getProtocolCode() {
        NetProtocol annotation = this.getClass().getAnnotation(NetProtocol.class);
        if (annotation != null) {
            return annotation.codec();
        }
        return 0;
    }
}
