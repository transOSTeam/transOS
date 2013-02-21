package frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;

public class TextEditor {
	JFrame frame = new JFrame("TEdit");
	JTextArea area =  new JTextArea();
	JMenuBar menuBar = new JMenuBar();
	JMenu file = new JMenu("File");
	JMenu edit = new JMenu("Edit");
	JMenuItem newFile = new JMenuItem("New");
	JMenuItem exit = new JMenuItem("Exit");
	
	public TextEditor() {
		frame.setBounds(50, 50, 500, 500);
		frame.setVisible(true);
		addMenuBar();
		addMenuItems();
		addTextArea();
	}
	
	private void addTextArea(){
		frame.add(area);		
	}
	
	private void addMenuBar(){
		frame.setJMenuBar(menuBar);
		
		menuBar.add(file);
		menuBar.add(edit);		
	}
	
	private void addMenuItems(){
		file.add(newFile);
		file.add(exit);
		
		//action listeners for menu items
		exit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
			}
		});
		
	}
}
