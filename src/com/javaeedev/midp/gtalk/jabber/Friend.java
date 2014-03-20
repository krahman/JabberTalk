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

import java.io.IOException;

import com.sun.lwuit.Image;

/**
 * Represent a Friend object and hold name, email, status.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class Friend {

    public static final int OFFLINE = 0;
    public static final int NORMAL  = 1;
    public static final int BUSY    = 2;
    public static final int IDLE    = 3;

    private String email;
    private String name;
    private String displayName;
    private int state;
    private Image img;

    public Friend(String email, String name, int state) {
        this.email = email.toLowerCase();
        this.name = name;
        this.displayName = displayName;
        this.state = OFFLINE;
    }

    public String getEmail() { return email; }

    public int getState() { return state; }

    public void setState(int state) { this.state = state; }

    public boolean isOnline() { return state>OFFLINE; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }

    public boolean equals(Object obj) {
        if(obj==this) {
            return true;
        }
        if(obj instanceof Friend) {
            return ((Friend)obj).email.equals(email);
        }
        return false;
    }

    public int hashCode() { return email.hashCode(); }
    
    /**
	 * Edited by MALTech
	 * Started from this line 
	 */
	public void setImg(Image img) {
		this.img = img;
	}
	
	public Image getImg() {
		try {
			switch (getState()) {
			case OFFLINE:
				this.img = Image.createImage("/img/offline.png");
				break;
			case NORMAL:
				this.img = Image.createImage("/img/online.png");
				break;
			case BUSY:
				this.img = Image.createImage("/img/busy.png");
				break;
			case IDLE:
				this.img = Image.createImage("/img/idle.png");
				break;
			default:
				this.img = Image.createImage("/img/offline.png");
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return img;
	}
}
