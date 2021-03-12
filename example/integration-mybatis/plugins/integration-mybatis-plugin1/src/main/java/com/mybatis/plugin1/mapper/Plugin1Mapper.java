package com.mybatis.plugin1.mapper;

import com.mybatis.main.config.mybatis.InsertByMapLanguageDriver;
import com.mybatis.plugin1.entity.Plugin1;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * description
 *
 * @author starBlues
 * @version 1.0
 */
@Mapper
public interface Plugin1Mapper {


    /**
     * 通过id获取数据
     * @param id id
     * @return Plugin2
     */
    Plugin1 getById(@Param("id") String id);


    List<Plugin1> getByCondition(Plugin1 plugin1);

    @Insert("INSERT INTO plugin1 VALUES (#{id}, #{name})")
    void insert(@Param("id") String id, @Param("name") String name);

    @Insert("insert into plugin1 (#{map})")
    @Lang(InsertByMapLanguageDriver.class)
    int insertPlugin(@Param("map") Map<String,Object> map);

}
