package simple.net.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import simple.net.NetClient;
import simple.net.NetClientOptions;
import simple.net.NetMessageHandler;
import simple.net.handler.MessageDispatcher;
import simple.util.PropsUtil;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2017/12/2.
 */
@Component
public class NetClientBootstrap {

    private final ConcurrentMap<Integer, NetClient> serverIdToClient = new ConcurrentHashMap<>();

    private NetClientOptions clientOptions;

    private MessageDispatcher messageDispatcher;

    private NetMessageHandler messageHandler;

    @Autowired
    private NetBootstrap netBootstrap;

    public void start() {
        netBootstrap.start();

        if (messageDispatcher != null) {
            messageHandler = new NetMessageHandler(messageDispatcher);
        }

        initClientOptions();
    }

    public void shutdown() {
        netBootstrap.shutdown();

        closeAllNetClient();
    }

    public NetClient createNetClient(int serverId, String host, int port) {
        NetClient netClient = new NetClient(clientOptions, host, port);
        NetClient oldNetClient = serverIdToClient.putIfAbsent(serverId, netClient);
        if (oldNetClient != null) {
            return oldNetClient;
        }

        if (messageHandler != null) {
            netClient.setMessageHandler(messageHandler);
        }
        netClient.start();
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
        clientOptions.setIdleTimeoutSeconds(PropsUtil.getInt(conf, "idleTimeoutSeconds", 3 * 60));
        clientOptions.setTcpNoDelay(PropsUtil.getBoolean(conf, "tcpNoDelay", true));
        clientOptions.setKeepAlive(PropsUtil.getBoolean(conf, "keepAlive", true));
        clientOptions.setMaxFrameLength(PropsUtil.getInt(conf, "maxFrameLength", 32 * 1024));
        clientOptions.setReceiveBufferSize(PropsUtil.getInt(conf, "receiveBufferSize", 32 * 1024));
        clientOptions.setSendBufferSize(PropsUtil.getInt(conf, "sendBufferSize", 32 * 1024));
        clientOptions.setReuseAddress(PropsUtil.getBoolean(conf, "reuseAddress", true));
        clientOptions.setReconnectIntervalMills(PropsUtil.getInt(conf, "reconnectIntervalMills", 60 * 1000));
        clientOptions.setHeartBeatIntervalMills(PropsUtil.getInt(conf, "heartBeatIntervalMills", 20 * 1000));
    }

    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }

    public void setMessageDispatcher(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }
}
