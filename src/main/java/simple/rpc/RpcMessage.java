package simple.rpc;

import io.netty.buffer.ByteBuf;
import simple.rpc.annotation.RpcProtocol;

public class RpcMessage {

    public static final int MESSAGE_FLAG_LENGTH = 1;
    public static final int MESSAGE_UUID_LENGTH = 8;
    public static final int MESSAGE_ID_LENGTH = 4;
    public static final int MESSAGE_DATA_LENGTH = 4;
    public static final int MESSAGE_FIXED_LENGTH = MESSAGE_FLAG_LENGTH + MESSAGE_UUID_LENGTH + MESSAGE_ID_LENGTH + MESSAGE_DATA_LENGTH;

    private byte flag;
    // rpc correlationId or the playerId of forward msg
    private long id;

    private byte[] data;

    public long getId() {
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
        rpcMessage.id = uuid;
        rpcMessage.data = data;
        return rpcMessage;
    }

    public static void encode(ByteBuf byteBuf, RpcMessage message) {
        byteBuf.writeByte(message.flag);
        byteBuf.writeLong(message.id);
        byteBuf.writeInt(message.getMsgId());
        byteBuf.writeInt(message.data.length);
        byteBuf.writeBytes(message.data);
    }

    public int getMsgId() {
        RpcProtocol annotation = this.getClass().getAnnotation(RpcProtocol.class);
        if (annotation != null) {
            return annotation.msgId();
        }
        return 0;
    }
}
