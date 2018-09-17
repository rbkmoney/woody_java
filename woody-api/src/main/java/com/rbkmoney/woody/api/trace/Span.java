package com.rbkmoney.woody.api.trace;

/**
 * Created by vpankrashkin on 21.04.16.
 */
public class Span {
    private String traceId;
    private String name;
    private String id;
    private String parentId;
    private long deadline;
    private long timestamp;
    private long duration;

    public Span() {
    }

    protected Span(Span oldSpan) {
        this.traceId = oldSpan.traceId;
        this.name = oldSpan.name;
        this.id = oldSpan.id;
        this.parentId = oldSpan.parentId;
        this.deadline = oldSpan.deadline;
        this.timestamp = oldSpan.timestamp;
        this.duration = oldSpan.duration;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isFilled() {
        return traceId != null && parentId != null && id != null;
    }

    public boolean isStarted() {
        return isFilled() && timestamp != 0;
    }

    public boolean hasDeadline() {
        return deadline > 0;
    }

    public void reset() {
        traceId = null;
        name = null;
        id = null;
        parentId = null;
        deadline = 0;
        timestamp = 0;
        duration = 0;
    }

    public Span cloneObject() {
        return new Span(this);
    }

    @Override
    public String toString() {
        return "Span{" +
                "traceId='" + traceId + '\'' +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", parentId='" + parentId + '\'' +
                ", deadline=" + deadline +
                ", timestamp=" + timestamp +
                ", duration=" + duration +
                '}';
    }
}
