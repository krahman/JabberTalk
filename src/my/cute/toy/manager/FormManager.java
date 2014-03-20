package my.cute.toy.manager;
import java.util.Stack;

import javax.microedition.lcdui.Display;

import com.sun.lwuit.Form;

public class FormManager {
	private Stack s; // memory stack
	private Form cf; // current form
	
	public FormManager() {		
		this.s = new Stack();
	}
	
	public void back() {
		if (this.s.size() > 0x00) {
			this.cf = (Form) this.s.pop();
			this.cf.show();
		}
	}

	public void next(Form next) {
		if (this.cf == next) {
			return;
		}

		if (this.cf != null) {
			this.s.push(this.cf);
		}
		this.cf = next;
		this.cf.show();
	}
}
