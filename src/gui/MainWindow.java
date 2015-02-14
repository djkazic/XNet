package gui;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import main.Core;
import main.Utils;
import peer.Peer;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private JPanel contentPane;
	private JTextField searchInput;
	private JTable searchRes;
	public DefaultTableModel tableModel;
	private CountDownLatch resLatch;
	public CountDownLatch debugLatch;
	private JScrollPane scrollPane;
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
		setBounds(100, 100, 480, 320);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		tableModel = new TableModelSpec();
		tableModel.addColumn("Status");
		
		resLatch = new CountDownLatch(1);
		
		searchInput = new JTextField();
		searchInput.setBounds(10, 13, 454, 20);
		
		searchInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				int key = arg0.getKeyCode();
				if(key == KeyEvent.VK_ENTER) {
					//clear any previous res
					clearTable();
					//clear fileToHash matcher
					Core.fileToHash.clear();
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
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 44, 454, 233);
		contentPane.add(scrollPane);
		
		lblPeers = new JLabel("Peers: [0|0]");
		lblPeers.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblPeers.setBounds(406, 278, 58, 14);
		contentPane.add(lblPeers);
		
		searchRes = new JTable(tableModel);
		searchRes.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(searchMode) {
					Point clickPoint = arg0.getPoint();
					int tableRow = searchRes.rowAtPoint(clickPoint);
					if(arg0.getClickCount() == 2) {
						String md5sum = Core.fileToHash.get(tableRow)[1];
						//Go through hashtable and select peer
						@SuppressWarnings("rawtypes")
						Iterator it = Core.index.entrySet().iterator();
						while(it.hasNext()) {
					        @SuppressWarnings("rawtypes")
							Map.Entry pairs = (Map.Entry) it.next();
					        Peer mapPeer = (Peer) pairs.getKey();
					        String[] mapStrArr = (String[]) pairs.getValue();
					        if(mapStrArr[1].equals(md5sum)) {
					        	//mapPeer is the peer you want a transfer with
					        	mapPeer.st.requestTransfer(md5sum);
					        	out("");
					        	clearTable();
					        }
					        it.remove();
					    }
					}
				}
			}
		});
		scrollPane.setViewportView(searchRes);
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
	
	public void setDebugLatch(CountDownLatch debugLatch) {
		this.debugLatch = debugLatch;
	}
}
