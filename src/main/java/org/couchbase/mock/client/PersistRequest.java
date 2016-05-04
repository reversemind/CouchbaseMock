/*
 * Copyright 2013 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.couchbase.mock.client;

import javax.validation.constraints.NotNull;

import java.util.List;

public class PersistRequest extends CacheRequest {
    public PersistRequest(@NotNull String key, @NotNull String value, long cas, boolean onMaster, int numReplicas) {
        this(key, value, cas, onMaster, numReplicas, "");
    }
    public PersistRequest(@NotNull String key, @NotNull String value, long cas, boolean onMaster, List<Integer> replicaIds) {
        this(key, value, cas, onMaster, replicaIds, "");
    }

    public PersistRequest(@NotNull String key, @NotNull String value, long cas, boolean onMaster, int numReplicas, @NotNull String bucket) {
        super(key, value, cas, onMaster, numReplicas, bucket);
        command.put("command", "persist");
    }
    public PersistRequest(@NotNull String key, @NotNull String value, long cas, boolean onMaster, List<Integer> replicaIds, @NotNull String bucket) {
        super(key, value, cas, onMaster, replicaIds, bucket);
        command.put("command", "persist");
    }
}
