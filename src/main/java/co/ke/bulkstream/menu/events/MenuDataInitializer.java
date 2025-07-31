package co.ke.bulkstream.menu.events;

import co.ke.bulkstream.menu.payload.MenuItemCreateUpdateDto;
import co.ke.bulkstream.menu.payload.MenuItemResponseDto;
import co.ke.bulkstream.menu.service.MenuService;
import co.ke.tucode.systemuser.entities.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class MenuDataInitializer {

        @Bean
        public CommandLineRunner initMenuData(MenuService menuService) {
                return args -> {

                        System.out.println("--- Resetting & Initializing Menu Items ---");

                        menuService.deleteAllMenuItems(); // You must implement this in MenuService

                        System.out.println("--- Initializing Menu Items ---");

                        List<Map<String, Object>> menuItemsData = getMenuItems();
                        createMenuItemsRecursive(menuItemsData, null, menuService);

                        System.out.println("--- Menu initial data population complete ---");
                };
        }

        private List<Map<String, Object>> getMenuItems() {
                // Removed explicit IDs from mapItem calls
                // Map<String, Object> dashboard = mapItem("Dashboard", "ri-dashboard-line", "/kaufer/dashboard");

                Map<String, Object> create = mapItem("Create", "ri-wallet-3-line",
                                List.of(
                                                mapItem("tabulate", "/form"),
                                                mapItem("history", "/form")));

                return List.of(create);
        }

        // Removed id parameter
        private Map<String, Object> mapItem(String label, String link) {
                return mapItem(label, null, link);
        }

        // Removed id parameter
        private Map<String, Object> mapItem(String label, String icon, String link) {
                Map<String, Object> item = new HashMap<>();
                item.put("label", label);
                if (icon != null)
                        item.put("icon", icon);
                item.put("link", link);
                return item;
        }

        // Removed id parameter
        private Map<String, Object> mapItem(String label, List<Map<String, Object>> subItems) {
                return mapItem(label, null, subItems);
        }

        // Removed id parameter
        private Map<String, Object> mapItem(String label, String icon, List<Map<String, Object>> subItems) {
                Map<String, Object> item = new HashMap<>();
                item.put("label", label);
                if (icon != null)
                        item.put("icon", icon);
                item.put("subItems", subItems);
                return item;
        }

        private void createMenuItemsRecursive(List<Map<String, Object>> items, Long parentId, MenuService menuService) {
                if (items == null || items.isEmpty())
                        return;

                for (int i = 0; i < items.size(); i++) {
                        Map<String, Object> itemData = items.get(i);
                        MenuItemCreateUpdateDto dto = new MenuItemCreateUpdateDto();

                        dto.setLabel((String) itemData.get("label"));
                        dto.setIcon((String) itemData.get("icon"));
                        dto.setLink((String) itemData.get("link"));
                        dto.setParentId(parentId);
                        dto.setItemOrder(i + 1);
                        dto.setActive(true);

                        Set<String> roles = new HashSet<>();
                        String label = (String) itemData.get("label");
                        switch (label) {
                                case "Dashboard":
                                        roles.addAll(Set.of(Role.USER.name(), Role.OFFICER.name(), Role.ADMIN.name()));
                                        break;
                                default:
                                        roles.addAll(Set.of(Role.OFFICER.name(), Role.ADMIN.name()));
                        }
                        dto.setRequiredRoles(roles);

                        MenuItemResponseDto savedItem = menuService.createMenuItem(dto);

                        List<Map<String, Object>> subItems = (List<Map<String, Object>>) itemData.get("subItems");
                        if (subItems != null && !subItems.isEmpty()) {
                                createMenuItemsRecursive(subItems, savedItem.getId(), menuService);
                        }
                }
        }
}