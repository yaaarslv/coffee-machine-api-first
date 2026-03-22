package org.isrpo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Drink entity of database
 */
@Getter
@Entity
@Table(name = "drink")
public class Drink {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "name")
    private String name;

    @Setter
    @Column(name = "water_amount")
    private Long waterAmount;

    @Setter
    @Column(name = "coffee_amount")
    private Long coffeeAmount;

    @Setter
    @Column(name = "milk_amount")
    private Long milkAmount;

    public Drink() {
    }

    public Drink(String name, Long waterAmount, Long coffeeAmount, Long milkAmount) {
        this.name = name;
        this.waterAmount = waterAmount;
        this.coffeeAmount = coffeeAmount;
        this.milkAmount = milkAmount;
    }
}
