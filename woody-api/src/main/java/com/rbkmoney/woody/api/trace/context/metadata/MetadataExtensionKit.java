package com.rbkmoney.woody.api.trace.context.metadata;

/**
 * Created by vpankrashkin on 24.01.17.
 */
public interface MetadataExtensionKit<T> {
    MetadataExtension<T> getExtension();

    MetadataConverter<T> getConverter();
}
