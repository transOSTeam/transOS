package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import backend.disk.DirEntry;
import backend.disk.Directory;
import backend.disk.Inode;

public class FolderListing extends JComponent{
	private static final long serialVersionUID = 1L;
	
	JFrame mainFrame;
	JDialog dialog;
	JPanel mainPanel;
	JPanel contentPanel;
	JPanel contentPanelSouth;
	JMenu menuBar;
	JButton openBtn;
	JTextField selectedFolder;
	String parentPath;
	int parentInodeNum;
	JPopupMenu popupMenu;
	JPopupMenu rightClickMenu;
	JButton rightClickedLbl;
	
	private HashMap<String, JComponent> componentMap;
	private Directory parentDir;
	private HashMap<Integer, DirEntry> dirContent;
	private static int count = 0;
	
	private Stack<String> s;
	
	public FolderListing(JFrame parent,String parentFolderpath,int parentInodeNum){
		mainPanel = new JPanel();
		contentPanel = new JPanel();
		contentPanelSouth = new JPanel();
		menuBar = new JMenu();
		openBtn = new JButton("Open");
		selectedFolder = new JTextField("");
		popupMenu = new JPopupMenu();
		rightClickMenu = new JPopupMenu();
		
		componentMap = new HashMap<String, JComponent>();
		parentDir = new Directory(parentInodeNum);
		dirContent = parentDir.getDirContent();
		
		s = new Stack<String>();
		
		this.parentPath = parentFolderpath;
		this.parentInodeNum = parentInodeNum;
		this.mainFrame = parent;
		
		dialog = new JDialog(parent, getPath(), false);
		dialog.setLocationRelativeTo(parent);
		dialog.setBounds(50, 50, 700, 600);		
		dialog.setVisible(true);
		
		selectedFolder.setPreferredSize(new Dimension(250, 30));
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(menuBar,BorderLayout.NORTH);
		mainPanel.add(contentPanel,BorderLayout.WEST);
		mainPanel.add(contentPanelSouth,BorderLayout.SOUTH);
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
		
		contentPanelSouth.add(selectedFolder);
		contentPanelSouth.add(openBtn);
		
		openBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println(selectedFolder.getText());
			}
		});
		
		dialog.add(mainPanel);
		
		addmenuItems();
		addPopupMenuItems();
		addRightClickMenuitems();
		showExistingFoldersAndFiles();
	}
	
	public FolderListing(JDialog parent,String parentFolderpath,int parentInodeNum){
		mainPanel = new JPanel();
		contentPanel = new JPanel();
		contentPanelSouth = new JPanel();
		menuBar = new JMenu();
		openBtn = new JButton("Open");
		selectedFolder = new JTextField("");
		popupMenu = new JPopupMenu();
		rightClickMenu = new JPopupMenu();
		
		componentMap = new HashMap<String, JComponent>();
		parentDir = new Directory(parentInodeNum);
		dirContent = parentDir.getDirContent();
		
		s = new Stack<String>();
		
		this.parentPath = parentFolderpath;
		this.parentInodeNum = parentInodeNum;
		
		dialog = new JDialog(parent, getPath() , false);
		dialog.setLocationRelativeTo(parent);
		dialog.setBounds(50, 50, 700, 600);
		dialog.setVisible(true);
		
		selectedFolder.setPreferredSize(new Dimension(250, 30));
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(menuBar,BorderLayout.NORTH);
		mainPanel.add(contentPanel,BorderLayout.WEST);
		mainPanel.add(contentPanelSouth,BorderLayout.SOUTH);
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
		
		contentPanelSouth.add(selectedFolder);
		contentPanelSouth.add(openBtn);
		
		openBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println(selectedFolder.getText());
			}
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
				TextEditor txtEdit = new TextEditor(mainFrame);
				mainPanel.add(txtEdit);
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
			}
		});
		
		JMenuItem item4 = new JMenuItem("Refresh");
		item4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parentDir = null;
				parentDir = new Directory(parentInodeNum);
				dirContent = parentDir.getDirContent();
				contentPanel.removeAll();
				contentPanel.revalidate();
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
				parentDir.deleteFile(Integer.parseInt(temp[2]));
				contentPanel.remove(rightClickedLbl);
				contentPanel.remove(tempTxt);
				contentPanel.revalidate();
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
		
		rightClickMenu.add(item4);
		rightClickMenu.add(item1);
		rightClickMenu.add(item2);
		rightClickMenu.add(item3);
	}
	
	private void showExistingFoldersAndFiles(){
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
			lbl.setName(lblName);
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
					selectedFolder.setText("");
				}
				public void focusGained(FocusEvent e) {
					String[] temp = e.getComponent().getName().split(",");
					JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
					selectedFolder.setText(tempTxt.getText());
				}
			});
			
			txt = new JTextField(entry.getValue().getName());			
			txt.setEnabled(false);
			txt.setBackground(mainPanel.getBackground());
			txt.setDisabledTextColor(Color.BLACK);
			txt.setBorder(null);
			String txtName = "txt,d," + entry.getKey().toString();
			txt.setName(txtName);
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
						parentDir.renameFile(Integer.parseInt(temp[2]), txt.getText());						
					}
				}
			});
			
			contentPanel.add(lbl);
			contentPanel.add(txt);
			contentPanel.revalidate();
		}
	}
	
	private void createFolder(int count){
		BufferedImage img = null;
		JButton lbl;
		String folderName = "new folder " + count;
		
		while(getNewFolderName(folderName).equals("")){
			folderName = getNewFolderName("new folder " + count++);
		}
		
		Inode dirInode = parentDir.makeDir(folderName);
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
				selectedFolder.setText("");
			}
			public void focusGained(FocusEvent e) {
				String[] temp = e.getComponent().getName().split(",");
				JTextField tempTxt = (JTextField)getComponentByName("txt,"+temp[1] + "," + temp[2]);
				selectedFolder.setText(tempTxt.getText());
			}
		});
		
		txt.setEnabled(false);
		txt.setBackground(mainPanel.getBackground());
		txt.setDisabledTextColor(Color.BLACK);
		txt.setBorder(null);
		String txtName = "txt,d," + dirInode.getInodeNum();
		txt.setName(txtName);
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
					parentDir.renameFile(Integer.parseInt(temp[2]), txt.getText());	
				}
			}
		});	
		
		contentPanel.add(lbl);
		contentPanel.add(txt);
		contentPanel.revalidate();
	}
	
	private JComponent getComponentByName(String componentName){
		if(componentMap.containsKey(componentName)){
			return (JComponent) componentMap.get(componentName);
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
