import java.util.*;

class EventHandler {

    private Queue<Customer> queue = new PriorityQueue<>(Comparator.comparing(Customer::getArrival));
    private Server[] servers;
    private List<Customer> customersLeft = new ArrayList<>();

    EventHandler(int maxServers) {
        this.servers = new Server[maxServers];
        for (int i = 0; i < maxServers; i++) {
            this.servers[i] = new Server();
        }
        if (Main.DEBUG) System.out.println("Created " + maxServers + " servers");
    }

    int arrive() {
        System.out.println("arrive() called");
        Customer c = new Customer();
        this.queue.add(c);
        return c.getId();
    }

    void leave(int customerId) {
        this.queue.stream().filter(c -> c.getId() == customerId).findAny().ifPresent(c -> {
            c.leave();
            this.queue.remove(c);
            this.customersLeft.add(c);
            System.out.println(this.customersLeft);
        });
    }

    int[] doubleServe(int serverIndex) {
        System.out.println("doubleServe() called on Server " + (serverIndex + 1));
        if (serverIndex > this.servers.length - 1) {
            System.out.println("Backend: Invalid serverIndex!");
            return new int[] {-1};
        }
        if (this.queue.isEmpty()) {
            System.out.println("Backend: No customer in queue!");
            return new int[] {-1};
        } else {
            Server s = this.servers[serverIndex];
            Customer first = this.queue.poll();
            if (this.queue.isEmpty()) {
                System.out.println("Backend: Only 1 customer in queue!");
                s.serve(first);
                return new int[] {first.getId()};
            }
            Customer second = this.queue.poll();
            s.doubleServe(first, second);
            return new int[] {first.getId(), second.getId()};
        }
    }

    int serve(int serverIndex) {
        System.out.println("serve() called on Server " + (serverIndex + 1));
        if (serverIndex > this.servers.length - 1) {
            System.out.println("Backend: Invalid serverIndex!");
            return -1;
        }
        if (this.queue.isEmpty()) {
            System.out.println("Backend: No customer in queue!");
            return -1;
        } else {
            Server s = this.servers[serverIndex];
            Customer toServe = this.queue.poll();
            s.serve(toServe);
            return toServe.getId();
        }
    }

    void done(int serverIndex) {
        System.out.println("done() called on Server " + (serverIndex + 1));
        if (serverIndex > this.servers.length - 1) {
            System.out.println("Backend: Invalid serverIndex!");
            return;
        }
        Server s = this.servers[serverIndex];
        s.done();
    }

    int getServerCount() {
        return this.servers.length;
    }

    List<Customer> getAllCustsSorted() {
        List<Customer> allCusts = new ArrayList<>(this.queue);
        for (Server s : this.servers) {
            allCusts.addAll(s.getAllCusts());
        }
        allCusts.addAll(this.customersLeft);
        allCusts.sort(Comparator.comparing(Customer::getArrival));
        return allCusts;
    }

    String getStats() {
        String stats = "";

        double arrivalLambda = Customer.getArrivalLambda();
        stats += "AVERAGES   Inter-arrival: " + String.format("%.3f", arrivalLambda) + "s, ";

        Date now = new Date();
        long queueTime = this.getAllCustsSorted().stream().mapToLong(Customer::getQueueTime).sum();
        double averageQueueTime = Customer.getAverage(queueTime) / 1000;
        stats += "Queue time: " + String.format("%.3f", averageQueueTime) + "s, ";

        long svcTime = this.getAllCustsSorted().stream().mapToLong(Customer::getServiceTime).sum();
        int doneServing = Arrays.stream(this.servers).mapToInt(Server::getDoneCount).sum();
        double averageServiceTime = (svcTime + 0.0) / (doneServing * 1000);
        stats += "Service time: " + String.format("%.3f", averageServiceTime) + "s";

        return stats;
    }
}
