package simple.net.bootstrap;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import simple.net.handler.MessageHandlerManager;
import simple.net.handler.annotation.NetMessageHandler;
import simple.net.protocol.MessageManager;
import simple.net.protocol.NetMessage;
import simple.net.protocol.ProtocolFactoryManager;
import simple.net.protocol.codec.protostuff.ProtostuffProtocolFactory;

@Component
class NetBootstrap implements ApplicationListener<ContextRefreshedEvent> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetBootstrap.class);

    private MessageManager messageManager = new MessageManager();

    private MessageHandlerManager messageHandlerManager = new MessageHandlerManager();

    private ProtocolFactoryManager protocolFactoryManager = new ProtocolFactoryManager();

    @Autowired
    private ConfigurableListableBeanFactory factory;

    @Autowired
    private ApplicationContext context;

    public void start() {
        // 注册默认的编解码factory: protostuff
        if (protocolFactoryManager.isEmpty()) {
            protocolFactoryManager.register(new ProtostuffProtocolFactory(messageManager));
        }
    }

    public void shutdown() {

    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        String[] beanDefinitionNames = contextRefreshedEvent.getApplicationContext().getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = factory.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null && !beanClassName.isEmpty()) {
                try {
                    final Class<?> beanClass = Class.forName(beanClassName);
                    if (NetMessage.class.isAssignableFrom(beanClass)) {
                        messageManager.register(beanClass);
                    } else {
                        NetMessageHandler annotation = beanClass.getAnnotation(NetMessageHandler.class);
                        if (annotation != null) {
                            messageHandlerManager.register(context.getBean(beanClass));
                        }
                    }
                } catch (ClassNotFoundException e) {
                    logger.error(beanClassName + "can't find class", e);
                }
            }
        }
    }

    public ProtocolFactoryManager getProtocolFactoryManager() {
        return protocolFactoryManager;
    }

    public MessageHandlerManager getMessageHandlerManager() {
        return messageHandlerManager;
    }
}
