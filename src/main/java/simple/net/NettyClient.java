package simple.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NettyClient<M extends Message> extends Bootstrap implements NettyService {

    private static final String CHANNEL_STATE_AWARE_HANDLER = "channel_state_aware_handler";
    private static final String CHANNEL_IDLE_HANDLER = "channle_idle_handler";
    private static final String FRAME_DECODER = "frame_decoder";
    private static final String MESSAGE_DECODER = "message_decoder";
    private static final String MESSAGE_ENCODER = "message_encoder";
    private static final String MESSAGE_HANDLER = "message_handler";

    private static Timer timer = new HashedWheelTimer(Executors.defaultThreadFactory(), 100L, TimeUnit.MILLISECONDS, 2048);

    private ByteToMessageDecoder decoder;
    private MessageToByteEncoder<M> encoder;
    private SimpleChannelInboundHandler<M> messageHandler;
    private ChannelDuplexHandler channelIdleHandler;
    private EventLoopGroup workerGroup;
    private int idleTimeoutSeconds;
    private int maxFrameLength;

    public NettyClient() {
    }

    public NettyClient(final NettyClientOptions clientOptions) {
        Class<? extends Channel> channelClass;
        if ("Linux".equals(System.getProperty("os.name"))) {
            // required lib : netty-transport-native-epoll
            workerGroup = new EpollEventLoopGroup(clientOptions.getWorkThreads());
            channelClass = EpollSocketChannel.class;
        } else {
            workerGroup = new NioEventLoopGroup(clientOptions.getWorkThreads());
            channelClass = NioSocketChannel.class;
        }

        this.group(workerGroup);
        this.channel(channelClass);
        this.option(ChannelOption.SO_REUSEADDR, clientOptions.isReuseAddress());
        this.option(ChannelOption.SO_SNDBUF, clientOptions.getSendBufferSize());
        this.option(ChannelOption.SO_RCVBUF, clientOptions.getReceiveBufferSize());
        this.option(ChannelOption.SO_KEEPALIVE, clientOptions.isKeepAlive());
        this.option(ChannelOption.TCP_NODELAY, clientOptions.isTcpNoDelay());
        this.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientOptions.getConnectTimeout());
        maxFrameLength(clientOptions.getMaxFrameLength());
        idleTimeoutSeconds(clientOptions.getIdleTimeoutSeconds());
    }

    public NettyClient<M> decoder(ByteToMessageDecoder decoder) {
        this.decoder = decoder;
        return this;
    }

    public NettyClient<M> encoder(MessageToByteEncoder<M> encoder) {
        this.encoder = encoder;
        return this;
    }

    public NettyClient<M> messageHandler(SimpleChannelInboundHandler<M> handler) {
        this.messageHandler = handler;
        return this;
    }

    public NettyClient<M> channelIdleHandler(ChannelDuplexHandler channelIdleHandler) {
        this.channelIdleHandler = channelIdleHandler;
        return this;
    }

    public NettyClient<M> idleTimeoutSeconds(int idleSeconds) {
        this.idleTimeoutSeconds = idleSeconds;
        return this;
    }

    public NettyClient<M> maxFrameLength(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
        return this;
    }

    private void verify() {
        if (this.messageHandler == null) {
            throw new IllegalStateException("message handler not set");
        }
        if (this.channelIdleHandler == null) {
            throw new IllegalStateException("channel idle handler not set");
        }
        if (encoder == null) {
            throw new IllegalStateException("encoder not set");
        }
        if (decoder == null) {
            throw new IllegalStateException("decoder not set");
        }
    }

    public Timer getTimer() {
        return timer;
    }

    public void start() {
        this.verify();
        this.handler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline channelPipe = channel.pipeline();

                channelPipe.addFirst(CHANNEL_STATE_AWARE_HANDLER, new IdleStateHandler(idleTimeoutSeconds, idleTimeoutSeconds, idleTimeoutSeconds));
                channelPipe.addFirst(CHANNEL_IDLE_HANDLER, channelIdleHandler);
                channelPipe.addFirst(MESSAGE_ENCODER, encoder);

                channelPipe.addLast(FRAME_DECODER, new LengthFieldBasedFrameDecoder(maxFrameLength, 4, 4));
                channelPipe.addLast(MESSAGE_DECODER, decoder);
                channelPipe.addLast(MESSAGE_HANDLER, messageHandler);
            }
        });
        this.validate();
    }

    public void shutdown() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (timer != null) {
            timer.stop();
        }
    }
}
