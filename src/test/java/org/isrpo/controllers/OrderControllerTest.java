package org.isrpo.controllers;

import org.isrpo.entities.Drink;
import org.isrpo.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController(orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void orderDrinkById_returnsDrink() throws Exception {
        Drink americano = new Drink("Американо", 50L, 15L, 0L);
        when(orderService.orderDrink(2L, null)).thenReturn(americano);

        mockMvc.perform(get("/api/order").param("id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Американо"))
                .andExpect(jsonPath("$.waterAmount").value(50))
                .andExpect(jsonPath("$.coffeeAmount").value(15))
                .andExpect(jsonPath("$.milkAmount").value(0));
    }

    @Test
    void orderDrinkByName_returnsDrink() throws Exception {
        Drink latte = new Drink("Латте", 30L, 15L, 150L);
        when(orderService.orderDrink(null, "Латте")).thenReturn(latte);

        mockMvc.perform(get("/api/order").param("name", "Латте"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Латте"))
                .andExpect(jsonPath("$.waterAmount").value(30))
                .andExpect(jsonPath("$.coffeeAmount").value(15))
                .andExpect(jsonPath("$.milkAmount").value(150));
    }
}