package org.isrpo.services;

import org.isrpo.entities.Drink;
import org.isrpo.entities.MachineInventory;
import org.isrpo.tools.ConsoleLogger;
import org.isrpo.tools.exceptions.CoffeeException;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import java.util.Objects;

/**
 * Service for ordering drink
 */
@Service
public class OrderService {
    private final DrinkService drinkService;
    private final CoffeeMachineService coffeeMachineService;
    private final MeterRegistry registry;
    private final ObservationRegistry observationRegistry;

    public OrderService(DrinkService drinkService, CoffeeMachineService coffeeMachineService, MeterRegistry registry, ObservationRegistry observationRegistry) {
        this.drinkService = drinkService;
        this.coffeeMachineService = coffeeMachineService;
        this.registry = registry;
        this.observationRegistry = observationRegistry;
    }

    /**
     * Method of ordering drink
     * @param id identifier of drink to be ordered
     * @param name name of drink to be ordered
     * @return ordered drink
     * @throws RuntimeException if drink with this is or name doesn't exist or not enough ingredients in coffee machine
     */
    public Drink orderDrink(Long id, String name) throws RuntimeException {

        return Timer.builder("coffee_order_duration")
                .description("Time to process coffee order")
                .register(registry)
                .record(() -> Observation.createNotStarted("order-drink", observationRegistry)
                        .lowCardinalityKeyValue("service", "OrderService")
                        .observe(() -> {

                            ConsoleLogger.log("BUSINESS action=order_drink id=" + id + " name=" + name, ConsoleLogger.LogLevel.INFO);

                            try {
                                Drink drink = Objects.requireNonNull(
                                        Observation.createNotStarted("get-drink", observationRegistry)
                                                .observe(() -> {
                                                    if (id != null) {
                                                        try {
                                                            return drinkService.getDrinkById(id);
                                                        } catch (CoffeeException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    } else if (name != null) {
                                                        try {
                                                            return drinkService.getDrinkByName(name, true);
                                                        } catch (CoffeeException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    } else {
                                                        ConsoleLogger.log("WARN reason=recipe_type_not_selected_exception", ConsoleLogger.LogLevel.WARNING);
                                                        registry.counter("coffee_order_errors_total", "type", "recipe_type_not_selected_exception").increment();
                                                        throw new RuntimeException(CoffeeException.recipeTypeNotSelectedException());
                                                    }
                                                }),
                                        "drink must not be null"
                                );

                                MachineInventory inventory = Objects.requireNonNull(
                                        Observation.createNotStarted("get-inventory", observationRegistry)
                                                .observe(() -> {
                                                    try {
                                                        return coffeeMachineService.getInventory(1L);
                                                    } catch (CoffeeException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }),
                                        "inventory must not be null"
                                );

                                Observation.createNotStarted("check-ingredients", observationRegistry)
                                        .observe(() -> {
                                            if (inventory.getWater() < drink.getWaterAmount()) {
                                                ConsoleLogger.log("WARN reason=not_enough_water drink=" + drink.getName() + " available=" + inventory.getWater() + " required=" + drink.getWaterAmount(), ConsoleLogger.LogLevel.WARNING);
                                                registry.counter("coffee_order_errors_total", "type", "not_enough_water").increment();
                                                throw new RuntimeException(CoffeeException.notEnoughWater(
                                                        drink.getName(), inventory.getWater(), drink.getWaterAmount()
                                                ));
                                            }
                                            if (inventory.getCoffee() < drink.getCoffeeAmount()) {
                                                ConsoleLogger.log("WARN reason=not_enough_coffee drink=" + drink.getName() + " available=" + inventory.getCoffee() + " required=" + drink.getCoffeeAmount(), ConsoleLogger.LogLevel.WARNING);
                                                registry.counter("coffee_order_errors_total", "type", "not_enough_coffee").increment();
                                                throw new RuntimeException(CoffeeException.notEnoughCoffee(
                                                        drink.getName(), inventory.getCoffee(), drink.getCoffeeAmount()
                                                ));
                                            }
                                            if (inventory.getMilk() < drink.getMilkAmount()) {
                                                ConsoleLogger.log("WARN reason=not_enough_milk drink=" + drink.getName() + " available=" + inventory.getMilk() + " required=" + drink.getMilkAmount(), ConsoleLogger.LogLevel.WARNING);
                                                registry.counter("coffee_order_errors_total", "type", "not_enough_milk").increment();
                                                throw new RuntimeException(CoffeeException.notEnoughMilk(
                                                        drink.getName(), inventory.getMilk(), drink.getMilkAmount()
                                                ));
                                            }
                                        });

                                Observation.createNotStarted("update-inventory", observationRegistry)
                                        .observe(() -> {
                                            inventory.setWater(inventory.getWater() - drink.getWaterAmount());
                                            inventory.setCoffee(inventory.getCoffee() - drink.getCoffeeAmount());
                                            inventory.setMilk(inventory.getMilk() - drink.getMilkAmount());

                                            coffeeMachineService.updateInventoryAfterOrdering(inventory);
                                        });

                                Observation.createNotStarted("save-drink-statistics", observationRegistry)
                                        .observe(() -> {
                                            try {
                                                drinkService.addCoffeeStatistics(drink);
                                            } catch (CoffeeException e) {
                                                throw new RuntimeException(e);
                                            }
                                        });

                                registry.counter("coffee_orders_total", "drink", drink.getName()).increment();

                                ConsoleLogger.log(
                                        "BUSINESS action=drink_prepared name=" + drink.getName()
                                        + " waterLeft=" + inventory.getWater()
                                        + " coffeeLeft=" + inventory.getCoffee()
                                        + " milkLeft=" + inventory.getMilk(),
                                        ConsoleLogger.LogLevel.INFO
                                );

                                return drink;

                            } catch (RuntimeException e) {
                                Throwable cause = e.getCause();
                                if (cause instanceof CoffeeException coffeeException) {
                                    ConsoleLogger.log("ERROR reason=order_failed exception=" + coffeeException.getClass().getSimpleName() + " message=" + coffeeException.getMessage(), ConsoleLogger.LogLevel.ERROR);
                                    registry.counter("coffee_order_errors_total", "type", "unknown").increment();
                                    throw new RuntimeException(coffeeException);
                                }
                                ConsoleLogger.log("ERROR reason=order_failed exception=" + e.getClass().getSimpleName() + " message=" + e.getMessage(), ConsoleLogger.LogLevel.ERROR);
                                registry.counter("coffee_order_errors_total", "type", "unknown").increment();
                                throw e;
                            }
                        }));
    }
}
