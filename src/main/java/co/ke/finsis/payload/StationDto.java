// ============================
// DTO: StationDto.java
// ============================
package co.ke.finsis.payload;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationDto {

    private Long id;

    private String stationName;

    private String levelType;

    private Long parentId;

    private LocationDto location;

    private String economicActivities;
    private String electricity;
    private String internet;
    private String roadAccess;

    private DocumentUploadDto document;
}
