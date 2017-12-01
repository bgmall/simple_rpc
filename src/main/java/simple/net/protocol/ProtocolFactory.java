package simple.net.protocol;

public interface ProtocolFactory {

    byte getProtocolCode();

    byte[] encode(NetMessage msg);

    <T> T decode(int msgId, byte[] data);
}
