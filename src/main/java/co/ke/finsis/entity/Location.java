// ============================
// ENTITY: Location.java
// ============================
package co.ke.finsis.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    private String county;
    private String subCounty;
    private String ward;
    private String gps;
}