package mainscreen;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Toolkit;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


public class MMPanel extends JPanel implements Runnable {

    private Image court;
    private Image ball;
    private Image crazyBall;
    private Thread animator;
    private final int DELAY = 35;
    private List<Ball> balls;
    private boolean animate = true;

    public MMPanel() {

        initPanel();
    }
    
    private void initPanel() {
        
        loadImage();
        
        int w = court.getWidth(this);
        int h =  court.getHeight(this);
        balls = new ArrayList<Ball>();
        
       
    }
    
    private void loadImage() {
        
        ImageIcon ii = new ImageIcon(getClass().getResource("/mainscreen/resources/court.jpg"));
        court = ii.getImage();    
        ii = new ImageIcon(getClass().getResource("/mainscreen/resources/ball.png"));
        ball = ii.getImage(); 
        ii = new ImageIcon(getClass().getResource("/mainscreen/resources/crazy_ball.gif"));
        crazyBall = ii.getImage().getScaledInstance(ball.getWidth(this), ball.getHeight(this), 0);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        animator = new Thread(this);
        animator.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(getWidth() <= 0 || getHeight() <= 0)
            return; 
        g.drawImage(court, 0, 0, null);
        Ball[] ballsArray = balls.toArray(new Ball[balls.size()]);
        for(Ball b: ballsArray) {
            Image image = (b.crazyBall ? crazyBall : ball);
            g.drawImage(image, b.getX(), b.getY(), this);
        }
        Toolkit.getDefaultToolkit().sync();
    }
    
    public void stopAnimation() {
        animate = false;
        ball = null;
        Iterator<Ball> itr = balls.iterator();
        
    }
    
    public void startAnimation() {
        animate = true;
        this.loadImage();
    }
    
    public void startCrazySequence() {
        if(!animate)
            return;
        Random gen = new Random();
        for(int j = 0; j < 10; j++) {
            for(int i = 0; i < getHeight()/5; i+=5) {
                int x = gen.nextInt(2500)-2500;
                int y = i;
                int speed = gen.nextInt(10)+5;
                int dx = 1;
                int dy = gen.nextInt(5);
                balls.add(new Ball(x,dx,y,dy,speed,true));
            }
        }
    }

    private void cycle() {
        if(!animate)
            return;
        
        int currSpeed = 1;
        int sign = 1;
        while((balls.size()+1)*ball.getHeight(this) < getHeight()) {
            Random rand = new Random();
            int x = 0;//rand.nextInt(getWidth());
            int y = (balls.size())*ball.getHeight(this);
            int dx = rand.nextInt(3)+1;
            int dy = 0;//rand.nextInt(3)+1;//(int)((Math.random()+.5)*5);
            int speed =  currSpeed;//rand.nextInt(5)+1;
            if(sign == 1 && currSpeed == Ball.MAX_SPEED)
                sign = -1;
            else if(sign == -1 && currSpeed == 1)
                sign = 1;
            currSpeed += sign;
            Ball b = new Ball(x,dx,y,dy,speed);
            //System.out.println("Ball:"+b.getX()+" "+b.getY());
            balls.add(b);
        }
        
        int maxBalls = getHeight() / ball.getHeight(this);//Max amount of non-crazy balls
        Iterator<Ball> itr = balls.iterator();
        while(itr.hasNext()) {
            Ball b = itr.next();
            b.move(getWidth(),getHeight());
            if(b.crazyBall && b.getX() > getWidth())
                itr.remove();
            if(!b.crazyBall){
                if(maxBalls > 0)
                    maxBalls--;
                else if(maxBalls == 0)
                    itr.remove();
            }
        }
    }

    @Override
    public void run() {

        long beforeTime, timeDiff, sleep;

        beforeTime = System.currentTimeMillis();

        while (true) {

            cycle();
            repaint();

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = DELAY - timeDiff;

            if (sleep < 0) {
                sleep = 2;
            }

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                System.out.println("Interrupted: " + e.getMessage());
            }

            beforeTime = System.currentTimeMillis();
        }
    }
}
class Ball {
    public int x,dx,y,dy,initX,initY;
    public double speed;
    private Random rand;
    public static final int MAX_SPEED = 6;
    public int accel = 1;
    public boolean crazyBall = false;


    public Ball(int initX,int dx, int initY, int dy, double speed) {
        this.x = initX;
        this.initX = initX;
        this.dx = dx;
        this.y = initY;
        this.initY = initY;
        this.dy = dy;
        this.speed = speed;
        rand = new Random();
    }
    
    public Ball(int initX,int dx, int initY, int dy, double speed,boolean crazyBall) {
        this.x = initX;
        this.initX = initX;
        this.dx = dx;
        this.y = initY;
        this.initY = initY;
        this.dy = dy;
        this.speed = speed;
        this.crazyBall = crazyBall;
        rand = new Random();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    
    public void move(int maxX, int maxY) {
        x += speed*dx;
        y += speed*dy;

        if (y > maxY) {
            dy *= -1;
            y = maxY;
        }else if(y < 0) {
            dy *= -1;
            y = 0;
        }

        if(!crazyBall && x > maxX) {
            dx *= -1;
            if(accel == 1 && speed == MAX_SPEED)
                accel = -1;
            else if(accel == -1 && speed == 1)
                accel = 1;
            speed += accel;
            x = maxX;
        }else if(!crazyBall && x < 0) {
            dx *= -1;
            if(accel == 1 && speed == MAX_SPEED)
                accel = -1;
            else if(accel == -11 && speed == 1)
                accel = 11;
            x = 0;
        }
    }
}
