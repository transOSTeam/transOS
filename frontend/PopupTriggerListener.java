package frontend;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

class PopupTriggerListener extends MouseAdapter{
	JPopupMenu popupMenu;
	public PopupTriggerListener(JPopupMenu popup){
		popupMenu = popup;
	}
	
	public void mousePressed(MouseEvent e){
		if(e.isPopupTrigger()){
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	public void mouseReleased(MouseEvent e){
		if(e.isPopupTrigger()){
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
