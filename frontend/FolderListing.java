package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import backend.PermissionDeniedException;
import backend.disk.DirEntry;
import backend.disk.Directory;
import backend.disk.Disk;
import backend.disk.Inode;

public class FolderListing extends JComponent{
	private static final long serialVersionUID = 1L;
	
	private JFrame mainFrame;
	private JDialog dialog;
	private JPanel mainPanel;
	private JPanel contentPanelWest;
	private JPanel contentPanelWestSouth;
	private JPanel contentPanelEast;
	private JPanel consolePanel;
	private JMenu menuBar;
	private String parentPath;
	private int parentInodeNum;
	private JPopupMenu popupMenu;
	private JPopupMenu rightClickMenu;
	private JButton rightClickedLbl;
	
	private Map<String, JComponent> componentMap;
	private Map<String, JPanel> colPanelMap;
	private Directory parentDir;
	private HashMap<Integer, DirEntry> dirContent;
	private int count = 0;
	private int count1 = 0;
	private int pnlCount;
	
	private Stack<String> s;
	
	public FolderListing(JFrame parent,String parentFolderpath,int parentInodeNum){
		mainPanel = new JPanel();
		contentPanelWest = new JPanel();
		contentPanelEast = new JPanel();
		contentPanelWestSouth = new JPanel();
		menuBar = new JMenu();
		popupMenu = new JPopupMenu();
		rightClickMenu = new JPopupMenu();
		
		componentMap = new HashMap<String, JComponent>();
		colPanelMap = new HashMap<String, JPanel>();
		pnlCount = 0;
		parentDir = new Directory(parentInodeNum);
		try {
			dirContent = parentDir.getDirContent();
		} catch (PermissionDeniedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		s = new Stack<String>();
		
		this.parentPath = parentFolderpath;
		this.parentInodeNum = parentInodeNum;
		this.mainFrame = parent;
		
		dialog = new JDialog(parent, getPath(), false);
		dialog.setLocationRelativeTo(parent);
		dialog.setBounds(50, 50, 800, 600);		
		dialog.setVisible(true);
		contentPanelEast.setPreferredSize(new Dimension(300,600));
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(menuBar,BorderLayout.NORTH);
		mainPanel.add(contentPanelWest,BorderLayout.WEST);
		mainPanel.add(contentPanelEast,BorderLayout.EAST);
		mainPanel.add(contentPanelWestSouth,BorderLayout.SOUTH);
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
			public void mouseClicked(MouseEvent arg0) {}
		});
		dialog.add(mainPanel);
		
