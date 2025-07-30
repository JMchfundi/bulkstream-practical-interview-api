package co.ke.finsis.inventory.service;

import co.ke.finsis.inventory.entity.Category;
import co.ke.finsis.inventory.entity.CategoryAttribute;
import co.ke.finsis.inventory.payload.CategoryAttributeDto;
import co.ke.finsis.inventory.payload.CategoryDto;
import co.ke.finsis.inventory.repository.CategoryAttributeRepository;
import co.ke.finsis.inventory.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepo;
    private final CategoryAttributeRepository attrRepo;

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        Category category = Category.builder().name(dto.getName()).build();
        category = categoryRepo.save(category);

        for (CategoryAttributeDto attr : dto.getAttributes()) {
            CategoryAttribute entity = CategoryAttribute.builder()
                .name(attr.getName())
                .type(attr.getType())
                .category(category)
                .build();
            attrRepo.save(entity);
        }

        return mapToDto(categoryRepo.findById(category.getId()).orElseThrow());
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepo.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategory(Long id) {
        return mapToDto(categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found")));
    }

    public void deleteCategory(Long id) {
        categoryRepo.deleteById(id);
    }

    // Mapping helper
    private CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .attributes(
                    category.getAttributes() != null
                        ? category.getAttributes().stream()
                            .map(attr -> CategoryAttributeDto.builder()
                                .id(attr.getId())
                                .name(attr.getName())
                                .type(attr.getType())
                                .build())
                            .collect(Collectors.toList())
                        : List.of()
                )
                .build();
    }
}
