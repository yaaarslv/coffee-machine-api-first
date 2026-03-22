package org.isrpo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Coffee machine inventory of database
 */
@Entity
@Getter
public class MachineInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private Long water;

    @Setter
    private Long coffee;

    @Setter
    private Long milk;

    public MachineInventory() {
    }

    public MachineInventory(Long water, Long coffee, Long milk) {
        this.water = water;
        this.coffee = coffee;
        this.milk = milk;
    }
}
