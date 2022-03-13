package com.example.checkers;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public class TransitionHandler extends Thread{

    private Queue<Transition> transitionQueue;
    private boolean run;
    private boolean animationRunning;
    private double delay;

    private static final double MAX_DELAY = 1000;

    public TransitionHandler()
    {
        this.transitionQueue = new Queue<>();
        this.run = true;
        this.animationRunning = false;
        this.delay = 0;
    }

    public synchronized void kill()
    {
        this.run = false;
        this.notify();
    }

    public synchronized void insert(Transition transition)
    {
        this.transitionQueue.insert(transition);
        this.notify();
    }

    public synchronized Transition remove()
    {
        return this.transitionQueue.remove();
    }

    public synchronized void runAnimation(Transition animation)
    {
        this.animationRunning = true;
        animation.play();
        animation.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                animationRunning = false;
                this.notify();
            }
        });
    }

    private double maxDelay(double delay)
    {
        // makes sure delay is no more than 1000
        double d = (delay > MAX_DELAY) ? MAX_DELAY : delay;
        return d;
    }

    public synchronized void run()
    {
        while (this.run) {
            if (this.transitionQueue.isEmpty()) {
                this.delay = 0;
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (!this.transitionQueue.isEmpty()) {
                    Transition transition = this.remove();
                    transition.setDelay(new Duration(this.delay));
                    this.delay += transition.getTotalDuration().toMillis();
                    transition.play();
                    try {
                        this.wait(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}

