package co.ke.finsis.menu.controller;

import co.ke.finsis.menu.payload.MenuItemCreateUpdateDto;
import co.ke.finsis.menu.payload.MenuItemResponseDto;
import co.ke.finsis.menu.service.MenuService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * Endpoint for the frontend to fetch the dynamic menu based on the logged-in
     * user's roles.
     * Accessible by any authenticated user.
     */
    @GetMapping("/items")
    @PreAuthorize("isAuthenticated()") // Ensure user is logged in
    public ResponseEntity<List<MenuItemResponseDto>> getDynamicMenuItems() {
        List<MenuItemResponseDto> menuItems = menuService.getMainMenuForCurrentUser();
        return ResponseEntity.ok(menuItems);
    }

    // --- Admin-facing CRUD Endpoints for Menu Items ---

    /**
     * Get all menu items for administrative purposes (no RBAC filtering here).
     * Requires ADMIN role.
     */
    @GetMapping("/admin/items")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can see all menu items for management
    public ResponseEntity<List<MenuItemResponseDto>> getAllMenuItemsForAdmin() {
        List<MenuItemResponseDto> menuItems = menuService.getAllMenuItemsFullHierarchy();
        return ResponseEntity.ok(menuItems);
    }

    /**
     * Get a single menu item by ID for administrative purposes.
     * Requires ADMIN role.
     */
    @GetMapping("/admin/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponseDto> getMenuItemById(@PathVariable Long id) {
        try {
            MenuItemResponseDto menuItem = menuService.getMenuItemById(id);
            return ResponseEntity.ok(menuItem);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new menu item.
     * Requires ADMIN role.
     */
    @PostMapping("/admin/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponseDto> createMenuItem(@RequestBody MenuItemCreateUpdateDto menuItemDto) {
        try {
            MenuItemResponseDto createdMenuItem = menuService.createMenuItem(menuItemDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMenuItem);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            // You might want to return a more structured error response here
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Update an existing menu item.
     * Requires ADMIN role.
     */
    @PutMapping("/admin/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponseDto> updateMenuItem(@PathVariable Long id,
            @RequestBody MenuItemCreateUpdateDto menuItemDto) {
        try {
            MenuItemResponseDto updatedMenuItem = menuService.updateMenuItem(id, menuItemDto);
            return ResponseEntity.ok(updatedMenuItem);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Delete a menu item.
     * Requires ADMIN role.
     */
    @DeleteMapping("/admin/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        try {
            menuService.deleteMenuItem(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}