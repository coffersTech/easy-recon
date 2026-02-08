package tech.coffers.recon.annotation;

import org.springframework.context.annotation.Import;
import tech.coffers.recon.autoconfigure.ReconSdkAutoConfiguration;

import java.lang.annotation.*;

/**
 * 启用对账 SDK 注解
 * <p>
 * 在 Spring Boot 应用的启动类上添加此注解，即可启用对账 SDK
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ReconSdkAutoConfiguration.class)
public @interface EnableEasyRecon {
}
