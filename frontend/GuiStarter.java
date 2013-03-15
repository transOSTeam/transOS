package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.UIManager;

import sun.org.mozilla.javascript.internal.ast.KeywordLiteral;

public class GuiStarter {
	JFrame mainFrame = new JFrame("Transparent OS");
	JPanel mainPanel = new JPanel();
	JPanel contentPanelWest = new JPanel();
	JMenuBar mainMenuBar = new JMenuBar();
	JMenu mainFileMenu = new JMenu("File");
	JMenu newFileOrFolder = new JMenu("New");
	JMenuItem newFile = new JMenuItem("File");
	JMenuItem newFolder = new JMenuItem("Folder");
	JPopupMenu popupMenu = new JPopupMenu("test");
	JLabel[] lblArray = new JLabel[100]; 
	static int count = 0;
	
	class PopupTriggerListener extends MouseAdapter{
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
	
	class FolderNameEditListener extends MouseAdapter implements KeyListener{
		public void mouseClicked(MouseEvent e){
			e.getComponent().setEnabled(true);
			e.getComponent().setBackground(mainPanel.getBackground());
			e.getComponent().setForeground(Color.BLACK);
		}
	
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER){
				e.getComponent().setEnabled(false);
				e.getComponent().setBackground(mainPanel.getBackground());
				e.getComponent().setForeground(Color.BLACK);
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
	
	public GuiStarter() {		
		mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
		mainFrame.setVisible(true);
		mainFrame.add(mainPanel);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(mainMenuBar, BorderLayout.NORTH);
		mainPanel.add(contentPanelWest,BorderLayout.WEST);
		mainPanel.addMouseListener(new PopupTriggerListener());
		addMenuItems();
		addPopupMenuItems();
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
				mainPanel.add(new TextEditor(mainFrame));
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
	
	private void createFolder(int count){
		ImageIcon icon = new ImageIcon("folder.gif");
		JLabel lbl = new JLabel(icon);
		JTextField txt = new JTextField("new folder" + count);
		txt.setEnabled(false);
		txt.setBackground(mainPanel.getBackground());
		txt.setDisabledTextColor(Color.BLACK);
		FolderNameEditListener listener = new FolderNameEditListener();
		txt.addMouseListener(listener);
		txt.addKeyListener(listener);
		//lbl.setText("\n"+"new folder" + count);
		contentPanelWest.add(lbl);
		contentPanelWest.add(txt);
		contentPanelWest.revalidate();
	}
	
}
