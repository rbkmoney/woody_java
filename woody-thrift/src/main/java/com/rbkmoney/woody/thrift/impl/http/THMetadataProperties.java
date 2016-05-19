package com.rbkmoney.woody.thrift.impl.http;

/**
 * Created by vpankrashkin on 29.04.16.
 */
public class THMetadataProperties {
    public static final String TH_PROPERTY_PREFIX = "md_thrift_http_";
    public static final String TH_ERROR_TYPE = TH_PROPERTY_PREFIX + "error_type";
    public static final String TH_ERROR_SUBTYPE = TH_PROPERTY_PREFIX + "error_subtype";

    public static final String TH_RESPONSE_STATUS = TH_PROPERTY_PREFIX + "response_status";
    public static final String TH_RESPONSE_MESSAGE = TH_PROPERTY_PREFIX + "response_message";

    public static final String TH_CALL_MSG_TYPE = TH_PROPERTY_PREFIX + "call_msg_type";
    public static final String TH_CALL_RESULT_MSG_TYPE = TH_PROPERTY_PREFIX + "call_result_msg_type";

    public static final String TH_TRANSPORT_REQUEST = TH_PROPERTY_PREFIX + "transport_request";
    public static final String TH_TRANSPORT_RESPONSE = TH_PROPERTY_PREFIX + "transport_response";
    public static final String TH_TRANSPORT_RESPONSE_SET = TH_PROPERTY_PREFIX + "transport_response_set";

    public static final String TH_ERROR_METADATA_SOURCE = TH_PROPERTY_PREFIX + "error_metadata_source";
}
