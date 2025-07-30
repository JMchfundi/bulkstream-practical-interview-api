package co.ke.finsis.menu.events;

import co.ke.finsis.menu.payload.MenuItemCreateUpdateDto;
import co.ke.finsis.menu.payload.MenuItemResponseDto;
import co.ke.finsis.menu.service.MenuService;
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
                Map<String, Object> dashboard = mapItem("Dashboard", "ri-dashboard-line", "/kaufer/dashboard");

                Map<String, Object> create = mapItem("Create", "ri-wallet-3-line",
                                List.of(
                                                mapItem("New Group", "/form"),
                                                mapItem("New Client", "/form"),
                                                mapItem("New Loan", "/form"),
                                                mapItem("New Officer", "/form"),
                                                mapItem("New Station", "/form")));

                Map<String, Object> finance = mapItem("Finance", "ri-bank-line",
                                List.of(
                                                mapItem("Chart of Accounts", "/form"),
                                                mapItem("New Product Definition", "/form"),
                                                mapItem("Approval Benchmark", "/display"),
                                                mapItem("Bankings", List.of(
                                                                mapItem("Receipt", "/tresentry"),
                                                                mapItem("Receipt Batch", "/tresentry"),
                                                                mapItem("Voucher", "/tresentry"))),
                                                mapItem("Disbursements", "/display")));

                Map<String, Object> inventory = mapItem("Inventory", "ri-archive-2-line",
                                List.of(
                                                mapItem("Goods Receipt", "/form"),
                                                mapItem("Item List", "/display"),
                                                mapItem("Stock Out", "/display"),
                                                mapItem("Stock Adjustments", "/display"),
                                                mapItem("Update Serials", "/display"),
                                                mapItem("Inventory Reports", "/display")));

                Map<String, Object> reports = mapReports();

                Map<String, Object> users = mapItem("Users", "ri-group-fill",
                                List.of(
                                                mapItem("Activity Logs", "ri-shield-star-line", "/display"),
                                                mapItem("Manage Roles", "ri-shield-star-line", "/display")));

                return List.of(dashboard, create, finance, inventory, reports, users);
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

        private Map<String, Object> mapReports() {
                List<Map<String, Object>> reportCategories = List.of(
                                mapItem("Repayments", "ri-hand-coin-line", List.of(
                                                mapItem("All Withdrawals", "/display"),
                                                mapItem("Loan Repayment - Savings", "/display"),
                                                mapItem("Loan Repayment - BDA", "/display"),
                                                mapItem("Loan Repayment - Journal Entry", "/display"),
                                                mapItem("Loan Repayment - BDA Settlement", "/display"))),
                                mapItem("Portfolio at Risk (PAR)", "ri-shield-line", List.of(
                                                mapItem("By Branch", "/display"),
                                                mapItem("By Region", "/display"),
                                                mapItem("Company-wide", "/display"))),
                                mapItem("Deposits Summary", "ri-bank-line", List.of(
                                                mapItem("By Group", "/display"),
                                                mapItem("By Officer", "/display"),
                                                mapItem("By Branch", "/display"))),
                                mapItem("Sales Reports", "ri-bar-chart-2-fill", List.of(
                                                mapItem("Daily - By Officer", "/display"),
                                                mapItem("Daily - By Branch", "/display"),
                                                mapItem("Weekly - By Region", "/display"),
                                                mapItem("Monthly - Company", "/display"))),
                                mapItem("Outstanding Loan Balances (OLB)", "ri-money-dollar-circle-line", List.of(
                                                mapItem("By Group", "/display"),
                                                mapItem("By Officer", "/display"),
                                                mapItem("By Branch", "/display"),
                                                mapItem("By Region", "/display"),
                                                mapItem("Company-wide", "/display"))),
                                mapItem("Arrears", "ri-alarm-warning-line", List.of(
                                                mapItem("By Group", "/display"),
                                                mapItem("By Officer", "/display"),
                                                mapItem("By Branch", "/display"),
                                                mapItem("By Region", "/display"),
                                                mapItem("Company-wide", "/display"))),
                                mapItem("Officer Reports", "ri-user-voice-line", List.of(
                                                mapItem("All Officer Entries", "/display"),
                                                mapItem("Officer Performance", "/display"))),
                                mapItem("Bulk Action Reports", "ri-task-line", List.of(
                                                mapItem("Client Creation", "/display"),
                                                mapItem("Client Closure", "/display"),
                                                mapItem("Loan Creation to Disbursement", "/display"),
                                                mapItem("Loan Repayments", "/display"),
                                                mapItem("Loan Closure", "/display"),
                                                mapItem("Loan Rejection", "/display"),
                                                mapItem("Loan Abandonment", "/display"),
                                                mapItem("Loan Rescheduling", "/display"))),
                                mapItem("Financial Reports", "ri-coins-line", List.of(
                                                mapItem("Balance Sheet", "/display"),
                                                mapItem("Trial Balance", "/display"),
                                                mapItem("Profit & Loss Statement (P&L)", "/display"),
                                                mapItem("Income Statement", "/display"),
                                                mapItem("Cash Flow Statement", "/display"),
                                                mapItem("General Ledger", "/display"))));
                return mapItem("Reports", "ri-file-chart-fill", reportCategories);
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
                                case "Create":
                                case "Finance":
                                case "Inventory":
                                case "Reports":
                                case "Bankings":
                                        roles.addAll(Set.of(Role.OFFICER.name(), Role.ADMIN.name()));
                                        break;
                                case "Users":
                                case "Activity Logs":
                                case "Manage Roles":
                                case "New Officer":
                                        roles.add(Role.ADMIN.name());
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