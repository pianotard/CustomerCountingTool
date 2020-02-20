import java.util.*;

class Server {

    private static int COUNTS = 0;

    private int id;
    private Optional<Customer> nowServing = Optional.empty();
    private Optional<Customer> secondServing = Optional.empty();
    private List<Customer> doneServing = new ArrayList<>();

    Server() {
        this.id = ++COUNTS;
    }

    void doubleServe(Customer first, Customer second) {
        this.nowServing.ifPresent(c -> {
            c.done();
            doneServing.add(c);
        });
        this.secondServing.ifPresent(c -> {
            c.done();
            doneServing.add(c);
        });
        assert first != null && second != null;
        first.serve(this.id);
        second.serve(this.id);
        this.nowServing = Optional.of(first);
        this.secondServing = Optional.of(second);
    }

    void serve(Customer toServe) {
        this.nowServing.ifPresent(c -> {
            c.done();
            doneServing.add(c);
        });
        this.secondServing.ifPresent(c -> {
            c.done();
            doneServing.add(c);
            this.secondServing = Optional.empty();
        });
        assert toServe != null;
        toServe.serve(this.id);
        this.nowServing = Optional.of(toServe);
        this.print();
    }

    void done() {
        this.nowServing.ifPresent(c -> {
            c.done();
            doneServing.add(c);
            this.nowServing = Optional.empty();
            this.print();
        });
        this.secondServing.ifPresent(c -> {
            c.done();
            doneServing.add(c);
            this.secondServing = Optional.empty();
            this.print();
        });
    }

    int getDoneCount() {
        return this.doneServing.size();
    }

    List<Customer> getAllCusts() {
        List<Customer> allCusts = new ArrayList<>();
        this.nowServing.ifPresent(allCusts::add);
        this.secondServing.ifPresent(allCusts::add);
        allCusts.addAll(this.doneServing);
        return allCusts;
    }

    private void print() {
        System.out.println("----- SERVER NO. " + id + " -----");
        System.out.println("--- Now Serving ---");
        System.out.println(this.nowServing);
        System.out.println(this.secondServing);
        System.out.println("--- Done Serving ---");
        System.out.println(this.doneServing);
        System.out.println("");
    }
}
