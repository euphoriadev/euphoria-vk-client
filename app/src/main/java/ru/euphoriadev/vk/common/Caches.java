package ru.euphoriadev.vk.common;

import java.io.Serializable;

/**
 * Created by Igor on 09.03.16.
 *
 * Static methods for operation on a {@link Cache}
 */
public class Caches {
    /** An immutable cache which does not store any values */
    public static final Cache EMPTY_CACHE = new EmptyCache();

    /**
     * Returns a type-safe empty cache, immutable {@link Cache}.
     *
     * @see #EMPTY_CACHE
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Cache<K, V> emptyCache() {
        return EMPTY_CACHE;
    }

    /**
     * Returns a wrapper on the specified {@link Cache} which synchronizes all
     * access to the {@link Cache}
     *
     * @param cache the cache to wrap in a synchronized
     */
    public static <K, V> Cache<K, V> syncCache(Cache<K, V> cache) {
        return new SyncCache<>(cache);
    }

    private static class EmptyCache implements Cache, Serializable {

        @Override public boolean put(Object key, Object value) {
            return false;
        }

        @Override public Object get(Object key) {
            return null;
        }

        @Override public boolean remove(Object key) {
            return false;
        }

        @Override public int size() {
            return 0;
        }

        @Override public void clear() {

        }
    }

    private static class SyncCache<K, V> implements Cache<K, V> {
        private Cache<K, V> mCache;
        private final Object mLock;


        public SyncCache(Cache<K, V> cache) {
            this.mCache = cache;
            this.mLock = new Object();
        }

        @Override
        public boolean put(K key, V value) {
            synchronized (mLock) {
                return mCache.put(key, value);
            }
        }

        @Override
        public V get(K key) {
            synchronized (mLock) {
                return mCache.get(key);
            }
        }

        @Override
        public boolean remove(K key) {
            synchronized (mLock) {
                return mCache.remove(key);
            }
        }

        @Override
        public int size() {
            synchronized (mLock) {
                return mCache.size();
            }
        }

        @Override
        public void clear() {
            synchronized (mLock) {
                mCache.clear();
            }
        }
    }
}
