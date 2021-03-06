import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ImageBuffer extends JPanel implements MouseListener, MouseMotionListener {
    static int[] y = new int[10];
    static int[] x = new int[10];
    static double[] xRobot = new double[10];
    static double[] yRobot = new double[10];
    static double xScale, xScale_YZ = 1, xScale_XZ = 1,xInit, xInit_YZ = 0, xInit_XZ = 0;
    static double yScale, yScale_YZ = 1, yScale_XZ = 1,yInit, yInit_YZ = 0, yInit_XZ = 0;
    static int xPoint;
    static int yPoint;
    static String coordinate;

    int x1, y1;
    String modifierKeys = "";
    BufferedImage image;
    Dimension size = new Dimension();  //  @jve:decl-index=0:
    String imgFile;
    private String dir = null;
    int amt_input = 0;
    int radius = 20;
    static int calnum;
    SocketFunction socketFunction = new SocketFunction();

    public ImageBuffer(BufferedImage image) {
        this.image = image;
        size.setSize(image.getWidth(), image.getHeight());
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    void coordinate_change(String str) {
        if (str == "Y-Z") {
            xPoint = x[0];
            yPoint = size.height - y[0];
        } else if (str == "X-Z : Left") {
            xPoint = size.width - x[0];
            yPoint = size.height - y[0];
        } else if (str == "X-Z : Right") {
            xPoint = x[0];
            yPoint = size.height - y[0];
        } else {
            xPoint = x[0];
            yPoint = y[0];
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = (getWidth() - size.width) / 2;
        int height = (getHeight() - size.height) / 2;
        g.drawImage(image, width, height, this);

        g.setColor(Color.red);
        coordinate_change(coordinate);
        if (coordinate == "") {
        } else if (coordinate != "") {
            g.drawString("(" + (xPoint * xScale + xInit) + "," + (yPoint * yScale + yInit) + ")", x[0], y[0]);
        }
    }

    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ImageBuffer();
            }
        });
    }

    public ImageBuffer() {
        try {
            Init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Init() throws Exception {
        xScale = 1;
        yScale = 1;

        JFrame frame = new JFrame();
        frame.setTitle("Picture");
        frame.setSize(200, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("File");
        Menu menu1 = new Menu("Socket");
        menu.add("Image");
        menu.add("Quit");
        menu1.add("SocketConnection");
        menuBar.add(menu);
        menuBar.add(menu1);
        menu.addActionListener(new axnListener());
        ;
        menu1.addActionListener(new socket_axnListener());
        frame.setMenuBar(menuBar);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.setVisible(true);
    }//end init

    public void loadImage() {
        JFrame choose = new JFrame();
        FileDialog dlg = new FileDialog(choose, "Choose Image", FileDialog.LOAD);
        //set current directory
        if (dir != null) {
            dlg.setDirectory(dir);
        }
        dlg.setVisible(true);
        //get image name and path
        imgFile = dlg.getDirectory() + dlg.getFile();
        dir = dlg.getDirectory();
        //create image using filename
        try {
            image = ImageIO.read(new File(imgFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageBuffer test = new ImageBuffer(image);
        JFrame f = new JFrame();

        f.setTitle("Viewer");
        f.add(new JScrollPane(test));
        f.setVisible(true);

        Insets insets = f.getInsets();
        f.setSize(image.getWidth() + insets.left + insets.right + 1, image.getHeight() + insets.top + insets.bottom + 1);

        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Y-Z");
        Menu menu1 = new Menu("X-Z");
        menu.add("Calibration");
        menu1.add("Calibration");
        menu.add("Move");
        menu1.add("Move");
        menu.add("Quit");
        menu1.add("Quit");
        menuBar.add(menu);
        menuBar.add(menu1);
        menu.addActionListener(new YZActionListener());
        menu1.addActionListener(new XZActionListener());
        f.setMenuBar(menuBar);
    }//end load image


    static JFrame cal_JFrame = new JFrame();
    static ImageBuffer cal_ImageBuffer;

    public void loadImage_cal() {
        JFrame choose = new JFrame();
        FileDialog dlg = new FileDialog(choose, "Choose Image", FileDialog.LOAD);
        //set current directory
        if (dir != null) {
            dlg.setDirectory(dir);
        }
        dlg.setVisible(true);
        //get image name and path
        imgFile = dlg.getDirectory() + dlg.getFile();
        dir = dlg.getDirectory();
        //create image using filename
        try {
            image = ImageIO.read(new File(imgFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        cal_ImageBuffer = new ImageBuffer(image);
        cal_JFrame.setTitle("Calibration");
        cal_JFrame.add(new JScrollPane(cal_ImageBuffer));
        //cal_ImageBuffer.setVisible(true);
        cal_JFrame.setVisible(true);

        Insets insets = cal_JFrame.getInsets();
        cal_JFrame.setSize(image.getWidth() + insets.left + insets.right + 1, image.getHeight() + insets.top + insets.bottom + 1);

        cal_JFrame.setResizable(false);
        cal_JFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        calnum = 2;
    }//end load image

    public void calibration(String coordinate1) {
        System.out.println("Point current TCP");
        if (coordinate1 == "Y-Z") {
            xScale=1;
            yScale=1;
            xScale_YZ = 1;
            yScale_YZ = 1;
            xInit=0;
            yInit=0;
            xInit_YZ = 0;
            yInit_YZ = 0;
        }else if (coordinate1 == "X-Z : Left"){
            xScale=1;
            yScale=1;
            xScale_XZ = 1;
            yScale_XZ = 1;
            xInit=0;
            yInit=0;
            xInit_XZ = 0;
            yInit_XZ = 0;
        }
        calnum = 1;
    }

    public void moveRobot(String coordinate1) {
        System.out.println("Pick a point");
        if (coordinate1 == "Y-Z") {
            xScale = xScale_YZ;
            yScale = yScale_YZ;
            xInit = xInit_YZ;
            yInit = yInit_YZ;
        }else if (coordinate1 == "X-Z : Left"){
            xScale = xScale_XZ;
            yScale = yScale_XZ;
            xInit = xInit_XZ;
            yInit = yInit_XZ;
        }
        calnum = 11;
    }

    class axnListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equalsIgnoreCase("Image")) {
                loadImage();
            } else System.exit(0);
        }
    }//end of inner class axnListener

    class socket_axnListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equalsIgnoreCase("SocketConnection")) {
                try {
                    socketFunction.SocketConnection("192.168.0.48", 1025);
                    System.out.println("Connection Successful!");
                } catch (Exception ex) {
                    System.out.println("Connection Failed: " + ex.getMessage());
                }
            } else System.exit(0);
        }
    }

    class YZActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equalsIgnoreCase("Calibration")) {
                calibration("Y-Z");
                coordinate = "Y-Z";
            } else if (e.getActionCommand().equalsIgnoreCase("Move")) {
                moveRobot("Y-Z");
                coordinate = "Y-Z";
            } else System.exit(0);
        }
    }

    class XZActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equalsIgnoreCase("Calibration")) {
                calibration("X-Z : Left");
                coordinate = "X-Z : Left";
            } else if (e.getActionCommand().equalsIgnoreCase("Move")) {
                moveRobot("X-Z : Left");
                coordinate = "X-Z : Left";
            } else System.exit(0);
        }
    }

    void setInfo(MouseEvent e) {
        // set up the information about event for display
        x[0] = e.getX();
        y[0] = e.getY();
        this.repaint();
    }

    public String IntToStr(double number) {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        sb.append(number);
        String strI = sb.toString();
        return strI;
    }

    public void CoordinateCal(int number) {
        x[number] = x[0];
        y[number] = y[0];
        socketFunction.SocketSending("CurrentPos" + "\n\0d");
        String[] a = socketFunction.SocketReading().split(",");
        xRobot[number] = Integer.valueOf(a[0]);
        yRobot[number] = Integer.valueOf(a[1]);
        if (number == 2 && coordinate == "Y-Z") {
            xScale_YZ = (xRobot[2] - xRobot[1]) / (x[2] - x[1]);
            yScale_YZ = (yRobot[2] - yRobot[1]) / (y[1] - y[2]);
            xScale = xScale_YZ;
            yScale = yScale_YZ;
            xInit_YZ = xRobot[1] - (xScale_YZ * x[1]);
            yInit_YZ = yRobot[1] - (yScale_YZ * (size.height - y[1]));
            xInit = xInit_YZ;
            yInit = yInit_YZ;
            System.out.println(IntToStr(xScale_YZ) + "," + IntToStr(yScale_YZ));
        } else if (number == 2 && coordinate == "X-Z : Left") {
            xScale_XZ = (xRobot[2] - xRobot[1]) / (x[1] - x[2]);
            yScale_XZ = (yRobot[2] - yRobot[1]) / (y[1] - y[2]);
            xScale = xScale_XZ;
            yScale = yScale_XZ;
            xInit_XZ = xRobot[1] - (xScale_XZ * (size.width - x[1]));
            yInit_XZ = yRobot[1] - (yScale_XZ * (size.height - y[1]));
            xInit = xInit_XZ;
            yInit = yInit_XZ;
            System.out.println(IntToStr(xScale_XZ) + "," + IntToStr(yScale_XZ));
        } else if (number == 2 && coordinate == "X-Z : Right") {
            xScale_XZ = (xRobot[2] - xRobot[1]) / (x[2] - x[1]);
            yScale_XZ = (yRobot[2] - yRobot[1]) / (y[1] - y[2]);
            System.out.println(IntToStr(xScale) + "," + IntToStr(yScale));
        }
    }

    @Override
    public void mouseClicked(MouseEvent m) {
        int radius = 20;
        amt_input++;
        x[0] = m.getX();
        y[0] = m.getY();
        coordinate_change(coordinate);
        System.out.println("mouse clicked " + amt_input);
        System.out.println((xPoint * xScale + xInit) + "," + (yPoint * yScale + yInit));
        if (calnum == 1) {
            System.out.println("Point Chosen. Move the robot and Select the next Point!");
            CoordinateCal(1);
            loadImage_cal();
        } else if (calnum == 2) {
            //cal_ImageBuffer.setVisible(false);
            cal_JFrame.dispose();
            CoordinateCal(2);
            calnum = 0;
        } else if (calnum == 11) {
            socketFunction.SocketSending(IntToStr(xPoint * xScale + xInit) + "," + IntToStr(yPoint * yScale + yInit) + "\n\0d");
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Graphics g = getGraphics();
        g.setColor(Color.blue);
        g.fillOval(x[0] - 5, y[0] - 5, 10, 10);
        g.drawOval(x[0] - 5, y[0] - 5, 10, 10);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        setInfo(e);
    }
}