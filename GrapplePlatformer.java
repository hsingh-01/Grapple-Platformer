import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.List;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Font;

import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JLabel;

import java.lang.Math;
import java.util.Random;

import java.text.DecimalFormat;

public class GrapplePlatformer {
	DecimalFormat speedFormat = new DecimalFormat("0.00");

	public static final int WIDTH = 800;
	public static final int HEIGHT = 800;
	public static final double G = 0.02;
	public static final int BALL_RAD = 14;
	public static boolean PAUSE = true;
	public static final double X_A = 0.02;
	public static final double X_FRIC = 0.0;
	public static final double Y_BOUNCE_FRIC = -1;
	public static final int GAME_SPEED = 5;
	public static final double GRAP_CD = 1.5;
	public static final double GRAP_GIVEBACK = 0.25;
	public static int MS_ELAPSED = 0;

	public static final int SCROLL_MARK = WIDTH;

	private JFrame window;

	private JButton resetButton;
	private JButton pauseButton;
	private JButton createGroundButton;

	private JLabel ballPosLabel;

	private JPanel buttonPanel;
	private GraphicsPanel graphicsPanel;
	private Timer timer;
	private ArrayList<Ground> grounds = new ArrayList<>();
	Ball ball = new Ball(WIDTH/2, HEIGHT/2);


	Color grappleBarColor = new Color(35, 49, 140);
	Color ballColor = new Color(255, 110, 134);
	Color backgroundColor = new Color(187, 199, 183);
	Color grappleColor = new Color(0, 0, 0);

