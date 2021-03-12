package com.github.zhanghui.integration.pf4j.support.resolver;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.DependencyResolver;
import org.pf4j.PluginDescriptor;
import org.pf4j.VersionManager;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 11:11
 **/
@Slf4j
public class SortDependencyResolver extends DependencyResolver {

    private final List<String> sortInitPluginIds;

    public SortDependencyResolver(List<String> sortInitPluginIds, VersionManager versionManager) {
        super(versionManager);
        this.sortInitPluginIds = sortInitPluginIds;
    }

    @Override
    public Result resolve(List<PluginDescriptor> plugins) {
        Result resolveResult = super.resolve(plugins);
        if(sortInitPluginIds == null || sortInitPluginIds.isEmpty()){
            return resolveResult;
        }

        List<String> sortPluginIds = resolveResult.getSortedPlugins();
        List<String> filterSortPluginIds =  sortInitPluginIds.stream().filter(sortInitPluginId ->{
            boolean isExists = sortPluginIds.contains(sortInitPluginId);
            if(isExists){
                sortPluginIds.remove(sortInitPluginId);
            }
            return isExists;
        }).collect(Collectors.toList());

        if(!sortPluginIds.isEmpty()){
            filterSortPluginIds.addAll(sortPluginIds);
        }

        return getSortResult(resolveResult,filterSortPluginIds);
    }

    /**
     * 尝试反射替换内存中resolveResult中的sortedPlugins值
     * @param resolveResult
     * @param filterSortPluginIds
     * @return
     */
    private Result getSortResult(Result resolveResult,  List<String> filterSortPluginIds ){

        try {
            Field sortPluginIds = ReflectionUtils.findField(Result.class,"sortedPlugins");
            List<String> sortedPlugins = null;
            if(sortPluginIds != null){
                if(!sortPluginIds.isAccessible()){
                    sortPluginIds.setAccessible(true);
                }
                sortedPlugins = (List<String>)sortPluginIds.get(resolveResult);
            }
            if(sortedPlugins == null || sortedPlugins.isEmpty()){
                return resolveResult;
            }
            // 清空原值 替换新值
            sortedPlugins.clear();
            sortedPlugins.addAll(filterSortPluginIds);
        } catch (IllegalAccessException e) {
            log.error("获取解析插件中的[sortedPlugins]异常");
        }
        return resolveResult;
    }

}
