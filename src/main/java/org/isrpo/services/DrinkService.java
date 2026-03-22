package org.isrpo.services;

import io.micrometer.core.instrument.MeterRegistry;
import org.isrpo.entities.Drink;
import org.isrpo.entities.DrinkStatistics;
import org.isrpo.repositories.DrinkRepository;
import org.isrpo.repositories.DrinkStatisticsRepository;
import org.isrpo.tools.ConsoleLogger;
import org.isrpo.tools.exceptions.CoffeeException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing drinks
 */
@Service
public class DrinkService {
    private final DrinkRepository drinkRepository;
    private final DrinkStatisticsRepository drinkStatisticsRepository;
    private final MeterRegistry registry;

    public DrinkService(DrinkRepository drinkRepository, DrinkStatisticsRepository drinkStatisticsRepository, MeterRegistry registry) {
        this.drinkRepository = drinkRepository;
        this.drinkStatisticsRepository = drinkStatisticsRepository;
        this.registry = registry;
    }

    /**
     * Method of creating new drink
     * @param drink Drink entity with all required fields
     * @return created drink
     * @throws CoffeeException if arguments are invalid or drink with this name already exists
     */
    public Drink createDrink(Drink drink) throws CoffeeException {
        ConsoleLogger.log("Получен запрос на добавление напитка: " + drink.getName(), ConsoleLogger.LogLevel.INFO);

        if (drink.getName() == null || drink.getName().isEmpty() || drink.getName().isBlank()) {
            ConsoleLogger.log("Передано пустое название напитка.", ConsoleLogger.LogLevel.ERROR);
            throw CoffeeException.recipeNameIsNullException();
        }

        if (drink.getWaterAmount() == null || drink.getCoffeeAmount() == null || drink.getMilkAmount() == null) {
            ConsoleLogger.log("Передан(ы) пустой(ые) необходимый(е) ингредиент(ы)", ConsoleLogger.LogLevel.ERROR);
            throw CoffeeException.recipeIngredientIsNullException();
        }

        if (drink.getWaterAmount() + drink.getCoffeeAmount() + drink.getMilkAmount() == 0) {
            ConsoleLogger.log("Передан напиток, не требующий ингредиентов", ConsoleLogger.LogLevel.ERROR);
            throw CoffeeException.recipeAmountsAreZeroException();
        }

        if (drink.getWaterAmount() < 0 || drink.getCoffeeAmount() < 0 || drink.getMilkAmount() < 0) {
            ConsoleLogger.log("Передан(ы) ингредиент(ы) с отрицательным значением", ConsoleLogger.LogLevel.ERROR);
            throw CoffeeException.recipeIngredientIsNegativeException();
        }

        Drink existingDrink = getDrinkByName(drink.getName());

        if (existingDrink != null) {
            throw CoffeeException.recipeTypeAlreadyExistsException();
        }

        drinkRepository.save(drink);
        registry.counter("drinks_created_total").increment();

        ConsoleLogger.log("Добавлен напиток: " + drink.getName(), ConsoleLogger.LogLevel.INFO);

        return drink;
    }

