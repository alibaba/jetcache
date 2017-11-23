package com.alicp.jetcache.autoconfigure;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

public class ConfigTreeTest {

    private ConfigTree configTree;

    @Before
    public void setup() {
        MockEnvironment environment = new MockEnvironment();
        environment.withProperty("jetcache.remote.default.type", "redis.lettuce")
                .withProperty("jetcache.remote.default.keyConvertor", "fastjson")
                .withProperty("jetcache.remote.default.uri", "redis://127.0.0.1:6379/")
                .withProperty("jetcache.remote.A1.type", "redis.lettuce")
                .withProperty("jetcache.remote.A1.keyConvertor", "fastjson")
                .withProperty("jetcache.remote.A1.uri[0]", "redis://127.0.0.1:7000")
                .withProperty("jetcache.remote.A1.uri[1]", "redis://127.0.0.1:7001")
                .withProperty("jetcache.remote.A1.uri[2]", "redis://127.0.0.1:7002");
        configTree = new ConfigTree(environment, "jetcache.");
    }

    @Test
    public void testSubTree() {
        Assert.assertEquals(8, configTree.getProperties().size());
        Assert.assertEquals(3, configTree.subTree("remote.default").getProperties().size());
        Assert.assertEquals(5, configTree.subTree("remote.A1.").getProperties().size());
        Assert.assertEquals(1, configTree.subTree("remote.default.uri").getProperties().size());
        Assert.assertEquals(3, configTree.subTree("remote.A1.uri").getProperties().size());
    }

    @Test
    public void testContainsProperty() {
        Assert.assertTrue(configTree.containsProperty("remote.default.type"));
        Assert.assertTrue(configTree.containsProperty("remote.default.uri"));
    }

    @Test
    public void testGetProperty() {
        Assert.assertEquals("redis://127.0.0.1:6379/", configTree.getProperty("remote.default.uri"));
    }


}
