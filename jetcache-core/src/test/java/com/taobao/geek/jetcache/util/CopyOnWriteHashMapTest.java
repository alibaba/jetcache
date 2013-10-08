/**
 * Created on  13-10-08 11:17
 */
package com.taobao.geek.jetcache.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CopyOnWriteHashMapTest {
    CopyOnWriteHashMap<String, String> map = new CopyOnWriteHashMap<String, String>();
//    HashMap<String, String> map = new HashMap<String, String>();

    @Test
    public void test() throws Exception {

        class Status {
            boolean fail = false;
        }
        final Status s = new Status();
        class T extends Thread {
            String keyPrefix;
            transient boolean stop;

            T(String keyPrefix) {
                this.keyPrefix = keyPrefix;
            }

            @Override
            public void run() {
                try {
                    int i = 0;
                    while (!stop) {
                        i++;
                        if (i > 1000) {
                            i = 0;
                        }
                        String key = keyPrefix + i;
                        String value = i + "";
                        map.put(key, value);
                        if (map.get(key) == null) {
                            s.fail = true;
                        } else if (!map.get(key).equals(value)) {
                            s.fail = true;
                        }
                    }
                } catch (Throwable e) {
                    s.fail = true;
                }

            }
        }

        T t1 = new T("K1");
        T t2 = new T("K2");
        T t3 = new T("K3");
        T t4 = new T("K4");
        t1.start();
        t2.start();
        t3.start();
        t4.start();

        Thread.sleep(1000);
        t1.stop = true;
        t2.stop = true;
        t3.stop = true;
        t4.stop = true;

        if (s.fail) {
            Assert.fail();
        }
    }

}
