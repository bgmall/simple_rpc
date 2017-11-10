package simple.rpc;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

public class RpcClientChannelInitializer extends ChannelInitializer<Channel> {

    private static final String CHANNEL_STATE_AWARE_HANDLER = "channel_state_aware_handler";
    private static final String CHANNEL_IDLE_HANDLER = "channle_idle_handler";
    private static final String FRAME_DECODER = "frame_decoder";
    private static final String MESSAGE_DECODER = "message_decoder";
    private static final String MESSAGE_ENCODER = "message_encoder";
    private static final String MESSAGE_HANDLER = "message_handler";

    private RpcClient rpcClient;

    public RpcClientChannelInitializer(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline channelPipe = ch.pipeline();

        int idleTimeoutSeconds = rpcClient.getRpcClientOptions().getIdleTimeoutSeconds();
        channelPipe.addFirst(CHANNEL_STATE_AWARE_HANDLER, new IdleStateHandler(idleTimeoutSeconds, idleTimeoutSeconds, idleTimeoutSeconds));
        channelPipe.addFirst(CHANNEL_IDLE_HANDLER, new RpcChannelIdleHandler());
        channelPipe.addFirst(MESSAGE_ENCODER, new RpcMessageEncoder());

        int maxFrameLength = rpcClient.getRpcClientOptions().getMaxFrameLength();
        channelPipe.addLast(FRAME_DECODER, new LengthFieldBasedFrameDecoder(maxFrameLength, 4, 4));
        channelPipe.addLast(MESSAGE_DECODER, new RpcMessageDecoder(maxFrameLength));
        channelPipe.addLast(MESSAGE_HANDLER, new RpcClientHandler());
    }
}
