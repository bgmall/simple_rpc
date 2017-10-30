package simple.rpc;

import io.netty.buffer.ByteBuf;
import simple.net.Message;

public class RpcMessage implements Message {

    public static final int HEAD_FLAG_LENGTH = 1;
    public static final int HEAD_UUID_LENGTH = 8;
    public static final int HEAD_ID_LENGTH = 4;
    public static final int HEAD_DATA_LENGTH = 4;
    public static final int HEAD_FIXED_LENGTH = HEAD_FLAG_LENGTH + HEAD_UUID_LENGTH + HEAD_ID_LENGTH + HEAD_DATA_LENGTH;

    private byte flag;
    // 唯一标识msg
    private long uuid;

    private int id;

    private byte[] data;

    @Override
    public int getId() {
        return id;
    }

    public static RpcMessage decode(ByteBuf byteBuf) {
        byte flag = byteBuf.readByte();
        long uuid = byteBuf.readLong();
        int id = byteBuf.readInt();
        int length = byteBuf.readInt();
        byte[] data = new byte[length];
        byteBuf.readBytes(data, 0, length);
        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.flag = flag;
        rpcMessage.uuid = uuid;
        rpcMessage.id = id;
        rpcMessage.data = data;
        return rpcMessage;
    }

    public static void encode(ByteBuf byteBuf, RpcMessage message) {
        byteBuf.writeByte(message.flag);
        byteBuf.writeLong(message.uuid);
        byteBuf.writeInt(message.id);
        byteBuf.writeInt(message.data.length);
        byteBuf.writeBytes(message.data);
    }
}
