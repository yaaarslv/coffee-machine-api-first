package org.isrpo.services;

import org.isrpo.entities.Drink;
import org.isrpo.entities.MachineInventory;
import org.isrpo.tools.ConsoleLogger;
import org.isrpo.tools.exceptions.CoffeeException;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Service for ordering drink
 */
@Service
public class OrderService {
    private final DrinkService drinkService;
    private final CoffeeMachineService coffeeMachineService;
    private final MeterRegistry registry;

    public OrderService(DrinkService drinkService, CoffeeMachineService coffeeMachineService, MeterRegistry registry) {
        this.drinkService = drinkService;
        this.coffeeMachineService = coffeeMachineService;
        this.registry = registry;
    }

    /**
     * Method of ordering drink
     * @param id identifier of drink to be ordered
     * @param name name of drink to be ordered
     * @return ordered drink
     * @throws CoffeeException if drink with this is or name doesn't exist or not enough ingredients in coffee machine
     */
    public Drink orderDrink(Long id, String name) throws RuntimeException, CoffeeException {

        return Timer.builder("coffee_order_duration")
                .description("Time to process coffee order")
                .register(registry)
                .record(() -> {

                    ConsoleLogger.log("BUSINESS action=order_drink id=" + id + " name=" + name, ConsoleLogger.LogLevel.INFO);

                    try {
                        Drink drink;
                        if (id != null) {
                            drink = drinkService.getDrinkById(id);
                        } else if (name != null) {
                            drink = drinkService.getDrinkByName(name, true);
                        } else {
                            ConsoleLogger.log("WARN reason=recipe_type_not_selected_exception", ConsoleLogger.LogLevel.WARNING);
                            registry.counter("coffee_order_errors_total", "type", "recipe_type_not_selected_exception").increment();
                            throw CoffeeException.recipeTypeNotSelectedException();
                        }

                        MachineInventory inventory = coffeeMachineService.getInventory(1L);

                        if (inventory.getWater() < drink.getWaterAmount()) {
                            ConsoleLogger.log("WARN reason=not_enough_water drink=" + drink.getName() + " available=" + inventory.getWater() + " required=" + drink.getWaterAmount(), ConsoleLogger.LogLevel.WARNING);
                            registry.counter("coffee_order_errors_total", "type", "not_enough_water").increment();
                            throw CoffeeException.notEnoughWater(drink.getName(), inventory.getWater(), drink.getWaterAmount());
                        }
                        if (inventory.getCoffee() < drink.getCoffeeAmount()) {
                            ConsoleLogger.log("WARN reason=not_enough_coffee drink=" + drink.getName() + " available=" + inventory.getCoffee() + " required=" + drink.getCoffeeAmount(), ConsoleLogger.LogLevel.WARNING);
                            registry.counter("coffee_order_errors_total", "type", "not_enough_coffee").increment();
                            throw CoffeeException.notEnoughCoffee(drink.getName(), inventory.getCoffee(), drink.getCoffeeAmount());
                        }
                        if (inventory.getMilk() < drink.getMilkAmount()) {
                            ConsoleLogger.log("WARN reason=not_enough_milk drink=" + drink.getName() + " available=" + inventory.getMilk() + " required=" + drink.getMilkAmount(), ConsoleLogger.LogLevel.WARNING);
                            registry.counter("coffee_order_errors_total", "type", "not_enough_milk").increment();
                            throw CoffeeException.notEnoughMilk(drink.getName(), inventory.getMilk(), drink.getMilkAmount());
                        }

                        inventory.setWater(inventory.getWater() - drink.getWaterAmount());
                        inventory.setCoffee(inventory.getCoffee() - drink.getCoffeeAmount());
                        inventory.setMilk(inventory.getMilk() - drink.getMilkAmount());

                        coffeeMachineService.updateInventoryAfterOrdering(inventory);

                        drinkService.addCoffeeStatistics(drink);

                        registry.counter("coffee_orders_total", "drink", drink.getName()).increment();

                        ConsoleLogger.log("BUSINESS action=drink_prepared name=" + drink.getName() + " waterLeft=" + inventory.getWater() + " coffeeLeft=" + inventory.getCoffee() + " milkLeft=" + inventory.getMilk(), ConsoleLogger.LogLevel.INFO);

                        return drink;

                    } catch (CoffeeException e) {
                        ConsoleLogger.log("ERROR reason=order_failed exception=" + e.getClass().getSimpleName() + " message=" + e.getMessage(), ConsoleLogger.LogLevel.ERROR);
                        registry.counter("coffee_order_errors_total", "type", "unknown").increment();
                        throw new RuntimeException(e);
                    }
                });
    }
}
