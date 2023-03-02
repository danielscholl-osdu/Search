/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.search.provider.ibm.provider.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.search.AggregationResponse;
import org.opengroup.osdu.core.common.model.search.Query;
import org.opengroup.osdu.core.common.model.search.QueryRequest;
import org.opengroup.osdu.core.common.model.search.QueryResponse;
import org.opengroup.osdu.search.config.SearchConfigurationProperties;
import org.opengroup.osdu.search.logging.AuditLogger;
import org.opengroup.osdu.search.provider.interfaces.IQueryService;
import org.opengroup.osdu.search.util.ElasticClientHandler;
import org.opengroup.osdu.search.util.IAggregationParserUtil;
import org.opengroup.osdu.search.util.ISearchRequestUtil;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;


@Service
public class QueryServiceImpl extends QueryBase implements IQueryService {

    @Inject
    private ElasticClientHandler elasticClientHandler;
    @Inject
    private AuditLogger auditLogger;
    @Inject
    private SearchConfigurationProperties configurationProperties;
    @Inject
	private TenantInfo tenant;
    @Inject
    private IAggregationParserUtil aggregationParserUtil;
    @Inject
    private ISearchRequestUtil searchRequestUtil;

    @Override
    public QueryResponse queryIndex(QueryRequest searchRequest) throws IOException {
    	// validateTenant(searchRequest);
        try {
			tenant.getName();
		} catch (Exception e) {
			throw new AccessDeniedException("Unauthorized");
		}

        try (RestHighLevelClient client = this.elasticClientHandler.createRestClient()) {
            QueryResponse queryResponse = this.executeQuery(searchRequest, client);
            return queryResponse;
        }
    }

    private QueryResponse executeQuery(QueryRequest searchRequest, RestHighLevelClient client) throws AppException {
        SearchResponse searchResponse = this.makeSearchRequest(searchRequest, client);
        List<Map<String, Object>> results = this.getHitsFromSearchResponse(searchResponse);
        List<AggregationResponse> aggregations = getAggregationFromSearchResponse(searchResponse);

        QueryResponse queryResponse = QueryResponse.getEmptyResponse();
        queryResponse.setTotalCount(searchResponse.getHits().getTotalHits().value);
        if (results != null) {
            queryResponse.setAggregations(aggregations);
            queryResponse.setResults(results);
        }
        return queryResponse;
    }

    @Override
    SearchRequest createElasticRequest(Query request) throws AppException, IOException {
        QueryRequest searchRequest = (QueryRequest) request;

        // set the indexes to search against
        SearchRequest elasticSearchRequest = new SearchRequest(this.getIndex(request));
        searchRequestUtil.setIgnoreUnavailable(elasticSearchRequest, true);

        // build query
        SearchSourceBuilder sourceBuilder = this.createSearchSourceBuilder(request);
        sourceBuilder.from(searchRequest.getFrom());

        if (!Strings.isNullOrEmpty(searchRequest.getAggregateBy())) {
            sourceBuilder.aggregation(aggregationParserUtil.parseAggregation(searchRequest.getAggregateBy()));
        }

        elasticSearchRequest.source(sourceBuilder);

        return elasticSearchRequest;
    }

    @Override
    void querySuccessAuditLogger(Query request) {
        this.auditLogger.queryIndexSuccess(Lists.newArrayList(request.toString()));
}

    @Override
    void queryFailedAuditLogger(Query request) {
        this.auditLogger.queryIndexFailed(Lists.newArrayList(request.toString()));
    }

}
