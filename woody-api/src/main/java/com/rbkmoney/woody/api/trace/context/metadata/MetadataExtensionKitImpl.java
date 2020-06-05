package com.rbkmoney.woody.api.trace.context.metadata;

public class MetadataExtensionKitImpl<T> implements MetadataExtensionKit<T> {
    private final MetadataExtension<T> extension;
    private final MetadataConverter<T> converter;

    public MetadataExtensionKitImpl(MetadataExtension<T> extension, MetadataConverter<T> converter) {
        this.extension = extension;
        this.converter = converter;
    }

    public MetadataExtension<T> getExtension() {
        return extension;
    }

    public MetadataConverter<T> getConverter() {
        return converter;
    }
}
