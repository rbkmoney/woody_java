package com.rbkmoney.woody.api.trace;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestMetadata {
    @Test
    public void testValueOverriding() {
        Metadata metadata = new Metadata();
        metadata.putValue("1", 1);
        assertEquals((Object) 1, metadata.getValue("1"));
        metadata.putValue("1", 2);
        assertEquals((Object) 2, metadata.getValue("1"));
        metadata.removeValue("1");
        assertNull(metadata.getValue("1"));

        metadata = new Metadata(false);
        metadata.putValue("1", 1);
        assertEquals((Object) 1, metadata.getValue("1"));

        try {
            metadata.putValue("1", 1);
            fail();
        } catch (IllegalStateException e) {}

        try {
            metadata.removeValue("1");
            fail();
        } catch (IllegalStateException e) {}
    }
}
