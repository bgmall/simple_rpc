package simple.net.protocol.codec;

import java.util.HashMap;
import java.util.Map;

public class CodecFactoryManager {

    private static class SingletonHolder {
        private static final CodecFactoryManager INSTANCE = new CodecFactoryManager();
    }

    private CodecFactoryManager() {

    }

    public static final CodecFactoryManager getInstance() {
        return CodecFactoryManager.SingletonHolder.INSTANCE;
    }

    private Map<Integer, CodecFactory> codeTypeToFactory = new HashMap<>();

    public void register(CodecFactory codecFactory) {
        if (codeTypeToFactory.putIfAbsent(codecFactory.getCodecType(), codecFactory) != null) {
            throw new IllegalStateException("The codec type already register");
        }
    }

    public CodecFactory select(int codecType) {
        return codeTypeToFactory.get(codecType);
    }

    public boolean isEmpty() {
        return codeTypeToFactory.isEmpty();
    }
}
