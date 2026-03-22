package org.isrpo.tools.exceptions;

/**
 * Custom exceptions for operations on drinks, coffee machine inventory
 */
public class CoffeeException extends Exception {
    private CoffeeException(String message) {
        super(message);
    }

    public static CoffeeException recipeTypeNotSelectedException() {
        return new CoffeeException("Не выбран рецепт для приготовления кофе");
    }

    public static CoffeeException recipeTypeAlreadyExistsException() {
        return new CoffeeException("Данный рецепт приготовления кофе уже существует");
    }

    public static CoffeeException recipeIsNullException() {
        return new CoffeeException("Передан пустой объект рецепта кофе");
    }

    public static CoffeeException recipeNameIsNullException() {
        return new CoffeeException("Пустое название рецепта кофе");
    }

    public static CoffeeException recipeIngredientIsNullException() {
        return new CoffeeException("Пустой ингредиент рецепта кофе");
    }

    public static CoffeeException recipeNotFoundException() {
        return new CoffeeException("Данный рецепт кофе не найден");
    }

    public static CoffeeException recipeIngredientIsNegativeException() {
        return new CoffeeException("Передан отрицательный ингредиент");
    }

    public static CoffeeException recipeAmountsAreZeroException() {
        return new CoffeeException("В рецепте должен присутствовать хотя бы один ненулевой ингредиент");
    }

    public static CoffeeException notEnoughWater(String coffeeName, Long inventoryWater, Long waterAmount) {
        return new CoffeeException("Недостаточно воды для приготовления " + coffeeName + ". В наличии: " + inventoryWater + ", требуется: " + waterAmount);
    }

    public static CoffeeException notEnoughCoffee(String coffeeName, Long inventoryCoffee, Long coffeeAmount) {
        return new CoffeeException("Недостаточно кофе для приготовления " + coffeeName + ". В наличии: " + inventoryCoffee + ", требуется: " + coffeeAmount);
    }

    public static CoffeeException notEnoughMilk(String coffeeName, Long inventoryMilk, Long milkAmount) {
        return new CoffeeException("Недостаточно молока для приготовления " + coffeeName + ". В наличии: " + inventoryMilk + ", требуется: " + milkAmount);
    }

    public static CoffeeException coffeeStatisticIsEmptyException() {
        return new CoffeeException("Статистика пустая");
    }

    public static CoffeeException coffeeMachineInventoryNotInitializedException() {
        return new CoffeeException("Запасы кофемашины не инициализированы");
    }

    public static CoffeeException coffeeMachineIngredientIsNegativeException() {
        return new CoffeeException("Передан отрицательный ингредиент для кофемашины");
    }
}