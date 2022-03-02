package com.example.checkers;

public abstract class BitboardEssentials {

    // WHITE TILES MASK
    public static final long WHITE_TILES = 0xaa55aa55aa55aa55L;

    // DARK QUEEN MASKS
    public static final long DARK_QUEEN = 0xaa;


    // LIGHT QUEEN MASKS
    public static final long LIGHT_QUEEN = 0x5500000000000000L;

    // GAME INITIATE MASKS
    public  static final long DARK_INIT = 0x55aa550000000000L;
    public  static final long LIGHT_INIT = 0xaa55aa;

    // SIMPLE VALID MOVES - STEP MASKS
    public static final int VALID_STEP1 = VisualBoard.getDimension() + 1;
    public static final int VALID_STEP2 = VisualBoard.getDimension() - 1;

    // SIMPLE VALID EATING MOVES MASKS
    public static final int VALID_EATING_STEP1 = 2 * VALID_STEP1;
    public static final int VALID_EATING_STEP2 = 2 * VALID_STEP2;
}
