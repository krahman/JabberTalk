package my.cute.toy.screen;

import my.cute.toy.Toy;

import com.javaeedev.midp.gtalk.jabber.JabberService;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Font;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.GridLayout;

public class TextChatScreen extends Form implements ActionListener {
	private Toy nexian;
	private Command back, send;
	private JabberService jabberService;
	private String email;
	private static TextArea chatBoard;
	private TextField msgcolumn;
	private int height;
	private Container baseContainer;

	public TextChatScreen(String title, Toy n, JabberService jabberService,
			String email) {
		this.nexian = n;
		this.email = email;
		this.jabberService = jabberService;
		this.setTitle(title);
		setLayout(new BorderLayout());
		setScrollable(false);
		// screen width
		height = Display.getInstance().getDisplayHeight();
		System.out.println("height: -----------------------------" + height);

		// parent container
		baseContainer = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		// header of the page
		Container header = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		// body of the page
		Container body = new Container(new GridLayout(1,1));
		body.setHeight(height);
		// footer of the page
		Container footer = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		body.setScrollable(false);

		// header board
		Label chatwith = new Label("To " + email);
		header.setHeight(chatwith.getHeight());
		System.out.println("header: -----------------------------"
				+ chatwith.getHeight());
		header.addComponent(chatwith);

		// body of the board
		chatBoard = new TextArea(5,2, TextArea.ANY);
		chatBoard.setSingleLineTextArea(false);		
		chatBoard.setGrowByContent(false);	
		chatBoard.setEditable(false);
		chatBoard.setIsScrollVisible(true);		
		//chatBoard.setFocusable(false);
		
		body.addComponent(chatBoard);
		body.setHeight(height - (2 * header.getHeight()));
		System.out.println("body: -----------------------------"
				+ (height - (2 * header.getHeight())));
		
		// footer of the board
		msgcolumn = new TextField();
		footer.addComponent(msgcolumn);
		footer.setHeight(height - (header.getHeight() + body.getHeight()));
		System.out.println("footer: -----------------------------"
				+ (height - (header.getHeight() + body.getHeight())));
		
		baseContainer.addComponent(header);		
		baseContainer.addComponent(body);
		baseContainer.addComponent(footer);
		
		addComponent(BorderLayout.CENTER, baseContainer);

		this.back = new Command("Back");
		this.send = new Command("Send");
		addCommand(back);
		addCommand(send);
		addCommandListener(this);
		msgcolumn.requestFocus();
	}

	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		String message = "";
		if (event.getSource().equals(back)) {
			nexian.prevForm();
		} else if (event.getSource().equals(send)) {
			message = msgcolumn.getText();
			jabberService.sendMessage(email, message);
			showMessage("me: " + message);
			msgcolumn.clear();
		}
	}

	public static void showMessage(String message) {
		if (chatBoard.getText().equals("") || chatBoard.getText() == null) {
			chatBoard.setText(message);			
		} else {
			chatBoard.setText(chatBoard.getText() + "\n" + message);
			chatBoard.refreshTheme();
		}
	}	

}
