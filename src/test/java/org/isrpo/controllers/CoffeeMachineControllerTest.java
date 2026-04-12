package org.isrpo.controllers;

import org.isrpo.entities.MachineInventory;
import org.isrpo.services.CoffeeMachineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CoffeeMachineControllerTest {

    @Mock
    private CoffeeMachineService coffeeMachineService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CoffeeMachineController controller = new CoffeeMachineController(coffeeMachineService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getInventory_returnsOk() throws Exception {
        MachineInventory inventory = new MachineInventory(1000L, 500L, 300L);
        when(coffeeMachineService.getInventory(1L)).thenReturn(inventory);

        mockMvc.perform(get("/api/machine/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.water").value(1000))
                .andExpect(jsonPath("$.coffee").value(500))
                .andExpect(jsonPath("$.milk").value(300));
    }

    @Test
    void updateInventory_returnsUpdatedInventory() throws Exception {
        MachineInventory inventory = new MachineInventory(900L, 450L, 250L);
        when(coffeeMachineService.updateInventory(eq(1L), any(MachineInventory.class))).thenReturn(inventory);

        String body = """
                {
                  "water": 900,
                  "coffee": 450,
                  "milk": 250
                }
                """;

        mockMvc.perform(put("/api/machine/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.water").value(900))
                .andExpect(jsonPath("$.coffee").value(450))
                .andExpect(jsonPath("$.milk").value(250));
    }
}