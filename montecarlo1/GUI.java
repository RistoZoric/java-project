import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.image.BufferedImage;

import java.lang.InterruptedException;
import java.lang.NullPointerException;
import java.lang.NumberFormatException;
import java.lang.SecurityException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class Square extends JPanel {
        private final int PIXEL_SIZE;
        private BufferedImage image;        
        private Graphics2D g2d;
        
        public Square(int pixelSize)
        {
                this.PIXEL_SIZE = pixelSize;
        }

        public void setup(Simulator sim)
        {
                image = new BufferedImage(
                        PIXEL_SIZE, PIXEL_SIZE, BufferedImage.TYPE_INT_ARGB);
                g2d = image.createGraphics();

                /*
                 * Draw the area we want to compute.
                 */
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                                     RenderingHints.VALUE_RENDER_QUALITY);

                g2d.setPaint(Color.LIGHT_GRAY);
                float scale = PIXEL_SIZE / sim.size();
                for (Circle c : sim.circles()) {
                        Point p = c.center();
                        int r = (int)(c.radius() * scale);
                        int x = (int)(c.center().x() * scale);
                        int y = (int)(c.center().y() * scale);
                        int size = r + r;
                        g2d.fillOval(x - r, y - r, size, size);
                }

                EventQueue.invokeLater(() -> repaint());
        }

        public void teardown()
        {
                g2d.dispose();
                image.flush();
        }

        @Override
        public void paintComponent(Graphics g)
        {
                super.paintComponent(g);

                if (image == null)
                        return;
                
                ((Graphics2D)g).drawImage(image, null, 0, 0);                
        }

        public void updateImage(Simulator sim, Iterator<Point> it) 
        {
                float scale = this.PIXEL_SIZE / sim.size();

                while (it.hasNext()) {
                        Point p = it.next();
                        Color c = p.isInside() ? Color.BLUE : Color.RED;
                        g2d.setPaint(c);
                        int x = (int)(p.x() * scale);
                        int y = (int)(p.y() * scale);
                        image.setRGB(x, y, c.getRGB());
                }
        }
}

public class GUI {
        private static final int FPS = 60;
        private static final int FRAME_WIDTH = 800;
        private static final int FRAME_HEIGHT = 600;
        private static final int HORIZ_PADDING = 5;
        private static final int VERT_SPACING = 30;
        private static final int LABEL_WIDTH = 101;
        private static final int LABEL_HEIGHT = 25;
        private static final int LEFT_PANEL_WIDTH = FRAME_WIDTH - FRAME_HEIGHT;
        private static final int FIELD_WIDTH = LEFT_PANEL_WIDTH -
                (HORIZ_PADDING * 3 + LABEL_WIDTH);
        
        private JFrame frame;
        private JPanel mainPanel;        
        private JPanel leftPanel;        
        private Square square;

        // Text field for the size of the whole square.
        private JTextField sizeField;

        // Text field for the number of cycles to run.
        private JTextField cycleField;

        // Text field for the number of circles.
        private JTextField circleField;

        // Push this button the simulation.
        private JButton runButton;

        // Label where the current estimated value of the area is
        // displayed.
        private JLabel areaDisplay;

        // Label where that display the percentage of cycles already
        // executed.
        private JLabel progressDisplay;

        // Label that displays the elapsed time since the start of the
        // simulation.
        private JLabel timeDisplay;

        // Specify the type of mode used for the simulation.
        private JComboBox modeList;        
        
