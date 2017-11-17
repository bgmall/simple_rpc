package simple.net.protocol;

public interface ProtocolFactory {

    int getProtocolCode();

    void encode(Object body);

    <T> T decode(int msgId, byte[] data);
}
