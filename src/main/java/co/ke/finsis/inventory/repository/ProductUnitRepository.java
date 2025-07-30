package co.ke.finsis.inventory.repository;

import co.ke.finsis.inventory.entity.Product;
import co.ke.finsis.inventory.entity.ProductUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductUnitRepository extends JpaRepository<ProductUnit, Long> {
    List<ProductUnit> findByProductId(Long productId);

    List<ProductUnit> findByProductIdAndAssignedFalse(Long productId);

    Boolean existsByProductAndAssignedFalse(Product p);
}
