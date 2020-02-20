public class Main {

    static boolean DEBUG = true;

    public static void main(String[] args) {
        new AppManager();
    }

    static void pause(int s) {
        System.out.println("Pause for " + s + "s");
        try {
            Thread.sleep(s * 1000);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
}
