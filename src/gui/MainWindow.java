package gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import main.Core;
import main.Utils;

import javax.swing.JTable;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private JPanel contentPane;
	private JTextField searchInput;
	private JTable searchRes;
	public DefaultTableModel tableModel;
	private CountDownLatch resLatch;

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		setVisible(true);
		setTitle("XNet v" + Core.version);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		tableModel = new DefaultTableModel();
		tableModel.addColumn("Filename");
		
		resLatch = new CountDownLatch(1);
		
		searchInput = new JTextField();
		searchInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				int key = arg0.getKeyCode();
				if(key == KeyEvent.VK_ENTER) {
					//clear any previous res
					clearTable();
					//clear fileToHash matcher
					Core.fileToHash.clear();
					if(Core.peerList.size() == 0) {
						out("No peers connected. Query is not possible.");
					} else {
						String input = searchInput.getText();
						if(input.equals("")) {
							out("You cannot search for a blank query.");
						} else {
							Utils.doSearch(input);
						}
						//dump results
					}
					searchInput.setText("");
				}
			}
		});
		searchInput.setBounds(10, 13, 414, 20);
		contentPane.add(searchInput);
		searchInput.setColumns(10);
		
		searchRes = new JTable(tableModel);
		searchRes.setCellSelectionEnabled(true);
		searchRes.setColumnSelectionAllowed(true);
		searchRes.setBounds(10, 44, 414, 207);
		contentPane.add(searchRes);
		resLatch.countDown();
	}
	
	public void out(String str) {
		try {
			resLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		tableModel.addRow(new String[]{str});
	}
	
	public void clearTable() {
		for(int i=0; i < tableModel.getRowCount(); i++) {
			tableModel.removeRow(i);
		}
	}
}
