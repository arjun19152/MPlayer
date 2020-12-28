import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class MPlayer {
    private static BufferedImage img;
    private static JFrame f=new JFrame();
    private static JFileChooser fileChooser;
    private static File myfile=null;
    private static String fileName;
    private static String filePath;

    private static long totalLength=0;
    private static long pauseLength=0;

    private FileInputStream fileInputStream= null;
    private BufferedInputStream bufferedInputStream=null;

    private Thread play;
    private Thread checker;
    private Thread resume;

    private Player player;

    private boolean wasPaused=false;
    private boolean playing=false;
    private Object pauseLock =new Object();


    public class Panel extends JPanel{

        Panel() throws IOException {

            resume=new Thread(runnableResume);
            resume.setName("resume");
            checker=new Thread();
            checker.setDaemon(true);
            checker.start();
            img= ImageIO.read(getClass().getResource("src/Radio.png"));
            this.setOpaque(false);
            this.setLayout(new FlowLayout());

            JButton btn0 =new JButton();
            JButton btn1=new JButton();
            JButton btn2=new JButton();
            JButton btn3=new JButton();
            JButton btn4=new JButton();

            btn0.setSize(2,2);
            btn0.setIcon(new ImageIcon(getClass().getResource("src/play-button.png")));
            btn0.setOpaque(false);
            btn0.setContentAreaFilled(false);
            btn0.setBorderPainted(false);
            btn0.setFocusPainted(false);



            btn1.setSize(2,2);
            btn1.setIcon(new ImageIcon(getClass().getResource("src/pause.png")));
            btn1.setOpaque(false);
            btn1.setContentAreaFilled(false);
            btn1.setBorderPainted(false);
            btn1.setFocusPainted(false);


            btn2.setSize(2,2);
            btn2.setIcon(new ImageIcon(getClass().getResource("src/folder.png")));
            btn2.setOpaque(false);
            btn2.setContentAreaFilled(false);
            btn2.setBorderPainted(false);
            btn2.setFocusPainted(false);


            btn3.setSize(2,2);
            btn3.setIcon(new ImageIcon(getClass().getResource("src/minimize.png")));
            btn3.setOpaque(false);
            btn3.setContentAreaFilled(false);
            btn3.setBorderPainted(false);
            btn3.setFocusPainted(false);


            btn4.setSize(2,2);
            btn4.setIcon(new ImageIcon(getClass().getResource("src/remove.png")));
            btn4.setOpaque(false);
            btn4.setContentAreaFilled(false);
            btn4.setBorderPainted(false);
            btn4.setFocusPainted(false);

            btn0.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {

                    if(myfile==null){
                        try {
                            BufferedImage imgx=ImageIO.read(getClass().getResource("src/music.png"));
                            JOptionPane.showMessageDialog(f,"Choose a Song to Play!","Select a Song Da",JOptionPane.WARNING_MESSAGE,new ImageIcon(imgx));
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        }
                    else{
                    if (playing==true){
                        player.close();
                        playing=false;
                        }
                    btn1.setIcon(new ImageIcon(getClass().getResource("src/pause.png")));
                    pauseLength=0;
                    wasPaused=false;
                    play=new Thread(runnablePlay);
                    play.setName("Play");
                    play.start();
                    }
                }
            });

            btn1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!wasPaused){
                        try {
                            System.out.println(resume.holdsLock(pauseLock));
                            if(resume.isAlive()){
                                resume=new Thread(runnableResume);
                            }
                            pauseLength=fileInputStream.available();
                            btn1.setIcon(new ImageIcon(getClass().getResource("src/resume.png")));
                        } catch (Exception Exception) {
                            Exception.printStackTrace();
                        }
                        player.close();
                        wasPaused=true;
                    }
                    else{
                        btn1.setIcon(new ImageIcon(getClass().getResource("src/pause.png")));
                        resume.start();
                    }
                }
            });
            btn2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                   fileChooser=new JFileChooser();
                   fileChooser.setCurrentDirectory(new File("src/Music"));
                   fileChooser.setDialogTitle("Select MP3");
                   if(fileChooser.showOpenDialog(btn2)==JFileChooser.APPROVE_OPTION){
                       myfile=fileChooser.getSelectedFile();
                       fileName=fileChooser.getSelectedFile().getName();
                       filePath=fileChooser.getSelectedFile().getPath();
                       pauseLength=0;
                   }
                }
            });

            btn3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    f.setState(f.ICONIFIED);
                }
            });

            btn4.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

            this.add(btn0);
            this.add(btn1);
            this.add(btn2);
            this.add(btn3);
            this.add(btn4);
        }

        public Dimension getPreferredSize(){
            return img==null?new Dimension(200,200):new Dimension(img.getWidth(),img.getHeight());
        }

        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            if(img!=null){
                Graphics2D g2d=(Graphics2D)g.create();
                g2d.drawImage(img,0,0,this);
                g2d.dispose();
            }
        }
    }

    Runnable runnablePlay =new Runnable() {
        @Override
        public void run() {
            try {

                fileInputStream = new FileInputStream(myfile);
                bufferedInputStream=new BufferedInputStream(fileInputStream);
                player=new Player(bufferedInputStream);
                totalLength=fileInputStream.available();
                playing=true;
                player.play();
                playing=false;
            } catch (Exception ex) {
                ex.printStackTrace();

            }
        }
    };


    Runnable runnableResume=new Runnable() {
        @Override
        public void run() {

            synchronized (pauseLock){
            try{
                wasPaused=false;
            fileInputStream=new FileInputStream(myfile);
            fileInputStream.skip(totalLength-pauseLength);
            bufferedInputStream=new BufferedInputStream(fileInputStream);
            player=new Player(bufferedInputStream);
            player.play();
            playing=false;}
            catch (Exception e){
                e.printStackTrace();
            }
        }}
    };



    private MouseListener listener=new MouseAdapter() {

        private Point mouseCoords=null;
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            Point coord=e.getPoint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            Point currCords=e.getLocationOnScreen();
            f.setLocation(currCords.x-mouseCoords.x,currCords.y-mouseCoords.y);
            mouseCoords=null;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            mouseCoords=e.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            Point currCords=e.getLocationOnScreen();
            f.setLocation(currCords.x-mouseCoords.x,currCords.y-mouseCoords.y);
        }
    };

    public static void main(String[] args) throws IOException {
        MPlayer obj=new MPlayer();
        Panel p=obj.new Panel();
        f.setTitle("MP3 Player");
        f.setIconImage(ImageIO.read(new File("src/TitleIcon.png")));
        f.setContentPane(p);
        f.setUndecorated(true);
        f.setBackground(new Color(0,0,0,0));
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        f.setAlwaysOnTop(true);
    }
}
