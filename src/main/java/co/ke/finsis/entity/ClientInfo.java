package co.ke.finsis.entity;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import co.ke.tucode.accounting.entities.Account;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "client_information")
public class ClientInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Personal Information
    @NotBlank(message = "Full Name is required")
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone Number is required")
    private String phoneNumber;

    @NotBlank(message = "ID Number is required")
    @Column(unique = true)
    private String idNumber;

    @Past(message = "Date of Birth should be in the past")
    private Date dob;

    private String gender;

    @Transient
    private Long groupId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonBackReference
    private Group clientGroup;

    // === Next of Kin - Using @OneToMany with @JoinTable (as per previous discussion) ===
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "client_next_of_kin",
        joinColumns = @JoinColumn(name = "client_id"),
        inverseJoinColumns = @JoinColumn(name = "nok_id")
    )
    private List<NextOfKin> nextOfKins = new ArrayList<>();

    // === Documents - Using @OneToMany with @JoinTable (as per previous discussion) ===
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "client_documents",
        joinColumns = @JoinColumn(name = "client_id"),
        inverseJoinColumns = @JoinColumn(name = "document_id")
    )
    private List<DocumentUpload> documents = new ArrayList<>();

    // === Linked Locations - Adopting OfficerRegistration's @ElementCollection format ===
    @ElementCollection // <--- This annotation is key for collections of Embeddables
    @CollectionTable(name = "client_locations", joinColumns = @JoinColumn(name = "client_id")) // <--- New join table for client locations
    private List<Location> locations = new ArrayList<>();


    @ManyToMany
    @JoinTable(
        name = "client_accounts",
        joinColumns = @JoinColumn(name = "client_id"),
        inverseJoinColumns = @JoinColumn(name = "account_id")
    )
    private List<Account> accounts = new ArrayList<>();

    // In ClientInfo
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Loan> loans = new ArrayList<>();
}