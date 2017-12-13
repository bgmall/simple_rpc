package simple.net.protocol.annotation;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Scope("prototype")
public @interface NetProtocol {

    /**
     * 消息Id
     *
     * @return
     */
    int msgId();

    byte codec() default 1;
}
