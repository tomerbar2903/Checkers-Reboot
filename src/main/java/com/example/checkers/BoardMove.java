package com.example.checkers;

public class BoardMove extends BitMove {

    public BoardMove() {
        super();
    }

    public BoardMove(Position source, Position dest) {
        super(Position.positionToLogicalNumber(source), Position.positionToLogicalNumber(dest));
    }

    public Position getPositionSource() {
        return Position.logicalNumberToPosition(this.source);
    }

    public Position getPositionDestination() {
        return Position.logicalNumberToPosition(this.destination);
    }

    public void setPositionSource(Position position) {
        this.source = Position.positionToLogicalNumber(position);
    }

    public void setPositionDestination(Position position) {
        this.destination = Position.positionToLogicalNumber(position);
    }

    @Override
    public String toString() {
        Position src = Position.logicalNumberToPosition(this.source);
        Position dest = Position.logicalNumberToPosition(this.destination);
        String s = src.toString();
        s += "  --->  " + dest.toString();
        return s;
    }
}
