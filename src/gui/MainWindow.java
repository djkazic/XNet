package gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.CountDownLatch;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import main.Core;
import main.Utils;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private JPanel contentPane;
	private JTextField searchInput;
	private JList searchRes;
	public DefaultListModel<String> listModel;
	private boolean doneInit = false;
	private CountDownLatch resLatch;

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
		resLatch = new CountDownLatch(1);
		
		searchInput = new JTextField();
		searchInput.setToolTipText("Enter your search query and press Enter.");
		searchInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				int key = arg0.getKeyCode();
				if(key == KeyEvent.VK_ENTER) {
					//clear any previous res
					listModel.clear();
					//clear core db
					Core.plainText.clear();
					String input = searchInput.getText();
					if(input.equals("")) { 
						listModel.addElement("You cannot search for a blank query.");
					} else {
						Utils.doSearch(input);
					}
					//dump results
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
		resLatch.countDown();
	}
	
	public void setResults(String str) {
		try {
			resLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		listModel.addElement(str);
	}
}
