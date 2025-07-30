// co.ke.finsis.payload.GroupDTO
package co.ke.finsis.payload;

import java.math.BigDecimal;
import java.util.List; // Import List

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDTO {
    private Long id;
    private String groupName;
    private String county;
    private String subCounty;
    private String ward;
    private String village;
    private String nearestLandmark;
    private String officeType;

    private Long officerId;
    private String officerName;

    private BigDecimal savingbalance;
    private int totalClients;
    private List<ClientDto> clients; // Changed from single ClientDto to List<ClientDto>
}