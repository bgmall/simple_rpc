package simple.net.protocol;

public interface ProtocolFactorySelector {

    ProtocolFactory select(int protocolCode);
}
