package simple.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import simple.rpc.RpcChannelIdleHandler;
import simple.rpc.RpcMessageDecoder;
import simple.rpc.RpcMessageEncoder;
import simple.rpc.RpcServerHandler;

public class NetServerChannelInitializer extends ChannelInitializer<Channel> {

    private static final String CHANNEL_STATE_AWARE_HANDLER = "channel_state_aware_handler";
    private static final String CHANNEL_IDLE_HANDLER = "channle_idle_handler";
    private static final String FRAME_DECODER = "frame_decoder";
    private static final String MESSAGE_DECODER = "message_decoder";
    private static final String MESSAGE_ENCODER = "message_encoder";
    private static final String MESSAGE_HANDLER = "message_handler";

    private NetServer netServer;

    public NetServerChannelInitializer(NetServer netServer) {
        this.netServer = netServer;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        int idleTimeoutSeconds = netServer.getServerOptions().getIdleTimeoutSeconds();
        pipeline.addLast(CHANNEL_STATE_AWARE_HANDLER, new IdleStateHandler(idleTimeoutSeconds, idleTimeoutSeconds, idleTimeoutSeconds));
        pipeline.addLast(CHANNEL_IDLE_HANDLER, new RpcChannelIdleHandler());

        int maxFrameLength = netServer.getServerOptions().getMaxFrameLength();
        pipeline.addLast(FRAME_DECODER, new LengthFieldBasedFrameDecoder(maxFrameLength, 4, 4));
        pipeline.addLast(MESSAGE_DECODER, new RpcMessageDecoder(maxFrameLength));
        pipeline.addLast(MESSAGE_HANDLER, new RpcServerHandler());
        pipeline.addFirst(MESSAGE_ENCODER, new RpcMessageEncoder());
    }
}
