package com.github.zhanghui.integration.core;

import com.github.zhanghui.exception.MainContextRuntimeException;
import com.github.zhanghui.exception.PluginContextRuntimeException;
import com.github.zhanghui.integration.configuration.IntegrationConfiguration;
import com.github.zhanghui.integration.core.operator.DefaultPluginOperator;
import com.github.zhanghui.integration.core.operator.PluginOperator;
import com.github.zhanghui.integration.core.operator.PluginOperatorWrapper;
import com.github.zhanghui.integration.core.user.DefaultPluginUser;
import com.github.zhanghui.integration.core.user.PluginUser;
import com.github.zhanghui.integration.listener.PluginInitializerListener;
import com.github.zhanghui.integration.pf4j.DefaultPf4jApplicationContext;
import com.github.zhanghui.integration.pf4j.Pf4jApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.springframework.context.ApplicationContext;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 10:34
 **/
@Slf4j
public class DefaultPluginApplication extends AbstractPluginApplication {

    private AtomicBoolean isInitial = new AtomicBoolean(false);

    private PluginOperator pluginOperator;

    private Pf4jApplicationContext pf4jApplicationContext;

    /**
     * 主版本初始化
     *
     * @param applicationContext
     * @param listener
     */
    @Override
    public void initialize(ApplicationContext applicationContext, PluginInitializerListener listener) {
        Objects.requireNonNull(applicationContext,"参数 [applicationContext] 不能为空");
        if(isInitial.get()){
            throw new MainContextRuntimeException("主版本已经初始化，不要重复初始化");
        }

        // 获取主版本对插件的配置
        IntegrationConfiguration configuration = getConfiguration(applicationContext);
        if(pf4jApplicationContext == null) {
            pf4jApplicationContext = new DefaultPf4jApplicationContext(configuration);
        }

        PluginManager pluginManager = pf4jApplicationContext.getPluginManager();
        pluginOperator = createPluginOperator(applicationContext,pluginManager,configuration);

        try {
            // 插件初始化（核心）
            pluginOperator.initPlugins(listener);
        } catch (Exception e) {
            throw new PluginContextRuntimeException("插件初始化异常：[" +e.getMessage()+ "]",e);
        }

        isInitial.set(true);
    }

    @Override
    public PluginOperator getPluginOperator() {
        return this.pluginOperator;
    }

    /**
     * 创建PluginUser
     *
     * @param applicationContext
     * @param pluginManager
     * @return
     */
    private PluginUser createPluginUser(ApplicationContext applicationContext, PluginManager pluginManager){
        return  new DefaultPluginUser(applicationContext,pluginManager);
    }

    /**
     * 创建PluginOperator
     *
     * @param applicationContext
     * @param pluginManager
     * @param configuration
     * @return
     */
    private PluginOperator createPluginOperator(ApplicationContext applicationContext, PluginManager pluginManager,IntegrationConfiguration configuration){
        PluginOperator defaultPluginOperator =  new DefaultPluginOperator(applicationContext,pluginManager,configuration,this.pluginListenerManager);
        return new PluginOperatorWrapper(defaultPluginOperator,configuration);
    }
}
