package my.cute.toy;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import my.cute.toy.manager.FormManager;
import my.cute.toy.screen.SplashScreen;



import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;

import com.javaeedev.midp.gtalk.jabber.Friend;
import com.javaeedev.midp.gtalk.jabber.JabberService;
import com.javaeedev.midp.gtalk.jabber.EventListener;

public class Toy extends MIDlet{
    private LoginScreen ls;
    private FormManager fm;
    public Toy() {
        // TODO Auto-generated constructor stub
    	this.fm = new FormManager();
    }

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // TODO Auto-generated method stub      
        notifyDestroyed();
    }

    protected void pauseApp() {
        // TODO Auto-generated method stub

    }

    protected void startApp() throws MIDletStateChangeException {
        // TODO Auto-generated method stub
        Display.init(this);
        

       /* Form splash = new Form(""); // no need for a title.
        splash.setScrollable(false);
        try {
            Image splashScreenImage = Image.createImage("/img/splash.png");
            //splash.getStyle().setBgImage(splashScreenImage);
        } catch (java.io.IOException e) {
            splash.getStyle().setBgColor(0x0000ff);
        }
        splash.show();*/
        // Setting the application theme is discussed
        // later in the theme chapter and the resources chapter
        try {
            Resources r = Resources.open("/LWUITtheme.res");
            UIManager.getInstance().setThemeProps(
                    r.getTheme(r.getThemeResourceNames()[0]));
        } catch (java.io.IOException e) {
        }
        SplashScreen splash = new SplashScreen(2000, this);
        splash.show();        
    }
    
    public void nextForm(Form nextform){
    	fm.next(nextform);
    }
    
    public void prevForm(){
    	fm.back();
    }

}
