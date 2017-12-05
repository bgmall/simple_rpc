package net;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import simple.net.NetClient;
import simple.net.bootstrap.NetBootstrap;

public class NetTest {

    private static NetBootstrap netBootstrap;

    @BeforeClass
    public static void setUp() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        netBootstrap = context.getBean(NetBootstrap.class);
        netBootstrap.start();
        netBootstrap.getNetClientBootstrap().createNetClient(1, "127.0.0.1", 8000);

    }

    @AfterClass
    public static void tearDown() {
        if (netBootstrap != null) {
            netBootstrap.shutdown();
            netBootstrap = null;
        }
    }

    @Test
    public void testHelloworld() throws InterruptedException {
        SimpleNetMessage simpleNetMessage = new SimpleNetMessage();
        simpleNetMessage.setMsg("hello");
        NetClient netClient = netBootstrap.getNetClientBootstrap().getNetClient(1);
        netClient.sendMessage(simpleNetMessage);
        Thread.sleep(2000);
    }
}
