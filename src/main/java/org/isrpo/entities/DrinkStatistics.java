package org.isrpo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Drink statistic entity of database
 */
@Getter
@Entity
@Table(name = "drink_statistics")
public class DrinkStatistics {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne
    private Drink drink;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    public DrinkStatistics() {
    }

    public DrinkStatistics(Drink drink) {
        this.drink = drink;
    }
}
