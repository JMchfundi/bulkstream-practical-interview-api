package co.ke.finsis.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "next_of_kin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NextOfKin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String relationship;
}
