package com.github.zhanghui.integration.core.process.post.extension;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.Set;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/20 10:05
 **/
@Getter
@Setter
public class ControllerWrapper {

    private String beanName;

    private Class<?> beanClass;

    private Set<RequestMappingInfo> requestMappingInfos;
}
