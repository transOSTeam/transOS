package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

public class GuiStarter {
	JFrame mainFrame = new JFrame("Transparent OS");
	JPanel mainPanel = new JPanel();
	JPanel contentPanelWest = new JPanel();
	JMenuBar mainMenuBar = new JMenuBar();
	JMenu mainFileMenu = new JMenu("File");
	JMenu newFileOrFolder = new JMenu("New");
	JMenuItem newFile = new JMenuItem("File");
	JMenuItem newFolder = new JMenuItem("Folder");
	JPopupMenu popupMenu = new JPopupMenu();
	JPopupMenu rightClickMenu = new JPopupMenu();
	JLabel[] lblArray = new JLabel[100]; 
	static int count = 0;
	
	public GuiStarter() {		
		mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
		mainFrame.setVisible(true);
		mainFrame.add(mainPanel);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(mainMenuBar, BorderLayout.NORTH);
		mainPanel.add(contentPanelWest,BorderLayout.WEST);
		mainPanel.addMouseListener(new PopupTriggerListener(popupMenu));
		addMenuItems();
		addPopupMenuItems();
		addRightClickMenuitems();
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
				createFolder(count++);
			}
		});
	}
	
	private void addPopupMenuItems(){
		JMenuItem item1 = new JMenuItem("New File");
		item1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				TextEditor txtEdit = new TextEditor(mainFrame);
				mainPanel.add(txtEdit);
			}
		});
		popupMenu.add(item1);
		JMenuItem item2 = new JMenuItem("New Folder");
		item2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				createFolder(count++);
			}
		});
		popupMenu.add(item2);
	}
	
	private void addRightClickMenuitems(){
		JMenuItem item1 = new JMenuItem("Delete");
		item1.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				e.getComponent().setVisible(false);
			}
			public void mouseReleased(MouseEvent e) {
			}
		});
		rightClickMenu.add(item1);
		
		JMenuItem item2 = new JMenuItem("Rename");
		item2.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				e.getComponent().setVisible(false);
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
	}
	
	private void createFolder(int count){
		//call a backend createfolder procedure here which returns a unique id for each folder
		
		ImageIcon icon = new ImageIcon("folder.gif");
		JLabel lbl = new JLabel(icon);
		JTextField txt = new JTextField("new folder" + count);
		
		txt.setEnabled(false);
		txt.setBackground(mainPanel.getBackground());
		txt.setDisabledTextColor(Color.BLACK);
		txt.setName("unique id");
		FolderNameEditListener listener = new FolderNameEditListener(mainPanel);
		txt.addMouseListener(listener);
		txt.addKeyListener(listener);
		
		lbl.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					FolderListing fldrpane = new FolderListing(mainFrame);
					mainPanel.add(fldrpane);
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger()){
					rightClickMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger()){
					rightClickMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		//lbl.setText("\n"+"new folder" + count);
		contentPanelWest.add(lbl);
		contentPanelWest.add(txt);
		contentPanelWest.revalidate();
	}
	
}
