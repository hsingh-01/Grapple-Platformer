import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.List;

import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.lang.Math;
import java.util.Random;


public class BouncyBallSim {
	public static final int WIDTH = 800;
	public static final int HEIGHT = 800;
	public static final double G = 0.029;
	public static final int BALL_RAD = 10;
	public static boolean PAUSE = true;
	public static final double X_A = 0.02;
	public static final double X_FRIC = 0.0;
	public static final double Y_BOUNCE_FRIC = -1;
	public static final int GAME_SPEED = 5;
	public Ball ball = new Ball(100, 100);
	private JFrame window;


	private JButton resetButton;
	private JButton pauseButton;
	private JButton createGroundButton;

	private JTextField ballPosLabel;

	private JPanel buttonPanel;
	private GraphicsPanel graphicsPanel;
	private Timer timer;
	private ArrayList<Ground> grounds = new ArrayList<>();

	public BouncyBallSim(){
		Ball ball = new Ball(WIDTH/2, 50);
		window = new JFrame("Bouncing Ball");
		window.setSize(WIDTH, HEIGHT);
		window.setLayout(new BorderLayout());

		buttonPanel = new JPanel(new FlowLayout());

		resetButton = new JButton("reset");
		pauseButton = new JButton("pause");
		createGroundButton = new JButton("create ground");

		ballPosLabel = new JTextField(20);
		ballPosLabel.setBounds(WIDTH/2, 100, 50, 100);
		ballPosLabel.setHorizontalAlignment(SwingConstants.CENTER);

		resetButton.addActionListener(new ResetListener());
		pauseButton.addActionListener(new PauseButtonListener());
		createGroundButton.addActionListener(new CreateGroundListener());

		buttonPanel.add(resetButton);
		buttonPanel.add(pauseButton);
		buttonPanel.add(createGroundButton);

		window.add(buttonPanel, BorderLayout.SOUTH);

		graphicsPanel = new GraphicsPanel();
		graphicsPanel.add(ballPosLabel);

		graphicsPanel.addKeyListener(new ballMotionListener());
		graphicsPanel.addKeyListener(new grappleListener());
		graphicsPanel.setFocusable(true);
		graphicsPanel.requestFocusInWindow();
		window.add(graphicsPanel, BorderLayout.CENTER);
		timer = new Timer(GAME_SPEED, new TimerListener());
 
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		timer.start();
	}

