package simple.net.protocol;

import java.util.HashMap;
import java.util.Map;

public class ProtocolFactoryManager {

    private static class SingletonHolder {
        private static final ProtocolFactoryManager INSTANCE = new ProtocolFactoryManager();
    }

    private ProtocolFactoryManager() {
        register(new NetProtocolFactory());
    }

    public static final ProtocolFactoryManager getInstance() {
        return ProtocolFactoryManager.SingletonHolder.INSTANCE;
    }

    private Map<Integer, ProtocolFactory> protocolCodeTypeToFactory = new HashMap<>();

    public void register(ProtocolFactory protocolFactory) {
        if (protocolCodeTypeToFactory.putIfAbsent(protocolFactory.getProtocolType(), protocolFactory) != null) {
            throw new IllegalStateException("The protocol code type already register");
        }
    }

    public ProtocolFactory select(int protocolType) {
        return protocolCodeTypeToFactory.get(protocolType);
    }

    public boolean isEmpty() {
        return protocolCodeTypeToFactory.isEmpty();
    }
}
