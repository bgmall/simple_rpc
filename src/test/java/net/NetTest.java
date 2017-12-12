package net;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import simple.net.NetClient;
import simple.net.bootstrap.NetClientBootstrap;
import simple.net.bootstrap.NetServerBootstrap;

public class NetTest {

    private static NetClientBootstrap clientBootstrap;
    private static NetServerBootstrap serverBootstrap;


    @BeforeClass
    public static void setUp() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        clientBootstrap = context.getBean(NetClientBootstrap.class);
        clientBootstrap.start();
        serverBootstrap = context.getBean(NetServerBootstrap.class);
        serverBootstrap.setMessageDispatcher(new SimpleMessageDispatcher());
        serverBootstrap.start();
        clientBootstrap.createNetClient(1, "127.0.0.1", 8000);
    }

    @AfterClass
    public static void tearDown() {
        if (clientBootstrap != null) {
            clientBootstrap.shutdown();
            clientBootstrap = null;
        }
        if (serverBootstrap != null) {
            serverBootstrap.shutdown();
            serverBootstrap = null;
        }
    }

    @Test
    public void testHelloworld() throws InterruptedException {
        SimpleNetMessage simpleNetMessage = new SimpleNetMessage();
        simpleNetMessage.setMsg("hello");
        NetClient netClient = clientBootstrap.getNetClient(1);
        netClient.sendMessage(simpleNetMessage);
        Thread.sleep(2000);
    }
}
