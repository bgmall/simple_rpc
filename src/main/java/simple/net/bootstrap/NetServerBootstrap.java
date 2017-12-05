package simple.net.bootstrap;

import io.netty.util.concurrent.DefaultThreadFactory;
import simple.net.NetMessageHandler;
import simple.net.NetServer;
import simple.net.NetServerOptions;
import simple.net.handler.MessageDispatcher;
import simple.net.handler.MessageHandlerManager;
import simple.net.protocol.ProtocolFactoryManager;
import simple.util.PropsUtil;

import java.util.Properties;
import java.util.concurrent.ThreadFactory;

public class NetServerBootstrap {

    private int ioThreadPoolSize;

    private ThreadFactory ioThreadFactory;

    private ProtocolFactoryManager protocolFactoryManager;

    private MessageHandlerManager messageHandlerManager;

    private MessageDispatcher messageDispatcher;

    private NetServerOptions serverOptions;

    private NetServer netServer;

    public NetServerBootstrap() {
        this(0);
    }

    public NetServerBootstrap(int ioThreadPoolSize) {
        this(ioThreadPoolSize, new DefaultThreadFactory("NetServerIoThread"));
    }

    public NetServerBootstrap(int ioThreadPoolSize, ThreadFactory ioThreadFactory) {
        this.ioThreadPoolSize = ioThreadPoolSize;
        this.ioThreadFactory = ioThreadFactory;
    }

    public void start() {
        if (protocolFactoryManager == null) {
            throw new RuntimeException("protocol factory manager is null");
        }
        if (messageDispatcher == null) {
            throw new RuntimeException("message dispatcher is null");
        }
        if (messageHandlerManager == null) {
            throw new RuntimeException("message handler manager is null");
        }

        initServerOptions();

        if (ioThreadPoolSize == 0) {
            ioThreadPoolSize = serverOptions.getWorkThreads();
            if (ioThreadPoolSize == 0) {
                ioThreadPoolSize = Runtime.getRuntime().availableProcessors();
            }
        }

        netServer = new NetServer(serverOptions);
        netServer.setProtocolFactoryManager(protocolFactoryManager);
        netServer.setMessageHandler(new NetMessageHandler(messageHandlerManager, messageDispatcher));
        netServer.start();
    }

    public void shutdown() {
        if (netServer != null) {
            netServer.shutdown();
        }
    }

    public ProtocolFactoryManager getProtocolFactoryManager() {
        return protocolFactoryManager;
    }

    public void setProtocolFactoryManager(ProtocolFactoryManager protocolFactoryManager) {
        this.protocolFactoryManager = protocolFactoryManager;
    }

    public MessageHandlerManager getMessageHandlerManager() {
        return messageHandlerManager;
    }

    public void setMessageHandlerManager(MessageHandlerManager messageHandlerManager) {
        this.messageHandlerManager = messageHandlerManager;
    }

    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }

    public void setMessageDispatcher(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    private void initServerOptions() {
        serverOptions = new NetServerOptions();
        Properties conf = PropsUtil.loadProps("server.properties");
        if (conf == null) {
            throw new RuntimeException("can't find server.properties file");
        }

        //  默认超时10s
        serverOptions.setConnectTimeout(PropsUtil.getInt(conf, "connectTimeout", 10000));
        serverOptions.setAcceptorThreads(PropsUtil.getInt(conf, "acceptorThreads", 1));
        serverOptions.setWorkThreads(PropsUtil.getInt(conf, "workerThreads", Runtime.getRuntime().availableProcessors()));
        serverOptions.setIdleTimeoutSeconds(PropsUtil.getInt(conf, "idleTimeoutSeconds", 3 * 60 * 1000));
        serverOptions.setTcpNoDelay(PropsUtil.getBoolean(conf, "tcpNoDelay", true));
        serverOptions.setKeepAlive(PropsUtil.getBoolean(conf, "keepAlive", true));
        serverOptions.setMaxFrameLength(PropsUtil.getInt(conf, "maxFrameLength", 32 * 1024 * 1024));
        serverOptions.setReceiveBufferSize(PropsUtil.getInt(conf, "receiveBufferSize", 8 * 1024));
        serverOptions.setSendBufferSize(PropsUtil.getInt(conf, "sendBufferSize", 32 * 1024));
        serverOptions.setReuseAddress(PropsUtil.getBoolean(conf, "reuseAddress", true));
        serverOptions.setBacklog(PropsUtil.getInt(conf, "backlog", 10000));
        serverOptions.setListenPort(PropsUtil.getInt(conf, "port", 8000));
    }

}
