package frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class GuiStarter{
	JFrame frame = new JFrame("Transparent OS");
	JDesktopPane desktop = new JDesktopPane();
	JMenuBar menuBar = new JMenuBar();
	JMenu file = new JMenu("File");
	JMenu edit = new JMenu("Edit");
	JMenuItem newFile = new JMenuItem("New");
	JMenuItem exit = new JMenuItem("Exit");
	
	public GuiStarter(){
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		frame.add(desktop);
		addMenuBar();
		addMenuItems();
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
				System.exit(0);
			}
		});
		
		newFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//call text editor here
				desktop.add(new TextEditor());
			}
		});
	}
}
