package org.isrpo.controllers;

import org.isrpo.entities.Drink;
import org.isrpo.services.OrderService;
import org.isrpo.tools.exceptions.CoffeeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for ordering drink
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Endpoint of ordering drink
     * @param id identifier of drink to be ordered
     * @param name name of drink to be ordered
     * @return ordered drink
     * @throws CoffeeException if drink with this is or name doesn't exist or not enough ingredients in coffee machine
     */
    @GetMapping()
    public ResponseEntity<Drink> orderDrink(@RequestParam(value = "id", required = false) Long id, @RequestParam(value = "name", required = false) String name) throws CoffeeException {
        return ResponseEntity.ok(orderService.orderDrink(id, name));
    }
}
