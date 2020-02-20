import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

class AppManager {

    private static final String VERSION = "1.3";
    private static final String TITLE = "Customer Counting Tool v" + VERSION;

    private EventHandler handler;

    private JFrame frame = new JFrame(TITLE);
    private String userDefinedTitle = "";
    private JPanel customerQueue = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    private JPanel serviceArea;
    private NavigableMap<Integer, JPanel> customersInQueue = new TreeMap<>();
    private JPanel[] customersBeingServed;
    private JLabel[] customersBeingServedLabels;
    private JLabel statLabel;

    AppManager() {
        this.frame.setLayout(new BorderLayout());
        this.frame.setBounds(30, 30, 1000, 600);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.userDefinedTitle = JOptionPane.showInputDialog(frame, "What are you counting? (This will also be the savefile name.)");
        this.frame.setTitle(TITLE + " - " + this.userDefinedTitle);

        boolean correctInput = false;
        int maxServers = 0;
        do {
            String userInput = JOptionPane.showInputDialog(frame, "How many servers do we have?");
            try {
                maxServers = Integer.parseInt(userInput);
                correctInput = true;
            } catch (NumberFormatException e) {
                System.err.println(e);
            }
        } while (!correctInput);

        this.handler = new EventHandler(maxServers);
        this.customersBeingServed = new JPanel[maxServers];
        this.customersBeingServedLabels = new JLabel[maxServers];

        this.initToolBar();
        this.initGraphics();
        this.initStatBar();

        this.frame.setVisible(true);
    }

    private void updateStats() {
        this.statLabel.setText(this.handler.getStats());
    }

