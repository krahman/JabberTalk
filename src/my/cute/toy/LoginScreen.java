package my.cute.toy;

import javax.microedition.midlet.MIDletStateChangeException;

import my.cute.toy.screen.MainScreen;


import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;

public class LoginScreen extends Form implements ActionListener {
	private TextField ut, pt;
	private Button lb;
	private Command exit, login;
	private Toy nexian;

	public LoginScreen(String t, Toy n) {
		this.setTitle(t);
		this.nexian = n;
		initiateform();
	}

	private void initiateform() {
		setLayout(new BorderLayout());

		Container body = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		// username container
		Container uc = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		// password container
		Container pc = new Container(new BoxLayout(BoxLayout.Y_AXIS));

		Label ul = new Label("Username ");
		ul.getStyle().setBgTransparency(0);
		ut = new TextField("demo.nexian.a");

		Label pl = new Label("Password ");
		pl.getStyle().setBgTransparency(0);
		pt = new TextField("n3x1and3m0");
        pt.setConstraint(TextArea.PASSWORD);

		lb = new Button("Login");
		lb.setAlignment(CENTER);
		lb.addActionListener(this);

		uc.addComponent(ul);
		uc.addComponent(ut);
		pc.addComponent(pl);
		pc.addComponent(pt);

		body.addComponent(uc);
		body.addComponent(pc);
		body.addComponent(lb);

		exit = new Command("Exit");
		login = new Command("Login");
		addCommand(exit);
		addCommand(login);
		addCommandListener(this);
		addComponent(BorderLayout.CENTER, body);
	}

	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		if (event.getSource().equals(lb)) {
			System.out.println("User Login");
			nexian.nextForm(new MainScreen("Main Screen", nexian, ut.getText(),
					pt.getText()));
		} else if (event.getSource().equals(exit)) {
			try {
				nexian.destroyApp(true);				
				nexian.notifyDestroyed();
			} catch (MIDletStateChangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (event.getSource().equals(login)){
			System.out.println("User Login");
			nexian.nextForm(new MainScreen("Main Screen", nexian, ut.getText(),
					pt.getText()));
		}
	}

}
