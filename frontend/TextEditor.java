package frontend;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JTextArea;

public class TextEditor extends Component{
	JDialog dialog ;
	JTextArea txtArea = new JTextArea();
	JMenuBar menuBar = new JMenuBar();
	JMenu fileMenu  = new JMenu("File");
	
	public TextEditor(JFrame parent) {
		dialog = new JDialog(parent, "TEdit", false);
		dialog.setLocationRelativeTo(parent);
		dialog.setBounds(50, 50, 300, 400);
		dialog.setJMenuBar(menuBar);
		menuBar.add(fileMenu);
		dialog.setVisible(true);
		dialog.add(txtArea);
	}
	
	
}
