package simple.net.protocol.message;

public class MessageHead {

    // codec标识占用1字节
    public static final int CODEC_SIZE = 1;
    // msgId占用4字节
    public static final int MSGID_SIZE = 4;
    // compress标记占用1字节
    public static final int COMPRESS_SIZE = 1;
    // length占用4字节
    public static final int LENGTH_SIZE = 4;
    // 消息包前缀长度
    public static final int HEAD_LENGTH = CODEC_SIZE + MSGID_SIZE + COMPRESS_SIZE + LENGTH_SIZE;
}
