package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import backend.DirEntry;
import backend.Directory;
import backend.Inode;

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
	
	private HashMap<String, JComponent> componentMap = new HashMap<String, JComponent>();;
	private int rootInoneNum = 2;// get root inode number
	private Directory rootDir = new Directory(rootInoneNum);
	private static int count = 0;
	
	public GuiStarter() {		
		mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		mainFrame.setVisible(true);
		mainFrame.add(mainPanel);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(mainMenuBar, BorderLayout.NORTH);
		mainPanel.add(contentPanelWest,BorderLayout.WEST);
		mainPanel.addMouseListener(new PopupTriggerListener(popupMenu));
		addMenuItems();
		addPopupMenuItems();
		addRightClickMenuitems();
		showExistingFolderAndFiles();
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
	
	//to do later
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
	
	private void showExistingFolderAndFiles() {
		//get existing folder list from parentpath and id
		//HashMap<Integer, String> dirContent = rootDir.getDirContent();
		HashMap<Integer, DirEntry> dirContent = rootDir.getDirContent();
		//dirContent.put(10, new DirEntry("test",'d'));
		Iterator<Map.Entry<Integer, DirEntry>> it = dirContent.entrySet().iterator();
		
		JTextField txt;
		BufferedImage img = null;
		JButton lbl;
		
		while(it.hasNext()){
			Map.Entry<Integer, DirEntry> entry = it.next();
			
			DirEntry tempDirEntry = entry.getValue();
			if(tempDirEntry.getType() == 'd'){
				try {
					img = ImageIO.read(new File("folder.gif"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			else{
				try {
					img = ImageIO.read(new File("txt_file.gif"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
			lbl = new JButton(new ImageIcon(img));
			lbl.setBorder(BorderFactory.createEmptyBorder());
			lbl.setContentAreaFilled(false);
			String lblName = "lbl,d," + entry.getKey().toString();
			lbl.setName(lblName);
			componentMap.put(lblName, lbl);
			
			txt = new JTextField(entry.getValue().getName());			
			txt.setEnabled(false);
			txt.setBackground(mainPanel.getBackground());
			txt.setDisabledTextColor(Color.BLACK);
			String txtName = "txt,d," + entry.getKey().toString();
			txt.setName(txtName); //test
			FolderNameEditListener listener = new FolderNameEditListener(mainPanel);
			txt.addMouseListener(listener);
			txt.addKeyListener(listener);
			componentMap.put(txtName, txt);
			
			lbl.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2){
						String[] temp = e.getComponent().getName().split(",");
						JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
						FolderListing fldrpane = new FolderListing(mainFrame,tempTxt.getText(),10);
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
			lbl.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
				}
				public void focusGained(FocusEvent e) {
				}
			});
			
			contentPanelWest.add(lbl);
			contentPanelWest.add(txt);
			contentPanelWest.revalidate();
			
			it.remove();
		}
	}
	
	private void createFolder(int count){
		//call a backend createfolder procedure here which returns a unique id for each folder
		Inode dirInode = rootDir.makeDir("");
		
		BufferedImage img = null;
		JButton lbl;
		JTextField txt = new JTextField("new folder" + count);
		try {
			img = ImageIO.read(new File("folder.gif"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		lbl = new JButton(new ImageIcon(img));
		lbl.setBorder(BorderFactory.createEmptyBorder());
		lbl.setContentAreaFilled(false);
		
		txt.setEnabled(false);
		txt.setBackground(mainPanel.getBackground());
		txt.setDisabledTextColor(Color.BLACK);
		txt.setName("path|inode number");
		FolderNameEditListener listener = new FolderNameEditListener(mainPanel);
		txt.addMouseListener(listener);
		txt.addKeyListener(listener);
		
		lbl.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					FolderListing fldrpane = new FolderListing(mainFrame,"",10);
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
		
		lbl.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
			}
			public void focusGained(FocusEvent e) {
			}
		});
		//lbl.setText("\n"+"new folder" + count);
		contentPanelWest.add(lbl);
		contentPanelWest.add(txt);
		contentPanelWest.revalidate();
	}
	
	private void createComponentMap(){
		JComponent[] components = (JComponent[]) mainFrame.getContentPane().getComponents();
		for(int i = 0;i < components.length;i++){
			componentMap.put(components[i].getName(), components[i]);
		}
	}
	private JComponent getComponentByName(String componentName){
		if(componentMap.containsKey(componentName)){
			return (JComponent) componentMap.get(componentName);
		}
		else return null;
	}
	
}
