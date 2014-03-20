package com.javaeedev.midp.gtalk.jabber;

import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

public class SendingThread extends Thread {

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

}
