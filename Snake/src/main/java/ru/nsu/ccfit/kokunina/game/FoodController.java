package ru.nsu.ccfit.kokunina.game;

import ru.nsu.ccfit.kokunina.game.exceptions.CellNotEmptyException;

import java.util.Random;

public class FoodController {
    private final Field field;
    private final int staticFoodCount;
    private final double foodPerPlayer;

    private int currentFoodCount;

    public FoodController(Field field, int staticFoodCount, double foodPerPlayer) {
        this.field = field;
        this.staticFoodCount = staticFoodCount;
        this.foodPerPlayer = foodPerPlayer;
    }

    private int recountFood(int aliveSnakes) {
        return (int) (staticFoodCount + aliveSnakes * foodPerPlayer);
    }

    public void addNewFood(Coordinates foodCoord) throws CellNotEmptyException {
        int x = foodCoord.getX();
        int y = foodCoord.getY();
        if (field.getCell(y, x).getState() == CellState.EMPTY) {
            field.getCell(y, x).setState(CellState.FOOD);
        } else {
            throw new CellNotEmptyException("Cell is not empty!");
        }
        currentFoodCount++;
    }

    /**
     * Remove food from cell and place snake on it
     * @param foodCoord coordinates on the field
     */
    public void eatFood(Coordinates foodCoord) {
        field.getCell(foodCoord.getY(), foodCoord.getX()).setState(CellState.SNAKE);
        currentFoodCount--;
    }

    /**
     * Add to a random free cell new food if necessary
     * @param aliveSnakes depends on this parameter adds new food or not
     */
    void update(int aliveSnakes) {
        int foodToAdd = recountFood(aliveSnakes) - currentFoodCount;
        if (foodToAdd <= 0) {
            return;
        }
        for (int i = 0; i < foodToAdd; i++) {
            addNewFoodOnRandomCell();
        }
    }

    private void addNewFoodOnRandomCell() {
        int randomX = new Random().nextInt(field.getWidth());
        int randomY = new Random().nextInt(field.getHeight());
        while (true) {
            try {
                addNewFood(new Coordinates(randomX, randomY));
                return;
            } catch (CellNotEmptyException e) {
                e.printStackTrace();
            }
        }
    }
}
