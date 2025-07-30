package co.ke.finsis.service;

import co.ke.finsis.entity.LoanType;
import co.ke.finsis.entity.LoanTypeAttribute;
import co.ke.finsis.inventory.entity.Category;
import co.ke.finsis.inventory.repository.CategoryRepository;
import co.ke.finsis.payload.LoanAttributeDto;
import co.ke.finsis.payload.LoanTypeDto;
import co.ke.finsis.repository.LoanTypeAttributeRepository;
import co.ke.finsis.repository.LoanTypeRepository;
import co.ke.tucode.systemuser.entities.TRES_User;
import co.ke.tucode.systemuser.repositories.Africana_UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanTypeService {

    private final LoanTypeRepository repository;
    private final Africana_UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LoanTypeAttributeRepository attributeRepository; // Still needed for specific attribute operations if any, but not for deleteAll here.

    public LoanTypeDto create(LoanTypeDto dto) {
        LoanType loanType = toEntity(dto);
        // When creating, the attributes are already set in toEntity and will be persisted via cascade
        LoanType saved = repository.save(loanType);
        return toDto(saved);
    }

    public LoanTypeDto getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Loan type ID must not be null");
        }
        System.out.println("Fetching LoanType with ID: " + id);
        LoanType loanType = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Loan type not found"));
        return toDto(loanType);
    }

    public List<LoanTypeDto> getAll() {
        return repository.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public LoanTypeDto update(Long id, LoanTypeDto dto) {
        LoanType existing = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Loan type not found"));

        // Update core fields
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setMaxTerm(dto.getMaxTerm());
        existing.setTermUnit(dto.getTermUnit());
        existing.setClassification(dto.getClassification());
        existing.setMinAmount(dto.getMinAmount());
        existing.setMaxAmount(dto.getMaxAmount());

        if (dto.getApproverUserIds() != null) {
            List<TRES_User> approvers = dto.getApproverUserIds().stream()
                .map(idVal -> userRepository.findById(idVal)
                    .orElseThrow(() -> new RuntimeException(
                        "User not found: " + idVal)))
                .collect(Collectors.toList());
            existing.setApprovers(approvers);
        }

        // Category
        if (dto.getProductCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getProductCategoryId())
                .orElseThrow(() -> new RuntimeException(
                    "Category not found: " + dto.getProductCategoryId()));
            existing.setProductCategory(category);
        } else {
            existing.setProductCategory(null);
        }

        // Custom Attributes - FIX APPLIED HERE
        // Clear the existing collection
        // Hibernate will handle the deletion of orphans because of cascade="all-delete-orphan"
        existing.getCustomAttributes().clear();

        if (dto.getCustomAttributes() != null && !dto.getCustomAttributes().isEmpty()) {
            List<LoanTypeAttribute> newAttrs = dto.getCustomAttributes().stream()
                .map(attrDto -> {
                    LoanTypeAttribute attr = toAttributeEntity(attrDto, existing); // Ensure the loanType reference is set
                    return attr;
                })
                .collect(Collectors.toList());

            // Add all new attributes to the existing (now empty) collection
            existing.getCustomAttributes().addAll(newAttrs);
        }
        // If dto.getCustomAttributes() is null or empty, the collection is already cleared, which is correct.

        LoanType updated = repository.save(existing); // This save will persist changes to the collection
        return toDto(updated);
    }

    public void delete(Long id) {
        LoanType existing = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Loan type not found"));
        repository.deleteById(id);
    }

    public List<LoanAttributeDto> getAttributesByLoanTypeId(Long loanTypeId) {
        LoanType loanType = repository.findById(loanTypeId)
            .orElseThrow(() -> new NoSuchElementException("Loan type not found"));

        return loanType.getCustomAttributes().stream()
            .map(this::toAttributeDto)
            .collect(Collectors.toList());
    }

    // ---------- MAPPING HELPERS ----------

    private LoanTypeDto toDto(LoanType entity) {
        return LoanTypeDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .maxTerm(entity.getMaxTerm())
            .termUnit(entity.getTermUnit())
            .classification(entity.getClassification())
            .minAmount(entity.getMinAmount())
            .maxAmount(entity.getMaxAmount())
            .approverUserIds(entity.getApprovers() != null
                ? entity.getApprovers().stream().map(TRES_User::getId)
                .collect(Collectors.toList())
                : List.of())
            .productCategoryId(entity.getProductCategory() != null
                ? entity.getProductCategory().getId()
                : null)
            .customAttributes(entity.getCustomAttributes() != null
                ? entity.getCustomAttributes().stream().map(this::toAttributeDto)
                .collect(Collectors.toList())
                : List.of())
            .build();
    }

    private LoanType toEntity(LoanTypeDto dto) {
        List<TRES_User> approvers = dto.getApproverUserIds() != null
            ? dto.getApproverUserIds().stream()
            .map(id -> userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                    "User not found: " + id)))
            .collect(Collectors.toList())
            : List.of();

        LoanType loanType = LoanType.builder()
            .id(dto.getId())
            .name(dto.getName())
            .description(dto.getDescription())
            .maxTerm(dto.getMaxTerm())
            .termUnit(dto.getTermUnit())
            .classification(dto.getClassification())
            .minAmount(dto.getMinAmount())
            .maxAmount(dto.getMaxAmount())
            .approvers(approvers)
            .build();

        // Set category if provided
        if (dto.getProductCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getProductCategoryId())
                .orElseThrow(() -> new RuntimeException(
                    "Category not found: " + dto.getProductCategoryId()));
            loanType.setProductCategory(category);
        }

        // Set attributes
        // When creating, the customAttributes collection is new and will be managed by Hibernate
        if (dto.getCustomAttributes() != null && !dto.getCustomAttributes().isEmpty()) {
            List<LoanTypeAttribute> attributes = dto.getCustomAttributes().stream()
                .map(attrDto -> toAttributeEntity(attrDto, loanType))
                .collect(Collectors.toList());
            loanType.setCustomAttributes(attributes);
        }

        return loanType;
    }

    private LoanAttributeDto toAttributeDto(LoanTypeAttribute attr) {
        return LoanAttributeDto.builder()
            .name(attr.getName())
            .value(attr.getValue())
            .percentage(attr.isPercentage()) // Ensure this is correctly mapped to valueType in frontend if needed
            .chargeType(attr.getChargeType())
            .oneTimeTiming(attr.getOneTimeTiming())
            .oneTimePeriodValue(attr.getOneTimePeriodValue())
            .oneTimePeriodUnit(attr.getOneTimePeriodUnit())
            .build();
    }

    private LoanTypeAttribute toAttributeEntity(LoanAttributeDto dto, LoanType loanType) {
        return LoanTypeAttribute.builder()
            .loanType(loanType) // Make sure the parent entity is set
            .name(dto.getName())
            .value(dto.getValue())
            .percentage(dto.isPercentage())
            .chargeType(dto.getChargeType())
            .oneTimeTiming(dto.getOneTimeTiming())
            .oneTimePeriodValue(dto.getOneTimePeriodValue())
            .oneTimePeriodUnit(dto.getOneTimePeriodUnit())
            .build();
    }
}