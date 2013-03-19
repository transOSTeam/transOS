package frontend;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SaveFileDialog extends JComponent{
	private static final long serialVersionUID = 1L;
	
	JDialog saveDialog;
	JPanel mainPanel = new JPanel();
	JTextField txt = new JTextField();
	JButton saveBtn = new JButton("Save");
	
	public SaveFileDialog(JDialog parent,final String textToSave){
		saveDialog = new JDialog(parent, "Name this file", true);
		saveDialog.setLocationRelativeTo(parent);
		saveDialog.add(mainPanel);
		saveDialog.setBounds(70,70,200,100);
		
		txt.setPreferredSize(new Dimension(100, 25));		
		mainPanel.add(txt);
		mainPanel.add(saveBtn);
		
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//call save file method here
				System.out.println(textToSave);
				if(!txt.getText().isEmpty()){
					saveDialog.dispose();
				}				
			}
		});
		
		saveDialog.setVisible(true);
	}
}
