package co.ke.finsis.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import co.ke.tucode.systemuser.entities.TRES_User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "officer_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficerRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Step 1: Officer details
    private String fullName;
    private String email;
    private String phoneNumber;
    private String idNumber;
    private String dob;
    private String gender;

    // Step 3: Bank details
    private String bankDetails;

    // co.ke.finsis.entity.OfficerRegistration.java
    @OneToOne(cascade = CascadeType.ALL)
    @JoinTable(name = "officer_user_link", 
    joinColumns = @JoinColumn(name = "officer_id", referencedColumnName = "id"), 
    inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private TRES_User systemUser;

    // === Groups (Optional: For example if officers belong to training or reporting
    // groups) ===
    @OneToMany(mappedBy = "officer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Group> groups = new ArrayList<>();

    // === Linked Locations (Embeddable, multiple allowed) ===
    @ElementCollection
    @CollectionTable(name = "officer_locations", joinColumns = @JoinColumn(name = "officer_id"))
    private List<Location> locations = new ArrayList<>();

    // === Linked Document Uploads (Shared, reusable) ===
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "officer_documents", joinColumns = @JoinColumn(name = "officer_id"), inverseJoinColumns = @JoinColumn(name = "document_id"))
    private List<DocumentUpload> documents = new ArrayList<>();

    // === Linked Next of Kin (Shared, reusable) ===
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "officer_next_of_kin", joinColumns = @JoinColumn(name = "officer_id"), inverseJoinColumns = @JoinColumn(name = "nok_id"))
    private List<NextOfKin> nextOfKins = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "station_id")
    private Station assignedStation;
}
