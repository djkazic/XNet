package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

public class WarningPopup extends JDialog implements Runnable {

	private final JPanel contentPanel = new JPanel();
	private String text;

	public WarningPopup(String text) {
		setVisible(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 340, 125);
		this.text = text;
	}
	
	public void run() {
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel lblSomeText = new JLabel(text);
			lblSomeText.setBounds(10, 11, 304, 52);
			contentPanel.add(lblSomeText);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
		}
		addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {
				//blank implementation
			}
			public void windowClosing(WindowEvent e) {
				//blank implementation
			}
			public void windowIconified(WindowEvent e) {
				//blank implementation
				
			}
			public void windowDeiconified(WindowEvent e) {
				//blank implementation
				
			}
			public void windowActivated(WindowEvent e) {
				//blank implementation
				
			}
			public void windowDeactivated(WindowEvent e) {
				//blank implementation
			}
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
		while(true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
}
