package co.ke.bulkstream.menu.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Generates constructor with all fields for easy mapping
public class MenuItemResponseDto {
    private Long id;
    private String label;
    private String icon;
    private String link;
    private List<MenuItemResponseDto> subItems; // Nested DTOs for sub-items
}