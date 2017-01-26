package com.rbkmoney.woody.api.trace.context.metadata;

/**
 * Created by vpankrashkin on 20.01.17.
 */
public interface MetadataConverter<T> {
    /**
     * @throws MetadataConversionException if conversion error occurs
     * */
    T convertToObject(String key, String value) throws MetadataConversionException;

    /**
     * @throws MetadataConversionException if conversion error occurs
     * */
    String convertToString(String key, T value) throws MetadataConversionException;

    /**
     * @return true - if converter can process entry with such key; false - otherwise
     * */
    boolean apply(String key);

    /**
     * @return true - if converter accepts absence of any entries that can be applied for conversion to object; false - otherwise
     * */
    default boolean applyToObject() {
        return false;
    }

    /**
     * @return true - if converter accepts absence of any entries that can be applied for conversion to string; false - otherwise
     * */
    default boolean applyToString() {
        return false;
    }
}
