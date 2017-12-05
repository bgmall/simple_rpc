package simple.net.bootstrap;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import simple.net.NetClient;
import simple.net.NetClientOptions;
import simple.net.protocol.ProtocolFactoryManager;
import simple.util.PropsUtil;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Administrator on 2017/12/2.
 */
public class NetClientBootstrap {

    private final ConcurrentMap<Integer, NetClient> serverIdToClient = new ConcurrentHashMap<>();

    private EventLoopGroup clientEventGroup;

    private int ioThreadPoolSize;

    private ThreadFactory ioThreadFactory;

    private ProtocolFactoryManager protocolFactoryManager;

    private NetClientOptions clientOptions;

    public NetClientBootstrap() {
        this(0);
    }

    public NetClientBootstrap(int ioThreadPoolSize) {
        this(ioThreadPoolSize, new DefaultThreadFactory("NetClientIoThread"));
    }

    public NetClientBootstrap(int ioThreadPoolSize, ThreadFactory ioThreadFactory) {
        this.ioThreadPoolSize = ioThreadPoolSize;
        this.ioThreadFactory = ioThreadFactory;
    }

    public void start() {
        if (protocolFactoryManager == null) {
            throw new RuntimeException("protocol factory manager is null");
        }

        initClientOptions();

        if (this.ioThreadPoolSize == 0) {
            this.ioThreadPoolSize = clientOptions.getEventLoopThreads();
        }
        clientEventGroup = NetClient.createWorkerGroup(this.ioThreadPoolSize, this.ioThreadFactory);
    }

    public void shutdown() {
        if (clientEventGroup != null) {
            clientEventGroup.shutdownGracefully();
            clientEventGroup = null;
        }

        closeAllNetClient();
    }

    public ProtocolFactoryManager getProtocolFactoryManager() {
        return protocolFactoryManager;
    }

    public void setProtocolFactoryManager(ProtocolFactoryManager protocolFactoryManager) {
        this.protocolFactoryManager = protocolFactoryManager;
    }

    public NetClient createNetClient(int serverId, String host, int port) {
        NetClient netClient = new NetClient(clientOptions, host, port);
        NetClient oldNetClient = serverIdToClient.putIfAbsent(serverId, netClient);
        if (oldNetClient != null) {
            return oldNetClient;
        }

        netClient.setProtocolFactoryManager(protocolFactoryManager);
        netClient.start(clientEventGroup);
        return netClient;
    }

    public NetClient getNetClient(int serverId) {
        return serverIdToClient.get(serverId);
    }

    private void closeAllNetClient() {
        for (NetClient netClient : serverIdToClient.values()) {
            netClient.shutdown();
        }
        serverIdToClient.clear();
    }

    private void initClientOptions() {
        clientOptions = new NetClientOptions();
        Properties conf = PropsUtil.loadProps("client.properties");
        if (conf == null) {
            throw new RuntimeException("can't find client.properties file");
        }

        //  默认超时10s
        clientOptions.setConnectTimeout(PropsUtil.getInt(conf, "connectTimeout", 10000));
        clientOptions.setEventLoopThreads(PropsUtil.getInt(conf, "eventLoopThreads", Runtime.getRuntime().availableProcessors()));
        clientOptions.setIdleTimeoutSeconds(PropsUtil.getInt(conf, "idleTimeoutSeconds", 3 * 60 * 1000));
        clientOptions.setTcpNoDelay(PropsUtil.getBoolean(conf, "tcpNoDelay", true));
        clientOptions.setKeepAlive(PropsUtil.getBoolean(conf, "keepAlive", true));
        clientOptions.setMaxFrameLength(PropsUtil.getInt(conf, "maxFrameLength", 32 * 1024 * 1024));
        clientOptions.setReceiveBufferSize(PropsUtil.getInt(conf, "receiveBufferSize", 8 * 1024));
        clientOptions.setSendBufferSize(PropsUtil.getInt(conf, "sendBufferSize", 32 * 1024));
        clientOptions.setReuseAddress(PropsUtil.getBoolean(conf, "reuseAddress", true));
        clientOptions.setReconnectIntervalMills(PropsUtil.getInt(conf, "reconnectIntervalMills", 60 * 1000));
    }
}
