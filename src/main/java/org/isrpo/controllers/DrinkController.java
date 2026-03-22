package org.isrpo.controllers;

import org.isrpo.dtos.CreateOrUpdateDrinkDTO;
import org.isrpo.entities.Drink;
import org.isrpo.services.DrinkService;
import org.isrpo.tools.exceptions.CoffeeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing drinks
 */
@RestController
@RequestMapping("/api/drinks")
public class DrinkController {
    private final DrinkService drinkService;

    public DrinkController(DrinkService drinkService) {
        this.drinkService = drinkService;
    }

    /**
     * Endpoint of receiving all drinks
     * @return list of all drinks
     */
    @GetMapping
    public ResponseEntity<List<Drink>> getAllDrinks() {
        return ResponseEntity.ok(drinkService.getAllDrinks(true));
    }

    /**
     * Endpoint of receiving drink by id
     * @param id identifier of drink
     * @return drink with selected id
     * @throws CoffeeException if drink wasn't found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Drink> getDrink(@PathVariable("id") Long id) throws CoffeeException {
        return ResponseEntity.ok(drinkService.getDrinkById(id));
    }

    /**
     * Endpoint of receiving most popular drinks
     * @return list of 1 drink or list of drinks with same order counts
     * @throws CoffeeException if statistic is empty
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Drink>> getMostPopularDrink() throws CoffeeException {
        return ResponseEntity.ok(drinkService.getMostPopularDrinks());
    }

    /**
     * Endpoint of creating new drink
     * @param drinkDTO object representing Drink entity with all required fields
     * @return created drink
     * @throws CoffeeException if drinkDTO fields are invalid or drink with this name already exists
     */
    @PostMapping("/create")
    public ResponseEntity<Drink> createDrink(@RequestBody CreateOrUpdateDrinkDTO drinkDTO) throws CoffeeException {
        Drink drink = new Drink(drinkDTO.getName(), drinkDTO.getWaterAmount(), drinkDTO.getCoffeeAmount(), drinkDTO.getMilkAmount());
        return ResponseEntity.ok(drinkService.createDrink(drink));
    }

    /**
     * Endpoint of updating drink
     * @param id identifier of drink to be updated
     * @param drinkDTO object representing Drink entity with fields to be updated
     * @return updated drink
     * @throws CoffeeException if drinkDTO fields are invalid or drink with this name doesn't exist
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Drink> updateDrink(@PathVariable("id") Long id, @RequestBody CreateOrUpdateDrinkDTO drinkDTO) throws CoffeeException {
        Drink drink = new Drink(drinkDTO.getName(), drinkDTO.getWaterAmount(), drinkDTO.getCoffeeAmount(), drinkDTO.getMilkAmount());
        return ResponseEntity.ok(drinkService.updateDrink(id, drink));
    }
}
