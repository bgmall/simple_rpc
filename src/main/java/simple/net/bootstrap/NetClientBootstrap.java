package simple.net.bootstrap;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import simple.net.NetClient;

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

    public NetClientBootstrap() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public NetClientBootstrap(int ioThreadPoolSize) {
        this(ioThreadPoolSize, new DefaultThreadFactory("NetClientIoThread"));
    }

    public NetClientBootstrap(int ioThreadPoolSize, ThreadFactory ioThreadFactory) {
        this.ioThreadPoolSize = ioThreadPoolSize;
        this.ioThreadFactory = ioThreadFactory;
    }

    public void start() {
        clientEventGroup = NetClient.createWorkerGroup(this.ioThreadPoolSize, this.ioThreadFactory);

        // NetClientOptions
        // ProtocolFactoryManager

    }

    public void shutdown() {
        if (clientEventGroup != null) {
            clientEventGroup.shutdownGracefully();
            clientEventGroup = null;
        }
    }

}
