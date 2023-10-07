package com.alicp.jetcache.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @Description
 * @author: zhangtong
 * @create: 2023/10/6 3:27 PM
 */
public class ObjectPool<T> {
        private final ArrayBlockingQueue<T> queue;
        private final int size;
        private final ObjectFactory<T> factory;
        private static final Logger logger = LoggerFactory.getLogger(ObjectPool.class);

        public ObjectPool(int size, ObjectFactory<T> factory) {
            this.size = size;
            this.factory = factory;
            queue = new ArrayBlockingQueue<>(size);
            for (int i = 0; i < size; i++) {
                queue.add(factory.create());
            }
            logger.debug("Init the object pool with size {}", size);
        }

        public T borrowObject() {
            T t = queue.poll();
            if(t == null) {
                logger.debug("The pool is not enough, create a new object");
                return factory.create();
            }
            return t;
        }

        public void returnObject(T obj) {
            if (obj == null) {
                return;
            }
            factory.reset(obj);
            queue.offer(obj);
        }

        public interface ObjectFactory<T> {
            T create();
            void reset(T obj);
        }
}
