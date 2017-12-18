package simple.net.protocol.message;

/**
 * MessageHead由两部分组成[MSGID] + [LENGTH]
 */
public class MessageHead {

    // msgId占用4字节
    public static final int MSGID_SIZE = 4;
    // length占用4字节
    public static final int LENGTH_SIZE = 4;
    // 消息包前缀长度
    public static final int HEAD_LENGTH = MSGID_SIZE + LENGTH_SIZE;
}
