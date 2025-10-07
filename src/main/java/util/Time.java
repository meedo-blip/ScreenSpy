package util;

public class Time {
    private static Thread timer;

    public static float dt;

    @Deprecated
    // please be careful with what your runnable does
    // Memory collisions can cause severe crashes
    public static void setInterval(long millis, Runnable run) {

        if(timer == null) {
            timer = new Thread(() -> {

                 while (true) {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    run.run();
                }
            });

            timer.start();
        }
    }
}
