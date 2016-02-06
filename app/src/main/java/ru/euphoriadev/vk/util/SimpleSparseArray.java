package ru.euphoriadev.vk.util;

import java.util.Arrays;

/**
 * Created by Igor on 05.02.16.
 * <p/>
 * Especially for {@link ResourcesLoader}.
 *
 * The thing is, SparseArray puts items randomly,
 * so I can't get the palette in the correct order
 * <p/>
 * Original code on {@link android.util.SparseIntArray}
 */
public class SimpleSparseArray implements Cloneable {
    private int[] mKeys;
    private int[] mValues;
    private int mSize;

    /**
     * Create a new SimpleSparseArray. The size in future cannot be changed
     */
    public SimpleSparseArray() {
        this(20);
    }

    /**
     * Create a new SimpleSparseArray with size. The size in future cannot be changed
     *
     * @param capacity the fixed capacity
     * @throws IllegalArgumentException when size < 0
     */
    public SimpleSparseArray(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity cannot be < 0");
        }
        mKeys = new int[capacity];
        mValues = new int[capacity];
        mSize = 0;
    }

    @Override
    public SimpleSparseArray clone() {
        SimpleSparseArray clone = null;
        try {
            clone = (SimpleSparseArray) super.clone();
            clone.mKeys = mKeys.clone();
            clone.mValues = mValues.clone();
        } catch (CloneNotSupportedException e) {
            /* ignore */
        }
        return clone;
    }

    /**
     * Adding a mapping from the specified key to the specified value
     *
     * @param key   the key for map to value
     * @param value the value for map to key
     * @throws RuntimeException when capacity is overflow
     */
    public void put(int key, int value) {
        if (mSize > mKeys.length) {
            throw new RuntimeException("Capacity is overflow");
        }
        mKeys[mSize] = key;
        mValues[mSize] = value;

        mSize++;
    }

    /**
     * Directly set the key at a particular index
     *
     * @param index the index at which to set the specified value
     * @param value the value to set
     */
    public void setValueAt(int index, int value) {
        mValues[index] = value;
    }

    /**
     * Directly set the key at a particular index
     *
     * @param index the index at which to set the specified value
     * @param key   the key to set
     */
    public void setKeyAt(int index, int key) {
        mKeys[index] = key;
    }

    /**
     * Remove key-value element at index
     *
     * @param index the index of the object to remove
     */
    public void removeAt(int index) {
        System.arraycopy(mKeys, index + 1, mKeys, index, mSize - (index + 1));
        System.arraycopy(mValues, index + 1, mValues, index, mSize - (index + 1));
        mSize--;
    }

    /**
     * Remove key-value element at value
     *
     * @param key the key to remove value
     */
    public void remove(int key) {
        int index = ArrayUtil.linearSearch(mKeys, key);

        if (index >= 0) {
            removeAt(index);
        }
    }

    /**
     * Get the int value mapped from the specified key
     *
     * @param key the key for to search
     * @return int value mapped from specified key
     */
    public int get(int key) {
        return get(key, 0);
    }

    /**
     * Get the int value mapped from the specified key
     *
     * @param key      the key for to search
     * @param defValue the value for key not found
     * @return int value mapped from specified key
     */
    public int get(int key, int defValue) {
        int index = ArrayUtil.linearSearch(mKeys, key);
        if (index >= 0) {
            return mValues[index];
        }

        return defValue;
    }

    /**
     * Returns the number of elements in this SimpleSparseArray
     */
    public int size() {
        return mSize;
    }

    /**
     * Get value at index.
     *
     * @param index the index to find value
     */
    public int valueAt(int index) {
        return mValues[index];
    }

    /**
     * Returns whether this map contains the specified key
     *
     * @param key the key to search
     * @return true, if this contains key
     */
    public boolean containsKey(int key) {
        return ArrayUtil.linearSearch(mKeys, key) != ArrayUtil.VALUE_NOT_FOUND;
    }

    /**
     * * Returns whether this map contains the specified value
     *
     * @param value the value to search
     * @return true, if this contains value
     */
    public boolean containsValue(int value) {
        return ArrayUtil.linearSearch(mValues, value) != ArrayUtil.VALUE_NOT_FOUND;
    }

    /**
     * Returns true if size is zero
     */
    public boolean isEmpty() {
        return mSize == 0;
    }

    /**
     * Remove all keys and values from this. Leaving it empty
     */
    public void clear() {
        Arrays.fill(mKeys, 0);
        Arrays.fill(mValues, 0);
        mSize = 0;
    }


}
