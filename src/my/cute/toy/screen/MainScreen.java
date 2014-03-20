package my.cute.toy.screen;

import java.util.Vector;

import my.cute.toy.Toy;


import com.javaeedev.midp.gtalk.jabber.EventListener;
import com.javaeedev.midp.gtalk.jabber.Friend;
import com.javaeedev.midp.gtalk.jabber.JabberService;
import com.sun.lwuit.ButtonGroup;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.RadioButton;
import com.sun.lwuit.TabbedPane;
import com.sun.lwuit.TextField;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.SelectionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.list.ListCellRenderer;

public class MainScreen extends Form implements ActionListener,
		ListCellRenderer, EventListener, SelectionListener {
	private Toy nexian;
	private TabbedPane tp; // tab panel
	private List lf; // list of friends;
	private Vector friends;
	private Command logout, audio, video, textmsg, write, setting;
	private JabberService jabberService;
	private boolean loggedin = false;
	private String err;
	private Friend[] friendsArray;
	private Container fc, sc, cc;
	private ButtonGroup bgState, bgProtocol, bgConn;
	private static final String[] STATUS = { "Offline", "Available", "Busy",
			"Idle" };
	private static final String[] CONNECTION = { "GPRS", "3G" };
	private static final String[] PROTOCOL = { "TCP", "UDP" };

	public MainScreen(String title, Toy n, String username, String password) {
		setTitle(title);
		this.nexian = n;
		friends = new Vector();
		doLogin(username, password);
		initiateform();
	}

	public void initiateform() {
		setLayout(new BorderLayout());
		setScrollable(true);
		// containers
		fc = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		sc = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		cc = new Container(new BoxLayout(BoxLayout.Y_AXIS));

		// friend tab components
		fillFriendTab();
		fillStatusTab();
		fillSettingTab();

		tp = new TabbedPane();
		tp.addTab("Friends", fc);
		tp.addTab("Status", sc);
		tp.addTab("Setting", cc);
		tp.addTabsListener(this);
		addComponent(BorderLayout.CENTER, tp);

		logout = new Command("Logout");
		audio = new Command("Audio Chat");
		video = new Command("Video Chat");
		textmsg = new Command("Text Chat");
		write = new Command("Write");
		setting = new Command("Save");

		addCommand(logout);
		addCommand(audio);
		addCommand(video);
		addCommand(textmsg);
		addCommandListener(this);
	}

	public void fillFriendTab() {
		lf = new List(friends);
		friendsArray = new Friend[friends.size()];
		FriendsRenderer fr = new FriendsRenderer();
		for (int i = 0; i < friends.size(); i++) {
			friendsArray[i] = (Friend) friends.elementAt(i);
			fr.getListCellRendererComponent(lf, friendsArray[i], 0, false);
		}
		lf.setListCellRenderer(fr);
		fc.addComponent(lf);
	}

	public void fillStatusTab() {

		bgState = new ButtonGroup();
		Label title = new Label("Set Status :");
		title.getStyle().setMargin(0, 0, 0, 0);
		title.getStyle().setBgTransparency(70);
		sc.addComponent(title);
		for (int i = 0; i < STATUS.length; i++) {
			RadioButton rb = new RadioButton(STATUS[i]);
			bgState.add(rb);
			sc.addComponent(rb);
		}
		bgState.setSelected(getCurrentStatus());
		Label status = new Label("What are you doing?");
		sc.addComponent(status);
		TextField txtStatus = new TextField("", TextField.ANY);
		sc.addComponent(txtStatus);
	}

	public void fillSettingTab() {
		// Connection type
		bgConn = new ButtonGroup();
		Label connLabel = new Label("Connection :");
		connLabel.getStyle().setMargin(0, 0, 0, 0);
		connLabel.getStyle().setBgTransparency(70);
		cc.addComponent(connLabel);
		for (int i = 0; i < CONNECTION.length; i++) {
			RadioButton rb = new RadioButton(CONNECTION[i]);
			bgConn.add(rb);
			cc.addComponent(rb);
		}
		bgConn.setSelected(getCurrentSetting(CONNECTION));

		// Protocol type
		bgProtocol = new ButtonGroup();
		Label protLabel = new Label("Protocol :");
		protLabel.getStyle().setMargin(0, 0, 0, 0);
		protLabel.getStyle().setBgTransparency(70);
		cc.addComponent(protLabel);
		for (int i = 0; i < PROTOCOL.length; i++) {
			RadioButton rb = new RadioButton(PROTOCOL[i]);
			bgProtocol.add(rb);
			cc.addComponent(rb);
		}
		bgProtocol.setSelected(getCurrentSetting(PROTOCOL));

		// Proxy option
		Label serverLabel = new Label("Audio/Video Proxy Server :");
		serverLabel.getStyle().setMargin(0, 0, 0, 0);
		serverLabel.getStyle().setBgTransparency(70);
		cc.addComponent(serverLabel);
		TextField txtServer = new TextField(TextField.DECIMAL);
		cc.addComponent(txtServer);
	}

	private int getCurrentStatus() {
		int state = 0;
		for (int i = 0; i < STATUS.length; i++) {
			if (jabberService.getState().equals(STATUS[i])) {
				state = i;
			}
		}
		return state;
	}

	private int getCurrentSetting(String[] param) {
		int setting = 0;
		for (int i = 0; i < param.length; i++) {
			// 0 should be a real type of setting index
			// in the real implementation
			if (i == 0)
				setting = i;
		}
		return setting;
	}

	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		if (event.getSource().equals(logout)) {
			System.out.println("Logout for user : " + lf.getSelectedIndex());
			jabberService.shutdown();
			nexian.prevForm();
		} else if (event.getSource().equals(audio)) {
			System.out.println("Audio for user : " + lf.getSelectedIndex());
			nexian.nextForm(new TextAudioChatScreen("Audio Chat", nexian,
					jabberService, getFriendEmail(lf.getSelectedIndex())));
		} else if (event.getSource().equals(video)) {
			System.out.println("Video for user : " + lf.getSelectedIndex());
			nexian.nextForm(new TextVideoChatScreen("Video Chat", nexian,
					jabberService, getFriendEmail(lf.getSelectedIndex())));
		} else if (event.getSource().equals(textmsg)) {
			System.out.println("Text for user : " + lf.getSelectedIndex());
			nexian.nextForm(new TextChatScreen("Text Chat", nexian,
					jabberService, getFriendEmail(lf.getSelectedIndex())));
		} else if (event.getSource().equals(write)) {
			System.out.println("Save");
			System.out.println(bgState.getSelectedIndex());
			jabberService.setState(bgState.getSelectedIndex());
			showDialog("Status has been changed.");
		} else if (event.getSource().equals(setting)) {
			System.out.println("Save");			
			showDialog("Not yet implemented.");			
		}
	}

	public void loading() {
		new Thread() {
			synchronized public void run() {
				if (loggedin) {
					initiateform();
				}
			}
		}.start();
	}

	public String getFriendEmail(int index) {
		Friend f = (Friend) friends.elementAt(index);
		return f.getEmail();
	}

	public void updateFriendList(String email, String name) {
		// TODO Auto-generated method stub

	}

	public void updateFriendStatus(String email, int state) {
		// TODO Auto-generated method stub
		for (int i = 0; i < friends.size(); i++) {
			Friend f = (Friend) friends.elementAt(i);
			if (f.getEmail().equals(email)) {
				if (f.getState() != state) {
					if (state > 0) {
						f.setState(state);
						friends.removeElementAt(i);
						friends.insertElementAt(f, 0);
					} else {
						f.setState(state);
						friends.removeElementAt(i);
						friends.addElement(f);
					}
				}
			}
		}
	}

	public void doLogin(String user, String password) {
		jabberService = new JabberService(this);
		jabberService.connect(user, password);
	}

	private void setChatMode() {
		new Thread() {
			public void run() {
				jabberService.getRoster();
				jabberService.setState(Friend.NORMAL);
			}
		}.start();
	}

	public void connectionClosed() {
		// TODO Auto-generated method stub
		System.out.println("connectionClosed.");
	}

	public void friendStateChanged(String email, int state) {
		// TODO Auto-generated method stub
		updateFriendStatus(email, state);
		System.out.println("Friend state change: " + email);
		refreshTheme();
	}

	public void connectionError(String err) {
		// TODO Auto-generated method stub
		// setErrorMode(err);
		loggedin = false;
		System.out.println("connectionError: " + err);
	}

	public void connectionEstablished() {
		// TODO Auto-generated method stub
		loggedin = true;
		setChatMode();
		System.out.println("connectionEstablished.");
	}

	public void friendFound(String email, String name) {
		// TODO Auto-generated method stub
		friends.addElement(new Friend(email, name, 0));
		System.out.println("Found friend: " + email);
		refreshTheme();
	}

	public void messageError(String id) {
		// TODO Auto-generated method stub

	}

	public void messageReceived(String email, String message) {
		// TODO Auto-generated method stub
		TextChatScreen.showMessage(email + ": " + message);
	}

	public Component getListCellRendererComponent(List arg0, Object arg1,
			int arg2, boolean arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	public Component getListFocusComponent(List arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Selection change listener implemented method for List item
	 */
	public void selectionChanged(int tab1, int tab2) {
		// TODO Auto-generated method stub
		System.out.println(tab1 +":"+tab2);
		if (!(tab1 == 0 && tab2 == 0 )) {
			if (tab1 == 1 && tab2==0) {
				addCommand(audio);
				addCommand(video);
				addCommand(textmsg);
				removeCommand(write);
			} else if (tab1==1 && tab2==2){
				removeCommand(audio);
				removeCommand(video);
				removeCommand(textmsg);
				removeCommand(write);
				addCommand(setting);
				System.out.println("Tab 3");
			} else {
				removeCommand(audio);
				removeCommand(video);
				removeCommand(textmsg);
				removeCommand(setting);
				addCommand(write);
			}
		}
	}

	public static void showDialog(String log) {
		Dialog.show("NexChat", log, null, null, 0, null, 2000,
				CommonTransitions.createSlide(CommonTransitions.SLIDE_VERTICAL,
						true, 0));
	}

	/**
	 * Scroll List implementation
	 */

	class FriendsRenderer extends Container implements ListCellRenderer {

		private Label name = new Label("");
		private Label email = new Label("");
		private Label image = new Label("");

		private Label focus = new Label("");

		public FriendsRenderer() {
			setLayout(new BorderLayout());
			addComponent(BorderLayout.WEST, image);
			Container cnt = new Container(new BoxLayout(BoxLayout.Y_AXIS));
			name.getStyle().setBgTransparency(0);
			email.getStyle().setBgTransparency(0);
			image.getStyle().setBgTransparency(0);
			cnt.addComponent(name);
			cnt.addComponent(email);
			addComponent(BorderLayout.CENTER, cnt);
			focus.setFocus(true);
		}

		public Component getListCellRendererComponent(List list, Object object,
				int index, boolean isSelected) {
			Friend friend = (Friend) object;
			name.setText(friend.getName());
			email.setText("");
			image.setIcon(friend.getImg());
			return this;
		}

		public Component getListFocusComponent(List list) {
			return focus;
		}

	}
}
