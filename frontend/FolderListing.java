package frontend;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class FolderListing extends JComponent{
	private static final long serialVersionUID = 1L;
	JDialog dialog;
	public FolderListing(JFrame parent){
		dialog = new JDialog(parent, "Folder Listing", false);
		dialog.setLocationRelativeTo(parent);
		dialog.setBounds(50, 50, 300, 400);
		dialog.setVisible(true);
	}
}