    /**
     * Method of updating drink
     * @param id identifier of drink to be updated
     * @param newDrink Drink entity with fields to be updated
     * @return updated drink
     * @throws CoffeeException if arguments are invalid or drink with this name doesn't exist
     */
    public Drink updateDrink(Long id, Drink newDrink) throws CoffeeException {
        ConsoleLogger.log("Получен запрос на обновление напитка с id: " + id, ConsoleLogger.LogLevel.INFO);

        Drink existingDrink = getDrinkById(id);

        if (newDrink.getName() != null && !newDrink.getName().isEmpty() && !newDrink.getName().isBlank()) {
            existingDrink.setName(newDrink.getName());
        }

        if (newDrink.getWaterAmount() != null) {
            if (newDrink.getWaterAmount() < 0) {
                ConsoleLogger.log("Передан ингредиет (вода) с отрицательным значением", ConsoleLogger.LogLevel.ERROR);
                throw CoffeeException.recipeIngredientIsNegativeException();
            }

            existingDrink.setWaterAmount(newDrink.getWaterAmount());
        }

        if (newDrink.getCoffeeAmount() != null) {
            if (newDrink.getCoffeeAmount() < 0) {
                ConsoleLogger.log("Передан ингредиет (кофе) с отрицательным значением", ConsoleLogger.LogLevel.ERROR);
                throw CoffeeException.recipeIngredientIsNegativeException();
            }

            existingDrink.setCoffeeAmount(newDrink.getCoffeeAmount());
        }

        if (newDrink.getMilkAmount() != null) {
            if (newDrink.getMilkAmount() < 0) {
                ConsoleLogger.log("Передан ингредиет (молоко) с отрицательным значением", ConsoleLogger.LogLevel.ERROR);
                throw CoffeeException.recipeIngredientIsNegativeException();
            }

            existingDrink.setMilkAmount(newDrink.getMilkAmount());
        }

        ConsoleLogger.log("Обновлён напиток: " + existingDrink.getName(), ConsoleLogger.LogLevel.INFO);

        return drinkRepository.save(existingDrink);
    }

    /**
     * Method of receiving drink by id
     * @param id identifier of drink
     * @return drink with selected id
     * @throws CoffeeException if drink wasn't found
     */
    public Drink getDrinkById(Long id) throws CoffeeException {
        ConsoleLogger.log("Получен запрос на получение напитка с id: " + id, ConsoleLogger.LogLevel.INFO);
        try {
            return drinkRepository.findById(id)
                    .orElseThrow(CoffeeException::recipeNotFoundException);
        } catch (CoffeeException e) {
            ConsoleLogger.log("Напиток с id " + id + " не найден.", ConsoleLogger.LogLevel.ERROR);
            registry.counter("drink_errors_total", "type", "not_found").increment();
            throw e;
        }
    }

    /**
     * Method of receiving drink by name
     * @param name name of drink
     * @return drink with selected name
     * @throws CoffeeException if drink wasn't found
     */
    public Drink getDrinkByName(String name) throws CoffeeException {
        ConsoleLogger.log("Получен запрос на получение напитка с названием: " + name, ConsoleLogger.LogLevel.INFO);
        try {
            return drinkRepository.findByName(name).orElseThrow(CoffeeException::recipeNotFoundException);
        } catch (CoffeeException e) {
            ConsoleLogger.log("Напиток с названием " + name + " не найден.", ConsoleLogger.LogLevel.ERROR);
            throw e;
        }
    }

    /**
     * Method of receiving all drinks
     * @return list of all drinks
     */
    public List<Drink> getAllDrinks(boolean log) {
        if (log) {
            ConsoleLogger.log("Получен запрос на получение всех напитков", ConsoleLogger.LogLevel.INFO);
        }

        registry.counter("drinks_get_all").increment();
        return drinkRepository.findAll();
    }

    /**
     * Method of receiving most popular drinks
     * @return list of 1 drink or list of drinks with same order counts
     * @throws CoffeeException if statistic is empty
     */
    public List<Drink> getMostPopularDrinks() throws CoffeeException {
        ConsoleLogger.log("Получен запрос на получение самого популярного напитка", ConsoleLogger.LogLevel.INFO);

        List<Object[]> results = drinkStatisticsRepository.findDrinkOrdersCount();

        if (results.isEmpty()) {
            throw CoffeeException.coffeeStatisticIsEmptyException();
        }

        Long maxOrdersCount = (Long) results.get(0)[1];

        return results.stream()
                .filter(result -> result[1].equals(maxOrdersCount))
                .map(result -> (Drink) result[0])
                .collect(Collectors.toList());
    }

    /**
     * Method of adding a drink order record
     * @param drink drink that was ordered
     * @throws CoffeeException if drink doesn't exist
     */
    public void addCoffeeStatistics(Drink drink) throws CoffeeException {
        if (drink == null) {
            throw CoffeeException.recipeIsNullException();
        }

        DrinkStatistics stats = new DrinkStatistics(drink);
        drinkStatisticsRepository.save(stats);
        registry.counter("drink_orders_statistics_total").increment();
    }
}
