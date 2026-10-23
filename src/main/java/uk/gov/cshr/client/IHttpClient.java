package uk.gov.cshr.client;

import org.springframework.http.RequestEntity;

public interface IHttpClient {
    <T, R> T executeRequest(RequestEntity<R> request, Class<T> responseClass);

}
