package org.isrpo.controllers;

import org.isrpo.dtos.UpdateInventoryDTO;
import org.isrpo.entities.MachineInventory;
import org.isrpo.services.CoffeeMachineService;
import org.isrpo.tools.exceptions.CoffeeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing coffee machine
 */
@RestController
@RequestMapping("/api/machine")
public class CoffeeMachineController {
    private final CoffeeMachineService coffeeMachineService;

    public CoffeeMachineController(CoffeeMachineService coffeeMachineService) {
        this.coffeeMachineService = coffeeMachineService;
    }

    /**
     * Endpoint of receiving inventory of coffee machine
     * @return inventory of coffee machine with balance of water, coffee and milk
     * @throws CoffeeException if inventory isn't initialized
     */
    @GetMapping("/inventory")
    public ResponseEntity<MachineInventory> getInventory() throws CoffeeException {
        return ResponseEntity.ok(coffeeMachineService.getInventory(1L));
    }

    /**
     * Endpoint of updating inventory of coffee machine
     * @param inventoryDTO object representing MachineInventory entity with fields to be updated
     * @return updated inventory of coffee machine with balance of water, coffee and milk
     * @throws CoffeeException if inventory isn't initialized or inventoryDTO field are invalid
     */
    @PutMapping("/update")
    public ResponseEntity<MachineInventory> updateInventory(@RequestBody UpdateInventoryDTO inventoryDTO) throws CoffeeException {
        MachineInventory inventory = new MachineInventory(inventoryDTO.getWater(), inventoryDTO.getCoffee(), inventoryDTO.getMilk());
        return ResponseEntity.ok(coffeeMachineService.updateInventory(1L, inventory));
    }
}
