package co.ke.bulkstream.menu.service;

import co.ke.bulkstream.menu.entity.MenuItem;
import co.ke.bulkstream.menu.payload.MenuItemCreateUpdateDto;
import co.ke.bulkstream.menu.payload.MenuItemResponseDto;
import co.ke.bulkstream.menu.repository.MenuItemRepository;
import co.ke.tucode.systemuser.entities.Role; // Import your Role enum
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    /**
     * Retrieves the entire menu structure (all active items) with RBAC applied for
     * the current user.
     * This is the primary method for the frontend to get its dynamic menu.
     *
     * @return A list of MenuItemResponseDto representing the accessible main menu.
     */
    @Transactional(readOnly = true)
    public List<MenuItemResponseDto> getMainMenuForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Get the single role name for the current user (e.g., "ADMIN", "OFFICER",
        // "USER")
        // Your TRES_User provides a single role.
        String userRoleName = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role.startsWith("ROLE_")) // Ensure it's a role authority
                .findFirst()
                .map(role -> role.replaceFirst("ROLE_", ""))
                .orElse("ANONYMOUS"); // Default for unauthenticated or no role found, adjust as per your security

        // Fetch all top-level menu items with their required role names eagerly
        List<MenuItem> topLevelEntities = menuItemRepository.findByParentIsNullOrderByItemOrderAsc();

        // Recursively filter and map to DTO
        return filterAndMapToDto(topLevelEntities, userRoleName);
    }

    // --- CRUD Operations for Menu Items (Admin-facing) ---

    /**
     * Get all menu items for administrative purposes (no RBAC filtering here).
     */
    @Transactional(readOnly = true)
    public List<MenuItemResponseDto> getAllMenuItemsFullHierarchy() {
        // This method fetches all menu items regardless of user roles
        List<MenuItem> topLevelEntities = menuItemRepository.findByParentIsNullOrderByItemOrderAsc();
        return topLevelEntities.stream()
                .map(this::mapEntityToDtoForAdmin)
                .collect(Collectors.toList());
    }

    /**
     * Get a single menu item by ID for administrative purposes.
     */
    @Transactional(readOnly = true)
    public MenuItemResponseDto getMenuItemById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MenuItem not found with ID: " + id));
        return mapEntityToDtoForAdmin(menuItem);
    }

    /**
     * Create a new menu item.
     */
    @Transactional
    public MenuItemResponseDto createMenuItem(MenuItemCreateUpdateDto dto) {
        MenuItem newMenuItem = new MenuItem();
        return saveOrUpdateMenuItem(newMenuItem, dto);
    }

    /**
     * Update an existing menu item.
     */
    @Transactional
    public MenuItemResponseDto updateMenuItem(Long id, MenuItemCreateUpdateDto dto) {
        MenuItem existingMenuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MenuItem not found with ID: " + id));
        return saveOrUpdateMenuItem(existingMenuItem, dto);
    }

    /**
     * Delete a menu item by ID.
     */
    @Transactional
    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new EntityNotFoundException("MenuItem not found with ID: " + id);
        }
        menuItemRepository.deleteById(id);
    }

    // --- Helper Methods ---

    /**
     * Saves or updates a MenuItem entity from a DTO. Handles parent and required
     * roles.
     * Recursively processes sub-items (simplified for full replacement in DTO).
     */
    private MenuItemResponseDto saveOrUpdateMenuItem(MenuItem menuItem, MenuItemCreateUpdateDto dto) {
        menuItem.setLabel(dto.getLabel());
        menuItem.setIcon(dto.getIcon());
        menuItem.setLink(dto.getLink());
        menuItem.setItemOrder(dto.getItemOrder());
        menuItem.setActive(dto.isActive());

        // Handle parent relationship
        if (dto.getParentId() != null) {
            MenuItem parent = menuItemRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Parent MenuItem not found with ID: " + dto.getParentId()));
            menuItem.setParent(parent);
        } else {
            menuItem.setParent(null); // Ensure parent is set to null if no parentId provided
        }

        // Handle required roles (using enum names as Strings)
        menuItem.getRequiredRoleNames().clear(); // Clear existing roles
        if (dto.getRequiredRoles() != null && !dto.getRequiredRoles().isEmpty()) {
            for (String roleName : dto.getRequiredRoles()) {
                // Validate if the role name is a valid enum value from your Role enum
                try {
                    Role.valueOf(roleName.toUpperCase()); // Convert to uppercase as enums are typically uppercase
                    menuItem.getRequiredRoleNames().add(roleName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Invalid role name specified: " + roleName + ". Must be one of: " + Set.of(Role.values()));
                }
            }
        }

        // Save the main item first (important for new items to get an ID before
        // sub-items link to it)
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        // Handle sub-items (recursive creation/update logic)
        // IMPORTANT: This current logic for sub-items in a nested DTO assumes that
        // if sub-items are provided in the DTO, they completely replace the existing
        // ones.
        // For more granular "add/remove/update specific child", you'd need more complex
        // logic.
        if (dto.getSubItems() != null) {
            // Remove existing sub-items that are not in the DTO or have different IDs
            savedMenuItem.getSubItems().removeIf(existingSubItem -> dto.getSubItems().stream().noneMatch(
                    dtoSubItem -> dtoSubItem.getId() != null && dtoSubItem.getId().equals(existingSubItem.getId())));

            for (MenuItemCreateUpdateDto subItemDto : dto.getSubItems()) {
                MenuItem subMenuItem;
                if (subItemDto.getId() != null) {
                    // Attempt to find and update existing sub-item
                    subMenuItem = savedMenuItem.getSubItems().stream()
                            .filter(s -> s.getId().equals(subItemDto.getId()))
                            .findFirst()
                            .orElseGet(MenuItem::new); // If not found, create new
                } else {
                    subMenuItem = new MenuItem(); // New sub-item
                }

                // Map DTO properties to the sub-item entity
                subMenuItem.setLabel(subItemDto.getLabel());
                subMenuItem.setIcon(subItemDto.getIcon());
                subMenuItem.setLink(subItemDto.getLink());
                subMenuItem.setItemOrder(subItemDto.getItemOrder());
                subMenuItem.setActive(subItemDto.isActive());
                subMenuItem.setParent(savedMenuItem); // Set parent correctly

                // Handle sub-item roles
                subMenuItem.getRequiredRoleNames().clear();
                if (subItemDto.getRequiredRoles() != null) {
                    for (String roleName : subItemDto.getRequiredRoles()) {
                        try {
                            Role.valueOf(roleName.toUpperCase());
                            subMenuItem.getRequiredRoleNames().add(roleName.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("Invalid role name for sub-item: " + roleName);
                        }
                    }
                }
                // Add the (potentially updated or new) sub-item back to the parent's collection
                if (!savedMenuItem.getSubItems().contains(subMenuItem)) { // Check if it's already there (for updates)
                    savedMenuItem.getSubItems().add(subMenuItem);
                }
            }
            // Re-sort sub-items based on updated itemOrder
            savedMenuItem.getSubItems().sort((s1, s2) -> Integer.compare(s1.getItemOrder(), s2.getItemOrder()));
        } else {
            // If subItems DTO is null or empty, clear all existing sub-items
            savedMenuItem.getSubItems().clear();
        }

        // Save again to cascade changes in sub-items (especially if new ones added or
        // old ones removed)
        savedMenuItem = menuItemRepository.save(savedMenuItem);

        // Map the (potentially updated) entity to DTO for the response
        return mapEntityToDtoForAdmin(savedMenuItem);
    }

    /**
     * Recursively filters menu items based on the user's single role and maps them
     * to DTOs.
     * This is for the dynamic menu fetched by regular users.
     */
    private List<MenuItemResponseDto> filterAndMapToDto(List<MenuItem> entities, String userRoleName) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .filter(MenuItem::isActive) // Only show active items
                .filter(item -> hasAccess(item, userRoleName)) // Apply RBAC filter
                .map(item -> new MenuItemResponseDto(
                        item.getId(),
                        item.getLabel(),
                        item.getIcon(),
                        item.getLink(),
                        filterAndMapToDto(item.getSubItems(), userRoleName) // Recursive call for sub-items
                ))
                .collect(Collectors.toList());
    }

    /**
     * Recursively maps MenuItem entities to MenuItemResponseDto for admin view (no
     * RBAC filtering).
     */
    private MenuItemResponseDto mapEntityToDtoForAdmin(MenuItem entity) {
        List<MenuItemResponseDto> subItemDtos = null;
        if (entity.getSubItems() != null && !entity.getSubItems().isEmpty()) {
            subItemDtos = entity.getSubItems().stream()
                    .map(this::mapEntityToDtoForAdmin) // Recursive
                    .collect(Collectors.toList());
        }
        return new MenuItemResponseDto(
                entity.getId(),
                entity.getLabel(),
                entity.getIcon(),
                entity.getLink(),
                subItemDtos);
    }

    /**
     * Determines if a user with the given role name has access to a menu item.
     */
    private boolean hasAccess(MenuItem item, String userRoleName) {
        // If the menu item has no required roles, grant access
        if (item.getRequiredRoleNames() == null || item.getRequiredRoleNames().isEmpty()) {
            return true;
        }

        // Check if the user's single role matches any of the required role names for
        // this menu item
        // Case-insensitive comparison is a good idea given enum .name() vs potential
        // client input
        return item.getRequiredRoleNames().stream()
                .anyMatch(requiredRole -> requiredRole.equalsIgnoreCase(userRoleName));
    }

    public void deleteAllMenuItems() {
        // TODO Auto-generated method stub
        menuItemRepository.deleteAll();
    }
}