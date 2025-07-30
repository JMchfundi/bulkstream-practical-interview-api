package co.ke.finsis.inventory.repository;

import co.ke.finsis.inventory.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

List<Product> findByCategoryId(Long categoryId);
List<Product> findBySellingPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

}