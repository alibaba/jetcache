/**
 * Created on  13-10-30 21:27
 */
package com.alicp.jetcache.local;

import java.lang.ref.SoftReference;
import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
//TODO BUGFIX
public class LirsCache extends AbstractLocalCache {

    public LirsCache(){
    }

    public LirsCache(boolean useSoftRef){
        super(useSoftRef);
    }

    @Override
    protected AreaCache createAreaCache(int localLimit) {
        int maxHirSize;
        if (localLimit <= 2) {
            throw new IllegalArgumentException("localLimit must greater than 2");
        } else if (localLimit < 10) {
            maxHirSize = 1;
        } else if (localLimit < 20) {
            maxHirSize = 2;
        } else {
            maxHirSize = (int) (localLimit * 0.1);
        }
        return new LirsAreaCache(localLimit - maxHirSize, maxHirSize, localLimit * 2, useSoftRef);
    }

    static class LirsAreaCache implements AreaCache {

        private final int MAX_LIR_SIZE;
        private final int MAX_HIR_SIZE;
        private final int MAX_STACK_S_SIZE;
        private final boolean useSoftRef;

        private boolean inited;
        private int lirSize;
        private int hirSize;
        private int stackSize;

        private HashMap<String, ValueEntry> m;

        private ValueEntry sStackHeader;
        private ValueEntry qListHeader;

        public LirsAreaCache(int sizeOfLirBlock, int sizeOfHirBlock, int sizeOfStackS, boolean useSoftRef) {
            MAX_LIR_SIZE = sizeOfLirBlock;
            MAX_HIR_SIZE = sizeOfHirBlock;
            MAX_STACK_S_SIZE = sizeOfStackS;
            this.useSoftRef = useSoftRef;
            int initCapacity = (int) (1.5f * (MAX_LIR_SIZE + MAX_HIR_SIZE));
            m = new HashMap<String, ValueEntry>(initCapacity, 0.75f);
            sStackHeader = new ValueEntry();
            sStackHeader.sBefore = sStackHeader.sAfter = sStackHeader;
            qListHeader = new ValueEntry();
            qListHeader.qBefore = qListHeader.qAfter = qListHeader;
        }

        public synchronized Object getValue(String key) {
            ValueEntry valueEntry = m.get(key);
            if (valueEntry == null || valueEntry.value == null) {
                return null;
            }
            if (valueEntry.lirStatus) {
                onLirHit(valueEntry);
            } else {
                onHirHit(valueEntry);
            }
            if (useSoftRef) {
                return ((SoftReference) valueEntry.value).get();
            } else {
                return valueEntry.value;
            }
        }

        private void onHirHit(ValueEntry valueEntry) {
            if (valueEntry.sAfter != null) { //in stack S
                valueEntry.lirStatus = true;
                removeFromStackS(valueEntry);
                addToTopOfStackS(valueEntry);
                removeFromListQ(valueEntry);

                ValueEntry bottom = sStackHeader.sAfter;
                bottom.lirStatus = false;
                removeFromStackS(bottom);
                stackSize--;
                addToEndOfListQ(bottom);
                doStackPruning();
            } else {
                addToTopOfStackS(valueEntry);
                stackSize++;
                removeFromListQ(valueEntry);
                addToEndOfListQ(valueEntry);
            }
        }

        private void onLirHit(ValueEntry valueEntry) {
            boolean bottom = valueEntry.sBefore == sStackHeader;
            removeFromStackS(valueEntry);
            addToTopOfStackS(valueEntry);
            if (bottom) {
                doStackPruning();
            }
        }

