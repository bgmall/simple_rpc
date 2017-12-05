package simple.net.protocol;

public interface ProtocolFactory {

    byte getProtocolCode();

    byte[] encode(NetMessage msg);

    Object decode(int msgId, byte[] data);
}
