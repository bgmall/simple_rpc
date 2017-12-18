package simple.net.protocol.compress.zlib;

import simple.net.protocol.compress.CompressFactory;
import simple.net.protocol.compress.CompressType;
import simple.util.ZlibUtil;

import java.io.IOException;

public class ZlibCompressFactory implements CompressFactory {
    @Override
    public int getCompressType() {
        return CompressType.COMPRESS_ZLIB;
    }

    @Override
    public byte[] compress(byte[] data) throws IOException {
        return ZlibUtil.compress(data);
    }

    @Override
    public byte[] decompress(byte[] data) throws IOException {
        return ZlibUtil.decompress(data);
    }
}
