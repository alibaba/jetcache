package com.alicp.jetcache.template;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.external.ExternalCacheConfig;
import com.alicp.jetcache.external.MockRemoteCacheBuilder;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.CacheMessage;
import com.alicp.jetcache.test.anno.TestUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotifyMonitorInstallerTest {

    @Test
    public void testConcurrentCacheCreationSharesBroadcastManager() throws Exception {
        AtomicInteger created = new AtomicInteger();
        AtomicInteger subscribed = new AtomicInteger();
        AtomicInteger closed = new AtomicInteger();
        AtomicInteger builderRequests = new AtomicInteger();
        CountDownLatch firstSubscriptionStarted = new CountDownLatch(1);
        CountDownLatch secondInitializerReady = new CountDownLatch(1);
        CountDownLatch secondSubscriptionStarted = new CountDownLatch(1);

        MockRemoteCacheBuilder<?> remoteBuilder = new MockRemoteCacheBuilder.MockRemoteCacheBuilderImpl() {
            @Override
            public BroadcastManager createBroadcastManager(CacheManager cacheManager) {
                created.incrementAndGet();
                return new BroadcastManager(cacheManager) {
                    @Override
                    public CacheResult publish(CacheMessage cacheMessage) {
                        return CacheResult.SUCCESS_WITHOUT_MSG;
                    }

                    @Override
                    public void startSubscribe() {
                        if (subscribed.incrementAndGet() == 1) {
                            firstSubscriptionStarted.countDown();
                            try {
                                assertTrue(secondInitializerReady.await(5, TimeUnit.SECONDS));
                                // Keep initialization in progress while the other cache is created.
                                // A serialized initializer cannot start a second subscription.
                                secondSubscriptionStarted.await(1, TimeUnit.SECONDS);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new IllegalStateException(e);
                            }
                        } else {
                            secondSubscriptionStarted.countDown();
                        }
                    }

                    @Override
                    public void close() {
                        closed.incrementAndGet();
                    }
                };
            }
        };
        GlobalCacheConfig config = TestUtil.createGloableConfig();
        ExternalCacheConfig original = ((ExternalCacheBuilder<?>) config.getRemoteCacheBuilders()
                .get(CacheConsts.DEFAULT_AREA)).getConfig();
        remoteBuilder.setKeyConvertor(original.getKeyConvertor());
        remoteBuilder.setValueEncoder(original.getValueEncoder());
        remoteBuilder.setValueDecoder(original.getValueDecoder());
        remoteBuilder.setBroadcastChannel(original.getBroadcastChannel());
        config.getRemoteCacheBuilders().put(CacheConsts.DEFAULT_AREA, remoteBuilder);

        CacheBuilderTemplate template = new CacheBuilderTemplate(false,
                config.getLocalCacheBuilders(), config.getRemoteCacheBuilders());
        template.getCacheMonitorInstallers().add(new NotifyMonitorInstaller(area -> {
            if (builderRequests.incrementAndGet() == 2) {
                secondInitializerReady.countDown();
            }
            return remoteBuilder;
        }));
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCacheBuilderTemplate(template);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Cache<Object, Object>> first = executor.submit(() -> cacheManager.getOrCreateCache(
                    QuickConfig.newBuilder("first").cacheType(CacheType.BOTH).syncLocal(true).build()));
            assertTrue(firstSubscriptionStarted.await(5, TimeUnit.SECONDS));
            Future<Cache<Object, Object>> second = executor.submit(() -> cacheManager.getOrCreateCache(
                    QuickConfig.newBuilder("second").cacheType(CacheType.BOTH).syncLocal(true).build()));
            assertEquals(1, first.get(10, TimeUnit.SECONDS).config().getMonitors().size());
            assertEquals(1, second.get(10, TimeUnit.SECONDS).config().getMonitors().size());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
            cacheManager.close();
        }
        assertEquals(1, created.get());
        assertEquals(1, subscribed.get());
        assertEquals(1, closed.get());
    }
}
