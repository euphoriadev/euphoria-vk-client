package ru.euphoriadev.vk.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Igor on 06.02.16.
 * <p/>
 * Utils for search in array
 */
public class ArrayUtil {
    /**
     * The index for linear and binary search, if the value is not found
     */
    public static final int VALUE_NOT_FOUND = -1;

    private ArrayUtil() {
        /* empty */
    }

    /**
     * Performs a linear search for value in the ascending array.
     * Beware that linear search returns only the first found element.
     *
     * @param array the array to search
     * @param value the value fo find
     * @return the non-negative index of value, or -1 if value not found
     */
    public static int linearSearch(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return VALUE_NOT_FOUND;
    }

    /**
     * Performs a linear search for value in the ascending array.
     * Beware that linear search returns only the first found element.
     *
     * @param array the array to search
     * @param value the value fo find
     * @return the non-negative index of value, or -1 if value not found
     */
    public static int linearSearch(long[] array, long value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return VALUE_NOT_FOUND;
    }

    /**
     * Performs a linear search for value in the ascending array.
     * Beware that linear search returns only the first found element.
     *
     * @param array the array to search
     * @param value the value fo find
     * @return the non-negative index of value, or -1 if value not found
     */
    public static int linearSearch(char[] array, char value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return VALUE_NOT_FOUND;
    }

    /**
     * Performs a linear search for value in the ascending array.
     * Beware that linear search returns only the first found element.
     *
     * @param array the array to search
     * @param value the value fo find
     * @return the non-negative index of value, or -1 if value not found
     */
    public static int linearSearch(short[] array, short value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return VALUE_NOT_FOUND;
    }

    /**
     * Performs a linear search for value in the ascending array.
     * Beware that linear search returns only the first found element.
     *
     * @param array the array to search
     * @param value the value fo find
     * @return the non-negative index of value, or -1 if value not found
     */
    public static int linearSearch(byte[] array, byte value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return VALUE_NOT_FOUND;
    }

    /**
     * Performs a binary search for value in the array. The array con be unsorted.
     * Beware that binary search returns only the first found element
     * <p/>
     * This algorithm differs from the classical binary search
     * is that, it is more optimized for unsorted arrays.
     * As the classic method - the search starts at the half-size of array
     * <p/>
     * NOTE: If array is sorted, use {@link Arrays#binarySearch(int[], int)}
     *
     * @param array the array to search
     * @param value the value fo find
     * @return the non-negative index of value, or -1 if value not found
     */
    public static int binarySearch(int[] array, int value) {
        int high = array.length - 1;
        int startIndex = high / 2;
        int index = startIndex;
        while (index >= 0 && index <= high) {
            if (array[index] == value) {
                return index;
            }

            if (index > startIndex) {
                index = index - ((index - startIndex) * 2);
            } else {
                index = index + ((startIndex - index) * 2) + 1;
            }
        }
        return VALUE_NOT_FOUND;
    }


    /**
     * Performs a binary search for value in the array. The array con be unsorted.
     * Beware that binary search returns only the first found element
     * <p/>
     * This algorithm differs from the classical binary search
     * is that, it is more optimized for unsorted arrays.
     * As the classic method - the search starts at the half-size of array
     * <p/>
     * NOTE: If array is sorted, use {@link Arrays#binarySearch(long[], long)}
     *
     * @param array the array to search
     * @param value the value fo find
     * @return the non-negative index of value, or -1 if value not found
     */
    public static int binarySearch(long[] array, long value) {
        int high = array.length - 1;
        int startIndex = high / 2;
        int index = startIndex;
        while (index >= 0 && index <= high) {
            if (array[index] == value) {
                return index;
            }

            if (index > startIndex) {
                index = index - ((index - startIndex) * 2);
            } else {
                index = index + ((startIndex - index) * 2) + 1;
            }
        }
        return VALUE_NOT_FOUND;
    }


