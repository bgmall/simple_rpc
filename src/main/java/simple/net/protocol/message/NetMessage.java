package simple.net.protocol.message;

import simple.net.protocol.annotation.NetProtocol;

public interface NetMessage {

    default int getMsgId() {
        NetProtocol annotation = this.getClass().getAnnotation(NetProtocol.class);
        if (annotation != null) {
            return annotation.msgId();
        }
        return 0;
    }

    default int getCodeType() {
        NetProtocol annotation = this.getClass().getAnnotation(NetProtocol.class);
        if (annotation != null) {
            return annotation.codecType();
        }
        return 0;
    }

    default int getCompressType() {
        NetProtocol annotation = this.getClass().getAnnotation(NetProtocol.class);
        if (annotation != null) {
            return annotation.compressType();
        }
        return 0;
    }

    default int getProtocolType() {
        NetProtocol annotation = this.getClass().getAnnotation(NetProtocol.class);
        if (annotation != null) {
            return annotation.protocolType();
        }
        return 0;
    }
}
