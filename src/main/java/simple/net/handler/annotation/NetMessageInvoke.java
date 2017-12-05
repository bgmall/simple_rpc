package simple.net.handler.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NetMessageInvoke {

    int msgId();
}
