package frontend;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.border.Border;

public class TextEditor extends JComponent{
	private static final long serialVersionUID = 1L;
	JDialog dialog ;
	JTextArea txtArea = new JTextArea();
	JMenuBar menuBar = new JMenuBar();
	JMenu fileMenu  = new JMenu("File");
	
	public TextEditor(JFrame parent) {
		dialog = new JDialog(parent, "TEdit", false);
		dialog.setLocationRelativeTo(parent);
		dialog.setBounds(50, 50, 700, 600);
		dialog.setJMenuBar(menuBar);
		menuBar.add(fileMenu);
		
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		txtArea.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		txtArea.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent arg0) {}
			public void keyReleased(KeyEvent arg0) {}
			public void keyPressed(KeyEvent e) {
				if(e.isControlDown() && e.getKeyChar() != 's' && e.getKeyCode() == 83){
					String textToSave = txtArea.getText();
					SaveFileDialog d = new SaveFileDialog(dialog, textToSave);
					dialog.add(d);
				}
			}
		});
		dialog.add(txtArea);	
		
		addMenuItems();
		dialog.setVisible(true);
	}
	
	private void addMenuItems(){
		JMenuItem item1 = new JMenuItem("New");
		fileMenu.add(item1);
		
		JMenuItem item2 = new JMenuItem("Open");
		item2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FolderListing openFileDialog = new FolderListing(dialog, "", 10);
				dialog.add(openFileDialog);
			}
		});
		fileMenu.add(item2);
		
		JMenuItem item3 = new JMenuItem("Save");
		item3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String textToSave = txtArea.getText();
				SaveFileDialog d = new SaveFileDialog(dialog, textToSave);
				dialog.add(d);
			}
		});
		fileMenu.add(item3);
	}
}
