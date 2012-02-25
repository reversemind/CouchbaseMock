/**
 *     Copyright 2012 Couchbase, Inc.
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
package org.couchbase.mock.control;

import java.util.List;
import org.couchbase.mock.CouchbaseMock;

/**
 * This provides an interface for mock commands. They are all issued by providing
 * ','-delimited tokens.
 * @author M. Nunberg
 *
 */
public interface MockControlCommandHandler {

    abstract void execute(CouchbaseMock mock, List<String> tokens);
}