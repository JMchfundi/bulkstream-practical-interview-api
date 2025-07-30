package co.ke.finsis.menu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "menu_item")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;

    private String icon;

    private String link;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private MenuItem parent;

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @OrderBy("itemOrder ASC")
    private List<MenuItem> subItems = new ArrayList<>();

    @Column(name = "item_order", nullable = false)
    private int itemOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Using ElementCollection for Set of String Role Names
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER) // Eagerly load roles for filtering
    @CollectionTable(name = "menu_item_required_roles", joinColumns = @JoinColumn(name = "menu_item_id"))
    @Column(name = "role_name") // Column in the junction table to store the role enum name
    private Set<String> requiredRoleNames = new HashSet<>(); // Store Role enum names as Strings

    // Helper methods for subItems management (optional, but good practice for relationships)
    public void addSubItem(MenuItem subItem) {
        if (!this.subItems.contains(subItem)) {
            this.subItems.add(subItem);
            subItem.setParent(this);
        }
    }

    public void removeSubItem(MenuItem subItem) {
        if (this.subItems.contains(subItem)) {
            this.subItems.remove(subItem);
            subItem.setParent(null);
        }
    }
}