package org.isrpo.dtos;

import lombok.Getter;
import lombok.Setter;

/**
 * Model for updating coffee machine inventory
 */
@Getter
@Setter
public class UpdateInventoryDTO {
    private Long water;
    private Long coffee;
    private Long milk;
}
