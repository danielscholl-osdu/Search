// Copyright © Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.search.provider.aws.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.opengroup.osdu.core.aws.cache.AwsElasticCache;
import org.opengroup.osdu.core.aws.ssm.K8sParameterNotFoundException;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.cache.RedisCache;
import org.opengroup.osdu.search.cache.CursorCache;
import org.opengroup.osdu.core.common.model.search.CursorSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CursorCacheImpl implements CursorCache {

    private ICache<String, CursorSettings> cache;
    private Boolean local;

    public void close() throws Exception {
        if (this.local){
            // local dummy cache, no need to close
        }else{
            ((RedisCache)this.cache).close();
        }
    }
    /**
     * Initializes a Cursor Cache with Redis connection parameters specified in the application
     * properties file.
     *
     * @param INDEX_CACHE_EXPIRATION - the expiration time for the Cursor Cache Redis cluster.
     */
    public CursorCacheImpl(@Value("${aws.elasticache.cluster.cursor.expiration}") final String INDEX_CACHE_EXPIRATION) throws K8sParameterNotFoundException, JsonProcessingException {
        cache = AwsElasticCache.RedisCache(Integer.parseInt(INDEX_CACHE_EXPIRATION) * 60, String.class, CursorSettings.class);
        local = cache.getClass() != RedisCache.class;
    }

    /**
     * Insert a CursorSettings object into the Redis cache
     * @param s the key of the object, used for retrieval
     * @param o the CursorSettings object to store
     */
    @Override
    public void put(String s, CursorSettings o) {
        this.cache.put(s, o);
    }

    /**
     * Gets a cached CursorSettings object by key
     * @param s the key of the cached CursorSettings object to get
     * @return
     */
    @Override
    public CursorSettings get(String s) {
        return this.cache.get(s);
    }

    /**
     * Deletes a CursorSettings item in the cache with the given key
     * @param s the key of the cached CursorSettings object to delete
     */
    @Override
    public void delete(String s) {
        this.cache.delete(s);
    }

    /**
     * Clears the entire Redis cache
     */
    @Override
    public void clearAll() {
        this.cache.clearAll();
    }

}
