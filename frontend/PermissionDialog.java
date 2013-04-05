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

import backend.disk.Directory;

public class PermissionDialog extends JComponent{
	private static final long serialVersionUID = 1L;
	
	private JFrame parent;
	private JDialog dialog; 
	private JPanel mainPanel;
	private JPanel columnPanel;
	private JTextField user;
	private JTextField group;
	private JTextField world;
	private Directory parentDir;
	private int inodeNum;
	
	public PermissionDialog(JFrame parent, Directory parentDir, int inodeNUm){
		this.parent = parent;
		this.inodeNum = inodeNUm;
		this.parentDir = parentDir;
		
		dialog = new JDialog(parent, "Change Permissions", false);
		mainPanel = new JPanel();
		columnPanel = new JPanel();
		columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
		fill();
		mainPanel.add(columnPanel);
		dialog.add(mainPanel);
		dialog.setLocationRelativeTo(parent);
		dialog.setBounds(70, 70, 200, 200);		
		dialog.setVisible(true);
	}
	
	private void fill(){
		JPanel pnl1 = new JPanel();
		pnl1.add(new JLabel("User  :"));
		user = new JTextField();
		user.setPreferredSize(new Dimension(30, 20));
		pnl1.add(user);
		
		JPanel pnl2 = new JPanel();
		pnl2.add(new JLabel("Group :"));
		group = new JTextField();
		group.setPreferredSize(new Dimension(30, 20));
		pnl2.add(group);
		
		JPanel pnl3 = new JPanel();
		pnl3.add(new JLabel("World :"));
		world = new JTextField();
		world.setPreferredSize(new Dimension(30, 20));
		pnl3.add(world);
		
		JButton btn  = new JButton("Done");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(user.getText().length() != 0 && group.getText().length() != 0 && world.getText().length() != 0){
					String permS = user.getText() + group.getText() + world.getText();
					parentDir.chmod(inodeNum, permS);
				}
				else{
					ErrorDialog er = new ErrorDialog(parent, "Fill all the three perms");
					parent.add(er);
				}
			}
		});
		
		columnPanel.add(pnl1);
		columnPanel.add(pnl2);
		columnPanel.add(pnl3);
		columnPanel.add(btn);
	}
}
