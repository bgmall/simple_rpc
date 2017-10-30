package simple.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class NettyRpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public NettyRpcMessageDecoder(int maxFrameLength) {
        super(maxFrameLength, RpcMessage.HEAD_FIXED_LENGTH  - RpcMessage.HEAD_DATA_LENGTH, RpcMessage.HEAD_DATA_LENGTH);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (frame == null) {
                return null;
            }
            return RpcMessage.decode(frame);
        } finally {
            if (frame != null) {
                frame.release();
            }
        }
    }
}
