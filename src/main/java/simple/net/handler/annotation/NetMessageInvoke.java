package simple.net.handler.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface NetMessageInvoke {

    int msgId();
}