        private void onMiss(ValueEntry valueEntry, String key, Object value) {
            if (!inited) {
                valueEntry = new ValueEntry();
                valueEntry.key = key;
                setValue(valueEntry, value);
                m.put(key, valueEntry);
                if (lirSize < MAX_LIR_SIZE) {
                    valueEntry.lirStatus = true;
                    addToTopOfStackS(valueEntry);
                    lirSize++;
                    stackSize++;
                } else if (hirSize < MAX_HIR_SIZE) {
                    addToTopOfStackS(valueEntry);
                    addToEndOfListQ(valueEntry);
                    stackSize++;
                    inited = ++hirSize == MAX_HIR_SIZE;
                } else {
                    assert false;
                }
            } else {
                ValueEntry front = qListHeader.qAfter;
                removeFromListQ(front);
                if (front.sAfter == null) {
                    m.remove(front.key);
                }
                if (valueEntry == null) {
                    valueEntry = new ValueEntry();
                    valueEntry.key = key;
                }
                setValue(valueEntry, value);
                if (valueEntry.sAfter != null) {
                    valueEntry.lirStatus = true;
                    removeFromStackS(valueEntry);
                    addToTopOfStackS(valueEntry);

                    ValueEntry bottom = sStackHeader.sAfter;
                    bottom.lirStatus = false;
                    removeFromStackS(bottom);
                    stackSize--;
                    addToEndOfListQ(bottom);
                    doStackPruning();
                } else {
                    addToTopOfStackS(valueEntry);
                    stackSize++;
                    addToEndOfListQ(valueEntry);
                }
            }
        }

        private void setValue(ValueEntry valueEntry, Object value) {
            if (useSoftRef) {
                valueEntry.value = new SoftReference(value);
            } else {
                valueEntry.value = value;
            }
        }

        private void addToTopOfStackS(ValueEntry valueEntry) {
            valueEntry.sAfter = sStackHeader;
            valueEntry.sBefore = sStackHeader.sBefore;
            sStackHeader.sBefore.sAfter = valueEntry;
            sStackHeader.sBefore = valueEntry;
        }

        private void removeFromStackS(ValueEntry valueEntry) {
            valueEntry.sBefore.sAfter = valueEntry.sAfter;
            valueEntry.sAfter.sBefore = valueEntry.sBefore;
            valueEntry.sAfter = valueEntry.sBefore = null;
        }

        private void removeFromListQ(ValueEntry valueEntry) {
            valueEntry.qBefore.qAfter = valueEntry.qAfter;
            valueEntry.qAfter.qBefore = valueEntry.qBefore;
            valueEntry.qAfter = valueEntry.qBefore = null;
        }

        private void addToEndOfListQ(ValueEntry valueEntry) {
            valueEntry.qAfter = qListHeader;
            valueEntry.qBefore = qListHeader.qBefore;
            qListHeader.qBefore.qAfter = valueEntry;
            qListHeader.qBefore = valueEntry;
        }

        private void doStackPruning() {
            ValueEntry e;
            for (e = sStackHeader.sAfter; e != sStackHeader && !e.lirStatus; e = e.sAfter) {
                removeFromStackS(e);
                stackSize--;
                if (e.value == null) {
                    m.remove(e.key);
                }
            }
        }

        public synchronized Object putValue(String key, Object value) {
            ValueEntry valueEntry = m.get(key);
            Object oldValue = null;
            if (valueEntry != null && valueEntry.value != null) {
                if (valueEntry.lirStatus) {
                    onLirHit(valueEntry);
                } else {
                    onHirHit(valueEntry);
                }

                if (useSoftRef) {
                    oldValue = ((SoftReference) valueEntry.value).get();
                    valueEntry.value = new SoftReference(value);
                } else {
                    oldValue = valueEntry.value;
                    valueEntry.value = value;
                }
            } else {
                onMiss(valueEntry, key, value);
            }
            return oldValue;
        }

        public synchronized Object removeValue(String key) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private static class ValueEntry {
        boolean lirStatus = false;
        ValueEntry sBefore;
        ValueEntry sAfter;
        ValueEntry qBefore;
        ValueEntry qAfter;
        String key;
        Object value;
    }

}
