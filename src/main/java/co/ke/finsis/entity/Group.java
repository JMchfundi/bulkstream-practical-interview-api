package co.ke.finsis.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String groupName;
    private String county;
    private String subCounty;
    private String ward;
    private String village;
    private String nearestLandmark;

    // Placeholder for officer; replace with @ManyToOne when ready
    private String officeType;

    @OneToMany(mappedBy = "clientGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ClientInfo> clients = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", nullable = false)
    @JsonBackReference
    private OfficerRegistration officer;

}
