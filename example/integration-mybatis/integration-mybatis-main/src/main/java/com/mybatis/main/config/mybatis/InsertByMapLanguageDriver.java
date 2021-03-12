package com.mybatis.main.config.mybatis;

import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/1/29 10:14
 **/
public class InsertByMapLanguageDriver extends XMLLanguageDriver {

    private static final Pattern pattern = Pattern.compile("\\(#\\{(\\w+)\\}\\)");
    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        Matcher matcher = pattern.matcher(script);
        if(matcher.find()){
            String field = "<foreach collection = \"$1\" index = \"_key\" separator=\",\">\\${_key}</foreach>";
            String value = "<foreach collection = \"$1\" item = \"_value\" separator=\",\">#{_value}</foreach>";

            script = matcher.replaceAll("("+field+") VALUES (" + value + ")");
        }
        script = "<script>" + script + "</script>";
        return super.createSqlSource(configuration, script, parameterType);
    }

}
