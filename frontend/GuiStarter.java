package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import backend.TransSystem;
import backend.disk.Directory;
import backend.disk.Disk;

public class GuiStarter {
	private JFrame mainFrame;
	private JPanel mainPanel;
	private JPanel contentPanelWest;
	private JPanel contentPanelEast;
	private JPanel loginPanel;
	private JTextField userName;
	private JTextField groupName;
	private JPasswordField password;
	private JButton loginBtn;
	private JButton regBtn;
	private JMenuBar mainMenuBar;

	private Map<String, JComponent> componentMap;
	private Directory rootDir;
	
	static int copiedInodeNum = 0;
	static int copyFrom = 0;
	static int cutInodeNum = 0;	
	
	public GuiStarter() {
		mainFrame = new JFrame("Transparent OS");
		mainPanel = new JPanel();
		contentPanelWest = new JPanel();
		contentPanelEast = new JPanel();
		loginPanel = new JPanel();
		mainMenuBar = new JMenuBar();
		
		componentMap = new HashMap<String, JComponent>();
		
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
		mainPanel.add(loginPanel, BorderLayout.CENTER);
		showLoginPanel();
	}

	private void showDesktop(){
		loginPanel.removeAll();
		loginPanel.repaint();
		mainPanel.add(mainMenuBar, BorderLayout.NORTH);
		mainPanel.add(contentPanelWest,BorderLayout.WEST);
		mainPanel.add(contentPanelEast,BorderLayout.EAST);
		showRoot();
	}
	
	private void showLoginPanel(){
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		
		JLabel userNameLbl = new JLabel("User Name: ");
		JLabel pwdLabel = new JLabel("Password:  ");
		JLabel groupNameLbl = new JLabel("Group Name: ");
		
		JPanel pnl1 = new JPanel();
		userName = new JTextField();
		userName.setPreferredSize(new Dimension(80, 20));
		pnl1.add(userNameLbl);
		pnl1.add(userName);		
		
		JPanel pnl2 = new JPanel();
		password = new JPasswordField();
		password.setPreferredSize(new Dimension(80, 20));
		pnl2.add(pwdLabel);
		pnl2.add(password);
		
		JPanel pnl4 = new JPanel();
		groupName = new JTextField();
		groupName.setPreferredSize(new Dimension(80, 20));
		groupName.setText("none");
		pnl4.add(groupNameLbl);
		pnl4.add(groupName);
		
		JPanel pnl3 = new JPanel();
		loginBtn = new JButton("Login");
		loginBtn.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {
				if(userName.getText().length() != 0 && password.getPassword().toString().length() != 0 ){
					char[] a = password.getPassword();
					String t = "";
					for(int q = 0;q < a.length;q++){
						t += a[q];
					}
					if(TransSystem.authenticateUser(userName.getText(), t)){
						showDesktop();
					}
					else{
						System.out.println("jhgv");
					}
				}
			}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent arg0) {}
		});
		regBtn = new JButton("Register");
		regBtn.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {
				if(userName.getText().length() != 0 && password.getPassword().toString().length() != 0 ){
					char[] a = password.getPassword();
					String t = "";
					for(int q = 0;q < a.length;q++){
						t += a[q];
					}
					if(TransSystem.registerUser(userName.getText(), t, groupName.getText())){
						showDesktop();
					}
				}
			}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent arg0) {}
		});
		pnl3.add(loginBtn);
		pnl3.add(regBtn);
		
		innerPanel.add(pnl1);
		innerPanel.add(pnl2);
		innerPanel.add(pnl4);
		innerPanel.add(pnl3);
		loginPanel.add(innerPanel);
		loginPanel.revalidate();
	}
	
	private void showRoot(){
		BufferedImage img = null;
		JButton lbl;
		String folderName = "root";
		
		JPanel columnPanel = null;
		columnPanel = new JPanel();
		columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
		
		JPanel pnl = null;
		pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.setBorder(new EmptyBorder(10,10,10,10));
		
		JTextField txt = new JTextField(folderName);
		
		try {
			img = ImageIO.read(new File("folder.gif"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		lbl = new JButton(new ImageIcon(img));
		lbl.setBorder(BorderFactory.createEmptyBorder());
		lbl.setContentAreaFilled(false);
		String lblName = "lbl,d," + TransSystem.getUser().getHomeDirInodeNum();
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
						rootDir.editFile(inodeNum);
					}
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger()){
					
				}
			}
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger()){
					
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
		String txtName = "txt,d," + TransSystem.getUser().getHomeDirInodeNum();
		txt.setName(txtName);
		txt.setAlignmentX(Component.CENTER_ALIGNMENT);
		componentMap.put(txtName, txt);
		
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
	
}