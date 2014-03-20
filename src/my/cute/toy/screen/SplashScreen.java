package my.cute.toy.screen;

import java.io.IOException;

import my.cute.toy.LoginScreen;
import my.cute.toy.Toy;

import com.sun.lwuit.Form;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.Painter;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.layouts.BorderLayout;


public class SplashScreen extends Form {
	private static final String imgpath = "/img/splash.png";
	private Toy nexian;

	public SplashScreen(final int interval, Toy n) {
		this.nexian = n;
		new Thread(){ 
			public void run(){
				setLayout(new BorderLayout());
				getStyle().setBgPainter(new ImagePainter());
				try {
					sleep(interval);					
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					nexian.nextForm(new LoginScreen("Sign In", nexian));
				}
			}
		}.start();	
	}

	public class ImagePainter implements Painter {
		private Image img;

		public void paint(Graphics g, Rectangle r) {
			g.setColor(0x1E90FF);
			g.fillRect(0, 0, getWidth(), getHeight());
			try {
				img = Image.createImage(imgpath);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			g.drawImage(img, (getWidth() - img.getWidth()) / 2,
					(getHeight() - img.getHeight()) / 2);
		}

	}
}
