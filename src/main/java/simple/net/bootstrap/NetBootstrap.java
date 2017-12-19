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
import simple.net.protocol.ProtocolFactory;
import simple.net.protocol.ProtocolFactoryManager;
import simple.net.protocol.annotation.NetProtocol;
import simple.net.protocol.codec.CodecFactoryManager;
import simple.net.protocol.codec.protostuff.ProtostuffCodecFactory;
import simple.net.protocol.compress.CompressFactoryManager;
import simple.net.protocol.message.MessageManager;
import simple.net.protocol.message.NetMessage;

import java.util.Collection;

@Component
class NetBootstrap implements ApplicationListener<ContextRefreshedEvent> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetBootstrap.class);

    @Autowired
    private ConfigurableListableBeanFactory factory;

    @Autowired
    private ApplicationContext context;

    public void start() {
        // 注册默认的编解码factory: protostuff
        if (CodecFactoryManager.getInstance().isEmpty()) {
            CodecFactoryManager.getInstance().register(new ProtostuffCodecFactory());
        }

        checkMessageValid();
    }

    public void shutdown() {

    }

    private void checkMessageValid() {
        Collection<Class<?>> messageClasses = MessageManager.getInstance().getMessageClasses();
        for (Class<?> clazz : messageClasses) {
            NetProtocol annotation = clazz.getAnnotation(NetProtocol.class);
            ProtocolFactory protocolFactory = ProtocolFactoryManager.getInstance().select(annotation.protocolType());
            if (protocolFactory == null || !protocolFactory.checkValidMessage(clazz)) {
                throw new IllegalStateException("msgId[" + annotation.msgId() + "] protocolType[" + annotation.protocolType() + "] invalid");
            }
            if (CompressFactoryManager.getInstance().select(annotation.compressType()) == null) {
                throw new IllegalStateException("msgId[" + annotation.msgId() + "] compressType[" + annotation.compressType() + "] invalid");
            }
            if (CodecFactoryManager.getInstance().select(annotation.codecType()) == null) {
                throw new IllegalStateException("msgId[" + annotation.msgId() + "] codeType[" + annotation.codecType() + "] invalid");
            }
        }
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
                        MessageManager.getInstance().register(beanClass);
                    } else {
                        NetMessageHandler annotation = beanClass.getAnnotation(NetMessageHandler.class);
                        if (annotation != null) {
                            MessageHandlerManager.getInstance().register(context.getBean(beanClass));
                        }
                    }
                } catch (ClassNotFoundException e) {
                    logger.error(beanClassName + "can't find class", e);
                }
            }
        }
    }
}