        public GUI()
        {
                this.frame = new JFrame("Montecarlo simulation");
                this.frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
                this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                this.mainPanel = new JPanel();
                this.mainPanel.setLayout(
                        new BoxLayout(mainPanel, BoxLayout.X_AXIS));
                this.frame.add(mainPanel);
                
                this.leftPanel = new JPanel();
                this.leftPanel.setPreferredSize(
                        new Dimension(LEFT_PANEL_WIDTH, FRAME_HEIGHT));

                this.square = new Square(FRAME_HEIGHT);
                this.square.setPreferredSize(
                        new Dimension(FRAME_HEIGHT, FRAME_HEIGHT));

                this.sizeField = new JTextField("1.0");
                this.cycleField = new JTextField("1000000");
                this.circleField= new JTextField("10");

                this.runButton = new JButton("Run");
                this.runButton.addActionListener(e -> runClicked());

                this.areaDisplay = new JLabel();
                this.progressDisplay = new JLabel("0%");
                this.timeDisplay = new JLabel("0.0s");

                String[] modes = { "sequential", "parallel" };
                this.modeList = new JComboBox<>(modes);                
        }

        private void init()
        {
                final int fieldX = HORIZ_PADDING * 2 + LABEL_WIDTH;
                
                leftPanel.setLayout(null);
                square.setLayout(null);                

                mainPanel.add(leftPanel);
                mainPanel.add(square);

                JLabel modeLabel = new JLabel("Mode:");
                int y = 10;
                modeLabel.setBounds(
                        HORIZ_PADDING, y, LABEL_WIDTH, LABEL_HEIGHT);
                leftPanel.add(modeLabel);

                modeList.setBounds(fieldX, y, FIELD_WIDTH, LABEL_HEIGHT);
                leftPanel.add(modeList);                
                
                JLabel sizeLabel = new JLabel("Square size");
                y += VERT_SPACING;                
                sizeLabel.setBounds(
                        HORIZ_PADDING, y, LABEL_WIDTH, LABEL_HEIGHT);
                leftPanel.add(sizeLabel);

                sizeField.setBounds(fieldX, y, FIELD_WIDTH, LABEL_HEIGHT);
                leftPanel.add(sizeField);

                JLabel cycleLabel = new JLabel("Number of cycles");
                y += VERT_SPACING;                
                cycleLabel.setBounds(
                        HORIZ_PADDING, y, LABEL_WIDTH, LABEL_HEIGHT);
                leftPanel.add(cycleLabel);

                cycleField.setBounds(fieldX, y, FIELD_WIDTH, LABEL_HEIGHT);
                leftPanel.add(cycleField);

                JLabel circleLabel = new JLabel("Number of circles");
                y += VERT_SPACING;
                circleLabel.setBounds(
                        HORIZ_PADDING, y, LABEL_WIDTH, LABEL_HEIGHT);
                leftPanel.add(circleLabel);

                circleField.setBounds(fieldX, y, FIELD_WIDTH, LABEL_HEIGHT);
                leftPanel.add(circleField);                

                y += VERT_SPACING;
                runButton.setBounds(
                        HORIZ_PADDING,
                        y,
                        LEFT_PANEL_WIDTH - 2 * HORIZ_PADDING,
                        LABEL_HEIGHT);
                leftPanel.add(runButton);

                JLabel areaLabel = new JLabel("Estimated area");
                y += VERT_SPACING;
                areaLabel.setBounds(
                        HORIZ_PADDING, y, LABEL_WIDTH, LABEL_HEIGHT);
                leftPanel.add(areaLabel);

                areaDisplay.setBounds(fieldX, y, FIELD_WIDTH, LABEL_HEIGHT);
                leftPanel.add(areaDisplay);

                JLabel progressLabel = new JLabel("Progess");
                y += VERT_SPACING * 0.6;
                progressLabel.setBounds(
                        HORIZ_PADDING, y, LABEL_WIDTH, LABEL_HEIGHT);
                leftPanel.add(progressLabel);

                progressDisplay.setBounds(fieldX, y, FIELD_WIDTH, LABEL_HEIGHT);
                leftPanel.add(progressDisplay);

                JLabel timeLabel = new JLabel("Elapsed time");
                y += VERT_SPACING * 0.6;
                timeLabel.setBounds(
                        HORIZ_PADDING, y, LABEL_WIDTH, LABEL_HEIGHT);
                leftPanel.add(timeLabel);

                timeDisplay.setBounds(fieldX, y, FIELD_WIDTH, LABEL_HEIGHT);
                leftPanel.add(timeDisplay);                
                
                frame.setVisible(true);                
        }

