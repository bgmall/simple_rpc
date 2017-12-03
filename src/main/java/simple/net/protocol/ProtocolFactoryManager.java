package simple.net.protocol;

import simple.net.protocol.ProtocolFactory;
import simple.net.protocol.ProtocolFactorySelector;

import java.util.HashMap;
import java.util.Map;

public class ProtocolFactoryManager implements ProtocolFactorySelector {

    private Map<Byte, ProtocolFactory> protocolCodeToFactory = new HashMap<>();

    public void register(ProtocolFactory protocolFactory) {
        if (protocolCodeToFactory.containsKey(protocolFactory.getProtocolCode())) {
            throw new IllegalStateException("The protocol code already register");
        }

        protocolCodeToFactory.put(protocolFactory.getProtocolCode(), protocolFactory);
    }

    @Override
    public ProtocolFactory select(byte protocolCode) {
        return protocolCodeToFactory.get(protocolCode);
    }
}
