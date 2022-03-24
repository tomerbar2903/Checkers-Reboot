package com.example.checkers;

public abstract class GamerAICalculations {

    public static double bottomRowOccupiedEvaluate(int amount) {
        // -0.05x^2 + 0.4x + 0.2
        return -0.05 * (amount * amount) + 0.4 * (amount) + 0.2;
    }
}
