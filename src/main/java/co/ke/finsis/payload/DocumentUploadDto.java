// ============================
// DTO: DocumentUploadDto.java
// ============================
package co.ke.finsis.payload;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadDto {
    private Long id;
    private String fileName;
    private String fileUrl;
    private String contentType;
}