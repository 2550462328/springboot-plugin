package com.github.zhanghui.integration.core.process.pre.loader.bean;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Description:
 * 加载资源信息的类
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/6 10:50
 **/
public class ResourceWrapper {

    private final List<Resource> resources = new ArrayList<>();
    private final Set<String> classPackages = new HashSet<>();

    private final Map<String, Object> extensions = new HashMap<>();

    public void addResource(Resource resource){
        if(resource == null){
            return;
        }
        resources.add(resource);
    }

    public void addResources(List<Resource> resources){
        if(resources == null || resources.isEmpty()){
            return;
        }
        this.resources.addAll(resources);
    }

    public List<Resource> getResources(){
        return Collections.unmodifiableList(resources);
    }

    public void addClassPackageName(String classFullName){
        if(StringUtils.isEmpty(classFullName)){
            return;
        }
        classPackages.add(classFullName);
    }

    public void addClassPackageNames(Set<String> classPackageNames){
        if(classPackageNames == null || classPackageNames.isEmpty()){
            return;
        }
        this.classPackages.addAll(classPackageNames);
    }

    public Set<String> getClassPackageNames(){
        return Collections.unmodifiableSet(classPackages);
    }


    public void addExtension(String key, Object value) {
        extensions.put(key, value);
    }

    public Object getExtension(String key){
        return extensions.get(key);
    }
}
