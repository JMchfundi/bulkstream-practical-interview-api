// ============================
// ENTITY: Station.java
// ============================
package co.ke.finsis.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stationName;

    private String levelType;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Station parent;

    @Embedded
    private Location location;

    private String economicActivities;
    private String electricity;
    private String internet;
    private String roadAccess;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "document_id")
    private DocumentUpload document;
}
