package org.isrpo.controllers;

import org.isrpo.entities.Drink;
import org.isrpo.services.DrinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DrinkControllerTest {

    @Mock
    private DrinkService drinkService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DrinkController controller = new DrinkController(drinkService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllDrinks_returnsOk() throws Exception {
        Drink espresso = new Drink("Espresso", 50L, 20L, 0L);
        Drink latte = new Drink("Latte", 30L, 15L, 150L);

        when(drinkService.getAllDrinks(true)).thenReturn(List.of(espresso, latte));

        mockMvc.perform(get("/api/drinks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Espresso"))
                .andExpect(jsonPath("$[1].name").value("Latte"));
    }

    @Test
    void createDrink_returnsCreatedDrink() throws Exception {
        Drink latte = new Drink("Latte", 30L, 15L, 150L);
        when(drinkService.createDrink(any(Drink.class))).thenReturn(latte);

        String body = """
                {
                  "name": "Latte",
                  "waterAmount": 30,
                  "coffeeAmount": 15,
                  "milkAmount": 150
                }
                """;

        mockMvc.perform(post("/api/drinks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Latte"))
                .andExpect(jsonPath("$.waterAmount").value(30))
                .andExpect(jsonPath("$.coffeeAmount").value(15))
                .andExpect(jsonPath("$.milkAmount").value(150));
    }

    @Test
    void updateDrink_returnsUpdatedDrink() throws Exception {
        Drink cappuccino = new Drink("Cappuccino", 40L, 20L, 120L);
        when(drinkService.updateDrink(eq(1L), any(Drink.class))).thenReturn(cappuccino);

        String body = """
                {
                  "name": "Cappuccino",
                  "waterAmount": 40,
                  "coffeeAmount": 20,
                  "milkAmount": 120
                }
                """;

        mockMvc.perform(put("/api/drinks/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cappuccino"))
                .andExpect(jsonPath("$.waterAmount").value(40))
                .andExpect(jsonPath("$.coffeeAmount").value(20))
                .andExpect(jsonPath("$.milkAmount").value(120));
    }
}