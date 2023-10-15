package com.alicp.jetcache.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.ReflectUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description
 * @author: zhangtong
 * @create: 2023/10/5 11:44 AM
 */
public class VirtualThreadUtil {

    private static Logger logger = LoggerFactory.getLogger(VirtualThreadUtil.class);

    private static final Boolean isVirtualThreadSupported;

    static {
        isVirtualThreadSupported = checkThreadSupported();
    }
    public static ExecutorService createExecuteor(){
        ExecutorService executorService = null;
        try {
            Method method = ReflectUtils.findDeclaredMethod(java.util.concurrent.Executors.class, "newVirtualThreadPerTaskExecutor", new Class[]{});
            if (method != null) {
                logger.info("Test Thread start with newVirtualThreadPerTaskExecutor(Virtual)");
                executorService = (ExecutorService) method.invoke(null);
            }
        }catch (Exception e){
            logger.warn("JDK version may < 19, this will be skip...");
            return null;
        }
        return executorService;
    }

    public static ExecutorService createExecuteor(String factryName){
        ExecutorService executorService = null;
        try {
            Method method = ReflectUtils.findDeclaredMethod(java.util.concurrent.Executors.class, "newThreadPerTaskExecutor", new Class[]{ThreadFactory.class});
            if (method != null) {
                logger.info("Test Thread start with newVirtualThreadPerTaskExecutor(Virtual)");
                executorService = (ExecutorService) method.invoke(null,createThreadFactory(true,factryName));
            }
        }catch (Exception e){
            logger.warn("JDK version may < 19, this will be skip...");
            return null;
        }
        return executorService;
    }
    public static boolean isVirtualThreadSupported() {
        return isVirtualThreadSupported;
    }

    private static boolean checkThreadSupported() {
        try {
            Method method = ReflectUtils.findDeclaredMethod(java.util.concurrent.Executors.class, "newVirtualThreadPerTaskExecutor", new Class[]{});
            return method != null;
        }catch (Exception e){
            return false;
        }
    }

    private static AtomicInteger threadCount = new AtomicInteger(0);
    public static ThreadFactory createThreadFactory(boolean isVirtual ,String factoryName){
        if(isVirtual && isVirtualThreadSupported()) {
            try {
                Method threadMehod = ReflectUtils.findDeclaredMethod(Thread.class, "ofVirtual", new Class[0]);
                Object threadObject = threadMehod.invoke(null);
                Class buiderClass = Class.forName("java.lang.Thread$Builder");
                Method setNameMehod = ReflectUtils.findDeclaredMethod(buiderClass, "name", new Class[]{String.class, Long.TYPE});
                threadObject = setNameMehod.invoke(threadObject, factoryName, 0L);
                Method factoryMehod = ReflectUtils.findDeclaredMethod(buiderClass, "factory", new Class[0]);
                Object factory = factoryMehod.invoke(threadObject);
                return (ThreadFactory) factory;
            } catch (Exception e) {
                logger.warn("JDK version may < 19, this will be skip...");
            }
        }
        return r -> {
            Thread t = new Thread(r, factoryName + threadCount.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
    }
}
