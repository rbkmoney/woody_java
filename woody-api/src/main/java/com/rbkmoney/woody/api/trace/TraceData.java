package com.rbkmoney.woody.api.trace;

/**
 * Created by vpankrashkin on 21.04.16.
 */
public class TraceData {
    private final ClientSpan clientSpan = new ClientSpan();
    private final ServiceSpan serviceSpan = new ServiceSpan();

    public ClientSpan getClientSpan() {
        return clientSpan;
    }

    public ServiceSpan getServiceSpan() {
        return serviceSpan;
    }

    /**
     * Checks if {@link ServiceSpan} is filled to determine root:
     * - request initialized by server: span must be filled by server with data referred from client: has filled server span, it's not root by default -> false
     * - request initialized by client, produced by any server request handling event: has filled server span, it's not root -> false
     * - request initialized by client, not produced by any server request handling event: server span not filled, it's root -> true
     *
     * @return true - if root call is running; false - otherwise
     */
    public boolean isRoot() {
        return !serviceSpan.isFilled();
    }

    /**
     * Checks combination of client and server spans to determine current state:
     * Consider this states scheme (S - server span, C - client span; 1 - if it's set, 0 - if not set, determined by checking traceId existence in corresponding span):
     * <p>
     * S | C
     * -----
     * 0 | 0
     * 0 | 1
     * 1 | 0
     * 1 | 1
     * <p>
     * 0,0 and 0,1 combinations don't have server span and context in the state can't be server by default (no server span is set) - it's client state -> true
     * 1,0 means that server span is created and no client span exists - it's server state -> false
     * 1,1 means that both spans exist and child client call is active now because for any client request client span is cleared after call completion, so after child call state returns to (1,0) case - (1,1) is child client state -> true
     * <p>
     * This allows to eliminate the necessity for call processing code to be explicitly configured with expected call state. This can be figured out directly from the context in runtime.
     * The only exclusion is {@link com.rbkmoney.woody.api.trace.context.TraceContext} itself. It uses already filled trace id field for server state initiazation
     *
     * @return true - if call is running as root client or child client call for server request handling; false - if call is running in server request handing
     */
    public boolean isClient() {
        return serviceSpan.isFilled() ? clientSpan.isFilled() : true;
    }

    public ContextSpan getActiveSpan() {
        return isClient() ? clientSpan : serviceSpan;
    }

    public ContextSpan getSpan(boolean isClient) {
        return isClient ? clientSpan : serviceSpan;
    }

    public void reset() {
        clientSpan.reset();
        serviceSpan.reset();
    }
}
