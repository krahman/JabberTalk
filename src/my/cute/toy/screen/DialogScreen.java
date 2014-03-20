package my.cute.toy.screen;

import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.BorderLayout;

public class DialogScreen extends Form {
	public DialogScreen(){
		setLayout(new BorderLayout());		
	}
	public void showDialog(String log){		
		addComponent(BorderLayout.CENTER, new Label(log));		
	}
}
