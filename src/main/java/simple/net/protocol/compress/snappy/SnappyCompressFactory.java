package simple.net.protocol.compress.snappy;

import org.xerial.snappy.Snappy;
import simple.net.protocol.compress.CompressFactory;
import simple.net.protocol.compress.CompressType;

import java.io.IOException;

public class SnappyCompressFactory implements CompressFactory {

    @Override
    public int getCompressType() {
        return CompressType.COMPRESS_SNAPPY;
    }

    @Override
    public byte[] compress(byte[] data) throws IOException {
        return Snappy.compress(data);
    }

    @Override
    public byte[] decompress(byte[] data) throws IOException {
        return Snappy.uncompress(data);
    }

    // 使用netty snappy 利用 netty pool bytebuf
//    @Override
//    public void compress(byte[] data, ByteBuf out) {
//        ByteBuf in = Unpooled.wrappedBuffer(data);
//        Snappy snappy = new Snappy();
//        snappy.encode(in, out, data.length);
//    }
//
//    @Override
//    public void decompress(ByteBuf in, int length, ByteBuf out) {
//        Snappy snappy = new Snappy();
//        snappy.decode(in, out);
//    }


}
