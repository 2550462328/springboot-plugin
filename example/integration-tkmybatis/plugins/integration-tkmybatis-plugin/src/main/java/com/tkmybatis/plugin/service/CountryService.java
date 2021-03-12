package com.tkmybatis.plugin.service;

import com.tkmybatis.plugin.entity.Country;
import com.tkmybatis.plugin.mapper.CountryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/9 15:48
 **/
@Service
public class CountryService {

    @Autowired
    private CountryMapper countryMapper;

    @Transactional(rollbackFor = Exception.class)
    public void testTrans(){
        countryMapper.insert(new Country(5,"123","333"));
        countryMapper.insert(new Country(6,"123","333"));

        int i =1/0;
        countryMapper.insert(new Country(7,"123","333"));
    }
}
