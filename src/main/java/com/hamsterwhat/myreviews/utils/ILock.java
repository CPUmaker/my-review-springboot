package com.hamsterwhat.myreviews.utils;

public interface ILock {
    /**
     * Try to get lock.
     * @param timeoutSeconds The time of key's ttl.
     * @return If true then succeed to get the lock, else failed to get the lock.
     */
    boolean tryLock(long timeoutSeconds);

    /**
     * Release lock.
     */
    void unlock();
}
