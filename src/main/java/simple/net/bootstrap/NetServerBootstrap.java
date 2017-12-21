package simple.net.bootstrap;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import simple.net.NetChannelStateHandler;
import simple.net.NetMessageHandler;
import simple.net.NetServer;
import simple.net.NetServerOptions;
import simple.net.handler.MessageDispatcher;
import simple.util.PropsUtil;

import java.util.Properties;

@Component
public class NetServerBootstrap {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetServerBootstrap.class);

    private NetServerOptions serverOptions;

    private NetServer netServer;

    private MessageDispatcher messageDispatcher;

    private NetChannelStateHandler stateHandler;

    private NetMessageHandler messageHandler;

    @Autowired
    private NetBootstrap netBootstrap;

    public void start() {
        if (messageDispatcher == null) {
            throw new RuntimeException("message dispatcher is null");
        }

        initServerOptions();

        netBootstrap.start();

        if (stateHandler == null) {
            stateHandler = new NetChannelStateHandler();
        }

        if (messageHandler == null) {
            messageHandler = new NetMessageHandler(messageDispatcher);
        }

        netServer = new NetServer(serverOptions);
        netServer.setMessageHandler(new NetMessageHandler(messageDispatcher));
        netServer.setStateHandler(stateHandler);
        netServer.start();
    }

    public void shutdown() {
        if (netServer != null) {
            netServer.shutdown();
        }

        netBootstrap.shutdown();

        if (stateHandler != null) {
            stateHandler = null;
        }

        if (messageHandler != null) {
            messageHandler = null;
        }
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
            conf = new Properties();
            logger.warn("can't find server.properties file, use default values");
        }

        //  默认超时10s
        serverOptions.setConnectTimeout(PropsUtil.getInt(conf, "connectTimeout", 10000));
        serverOptions.setAcceptorThreads(PropsUtil.getInt(conf, "acceptorThreads", 1));
        serverOptions.setWorkThreads(PropsUtil.getInt(conf, "workerThreads", Runtime.getRuntime().availableProcessors()));
        serverOptions.setIdleTimeoutSeconds(PropsUtil.getInt(conf, "idleTimeoutSeconds", 3 * 60));
        serverOptions.setTcpNoDelay(PropsUtil.getBoolean(conf, "tcpNoDelay", true));
        serverOptions.setKeepAlive(PropsUtil.getBoolean(conf, "keepAlive", true));
        serverOptions.setMaxFrameLength(PropsUtil.getInt(conf, "maxFrameLength", 32 * 1024));
        serverOptions.setReceiveBufferSize(PropsUtil.getInt(conf, "receiveBufferSize", 32 * 1024));
        serverOptions.setSendBufferSize(PropsUtil.getInt(conf, "sendBufferSize", 32 * 1024));
        serverOptions.setReuseAddress(PropsUtil.getBoolean(conf, "reuseAddress", true));
        serverOptions.setBacklog(PropsUtil.getInt(conf, "backlog", 10000));
        serverOptions.setListenPort(PropsUtil.getInt(conf, "port", 8000));
    }

}
