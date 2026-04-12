package org.isrpo.services;

import io.micrometer.core.instrument.Gauge;
import org.isrpo.entities.MachineInventory;
import org.isrpo.repositories.MachineInventoryRepository;
import org.isrpo.tools.ConsoleLogger;
import org.isrpo.tools.exceptions.CoffeeException;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Service for managing coffee machine
 */
@Service
public class CoffeeMachineService {
    private final MachineInventoryRepository machineInventoryRepository;
    private final MeterRegistry registry;

    public CoffeeMachineService(MachineInventoryRepository machineInventoryRepository, MeterRegistry registry) {
        this.machineInventoryRepository = machineInventoryRepository;
        this.registry = registry;
    }

    /**
     * Method of receiving inventory of coffee machine
     * @return inventory of coffee machine with balance of water, coffee and milk
     * @throws CoffeeException if inventory isn't initialized
     */
    public MachineInventory getInventory(Long id) throws CoffeeException {
        ConsoleLogger.log("BUSINESS action=get_inventory inventoryId=" + id, ConsoleLogger.LogLevel.INFO);
        try {
            MachineInventory inventory = machineInventoryRepository.findById(id)
                    .orElseThrow(CoffeeException::coffeeMachineInventoryNotInitializedException);

            if (inventory != null) {
                Gauge.builder("coffee_inventory_water", machineInventoryRepository,
                                repo -> repo.findById(1L).map(MachineInventory::getWater).orElse(0L))
                        .description("Current water level in coffee machine")
                        .register(registry);

                Gauge.builder("coffee_inventory_coffee", machineInventoryRepository,
                                repo -> repo.findById(1L).map(MachineInventory::getCoffee).orElse(0L))
                        .description("Current coffee level in coffee machine")
                        .register(registry);

                Gauge.builder("coffee_inventory_milk", machineInventoryRepository,
                                repo -> repo.findById(1L).map(MachineInventory::getMilk).orElse(0L))
                        .description("Current milk level in coffee machine")
                        .register(registry);
            }

            return inventory;
        } catch (CoffeeException e) {
            ConsoleLogger.log("ERROR reason=inventory_not_initialized inventoryId=" + id, ConsoleLogger.LogLevel.ERROR);
            throw e;
        }
    }

    /**
     * Method of initializing coffee machine inventory
     * @param inventory MachineInventory entity to be saved
     * @return created inventory
     * @throws CoffeeException if inventory fields are invalid
     */
    public MachineInventory createInventory(MachineInventory inventory) throws CoffeeException {
        ConsoleLogger.log("BUSINESS action=create_inventory water=" + inventory.getWater() + " coffee=" + inventory.getCoffee() + " milk=" + inventory.getMilk(), ConsoleLogger.LogLevel.INFO);
        if (inventory.getWater() == null || inventory.getCoffee() == null || inventory.getMilk() == null) {
            ConsoleLogger.log("ERROR reason=inventory_ingredient_null", ConsoleLogger.LogLevel.ERROR);
            throw CoffeeException.recipeIngredientIsNullException();
        }

        if (inventory.getWater() < 0 || inventory.getCoffee() < 0 || inventory.getMilk() < 0) {
            ConsoleLogger.log("ERROR reason=inventory_ingredient_negative water=" + inventory.getWater() + " coffee=" + inventory.getCoffee() + " milk=" + inventory.getMilk(), ConsoleLogger.LogLevel.ERROR);
            throw CoffeeException.recipeIngredientIsNegativeException();
        }

        machineInventoryRepository.save(inventory);
        ConsoleLogger.log("BUSINESS action=inventory_created water=" + inventory.getWater() + " coffee=" + inventory.getCoffee() + " milk=" + inventory.getMilk(), ConsoleLogger.LogLevel.INFO);
        registry.counter("inventory_created_total").increment();
        return inventory;
    }

    /**
     * Method of updating inventory of coffee machine
     * @param id always 1
     * @param newInventory MachineInventory entity with fields to be updated
     * @return updated inventory of coffee machine with balance of water, coffee and milk
     * @throws CoffeeException if inventory isn't initialized or inventoryDTO field are invalid
     */
    public MachineInventory updateInventory(Long id, MachineInventory newInventory) throws CoffeeException {
        ConsoleLogger.log("BUSINESS action=update_inventory inventoryId=" + id + " water=" + newInventory.getWater() + " coffee=" + newInventory.getCoffee() + " milk=" + newInventory.getMilk(), ConsoleLogger.LogLevel.INFO);

        MachineInventory existingInventory = getInventory(id);

        if (newInventory.getWater() != null) {
            if (newInventory.getWater() < 0) {
                ConsoleLogger.log("ERROR reason=negative_inventory_value ingredient=water value=" + newInventory.getWater(), ConsoleLogger.LogLevel.ERROR);
                throw CoffeeException.coffeeMachineIngredientIsNegativeException();
            }

            existingInventory.setWater(newInventory.getWater());
        }

        if (newInventory.getCoffee() != null) {
            if (newInventory.getCoffee() < 0) {
                ConsoleLogger.log("ERROR reason=negative_inventory_value ingredient=coffee value=" + newInventory.getCoffee(), ConsoleLogger.LogLevel.ERROR);
                throw CoffeeException.coffeeMachineIngredientIsNegativeException();
            }

            existingInventory.setCoffee(newInventory.getCoffee());
        }

        if (newInventory.getMilk() != null) {
            if (newInventory.getMilk() < 0) {
                ConsoleLogger.log("ERROR reason=negative_inventory_value ingredient=milk value=" + newInventory.getMilk(), ConsoleLogger.LogLevel.ERROR);
                throw CoffeeException.coffeeMachineIngredientIsNegativeException();
            }

            existingInventory.setMilk(newInventory.getMilk());
        }

        ConsoleLogger.log("BUSINESS action=inventory_updated inventoryId=" + id + " water=" + existingInventory.getWater() + " coffee=" + existingInventory.getCoffee() + " milk=" + existingInventory.getMilk(), ConsoleLogger.LogLevel.INFO);
        registry.counter("inventory_updates_total").increment();

        return machineInventoryRepository.save(existingInventory);
    }

    /**
     * Method of updating coffee machine inventory after ordering drink
     * @param inventory  MachineInventory entity with fields to be updated
     */
    public void updateInventoryAfterOrdering(MachineInventory inventory) {
        machineInventoryRepository.save(inventory);
        ConsoleLogger.log("BUSINESS action=inventory_updated_after_order water=" + inventory.getWater() + " coffee=" + inventory.getCoffee() + " milk=" + inventory.getMilk(), ConsoleLogger.LogLevel.INFO);
        registry.counter("inventory_updates_total", "type", "order").increment();
    }
}
