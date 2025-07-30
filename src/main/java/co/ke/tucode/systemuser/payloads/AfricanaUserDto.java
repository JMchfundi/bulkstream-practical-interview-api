package co.ke.tucode.systemuser.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AfricanaUserDto {
    private Long id;
    private String username;
    private String email;
    private String access;
    private String role;
}
