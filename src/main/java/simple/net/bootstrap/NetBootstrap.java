package simple.net.bootstrap;

public class NetBootstrap {

    public void start() {
        initHandlers();
        initProtocols();
        // 构建NetClientBootstrap
        // 构建NetServerBootstrap
    }

    public void shutdown() {

    }

    private void initHandlers() {
        // 扫描整个项目目录，找出message handler 与 message invoke
        // 注册到MessageHandlerManager对象
        // 构建Dispatch
    }

    private void initProtocols() {
        // 扫描整个项目目录，找出NetMessage
        // 构建ProtocolFactoryManager
        // 注册ProtocolFactory
        // 预处理消息的编码结构
    }
}
