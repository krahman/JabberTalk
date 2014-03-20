/**
 * Copyright 2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.javaeedev.midp.gtalk.jabber;

import java.io.*;
import java.util.Vector;
import javax.microedition.io.*;
//com/javaeedev/midp/gtalk/jabber/SendingThread
import com.javaeedev.midp.gtalk.jabber.*;

/**
 * Core background service that handles all network communications over ssl.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class JabberService {

    public static final String STATE_ONLINE  = "Online";
    public static final String STATE_OFFLINE = "Offline";
    public static final String STATE_BUSY    = "Busy";

    private static final String XML_START = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<stream:stream to=\"gmail.com\" version=\"1.0\" xmlns=\"jabber:client\" xmlns:stream=\"http://etherx.jabber.org/streams\">";
    private static final String XML_END = "</stream:stream>";

    private Thread connectingThread = null;
    private SendingThread sendingThread = null;
    private ReceivingThread receivingThread = null;

    private String username;
    private String password;

    private volatile String sessionId = null;

    private EventListener listener = null;

    private SecureConnection connection = null;
    private Reader reader = null;
    private Writer writer = null;
	private String s = "Available";

    public JabberService(EventListener listener) {
        this.listener = listener;
    }

    public EventListener getEventListener() {
        return listener;
    }

    /**
     * Send a message and return this message's ID for future check.
     * 
     * @param email Like "username@gmail.com".
     * @param message Text message.
     * @return Message id.
     */
    public String sendMessage(String email, String message) {
        String id = getNextId();
        String xml = "<message xmlns=\"jabber:client\" id=\"" + id
            + "\" type=\"chat\" to=\"" + email + "\"><body>"
            + encode(message) + "</body></message>";
        System.out.println("---------------------------------: "+xml);
        sendingThread.sendLater(xml);
        return id;
    }
    
    public String callFriend(String email, byte[] voice){
    	String id =  getNextId();
    	String xml ="" + id
    		+ "\" type=\"chat\" to=\"" + email + "\"><body>"
    		+ encode(voice)+ "</body></message>";
    	sendingThread.sendLater(xml);
    	return id;
    }
    
    private String encode(String message) {
        StringBuffer sb = new StringBuffer(message.length());
        for(int i=0; i<message.length(); i++) {
            char c = message.charAt(i);
            if(c=='<')
                sb.append("&lt;");
            else if(c=='>')
                sb.append("&gt;");
            else if(c=='&')
                sb.append("&amp;");
            else
                sb.append(c);
        }
        return sb.toString();
    }
    
    private ByteArrayInputStream encode(byte[] voice){
    	ByteArrayInputStream baisVoice = new ByteArrayInputStream(voice);
    	return baisVoice;
    }

    public String getRoster() {
        String id = getNextId();
        String xml = "<iq xmlns=\"jabber:client\" id=\"" + id
            + "\" type=\"get\"><query xmlns=\"jabber:iq:roster\"/></iq>";
        sendingThread.sendLater(xml);        
        return id;
    }

    public String setState(int state) {
        if(state==Friend.BUSY)
            s  = "Busy";
        else if(state==Friend.IDLE)
            s = "Idle";
        else if(state==Friend.OFFLINE)
        	s ="Offline";
        else if(state==Friend.NORMAL)
        	s ="Available";
        String id = getNextId();
        String xml = "<presence xmlns=\"jabber:client\" id=\"" + id
            + "\"><status>" + s + "</status></presence>";
        sendingThread.sendLater(xml);
        return id;
    }
    
    public String getState(){    	
    	return s; 
    }
    
    public void setStatus(String status){
    	String id = getNextId();
        String xml = "<presence xmlns=\"jabber:client\" id=\"" + id
            + "\"><status>" + status + "</status></presence>";
        sendingThread.sendLater(xml);
    }
    
    private String getLoginXml() {
        String xml = "<iq xmlns=\"jabber:client\" id=\"" + getNextId()
            + "\" type=\"set\"><query xmlns=\"jabber:iq:auth\"><username>"
            + username+ "</username><password>" + password
            + "</password><resource>Home</resource></query></iq>";
        return xml;
    }

    public void connect(String username, String password) {
        if(connection==null && connectingThread==null) {
            this.username = username;
            this.password = password;
            connectingThread = new ConnectingThread(this);
            connectingThread.start();
        }
        else {
            Main.debug("error: already connecting now.");
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public void shutdown() {
        Main.debug("shutdown now...");
        if(connectingThread!=null) {
            Main.debug("waiting for connecting thread...");
            connectingThread.interrupt();
            try {
                connectingThread.join();
            }
            catch (InterruptedException ie) {}
        }
        if(connection!=null) {
            Main.debug("try to close sending & receiving threads...");
            sendingThread.shutdown();
            receivingThread.shutdown();
            try {
                writer.write(XML_END);
                if(writer!=null) {
                    try { writer.close(); } catch (Exception e) {}
                }
                if(reader!=null) {
                    try { reader.close(); } catch (Exception e) {}
                }
                try { connection.close(); } catch(Exception e) {}
            }
            catch(Exception e) {
                Main.debug(e.getMessage());
            }
            connection = null;
            listener.connectionClosed();
        }
        Main.debug("Application end.");
    }

    private static int s_id = 100;
    static synchronized String getNextId() {
        s_id++;
        return "m_" + s_id;
    }

    class ConnectingThread extends Thread {

        private JabberService service;

        public ConnectingThread(JabberService service) {
            this.service = service;
        }
        public void run() {
            try {
                Main.debug("begin ssl...");
                connection = (SecureConnection)Connector.open("ssl://talk.google.com:5223");
                connection.setSocketOption(SocketConnection.LINGER, 5);
                Main.debug("socket connected.");
                reader = new InputStreamReader(connection.openInputStream(), "UTF-8");
                writer = new OutputStreamWriter(connection.openOutputStream(), "UTF-8");
                // send xml:
                writer.write(XML_START);
                writer.flush();
                // read server response:
                while(true) {
                    String token = ReceivingThread.blockReadToken(reader);
                    // TODO: debug:
                    Main.debug(token);
                    if(ReceivingThread.isStartToken(token, "stream:stream")) {
                        service.sessionId = ReceivingThread.getAttribute(token, "id");
                    }
                    if(ReceivingThread.isEndToken(token, "stream:features")) {
                        // start login:
                        SendingThread.blockWriteToken(writer, getLoginXml());
                        // read result:
                        String result = ReceivingThread.blockReadToken(reader);
                        if(ReceivingThread.isStartToken(result, "iq") && !"error".equals(ReceivingThread.getAttribute(result, "type"))) {
                            Main.debug("Login ok!");
                            break;
                        }
                        else {
                            connection.close();
                            connection = null;
                            listener.connectionError("Failed login.");
                            return;
                        }
                    }
                }
            }
            catch (Exception e) {
                Main.debug(e.getMessage());
                e.printStackTrace();
                connection = null;
                // notify connection error:
                listener.connectionError(e.getMessage());
                return;
            }
            //if(sessionId==null) {
            //    listener.connectionError("Not got session id.");
            //    return;
            //}
            // OK, now start sending & receiving thead:
            sendingThread = new SendingThread(writer, service);
            receivingThread = new ReceivingThread(reader, service);
            sendingThread.start();
            receivingThread.start();
            // notify that connection is established:
            listener.connectionEstablished();
            connectingThread = null;
            Main.debug("connecting thread ended normally.");
        }
    }
  
}

/*class SendingThread extends Thread {

    private volatile boolean running = false;

    private Writer writer;
    private Vector queue = new Vector();
    private JabberService service;

    public boolean isRunning() { return running; }

    public void setEventListener(JabberService service) {
        this.service = service;
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }

    public void sendNow(String xml) {
        Main.debug("sendNow: " + xml);
        synchronized(queue) {
            queue.insertElementAt(xml, 0);
            queue.notify();
        }
    }

    public void sendLater(String xml) {
        Main.debug("sendLater: " + xml);
        synchronized(queue) {
            queue.addElement(xml);
            queue.notify();
        }
    }

    static void blockWriteToken(Writer writer, String token) throws IOException {
        Main.debug("send: " + token);
        writer.write(token);
        writer.flush();
    }

    public void run() {
        Main.debug("sending thread start...");
        running = true;
        while(running) {
            String xml = null;
            synchronized(queue) {
                if(queue.size()==0) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ie) {
                        if(!running)
                            break;
                    }
                }
                xml = queue.firstElement().toString();
                queue.removeElementAt(0);
            }
            // send:
            try {
                blockWriteToken(writer, xml);
            } catch (IOException e) {
                Main.debug("[Sending Thread IOException]" + e.getMessage());
                service.getEventListener().connectionError("IOException");
                break;
            }
        }
        Main.debug("sending thread ended.");
    }

    public SendingThread(Writer writer, JabberService service) {
        this.writer = writer;
        this.service = service;
    }

}*/

/*class ReceivingThread extends Thread {

	// if like: <start>
	static boolean isStartToken(String token, String name) {
		return token.length() >= name.length() + 2
				&& token.substring(1, 1 + name.length()).equals(name);
	}

	// if like: </end>
	static boolean isEndToken(String token, String name) {
		return token.length() >= name.length() + 3 && token.charAt(1) == '/'
				&& token.substring(2, 2 + name.length()).equals(name);
	}

	// if like: <abc/>
	static boolean isEmptyToken(String token, String name) {
		return token.length() >= name.length() + 3
				&& token.charAt(token.length() - 2) == '/'
				&& token.substring(1, 1 + name.length()).equals(name);
	}

	// read text from current to front of "<endToken>":
	static String blockReadText(Reader reader) throws IOException {
		StringBuffer sb = new StringBuffer(128);
		boolean gotLt = false;
		while (true) {
			int c = reader.read();
			if (c == (-1))
				break;
			char ch = (char) c;
			if (ch == '<') {
				// ok, got text, but should until read '>':
				gotLt = true;
			} else if (ch == '>') {
				return sb.toString();
			} else {
				if (!gotLt)
					sb.append(ch);
			}
		}
		throw new IOException("Unexpected char.");
	}

	// read a token like <xxx>, </xxx> or <xxx/>:
	static String blockReadToken(Reader reader) throws IOException {
		StringBuffer sb = null;
		while (true) {
			int c = reader.read();
			if (c == (-1))
				break;
			char ch = (char) c;
			if (ch == '<') {
				sb = new StringBuffer(1024);
				sb.append(ch);
			} else if (ch == '>') {
				if (sb != null) {
					sb.append(ch);
					Main.debug("[read token] " + sb.toString());
					return sb.toString();
				} else
					break;
			} else {
				if (sb != null)
					sb.append(ch);
			}
		}
		throw new IOException("Unexpected char.");
	}

	private volatile boolean running = false;

	private Reader reader;
	private JabberService service;

	public boolean isRunning() {
		return running;
	}

	public void shutdown() {
		running = false;
		this.interrupt();
	}

	// get element attribute like: <abc id="100">
	static String getAttribute(String token, String attrName) {
		int n0 = token.indexOf(" " + attrName + "=");
		if (n0 == (-1))
			return null;
		int n1 = token.indexOf('\"', n0);
		if (n1 == (-1))
			return null;
		int n2 = token.indexOf('\"', n1 + 1);
		if (n2 == (-1))
			return null;
		Main.debug("got attr: " + token.substring(n1 + 1, n2));
		return token.substring(n1 + 1, n2);
	}

	static String getTypeAttribute(String token) {
		return getAttribute(token, "type");
	}

	public static final String TOKEN_TYPE_IQ = "iq";
	public static final String TOKEN_TYPE_MESSAGE = "message";
	public static final String TOKEN_TYPE_PRESENCE = "presence";

	public static final String TYPE_CHAT = "chat";
	public static final String TYPE_ERROR = "error";
	public static final String TYPE_UNAVAILABLE = "unavailable";

	private String getFromAttribute(String token) {
		String from = getAttribute(token, "from");
		if (from == null)
			return null;
		int n = from.indexOf('/');
		if (n != (-1))
			from = from.substring(0, n);
		return from;
	}

	public void run() {
		Main.debug("receving thread start...");
		running = true;
		try {
			String lastTokenType = null;
			String lastType = null;
			String lastFrom = null; // last message sent from who?
			String lastId = null; // last message id
			while (running) {
				String token = blockReadToken(reader);
				if (isStartToken(token, TOKEN_TYPE_IQ)) {
					lastTokenType = TOKEN_TYPE_IQ;
					lastType = getTypeAttribute(token);
					lastFrom = null;
					lastId = null;
				} else if (isStartToken(token, TOKEN_TYPE_MESSAGE)) {
					lastTokenType = TOKEN_TYPE_MESSAGE;
					lastType = getTypeAttribute(token);
					lastFrom = getFromAttribute(token);
					lastId = getAttribute(token, "id");
				} else if (isStartToken(token, TOKEN_TYPE_PRESENCE)) {
					lastTokenType = TOKEN_TYPE_PRESENCE;
					lastType = getTypeAttribute(token);
					lastFrom = getFromAttribute(token);
					lastId = null;
					if (lastType == null) {
						// user online:
						service.getEventListener().friendStateChanged(lastFrom,
								Friend.NORMAL);
					} else if (TYPE_UNAVAILABLE.equals(lastType)) {
						// user offline:
						service.getEventListener().friendStateChanged(lastFrom,
								Friend.OFFLINE);
					}
				}
				// handle other token *****************
				// handle <body>***</body>
				else if (TOKEN_TYPE_MESSAGE.equals(lastTokenType)
						&& isStartToken(token, "body")) {
					if (lastFrom != null) {
						String text = blockReadText(reader);
						System.out.println("Got raw message: " + text);
						if (TYPE_CHAT.equals(lastType)) {
							// message received from a friend:
							// decode:
							int n = 0;
							int len;
							while (true) {
								len = text.length();
								n = text.indexOf("&gt;");
								if (n == (-1))
									break;
								text = text.substring(0, n) + ">"
										+ text.substring(n + 4, len);
							}
							while (true) {
								len = text.length();
								n = text.indexOf("&lt;");
								if (n == (-1))
									break;
								text = text.substring(0, n) + "<"
										+ text.substring(n + 4, len);
							}
							while (true) {
								len = text.length();
								n = text.indexOf("&amp;");
								if (n == (-1))
									break;
								text = text.substring(0, n) + "&"
										+ text.substring(n + 5, len);
							}
							service.getEventListener().messageReceived(
									lastFrom, text);
						}
						if (TYPE_ERROR.equals(lastType)) {
							// sent-message is reject:
							service.getEventListener().messageError(lastId);
						}
					}
				}
				// handle <item jid="***" subscription="" />
				else if (TOKEN_TYPE_IQ.equals(lastTokenType)
						&& isStartToken(token, "item")) {
					String jid = getAttribute(token, "jid");
					if (jid != null) {
						String subscription = getAttribute(token,
								"subscription");
						String name = getAttribute(token, "name");
						if (name == null || name.equals(""))
							name = jid;
						if ("both".equals(subscription)) {
							// Load a friend!!!
							System.out.println("Found friend: " + jid);
							service.getEventListener().friendFound(jid, name);
						}
					}
				}
				// handle status changed <show>***</show>
				else if (TOKEN_TYPE_PRESENCE.equals(lastTokenType)
						&& lastType == null && isStartToken(token, "show")) {
					Main.debug("Found a token: show");
					if (!isEmptyToken(token, "show")) {
						String sh = blockReadText(reader);
						Main.debug("Got show of user: " + lastFrom + " show: "
								+ sh);
						service.getEventListener().friendStateChanged(lastFrom,
								"dnd".equals(sh) ? Friend.BUSY : Friend.NORMAL);
					}
				}
				// handle status changed <status>***</status>
				else if (TOKEN_TYPE_PRESENCE.equals(lastTokenType)
						&& lastType == null && isStartToken(token, "status")) {
					Main.debug("Found a token: status");
					if (!isEmptyToken(token, "status")) {
						String st = blockReadText(reader);
						Main.debug("Got status of user: " + lastFrom + " is "
								+ st);
						service
								.getEventListener()
								.friendStateChanged(
										lastFrom,
										"Away".equals(st) ? ("Busy".equals(st) ? Friend.BUSY
												: Friend.IDLE)
												: Friend.NORMAL);
					} else
						Main.debug("Empty token: status.");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			Main.debug("[Receiving Thread IOException]" + e.getMessage());
			service.getEventListener().connectionError("IOException");
		}
		Main.debug("receiving thread ended.");
	}

	public ReceivingThread(Reader reader, JabberService service) {
		this.reader = reader;
		this.service = service;
	}
}*/

/*class Main {

    public static void debug(String s) {
        System.out.println(s);
    }

}*/
