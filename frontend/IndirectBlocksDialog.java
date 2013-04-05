package frontend;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import backend.disk.Disk;

public class IndirectBlocksDialog extends JComponent{
	private static final long serialVersionUID = 1L;
	private JDialog dialog; 
	private JPanel mainPanel;
	private JPanel columnPanel;
	private ArrayList<Integer> indirectBlocks;
	private int pnlCount = 0;
	public IndirectBlocksDialog(JFrame parent,ArrayList<Integer> indirectBlocks){
		this.indirectBlocks = indirectBlocks;
		dialog = new JDialog(parent, "Indirect Blocks", true);
		mainPanel = new JPanel();
		columnPanel = new JPanel();
		columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
		columnPanel.setName("col" + pnlCount);
		fill();
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		dialog.add(mainPanel);
		dialog.setLocationRelativeTo(parent);
		dialog.setBounds(90, 90, 300, 300);		
		dialog.setVisible(true);
	}
	
	private void fill(){
		mainPanel.add(columnPanel);
		for(int i = 0;i < indirectBlocks.size();i++){
			JButton btn = new JButton(String.format("%05d", indirectBlocks.get(i)));
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
			columnPanel.add(btn);
			if(columnPanel.getComponentCount() == 5){
				columnPanel = new JPanel();
				columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
				pnlCount++;
				columnPanel.setName("col" + pnlCount);
				mainPanel.add(columnPanel);
			}
		}
	}
}
