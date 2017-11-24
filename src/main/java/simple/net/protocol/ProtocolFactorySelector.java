package simple.net.protocol;

public interface ProtocolFactorySelector {

    ProtocolFactory select(byte protocolCode);
}
