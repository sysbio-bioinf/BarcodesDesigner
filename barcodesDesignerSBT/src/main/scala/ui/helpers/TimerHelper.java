package main.code.ui.helpers;

/**
 * A helper class to determine computation time
 * @author Marietta Hamberger
 */
public class TimerHelper {

    public String taskName = "";
    public long start = 0;
    public long end = 0;

    /**
     * Starts the timer
     * @param name The task name
     */
    public void start(String name){
        taskName = name;
        this.start = System.currentTimeMillis();
    }

    /**
     * Ends the timer
     */
    public void end(){
        end = System.currentTimeMillis();
    }
}
