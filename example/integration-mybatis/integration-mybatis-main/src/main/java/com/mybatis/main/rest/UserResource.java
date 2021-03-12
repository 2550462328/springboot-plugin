package com.mybatis.main.rest;

import com.mybatis.main.entity.User;
import com.mybatis.main.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * description
 *
 * @author starBlues
 * @version 1.0
 */
@RestController
@RequestMapping("/user")
public class UserResource {

    @Autowired
    private UserMapper userMapper;


    @GetMapping()
    public List<User> getUsers(){
        return userMapper.getList();
    }

    @PostMapping
    public Integer saveUsers(@RequestBody Map<String, Object> map){
        return userMapper.insertUser(map);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable("id") String id){
        return userMapper.getById(id);
    }

}
