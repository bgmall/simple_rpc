package simple.net.bootstrap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import springScan.message.MessageHandler;

@Component
public class SpringScan implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ConfigurableListableBeanFactory factory;


    public static void main(String[] args) {
//        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(BeanConfiguration.class);
    }

//    @Override
//    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
//        System.out.println("wwwwwwwwwwwwwwwwww");
//        String[] beanNamesForAnnotation = configurableListableBeanFactory.getBeanNamesForAnnotation(MessageHandler.class);
//        for (String name : beanNamesForAnnotation) {
//            System.out.println(name);
//        }
//
//        Map<String, Object> beansWithAnnotation = configurableListableBeanFactory.getBeansWithAnnotation(MessageHandler.class);
//        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
//            System.out.println(entry.getKey());
//            System.out.println(entry.getValue());
//        }
//    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        String[] beanDefinitionNames = contextRefreshedEvent.getApplicationContext().getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.println(beanDefinitionName);

            try {
                BeanDefinition beanDefinition = factory.getBeanDefinition(beanDefinitionName);
                String originalClassName = beanDefinition.getBeanClassName();
//                System.out.println(originalClassName);
                if (originalClassName != null && !originalClassName.isEmpty()) {
                    try {
                        final Class<?> originalClass = Class.forName(originalClassName);
                        MessageHandler annotation = originalClass.getAnnotation(MessageHandler.class);
                        if (annotation != null) {
                            System.out.println(originalClassName);
                        }
                    } catch (ClassNotFoundException e) {
                        // ...
                    }
                }
            } catch (BeansException e) {
                // ...
            }
        }
    }


//    @Override
//    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
//        ApplicationContext context = contextRefreshedEvent.getApplicationContext();
//        String[] beanNamesForAnnotation = context.getBeanNamesForAnnotation(MessageHandler.class);
//        for (String name : beanNamesForAnnotation) {
//           System.out.println(name);
//        }
////        String[] names = context.getBeanDefinitionNames();
////        for (String name : names) {
////            try {
////                BeanDefinition definition = factory.getBeanDefinition(name);
////                String originalClassName = definition.getBeanClassName();
////                Class<?> originalClass = Class.forName(originalClassName);
////                Method[] methods = originalClass.getMethods();
////                for (Method method : methods) {
////                    if (method.isAnnotationPresent(AfterSpringLoadComplete.class)){
////                        Object bean = context.getBean(name);
////                        Method currentMethod = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
////                        currentMethod.invoke(bean);
////                    }
////                }
////            } catch (Exception ignored) {
////            }
////        }

}
