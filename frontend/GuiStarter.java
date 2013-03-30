package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import backend.disk.DirEntry;
import backend.disk.Directory;
import backend.disk.Disk;
import backend.disk.Inode;

public class GuiStarter {
	JFrame mainFrame;
	JPanel mainPanel;
	JPanel contentPanelWest;
	JMenuBar mainMenuBar;
	JMenu mainFileMenu;
	JMenu newFileOrFolder;
	JMenuItem newFile;
	JMenuItem newFolder;
	JPopupMenu popupMenu;
	JPopupMenu rightClickMenu;
	JButton rightClickedLbl;

	private Map<String, JComponent> componentMap;
	private Map<String, JPanel> colPanelMap;
	private static int rootInodeNum = 2;// get root inode number
	private Directory rootDir;
	private HashMap<Integer, DirEntry> dirContent;
	private int count = 0;
	private int count1 = 0;
	private int pnlCount;
	
	static int copiedInodeNum = 0;
	static int copyFrom = 0;
	
	public GuiStarter() {
		mainFrame = new JFrame("Transparent OS");
		mainPanel = new JPanel();
		contentPanelWest = new JPanel();
		mainMenuBar = new JMenuBar();
		mainFileMenu = new JMenu("File");
		newFileOrFolder = new JMenu("New");
		newFile = new JMenuItem("File");
		newFolder = new JMenuItem("Folder");
		popupMenu = new JPopupMenu();
		rightClickMenu = new JPopupMenu();
		
		componentMap = new HashMap<String, JComponent>();
		colPanelMap = new HashMap<String, JPanel>();
		pnlCount = 0;
		rootDir = new Directory(rootInodeNum);
		dirContent = rootDir.getDirContent();
		
		mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		mainFrame.setVisible(true);
		mainFrame.add(mainPanel);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowClosing(WindowEvent arg0) {
				Disk.shutDown();
			}
			public void windowClosed(WindowEvent arg0) {}
			public void windowActivated(WindowEvent arg0) {}
		});
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(mainMenuBar, BorderLayout.NORTH);
		mainPanel.add(contentPanelWest,BorderLayout.WEST);
		mainPanel.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger()){
					if(GuiStarter.copiedInodeNum != 0){
						JMenuItem item = (JMenuItem)popupMenu.getComponent(2);
						item.setEnabled(true);
					}
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger()){
					if(GuiStarter.copiedInodeNum != 0){
						JMenuItem item = (JMenuItem)popupMenu.getComponent(2);
						item.setEnabled(true);
					}
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent e) {}
		});
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
				createFile(count1++);
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
				createFile(count1++);
			}
		});
		
		JMenuItem item2 = new JMenuItem("New Folder");
		item2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				createFolder(count++);
			}
		});
		
		JMenuItem item3  = new JMenuItem("Paste");
		item3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(GuiStarter.copyFrom != rootInodeNum){
					rootDir.copy(copiedInodeNum, rootInodeNum);
					rootDir = null;
					rootDir = new Directory(rootInodeNum);
					dirContent = rootDir.getDirContent();
					contentPanelWest.removeAll();
					contentPanelWest.revalidate();
					showExistingFolderAndFiles();
				}
			}
		});
		
		JMenuItem item4 = new JMenuItem("Refresh");
		item4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				rootDir = null;
				rootDir = new Directory(rootInodeNum);
				dirContent = rootDir.getDirContent();
				contentPanelWest.removeAll();
				contentPanelWest.revalidate();
				showExistingFolderAndFiles();
			}
		});
		
		popupMenu.add(item1);
		popupMenu.add(item2);
		popupMenu.add(item3);
		popupMenu.add(item4);
		
		item3.setEnabled(false);
	}
	
	private void addRightClickMenuitems(){
		JMenuItem item1 = new JMenuItem("Delete");
		item1.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				String[] temp = rightClickedLbl.getName().split(",");
				JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
				rootDir.deleteFile(Integer.parseInt(temp[2]));
				JPanel pnl = (JPanel)rightClickedLbl.getParent();
				pnl.remove(rightClickedLbl);
				pnl.remove(tempTxt);
				JPanel colPan = (JPanel)pnl.getParent();
				colPan.remove(pnl);
				contentPanelWest.revalidate();
			}
			public void mouseReleased(MouseEvent e) {
			}
		});
		
		JMenuItem item2 = new JMenuItem("Rename");
		item2.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				String[] temp = rightClickedLbl.getName().split(",");
				JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
				tempTxt.setEnabled(true);
				tempTxt.setForeground(Color.BLUE);
				tempTxt.setBackground(mainPanel.getBackground());
			}
			public void mouseReleased(MouseEvent e) {}
		});
		
		JMenuItem item3  = new JMenuItem("Properties");
		item3.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				String[] temp = rightClickedLbl.getName().split(",");
				JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
				Properties prop = new Properties(mainFrame, Integer.parseInt(temp[2]), tempTxt.getText());
				mainPanel.add(prop);
			}
			public void mouseReleased(MouseEvent e) {}
		});
		
		JMenuItem item4 = new JMenuItem("Copy");
		item4.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				String[] temp = rightClickedLbl.getName().split(",");
				GuiStarter.copiedInodeNum = Integer.parseInt(temp[2]);
				GuiStarter.copyFrom = 2;
			}
			public void mouseReleased(MouseEvent e) {
			}
		});
		
		JMenuItem item5 = new JMenuItem("Cut");
		item5.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				String[] temp = rightClickedLbl.getName().split(",");
				rightClickedLbl.setIcon(new ImageIcon("folder_light.gif"));
				JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
				tempTxt.setBackground(Color.LIGHT_GRAY);
				GuiStarter.copiedInodeNum = Integer.parseInt(temp[2]);
				GuiStarter.copyFrom = 2;
			}
			public void mouseReleased(MouseEvent e) {
			}
		});
		
		rightClickMenu.add(item4);
		rightClickMenu.add(item5);
		rightClickMenu.add(item1);
		rightClickMenu.add(item2);
		rightClickMenu.add(item3);
	}
	
	private void showExistingFolderAndFiles() {
		Iterator<Map.Entry<Integer, DirEntry>> it = dirContent.entrySet().iterator();
		
		JTextField txt;
		BufferedImage img = null;
		JButton lbl;
		JPanel pnl = null;
		JPanel columnPanel = null;
		columnPanel = new JPanel();
		columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
		columnPanel.setName("col" + pnlCount);
		colPanelMap.put("col" + pnlCount, columnPanel);
		while(it.hasNext()){
			
			pnl = new JPanel();
			pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
			pnl.setBorder(new EmptyBorder(10,10,10,10));
			
			Map.Entry<Integer, DirEntry> entry = it.next();
			
			DirEntry tempDirEntry = entry.getValue();
			if(tempDirEntry.getType() == 'd'){
				try {
					img = ImageIO.read(new File("folder.gif"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			else if(tempDirEntry.getType() == 'r'){
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
			if(tempDirEntry.getType() == 'r'){
				lblName = "lbl,r," + entry.getKey().toString();
			}
			lbl.setName(lblName);
			lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
			componentMap.put(lblName, lbl);
			
			lbl.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2){
						String[] temp = e.getComponent().getName().split(",");
						JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
						if(temp[1].equals("d")){
							FolderListing fldrpane = new FolderListing(mainFrame,"/" + tempTxt.getText(),Integer.parseInt(temp[2]));
							mainPanel.add(fldrpane);
						}
						else if(temp[1].equals("r")){
							int inodeNum = Integer.parseInt(temp[2]);
							/*Inode tempInode = new Inode(inodeNum);							
							String fileContent = tempInode.getFileContent();
							TextEditor txtEdit = new TextEditor(mainFrame, fileContent, rootDir, inodeNum);
							mainPanel.add(txtEdit);*/
							rootDir.editFile(inodeNum);
						}
					}
				}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {
					if(e.isPopupTrigger()){
						rightClickedLbl = (JButton)e.getComponent();
						rightClickMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}
				public void mouseReleased(MouseEvent e) {
					if(e.isPopupTrigger()){
						rightClickedLbl = (JButton)e.getComponent();
						rightClickMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
			lbl.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					JButton tempLbl = (JButton)e.getSource();
					JPanel tempPanel = (JPanel) tempLbl.getParent();
					tempPanel.setBackground(null);
					String[] temp = tempLbl.getName().split(",");
					JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
					tempTxt.setBackground(null);
				}
				public void focusGained(FocusEvent e) {
					JButton tempLbl = (JButton)e.getSource();
					JPanel tempPanel = (JPanel) tempLbl.getParent();
					tempPanel.setBackground(Color.LIGHT_GRAY);
					String[] temp = tempLbl.getName().split(",");
					JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
					tempTxt.setBackground(Color.LIGHT_GRAY);
				}
			});
			
			txt = new JTextField(entry.getValue().getName());			
			txt.setEnabled(false);
			txt.setBackground(mainPanel.getBackground());
			txt.setDisabledTextColor(Color.BLACK);
			txt.setBorder(null);
			String txtName = "txt,d," + entry.getKey().toString();
			if(tempDirEntry.getType() == 'r'){
				txtName = "txt,r," + entry.getKey().toString();
			}
			txt.setName(txtName);
			txt.setAlignmentX(Component.CENTER_ALIGNMENT);
			componentMap.put(txtName, txt);
			
			txt.addMouseListener(new MouseListener() {
				public void mouseReleased(MouseEvent arg0) {}
				public void mousePressed(MouseEvent arg0) {}
				public void mouseExited(MouseEvent e) {
					e.getComponent().setForeground(Color.BLACK);
				}
				public void mouseEntered(MouseEvent arg0) {}
				public void mouseClicked(MouseEvent e) {
					e.getComponent().setEnabled(true);
					e.getComponent().setBackground(mainPanel.getBackground());
					e.getComponent().setForeground(Color.BLUE);
				}
			});
			txt.addKeyListener(new KeyListener() {
				public void keyTyped(KeyEvent e) {}
				public void keyReleased(KeyEvent e) {}
				public void keyPressed(KeyEvent e) {
					JTextField txt;
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						e.getComponent().setEnabled(false);
						e.getComponent().setBackground(mainPanel.getBackground());
						e.getComponent().setForeground(Color.BLACK);
						
						txt = (JTextField)e.getSource();
						
						String[] temp = txt.getName().split(",");
						rootDir.renameFile(Integer.parseInt(temp[2]), txt.getText());						
					}
				}
			});
			
			pnl.add(lbl);
			pnl.add(txt);
			columnPanel.add(pnl);
			contentPanelWest.add(columnPanel);
			contentPanelWest.revalidate();
			if(columnPanel.getComponentCount() == 9){
				columnPanel = new JPanel();
				columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
				pnlCount++;
				columnPanel.setName("col" + pnlCount);
				colPanelMap.put("col" + pnlCount, columnPanel);
			}
		}
	}
	
	private void createFolder(int count){
		BufferedImage img = null;
		JButton lbl;
		String folderName = "new folder " + count;
		
		JPanel columnPanel = getPanelByName("col" + pnlCount);
		if(columnPanel.getComponentCount() == 9){
			columnPanel = new JPanel();
			columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
			pnlCount++;
			columnPanel.setName("col" + pnlCount);
			colPanelMap.put("col" + pnlCount, columnPanel);
		}
		
		JPanel pnl = null;
		pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.setBorder(new EmptyBorder(10,10,10,10));
		
		while(getNewFolderName(folderName).equals("")){
			folderName = getNewFolderName("new folder " + count++);
		}
		
		Inode dirInode = rootDir.makeDir(folderName);//problem here
		JTextField txt = new JTextField(folderName);
		
		try {
			img = ImageIO.read(new File("folder.gif"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		lbl = new JButton(new ImageIcon(img));
		lbl.setBorder(BorderFactory.createEmptyBorder());
		lbl.setContentAreaFilled(false);
		String lblName = "lbl,d," + dirInode.getInodeNum();
		lbl.setName(lblName);
		lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		componentMap.put(lblName, lbl);		
		lbl.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					String[] temp = e.getComponent().getName().split(",");
					JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
					if(temp[1].equals("d")){
						FolderListing fldrpane = new FolderListing(mainFrame,"/" + tempTxt.getText(),Integer.parseInt(temp[2]));
						mainPanel.add(fldrpane);
					}
					else if(temp[1].equals("d")){
						TextEditor txtEdit = new TextEditor(mainFrame);
						mainPanel.add(txtEdit);
					}
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger()){
					rightClickedLbl = (JButton)e.getComponent();
					rightClickMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger()){
					rightClickedLbl = (JButton)e.getComponent();
					rightClickMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});		
		lbl.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				JButton tempLbl = (JButton)e.getSource();
				JPanel tempPanel = (JPanel) tempLbl.getParent();
				tempPanel.setBackground(null);
				String[] temp = tempLbl.getName().split(",");
				JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
				tempTxt.setBackground(null);
			}
			public void focusGained(FocusEvent e) {
				JButton tempLbl = (JButton)e.getSource();
				JPanel tempPanel = (JPanel) tempLbl.getParent();
				tempPanel.setBackground(Color.LIGHT_GRAY);
				String[] temp = tempLbl.getName().split(",");
				JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
				tempTxt.setBackground(Color.LIGHT_GRAY);
			}
		});
		
		
		txt.setEnabled(false);
		txt.setBackground(mainPanel.getBackground());
		txt.setDisabledTextColor(Color.BLACK);
		txt.setBorder(null);
		String txtName = "txt,d," + dirInode.getInodeNum();
		txt.setName(txtName);
		txt.setAlignmentX(Component.CENTER_ALIGNMENT);
		componentMap.put(txtName, txt);
		
		txt.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseExited(MouseEvent e) {
				e.getComponent().setForeground(Color.BLACK);
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent e) {
				e.getComponent().setEnabled(true);
				e.getComponent().setBackground(mainPanel.getBackground());
				e.getComponent().setForeground(Color.BLUE);
			}
		});
		txt.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyPressed(KeyEvent e) {
				JTextField txt;
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					e.getComponent().setEnabled(false);
					e.getComponent().setBackground(mainPanel.getBackground());
					e.getComponent().setForeground(Color.BLACK);
					
					txt = (JTextField)e.getSource();
					
					String[] temp = txt.getName().split(",");
					rootDir.renameFile(Integer.parseInt(temp[2]), txt.getText());						
				}
			}
		});
		
		pnl.add(lbl);
		pnl.add(txt);
		columnPanel.add(pnl);
		contentPanelWest.add(columnPanel);
		contentPanelWest.revalidate();
		
	}
	
	private void createFile(int count){
		BufferedImage img = null;
		JButton lbl;
		String fileName = "new file " + count;
		
		JPanel columnPanel = getPanelByName("col" + pnlCount);
		if(columnPanel.getComponentCount() == 9){
			columnPanel = new JPanel();
			columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
			pnlCount++;
			columnPanel.setName("col" + pnlCount);
			colPanelMap.put("col" + pnlCount, columnPanel);
		}
		
		JPanel pnl = null;
		pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.setBorder(new EmptyBorder(10,10,10,10));
		
		while(getNewFileName(fileName).equals("")){
			fileName = getNewFileName("new file " + count++);
		}
		
		Inode dirInode = rootDir.makeFile(fileName, "");
		JTextField txt = new JTextField(fileName);
		
		try {
			img = ImageIO.read(new File("txt_file.gif"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		lbl = new JButton(new ImageIcon(img));
		lbl.setBorder(BorderFactory.createEmptyBorder());
		lbl.setContentAreaFilled(false);
		String lblName = "lbl,r," + dirInode.getInodeNum();
		lbl.setName(lblName);
		lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		componentMap.put(lblName, lbl);		
		lbl.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					String[] temp = e.getComponent().getName().split(",");
					JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
					if(temp[1].equals("d")){
						FolderListing fldrpane = new FolderListing(mainFrame,"/" + tempTxt.getText(),Integer.parseInt(temp[2]));
						mainPanel.add(fldrpane);
					}
					else if(temp[1].equals("r")){
						int inodeNum = Integer.parseInt(temp[2]);
						/*Inode tempInode = new Inode(inodeNum);							
						String fileContent = tempInode.getFileContent();
						TextEditor txtEdit = new TextEditor(mainFrame, fileContent, rootDir, inodeNum);
						mainPanel.add(txtEdit);*/
						rootDir.editFile(inodeNum);
					}
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger()){
					rightClickedLbl = (JButton)e.getComponent();
					rightClickMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger()){
					rightClickedLbl = (JButton)e.getComponent();
					rightClickMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});		
		lbl.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				JButton tempLbl = (JButton)e.getSource();
				JPanel tempPanel = (JPanel) tempLbl.getParent();
				tempPanel.setBackground(null);
				String[] temp = tempLbl.getName().split(",");
				JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
				tempTxt.setBackground(null);
			}
			public void focusGained(FocusEvent e) {
				JButton tempLbl = (JButton)e.getSource();
				JPanel tempPanel = (JPanel) tempLbl.getParent();
				tempPanel.setBackground(Color.LIGHT_GRAY);
				String[] temp = tempLbl.getName().split(",");
				JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
				tempTxt.setBackground(Color.LIGHT_GRAY);
			}
		});
		
		
		txt.setEnabled(false);
		txt.setBackground(mainPanel.getBackground());
		txt.setDisabledTextColor(Color.BLACK);
		txt.setBorder(null);
		String txtName = "txt,r," + dirInode.getInodeNum();
		txt.setName(txtName);
		txt.setAlignmentX(Component.CENTER_ALIGNMENT);
		componentMap.put(txtName, txt);
		
		txt.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseExited(MouseEvent e) {
				e.getComponent().setForeground(Color.BLACK);
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent e) {
				e.getComponent().setEnabled(true);
				e.getComponent().setBackground(mainPanel.getBackground());
				e.getComponent().setForeground(Color.BLUE);
			}
		});
		txt.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyPressed(KeyEvent e) {
				JTextField txt;
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					e.getComponent().setEnabled(false);
					e.getComponent().setBackground(mainPanel.getBackground());
					e.getComponent().setForeground(Color.BLACK);
					
					txt = (JTextField)e.getSource();
					
					String[] temp = txt.getName().split(",");
					rootDir.renameFile(Integer.parseInt(temp[2]), txt.getText());						
				}
			}
		});
		
		pnl.add(lbl);
		pnl.add(txt);
		columnPanel.add(pnl);
		contentPanelWest.add(columnPanel);
		contentPanelWest.revalidate();
		
	}
	
	private JComponent getComponentByName(String componentName){
		if(componentMap.containsKey(componentName)){
			return (JComponent) componentMap.get(componentName);
		}
		else return null;
	}
	
	private JPanel getPanelByName(String componentName){
		if(colPanelMap.containsKey(componentName)){
			return (JPanel) colPanelMap.get(componentName);
		}
		else return null;
	}
	
	private String getNewFolderName(String compare){
		String ret = "";
		Iterator<Map.Entry<Integer, DirEntry>> it = dirContent.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<Integer, DirEntry> entry = it.next();
			if(entry.getValue().getName().equals(compare)){
				return "";
			}
		}
		ret = compare;
		return ret;
	}
	
	private String getNewFileName(String compare){
		String ret = "";
		Iterator<Map.Entry<Integer, DirEntry>> it = dirContent.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<Integer, DirEntry> entry = it.next();
			if(entry.getValue().getName().equals(compare)){
				return "";
			}
		}
		ret = compare;
		return ret;
	}
}
