package frontend;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JTextField;

class FolderNameEditListener extends MouseAdapter implements KeyListener{
	JPanel mainPanel;
	public FolderNameEditListener(JPanel p){
		mainPanel = p;
	}
	
	public void mouseClicked(MouseEvent e){
		e.getComponent().setEnabled(true);
		e.getComponent().setBackground(mainPanel.getBackground());
		e.getComponent().setForeground(Color.BLACK);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		JTextField txt;
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			e.getComponent().setEnabled(false);
			e.getComponent().setBackground(mainPanel.getBackground());
			e.getComponent().setForeground(Color.BLACK);
			
			txt = (JTextField)e.getSource();
			//call rename method here
			System.out.println(txt.getText() + txt.getName());
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
