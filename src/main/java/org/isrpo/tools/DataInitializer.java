package org.isrpo.tools;

import org.isrpo.entities.Drink;
import org.isrpo.entities.MachineInventory;
import org.isrpo.repositories.MachineInventoryRepository;
import org.isrpo.services.CoffeeMachineService;
import org.isrpo.services.DrinkService;
import org.isrpo.tools.exceptions.CoffeeException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Class-initialization of base drinks and creating coffee machine inventory if it doesn't exist
 */
@Component
public class DataInitializer implements CommandLineRunner {
    private final DrinkService drinkService;
    private final CoffeeMachineService coffeeMachineService;
    private final MachineInventoryRepository machineInventoryRepository;

    public DataInitializer(DrinkService drinkService, CoffeeMachineService coffeeMachineService, MachineInventoryRepository machineInventoryRepository) {
        this.drinkService = drinkService;
        this.coffeeMachineService = coffeeMachineService;
        this.machineInventoryRepository = machineInventoryRepository;
    }

    @Override
    public void run(String... args) throws CoffeeException {
        createDrinkIfNotExists("Эспрессо", 30L, 10L, 0L);
        createDrinkIfNotExists("Американо", 50L, 10L, 0L);
        createDrinkIfNotExists("Капучино", 40L, 10L, 20L);

        if (machineInventoryRepository.findById(1L).isEmpty()) {
            MachineInventory inventory = new MachineInventory(1000L, 500L, 300L);
            coffeeMachineService.createInventory(inventory);
        }
    }

    private void createDrinkIfNotExists(String name, Long waterAmount, Long coffeeAmount, Long milkAmount) throws CoffeeException {
        if (drinkService.getAllDrinks(false).stream().noneMatch(drink -> drink.getName().equals(name))) {
            Drink drink = new Drink(name, waterAmount, coffeeAmount, milkAmount);
            drinkService.createDrink(drink);
        }
    }
}
