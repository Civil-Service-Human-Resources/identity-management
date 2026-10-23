package uk.gov.cshr.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadableFile {
    private String fileName;
    private ByteArrayResource data;
}
