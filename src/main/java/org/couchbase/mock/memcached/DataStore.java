/**
 *     Copyright 2011 Membase, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.couchbase.mock.memcached;

import org.couchbase.mock.memcached.protocol.ErrorCode;

import java.security.AccessControlException;
import java.util.Date;
import java.util.Map;

import org.couchbase.mock.Bucket.BucketType;

/**
 * A small little data store.. Please note that since this is a dummy
 * data store I'm using in my test program, I don't care if the operations
 * are atomic... feel free to change that if you like...
 *
 * @author Trond Norbye
 */
public class DataStore {

    private volatile long casCounter = 1;
    private final VBucket vBucketMap[];
    private static final long THIRTY_DAYS = 30 * 24 * 60 * 60;

    public DataStore(int size) {
        vBucketMap = new VBucket[size];
        for (int ii = 0; ii < size; ++ii) {
            vBucketMap[ii] = new VBucket(null);
        }
    }

    public VBucket getVBucket(short vbucket) {
        if (vbucket >= vBucketMap.length) {
            // Illegal vbucket.. just report as no access..
            throw new AccessControlException("Illegal vbucket");
        }
        return vBucketMap[vbucket];
    }

    private Map<String, Item> getMap(MemcachedServer server, short vbucket) throws AccessControlException {
        if (vbucket >= vBucketMap.length && server.getType() == BucketType.COUCHBASE) {
            // Illegal vbucket.. just report as no access..
            throw new AccessControlException("Illegal vbucket");
        }
        return vBucketMap[vbucket].getMap(server);
    }

    public void setOwnership(int vbucket, MemcachedServer server) {
        vBucketMap[vbucket].setOwner(server);
    }

    public ErrorCode add(MemcachedServer server, short vBucketId, Item item) {
        // I don't give a shit about atomicity right now..
        Map<String, Item> map = getMap(server, vBucketId);
        Item old = lookup(map, item.getKey());
        if (old != null || item.getCas() != 0) {
            return ErrorCode.KEY_EEXISTS;
        }

        item.setCas(++casCounter);
        map.put(item.getKey(), item);
        return ErrorCode.SUCCESS;
    }

    public ErrorCode replace(MemcachedServer server, short vBucketId, Item item) {
        // I don't give a shit about atomicity right now..
        Map<String, Item> map = getMap(server, vBucketId);
        Item old = lookup(map, item.getKey());
        if (old == null) {
            return ErrorCode.KEY_ENOENT;
        }

        if (item.getCas() != old.getCas()) {
            if (item.getCas() != 0) {
                return ErrorCode.KEY_EEXISTS;
            }
        }

        if (!old.ensureUnlocked(item.getCas())) {
            return ErrorCode.KEY_EEXISTS;
        }

        item.setCas(++casCounter);
        map.put(item.getKey(), item);
        return ErrorCode.SUCCESS;
    }

    public ErrorCode set(MemcachedServer server, short vBucketId, Item item) {
        Map<String, Item> map = getMap(server, vBucketId);

        if (item.getCas() == 0) {

            Item old = lookup(map, item.getKey());
            if (old != null && old.isLocked()) {
                return ErrorCode.KEY_EEXISTS;
            }

            item.setCas(++casCounter);
            map.put(item.getKey(), item);
            return ErrorCode.SUCCESS;
        } else {
            return replace(server, vBucketId, item);
        }
    }

    ErrorCode delete(MemcachedServer server, short vBucketId, String key, long cas) {
        // I don't give a shit about atomicity right now..
        Map<String, Item> map = getMap(server, vBucketId);
        Item i = lookup(map, key);
        if (i == null) {
            return ErrorCode.KEY_ENOENT;
        }

        if (i.ensureUnlocked(cas)) {
            return ErrorCode.ETMPFAIL;
        }

        if (cas == 0 || cas == i.getCas()) {
            map.remove(key);
            return ErrorCode.SUCCESS;
        }
        return ErrorCode.KEY_EEXISTS;
    }

    Item get(MemcachedServer server, short vBucketId, String key) {
        Map<String, Item> map = getMap(server, vBucketId);
        return lookup(map, key);
    }

    void flush(MemcachedServer server) {
        for (VBucket b : vBucketMap) {
            b.flush(server);
        }
    }

    /**
     * Converts an expiration value to an absolute Unix timestamp.
     * @param original
     * @return The converted value
     */
    public static int convertExptime(int original)
    {
        if (original == 0 || original > THIRTY_DAYS) {
            return original;
        }

        return (int)((new Date().getTime() / 1000) + original);
    }

    private Item lookup(Map<String, Item> map, String key) {
        Item ii = map.get(key);
        if (ii != null) {
            long now = new Date().getTime();
            if (ii.getExpiryTime() == 0 || now < ii.getExpiryTimeInMillis()) {
                return ii;

            } else {
                map.remove(key);
            }
        }
        return null;
    }
}
