package co.ke.bulkstream.menu.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class MenuItemCreateUpdateDto {
    private Long id; // Optional for updates, allows client to specify ID if needed
    private String label;
    private String icon;
    private String link;
    private Long parentId; // Use ID for parent
    private int itemOrder;
    private boolean isActive = true;
    private Set<String> requiredRoles; // Role names (e.g., "ADMIN", "OFFICER", "USER")
    private List<MenuItemCreateUpdateDto> subItems; // For creating/updating nested items
}