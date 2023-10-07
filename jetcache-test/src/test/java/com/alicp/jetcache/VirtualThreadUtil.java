package com.alicp.jetcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.ReflectUtils;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @author: zhangtong
 * @create: 2023/10/5 11:44 AM
 */
public class VirtualThreadUtil {

    private static Logger logger = LoggerFactory.getLogger(VirtualThreadUtil.class);
    public static ExecutorService createExecuteor(){
        ExecutorService executorService = null;
        try {
            Method method = ReflectUtils.findDeclaredMethod(java.util.concurrent.Executors.class, "newVirtualThreadPerTaskExecutor", new Class[]{});
            if (method != null) {
                logger.info("Test Thread start with newVirtualThreadPerTaskExecutor(Virtual)");
                executorService = (ExecutorService) method.invoke(null);
            }
        }catch (Exception e){
            logger.warn("JDK version may < 19, The Test will be skip...");
            return null;
        }
        return executorService;
    }
}
