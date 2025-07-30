package co.ke.finsis.payload;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NextOfKinDto {
    private Long id;
    private String name;
    private String phone;
    private String relationship;
}
