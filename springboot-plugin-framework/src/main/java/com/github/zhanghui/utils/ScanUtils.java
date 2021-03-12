package com.github.zhanghui.utils;

import org.pf4j.PluginWrapper;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description:
 * 包扫描工具类
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/6 14:44
 **/
public class ScanUtils {

    /**
     * dev环境下对插件包类的加载(非打包jar)
     * @param basePackage
     * @param baseClass
     * @return
     * @throws IOException
     */
    public static Set<String> scanClassPackageName(String basePackage, Class baseClass) throws IOException{
        String osName = System.getProperty("os.name");
        if(osName.startsWith("Windows")){
            return scanPackagesInWindows(basePackage,baseClass);
        }else{
            return scanPackagesInOther(baseClass);
        }
    }

    /**
     * deploy环境下对插件包类的加载(从jar包中加载类)
     *
     * @param basePackage
     * @param pluginWrapper
     * @return
     * @throws IOException
     */
    public static Set<String> scanClassPackageName(String basePackage, PluginWrapper pluginWrapper) throws IOException {
        String pluginPath = pluginWrapper.getPluginPath().toString();
        Set<String> scanPackageClasses = new HashSet<>();
        JarFile jarFile = new JarFile(pluginPath);
        Enumeration<JarEntry> entryEnumeration = jarFile.entries();
        while(entryEnumeration.hasMoreElements()){
            JarEntry jarEntry = entryEnumeration.nextElement();
            String jarEntryName = jarEntry.getName();
            if(jarEntryName.contains(".class") && jarEntryName.replaceAll("/","").startsWith(basePackage)){
                String scanClassName = jarEntryName.substring(0,jarEntryName.lastIndexOf(".")).replace("/",".");
                scanPackageClasses.add(scanClassName);
            }
        }
        return scanPackageClasses;
    }

    /**
     * Windows环境
     * 扫描baseClass（获取绝对位置） + basePackage（相对位置）下的class文件
     * @param basePackage
     * @param baseClass
     * @return 类全路径(形如：com.zhanghui.User)
     * @throws IOException
     */
    private static Set<String> scanPackagesInWindows(String basePackage, Class baseClass) throws IOException{
        String classPath = baseClass.getResource("/").getPath();

        final String classPathTransfer =classPath.replaceAll("/", Matcher.quoteReplacement(File.separator)).replaceFirst("\\\\","");

        basePackage = basePackage.replaceAll("\\.", Matcher.quoteReplacement(File.separator));

        String startPath = classPathTransfer + basePackage;

        return filterPath(startPath).map(path -> {
            String pathString = path.toString();
            return pathString.replace(classPathTransfer,"")
                    .replace("\\",".")
                    .replace(".class","");
        }).collect(Collectors.toSet());

    }

    /**
     * Linux / Unix 环境
     * 扫描基于baseClass（可以获得绝对位置 + 相对位置）下的class文件
     * @param baseClass
     * @return 类全路径(形如：com.zhanghui.User)
     * @throws IOException
     */
    private static Set<String> scanPackagesInOther(Class baseClass) throws IOException{
        String classPath = baseClass.getResource("/").getPath();

        String startPath =classPath + ClassUtils.classPackageAsResourcePath(baseClass);

        return filterPath(startPath).map(path -> {
            String pathString  = path.toString();
            pathString = pathString.replace(classPath,"")
                    .replace(".class","");
            return ClassUtils.convertClassNameToResourcePath(pathString);
        }).collect(Collectors.toSet());

    }

    /**
     * 遍历出startPath下的所有的class文件
     *
     * @param startPath
     * @return
     * @throws IOException
     */
    private static Stream<Path> filterPath(String startPath) throws IOException {
        return Files.walk(Paths.get(startPath))
                .filter(Objects::nonNull)
                .filter(Files::isRegularFile)
                .filter(path ->{
                    Path fileName = path.getFileName();
                    if(fileName == null){
                        return false;
                    }
                    return fileName.toString().endsWith(".class");
                });
    }

}
