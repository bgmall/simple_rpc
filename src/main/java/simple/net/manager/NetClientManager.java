package simple.net.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.util.internal.ConcurrentSet;
import simple.net.NetClient;
import simple.net.NetClientOptions;
import simple.net.protocol.NetMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetClientManager {

    private ExecutorService connectService;

    private ConcurrentSet<Integer> needConnectServerIds = new ConcurrentSet<>();

    private ConcurrentMap<Integer, NetClient> serverIdToClient = new ConcurrentHashMap<>();

    private EventLoopGroup clientEventGroup;

    public void init() {
        clientEventGroup = NetClient.createWorkerGroup(Runtime.getRuntime().availableProcessors(), null);
        connectService = Executors.newSingleThreadExecutor();
    }

    public void shutdown() {
        if (clientEventGroup != null) {
            clientEventGroup.shutdownGracefully();
            clientEventGroup = null;
        }
        if (connectService != null) {
            connectService.shutdown();
            connectService = null;
        }
    }

    private String getRemoteHost(int serverId) {
        return "127.0.0.1";
    }

    private int getRemotePort(int serverId) {
        return 4000;
    }

    public void createClientToServer(int serverId, NetClientOptions clientOptions) {
        String host = getRemoteHost(serverId);
        int port = getRemotePort(serverId);
        NetClient netClient = new NetClient(clientOptions, host, port);
        serverIdToClient.put(serverId, netClient);
        netClient.start(clientEventGroup);
        netClient.tryToConnect();
    }

    public void sendMessage(int serverId, NetMessage message) {
        NetClient netClient = serverIdToClient.get(serverId);
        if (netClient == null) {
            throw new RuntimeException();
        }

        netClient.sendMessage(message);

        if (!netClient.isConnected()) {
            // 加入连接线程队列
            reconnect(serverId);
        }
    }

    private void reconnect(int serverId) {
        if (needConnectServerIds.add(serverId)) {
            connectService.submit(new Runnable() {
                @Override
                public void run() {
                    NetClient netClient = serverIdToClient.get(serverId);
                    if (netClient != null) {
                        if (netClient.tryToConnect()) {
                            needConnectServerIds.remove(serverId);
                        }
                    }
                }
            });
        }
    }
}
