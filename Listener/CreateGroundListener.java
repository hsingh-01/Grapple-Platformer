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
				int gx = GrapplePlatformer.WIDTH/2 + 40 + 1300 * i;
				int gy = GrapplePlatformer.HEIGHT + 500 * j;
				int gw = 250;
				int gh = 2500;

				Ground g = new Ground(15, gx + gw/2, gy, "CIRC", true, false);
				GrapplePlatformer.grounds.add(g);
				Ground g_rect = new Ground(gx, gy, 250, 2500, "RECT", false, true);
				GrapplePlatformer.grounds.add(g_rect);

				// System.out.println(floor.isCollideable());
			}
		}
		// Ground floor = new Ground(0, GrapplePlatformer.HEIGHT, 8000, 50, "RECT", true, true);
		// GrapplePlatformer.grounds.add(floor);
		GrapplePlatformer.graphicsPanel.requestFocusInWindow();
	}
}	