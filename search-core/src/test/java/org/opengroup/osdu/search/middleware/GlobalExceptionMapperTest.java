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

package org.opengroup.osdu.search.middleware;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import javassist.NotFoundException;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.search.config.SearchConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;


import jakarta.validation.ValidationException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opengroup.osdu.search.util.Constants.LARGE_ERROR_MESSAGE;

@RunWith(MockitoJUnitRunner.class)
public class GlobalExceptionMapperTest {

    @Mock
    private JaxRsDpsLog log;
    @Mock
    private SearchConfigurationProperties configurationProperties;
    @InjectMocks
    private GlobalExceptionMapper sut;

    @Test
    public void should_useValuesInAppExceptionInResponse_When_AppExceptionIsHandledByGlobalExceptionMapper() {

        AppException exception = new AppException(409, "any reason", "any message");

        ResponseEntity<Object> response = sut.handleAppException(exception);
        assertEquals(409, response.getStatusCodeValue());
        assertEquals(exception.getError(), response.getBody());
    }

    @Test
    public void should_use404ValueInResponse_When_NotFoundExceptionIsHandledByGlobalExceptionMapper() {

        NotFoundException exception = new NotFoundException("any message");

         	ResponseEntity<Object> response = sut.handleNotFoundException(exception);
         	assertEquals(404, response.getStatusCodeValue());
         	assertTrue(response.getBody().toString().contains("any message"));
    }

//    @Test
//    public void should_use405ValueInResponse_When_NotAllowedExceptionIsHandledByGlobalExceptionMapper() {
//
//        NotAllowedException exception = new NotAllowedException("any message");
//
//        Response response = sut.toResponse(exception);
//        assertEquals(405, response.getStatus());
//        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
//        assertNotNull(response.getEntity());
//    }

//    @Test
//    public void should_use415ValueInResponse_When_NotSupportedExceptionIsHandledByGlobalExceptionMapper() {
//
//        NotSupportedException exception = new NotSupportedException("any message");
//
//        Response response = sut.toResponse(exception);
//        assertEquals(415, response.getStatus());
//        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
//        assertNotNull(response.getEntity());
//    }

    @Test
    public void should_useGenericValuesInResponse_When_ExceptionIsHandledByGlobalExceptionMapper() {

        Exception exception = new Exception("any message");

        ResponseEntity<Object> response = sut.handleGeneralException(exception);
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("AppError(code=500, reason=Server error., message=An unknown error has occurred., errors=null, debuggingInfo=null, originalException=java.lang.Exception: any message)", response.getBody().toString());
    }

    @Test
    public  void should_useBadRequestInResponse_When_JsonProcessingExceptionIsHandledByGlobalExceptionMapper() {
        JsonProcessingException exception = new JsonParseException(null, "any message");

        ResponseEntity<Object> response = sut.handleJsonProcessingException(exception);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusCodeValue());
    }

    @Test
    public  void should_useBadRequestInResponse_When_handleUnrecognizedPropertyExceptionIsHandledByGlobalExceptionMapper() {
        UnrecognizedPropertyException exception = mock(UnrecognizedPropertyException.class);

        ResponseEntity<Object> response = sut.handleUnrecognizedPropertyException(exception);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusCodeValue());
    }

    @Test
    public  void should_useBadRequestInResponse_When_handleValidationExceptionIsHandledByGlobalExceptionMapper() {
        ValidationException exception = new ValidationException();

        ResponseEntity<Object> response = sut.handleValidationException(exception);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusCodeValue());
    }

    @Test
    public  void should_useBadRequestInResponse_When_handleAccessDeniedExceptionIsHandledByGlobalExceptionMapper() {
        AccessDeniedException exception = new AccessDeniedException("Access is denied.");

        ResponseEntity<Object> response = sut.handleAccessDeniedException(exception);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCodeValue());
    }

    @Test
    public void should_returnNullResponse_when_BrokenPipeIOExceptionIsCaptured() {
        IOException ioException = new IOException("Broken pipe");

        ResponseEntity response = this.sut.handleIOException(ioException);

        assertNull(response);
    }

    @Test
    public void should_returnServiceUnavailable_when_IOExceptionIsCaptured() {
        IOException ioException = new IOException("Not broken yet");

        ResponseEntity response = this.sut.handleIOException(ioException);

        assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatusCodeValue());
    }

    @Test
    public void should_truncateMessage_when_hugeExceptionIsCaptureed() {
        AppException appException = new AppException(HttpStatus.SC_BAD_REQUEST, "Too many clauses", LARGE_ERROR_MESSAGE);
        when(this.configurationProperties.getMaxExceptionLogMessageLength()).thenReturn(5000);
        ResponseEntity response = this.sut.handleAppException(appException);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusCodeValue());
        String loggingMsg = LARGE_ERROR_MESSAGE.substring(0, this.configurationProperties.getMaxExceptionLogMessageLength());
        verify(this.log).error(loggingMsg, new AppException(HttpStatus.SC_BAD_REQUEST, "Too many clauses", loggingMsg));
    }
}
