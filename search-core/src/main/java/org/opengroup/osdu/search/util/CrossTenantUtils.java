// Copyright 2017-2019, Schlumberger
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

package org.opengroup.osdu.search.util;

import com.google.api.client.util.Strings;
import org.opengroup.osdu.core.common.search.ElasticIndexNameResolver;
import org.opengroup.osdu.core.common.model.search.Query;
import org.opengroup.osdu.core.common.util.KindParser;
import org.opengroup.osdu.search.service.IndexAliasService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Component
public class CrossTenantUtils {
    // For details, please refer to implementation of class
    // org.opengroup.osdu.core.common.model.search.validation.MultiKindValidator
    private static final int MAX_INDEX_NAME_LENGTH = 3840;

    @Inject
    private ElasticIndexNameResolver elasticIndexNameResolver;
    @Inject
    private IndexAliasService indexAliasService;

    public String getIndexName(Query searchRequest) {
        List<String> kinds = KindParser.parse(searchRequest.getKind());
        StringBuilder builder = new StringBuilder();
        for(String kind : kinds) {
            String index = this.elasticIndexNameResolver.getIndexNameFromKind(kind);
            builder.append(index);
            builder.append(",");
        }
        builder.append("-.*"); // Exclude Lucene/ElasticSearch internal indices
        String index = builder.toString();
        if(index.length() <= MAX_INDEX_NAME_LENGTH) {
            return index;
        }

        return getIndexNamesWithAliases(kinds);
    }

    private String getIndexNamesWithAliases(List<String> kinds) {
        StringBuilder builder = new StringBuilder();
        Map<String, String> aliases = this.indexAliasService.getIndicesAliases(kinds);
        for(String kind : kinds) {
            if(aliases.containsKey(kind) && !Strings.isNullOrEmpty(aliases.get(kind))) {
                String alias = aliases.get(kind);
                builder.append(alias);
            }
            else {
                String index = this.elasticIndexNameResolver.getIndexNameFromKind(kind);
                builder.append(index);
            }
            builder.append(",");
        }
        builder.append("-.*"); // Exclude Lucene/ElasticSearch internal indices
        return builder.toString();
    }
}
