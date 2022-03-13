package com.example.checkers;

import java.util.Objects;

public class BitMove {

    protected long source;
    protected long destination;

    public BitMove() {
        this.source = 0;
        this.destination = 0;
    }

    public BitMove(long src, long dest) {
        this.source = src;
        this.destination = dest;
    }

    public long getSource() {
        return source;
    }

    public void setSource(long source) {
        this.source = source;
    }

    public long getDestination() {
        return destination;
    }

    public void setDestination(long destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return this.source + "  --->  " + this.destination;
    }

    @Override
    public boolean equals(Object o) {
        boolean e = false;
        if (o instanceof BitMove) {
            e = (((BitMove)o).source == this.source) && (((BitMove)o).destination == this.destination);
        }
        return e;
    }
}
