package uk.gov.cshr.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagedResponse<T> {
    protected List<T> content;
    protected boolean last;
    protected Integer number;
    protected Integer totalPages;
    protected Integer totalElements;
    protected Integer size;
}
