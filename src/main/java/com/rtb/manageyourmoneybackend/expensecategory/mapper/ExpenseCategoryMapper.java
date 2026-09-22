package com.rtb.manageyourmoneybackend.expensecategory.mapper;

import com.rtb.manageyourmoneybackend.expensecategory.dto.ExpenseCategoryCreateRequestDTO;
import com.rtb.manageyourmoneybackend.expensecategory.dto.ExpenseCategoryResponseDTO;
import com.rtb.manageyourmoneybackend.expensecategory.dto.ExpenseCategoryUpdateRequestDTO;
import com.rtb.manageyourmoneybackend.expensecategory.entity.ExpenseCategory;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper bridging {@link ExpenseCategory} and its DTO representations.
 * <p>
 * The {@code @ManyToOne user} association is intentionally NOT populated by this
 * mapper on create/update — the service layer resolves and assigns the managed
 * {@code UserEntity} explicitly, since the mapper only has the raw DTOs to work with.
 */
@Mapper(componentModel = "spring")
public interface ExpenseCategoryMapper {

    /**
     * Maps a create request into a brand-new entity graph.
     * {@code id}, {@code user}, {@code created} and {@code modified} are all
     * managed outside of this mapping (generated key, resolved parent, DB-side
     * timestamps) and are therefore ignored here.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isSynced", ignore = true)
    ExpenseCategory toEntity(ExpenseCategoryCreateRequestDTO dto);

    /**
     * Applies an update request onto an already-managed entity in place.
     * Identity, ownership and audit timestamps are preserved / ignored so
     * they cannot be overwritten through this endpoint.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "modified", ignore = true)
    void updateEntityFromDto(ExpenseCategoryUpdateRequestDTO dto, @MappingTarget ExpenseCategory entity);

    /**
     * Flattens the entity, including the {@code user} association, down to
     * {@code userId} for the outbound representation.
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "totalExpenseAmount", ignore = true)
    ExpenseCategoryResponseDTO toResponseDto(ExpenseCategory entity);
}
