package simple.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.net.callback.ClientCallState;
import simple.net.callback.MessageCallback;
import simple.net.exception.ConnectionException;
import simple.net.exception.SendTimeoutException;
import simple.net.protocol.*;
import simple.util.NettyUtil;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class NetClient extends Bootstrap {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetClient.class);

    private static final String CHANNEL_STATE_AWARE_HANDLER = "channel_state_aware_handler";
    private static final String CHANNEL_STATE_HANDLER = "channle_state_handler";
    private static final String MESSAGE_DECODER = "message_decoder";
    private static final String MESSAGE_ENCODER = "message_encoder";
    private static final String MESSAGE_CALLBACK_HANDLER = "message_callback_handler";
    private static final String MESSAGE_HANDLER = "message_handler";

    /**
     * Tick count of each wheel instance for timer.
     */
    private static final int DEFAULT_TICKS_PER_WHEEL = 2048;
    /**
     * Tick duration for timer.
     */
    private static final int DEFAULT_TICK_DURATION = 100;

    private EventLoopGroup workerGroup;

    private NetClientOptions clientOptions;

    private String remoteAddress;

    private ProtocolFactoryManager protocolFactoryManager;

    private SimpleChannelInboundHandler<NetMessage> messageHandler;

    private ChannelPool channelPool;

    private AtomicLong correlationId = new AtomicLong(1);

    private final ConcurrentMap<Long, ClientCallState> requestMap = new ConcurrentHashMap<>();

    private static Timer timer = createTimer(); // 初始化定时器

    private static Timer createTimer() {
        Timer timer = new HashedWheelTimer(Executors.defaultThreadFactory(), DEFAULT_TICK_DURATION,
                TimeUnit.MILLISECONDS, DEFAULT_TICKS_PER_WHEEL);
        return timer;
    }

    public Timer getTimer() {
        return timer;
    }

    public static EventLoopGroup createWorkerGroup(int eventLoopThreads, ThreadFactory threadFactory) {
        if (NettyUtil.isLinuxPlatform()) {
            if (threadFactory != null) {
                return new EpollEventLoopGroup(eventLoopThreads, threadFactory);
            } else {
                return new EpollEventLoopGroup(eventLoopThreads);
            }
        } else {
            if (threadFactory != null) {
                return new NioEventLoopGroup(eventLoopThreads, threadFactory);
            } else {
                return new NioEventLoopGroup(eventLoopThreads);
            }
        }
    }

    public NetClient(NetClientOptions clientOptions, String host, int port) {
        this.remoteAddress = host + ":" + port;
        this.remoteAddress(host, port);
        this.clientOptions = clientOptions;
    }

    public void start() {
        EventLoopGroup workerGroup = createWorkerGroup(clientOptions.getEventLoopThreads(), null);
        this.workerGroup = workerGroup;
        start(workerGroup);
    }

    public void start(EventLoopGroup workerGroup) {
        Class<? extends Channel> channelClass;
        if (workerGroup instanceof EpollEventLoopGroup) {
            channelClass = EpollSocketChannel.class;
        } else {
            channelClass = NioSocketChannel.class;
        }

        this.group(workerGroup);
        this.channel(channelClass);
        this.handler(createNetClientChannelInitializer());
        this.option(ChannelOption.SO_REUSEADDR, clientOptions.isReuseAddress());
        this.option(ChannelOption.SO_SNDBUF, clientOptions.getSendBufferSize());
        this.option(ChannelOption.SO_RCVBUF, clientOptions.getReceiveBufferSize());
        this.option(ChannelOption.SO_KEEPALIVE, clientOptions.isKeepAlive());
        this.option(ChannelOption.TCP_NODELAY, clientOptions.isTcpNoDelay());
        this.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientOptions.getConnectTimeout());

        if (channelPool == null) {
            channelPool = new SingleChannelPool(this);
        }
    }

    public void shutdown() {
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
            this.workerGroup = null;
        }
        if (channelPool != null) {
            channelPool.close();
            channelPool = null;
        }
    }

    public long getNextCorrelationId() {
        return correlationId.getAndIncrement();
    }

    public ClientCallState removePendingRequest(long seqId) {
        return requestMap.remove(seqId);
    }

    public void registerPendingRequest(long seqId, ClientCallState state) {
        if (requestMap.containsKey(seqId)) {
            throw new IllegalArgumentException("State already registered");
        }
        requestMap.put(seqId, state);
    }

    public NetClientOptions getClientOptions() {
        return clientOptions;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public ProtocolFactoryManager getProtocolFactoryManager() {
        return protocolFactoryManager;
    }

    public void setProtocolFactoryManager(ProtocolFactoryManager protocolFactoryManager) {
        this.protocolFactoryManager = protocolFactoryManager;
    }

    public SimpleChannelInboundHandler<NetMessage> getMessageHandler() {
        return messageHandler;
    }

    public void setMessageHandler(SimpleChannelInboundHandler<NetMessage> messageHandler) {
        this.messageHandler = messageHandler;
    }

    public ChannelPool getChannelPool() {
        return channelPool;
    }

    public void setChannelPool(ChannelPool channelPool) {
        this.channelPool = channelPool;
    }

    private ChannelInitializer<Channel> createNetClientChannelInitializer() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline channelPipe = ch.pipeline();

                if (protocolFactoryManager == null) {
                    throw new RuntimeException("protocol factory manager is null");
                }

                channelPipe.addLast(MESSAGE_ENCODER, new MessageEncoder(protocolFactoryManager));

                int idleTimeoutSeconds = getClientOptions().getIdleTimeoutSeconds();
                channelPipe.addLast(CHANNEL_STATE_AWARE_HANDLER, new IdleStateHandler(idleTimeoutSeconds, idleTimeoutSeconds, idleTimeoutSeconds));
                channelPipe.addLast(CHANNEL_STATE_HANDLER, new NetChannelStateHandler());
                int maxFrameLength = getClientOptions().getMaxFrameLength();
                channelPipe.addLast(MESSAGE_DECODER, new MessageDecoder(maxFrameLength, protocolFactoryManager));
                channelPipe.addLast(MESSAGE_CALLBACK_HANDLER, new NetClientCallbackHandler(NetClient.this));

                // 这边假设client可以不监听message，统一由server监听处理
                if (getMessageHandler() != null) {
                    channelPipe.addLast(MESSAGE_HANDLER, getMessageHandler());
                }
            }
        };
    }

    public void sendMessage(NetMessage message) {
        NetConnection connection = channelPool.choose(message);
        connection.writeAndFlush(message);
    }

    public void sendMessage(NetMessage message, MessageCallback callback) {
        if (!(message instanceof CallbackMessage)) {
            if (callback != null) {
                callback.exceptionCaught(new IllegalArgumentException("message must be callbackMessage"));
            }
            return;
        }
        NetConnection connection = channelPool.choose(message);
        if (connection.invalidChannel()) {
            if (callback != null) {
                callback.exceptionCaught(createConnectionException());;
            }
            return;
        }

        CallbackMessage callbackMessage = (CallbackMessage) message;
        long nextCorrelationId = getNextCorrelationId();
        ClientCallState clientCallState = new ClientCallState();
        clientCallState.setCallback(callback);
        clientCallState.setCallbackId(nextCorrelationId);
        if (callbackMessage.getTimeoutMills() > 0) {
            Timeout timeout = createTimeout(clientCallState, callbackMessage.getTimeoutMills(), message.getMsgId());
            clientCallState.setTimeout(timeout);
        }
        registerPendingRequest(nextCorrelationId, clientCallState);
        connection.writeAndFlush(message);
    }

    private Timeout createTimeout(ClientCallState clientCallState, long timeoutMills, int msgId) {
        return getTimer().newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                ClientCallState callState = removePendingRequest(clientCallState.getCallbackId());
                if (callState != null) {
                    logger.error("send msg[{}] to remote address[{}] timeout", msgId, getRemoteAddress());
                    clientCallState.handleException(createSendTimeoutException(msgId));
                }
            }
        }, timeoutMills, TimeUnit.MILLISECONDS);
    }

    private SendTimeoutException createSendTimeoutException(int msgId) {
        return new SendTimeoutException("send msg[" + msgId + "] to remote address[" + getRemoteAddress() + "] timeout");
    }

    private ConnectionException createConnectionException() {
        return new ConnectionException("channel to remote address[" + getRemoteAddress() + "] is closed");
    }
}
