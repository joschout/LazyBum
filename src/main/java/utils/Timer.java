package utils;

/**
 * Created by joschout.
 */
public class Timer {

    public static final double convertNanoTimeToSeconds = 1.0 / 1000_000_000;

    private boolean isRunning = false;
    private long restartTimePoint;

    private long duration;


    public Timer(){
        duration = 0L;
    }

    public static Timer getStartedTimer(){
        Timer timer = new Timer();
        timer.start();
        return timer;
    }

    public void start(){
        if(isRunning){
            throw new UnsupportedOperationException("Trying to start a running timer");
        } else {
            restartTimePoint = System.nanoTime();
            isRunning = true;
        }
    }

    public double stop(){
        long now = System.nanoTime();
        if(isRunning){
            isRunning = false;
            long timeInterval = now - restartTimePoint;
            duration += timeInterval;
            return getDurationInSeconds();
        } else{
            throw new UnsupportedOperationException("Trying to stop a timer that is not running");
        }
    }

    public double getDurationInSeconds(){
        if(isRunning){
            long now = System.nanoTime();
            return (duration + (now - restartTimePoint))
                    * convertNanoTimeToSeconds;
        } else{
            return duration * convertNanoTimeToSeconds;
        }
    }
}
