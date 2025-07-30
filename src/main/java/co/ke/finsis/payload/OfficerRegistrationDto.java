package co.ke.finsis.payload;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficerRegistrationDto {

    // Basic Officer Info
    private Long Id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String idNumber;
    private String dob;
    private String gender;

    // Bank Info
    private String bankDetails;

    // Locations
    private List<LocationDto> locations;

    // Next of Kins
    private List<NextOfKinDto> nextOfKins;

    // Associated documents metadata
    private List<DocumentUploadDto> documents;

    private Long stationId;
    private String stationName;    
}
