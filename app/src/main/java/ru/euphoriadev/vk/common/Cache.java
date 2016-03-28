package ru.euphoriadev.vk.common;

/**
 * Created by Igor on 09.03.16.
 * <p/>
 * The base interface for caching objects
 */
public interface Cache<K, V> {
    /**
     * Adds the specified object at the this cache,
     * If a cache does not yet contain an value
     *
     * @param key   the key of specified value
     * @param value the value to add
     * @return true if this cache is modified (value is added),
     *         false otherwise (cache contain an value)
     */
    boolean put(K key, V value);

    /**
     * Returns the value at specified key if this cache contain an it, null otherwise
     *
     * @param key the key for get value
     * @return value at key, or {@code null} if the value does not contain
     */
    V get(K key);

    /**
     * Removes value at the specified key
     *
     * @param key the key of the value to remove
     * @return true if this cache is modified (value is removed),
     *         false otherwise (cache contain an value)
     */
    boolean remove(K key);

    /**
     * Returns the current size of the cache, or {@link Integer#MAX_VALUE}
     * if the size of more
     */
    int size();

    /**
     * Remove all values from this cache
     */
    void clear();



}
