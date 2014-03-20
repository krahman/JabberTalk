package my.cute.toy.screen;

import my.cute.toy.Toy;

import com.javaeedev.midp.gtalk.jabber.JabberService;
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
import com.sun.lwuit.layouts.GridLayout;

public class TextAudioChatScreen extends Form implements ActionListener{
	private Toy nexian;
	private Command back, send, stop, start, record;
	private JabberService jabberService;
	private String email;
	private String msg;
	private static TextArea chatBoard;
	private TextField msgcolumn;
	private Label chatwith;
	// Audio recording fields below	
	private Container baseContainer;
	
	public TextAudioChatScreen(String title, Toy n, JabberService jabberService, String email) {
		// TODO Auto-generated constructor stub
		setTitle(title);
		this.nexian = n;
		this.jabberService = jabberService;
		this.email = email;
		setLayout(new BorderLayout());
		// parent container
		baseContainer = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		// header of the page
		Container header = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		// body of the page
		Container body = new Container(new GridLayout(1,1));
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
		
		// footer of the board
		msgcolumn = new TextField();
		footer.addComponent(msgcolumn);
		
		baseContainer.addComponent(header);		
		baseContainer.addComponent(body);
		baseContainer.addComponent(footer);
		
		addComponent(BorderLayout.CENTER, baseContainer);
		
		this.back = new Command("Back");
		this.send = new Command("Send");
		this.stop = new Command("Stop Audio");
		this.start = new Command("Start");
		this.record = new Command("Record");
		
		addCommand(back);		
		addCommand(send);		
		addCommand(start);
		
		addCommandListener(this);
		msgcolumn.requestFocus();
	}

	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		String message ="";
		if (event.getSource().equals(back)){
			nexian.prevForm();
		} else if (event.getSource().equals(send)){
			message = msgcolumn.getText();
			jabberService.sendMessage(email, msg);	
			showMessage("me: " + message);			
			msgcolumn.clear();
		} else if (event.getSource().equals(stop)){
			removeCommand(stop);
			addCommand(start);
			handleStop();
			System.out.println("Stop Audio.");
		} else if (event.getSource().equals(start)){
			removeCommand(start);
			addCommand(record);
			handleStart();
			System.out.println("Start Audio.");
		} else if (event.getSource().equals(record)){
			removeCommand(record);
			addCommand(stop);
			handleRecord();
			System.out.println("Record Audio.");
		}
	}

	public static void showMessage(String message) {
		if (chatBoard.getText().equals("") || chatBoard.getText() == null) {
			chatBoard.setText(message);
		} else {
			chatBoard.setText(chatBoard.getText() + "\n" + message);
		}
	}
	
	// Audio recording
	private void handleStop() {
		//alert.removeCommand(stopCommand);
		System.out.println("handling Stop");		
	}
	
	private void handleRecord() {
		//alert.setString("START recording...");
		//alert.removeCommand(recordCommand);
		//alert.addCommand(stopCommand);
		System.out.println("handling Record");
		recordAndSend();
	}
	
	private void handleStart() {
		//alert.removeCommand(startCommand);
		new Thread() {
			public void run() {
				startNetworking();
				startRecorderPlayer();
				startReaderWriter();
			}
		}.start();
	}
	
	private void recordAndSend() {
	}
	
	private void startRecorderPlayer() {		
	}
	
	private void startNetworking() {		
	}
	
	private void startReaderWriter() {
		
	}
}
