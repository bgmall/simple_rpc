package simple.net.protocol.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NetProtocol {

    /**
     * 消息Id
     *
     * @return
     */
    int msgId();

    byte codec();
}
