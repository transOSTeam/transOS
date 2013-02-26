package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class GuiStarter {
	JFrame mainFrame = new JFrame("Transparent OS");
	JPanel mainPanel = new JPanel();
	JPanel contentPanelWest = new JPanel();
	JMenuBar mainMenuBar = new JMenuBar();
	JMenu mainFileMenu = new JMenu("File");
	JMenu newFileOrFolder = new JMenu("New");
	JMenuItem newFile = new JMenuItem("File");
	JMenuItem newFolder = new JMenuItem("Folder");
	JLabel[] lblArray = new JLabel[100]; 
	static int count = 0;
	
	public GuiStarter() {
		mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
		mainFrame.setVisible(true);
		mainFrame.add(mainPanel);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(mainMenuBar, BorderLayout.NORTH);
		mainPanel.add(contentPanelWest,BorderLayout.WEST);
		addMenuItems();
	}
	
	private void addMenuItems(){
		mainMenuBar.add(mainFileMenu);
		mainFileMenu.add(newFileOrFolder);
		newFileOrFolder.add(newFile);
		newFileOrFolder.add(newFolder);
		
		newFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mainPanel.add(new TextEditor(mainFrame));
			}
		});
		
		newFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CreateFolder(count++);
			}
		});
	}
	
	private void CreateFolder(int count){
		ImageIcon icon = new ImageIcon("folder.gif");
		lblArray[count] = new JLabel(icon);
		lblArray[count].setText("new folder" + count);
		contentPanelWest.add(lblArray[count]);
		contentPanelWest.revalidate();
	}
}
