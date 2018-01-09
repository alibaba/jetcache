/**
 * Created on 2018/1/8.
 */
package com.alicp.jetcache;

import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@SuppressWarnings("unchecked")
public class CacheTest {

    private Cache cache;
    private Cache concreteCache;
    private int tryLockUnlockCount = 2;
    private int tryLockInquiryCount = 2;
    private int tryLockLockCount = 2;

    private Answer delegateAnswer = (invocation) -> invocation.getMethod().invoke(concreteCache, invocation.getArguments());

    @BeforeEach
    public void setup() {
        cache = mock(Cache.class);
        concreteCache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder().buildCache();
        when(cache.tryLock(any(), anyLong(), any())).thenCallRealMethod();
        when(cache.PUT_IF_ABSENT(any(), any(), anyLong(), any())).then(delegateAnswer);
        when(cache.REMOVE(any())).then(delegateAnswer);
        when(cache.GET(any())).then(delegateAnswer);
        when(cache.config()).thenReturn(new CacheConfig() {
            @Override
            public int getTryLockInquiryCount() {
                return tryLockInquiryCount;
            }

            @Override
            public int getTryLockLockCount() {
                return tryLockLockCount;
            }

            @Override
            public int getTryLockUnlockCount() {
                return tryLockUnlockCount;
            }
        });
    }

    @Test
    public void testTryLock_Null() {
        assertNull(cache.tryLock(null, 1, TimeUnit.HOURS));
    }

    @Test
    public void testTryLock_Success() {
        AutoReleaseLock lock = cache.tryLock("key", 1, TimeUnit.HOURS);
        assertNotNull(lock);
        assertNotNull(concreteCache.get("key"));
    }

    @Test
    public void testTryLock_LockRetry1() {
        when(cache.PUT_IF_ABSENT(any(), any(), anyLong(), any())).thenReturn(CacheResult.FAIL_WITHOUT_MSG)
            .then(delegateAnswer);
        AutoReleaseLock lock = cache.tryLock("key", 1, TimeUnit.HOURS);
        assertNotNull(lock);
    }

    @Test
    public void testTryLock_LockRetry2() {
        when(cache.PUT_IF_ABSENT(any(), any(), anyLong(), any())).thenReturn(CacheResult.FAIL_WITHOUT_MSG)
                .thenReturn(CacheResult.PART_SUCCESS_WITHOUT_MSG);
        AutoReleaseLock lock = cache.tryLock("key", 1, TimeUnit.HOURS);
        assertNull(lock);
    }

    @Test
    public void testTryLock_LockAndGetRetry1() {
        when(cache.PUT_IF_ABSENT(any(), any(), anyLong(), any())).thenReturn(CacheResult.FAIL_WITHOUT_MSG);
        AutoReleaseLock lock = cache.tryLock("key", 1, TimeUnit.HOURS);
        assertNull(lock);
    }
    @Test
    public void testTryLock_LockAndGetRetry2() {
        when(cache.PUT_IF_ABSENT(any(), any(), anyLong(), any())).thenReturn(CacheResult.FAIL_WITHOUT_MSG);
        concreteCache.put("key", "other value");
        AutoReleaseLock lock = cache.tryLock("key", 1, TimeUnit.HOURS);
        assertNull(lock);
    }
    @Test
    public void testTryLock_LockAndGetRetry3() {
        when(cache.PUT_IF_ABSENT(any(), any(), anyLong(), any())).then((i)->{
            i.getMethod().invoke(concreteCache, i.getArguments());
            return CacheResult.FAIL_WITHOUT_MSG;
        });
        AutoReleaseLock lock = cache.tryLock("key", 1, TimeUnit.HOURS);
        assertNotNull(lock);
    }
    @Test
    public void testTryLock_LockAndGetRetry4() {
        when(cache.PUT_IF_ABSENT(any(), any(), anyLong(), any())).then((i)->{
            i.getMethod().invoke(concreteCache, i.getArguments());
            return CacheResult.FAIL_WITHOUT_MSG;
        });
        when(cache.GET(any())).thenReturn(new CacheGetResult(CacheResultCode.FAIL,null,null))
                .then(delegateAnswer);
        AutoReleaseLock lock = cache.tryLock("key", 1, TimeUnit.HOURS);
        assertNotNull(lock);
    }
    @Test
    public void testTryLock_LockAndGetRetry5() {
        when(cache.PUT_IF_ABSENT(any(), any(), anyLong(), any())).then((i)->{
            i.getMethod().invoke(concreteCache, i.getArguments());
            return CacheResult.FAIL_WITHOUT_MSG;
        });
        when(cache.GET(any())).thenReturn(new CacheGetResult(CacheResultCode.FAIL,null,null));
        AutoReleaseLock lock = cache.tryLock("key", 1, TimeUnit.HOURS);
        assertNull(lock);
    }


    @Test
    public void testTryLock_Unlock1() {
        AutoReleaseLock lock = cache.tryLock("key", 1, TimeUnit.HOURS);
        lock.close();
        assertEquals(CacheResultCode.NOT_EXISTS, concreteCache.GET("key").getResultCode());
    }
    @Test
    public void testTryLock_Unlock2() {
        when(cache.REMOVE(any())).thenReturn(CacheResult.FAIL_WITHOUT_MSG).then(delegateAnswer);
        AutoReleaseLock lock = cache.tryLock("key", 1, TimeUnit.HOURS);
        lock.close();
        assertEquals(CacheResultCode.NOT_EXISTS, concreteCache.GET("key").getResultCode());
    }
    @Test
    public void testTryLock_Unlock3() {
        when(cache.REMOVE(any())).thenReturn(CacheResult.FAIL_WITHOUT_MSG).thenReturn(CacheResult.PART_SUCCESS_WITHOUT_MSG);
        AutoReleaseLock lock = cache.tryLock("key", 1, TimeUnit.HOURS);
        lock.close();
        assertNotNull(concreteCache.get("key"));
    }
    @Test
    public void testTryLock_Unlock4() {
        when(cache.REMOVE(any())).thenReturn(CacheResult.EXISTS_WITHOUT_MSG);
        AutoReleaseLock lock = cache.tryLock("key", 1, TimeUnit.HOURS);
        lock.close();
        assertNotNull(concreteCache.get("key"));
    }
    @Test
    public void testTryLock_Unlock5() throws Exception {
        when(cache.PUT_IF_ABSENT(any(), any(), anyLong(), any())).then(i ->
                //change expire time
            concreteCache.PUT_IF_ABSENT("key", i.getArgument(1).toString(), 100, TimeUnit.HOURS)
        );
        AutoReleaseLock lock = cache.tryLock("key", 1, TimeUnit.MILLISECONDS);
        Thread.sleep(2);
        lock.close();
        assertNotNull(concreteCache.GET("key"));
    }

}
