package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import backend.disk.Inode;

public class Properties extends JComponent{
	private static final long serialVersionUID = 1L;
	
	private JDialog dialog;
	private JPanel mainPanel;
	private JLabel nameLbl;
	private JLabel typeLbl;
	private JLabel accessedLbl;
	private JLabel modifiedLbl;
	private JLabel createdLbl;
	private FormUtility formUtility;
	
	private int inodeNum;
	private String name;
	
	public Properties(JFrame owner, int inodeNum, String name){
		this.inodeNum = inodeNum;
		this.name = name;
		
		dialog = new JDialog(owner, "Properties", false);
		mainPanel = new JPanel();
		nameLbl = new JLabel();
		typeLbl = new JLabel();
		accessedLbl = new JLabel();
		modifiedLbl = new JLabel();
		createdLbl = new JLabel();
		formUtility = new FormUtility();
		
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(mainPanel, BorderLayout.NORTH);
		dialog.setLocationRelativeTo(owner);
		dialog.setBounds(50, 50, 400, 200);
		
		mainPanel.setLayout(new GridBagLayout());
		Border border = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
		mainPanel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		makeForm();
		fillForm();
		
		dialog.setVisible(true);
	}
	
	public Properties(JDialog owner, int inodeNum, String name){
		this.inodeNum = inodeNum;
		this.name = name;
		
		dialog = new JDialog(owner, "Properties", false);
		mainPanel = new JPanel();
		nameLbl = new JLabel();
		typeLbl = new JLabel();
		accessedLbl = new JLabel();
		modifiedLbl = new JLabel();
		createdLbl = new JLabel();
		formUtility = new FormUtility();
		
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(mainPanel, BorderLayout.NORTH);
		dialog.setLocationRelativeTo(owner);
		dialog.setBounds(50, 50, 400, 200);
		
		mainPanel.setLayout(new GridBagLayout());
		Border border = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
		mainPanel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		makeForm();
		fillForm();
		
		dialog.setVisible(true);
	}
	
	private void makeForm(){
		formUtility.addLabel("Name : ", mainPanel);
		formUtility.addLastField(nameLbl, mainPanel);		
		formUtility.addLabel("", mainPanel);
		formUtility.addLastField(new JLabel(""), mainPanel);
		
		formUtility.addLabel("Type : ", mainPanel);
		formUtility.addLastField(typeLbl, mainPanel);		
		formUtility.addLabel("", mainPanel);
		formUtility.addLastField(new JLabel(""), mainPanel);
		
		formUtility.addLabel("Accessed Tume : ", mainPanel);
		formUtility.addLastField(accessedLbl, mainPanel);		
		formUtility.addLabel("", mainPanel);
		formUtility.addLastField(new JLabel(""), mainPanel);
		
		formUtility.addLabel("Modified Time : ", mainPanel);
		formUtility.addLastField(modifiedLbl, mainPanel);		
		formUtility.addLabel("", mainPanel);
		formUtility.addLastField(new JLabel(""), mainPanel);
		
		formUtility.addLabel("Created Time : ", mainPanel);
		formUtility.addLastField(createdLbl, mainPanel);		
		formUtility.addLabel("", mainPanel);
		formUtility.addLastField(new JLabel(""), mainPanel);
	}
	
	private void fillForm(){
		Inode tempInode = new Inode(inodeNum);
		
		nameLbl.setText(name);
		if(tempInode.getFileType() == 'r'){
			typeLbl.setText("Text File");
		}
		else if(tempInode.getFileType() == 'd'){
			typeLbl.setText("Directory");
		}
		
		accessedLbl.setText(tempInode.getAccessedTime());
		modifiedLbl.setText(tempInode.getModifiedTime());
		createdLbl.setText(tempInode.getCreatedTime());
	}
}
