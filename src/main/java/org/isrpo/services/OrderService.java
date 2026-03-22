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

                    ConsoleLogger.log("Получен новый заказ. id = " + id + ", name = " + name, ConsoleLogger.LogLevel.INFO);

                    try {
                        Drink drink;
                        if (id != null) {
                            drink = drinkService.getDrinkById(id);
                        } else if (name != null) {
                            drink = drinkService.getDrinkByName(name);
                        } else {
                            registry.counter("coffee_order_errors_total", "type", "validation").increment();
                            throw CoffeeException.recipeTypeNotSelectedException();
                        }

                        MachineInventory inventory = coffeeMachineService.getInventory(1L);

                        if (inventory.getWater() < drink.getWaterAmount()) {
                            registry.counter("coffee_order_errors_total", "type", "water").increment();
                            throw CoffeeException.notEnoughWater(drink.getName(), inventory.getWater(), drink.getWaterAmount());
                        }
                        if (inventory.getCoffee() < drink.getCoffeeAmount()) {
                            registry.counter("coffee_order_errors_total", "type", "coffee").increment();
                            throw CoffeeException.notEnoughCoffee(drink.getName(), inventory.getCoffee(), drink.getCoffeeAmount());
                        }
                        if (inventory.getMilk() < drink.getMilkAmount()) {
                            registry.counter("coffee_order_errors_total", "type", "milk").increment();
                            throw CoffeeException.notEnoughMilk(drink.getName(), inventory.getMilk(), drink.getMilkAmount());
                        }

                        inventory.setWater(inventory.getWater() - drink.getWaterAmount());
                        inventory.setCoffee(inventory.getCoffee() - drink.getCoffeeAmount());
                        inventory.setMilk(inventory.getMilk() - drink.getMilkAmount());

                        coffeeMachineService.updateInventoryAfterOrdering(inventory);

                        drinkService.addCoffeeStatistics(drink);

                        registry.counter("coffee_orders_total", "drink", drink.getName()).increment();

                        ConsoleLogger.log(drink.getName() + " готов", ConsoleLogger.LogLevel.INFO);

                        return drink;

                    } catch (CoffeeException e) {
                        registry.counter("coffee_order_errors_total", "type", "runtime").increment();
                        throw new RuntimeException(e);
                    }
                });
    }
}
