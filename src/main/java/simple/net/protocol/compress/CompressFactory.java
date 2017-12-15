package simple.net.protocol.compress;

import java.io.IOException;

public interface CompressFactory {

    int getCompressType();

    byte[] compress(byte[] data) throws IOException;

    byte[] decompress(byte[] data) throws IOException;

//    void compress(byte[] data, ByteBuf out);
//
//    void decompress(ByteBuf in, int length, ByteBuf out);
}
