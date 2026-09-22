package com.rtb.manageyourmoneybackend.expense.mapper;

import com.rtb.manageyourmoneybackend.expense.dto.ExpenseCreateRequestDTO;
import com.rtb.manageyourmoneybackend.expense.dto.ExpenseResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.ExpenseUpdateRequestDTO;
import com.rtb.manageyourmoneybackend.expense.entity.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Entity is the JPA source of truth for {@code category}, {@code user},
 * {@code isSynced} and {@code modified} — none of these are populated
 * by the mapper. The service layer resolves and sets them explicitly.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExpenseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isSynced", ignore = true)
    @Mapping(target = "modified", ignore = true)
    Expense toEntity(ExpenseCreateRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "synced", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "modified", ignore = true)
    void updateEntityFromDto(ExpenseUpdateRequestDTO dto, @MappingTarget Expense entity);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "synced", source = "synced")
    @Mapping(target = "categoryName", source = "category.name")
    ExpenseResponseDTO toResponseDto(Expense entity);
}
