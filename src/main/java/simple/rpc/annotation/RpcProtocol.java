package simple.rpc.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcProtocol {

    /**
     * 消息Id
     *
     * @return
     */
    int msgId();
}
