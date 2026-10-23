package uk.gov.cshr.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;

public interface IHttpClient {
    <T, R> T executeRequest(RequestEntity<R> request, Class<T> responseClass);

    <T, R> T executeTypeReferenceRequest(RequestEntity<R> request, ParameterizedTypeReference<T> parameterizedTypeReference);

}
