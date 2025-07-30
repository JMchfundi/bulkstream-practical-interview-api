package co.ke.finsis.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

import co.ke.finsis.inventory.entity.Category;
import co.ke.tucode.systemuser.entities.TRES_User;

@Entity
@Table(name = "loan_types_")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    private Integer maxTerm;
    private String termUnit;

    private BigDecimal minAmount; // 🔧 For Cash
    private BigDecimal maxAmount; // 🔧 For Cash

    private String classification; // 🔧 "Cash" or "Product"

    @ManyToMany
    @JoinTable(name = "loan_type_approvers", 
    joinColumns = @JoinColumn(name = "loan_type_id"), 
    inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<TRES_User> approvers;

    @ManyToOne
    @JoinColumn(name = "product_category_id_")
    private Category productCategory;

    @OneToMany(mappedBy = "loanType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanTypeAttribute> customAttributes; // 🔧 Dynamic interest/fee fields
}

