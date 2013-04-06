package frontend;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import backend.InvalidUserException;
import backend.PermissionDeniedException;
import backend.disk.Directory;

public class ChangeOwnerDialog extends JComponent{
	private static final long serialVersionUID = 1L;
	
	private JFrame parent;
	private JDialog dialog; 
	private JPanel mainPanel;
	private JPanel columnPanel;
	private JTextField ownerName;
	private Directory parentDir;
	private int inodeNum;
	
	public ChangeOwnerDialog(JFrame parent, Directory parentDir, int inodeNUm){
		this.parent = parent;
		this.inodeNum = inodeNUm;
		this.parentDir = parentDir;
		
		dialog = new JDialog(parent, "Change Owner", false);
		mainPanel = new JPanel();
		columnPanel = new JPanel();
		columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
		fill();
		mainPanel.add(columnPanel);
		dialog.add(mainPanel);
		dialog.setLocationRelativeTo(parent);
		dialog.setBounds(70, 70, 300, 150);		
		dialog.setVisible(true);
	}
	
	private void fill(){
		JPanel pnl1 = new JPanel();
		pnl1.add(new JLabel("Owner Name :"));
		ownerName = new JTextField();
		ownerName.setPreferredSize(new Dimension(70, 20));
		pnl1.add(ownerName);
		
		JButton btn  = new JButton("Done");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(ownerName.getText().length() != 0){
					String name = ownerName.getText();
					try {
						parentDir.chown(inodeNum, name);
						dialog.dispose();
					} catch (PermissionDeniedException e1) {
						ErrorDialog er = new ErrorDialog(parent, "Permission denied!!");
						mainPanel.add(er);
						e1.printStackTrace();
					} catch (InvalidUserException e1) {
						ErrorDialog er = new ErrorDialog(parent, "Invalid user!");
						mainPanel.add(er);
						e1.printStackTrace();
					}
				}
				else{
					ErrorDialog er = new ErrorDialog(parent, "Owner name cannot be empty");
					parent.add(er);
				}
			}
		});
		
		columnPanel.add(pnl1);
		columnPanel.add(btn);
	}
}