    /**
     * Performs a binary search for value in the array. The array con be unsorted.
     * Beware that binary search returns only the first found element
     * <p/>
     * This algorithm differs from the classical binary search
     * is that, it is more optimized for unsorted arrays.
     * As the classic method - the search starts at the half-size of array
     * <p/>
     * NOTE: If array is sorted, use {@link Arrays#binarySearch(short[], short)}
     *
     * @param array the array to search
     * @param value the value fo find
     * @return the non-negative index of value, or -1 if value not found
     */
    public static int binarySearch(short[] array, short value) {
        int high = array.length - 1;
        int startIndex = high / 2;
        int index = startIndex;
        while (index >= 0 && index <= high) {
            if (array[index] == value) {
                return index;
            }

            if (index > startIndex) {
                index = index - ((index - startIndex) * 2);
            } else {
                index = index + ((startIndex - index) * 2) + 1;
            }
        }
        return VALUE_NOT_FOUND;
    }


    /**
     * Performs a binary search for value in the array. The array con be unsorted.
     * Beware that binary search returns only the first found element
     * <p/>
     * This algorithm differs from the classical binary search
     * is that, it is more optimized for unsorted arrays.
     * As the classic method - the search starts at the half-size of array
     * <p/>
     * NOTE: If array is sorted, use {@link Arrays#binarySearch(char[], char)}
     *
     * @param array the array to search
     * @param value the value fo find
     * @return the non-negative index of value, or -1 if value not found
     */
    public static int binarySearch(char[] array, char value) {
        int high = array.length - 1;
        int startIndex = high / 2;
        int index = startIndex;
        while (index >= 0 && index <= high) {
            if (array[index] == value) {
                return index;
            }

            if (index > startIndex) {
                index = index - ((index - startIndex) * 2);
            } else {
                index = index + ((startIndex - index) * 2) + 1;
            }
        }
        return VALUE_NOT_FOUND;
    }

    /**
     * Performs a binary search for value in the array. The array con be unsorted.
     * Beware that binary search returns only the first found element
     * <p/>
     * This algorithm differs from the classical binary search
     * is that, it is more optimized for unsorted arrays.
     * As the classic method - the search starts at the half-size of array
     * <p/>
     * NOTE: If array is sorted, use {@link Arrays#binarySearch(byte[], byte)}
     *
     * @param array the array to search
     * @param value the value fo find
     * @return the non-negative index of value, or -1 if value not found
     */
    public static int binarySearch(byte[] array, byte value) {
        int high = array.length - 1;
        int startIndex = high / 2;
        int index = startIndex;
        while (index >= 0 && index <= high) {
            if (array[index] == value) {
                return index;
            }

            if (index > startIndex) {
                index = index - ((index - startIndex) * 2);
            } else {
                index = index + ((startIndex - index) * 2) + 1;
            }
        }
        return VALUE_NOT_FOUND;
    }

    /**
     * Compares the two lists.
     *
     * @param list1 the first collection
     * @param list2 the second collection
     * @return true, if both list are have same length
     * and all elements at index is equal according to {@link Object#equals},
     * false otherwise
     */
    public static boolean equals(List list1, List list2) {
        if (list1 == list2) {
            return true;
        }

        if (list1.size() != list2.size()) {
            return false;
        }

        for (int i = 0; i < list1.size(); i++) {
            Object o1 = list1.get(i);
            Object o2 = list2.get(i);
            if (!(o1 == null ? o2 == null : o1.equals(o2))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the array has the specified value
     *
     * @param array the array to search specified value
     * @param value  the value to search for
     */
    public static boolean contains(int[] array, int value) {
        return linearSearch(array, value) != VALUE_NOT_FOUND;
    }

    public static int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        if (isEmpty(list)) {
            return array;
        }

        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * Returns true if the collection is null or empty
     *
     * @param collection the collection to be examined
     */
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

}
