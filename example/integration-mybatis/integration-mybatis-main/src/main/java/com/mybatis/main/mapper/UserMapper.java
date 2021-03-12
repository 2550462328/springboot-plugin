package com.mybatis.main.mapper;

import com.mybatis.main.config.mybatis.InsertByMapLanguageDriver;
import com.mybatis.main.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * description
 *
 * @author starBlues
 * @version 1.0
 */
@Repository
public interface UserMapper {

    /**
     * 得到用户列表
     * @return List
     */
    List<User> getList();

    /**
     * 通过id得到用户
     * @param id id
     * @return User
     */
    @Select("select * from user where userid = #{id}")
    User getById(@Param("id") String id);

    @Insert("insert into user (#{map})")
    @Lang(InsertByMapLanguageDriver.class)
    int insertUser(@Param("map")Map<String,Object> map);
}
