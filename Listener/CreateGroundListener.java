package Listener;
import Main.GrapplePlatformer;
import Class.Ground;
import java.awt.event.*;
import java.util.ArrayList;


public class CreateGroundListener implements ActionListener {
	public void actionPerformed(ActionEvent e){
		GrapplePlatformer.grounds.clear();
		for (int i = 0; i < 50; i++){
			for (int j = 0; j < 1; j++){
				int gx = GrapplePlatformer.WIDTH/2 + 40 + 450 * i;
				int gy = GrapplePlatformer.HEIGHT/2 + 500 * j;
				Ground g = new Ground(25, gx, gy, "CIRC", true, true);
				GrapplePlatformer.grounds.add(g);
				Ground g_rect = new Ground(gx, gy, 50, 250, "RECT", false, true);
				GrapplePlatformer.grounds.add(g_rect);

				// System.out.println(floor.isCollideable());
			}
		}
		Ground floor = new Ground(0, GrapplePlatformer.HEIGHT, 8000, 50, "RECT", true, true);
		GrapplePlatformer.grounds.add(floor);
		GrapplePlatformer.graphicsPanel.requestFocusInWindow();
	}
}	