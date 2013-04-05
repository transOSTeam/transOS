package frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ErrorDialog extends JComponent{
	private static final long serialVersionUID = 1L;
	private JDialog dialog; 
	private JPanel mainPanel;
	public ErrorDialog(JFrame parent,String errorText){
		dialog = new JDialog(parent, "ERROR!", true);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		JPanel pnl1 = new JPanel();
		JLabel lbl = new JLabel(errorText);
		pnl1.add(lbl);
		JPanel pnl2 = new JPanel();
		JButton btn = new JButton("Ok");
		pnl2.add(btn);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dialog.dispose();
			}
		});
		mainPanel.add(pnl1);
		mainPanel.add(pnl2);
		dialog.add(mainPanel);
		dialog.setLocationRelativeTo(parent);
		dialog.setBounds(90, 90, 300, 125);		
		dialog.setVisible(true);
	}

}
