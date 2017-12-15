package simple.net.protocol.codec;

import simple.net.protocol.message.NetMessage;

public interface CodecFactory {

    int getCodecType();

    byte[] encode(NetMessage msg);

    Object decode(int msgId, byte[] data);
}
