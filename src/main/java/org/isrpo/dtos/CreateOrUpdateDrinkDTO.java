package org.isrpo.dtos;

import lombok.Getter;
import lombok.Setter;

/**
 * Model for creating or updating drink
 */
@Getter
@Setter
public class CreateOrUpdateDrinkDTO {
    private String name;
    private Long waterAmount;
    private Long coffeeAmount;
    private Long milkAmount;
}