    private void customerArrives() {
        int arrivalId = this.handler.arrive();
        JPanel sprite = Customer.getQueuePanel();
        JPopupMenu menu = new JPopupMenu();
        JMenuItem leave = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                AppManager.this.customerLeaves(arrivalId);
            }
        });
        leave.setText("Leave queue");
        menu.add(leave);
        sprite.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent mouseEvent) {
                if (SwingUtilities.isRightMouseButton(mouseEvent)) {
                    menu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
                }
            }
            public void mousePressed(MouseEvent mouseEvent) {}
            public void mouseReleased(MouseEvent mouseEvent) {}
            public void mouseEntered(MouseEvent mouseEvent) {}
            public void mouseExited(MouseEvent mouseEvent) {}
        });
        this.customersInQueue.put(arrivalId, sprite);
        this.customerQueue.removeAll();
        for (Integer id : this.customersInQueue.descendingKeySet()) {
            this.customerQueue.add(this.customersInQueue.get(id));
        }
        this.customerQueue.revalidate();
        this.customerQueue.repaint();
        this.updateStats();
    }

    private void customerLeaves(int customerId) {
        this.handler.leave(customerId);
        JPanel sprite = this.customersInQueue.get(customerId);
        this.customerQueue.remove(sprite);
        this.customersInQueue.remove(customerId);
        this.customerQueue.revalidate();
        this.customerQueue.repaint();
    }

    private void serveCustomer(int serverIndex) {
        int customerId = this.handler.serve(serverIndex);
        if (customerId != -1) {
            JPanel sprite = this.customersInQueue.get(customerId);
            this.customerQueue.remove(sprite);
            this.customersInQueue.remove(customerId);
            this.customersBeingServed[serverIndex].setBackground(Color.GREEN);
            this.customersBeingServedLabels[serverIndex].setText("");
            this.customerQueue.revalidate();
            this.customerQueue.repaint();
        } else {
            System.out.println("UI: -1 returned by handler::serve");
        }
        this.updateStats();
    }

    private void doubleServeCustomer(int serverIndex) {
        int[] customersServed = this.handler.doubleServe(serverIndex);
        this.customersBeingServedLabels[serverIndex].setText("");
        int serveCount = 0;
        for (int customerId : customersServed) {
            if (customerId != -1) {
                JPanel sprite = this.customersInQueue.get(customerId);
                this.customerQueue.remove(sprite);
                this.customersInQueue.remove(customerId);
                this.customersBeingServed[serverIndex].setBackground(Color.GREEN);
                serveCount++;
            } else {
                System.out.println("UI: -1 returned by handler::doubleServe");
            }
        }
        if (serveCount == 2) this.customersBeingServedLabels[serverIndex].setText("2");
        this.customerQueue.revalidate();
        this.customerQueue.repaint();
        this.updateStats();
    }

    private void doneServing(int serverIndex) {
        this.handler.done(serverIndex);
        this.customersBeingServed[serverIndex].setBackground(Color.BLACK);
        this.customersBeingServedLabels[serverIndex].setText("");
        this.updateStats();
    }

    private void terminate() {
        List<Customer> allCusts = this.handler.getAllCustsSorted();
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss aa");
        Date now = new Date();
        try {
            FileWriter writer = new FileWriter(Parser.dashed(this.userDefinedTitle) + "-timestamps-" + now.getTime() + ".csv");
            writer.append("Arrive,Left,Serve,Done,Server\n");
            for (Customer c : allCusts) {
                writer.append(formatter.format(c.getArrival()) + ",");

                if (c.getLeft() != null) {
                    writer.append(formatter.format(c.getLeft()) + ",");
                } else {
                    writer.append("NA,");
                }

                if (c.getServed() != null) {
                    writer.append(formatter.format(c.getServed()) + ",");
                } else {
                    writer.append("NA,");
                }

                if (c.getDone() != null) {
                    writer.append(formatter.format(c.getDone()) + ",");
                } else {
                    writer.append("NA,");
                }

                if (c.getServed() != null) {
                    writer.append(c.getServerId() + "\n");
                } else {
                    writer.append("NA\n");
                }
            }
            writer.flush();
            writer.close();

            writer = new FileWriter(Parser.dashed(this.userDefinedTitle) + "-data-" + now.getTime() + ".csv");
            writer.append("Inter-arrival, Queue time, Service time\n , ");
            for (int i = 0; i < allCusts.size(); i++) {
                Customer c = allCusts.get(i);
                if (i == 0) {
                    writer.append((double) (c.getQueueTime()) / 1000 + ", ");
                    writer.append((double) (c.getServiceTime()) / 1000 + "\n");
                } else {
                    writer.append((double) (c.getArrival().getTime() - allCusts.get(i - 1).getArrival().getTime()) / 1000 + ", ");
                    writer.append((double) (c.getQueueTime()) / 1000 + ", ");
                    writer.append((double) (c.getServiceTime()) / 1000 + "\n");
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println(e);
        }
        this.frame.dispose();
    }

    private void initGraphics() {
        JPanel graphics = new JPanel();
        graphics.setLayout(new GridLayout(1, 2));
        graphics.setBackground(Color.BLACK);

        this.customerQueue.setBackground(Color.BLACK);

        this.serviceArea = new JPanel();
        this.serviceArea.setLayout(new BoxLayout(serviceArea, BoxLayout.Y_AXIS));
        this.serviceArea.setBackground(Color.BLACK);
        for (int i = 0; i < this.handler.getServerCount(); i++) {
            int serverIndex = i;
            JPanel serverToolbar = new JPanel(new BorderLayout());
            serverToolbar.setBackground(Color.BLACK);

            JPanel customerServedHolder = new JPanel(new FlowLayout(FlowLayout.LEFT));
            customerServedHolder.setBackground(Color.BLACK);
            JPanel customerServed = Customer.getServingPanel();
            customerServed.setBackground(Color.BLACK);
            JLabel customerLabel = new JLabel("");
            customerLabel.setLayout(new FlowLayout(FlowLayout.CENTER));
            customerLabel.setBackground(null);
            customerServed.add(customerLabel);
            this.customersBeingServed[i] = customerServed;
            this.customersBeingServedLabels[i] = customerLabel;
            customerServedHolder.add(customerServed);
            serverToolbar.add(customerServedHolder, BorderLayout.WEST);

            JPanel serverController = new JPanel();
            serverController.setBackground(Color.BLACK);

            JButton serveButton = new JButton("Serve");
            serveButton.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent mouseEvent) {
                    AppManager.this.serveCustomer(serverIndex);
                }
                public void mousePressed(MouseEvent mouseEvent) {}
                public void mouseReleased(MouseEvent mouseEvent) {}
                public void mouseEntered(MouseEvent mouseEvent) {}
                public void mouseExited(MouseEvent mouseEvent) {}
            });
            serverController.add(serveButton);

            JButton doubleServeButton = new JButton("Serve x2");
            doubleServeButton.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent mouseEvent) {
                    AppManager.this.doubleServeCustomer(serverIndex);
                }
                public void mousePressed(MouseEvent mouseEvent) {}
                public void mouseReleased(MouseEvent mouseEvent) {}
                public void mouseEntered(MouseEvent mouseEvent) {}
                public void mouseExited(MouseEvent mouseEvent) {}
            });
            serverController.add(doubleServeButton);

            JButton doneButton = new JButton("Done");
            doneButton.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent mouseEvent) {
                    AppManager.this.doneServing(serverIndex);
                }
                public void mousePressed(MouseEvent mouseEvent) {}
                public void mouseReleased(MouseEvent mouseEvent) {}
                public void mouseEntered(MouseEvent mouseEvent) {}
                public void mouseExited(MouseEvent mouseEvent) {}
            });
            serverController.add(doneButton);

            JPanel serverControllerHolder = new JPanel(new FlowLayout(FlowLayout.CENTER));
            serverControllerHolder.setBackground(Color.BLACK);
            serverControllerHolder.add(serverController);

            serverToolbar.add(serverControllerHolder);
            this.serviceArea.add(serverToolbar);
        }

        graphics.add(this.customerQueue);
        graphics.add(this.serviceArea);

        this.frame.add(graphics);
    }

    private void initStatBar() {
        this.statLabel = new JLabel(this.handler.getStats());
        JPanel statBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statBar.setBackground(Color.BLACK);
        statBar.add(this.statLabel);

        this.statLabel.setForeground(Color.GRAY);

        this.frame.add(statBar, BorderLayout.SOUTH);
    }

    private void initToolBar() {
        JPanel customerToolbar = new JPanel();
        customerToolbar.setLayout(new FlowLayout());
        customerToolbar.setBackground(Color.BLACK);

        JButton arriveButton = new JButton("<html><u>A</u>rrive</html>");
        arriveButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                AppManager.this.customerArrives();
            }
            public void mousePressed(MouseEvent mouseEvent) {}
            public void mouseReleased(MouseEvent mouseEvent) {}
            public void mouseEntered(MouseEvent mouseEvent) {}
            public void mouseExited(MouseEvent mouseEvent) {}
        });
        customerToolbar.add(arriveButton);

        JButton endButton = new JButton("End & Export");
        endButton.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent mouseEvent) {
                AppManager.this.terminate();
            }
            public void mousePressed(MouseEvent mouseEvent) {}
            public void mouseReleased(MouseEvent mouseEvent) {}
            public void mouseEntered(MouseEvent mouseEvent) {}
            public void mouseExited(MouseEvent mouseEvent) {}
        });
        customerToolbar.add(endButton);

        this.frame.add(customerToolbar, BorderLayout.NORTH);
    }
}
