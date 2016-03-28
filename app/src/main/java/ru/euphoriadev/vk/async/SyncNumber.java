package ru.euphoriadev.vk.async;

/**
 * Created by Igor on 04.03.16.
 * <p/>
 * A Thread-save number class
 */
public class SyncNumber extends Number {
    private static final Object mLock = new Object();

    private volatile int mValue;


    public SyncNumber(int value) {
        this.mValue = value;
    }

    public int get() {
        synchronized (mLock) {
            return mValue;
        }
    }

    public void set(int value) {
        synchronized (mLock) {
            if (mValue != value) {
                mValue = value;
            }
        }
    }

    public int getAndSet(int value) {
        synchronized (mLock) {
            try {
                return mValue;
            } finally {
                if (mValue != value) {
                    mValue = value;
                }
            }
        }
    }

    public int incrementAndGet() {
        synchronized (mLock) {
            return ++mValue;
        }
    }

    public int getAndIncrement() {
        synchronized (mLock) {
            return mValue++;
        }
    }

    public int decrementAndGet() {
        synchronized (mLock) {
            return --mValue;
        }
    }

    public int getAndDecrement() {
        synchronized (mLock) {
            return mValue--;
        }
    }

    @Override
    public double doubleValue() {
        return (double) mValue;
    }

    @Override
    public float floatValue() {
        return (float) mValue;
    }

    @Override
    public int intValue() {
        return mValue;
    }

    @Override
    public long longValue() {
        return (long) mValue;
    }


}
