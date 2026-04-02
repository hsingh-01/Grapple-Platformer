package Class;
import Main.*;
import java.awt.event.*;

public class Ball{
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
		public static int BALL_RAD = 8;
		public static final double X_A = 0.02;

		private boolean grapOnCd = false;

		public static final int GRAP_LIMIT = 4800;
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
			for (Ground gr : GrapplePlatformer.grounds){
				if (gr.isCollideable()){
					if (gr.getType().equals("RECT")){
						double[] n = getDxDy(gr);
						double dx = n[0];
						double dy = n[1];

						if (Math.abs(dx) > Math.abs(dy)){
							n = new double[] {Math.signum(dx), 0};
						}
						else{
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
			}
			setColl(collFound);
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

		public void setGrapOnCooldown(boolean g){
			grapOnCd = g;
			if (g){
				GrapplePlatformer.MS_ELAPSED = 0;
			}
		}

		public boolean grapOnCd(){
			return grapOnCd;
		}
	} 