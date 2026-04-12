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
        ConsoleLogger.log("BUSINESS action=create_drink name=" + drink.getName() + " water=" + drink.getWaterAmount() + " coffee=" + drink.getCoffeeAmount() + " milk=" + drink.getMilkAmount(), ConsoleLogger.LogLevel.INFO);

        if (drink.getName() == null || drink.getName().isEmpty() || drink.getName().isBlank()) {
            ConsoleLogger.log("ERROR reason=drink_name_empty", ConsoleLogger.LogLevel.ERROR);
            throw CoffeeException.recipeNameIsNullException();
        }

        if (drink.getWaterAmount() == null || drink.getCoffeeAmount() == null || drink.getMilkAmount() == null) {
            ConsoleLogger.log("ERROR reason=drink_ingredient_null name=" + drink.getName(), ConsoleLogger.LogLevel.ERROR);
            throw CoffeeException.recipeIngredientIsNullException();
        }

        if (drink.getWaterAmount() + drink.getCoffeeAmount() + drink.getMilkAmount() == 0) {
            ConsoleLogger.log("ERROR reason=drink_all_amounts_zero name=" + drink.getName(), ConsoleLogger.LogLevel.ERROR);
            throw CoffeeException.recipeAmountsAreZeroException();
        }

        if (drink.getWaterAmount() < 0 || drink.getCoffeeAmount() < 0 || drink.getMilkAmount() < 0) {
            ConsoleLogger.log("ERROR reason=drink_ingredient_negative name=" + drink.getName() + " water=" + drink.getWaterAmount() + " coffee=" + drink.getCoffeeAmount() + " milk=" + drink.getMilkAmount(), ConsoleLogger.LogLevel.ERROR);
            throw CoffeeException.recipeIngredientIsNegativeException();
        }

        Drink existingDrink = getDrinkByName(drink.getName(), false);

        if (existingDrink != null) {
            throw CoffeeException.recipeTypeAlreadyExistsException();
        }

        drinkRepository.save(drink);
        registry.counter("drinks_created_total").increment();

        ConsoleLogger.log("BUSINESS action=drink_created name=" + drink.getName(), ConsoleLogger.LogLevel.INFO);

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
        ConsoleLogger.log("BUSINESS action=update_drink id=" + id + " name=" + newDrink.getName() + " water=" + newDrink.getWaterAmount() + " coffee=" + newDrink.getCoffeeAmount() + " milk=" + newDrink.getMilkAmount(), ConsoleLogger.LogLevel.INFO);

        Drink existingDrink = getDrinkById(id);

        if (newDrink.getName() != null && !newDrink.getName().isEmpty() && !newDrink.getName().isBlank()) {
            existingDrink.setName(newDrink.getName());
        }

        if (newDrink.getWaterAmount() != null) {
            if (newDrink.getWaterAmount() < 0) {
                ConsoleLogger.log("ERROR reason=drink_ingredient_negative ingredient=water value=" + newDrink.getWaterAmount(), ConsoleLogger.LogLevel.ERROR);
                throw CoffeeException.recipeIngredientIsNegativeException();
            }

            existingDrink.setWaterAmount(newDrink.getWaterAmount());
        }

        if (newDrink.getCoffeeAmount() != null) {
            if (newDrink.getCoffeeAmount() < 0) {
                ConsoleLogger.log("ERROR reason=drink_ingredient_negative ingredient=coffee value=" + newDrink.getCoffeeAmount(), ConsoleLogger.LogLevel.ERROR);
                throw CoffeeException.recipeIngredientIsNegativeException();
            }

            existingDrink.setCoffeeAmount(newDrink.getCoffeeAmount());
        }

        if (newDrink.getMilkAmount() != null) {
            if (newDrink.getMilkAmount() < 0) {
                ConsoleLogger.log("ERROR reason=drink_ingredient_negative ingredient=milk value=" + newDrink.getMilkAmount(), ConsoleLogger.LogLevel.ERROR);
                throw CoffeeException.recipeIngredientIsNegativeException();
            }

            existingDrink.setMilkAmount(newDrink.getMilkAmount());
        }

        ConsoleLogger.log("BUSINESS action=drink_updated id=" + id + " name=" + existingDrink.getName(), ConsoleLogger.LogLevel.INFO);

        return drinkRepository.save(existingDrink);
    }

    /**
     * Method of receiving drink by id
     * @param id identifier of drink
     * @return drink with selected id
     * @throws CoffeeException if drink wasn't found
     */
    public Drink getDrinkById(Long id) throws CoffeeException {
        ConsoleLogger.log("BUSINESS action=get_drink_by_id id=" + id, ConsoleLogger.LogLevel.INFO);
        try {
            return drinkRepository.findById(id)
                    .orElseThrow(CoffeeException::recipeNotFoundException);
        } catch (CoffeeException e) {
            ConsoleLogger.log("ERROR reason=drink_not_found id=" + id, ConsoleLogger.LogLevel.ERROR);
            registry.counter("drink_errors_total", "type", "drink_not_found").increment();
            throw e;
        }
    }

    /**
     * Method of receiving drink by name
     * @param name name of drink
     * @return drink with selected name
     * @throws CoffeeException if drink wasn't found
     */
    public Drink getDrinkByName(String name, boolean throwIfNotExists) throws CoffeeException {
        ConsoleLogger.log("BUSINESS action=get_drink_by_name name=" + name, ConsoleLogger.LogLevel.INFO);
        try {
            if (throwIfNotExists) {
                return drinkRepository.findByName(name).orElseThrow(CoffeeException::recipeNotFoundException);
            } else {
                return drinkRepository.findByName(name).orElse(null);
            }

        } catch (CoffeeException e) {
            ConsoleLogger.log("ERROR reason=drink_not_found name=" + name, ConsoleLogger.LogLevel.ERROR);
            throw e;
        }
    }

    /**
     * Method of receiving all drinks
     * @return list of all drinks
     */
    public List<Drink> getAllDrinks(boolean log) {
        if (log) {
            ConsoleLogger.log("BUSINESS action=get_all_drinks", ConsoleLogger.LogLevel.INFO);
            registry.counter("drinks_get_all").increment();
        }

        return drinkRepository.findAll();
    }

    /**
     * Method of receiving most popular drinks
     * @return list of 1 drink or list of drinks with same order counts
     * @throws CoffeeException if statistic is empty
     */
    public List<Drink> getMostPopularDrinks() throws CoffeeException {
        ConsoleLogger.log("BUSINESS action=get_most_popular_drinks", ConsoleLogger.LogLevel.INFO);

        List<Object[]> results = drinkStatisticsRepository.findDrinkOrdersCount();

        if (results.isEmpty()) {
            ConsoleLogger.log("WARN reason=drink_statistics_empty", ConsoleLogger.LogLevel.WARNING);
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
            ConsoleLogger.log("ERROR reason=drink_is_null_for_statistics", ConsoleLogger.LogLevel.ERROR);
            throw CoffeeException.recipeIsNullException();
        }

        DrinkStatistics stats = new DrinkStatistics(drink);
        ConsoleLogger.log("BUSINESS action=add_drink_statistics name=" + drink.getName(), ConsoleLogger.LogLevel.INFO);
        drinkStatisticsRepository.save(stats);
        registry.counter("drink_orders_statistics_total").increment();
    }
}
