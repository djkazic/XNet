package gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import main.Core;
import main.Utils;
import blocks.BlockedFile;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private JPanel contentPane;
	private JTextField searchInput;
	private JTable searchRes;
	private JTable downloadList;
	public DefaultTableModel tableModel;
	public DefaultTableModel downloadModel;
	private CountDownLatch resLatch;
	public CountDownLatch debugLatch;
	private JScrollPane searchResScrollPane;
	private JScrollPane downloadScrollPane;
	private JLabel lblPeers;
	private boolean searchMode;
	public String debugHost;

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		setResizable(false);
		searchMode = false;
		setVisible(true);
		setTitle("XNet v" + Core.version);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 590, 480);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		tableModel = new TableModelSpec();
		tableModel.addColumn("Status");
		
		downloadModel = new TableModelDL();
		downloadModel.addColumn("Filename");
		downloadModel.addColumn("Progress");
		
		resLatch = new CountDownLatch(1);
		
		searchInput = new JTextField();
		searchInput.setBounds(12, 12, 512, 25);
		
		searchInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				int key = arg0.getKeyCode();
				if(key == KeyEvent.VK_ENTER) {
					//clear any previous res
					clearTable();
					//clear core index
					Core.index.clear();
					if(Core.debugServer) {
						debugHost = searchInput.getText();
						if(debugHost.equals("")) {
							out("You cannot enter a blank IP.");
						} else {
							out("Debug host set!");
							debugLatch.countDown();
						}
						Core.debugServer = false;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Core.resetTable();
					} else {
						if(Core.peerList.size() == 0) {
							out("No peers connected. Query is not possible.");
						} else {
							String input = searchInput.getText();
							if(input.equals("")) {
								out("You cannot search for a blank query.");
							} else {
								if(!searchMode) {
									removeColumnAndData(searchRes, 0);
									tableModel.addColumn("Filename");
									tableModel.addColumn("Checksum");
									searchMode = true;
								}
								Utils.doSearch(input);
							}
							//dump results
						}
					}
					searchInput.setText("");
				}
			}
		});
		contentPane.setLayout(null);
		contentPane.add(searchInput);
		searchInput.setColumns(10);
		
		searchResScrollPane = new JScrollPane();
		searchResScrollPane.setBounds(12, 44, 512, 220);
		contentPane.add(searchResScrollPane);
		
		downloadScrollPane = new JScrollPane();
		downloadScrollPane.setBounds(12, 277, 512, 102);
		contentPane.add(downloadScrollPane);
		
		lblPeers = new JLabel("Peers: [0|0]");
		lblPeers.setBounds(476, 381, 60, 20);
		lblPeers.setFont(new Font("Tahoma", Font.PLAIN, 11));
		contentPane.add(lblPeers);
		
		downloadList = new JTable(downloadModel);
		downloadList.setIntercellSpacing(new Dimension(10, 0));
		downloadList.getTableHeader().setReorderingAllowed(false);
		downloadList.getTableHeader().setResizingAllowed(false);
		downloadScrollPane.setViewportView(downloadList);
		
		searchRes = new JTable(tableModel);
		searchRes.setIntercellSpacing(new Dimension(10, 0));
		searchRes.getTableHeader().setReorderingAllowed(false);
		searchRes.getTableHeader().setResizingAllowed(false);
		searchRes.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(searchMode) {
					Point clickPoint = arg0.getPoint();
					int tableRow = searchRes.rowAtPoint(clickPoint);
					if(arg0.getClickCount() == 2) {
						String fileName = (String) tableModel.getValueAt(tableRow, 0);
						String blockListStr = (String) tableModel.getValueAt(tableRow, 1);
						@SuppressWarnings("rawtypes")
						Iterator it = Core.index.entrySet().iterator();
						//Iterate through HashMap until a match by blockListStr is found
						while(it.hasNext()) {
					        @SuppressWarnings("rawtypes")
							Map.Entry pairs = (Map.Entry) it.next();
					        //TODO: use HashMap to contact known peers first
					        //Peer mapPeer = (Peer) pairs.getKey();
					        @SuppressWarnings("unchecked")
							ArrayList<String> blockList = (ArrayList<String>) pairs.getValue();
					        //Check to see if the HashMap's matching is accurate
					        if(blockList.toString().equals(blockListStr)) {
					        	BlockedFile bf;
					        	//Check if this BlockedFile exists
					        	if(Utils.getBlockedFile(blockList) != null) {
					        		bf = Utils.getBlockedFile(blockList);
					        		System.out.println(bf.getName());
					        	} else {
					        		//If not, create a new BlockedFile instance
					        		bf = new BlockedFile(fileName, blockList);
					        	}
					        	int numRows = downloadModel.getRowCount();
					        	boolean alreadyDoneInPane = false;
					        	for(int i = 0; i < numRows; i++) {
					        		if(downloadModel.getValueAt(i, 0).equals(bf.getName())) {
					        			if(downloadModel.getValueAt(i, 1).equals("100%")) {
					        				alreadyDoneInPane = true;
						        			break;
					        			}
					        		}
					        	}
					        	if(!alreadyDoneInPane) {
					        		downloadModel.addRow(new String[]{bf.getName(), "0%"});
						        	downloadList.getColumnModel().getColumn(1).setCellRenderer(new ProgressCellRenderer());
						        	bf.download();
					        	}
					        	Core.resetTable();
					        }
					        it.remove();
					    }
					}
				}
			}
		});
		searchResScrollPane.setViewportView(searchRes);
		searchRes.setCellSelectionEnabled(true);
		searchRes.setColumnSelectionAllowed(true);
		resLatch.countDown();
	}
	
	public void out(String str) {
		try {
			resLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(searchMode) {
			removeColumnAndData(searchRes, 1);
			removeColumnAndData(searchRes, 0);
			tableModel.addColumn("Status");
			searchMode = false;
		}
		clearTable();
		tableModel.addRow(new String[]{str});
	}
	
	public void clearTable() {
		for(int i=0; i < tableModel.getRowCount(); i++) {
			tableModel.removeRow(i);
		}
	}
	
	public void removeColumnAndData(JTable table, int vColIndex) {
	    TableModelSpec model = (TableModelSpec)table.getModel();
	    TableColumn col = table.getColumnModel().getColumn(vColIndex);
	    int columnModelIndex = col.getModelIndex();
	    Vector<?> data = model.getDataVector();
	    Vector<?> colIds = model.getColumnIdentifiers();
	    table.removeColumn(col);
	    colIds.removeElementAt(columnModelIndex);
	    for (int r=0; r<data.size(); r++) {
	        Vector<?> row = (Vector<?>)data.get(r);
	        row.removeElementAt(columnModelIndex);
	    }
	    model.setDataVector(data, colIds);
	    Enumeration<?> enumer = table.getColumnModel().getColumns();
	    for(;enumer.hasMoreElements();) {
	        TableColumn c = (TableColumn)enumer.nextElement();
	        if (c.getModelIndex() >= columnModelIndex) {
	            c.setModelIndex(c.getModelIndex()-1);
	        }
	    }
	    model.fireTableStructureChanged();
	}
	
	public void updatePeerCount() {
		lblPeers.setText("Peers: " + Core.peersCount());
	}
	
	public void updateProgress(String forFile, String progress) {
		int rowCount = downloadModel.getRowCount();
		for(int i=0; i < rowCount; i++) {
			if(downloadModel.getValueAt(i, 0).equals(forFile)) {
				downloadModel.setValueAt(progress, i, 1);
			}
		}
	}
	
	public void setDebugLatch(CountDownLatch debugLatch) {
		this.debugLatch = debugLatch;
	}
}