	public GrapplePlatformer(){
		window = new JFrame("Grapple Platformer");
		window.setSize(WIDTH, HEIGHT);
		window.setLayout(new BorderLayout());

		buttonPanel = new JPanel(new FlowLayout());

		resetButton = new JButton("reset");
		pauseButton = new JButton("pause");
		createGroundButton = new JButton("create ground");

		ballPosLabel = new JLabel();
		ballPosLabel.setBounds(WIDTH/2, 200, 50, 100);
		ballPosLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		Font monoF = new Font(Font.MONOSPACED, Font.PLAIN, 14);
		ballPosLabel.setFont(monoF);

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
		graphicsPanel.setBackground(backgroundColor);
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
			ball.setY(HEIGHT/2);
			ball.setDisplayX(0);
			ball.setDisplayY(0);
			ball.setVX(0);
			ball.setVY(0);

			ball.setGrapOnCooldown(false);
			ball.setGrapMeter(Ball.GRAP_LIMIT);

			grounds.clear();
			CreateGroundListener resetGroundTemp = new CreateGroundListener();
			resetGroundTemp.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
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
				pauseButton.setText("pause");
				graphicsPanel.updatePosition();  
				graphicsPanel.repaint();  
			}
			if (PAUSE){
				pauseButton.setText("play");
			}
			MS_ELAPSED += GAME_SPEED;
		}
	}

	private class CreateGroundListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
			grounds.clear();
			for (int i = 0; i < 5; i++){
				for (int j = 0; j < 5; j++){
					Ground g = new Ground(25, 150 + 500 * i, 100 + 500 * j, "CIRC");
					grounds.add(g);

					// Ground floor = new Ground(0, graphicsPanel.getHeight() - 30, 5000, 15, "RECT");
					// grounds.add(floor);
				}
			}
			graphicsPanel.requestFocusInWindow();
		}
	}

	private class ballMotionListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e){
			if (e.getKeyCode() == KeyEvent.VK_LEFT){
				ball.setAX(-X_A);
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT){
				ball.setAX(X_A);
			}
		}
		@Override
		public void keyReleased(KeyEvent e){
			if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT){
				ball.setAX(0);
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
			if (e.getKeyCode() == KeyEvent.VK_Z){
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

		private double display_x, display_y; 

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

		private boolean grapOnCd = false;

		public static final int GRAP_LIMIT = 2400;
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

		public void setDisplayX(double dpx){this.display_x = dpx; }
		public void setDisplayY(double dpy){this.display_y = dpy; }

		public double getDisplayX(){ return this.display_x; }
		public double getDisplayY(){ return this.display_y; }


		public boolean grapAvailable() { return grapAvailable; }
		public void setGrapAvailable(boolean t){ grapAvailable = t; }

		public int getGrapMeter(){ return grapMeter; }
		public void changeGrapMeter(int g){ grapMeter += g; }
		public void setGrapMeter(int g){ grapMeter = g; }

		public void checkCollisions(){
			speed = Math.sqrt(getVX() * getVX() + getVY() * getVY());
			boolean collFound = false;
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
						collFound = true;
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
						collFound = true;
					}
				}
			}
			ball.setColl(collFound);
		}
		public void collisionVelocityUpdate(double[] v, double[] n){
			// v' = v - 2(v \cdot n)n
			double[] v_prime;
			double[] term2 = Vector.scale(n, 2 * Vector.dot(v, n));
			v_prime = Vector.subtract(v, term2);
			ball_v = v_prime;
			// ball_x -= n[0] * BALL_RAD;
			// ball_y -= n[1] * BALL_RAD;
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

		public void setGrapOnCooldown(boolean g){
			grapOnCd = g;
			if (g){
				MS_ELAPSED = 0;
			}
		}

		public boolean grapOnCd(){
			return grapOnCd;
		}
	}

	private class Ground{
		private double x, y;
		private int w, h;
		private int rad;
		private String shapeType;

		public Ground(double x, double y, int w, int h, String shape){
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.shapeType = shape;
			graphicsPanel.repaint();
		}
		public Ground(int rad, double x, double y, String shape){
			this.rad = rad;
			this.x = x;
			this.y = y;
			this.shapeType = shape;
			graphicsPanel.repaint();
		}

		public double getX(){ return x; }
		public double getY(){ return y; }
		public int getW(){ return w; }
		public int getH(){ return h; }

		public void setX(double x) { this.x = x; }
		public void setY(double y) { this.y = y; }

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

		public static final double noGrappleCircleWidth = 1; 
		public static final int GRAP_USE = -3;
		public static final int GRAP_REC = +2;
		public static final int BAR_SCALE = 3;

		public void updatePosition(){
			if (MS_ELAPSED == GRAP_CD * 1000){
				if (ball.grapOnCd()){
					ball.setGrapOnCooldown(false);
					ball.changeGrapMeter((int)(Ball.GRAP_LIMIT * (GRAP_GIVEBACK)));
				}
			}

			if (!ball.getGrap() && ball.getGrapMeter() < ball.GRAP_LIMIT && !ball.grapOnCd()){ ball.changeGrapMeter(GRAP_REC); }
			
			ballPosLabel.setText("(" + (int)(ball.getDisplayX()) + ", " + (int)(ball.getDisplayY()) + ") speed: " + speedFormat.format(ball.getSpeed()));
			ball.setVY(ball.getVY() + ball.getAY() + G);
			ball.setDisplayY(ball.getDisplayY() + ball.getVY());
			ball.setY(ball.getY() + ball.getVY());


			ball.setVX(ball.getVX() + ball.getAX());
			ball.setDisplayX(ball.getDisplayX() + ball.getVX());
			ball.setX(ball.getX() + ball.getVX());

			if (ball.getVX() > 0){
				ball.setVX(Math.max(0, ball.getVX() - X_FRIC));
			}
			if (ball.getVX() < 0){
				ball.setVX(Math.min(0, ball.getVX() + X_FRIC));
			}

			if (ball.getX() > graphicsPanel.getWidth() - SCROLL_MARK || ball.getX() < SCROLL_MARK){
				double ballvx = ball.getVX();
				ball.setX(ball.getX() - ballvx);

				for (Ground gr : grounds) {
					gr.setX((gr.getX()-ballvx));
				}
			}		
			if (ball.getY() > graphicsPanel.getHeight() - SCROLL_MARK || ball.getY() < SCROLL_MARK){
				double ballvy = ball.getVY();
				ball.setY(ball.getY() - ballvy);

				for (Ground gr : grounds) {
					gr.setY((gr.getY()-ballvy));
				}
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
			// System.out.println(ball.grapOnCd());
			if (ball.getGrap() && ball.grapAvailable() && !ball.grapOnCd()){
				double nx = dx / dist;
				double ny = dy / dist;
				double v[] = {ball.getVX(),	ball.getVY()};
				double n[] = {nx, ny};
				double mag = Math.sqrt(nx * nx + ny * ny);
				n = Vector.scale(n, 1/mag);
				double radVel = Vector.dot(v, n);
				if (ball.getGrapMeter() > 0){ ball.changeGrapMeter(GRAP_USE); } // deplete meter

				if (radVel < 0){ // ball moving away from grap point
					ball.setVX(ball.getVX() - radVel * n[0]);
					ball.setVY(ball.getVY() - radVel * n[1]);		
				}
			}
			if (ball.getGrapMeter() <= 0 && !ball.grapOnCd()){
				ball.setGrapOnCooldown(true);
			}
			graphicsPanel.repaint();
		}

		@Override
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			float thickness = 1;
			int ballBorderThickness = 2;
			int grappleThickness = 1;
			int grapBarThickness = 5;

			Graphics2D g2d = (Graphics2D) g; //needed for setsrtoke
			g2d.setStroke(new BasicStroke(thickness));

			if (ball.getGrap() == true && ball.grapAvailable() && !ball.grapOnCd()){
				thickness = grappleThickness;
				g2d.setStroke(new BasicStroke(thickness));
				g2d.setColor(grappleColor);
				g2d.drawLine((int) ball.getX(), (int) ball.getY(), (int)ball.getX() + (int) GraphicsPanel.getDX(), (int)ball.getY() + (int) GraphicsPanel.getDY());
			}
			if (ball.getGrap() && (ball.grapOnCd() || !ball.grapAvailable())){
				g2d.setColor(grappleColor);
				thickness = ballBorderThickness;
				g2d.setStroke(new BasicStroke(thickness));
				g2d.drawOval((int) (ball.getX() - GRAP_LEN), (int) (ball.getY() - GRAP_LEN), (int) (2 * GRAP_LEN), (int)(2 * GRAP_LEN));
			}	

			g2d.setColor(ballColor);
			g2d.fillOval((int)ball.getX() - BALL_RAD, (int)ball.getY() - BALL_RAD, BALL_RAD*2, BALL_RAD*2);
			g2d.setColor(Color.BLACK);
			g2d.drawOval((int)ball.getX() - BALL_RAD, (int)ball.getY() - BALL_RAD, BALL_RAD*2, BALL_RAD*2);

			for (Ground gr : grounds){
				if (gr.getType().equals("RECT")){
					g2d.fillRect((int)gr.getX(), (int)gr.getY(), gr.getW(), gr.getH());
				}
				if (gr.getType().equals("CIRC")){
					g2d.fillOval((int)gr.getX(), (int)gr.getY(), (int)gr.getRad() * 2, (int)gr.getRad() * 2);
				}
			}

			g2d.setColor(grappleBarColor);
			g2d.fillRect(((int)getWidth()/2 - ball.GRAP_LIMIT/(BAR_SCALE*2)), 30, ball.getGrapMeter()/BAR_SCALE, 25);
			
			if (ball.grapOnCd() && ball.getGrap()){ g2d.setColor(Color.RED); }
			else { g2d.setColor(Color.BLACK); }

			// outline of meter bar
			thickness = grapBarThickness;
			g2d.setStroke(new BasicStroke(thickness));
			g2d.drawRect(((int)getWidth()/2 - ball.GRAP_LIMIT/(BAR_SCALE*2)), 30, ball.GRAP_LIMIT/BAR_SCALE, 25);
		}
	}
}

public void main(String[] args){
	new GrapplePlatformer();
}