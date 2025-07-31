package co.ke.bulkstream.menu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.ke.bulkstream.menu.entity.MenuItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Retrieves top-level menu items (those without a parent) ordered by itemOrder
    List<MenuItem> findByParentIsNullOrderByItemOrderAsc();

    // Retrieves a menu item by ID. @ElementCollection (requiredRoleNames) is EAGER, so roles are loaded.
    // Use the default findById as the ElementCollection is already EAGER.
    Optional<MenuItem> findById(Long id);
}