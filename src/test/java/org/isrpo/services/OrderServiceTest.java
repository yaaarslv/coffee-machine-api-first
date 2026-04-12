package org.isrpo.services;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.isrpo.entities.Drink;
import org.isrpo.entities.MachineInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private DrinkService drinkService;
    private CoffeeMachineService coffeeMachineService;
    private SimpleMeterRegistry meterRegistry;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        drinkService = mock(DrinkService.class);
        coffeeMachineService = mock(CoffeeMachineService.class);
        meterRegistry = new SimpleMeterRegistry();
        orderService = new OrderService(drinkService, coffeeMachineService, meterRegistry);
    }

    @Test
    void orderDrink_successfullyUpdatesInventoryAndMetrics() throws Exception {
        Drink drink = new Drink("Espresso", 50L, 20L, 0L);
        MachineInventory inventory = new MachineInventory(200L, 100L, 50L);

        when(drinkService.getDrinkById(1L)).thenReturn(drink);
        when(coffeeMachineService.getInventory(1L)).thenReturn(inventory);

        Drink result = orderService.orderDrink(1L, null);

        assertEquals("Espresso", result.getName());

        ArgumentCaptor<MachineInventory> captor = ArgumentCaptor.forClass(MachineInventory.class);
        verify(coffeeMachineService).updateInventoryAfterOrdering(captor.capture());
        verify(drinkService).addCoffeeStatistics(drink);

        MachineInventory updated = captor.getValue();
        assertEquals(150L, updated.getWater());
        assertEquals(80L, updated.getCoffee());
        assertEquals(50L, updated.getMilk());

        double counterValue = meterRegistry
                .get("coffee_orders_total")
                .tag("drink", "Espresso")
                .counter()
                .count();

        assertEquals(1.0, counterValue);
    }
}