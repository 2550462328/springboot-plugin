package com.mybatisplus.plugin.rest;

import com.mybatisplus.plugin.entity.PluginData;
import com.mybatisplus.plugin.mapper.PluginDataMapper;
import com.mybatisplus.plugin.service.PluginDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description
 *
 * @author starBlues
 * @version 1.0
 */
@RestController
@RequestMapping("pluginData")
public class PluginDataController {

    @Autowired
    private PluginDataService pluginDataService;

    @GetMapping
    public List<PluginData> getAll(){
        return pluginDataService.list();
    }

    @GetMapping("page/{size}/{currentPage}")
    public List<PluginData> getPage(@PathVariable("size") Long size,
                                    @PathVariable("currentPage") Long currentPage){
        return pluginDataService.getByPage(size, currentPage);
    }

    @GetMapping("all")
    public List<PluginData> getPage(){
        return pluginDataService.getTestAll();
    }

    @GetMapping("one")
    public PluginData getOne(){
        return pluginDataService.getTestOne();
    }

    @GetMapping("name/{name}")
    public List<PluginData> getByName(@PathVariable("name") String name){
        return pluginDataService.getByName(name);
    }

    @PostMapping()
    public Boolean save(@RequestBody PluginData pluginData){
        return pluginDataService.save(pluginData);
    }

    @DeleteMapping("{id}")
    public Boolean delete(@PathVariable("id") String id){
        return pluginDataService.removeById(id);
    }

}
