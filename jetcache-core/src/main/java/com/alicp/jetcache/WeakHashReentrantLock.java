package com.alicp.jetcache;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Description
 * @author: zhangtong
 * @create: 2023/9/4 6:01 PM
 */
public class WeakHashReentrantLock {

    protected final Map<Object,WeakReference<?>> weakReentrantLockMap = new ConcurrentHashMap<>();
    protected final ReferenceQueue queue = new ReferenceQueue<>();

    protected final int MAX_SIZE = 4096;
    public WeakHashReentrantLock(){
    }

    public Lock reentrantLock(Object lockObj){
        return getLock(lockObj,LOCK_TYPE.NOMORE);
    }

    public Lock readLock(Object lockObj){
        return getLock(lockObj,LOCK_TYPE.READ);
    }

    public Lock writeLock(Object lockObj){
        return getLock(lockObj,LOCK_TYPE.WRITE);
    }

    protected Lock getLock(Object lockObj,LOCK_TYPE lockType){
        if(weakReentrantLockMap.size() > MAX_SIZE)
            clearRef();
        Lock lock = null;
        while(lock == null){
            if(lockType == LOCK_TYPE.NOMORE) {
                lock = (Lock) weakReentrantLockMap.computeIfAbsent(lockObj,
                        item -> new WeakReentrantLock(lockObj, new ReentrantLock(), queue)).get();
            }else {
                WeakReadWriteReentrantLock wk = (WeakReadWriteReentrantLock) weakReentrantLockMap.computeIfAbsent(lockObj,
                        item -> new WeakReadWriteReentrantLock(lockObj, new ReentrantReadWriteLock(), queue)
                );
                ReentrantReadWriteLock rk = wk.get();
                if(rk != null) {
                    if (lockType == LOCK_TYPE.READ) {
                        lock = rk.readLock();
                    } else {
                        lock = rk.writeLock() ;
                    }
                }
            }

            if(lock == null)
                clearRef();
        }
        return lock;
    }

    protected void clearRef(){
        Reference<?> ref;
        while ((ref = queue.poll()) != null){
            if(ref instanceof  LockObject) {
                LockObject lockObj = (LockObject) ref;
                weakReentrantLockMap.remove(lockObj.getObj());
            }
        }
    }

    protected static enum LOCK_TYPE{
        NOMORE,
        READ,
        WRITE
    }

    protected static interface LockObject{
        Object getObj();
    }

    protected static class WeakReentrantLock extends WeakReference<ReentrantLock> implements LockObject{

        private final Object lockObj;

        public WeakReentrantLock(Object lockObj,ReentrantLock reentrantLock,ReferenceQueue<ReentrantLock> queue) {
            super(reentrantLock,queue);
            this.lockObj = lockObj;
        }

        @Override
        public Object getObj() {
            return lockObj;
        }
    }

    protected static class WeakReadWriteReentrantLock extends WeakReference<ReentrantReadWriteLock> implements LockObject{

        private final Object lockObj;

        public WeakReadWriteReentrantLock(Object lockObj,ReentrantReadWriteLock reentrantReadWriteLock,ReferenceQueue<ReentrantReadWriteLock> queue) {
            super(reentrantReadWriteLock,queue);
            this.lockObj = lockObj;
        }

        @Override
        public Object getObj() {
            return lockObj;
        }
    }
}
