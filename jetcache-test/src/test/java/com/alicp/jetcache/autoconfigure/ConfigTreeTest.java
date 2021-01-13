package com.alicp.jetcache.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.Set;

public class ConfigTreeTest {

    private ConfigTree configTree;

    @BeforeEach
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
        assertEquals(8, configTree.getProperties().size());
        assertEquals(3, configTree.subTree("remote.default").getProperties().size());
        assertEquals(5, configTree.subTree("remote.A1.").getProperties().size());
        assertEquals(1, configTree.subTree("remote.default.uri").getProperties().size());
        assertEquals(3, configTree.subTree("remote.A1.uri").getProperties().size());
    }

    @Test
    public void testContainsProperty() {
        assertTrue(configTree.containsProperty("remote.default.type"));
        assertTrue(configTree.containsProperty("remote.default.uri"));
    }

    @Test
    public void testGetProperty() {
        assertEquals("redis://127.0.0.1:6379/", configTree.getProperty("remote.default.uri"));
    }

    @Test
    public void testDirectChildrenKeys() {
        Set<String> children = configTree.directChildrenKeys();
        assertEquals(1, children.size());
        assertTrue( children.contains("remote"));
        children = configTree.subTree("remote.").directChildrenKeys();
        assertEquals(2, children.size());
        assertTrue( children.contains("default"));
        assertTrue( children.contains("A1"));
        children = configTree.subTree("notexists.").directChildrenKeys();
        assertEquals(0, children.size());
    }


}
