package paintbrush;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class PaintBrush extends JFrame {

    private Color currentColor = Color.BLACK;
    private ArrayList<Shape> shapes = new ArrayList<>();
    private int startX, startY, endX, endY;
    private String shapeType = "Line";
    private boolean isDotted = false;
    private boolean isFilled = false; // For fill checkbox
    private boolean isEraser = false; // Separate flag for eraser functionality
    private static final int ERASER_SIZE = 20;

    public PaintBrush() {
        setTitle("Paint Brush");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        DrawPanel drawPanel = new DrawPanel();
        add(drawPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        // Buttons for shapes
        JButton lineButton = new JButton("Line");
        lineButton.addActionListener(e -> {
            shapeType = "Line";
            isEraser = false;
        });
        controlPanel.add(lineButton);

        JButton rectangleButton = new JButton("Rectangle");
        rectangleButton.addActionListener(e -> {
            shapeType = "Rectangle";
            isEraser = false;
        });
        controlPanel.add(rectangleButton);

        JButton ovalButton = new JButton("Oval");
        ovalButton.addActionListener(e -> {
            shapeType = "Oval";
            isEraser = false;
        });
        controlPanel.add(ovalButton);

        JButton pencilButton = new JButton("Pencil");
        pencilButton.addActionListener(e -> {
            shapeType = "Pencil";
            isEraser = false;
        });
        controlPanel.add(pencilButton);

        JButton eraserButton = new JButton("Eraser");
        eraserButton.addActionListener(e -> {
            shapeType = "Pencil"; // Eraser works like a pencil
            isEraser = true;
        });
        controlPanel.add(eraserButton);

        // Color buttons
        JButton blackButton = createColorButton(Color.BLACK);
        controlPanel.add(blackButton);

        JButton redButton = createColorButton(Color.RED);
        controlPanel.add(redButton);

        JButton greenButton = createColorButton(Color.GREEN);
        controlPanel.add(greenButton);

        JButton blueButton = createColorButton(Color.BLUE);
        controlPanel.add(blueButton);

        // Dotted checkbox
        JCheckBox dottedCheckbox = new JCheckBox("Dotted");
        dottedCheckbox.addItemListener(e -> isDotted = (e.getStateChange() == ItemEvent.SELECTED));
        controlPanel.add(dottedCheckbox);

        // Fill checkbox
        JCheckBox fillCheckbox = new JCheckBox("Fill");
        fillCheckbox.addItemListener(e -> isFilled = (e.getStateChange() == ItemEvent.SELECTED));
        controlPanel.add(fillCheckbox);

        // Undo button
        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> {
            if (!shapes.isEmpty()) {
                shapes.remove(shapes.size() - 1);
                drawPanel.repaint();
            }
        });
        controlPanel.add(undoButton);

        // Clear button
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            shapes.clear();
            drawPanel.repaint();
        });
        controlPanel.add(clearButton);

        add(controlPanel, BorderLayout.NORTH);
    }

    private JButton createColorButton(Color color) {
        JButton button = new JButton();
        button.setBackground(color);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.addActionListener(e -> {
            currentColor = color;
            isEraser = false; // Reset eraser mode when switching colors
        });
        return button;
    }

    private class DrawPanel extends JPanel {
        private ArrayList<Point> pencilPoints = new ArrayList<>();

        public DrawPanel() {
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    startX = e.getX();
                    startY = e.getY();
                    if (shapeType.equals("Pencil")) {
                        pencilPoints.clear();
                        pencilPoints.add(new Point(startX, startY));
                    }
                }

                public void mouseReleased(MouseEvent e) {
                    if (shapeType.equals("Pencil")) {
                        shapes.add(new MyFreehand(pencilPoints, isEraser ? Color.WHITE : currentColor, isDotted, isEraser));
                        pencilPoints.clear();
                        repaint();
                    } else {
                        endX = e.getX();
                        endY = e.getY();
                        Shape shape = createShape(startX, startY, endX, endY, isDotted, isFilled);
                        if (shape != null) {
                            shapes.add(shape);
                            repaint();
                        }
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (shapeType.equals("Pencil")) {
                        pencilPoints.add(new Point(e.getX(), e.getY()));
                        repaint();
                    }
                }
            });
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            for (Shape shape : shapes) {
                g2d.setColor(shape.color);

                if (shape.isDotted) {
                    g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
                } else if (shape instanceof MyFreehand && ((MyFreehand) shape).isEraser) {
                    g2d.setStroke(new BasicStroke(ERASER_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                } else {
                    g2d.setStroke(new BasicStroke(1));
                }
                shape.draw(g2d);
            }

            if (!pencilPoints.isEmpty()) {
                g2d.setColor(isEraser ? Color.WHITE : currentColor);
                if (isEraser) {
                    g2d.setStroke(new BasicStroke(ERASER_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                } else {
                    g2d.setStroke(new BasicStroke(1));
                }
                for (int i = 0; i < pencilPoints.size() - 1; i++) {
                    Point p1 = pencilPoints.get(i);
                    Point p2 = pencilPoints.get(i + 1);
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }

        private Shape createShape(int startX, int startY, int endX, int endY, boolean isDotted, boolean isFilled) {
            switch (shapeType) {
                case "Rectangle":
                    return new MyRectangle(Math.min(startX, endX), Math.min(startY, endY),
                            Math.abs(endX - startX), Math.abs(endY - startY), currentColor, isDotted, isFilled);
                case "Oval":
                    return new MyOval(Math.min(startX, endX), Math.min(startY, endY),
                            Math.abs(endX - startX), Math.abs(endY - startY), currentColor, isDotted, isFilled);
                case "Line":
                    return new MyLine(startX, startY, endX, endY, currentColor, isDotted);
                default:
                    return null;
            }
        }
    }

    private abstract class Shape {
        Color color;
        boolean isDotted;

        Shape(Color color, boolean isDotted) {
            this.color = color;
            this.isDotted = isDotted;
        }

        abstract void draw(Graphics2D g);
    }

    private class MyRectangle extends Shape {
        int x, y, width, height;
        boolean isFilled;

        MyRectangle(int x, int y, int width, int height, Color color, boolean isDotted, boolean isFilled) {
            super(color, isDotted);
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.isFilled = isFilled;
        }

        @Override
        void draw(Graphics2D g) {
            if (isFilled) {
                g.fillRect(x, y, width, height);
            } else {
                g.drawRect(x, y, width, height);
            }
        }
    }

    private class MyOval extends Shape {
        int x, y, width, height;
        boolean isFilled;

        MyOval(int x, int y, int width, int height, Color color, boolean isDotted, boolean isFilled) {
            super(color, isDotted);
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.isFilled = isFilled;
        }

        @Override
        void draw(Graphics2D g) {
            if (isFilled) {
                g.fillOval(x, y, width, height);
            } else {
                g.drawOval(x, y, width, height);
            }
        }
    }

    private class MyLine extends Shape {
        int x1, y1, x2, y2;

        MyLine(int x1, int y1, int x2, int y2, Color color, boolean isDotted) {
            super(color, isDotted);
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        @Override
        void draw(Graphics2D g) {
            g.drawLine(x1, y1, x2, y2);
        }
    }

    private class MyFreehand extends Shape {
        ArrayList<Point> points;
        boolean isEraser;

        MyFreehand(ArrayList<Point> points, Color color, boolean isDotted, boolean isEraser) {
            super(color, isDotted);
            this.points = new ArrayList<>(points);
            this.isEraser = isEraser;
        }

        @Override
        void draw(Graphics2D g) {
            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PaintBrush paintBrush = new PaintBrush();
            paintBrush.setVisible(true);
        });
    }
}