	private class ResetListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			ball.setX(WIDTH/2);
			ball.setY(100);
			ball.setVX(0);
			ball.setVY(0);
			graphicsPanel.requestFocusInWindow();
		}
	}

	private class PauseButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			PAUSE = !PAUSE;
			graphicsPanel.requestFocusInWindow();
		}
	}

	private class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (!PAUSE){
				graphicsPanel.updatePosition();  
				graphicsPanel.repaint();  
			}
		}
	}

	private class CreateGroundListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
			for (int i = 0; i < 5; i++){
				Ground g2 = new Ground(25, 300, 200, "CIRC");
				Ground g = new Ground(25, 100, 300, "CIRC");
				grounds.add(g);
				grounds.add(g2);    			
				Ground floor = new Ground(200, 500, 350, 25, "RECT");
				grounds.add(floor);
			}

			graphicsPanel.requestFocusInWindow();
		}
	}

	private class ballMotionListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e){
			if (e.getKeyCode() == KeyEvent.VK_LEFT){
				ball.ball_a[0] = -X_A;

			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT){
				ball.ball_a[0] = X_A;
			}
		}
		@Override
		public void keyReleased(KeyEvent e){
			if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT){
				ball.ball_a[0] = 0;
			}
		}
		@Override
		public void keyTyped(KeyEvent e){
			return; 
		}
	}

	public class grappleListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e){
			if (e.getKeyCode() == KeyEvent.VK_Z && ball.grapAvailable()){
				ball.setGrap(true);
			}
		}
		@Override
		public void keyReleased(KeyEvent e){
			if (e.getKeyCode() == KeyEvent.VK_Z){
				ball.setGrap(false);
				graphicsPanel.grappleTarget = null;
			}
		}
		@Override
		public void keyTyped(KeyEvent e){
			return;
		}

	}

	private class Ball{
		private double ball_x;
		private double ball_y;

		private double[] ball_v = new double[2];

		private double[] ball_a = new double[2];
		private double dist;
		//private double dx, dy;
		private double closeX, closeY;
		private double[] n = {0, 0};
		public boolean GRAP = false;
		public double speed = 0.0;
		private boolean isColl = false;
		private boolean grapAvailable = false;

		public static final int GRAP_LIMIT = 500;
		private int grapMeter = GRAP_LIMIT;


		public Ball(double bx, double by){
			this.ball_x = bx;
			this.ball_y = by;

			ball_v[0] = 0;
			ball_v[1] = 0;

			ball_a[0] = 0;
			ball_a[1] = 0;
		}

		public double getX(){ return ball_x; }
		public double getY(){ return ball_y; }
		public double getVX(){ return ball_v[0]; }
		public double getVY(){ return ball_v[1]; }
		public double getAX(){ return ball_a[0]; }
		public double getAY(){ return ball_a[1]; }
		public double getSpeed() { return speed; }
		public boolean isColl() {return isColl; }
		public void setColl(boolean t) { isColl = t; }

		public boolean getGrap() { return GRAP; }
		public void setGrap(boolean g) { GRAP = g; }

		public void setX(double dx){ this.ball_x = dx;}
		public void setY(double dy){ this.ball_y = dy;}

		public void setVX(double dvx){ this.ball_v[0] = dvx; }
		public void setVY(double dvy){ this.ball_v[1] = dvy; }

		public void setAX(double dax){ this.ball_a[0] = dax; }
		public void setAY(double day){ this.ball_a[1] = day; }

		public boolean grapAvailable() { return grapAvailable; }
		public void setGrapAvailable(boolean t){ grapAvailable = t; }

		public int getGrapMeter(){ return grapMeter; }
		public void changeGrapMeter(int g){ grapMeter += g; }
		public void setGrapMeter(int g){ grapMeter = g; }



		public void checkCollisions(){
			speed = Math.sqrt(getVX() * getVX() + getVY() * getVY());
			for (Ground gr : grounds){
				if (gr.getType().equals("RECT")){
	
					double[] n = getDxDy(gr);
					double dx = n[0];
					double dy = n[1];

					if (Math.abs(dx) > Math.abs(dy)){
						n = new double[] {Math.signum(dx), 0};
					}
					else if (Math.abs(dy) > Math.abs(dx)){
						n = new double[] {0, Math.signum(dy)};
					}
					if (dx * dx + dy * dy < BALL_RAD * BALL_RAD){
						collisionVelocityUpdate(ball_v, n);
						// setGrapAvailable(false);
						ball.setColl(true);
					}
				}
				else if (gr.getType().equals("CIRC")){
					double[] n = getDxDy(gr);
					double dx = n[0];
					double dy = n[1];

					double mag = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
					n = Vector.scale(n, (1/mag));	
					if (dx * dx + dy * dy < Math.pow(gr.getRad() + BALL_RAD, 2)){	
						collisionVelocityUpdate(ball_v, n);			
						ball.setColl(true);
					}
				}
			}
			ball.setColl(false);
		}
		public void collisionVelocityUpdate(double[] v, double[] n){
			// v' = v - 2(v \cdot n)n
			double[] v_prime;
			double[] term2 = Vector.scale(n, 2 * Vector.dot(v, n));
			v_prime = Vector.subtract(v, term2);
			ball_v = v_prime;
		}

		public double[] getDxDy(Ground gr){
			if (gr.getType().equals("RECT")){
				closeX = 0;
				if (ball_x < gr.getX()){ closeX = gr.getX(); }
				else if (ball_x > gr.getX() + gr.getW()){ closeX = gr.getX() + gr.getW(); }
				else { closeX = ball_x; }

				closeY = 0;
				if (ball_y < gr.getY()){ closeY = gr.getY(); }
				else if (ball_y > gr.getY() + gr.getH()){ closeY = gr.getY() + gr.getH(); }
				else { closeY = ball_y; }	

				double dx = (closeX - ball_x);
				double dy = (closeY - ball_y);	
				return new double[] {dx, dy};
			}
			else if (gr.getType().equals("CIRC")){
				double dx = gr.getX() + gr.getRad() - ball_x;
				double dy = gr.getY() + gr.getRad() - ball_y;	
				return new double[] {dx, dy};			
			}
			return new double[] {0, 0};
		}
	}

	private class Ground{
		private int x, y, w, h;
		private int rad;
		private String shapeType;
		public Ground(int x, int y, int w, int h, String shape){
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.shapeType = shape;
			graphicsPanel.repaint();
		}
		public Ground(int rad, int x, int y, String shape){
			this.rad = rad;
			this.x = x;
			this.y = y;
			this.shapeType = shape;
			graphicsPanel.repaint();
		}

		public int getX(){ return x; }
		public int getY(){ return y; }
		public int getW(){ return w; }
		public int getH(){ return h; }
		public double getRad(){ return rad; }
		public String getType(){ return shapeType; }
	}


	private class GraphicsPanel extends JPanel{
		private static double dx = 0;
		private static double dy = 0;
		public static Ground grappleTarget = null;
		public static final double GRAP_LEN = 300.0;

		public static double getDX(){ return dx; }
		public static double getDY(){ return dy; }

		public void updatePosition(){
			if (!ball.getGrap() && ball.getGrapMeter() < ball.GRAP_LIMIT){ ball.changeGrapMeter(+1); }
			ballPosLabel.setText((int)ball.getX() + ", " + (int)ball.getY());
			ball.setVY(ball.getVY() + ball.getAY() + G);
			ball.setY(ball.getY() + ball.getVY());

			ball.setVX(ball.getVX() + ball.getAX());
			ball.setX(ball.getX() + ball.getVX());

			if (ball.getVX() > 0){
				ball.setVX(Math.max(0, ball.getVX() - X_FRIC));
			}
			if (ball.getVX() < 0){
				ball.setVX(Math.min(0, ball.getVX() + X_FRIC));
			}

			ball.checkCollisions();

			double minDist = Double.MAX_VALUE, dist = 0;
			Ground closeGround = null;

			if (ball.getGrap() && grappleTarget == null){
				for (Ground gr : grounds){
					double[] d = ball.getDxDy(gr);
					double d2 = Math.sqrt(d[0] * d[0] + d[1] * d[1]);

					if (d2 < minDist && gr.getType().equals("CIRC")){
						minDist = d2;
						closeGround = gr;
					}
				}
				grappleTarget = closeGround; // find closest ground
			}

			if (grappleTarget != null){ // find distance from closest ground
				double[] d = ball.getDxDy(grappleTarget);

				dx = d[0];
				dy = d[1];

				if (grappleTarget.getType().equals("CIRC")){
					dist = (Math.sqrt(d[0] * d[0] + d[1] * d[1]) - BALL_RAD - grappleTarget.getRad());
				}
				else {
					dist = Math.sqrt(d[0] * d[0] + d[1] * d[1]);
				}
			}
			 // only then calculate distances - prevents multi-object and object switching when grappling
			ball.setGrapAvailable(!ball.isColl() && dist < GRAP_LEN && ball.getGrapMeter() > 0);
			// System.out.println(ball.grapAvailable());
			if (ball.getGrap() && ball.grapAvailable()){
				double nx = dx / dist;
				double ny = dy / dist;
				double v[] = {ball.getVX(),	ball.getVY()};
				double n[] = {nx, ny};
				double mag = Math.sqrt(nx * nx + ny * ny);
				n = Vector.scale(n, 1/mag);
				System.out.println(dist);
				double radVel = Vector.dot(v, n);
				// System.out.println(Vector.mag(n));
				if (ball.getGrapMeter() > 0){ ball.changeGrapMeter(-1); }

				if (radVel < 0){ // ball moving away from grap point
					ball.setVX(ball.getVX() - radVel * n[0]);
					ball.setVY(ball.getVY() - radVel * n[1]);		
				}
			}

			graphicsPanel.repaint();
		}

		@Override
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			g.setColor(Color.RED);
			g.fillOval((int)ball.getX() - BALL_RAD, (int)ball.getY() - BALL_RAD, BALL_RAD*2, BALL_RAD*2);
			g.setColor(Color.BLACK);

			for (Ground gr : grounds){
				if (gr.getType().equals("RECT")){
					g.fillRect(gr.getX(), gr.getY(), gr.getW(), gr.getH());
				}
				if (gr.getType().equals("CIRC")){
					g.fillOval(gr.getX(), gr.getY(), (int)gr.getRad() * 2, (int)gr.getRad() * 2);
				}
			}
			g.setColor(Color.GRAY);
			if (ball.getGrap() == true && ball.grapAvailable()){
				g.drawLine((int) ball.getX(), (int) ball.getY(), (int)ball.getX() + (int) GraphicsPanel.getDX(), (int)ball.getY() + (int) GraphicsPanel.getDY());
			}
			g.fillRect(30, 30, ball.getGrapMeter(), 25);
		}
	}
}

public void main(String[] args){
	new BouncyBallSim();
}