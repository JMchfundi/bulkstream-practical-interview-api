package co.ke.finsis.inventory.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString(exclude = {"product", "categoryAttribute"})
public class ProductAttributeValue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String attributeValue; // Renamed 'value' to 'attributeValue'

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "category_attribute_id")
    private CategoryAttribute categoryAttribute;
}