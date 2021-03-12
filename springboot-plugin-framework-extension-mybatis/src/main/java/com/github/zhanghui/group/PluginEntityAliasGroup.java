package com.github.zhanghui.group;

import com.github.zhanghui.integration.core.process.pre.classes.PluginClassGroup;
import com.github.zhanghui.mybatisplus.SpringBootMybatisPlusConfig;
import com.github.zhanghui.realize.BasePlugin;
import com.github.zhanghui.utils.AnnotationUtils;
import org.apache.ibatis.type.Alias;

import java.util.Set;

/**
 * Description:
 * 插件扩展 数据库中实体类的别名 分组器
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/2 16:24
 **/
public class PluginEntityAliasGroup implements PluginClassGroup {

    public static final String GROUP_ID = "plugin_mybatis_alias";

    @Override
    public String groupId() {
        return GROUP_ID;
    }

    //todo（后期看看是否需要在SpringMybatisConfig中定义）
    private Set<String> typeAliasPackages;

    @Override
    public void initialize(BasePlugin basePlugin) {
        if(basePlugin instanceof SpringBootMybatisPlusConfig){
            SpringBootMybatisPlusConfig mybatisPlusConfig = (SpringBootMybatisPlusConfig) basePlugin;
            typeAliasPackages = null;
        }
    }

    @Override
    public boolean filter(Class<?> aClass) {
        boolean isAnnotated = AnnotationUtils.hasAnnotation(aClass,false, Alias.class);
        if(isAnnotated){
            return true;
        }
        if(typeAliasPackages == null){
            return false;
        }
        for(String typeAliasPackage : typeAliasPackages){
            //todo(后期尝试在typeAlisaPackage中去找这个aClass)
            if(aClass.getPackage().getName().equals(typeAliasPackage)){
                return true;
            }
        }
        return false;
    }
}
