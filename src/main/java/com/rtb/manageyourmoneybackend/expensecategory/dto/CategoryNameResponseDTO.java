package com.rtb.manageyourmoneybackend.expensecategory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CategoryNameResponseDTO {
    List<CategoryNameItem> categories;  // { id, name }
}
