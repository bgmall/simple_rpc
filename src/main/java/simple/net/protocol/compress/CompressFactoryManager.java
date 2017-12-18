package simple.net.protocol.compress;

import simple.net.protocol.compress.snappy.SnappyCompressFactory;
import simple.net.protocol.compress.zlib.ZlibCompressFactory;

import java.util.HashMap;
import java.util.Map;

public class CompressFactoryManager {

    private static class SingletonHolder {
        private static final CompressFactoryManager INSTANCE = new CompressFactoryManager();
    }

    private CompressFactoryManager() {
        register(new SnappyCompressFactory());
        register(new ZlibCompressFactory());
    }

    public static final CompressFactoryManager getInstance() {
        return CompressFactoryManager.SingletonHolder.INSTANCE;
    }

    private Map<Integer, CompressFactory> compressTypeToFactory = new HashMap<>();

    public void register(CompressFactory compressFactory) {
        if (compressTypeToFactory.putIfAbsent(compressFactory.getCompressType(), compressFactory) != null) {
            throw new IllegalStateException("The compress type already register");
        }
    }

    public CompressFactory select(int compressType) {
        return compressTypeToFactory.get(compressType);
    }
}
