package org.couchbase.mock.client;

import javax.validation.constraints.Null;

/**
 * @author Mark Nunberg
 */
public class GetMCPortsRequest extends MockRequest {
    public GetMCPortsRequest(@Null String bucket) {
        super();
        setName("get_mcports");
        if (bucket != null) {
            payload.put("bucket", bucket);
        }
    }

    public GetMCPortsRequest() {
        this(null);
    }
}
