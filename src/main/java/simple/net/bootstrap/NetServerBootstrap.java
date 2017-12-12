package simple.net.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import simple.net.NetMessageHandler;
import simple.net.NetServer;
import simple.net.NetServerOptions;
import simple.net.handler.MessageDispatcher;
import simple.net.handler.MessageHandlerManager;
import simple.net.protocol.ProtocolFactoryManager;
import simple.util.PropsUtil;

import java.util.Properties;

@Component
public class NetServerBootstrap {

    private MessageDispatcher messageDispatcher;

    private NetServerOptions serverOptions;

    private NetServer netServer;

    @Autowired
    private NetBootstrap netBootstrap;

    public void start() {
        netBootstrap.start();

        if (getProtocolFactoryManager().isEmpty()) {
            throw new RuntimeException("protocol factory manager is empty, need register protocol factory!!!");
        }
        if (messageDispatcher == null) {
            throw new RuntimeException("message dispatcher is null");
        }

        initServerOptions();

        netServer = new NetServer(serverOptions);
        netServer.setProtocolFactoryManager(getProtocolFactoryManager());
        netServer.setMessageHandler(new NetMessageHandler(getMessageHandlerManager(), messageDispatcher));
        netServer.start();
    }

    public void shutdown() {
        netBootstrap.shutdown();

        if (netServer != null) {
            netServer.shutdown();
        }
    }

    public ProtocolFactoryManager getProtocolFactoryManager() {
        return netBootstrap.getProtocolFactoryManager();
    }

    private MessageHandlerManager getMessageHandlerManager() {
        return netBootstrap.getMessageHandlerManager();
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
