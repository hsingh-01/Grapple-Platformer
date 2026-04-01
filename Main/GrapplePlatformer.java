// jar cfe GrapplePlatformer.jar GrapplePlatformer *.class
// javac -d build Main/*.java Listener/*.java Class/*.java 
// java -cp build Main.GrapplePlatformer  

package Main;
import Class.*;
import Listener.*;

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
import java.util.ArrayList;

import java.text.DecimalFormat;

public class GrapplePlatformer {
	DecimalFormat speedFormat = new DecimalFormat("0.00");

	public static final int WIDTH = 1600;
	public static final int HEIGHT = 800;
	public static final double G = 0.023;
	public static boolean PAUSE = true;
	public static final double X_FRIC = 0.004;
	public static final double Y_BOUNCE_FRIC = -1;
	public static final int GAME_SPEED = 4;
	public static final double GRAP_CD = 1.5;
	public static final double GRAP_GIVEBACK = 0.25;
	public static int MS_ELAPSED = 0;

	public static final int SCROLL_MARK = WIDTH;

	private JFrame window;

	public JButton resetButton;
	public static JButton pauseButton;
	public JButton createGroundButton;

	private JLabel ballPosLabel;

	private JPanel buttonPanel;
	public static GraphicsPanel graphicsPanel;
	private Timer timer;
	public static ArrayList<Ground> grounds = new ArrayList<>();
	public static Ball ball = new Ball(WIDTH/2, HEIGHT/2);


	Color grappleBarColor = new Color(35, 49, 140);
	Color ballColor = new Color(255, 110, 134);
	Color backgroundColor = new Color(52, 180, 235);
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
		//buttonPanel.add(createGroundButton);

		window.add(buttonPanel, BorderLayout.SOUTH);

		graphicsPanel = new GraphicsPanel();
		graphicsPanel.add(ballPosLabel);

		graphicsPanel.addKeyListener(new BallMotionListener());
		graphicsPanel.addKeyListener(new GrappleListener());
		graphicsPanel.setFocusable(true);
		graphicsPanel.requestFocusInWindow();
		graphicsPanel.setBackground(backgroundColor);
		window.add(graphicsPanel, BorderLayout.CENTER);
		timer = new Timer(GAME_SPEED, new TimerListener());

		createGround();

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		timer.start();
	}

	public void createGround(){
		CreateGroundListener resetGroundTemp = new CreateGroundListener();
		resetGroundTemp.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));		
	}

	public class GraphicsPanel extends JPanel{
		private static double dx = 0;
		private static double dy = 0;
		public static Ground grappleTarget = null;
		public static final double GRAP_LEN = 600.0;

		public static double getDX(){ return dx; }
		public static double getDY(){ return dy; }

		public static final double noGrappleCircleWidth = 1; 
		public static final int GRAP_USE = -3;
		public static final int GRAP_REC = +2;
		public static final int BAR_SCALE = 6;

		public void updatePosition(){
			if (MS_ELAPSED >= GRAP_CD * 1000){
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
				ball.setVX(Math.max(0, ball.getVX() * (1-X_FRIC)));
			}
			if (ball.getVX() < 0){
				ball.setVX(Math.min(0, ball.getVX() * (1-X_FRIC)));
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
					if (gr.isScrolleable()){gr.setY((gr.getY()-ballvy));}
				}
			}		

			ball.checkCollisions();

			double minDist = Double.MAX_VALUE, dist = 0;
			Ground closeGround = null;

			if (ball.getGrap() && grappleTarget == null){ // find closest ground
				for (Ground gr : grounds){
					double[] d = ball.getDxDy(gr);
					double d2 = Math.sqrt(d[0] * d[0] + d[1] * d[1]);

					if (d2 < minDist && gr.getType().equals("CIRC")){
						minDist = d2;
						closeGround = gr;
					}
				}
				grappleTarget = closeGround; 
			}

			if (grappleTarget != null){ // find distance from closest ground
				double[] d = ball.getDxDy(grappleTarget);

				dx = d[0];
				dy = d[1];

				if (grappleTarget.getType().equals("CIRC")){
					dist = (Math.sqrt(d[0] * d[0] + d[1] * d[1]) - Ball.BALL_RAD - grappleTarget.getRad());
				}
				else {
					dist = Math.sqrt(d[0] * d[0] + d[1] * d[1]);
				}
			}
			 // only then calculate distances - prevents multi-object and object switching when grappling
			ball.setGrapAvailable(!ball.isColl() && dist < GRAP_LEN && ball.getGrapMeter() > 0);
			if (ball.getGrap() && ball.grapAvailable() && !ball.grapOnCd()){
				double nx = dx;
				double ny = dy;
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

			g2d.setColor(Color.BLACK);
			g2d.drawOval((int)ball.getX() - Ball.BALL_RAD, (int)ball.getY() - Ball.BALL_RAD, Ball.BALL_RAD*2, Ball.BALL_RAD*2);

			for (Ground gr : grounds){
				if (gr.getType().equals("RECT")){
					g2d.setColor(Color.GRAY);
					g2d.fillRect((int)gr.getX(), (int)gr.getY(), gr.getW(), gr.getH());
				}
				if (gr.getType().equals("CIRC")){
					g2d.setColor(Color.RED);
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

			g2d.setColor(ballColor);
			g2d.fillOval((int)ball.getX() - Ball.BALL_RAD, (int)ball.getY() - Ball.BALL_RAD, Ball.BALL_RAD*2, Ball.BALL_RAD*2);
			
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
		}
	}

	public static void main(String[] args){
		new GrapplePlatformer();
	}
}

