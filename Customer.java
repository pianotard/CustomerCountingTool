import javax.swing.*;
import java.awt.*;
import java.util.Date;

class Customer {

    private static int COUNTS = 0;
    private static Date FIRST_ARRIVAL, LAST_ARRIVAL;
    private static Dimension MINIMUM_SIZE = new Dimension(40, 40);

    private int id, serverId;
    private Date arrival, left, served, done;

    Customer() {
        this.arrival = new Date();
        this.id = ++COUNTS;
        if (COUNTS == 1) {
            FIRST_ARRIVAL = this.arrival;
        }
        LAST_ARRIVAL = this.arrival;
        if (Main.DEBUG) System.out.println(this.id + " Arrived: " + this.arrival);
    }

    void leave() {
        this.left = new Date();
    }

    void serve(int serverId) {
        if (this.served == null) {
            this.served = new Date();
            this.serverId = serverId;
            if (Main.DEBUG) System.out.println(this.id + " Served: " + this.served);
        } else {
            System.out.println("already served!");
        }
    }

    void done() {
        if (this.done == null) {
            this.done = new Date();
            if (Main.DEBUG) System.out.println(this.id + " Done: " + this.done);
        } else {
            System.out.println("Already done!");
        }
    }

    Date getArrival() {
        return this.arrival;
    }

    Date getLeft() { return this.left; }

    Date getServed() {
        return this.served;
    }

    Date getDone() {
        return this.done;
    }

    long getServiceTime() {
        if (this.served == null) {
            //System.out.println("Not served yet!");
            return 0L;
        }
        if (this.done == null) {
            //System.out.println("Not done yet!");
            return new Date().getTime() - this.served.getTime();
        }
        return this.done.getTime() - this.served.getTime();
    }

    long getQueueTime() {
        if (this.left != null) {
            return this.left.getTime() - this.arrival.getTime();
        }
        if (this.served == null) {
            //System.out.println("Not served yet!");
            return new Date().getTime() - this.arrival.getTime();
        }
        return this.served.getTime() - this.arrival.getTime();
    }

    int getId() {
        return this.id;
    }

    int getServerId() {
        return this.serverId;
    }

    static double getAverage(long... nums) {
        long result = 0L;
        for (long num : nums) {
            result += num;
        }
        return (result + 0.0) / COUNTS;
    }

    static double getArrivalLambda() {
        if (LAST_ARRIVAL == null || FIRST_ARRIVAL == null) {
            return 0;
        }
        return (LAST_ARRIVAL.getTime() - FIRST_ARRIVAL.getTime() + 0.0) / (COUNTS * 1000);
    }

    static JPanel getServingPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.GREEN);
        panel.setPreferredSize(MINIMUM_SIZE);
        return panel;
    }

    static JPanel getQueuePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.RED);
        panel.setPreferredSize(MINIMUM_SIZE);
        return panel;
    }

    @Override
    public String toString() {
        return this.id + "";
    }
}
