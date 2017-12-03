package net;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import simple.net.NetClientOptions;
import simple.net.NetServer;
import simple.net.NetServerOptions;

public class NetTest {

    private static NetClientManager connectManager = new NetClientManager();

    private static NetServer netServer;

    @BeforeClass
    public static void setUp() {
        connectManager.init();

        NetServerOptions netServerOptions = new NetServerOptions();
        netServerOptions.setBacklog(100);
        netServerOptions.setConnectTimeout(1000);
        netServerOptions.setIdleTimeoutSeconds(60);
        netServerOptions.setKeepAlive(true);
        netServerOptions.setMaxFrameLength(100);
        netServerOptions.setReceiveBufferSize(64 * 1024);
        netServerOptions.setSendBufferSize(64 * 1024);
        netServerOptions.setWorkThreads(2);
        NetServer netServer = new NetServer(netServerOptions);
        netServer.start(4000);

        NetClientOptions clientOptions = new NetClientOptions();
        clientOptions.setConnectTimeout(1000);
        clientOptions.setIdleTimeoutSeconds(60);
        clientOptions.setKeepAlive(true);
        clientOptions.setMaxFrameLength(100);
        clientOptions.setReceiveBufferSize(64 * 1024);
        clientOptions.setSendBufferSize(64 * 1024);
        connectManager.createClientToServer(1, clientOptions);
    }

    @AfterClass
    public static void tearDown() {
        connectManager.shutdown();
    }

    @Test
    public void testHelloworld() throws InterruptedException {
        SimpleNetMessage simpleNetMessage = new SimpleNetMessage();
        simpleNetMessage.setMsg("hello");
        connectManager.sendMessage(1, simpleNetMessage);
        Thread.sleep(2000);
    }
}
