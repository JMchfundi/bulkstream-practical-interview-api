package co.ke.finsis.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryAttribute {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type; // TEXT, NUMBER, BOOLEAN

    @ManyToOne
    private Category category;
}
