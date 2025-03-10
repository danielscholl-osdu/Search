/*
 *  Copyright 2020-2024 Google LLC
 *  Copyright 2020-2024 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.search.cache;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.cache.RedisCache;

@RequiredArgsConstructor
public class FieldTypeMappingCache implements IFieldTypeMappingCache {

  private final RedisCache<String, Map> cache;

  @Override
  public void put(String key, Map value) {
    this.cache.put(key, value);
  }

  @Override
  public Map get(String key) {
    return this.cache.get(key);
  }

  @Override
  public void delete(String s) {
    this.cache.delete(s);
  }

  @Override
  public void clearAll() {
    this.cache.clearAll();
  }
}
