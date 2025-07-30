// ============================
// DTO: LocationDto.java
// ============================
package co.ke.finsis.payload;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDto {
    private String county;
    private String subCounty;
    private String ward;
    private String gps;
}