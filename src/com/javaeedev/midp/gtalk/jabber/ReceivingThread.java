package com.javaeedev.midp.gtalk.jabber;

import java.io.IOException;
import java.io.Reader;

class ReceivingThread extends Thread {

    // if like: <start>
    static boolean isStartToken(String token, String name) {
        return token.length()>=name.length()+2 && token.substring(1, 1+name.length()).equals(name);
    }

    // if like: </end>
    static boolean isEndToken(String token, String name) {
        return token.length()>=name.length()+3 && token.charAt(1)=='/' && token.substring(2, 2 + name.length()).equals(name);
    }

    // if like: <abc/>
    static boolean isEmptyToken(String token, String name) {
        return token.length()>=name.length()+3 && token.charAt(token.length()-2)=='/' && token.substring(1, 1+name.length()).equals(name);
    }

    // read text from current to front of "<endToken>":
    static String blockReadText(Reader reader) throws IOException {
        StringBuffer sb = new StringBuffer(128);
        boolean gotLt = false;
        while(true) {
            int c = reader.read();
            if(c==(-1))
                break;
            char ch = (char)c;
            if(ch=='<') {
                // ok, got text, but should until read '>':
                gotLt = true;
            }
            else if(ch=='>') {
                return sb.toString();
            }
            else {
                if(!gotLt)
                    sb.append(ch);
            }
        }
        throw new IOException("Unexpected char.");
    }

    // read a token like <xxx>, </xxx> or <xxx/>:
    static String blockReadToken(Reader reader) throws IOException {
        StringBuffer sb = null;
        while(true) {
            int c = reader.read();
            if(c==(-1))
                break;
            char ch = (char)c;
            if(ch=='<') {
                sb = new StringBuffer(1024);
                sb.append(ch);
            }
            else if(ch=='>') {
                if(sb!=null) {
                    sb.append(ch);
                    Main.debug("[read token] " + sb.toString());
                    return sb.toString();
                }
                else
                    break;
            }
            else {
                if(sb!=null)
                    sb.append(ch);
            }
        }
        throw new IOException("Unexpected char.");
    }

    private volatile boolean running = false;

    private Reader reader;
    private JabberService service;

    public boolean isRunning() { return running; }

    public void shutdown() {
        running = false;
        this.interrupt();
    }

    // get element attribute like: <abc id="100">
    static String getAttribute(String token, String attrName) {
        int n0 = token.indexOf(" " + attrName + "=");
        if(n0==(-1))
            return null;
        int n1 = token.indexOf('\"', n0);
        if(n1==(-1))
            return null;
        int n2 = token.indexOf('\"', n1+1);
        if(n2==(-1))
            return null;
        Main.debug("got attr: " + token.substring(n1+1, n2));
        return token.substring(n1+1, n2);
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
        if(from==null)
            return null;
        int n = from.indexOf('/');
        if(n!=(-1))
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
            while(running) {
                String token = blockReadToken(reader);
                if(isStartToken(token, TOKEN_TYPE_IQ)) {
                    lastTokenType = TOKEN_TYPE_IQ;
                    lastType = getTypeAttribute(token);
                    lastFrom = null;
                    lastId = null;
                }
                else if(isStartToken(token, TOKEN_TYPE_MESSAGE)) {
                    lastTokenType = TOKEN_TYPE_MESSAGE;
                    lastType = getTypeAttribute(token);
                    lastFrom = getFromAttribute(token);
                    lastId = getAttribute(token, "id");
                }
                else if(isStartToken(token, TOKEN_TYPE_PRESENCE)) {
                    lastTokenType = TOKEN_TYPE_PRESENCE;
                    lastType = getTypeAttribute(token);
                    lastFrom = getFromAttribute(token);
                    lastId = null;
                    if(lastType==null) {
                        // user online:
                        service.getEventListener().friendStateChanged(lastFrom, Friend.NORMAL);
                    }
                    else if(TYPE_UNAVAILABLE.equals(lastType)) {
                        // user offline:
                        service.getEventListener().friendStateChanged(lastFrom, Friend.OFFLINE);
                    }
                }
                // handle other token *****************
                // handle <body>***</body>
                else if(TOKEN_TYPE_MESSAGE.equals(lastTokenType) && isStartToken(token, "body")) {
                    if(lastFrom!=null) {
                        String text = blockReadText(reader);
                        System.out.println("Got raw message: " + text);
                        if(TYPE_CHAT.equals(lastType)) {
                            // message received from a friend:
                            // decode:
                            int n = 0;
                            int len;
                            while(true) {
                                len = text.length();
                                n = text.indexOf("&gt;");
                                if(n==(-1))
                                    break;
                                text = text.substring(0, n) + ">" + text.substring(n+4, len);
                            }
                            while(true) {
                                len = text.length();
                                n = text.indexOf("&lt;");
                                if(n==(-1))
                                    break;
                                text = text.substring(0, n) + "<" + text.substring(n+4, len);
                            }
                            while(true) {
                                len = text.length();
                                n = text.indexOf("&amp;");
                                if(n==(-1))
                                    break;
                                text = text.substring(0, n) + "&" + text.substring(n+5, len);
                            }
                            service.getEventListener().messageReceived(lastFrom, text);
                        }
                        if(TYPE_ERROR.equals(lastType)) {
                            // sent-message is reject:
                            service.getEventListener().messageError(lastId);
                        }
                    }
                }
                // handle <item jid="***" subscription="" />
                else if(TOKEN_TYPE_IQ.equals(lastTokenType) && isStartToken(token, "item")) {
                    String jid = getAttribute(token, "jid");
                    if(jid!=null) {
                        String subscription = getAttribute(token, "subscription");
                        String name = getAttribute(token, "name");
                        if(name==null || name.equals(""))
                            name = jid;
                        if("both".equals(subscription)) {
                            // Load a friend!!!
                            System.out.println("Found friend: " + jid);
                            service.getEventListener().friendFound(jid, name);
                        }
                    }
                }
                // handle status changed <show>***</show>
                else if(TOKEN_TYPE_PRESENCE.equals(lastTokenType) && lastType==null && isStartToken(token, "show")) {
                    Main.debug("Found a token: show");
                    if(!isEmptyToken(token, "show")) {
                        String sh = blockReadText(reader);
                        Main.debug("Got show of user: " + lastFrom + " show: " + sh);
                        service.getEventListener().friendStateChanged(lastFrom, "dnd".equals(sh) ? Friend.BUSY : Friend.NORMAL);
                    }
                }
                // handle status changed <status>***</status>
                else if(TOKEN_TYPE_PRESENCE.equals(lastTokenType) && lastType==null && isStartToken(token, "status")) {
                    Main.debug("Found a token: status");
                    if(!isEmptyToken(token, "status")) {
                        String st = blockReadText(reader);
                        Main.debug("Got status of user: " + lastFrom + " is " + st);
                        service.getEventListener().friendStateChanged(lastFrom, "Away".equals(st) ? ("Busy".equals(st) ? Friend.BUSY : Friend.IDLE) : Friend.NORMAL);
                    }
                    else
                        Main.debug("Empty token: status.");
                }
            }
        }
        catch (IOException e) {
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
}