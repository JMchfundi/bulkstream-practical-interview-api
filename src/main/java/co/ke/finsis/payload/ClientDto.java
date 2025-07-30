// co.ke.finsis.payload.ClientDto.java
package co.ke.finsis.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String idNumber;
    private Date dob;
    private String gender;
    private Long groupId;
    private String groupName;

    // Updated lists for relationships, now using your existing DTOs
    private List<LocationDto> locations;
    private List<NextOfKinDto> nextOfKins;
    private List<DocumentUploadDto> documents;
    private List<AccountSummaryDto> accountSummaries;
}