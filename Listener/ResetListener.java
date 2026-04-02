package Listener;
import Main.GrapplePlatformer;
import Class.Ball;
import Class.Ground;
import java.awt.event.*;

public class ResetListener implements ActionListener{
	public void actionPerformed(ActionEvent e){
		GrapplePlatformer.ball.setX(GrapplePlatformer.WIDTH/2);
		GrapplePlatformer.ball.setY(GrapplePlatformer.HEIGHT/2);
		GrapplePlatformer.ball.setDisplayX(0);
		GrapplePlatformer.ball.setDisplayY(0);
		GrapplePlatformer.ball.setVX(0);
		GrapplePlatformer.ball.setVY(0);

		GrapplePlatformer.ball.setGrapOnCooldown(false);
		GrapplePlatformer.ball.setGrapMeter(Ball.GRAP_LIMIT);

		GrapplePlatformer.grounds.clear();
		Ground startingPlatform = new Ground(25, GrapplePlatformer.ball.getX() - 25, GrapplePlatformer.ball.getY() + 25, "CIRC", false, true);
		GrapplePlatformer.grounds.add(startingPlatform);
		GrapplePlatformer.graphicsPanel.requestFocusInWindow();
	}
}