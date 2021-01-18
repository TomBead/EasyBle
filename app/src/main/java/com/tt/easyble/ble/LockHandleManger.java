package com.tt.easyble.ble;


import java.util.HashMap;


/**
 * 管理数据分发，可能同一时间连接的不止一个设备
 */
public class LockHandleManger {


    private HashMap<String, LockHandle> lockHandleHashMap = new HashMap<>();

    LockHandleManger() {
    }

    void addLockHandle(LockHandle lockHandle) {
        lockHandleHashMap.put(lockHandle.getName(), lockHandle);
    }

    void removeLockHandle(LockHandle lockHandle) {
        lockHandleHashMap.remove(lockHandle.getName());
    }


    void connectSuccess() {
        for (String key : lockHandleHashMap.keySet()) {
            lockHandleHashMap.get(key).connectSuccess();
        }
    }


    void connectFail() {
        for (String key : lockHandleHashMap.keySet()) {
            lockHandleHashMap.get(key).connectFail();
        }
    }

    void connectTimeOut() {
        for (String key : lockHandleHashMap.keySet()) {
            lockHandleHashMap.get(key).writeTimeOut();
        }
    }

    void handleMsg(String hexString, byte[] value) {
        for (String key : lockHandleHashMap.keySet()) {
            lockHandleHashMap.get(key).handleMsg(hexString, value);
        }
    }
}
