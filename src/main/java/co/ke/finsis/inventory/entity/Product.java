package co.ke.finsis.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import co.ke.finsis.entity.DocumentUpload;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal sellingPrice; // selling price
    private BigDecimal purchasePrice; // optional, if needed
    private int stock;

    @ManyToOne
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductAttributeValue> attributes;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "product_images",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "document_id")
    )
    private List<DocumentUpload> images;
}