		addmenuItems();
		addPopupMenuItems();
		addRightClickMenuitems();
		showExistingFoldersAndFiles();
	}

	private void addmenuItems(){
		
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
				if(GuiStarter.copyFrom != parentInodeNum){
					if(GuiStarter.cutInodeNum == GuiStarter.copiedInodeNum){
						try {
							parentDir.move(GuiStarter.copiedInodeNum, GuiStarter.copyFrom);
						} catch (PermissionDeniedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else{
						try {
							parentDir.copy(GuiStarter.copiedInodeNum, GuiStarter.copyFrom);
						} catch (PermissionDeniedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					parentDir = null;
					parentDir = new Directory(parentInodeNum);
					try {
						dirContent = parentDir.getDirContent();
					} catch (PermissionDeniedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					contentPanelWest.removeAll();
					contentPanelWest.revalidate();
					showExistingFoldersAndFiles();
				}
			}
		});
		
		JMenuItem item4 = new JMenuItem("Refresh");
		item4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parentDir = null;
				parentDir = new Directory(parentInodeNum);
				try {
					dirContent = parentDir.getDirContent();
				} catch (PermissionDeniedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				contentPanelWest.removeAll();
				contentPanelWest.revalidate();
				showExistingFoldersAndFiles();
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
				try {
					parentDir.deleteFile(Integer.parseInt(temp[2]));
				} catch (NumberFormatException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (PermissionDeniedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				JPanel pnl = (JPanel)rightClickedLbl.getParent();
				pnl.remove(rightClickedLbl);
				pnl.remove(tempTxt);
				JPanel colPan = (JPanel)pnl.getParent();
				colPan.remove(pnl);
				contentPanelWest.revalidate();
				contentPanelEast.removeAll();
				contentPanelEast.repaint();
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
				GuiStarter.copyFrom = parentInodeNum;
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
				GuiStarter.cutInodeNum = Integer.parseInt(temp[2]);
				GuiStarter.copyFrom = parentInodeNum;
			}
			public void mouseReleased(MouseEvent e) {
			}
		});
		
		JMenuItem item6 = new JMenuItem("Change permissions");
		item6.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				String[] temp = rightClickedLbl.getName().split(",");
				PermissionDialog p = new PermissionDialog(mainFrame, parentDir,Integer.parseInt(temp[2]));
				mainPanel.add(p);
			}
			public void mouseReleased(MouseEvent e) {
			}
		});
		
		JMenuItem item7 = new JMenuItem("Change Owner");
		item7.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				String[] temp = rightClickedLbl.getName().split(",");
				ChangeOwnerDialog c = new ChangeOwnerDialog(mainFrame, parentDir,Integer.parseInt(temp[2]));
				mainPanel.add(c);
			}
			public void mouseReleased(MouseEvent e) {
			}
		});
		
		rightClickMenu.add(item4);
		rightClickMenu.add(item5);
		rightClickMenu.add(item1);
		rightClickMenu.add(item2);
		rightClickMenu.add(item6);
		rightClickMenu.add(item7);
		rightClickMenu.add(item3);
	}
	
	private void showExistingFoldersAndFiles(){
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
							FolderListing fldrpane = new FolderListing(mainFrame,parentPath + "/" + tempTxt.getText(),Integer.parseInt(temp[2]));
							dialog.dispose();
							mainPanel.add(fldrpane);
						}
						else if(temp[1].equals("r")){
							int inodeNum = Integer.parseInt(temp[2]);
							/*Inode tempInode = new Inode(inodeNum);							
							String fileContent = tempInode.getFileContent();
							TextEditor txtEdit = new TextEditor(mainFrame, fileContent, rootDir, inodeNum);
							mainPanel.add(txtEdit);*/
							try {
								parentDir.editFile(inodeNum);
							} catch (PermissionDeniedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
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
					
					showConsoleContent(Integer.parseInt(temp[2]));
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
						try {
							parentDir.renameFile(Integer.parseInt(temp[2]), txt.getText());
						} catch (NumberFormatException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (PermissionDeniedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}						
					}
				}
			});
			
			pnl.add(lbl);
			pnl.add(txt);
			columnPanel.add(pnl);
			contentPanelWest.add(columnPanel);
			contentPanelWest.revalidate();
			if(columnPanel.getComponentCount() == 6){
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
		if(columnPanel.getComponentCount() == 6){
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
		
		Inode dirInode = null;
		try {
			dirInode = parentDir.makeDir(folderName);
		} catch (PermissionDeniedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
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
						FolderListing fldrpane = new FolderListing(mainFrame,parentPath + "/" + tempTxt.getText(),Integer.parseInt(temp[2]));
						dialog.dispose();
						mainPanel.add(fldrpane);
					}
					else if(temp[1].equals("r")){
						int inodeNum = Integer.parseInt(temp[2]);
						/*Inode tempInode = new Inode(inodeNum);							
						String fileContent = tempInode.getFileContent();
						TextEditor txtEdit = new TextEditor(mainFrame, fileContent, rootDir, inodeNum);
						mainPanel.add(txtEdit);*/
						try {
							parentDir.editFile(inodeNum);
						} catch (PermissionDeniedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
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
				showConsoleContent(Integer.parseInt(temp[2]));
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
					try {
						parentDir.renameFile(Integer.parseInt(temp[2]), txt.getText());
					} catch (NumberFormatException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (PermissionDeniedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
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
		if(columnPanel.getComponentCount() == 6){
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
		
		Inode dirInode = null;
		try {
			dirInode = parentDir.makeFile(fileName, "");
		} catch (PermissionDeniedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
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
						try {
							parentDir.editFile(inodeNum);
						} catch (PermissionDeniedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
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
				showConsoleContent(Integer.parseInt(temp[2]));
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
					try {
						parentDir.renameFile(Integer.parseInt(temp[2]), txt.getText());
					} catch (NumberFormatException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (PermissionDeniedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}						
				}
			}
		});
		
		pnl.add(lbl);
		pnl.add(txt);
		columnPanel.add(pnl);
		contentPanelWest.add(columnPanel);
		contentPanelWest.revalidate();
		
	}
	
	private void showConsoleContent(int inodeNum){
		contentPanelEast.removeAll();
		contentPanelEast.repaint();
		final Inode tempInode = new Inode(inodeNum);
		consolePanel = new JPanel();
		consolePanel.setLayout(new BoxLayout(consolePanel, BoxLayout.Y_AXIS));
		int i = 0;
		JLabel lbl = null;
		while(i != 9){
			if(i == 0) lbl = new JLabel("Inode Number : " + inodeNum);
			else if(i == 1) lbl = new JLabel("File Type : " + tempInode.getFileType());
			else if(i == 2) lbl = new JLabel("User Id : " + tempInode.getUserId());
			else if(i == 3) lbl = new JLabel("Group Id : " + tempInode.getGrpId());
			else if(i == 4) {
				Timestamp t = tempInode.getAccessedTime();
				lbl = new JLabel("Last Accessed : " + t.toString());
			}
			else if(i == 5){
				Timestamp t = tempInode.getModifiedTime(); 
				lbl = new JLabel("Last Modified : " + t.toString());
			}
			else if(i == 6) {
				Timestamp t = tempInode.getCreatedTime(); 
				lbl = new JLabel("Created On : " + t.toString());
			}
			else if(i == 7) lbl = new JLabel("Block Count : " + tempInode.getBlockCount());
			
			else if(i == 8){
				int[] temp = tempInode.getPermissions();
				lbl = new JLabel("Permission : " + temp[0] + "" + temp[1] + "" + temp[2]);
			}
			consolePanel.add(lbl);
			i++;
		}
		int blkCnt = tempInode.getBlockCount();
		if(blkCnt <= 4){
			int[] arr = tempInode.getBlockPointers();
			for(i = 0;i < blkCnt;i++){
				JButton btn = new JButton(String.format("%05d", arr[i]));
				btn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						JButton b = (JButton)ev.getSource();
						File tempFile = new File(Disk.transDisk.toString() + "/" +  b.getText());
						try {
							Desktop.getDesktop().open(tempFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				consolePanel.add(btn);
			}
		}
		else{
			int[] arr = tempInode.getBlockPointers();
			for(i = 0;i < 4;i++){
				JButton btn = new JButton(String.format("%05d", arr[i]));
				btn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						JButton b = (JButton)ev.getSource();
						File tempFile = new File(Disk.transDisk.toString() + "/" +  b.getText());
						try {
							Desktop.getDesktop().open(tempFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				consolePanel.add(btn);
			}
			JButton indirectBtn = new JButton("Indirect blocks");
			indirectBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					IndirectBlocksDialog i = new IndirectBlocksDialog(mainFrame, tempInode.getIndirectBlocks());
					mainPanel.add(i);
				}
			});
			consolePanel.add(indirectBtn);
		}
		
		contentPanelEast.add(consolePanel);
		contentPanelEast.revalidate();
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
	
	private String getPath(){
		String ret = "";
		String[] temp = this.parentPath.split("/");
		for(int i = 0; i < temp.length; i++){
			if(temp[i].isEmpty()){
				s.push("/");
			}
			else if(temp[i].equals(".")){}
			else if(temp[i].equals("..")){
				if(!s.peek().equals("/")){
					s.pop();
				}
			}
			else{
				s.push(temp[i]);
			}
		}
		
		for (int j = 0;j < s.size();j++){
			if(s.get(j).equals("/")) ret += s.get(j);
			else ret += s.get(j) + "/";
		}
		return ret;
	}
}
