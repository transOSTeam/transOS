package frontend;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

public class UsageBitmap extends JComponent {

	private static final long serialVersionUID = 1382660235169701615L;
	
	private JDialog dialog; 
	private JPanel panel;
	private JScrollPane scrollPane;
	public UsageBitmap(JFrame parent, byte[] bitmap, String bitmapTitle) {
		dialog = new JDialog(parent, bitmapTitle, true);
		panel = new JPanel();
		scrollPane = new JScrollPane(panel);
		for(int i = 0; i < bitmap.length; i++) {
			Boolean isUsed;
			if(bitmap[i] == 1)
				isUsed = true;
			else
				isUsed = false;
			String txt = bitmap.length > 1000 ? String.format("%04d", i) : String.format("%03d", i);
			BitmapEntry tempEntry = new BitmapEntry(txt, isUsed);
			panel.add(tempEntry);
		}
		
		
		scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		dialog.add(scrollPane);
		dialog.setLocationRelativeTo(parent);
		int width, height, swt, sht;
		if(bitmap.length > 1000) {
			width = 1000;
			height = 700;
			swt = 150;
			sht = 3000;
		}
		else {
			width = 800;
			height = 650;
			swt = 760;
			sht = 650;
		}
		panel.setPreferredSize(new Dimension(swt, sht));
		dialog.setBounds(90, 90, width, height);
		dialog.setVisible(true);
	}

	
	public class BitmapEntry extends JPanel {
		private static final long serialVersionUID = 7509036403330226902L;
		
		private JLabel label;
		
		public BitmapEntry(String labelTxt, Boolean isUsed) {
			this.label = new JLabel();
			this.label.setText(labelTxt);
			this.label.setVisible(true);
			if(isUsed) {
				Color usedColor = new Color(248, 167, 167);
				this.setBackground(usedColor);
			}
			else {
				Color freeColor = new Color(183, 250, 177);
				this.setBackground(freeColor);
			}
			this.add(this.label);
			this.setVisible(true);
		}
	}
}
