package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FolderListing extends JComponent{
	private static final long serialVersionUID = 1L;
	JDialog dialog;
	JPanel mainPanel = new JPanel();
	JPanel contentPanel = new JPanel();
	JPanel contentPanelSouth = new JPanel();
	JMenu menuBar = new JMenu();
	JButton openBtn = new JButton("Open");
	JTextField selectedFolder = new JTextField("vhgvjhvhjvhk");
	String parentPath;
	int parentId;
	JFrame mainFrame;
	public FolderListing(JFrame parent,String parentFolderpath,int parentId){
		this.parentPath = parentFolderpath;
		this.parentId = parentId;
		this.mainFrame = parent;
		
		dialog = new JDialog(parent, parentFolderpath + "/", false);
		dialog.setLocationRelativeTo(parent);
		dialog.setBounds(50, 50, 500, 400);		
		dialog.setVisible(true);
		
		selectedFolder.setPreferredSize(new Dimension(250, 30));
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(menuBar,BorderLayout.NORTH);
		mainPanel.add(contentPanel,BorderLayout.WEST);
		mainPanel.add(contentPanelSouth,BorderLayout.SOUTH);
		contentPanelSouth.add(selectedFolder);
		contentPanelSouth.add(openBtn);
		
		openBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println(selectedFolder.getText());
			}
		});
		
		dialog.add(mainPanel);
		
		showExistingFoldersAndFiles();
	}
	
	public FolderListing(JDialog parent,String parentFolderpath,int parentId){
		this.parentPath = parentFolderpath;
		this.parentId = parentId;
		
		dialog = new JDialog(parent, parentFolderpath + "/", false);
		dialog.setLocationRelativeTo(parent);
		dialog.setBounds(50, 50, 500, 400);		
		dialog.setVisible(true);
		
		selectedFolder.setPreferredSize(new Dimension(250, 30));
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(menuBar,BorderLayout.NORTH);
		mainPanel.add(contentPanel,BorderLayout.WEST);
		mainPanel.add(contentPanelSouth,BorderLayout.SOUTH);
		contentPanelSouth.add(selectedFolder);
		contentPanelSouth.add(openBtn);
		
		openBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println(selectedFolder.getText());
			}
		});
		
		dialog.add(mainPanel);
		
		showExistingFoldersAndFiles();
	}
	
	private void addmenuItems(){
		
	}
	
	private void showExistingFoldersAndFiles(){
		//get existing folder list from parentpath and id
		String[] existingFolderAndFileNames ={"abc","bcd"};
		BufferedImage img = null;
		JButton lbl;
		JTextField txt;
		for(int i = 0;i < existingFolderAndFileNames.length;i++){
			try {
				img = ImageIO.read(new File("folder.gif"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			lbl = new JButton(new ImageIcon(img));
			lbl.setBorder(BorderFactory.createEmptyBorder());
			lbl.setContentAreaFilled(false);
			lbl.setName(existingFolderAndFileNames[i]); //this has to be unique like path
			
			txt = new JTextField(existingFolderAndFileNames[i]); 
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
						dialog.dispose();
					}
				}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
			});
			
			lbl.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					
				}
				public void focusGained(FocusEvent e) {
					selectedFolder.setText("");
					selectedFolder.setText(e.getComponent().getName());
				}
			});
			contentPanel.add(lbl);
			contentPanel.add(txt);
			contentPanel.revalidate();
		}
	}
}
