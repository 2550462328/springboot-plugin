package com.github.zhanghui.integration.configuration.parser.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.github.zhanghui.integration.configuration.IntegrationConfiguration;
import com.github.zhanghui.integration.configuration.parser.AbstractConfigurationParser;
import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * Description:
 * 将yaml文件解析成一个对象
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/8 10:18
 **/
public class YamlConfigurationParser extends AbstractConfigurationParser {

    private final YAMLFactory yamlFactory;
    private final ObjectMapper objectMapper;

    public YamlConfigurationParser(IntegrationConfiguration integrationConfiguration) {
        super(integrationConfiguration);
        this.yamlFactory = new YAMLFactory();
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    /**
     * 从resource中解析出configDefinitionClass的实体类
     *
     * @param resource plugin.yml文件
     * @param configDefinitionClass 被@ConfigDefinition注解的类
     * @return
     * @throws Exception
     */
    @Override
    protected Object parse(Resource resource, Class configDefinitionClass) throws Exception {
        InputStream inputStream = null;
        YAMLParser yamlParser = null;
        TreeTraversingParser treeTraversingParser = null;

        try {
            inputStream = resource.getInputStream();
            yamlParser = yamlFactory.createParser(inputStream);
            final JsonNode root = objectMapper.readTree(yamlParser);
            if(root == null){
                return configDefinitionClass.newInstance();
            }
            treeTraversingParser = new TreeTraversingParser(root);
            return objectMapper.readValue(treeTraversingParser,configDefinitionClass);
        }finally {
            if(treeTraversingParser != null){
                treeTraversingParser.close();
            }
            if(yamlParser != null){
                yamlParser.close();
            }
            if(inputStream != null){
                inputStream.close();
            }
        }

    }
}
