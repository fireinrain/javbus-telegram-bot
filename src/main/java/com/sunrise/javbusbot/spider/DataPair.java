package com.sunrise.javbusbot.spider;


import java.io.Serializable;

/**
 * @author : fireinrain
 * @description:
 * @site : https://github.com/fireinrain
 * @file : DataDataPair
 * @software: IntelliJ IDEA
 * @time : 2022/10/20 12:47 PM
 */

public class DataPair<K, V> implements Serializable {

    /**
     * Key of this <code>DataPair</code>.
     */
    private K key;

    /**
     * Gets the key for this DataPair.
     *
     * @return key for this DataPair
     */
    public K getKey() {
        return key;
    }

    /**
     * Value of this this <code>DataPair</code>.
     */
    private V value;

    /**
     * Gets the value for this DataPair.
     *
     * @return value for this DataPair
     */
    public V getValue() {
        return value;
    }

    /**
     * Creates a new DataPair
     *
     * @param key   The key for this DataPair
     * @param value The value to use for this DataPair
     */
    public DataPair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * <p><code>String</code> representation of this
     * <code>DataPair</code>.</p>
     *
     * <p>The default name/value delimiter '=' is always used.</p>
     *
     * @return <code>String</code> representation of this <code>DataPair</code>
     */
    @Override
    public String toString() {
        return key + "=" + value;
    }

    /**
     * <p>Generate a hash code for this <code>DataPair</code>.</p>
     *
     * <p>The hash code is calculated using both the name and
     * the value of the <code>DataPair</code>.</p>
     *
     * @return hash code for this <code>DataPair</code>
     */
    @Override
    public int hashCode() {
        // name's hashCode is multiplied by an arbitrary prime number (13)
        // in order to make sure there is a difference in the hashCode between
        // these two parameters:
        //  name: a  value: aa
        //  name: aa value: a
        return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
    }

    /**
     * <p>Test this <code>DataPair</code> for equality with another
     * <code>Object</code>.</p>
     *
     * <p>If the <code>Object</code> to be tested is not a
     * <code>DataPair</code> or is <code>null</code>, then this method
     * returns <code>false</code>.</p>
     *
     * <p>Two <code>DataPair</code>s are considered equal if and only if
     * both the names and values are equal.</p>
     *
     * @param o the <code>Object</code> to test for
     *          equality with this <code>DataPair</code>
     * @return <code>true</code> if the given <code>Object</code> is
     * equal to this <code>DataPair</code> else <code>false</code>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof DataPair) {
            DataPair DataPair = (DataPair) o;
            if (key != null ? !key.equals(DataPair.key) : DataPair.key != null) return false;
            if (value != null ? !value.equals(DataPair.value) : DataPair.value != null) return false;
            return true;
        }
        return false;
    }
}

