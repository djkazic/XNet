package gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import main.Core;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private JPanel contentPane;
	private JTextField searchInput;
	private JList searchRes;
	private DefaultListModel listModel;

	/**
	 * Tester launch of GUI.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	@SuppressWarnings("unchecked")
	public MainWindow() {
		setVisible(true);
		setTitle("XNet v" + Core.version);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		listModel = new DefaultListModel<String>();
		
		searchInput = new JTextField();
		searchInput.setToolTipText("Enter your search query and press Enter.");
		searchInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				int key = arg0.getKeyCode();
				if(key == KeyEvent.VK_ENTER) {
					//perform search
					//listModel.addElement(searchInput.getText());
					searchInput.setText("");
				}
			}
		});
		searchInput.setBounds(10, 13, 414, 20);
		contentPane.add(searchInput);
		searchInput.setColumns(10);
		
		searchRes = new JList<String>(listModel);
		searchRes.setBounds(10, 44, 414, 207);
		contentPane.add(searchRes);
	}
}
