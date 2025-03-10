package org.opengroup.osdu.common;

import com.google.gson.Gson;
import org.opengroup.osdu.core.common.model.search.SortQuery;
import org.opengroup.osdu.request.Query;
import org.opengroup.osdu.response.ErrorResponseMock;
import org.opengroup.osdu.response.ResponseMock;
import org.opengroup.osdu.util.Config;
import org.opengroup.osdu.util.HTTPClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class QueryBase extends TestsBase {

    private Query requestQuery = new Query();

    public QueryBase(HTTPClient httpClient) {
        super(httpClient);
    }

    @Override
    protected String getApi() {
        return Config.getSearchBaseURL() + "query";
    }

    @Override
    protected String getHttpMethod() {
        return "POST";
    }

    public void i_limit_the_count_of_returned_results_to(int limit) {
        requestQuery.setLimit(limit);
    }

    public void i_send_None_with(String kind) {
        String actualKind = generateActualName(kind, timeStamp);
        System.out.println("Kind while searching data is: "+actualKind);
        requestQuery.setKind(actualKind);
    }

    public void i_send_with(String query, String kind) {
        requestQuery.setQuery(query);
        requestQuery.setKind(generateActualName(kind, timeStamp));
    }

    public void i_send_with_multi_kinds(String query, int number, String kind) {
        requestQuery.setQuery(query);
        String actualKind = generateActualName(kind, timeStamp);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < number; i++) {
            if(builder.length() > 0) {
                builder.append(",");
            }
            builder.append(actualKind);
        }
        requestQuery.setKind(builder.toString());
    }

    public void i_set_the_fields_I_want_in_response_as(List<String> returnedFileds) {
        if(returnedFileds.contains("NULL")) requestQuery.setReturnedFields(null);
        else if (!returnedFileds.contains("All")) requestQuery.setReturnedFields(returnedFileds);
    }

    public void i_set_autocomplete_phrase(String autocompletePhrase) {
        requestQuery.setSuggestPhrase(autocompletePhrase);
    }

    public void i_apply_geographical_query_on_field(String field) {
        spatialFilter.setField(field);
        requestQuery.setSpatialFilter(spatialFilter);
    }

    public void i_should_get_in_response_records(int resultCount) {
        String payload = requestQuery.toString();
        ResponseMock response = executeQuery(payload, headers, httpClient.getAccessToken(), ResponseMock.class);
        assertEquals(200, response.getResponseCode());
        assertEquals(resultCount, response.getResults().size());
    }

    public void i_should_get_following_autocomplete_suggestions(String autocompleteOptions) {
        String payload = requestQuery.toString();
        ResponseMock response = executeQuery(payload, headers, httpClient.getAccessToken(), ResponseMock.class);
        assertEquals(200, response.getResponseCode());
        assertEquals(Arrays.asList(autocompleteOptions.split(",")), response.getPhraseSuggestions());
    }

    public void i_should_get_in_response_records_using_search_as_mode(int resultCount) {
        String payload = requestQuery.toString();
        ResponseMock response = executeQuery(payload, headers, httpClient.getAccessToken(), ResponseMock.class);
        assertEquals(200, response.getResponseCode());
        assertEquals(resultCount, response.getResults().size());
    }

    public void i_should_get_in_response_records(int resultCount, List<String> returnedFields) {
        String payload = requestQuery.toString();
        ResponseMock response = executeQuery(payload, headers, httpClient.getAccessToken(), ResponseMock.class);
        assertEquals(200, response.getResponseCode());
        assertEquals(resultCount, response.getResults().size());

        if(returnedFields.contains("NULL") || returnedFields.contains("All")) { return; }

        for (Map<String, Object> results: response.getResults()) {
            assertTrue(results.keySet().containsAll(returnedFields));
        }
    }

    public void i_should_get_response_with_reason_message_and_errors(List<Integer> codes, String type, String msg,
                                                                     String error) {
        String payload = requestQuery.toString();
        ErrorResponseMock response = executeQuery(payload, headers, httpClient.getAccessToken(), ErrorResponseMock.class);
        if (response.getErrors() != null) {
            assertEquals(generateActualName(error, timeStamp), response.getErrors().get(0));
        }
        assertEquals(type, response.getReason());
        assertEquals(msg, response.getMessage());
        assertTrue(codes.contains(response.getResponseCode()));
    }

    public void i_set_the_offset_of_starting_point_as(int offset) {
        requestQuery.setOffset(offset);
    }

    public void i_sort_with(String sortJson) {
        SortQuery sortArg = (new Gson()).fromJson(sortJson, SortQuery.class);
        requestQuery.setSort(sortArg);
    }

    public void i_aggregate_by(String aggField) throws Throwable {
        requestQuery.setAggregateBy(aggField);
    }

    public void i_search_as(String isOwner) {
        requestQuery.setQueryAsOwner(Boolean.parseBoolean(isOwner));
    }

    public void i_should_get_records_in_right_order(String firstRecId, String lastRecId) {
        String actualFirstId = generateActualName(firstRecId,timeStamp);
        String actualLastId = generateActualName(lastRecId,timeStamp);
        String payload = requestQuery.toString();
        ResponseMock response = executeQuery(payload, headers, httpClient.getAccessToken(), ResponseMock.class);
        assertEquals(200, response.getResponseCode());
        assertNotNull(response.getResults());
        assertTrue(response.getResults().size() > 0);
        assertEquals(actualFirstId, response.getResults().get(0).get("id").toString());
        assertEquals(actualLastId, response.getResults().get(response.getResults().size()-1).get("id").toString());
    }

    public void i_should_get_aggregation_with_unique_values(int uniqueValueCount) {
        String payload = requestQuery.toString();
        ResponseMock response = executeQuery(payload, headers, httpClient.getAccessToken(), ResponseMock.class);
        assertEquals(200, response.getResponseCode());
        assertEquals(uniqueValueCount, response.getAggregations().size());
        assertEquals(0, response.getAggregations().stream().filter(x -> x == null).count());
    }

    public void i_should_get_included_kinds_and_should_not_get_excluded_kinds_from_aggregation(String includedKinds, String excludedKinds) {
        String payload = requestQuery.toString();
        ResponseMock response = executeQuery(payload, headers, httpClient.getAccessToken(), ResponseMock.class);
        assertEquals(200, response.getResponseCode());
        Set<String> kinds = response.getAggregations().stream().map(agg -> agg.getKey()).collect(Collectors.toSet());
        for(String kind:  generateActualName(includedKinds, timeStamp).split(",")) {
            assertTrue(kinds.contains(kind));
        }
        for(String kind:  generateActualName(excludedKinds, timeStamp).split(",")) {
            assertFalse(kinds.contains(kind));
        }
    }
}
