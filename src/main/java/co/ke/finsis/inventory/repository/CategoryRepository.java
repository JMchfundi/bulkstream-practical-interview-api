package co.ke.finsis.inventory.repository;

import co.ke.finsis.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {}