        // Display the message to the user.
        private void alert(String msg) 
        {
                JOptionPane.showMessageDialog(
                        this.frame,
                        msg,
                        "Warning!",
                        JOptionPane.WARNING_MESSAGE);                
        }

        // Read and return a float from the given text field.
        // @throws NumberFormatException - if the text is not a valid
        // positive double representation.
        private float readFloat(JTextField text) throws NumberFormatException
        {
                float f;

                try {
                        f = Float.parseFloat(text.getText());
                        if (f < 0)
                                throw new NumberFormatException();
                } catch (NullPointerException e) {
                        throw new NumberFormatException();
                }

                return f;                
        }

        // Read and return an integer from the given text field.
        // @throws NumberFormatException - if the text is not a valid
        // positive integer representation.
        private int readInt(JTextField text) throws NumberFormatException
        {
                int i;

                try {
                        i = Integer.parseInt(text.getText());
                        if (i < 0)
                                throw new NumberFormatException();
                } catch (NullPointerException e) {
                        throw new NumberFormatException();
                }

                return i;                
        }

        private void updateGUI(Simulator sim, long startTime)
        {
                double dur =  (System.currentTimeMillis() - startTime) / 1000.0;
                int perc = (int)((sim.currentCycle()*100.0)/sim.numCycles());
                double area = sim.currentEstim();                

                square.repaint();                
                progressDisplay.setText(String.format("%d%%", perc));
                timeDisplay.setText(String.format("%.1fs", dur));
                areaDisplay.setText(Double.isNaN(area) ? "" :
                                    String.format("%,.4f", sim.currentEstim()));
        }

        // Execute when the run button is clicked.
        private void runClicked()
        {
                float size;
                int numCycles;
                int numCircles;                
                
                runButton.setEnabled(false);

                try {
                        size = readFloat(sizeField);
                } catch (NumberFormatException e) {
                        alert("Please enter a valid square size.");
                        return;                        
                }

                try {
                        numCycles = readInt(cycleField);                        
                } catch (NumberFormatException e) {
                        alert("Please enter a valid number of cycles.");
                        return;                        
                }

                try {
                        numCircles = readInt(circleField);
                } catch (NumberFormatException e) {
                        alert("Please enter a valid number of circles.");
                        return;                        
                }

                // We run the simulation in this thread.
                ExecutorService exec = Executors.newSingleThreadExecutor();

                // We update the image in the animated square at
                // regular interval in this thread.
                ScheduledExecutorService schedExec =
                        Executors.newSingleThreadScheduledExecutor();

                Simulator sim;
                String mode = (String)modeList.getSelectedItem();
                switch (mode) {
                case "sequential":
                        sim = new SequentialSimulator(
                                numCircles, numCycles, size);
                        break;
                case "parallel":
                        sim = new ParallelSimulator(
                                numCircles, numCycles, size);
                        break;
                default:
                        throw new RuntimeException("Unknown mode: " + mode);
                }

                long period = 1000/FPS;
                Iterator<Point> it = sim.iterator();
                long start = System.currentTimeMillis();
                Runnable updater = () -> {
                        square.updateImage(sim, it);
                        EventQueue.invokeLater(() -> updateGUI(sim, start));
                };                
                final ScheduledFuture<?> updaterFuture =
                        schedExec.scheduleAtFixedRate(
                                updater, 0, period, TimeUnit.MILLISECONDS);
                
                exec.submit(() -> {
                                square.setup(sim);
                                sim.run();        
                                updaterFuture.cancel(true);
                                schedExec.shutdown();
                                updater.run();
                                square.teardown();
                                runButton.setEnabled(true);                
                        });
                exec.shutdown();

        }

        public static void main(String[] args)
        {
                EventQueue.invokeLater(() -> {
                                GUI g = new GUI();
                                g.init();                                
                        });
        }
}
