package com.mybatis.plugin2;

import com.github.zhanghui.SpringBootStaticResourceConfig;
import com.github.zhanghui.realize.BasePlugin;
import org.pf4j.PluginWrapper;

import java.util.HashSet;
import java.util.Set;

/**
 * description
 *
 * @author starBlues
 * @version 1.0
 */
public class ExamplePlugin2 extends BasePlugin implements SpringBootStaticResourceConfig {

    private final Set<String> locations = new HashSet<>();

    public ExamplePlugin2(PluginWrapper wrapper) {
        super(wrapper);
        locations.add("classpath:/static");
    }

    @Override
    protected void startEvent() {

    }

    @Override
    protected void deleteEvent() {

    }

    @Override
    protected void stopEvent() {

    }

    @Override
    public Set<String> locations() {
        return locations;
    }
}
