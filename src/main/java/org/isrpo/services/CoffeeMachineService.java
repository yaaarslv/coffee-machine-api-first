package org.isrpo.services;

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
        ConsoleLogger.log("Получен запрос на получение информации о засапах кофемашины", ConsoleLogger.LogLevel.INFO);
        try {
            return machineInventoryRepository.findById(id)
                    .orElseThrow(CoffeeException::coffeeMachineInventoryNotInitializedException);
        } catch (CoffeeException e) {
            ConsoleLogger.log("Запасы кофемашины не инициализированы", ConsoleLogger.LogLevel.ERROR);
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
        if (inventory.getWater() == null || inventory.getCoffee() == null || inventory.getMilk() == null) {
            throw CoffeeException.recipeIngredientIsNullException();
        }

        if (inventory.getWater() < 0 || inventory.getCoffee() < 0 || inventory.getMilk() < 0) {
            throw CoffeeException.recipeIngredientIsNegativeException();
        }

        machineInventoryRepository.save(inventory);
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
        ConsoleLogger.log("Получен запрос на обновление запаса с id: " + id, ConsoleLogger.LogLevel.INFO);

        MachineInventory existingInventory = getInventory(id);

        if (newInventory.getWater() != null) {
            if (newInventory.getWater() < 0) {
                ConsoleLogger.log("Передан ингредиет (вода) с отрицательным значением", ConsoleLogger.LogLevel.ERROR);
                throw CoffeeException.coffeeMachineIngredientIsNegativeException();
            }

            existingInventory.setWater(newInventory.getWater());
        }

        if (newInventory.getCoffee() != null) {
            if (newInventory.getCoffee() < 0) {
                ConsoleLogger.log("Передан ингредиет (кофе) с отрицательным значением", ConsoleLogger.LogLevel.ERROR);
                throw CoffeeException.coffeeMachineIngredientIsNegativeException();
            }

            existingInventory.setCoffee(newInventory.getCoffee());
        }

        if (newInventory.getMilk() != null) {
            if (newInventory.getMilk() < 0) {
                ConsoleLogger.log("Передан ингредиет (молоко) с отрицательным значением", ConsoleLogger.LogLevel.ERROR);
                throw CoffeeException.coffeeMachineIngredientIsNegativeException();
            }

            existingInventory.setMilk(newInventory.getMilk());
        }

        ConsoleLogger.log("Запасы кофемашины обновлены ", ConsoleLogger.LogLevel.INFO);
        registry.counter("inventory_updates_total").increment();

        return machineInventoryRepository.save(existingInventory);
    }

    /**
     * Method of updating coffee machine inventory after ordering drink
     * @param inventory  MachineInventory entity with fields to be updated
     */
    public void updateInventoryAfterOrdering(MachineInventory inventory) {
        machineInventoryRepository.save(inventory);
        registry.counter("inventory_updates_total", "type", "order").increment();
    }
}
