package simple.net.protocol.annotation;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import simple.net.protocol.ProtocolType;
import simple.net.protocol.codec.CodecType;
import simple.net.protocol.compress.CompressType;

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

    /**
     * 协议类型, 目前只有普通的Net类型, 后面会加上Rpc类型，可以加上自己自定义的类型, 不同类型的协议数据流处理会不一样
     *
     * @return
     */
    int protocolType() default ProtocolType.NET;

    /**
     * 编码, 目前有(1:protostuff), 若没有注册protocolFactory时会默认注册, 可以自定义注册protocolFactory
     *
     * @return
     */
    int codecType() default CodecType.PROTOSTUFF;

    /**
     * 压缩类型，目前支持3种(0:不压缩 1:snappy 2:zlib), 具体要不要压缩，通过判定数据大小是否达到压缩长度要求
     *
     * @return
     */
    int compressType() default CompressType.COMPRESS_SNAPPY;

    /**
     * 数据达到多少字节才压缩
     *
     * @return
     */
    int compressRequiredLength() default 256;
}
