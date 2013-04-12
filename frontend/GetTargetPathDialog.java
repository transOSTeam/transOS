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

public class GetTargetPathDialog extends JComponent {
	private static final long serialVersionUID = -5340752002379608964L;
	private JFrame parent;
	private JDialog dialog; 
	private JPanel mainPanel;
	private JPanel columnPanel;
	private JTextField pathName;
	
	public GetTargetPathDialog(JFrame parent){
		
		dialog = new JDialog(parent, "Please give path of the source file", true);
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
		pnl1.add(new JLabel("Path :"));
		pathName = new JTextField();
		pathName.setPreferredSize(new Dimension(100, 20));
		pnl1.add(pathName);
		
		JButton btn  = new JButton("Done");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(pathName.getText().length() != 0){
					GuiStarter.linkPath = "";
					GuiStarter.linkPath = pathName.getText();
					dialog.dispose();
				}
				else{
					ErrorDialog er = new ErrorDialog(parent, "Path cannot be empty");
					parent.add(er);
				}
			}
		});
		
		columnPanel.add(pnl1);
		columnPanel.add(btn);
	}

}
