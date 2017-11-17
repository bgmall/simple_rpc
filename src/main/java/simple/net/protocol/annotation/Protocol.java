package simple.net.protocol.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Protocol {

    /**
     * 消息Id
     *
     * @return
     */
    int msgId();

    int codec();
}
