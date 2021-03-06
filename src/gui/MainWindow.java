package gui;

import gui.render.ProgressCellRenderer;
import gui.render.TableModelDL;
import gui.render.TableModelSpec;

import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import main.Core;
import main.Settings;
import main.Utils;
import blocks.BlockedFile;

import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private JPanel contentPane;
	private JTextField searchInput;
	private JTable searchRes;
	private JTable downloadList;
	private DefaultTableModel searchModel;
	private DefaultTableModel libraryModel;
	private DefaultTableModel downloadModel;
	private DefaultTableCellRenderer betterRenderer;
	private CountDownLatch resLatch;
	private JScrollPane searchResScrollPane;
	private JScrollPane downloadScrollPane;
	private JSeparator separator;
	private JLabel lblPeers;
	private JMenuItem mntmAbout;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JPopupMenu downloadPopupMenu;
	private boolean searchMode;
	private JMenuItem downloadPopupMenuRemoveFromList;
	private JTabbedPane tabbedPane;
	private JScrollPane libraryScrollPane;
	private JTable libraryTable;
	
	public CountDownLatch debugLatch;
	public String debugHost;

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		if(!Settings.daemon) {
			setResizable(false);
			searchMode = false;
			setTitle("XNet v" + Settings.version);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setBounds(100, 100, 550, 570);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			
			//Set title icon
			try {
				Image iconImage = ImageIO.read(getClass().getResourceAsStream("/res/imgres/titleicon.png"));
				setIconImage(iconImage);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			searchModel = new TableModelSpec();
			searchModel.addColumn("Status");
			
			downloadModel = new TableModelDL();
			downloadModel.addColumn("Filename");
			downloadModel.addColumn("Progress");
			
			libraryModel = new TableModelSpec();
			libraryModel.addColumn("Filename");
			libraryModel.addColumn("Size");
			libraryModel.addColumn("Date");
			
			resLatch = new CountDownLatch(1);
			
			searchInput = new JTextField();
			searchInput.setBounds(8, 39, 516, 25);
			searchInput.setColumns(10);
			searchInput.setFocusable(false);
			searchInput.setEditable(false);
			contentPane.add(searchInput);
			
			contentPane.setLayout(null);
			
			menuBar = new JMenuBar();
			menuBar.setBounds(0, -4, 546, 30);
			contentPane.add(menuBar);
			
			mnFile = new JMenu("Help");
			menuBar.add(mnFile);

			mntmAbout = new JMenuItem("About");
			try {
				ImageIcon imageIcon = new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/res/imgres/glasses.png")));
				mntmAbout.setIcon(imageIcon);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			mntmAbout.setHorizontalAlignment(SwingConstants.LEFT);
			mnFile.add(mntmAbout);
			
			downloadScrollPane = new JScrollPane();
			downloadScrollPane.setBounds(6, 346, 520, 149);
			contentPane.add(downloadScrollPane);
			
			separator = new JSeparator();
			separator.setBounds(0, 507, 532, 2);
			contentPane.add(separator);
			
			lblPeers = new JLabel("");
			lblPeers.setToolTipText("[0|0]");
			lblPeers.setIcon(new ImageIcon(MainWindow.class.getResource("/res/imgres/0bars.png")));
			lblPeers.setBounds(508, 515, 24, 24);
			lblPeers.setFont(new Font("Tahoma", Font.PLAIN, 11));
			contentPane.add(lblPeers);
			betterRenderer = new DefaultTableCellRenderer();
			betterRenderer.setHorizontalAlignment(SwingConstants.CENTER);
			
			downloadPopupMenu = new JPopupMenu();
			downloadPopupMenuRemoveFromList = new JMenuItem("Remove from list");
			downloadPopupMenu.add(downloadPopupMenuRemoveFromList);

			downloadList = new JTable(downloadModel);
			downloadList.getColumnModel().getColumn(0).setCellRenderer(betterRenderer);
			downloadList.getColumnModel().getColumn(1).setCellRenderer(betterRenderer);
			downloadList.getTableHeader().setReorderingAllowed(false);
			downloadList.getTableHeader().setResizingAllowed(false);
			downloadScrollPane.setViewportView(downloadList);
			
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.setBounds(6, 75, 520, 260);
			contentPane.add(tabbedPane);
			
			searchResScrollPane = new JScrollPane();
			tabbedPane.addTab("Search", null, searchResScrollPane, null);
			
			searchRes = new JTable(searchModel);
			searchRes.setDefaultRenderer(Object.class, betterRenderer);
			//.getColumn(0).setCellRenderer(betterRenderer);
			searchRes.getTableHeader().setReorderingAllowed(false);
			searchRes.getTableHeader().setResizingAllowed(false);
			searchResScrollPane.setViewportView(searchRes);
			searchRes.setCellSelectionEnabled(true);
			searchRes.setColumnSelectionAllowed(true);
			
			libraryScrollPane = new JScrollPane();
			tabbedPane.addTab("Library", null, libraryScrollPane, null);
			
			libraryTable = new JTable(libraryModel);
			libraryTable.getColumnModel().getColumn(0).setPreferredWidth(300);
			libraryTable.getTableHeader().setReorderingAllowed(false);
			libraryTable.getTableHeader().setResizingAllowed(false);
			libraryScrollPane.setViewportView(libraryTable);
			
			registerListeners();
		}
	}
	
	public void registerListeners() {
		downloadPopupMenuRemoveFromList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int[] selectedRows = downloadList.getSelectedRows();
				for(Integer i : selectedRows) {
					downloadModel.removeRow(i);
				}
			}
		});
		
		downloadList.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent arg0) {
				if(arg0.isPopupTrigger()) {
					Point clickPoint = arg0.getPoint();
					int tableRow = downloadList.rowAtPoint(clickPoint);
					if(!downloadList.isRowSelected(tableRow)) {
						downloadList.changeSelection(tableRow, 0, false, false);
					}
					downloadPopupMenu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
				}
			}
		});
		
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new AboutWindow();
			}
		});
		
		searchInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				int key = arg0.getKeyCode();
				if(key == KeyEvent.VK_ENTER) {
					//clear any previous res
					clearTable(searchModel);
					//clear core index
					Core.index.clear();
					if(Settings.debugServer) {
						debugHost = searchInput.getText();
						if(debugHost.equals("")) {
							out("You cannot enter a blank IP.");
						} else {
							out("Debug host set!");
							debugLatch.countDown();
						}
						Settings.debugServer = false;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						resetTable();
					} else {
						if(Core.peerList.size() == 0) {
							out("No peers connected. Query is not possible.");
						} else {
							String input = searchInput.getText();
							if(input.equals("")) {
								out("You cannot search for a blank query.");
							} else if(input.length() < 3) {
								out("You cannot search for a query shorter than 3 characters.");
							} else {
								if(!searchMode) {
									removeColumnAndData(searchRes, 0);
									searchModel.addColumn("Filename");
									searchModel.addColumn("Size");
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
		
		libraryTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				Point clickPoint = arg0.getPoint();
				int tableRow = libraryTable.rowAtPoint(clickPoint);
				if(arg0.getClickCount() == 2) {
					System.out.println("DUB CLICKED");
					String fileName = (String) libraryModel.getValueAt(tableRow, 0);
					BlockedFile bf = Utils.getBlockedFileByName(fileName);
					bf.open();
				}
			}
		});
		
		searchRes.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(searchMode) {
					Point clickPoint = arg0.getPoint();
					int tableRow = searchRes.rowAtPoint(clickPoint);
					if(arg0.getClickCount() == 2) {
						String fileName = (String) searchModel.getValueAt(tableRow, 0);
						@SuppressWarnings("rawtypes")
						Iterator it = Core.index.entrySet().iterator();
						//Iterate through HashMap until a match by blockListStr is found
						while(it.hasNext()) {
					        @SuppressWarnings("rawtypes")
							Map.Entry pairs = (Map.Entry) it.next();
					        //TODO: use HashMap to contact known peers first
					        String tableFileName = (String) pairs.getKey();
							ArrayList<String> blockList = (ArrayList<String>) pairs.getValue();
					        //Check to see if the HashMap's matching is accurate
					        if(tableFileName.equals(fileName)) {
					        	BlockedFile bf;
					        	//Check if this BlockedFile exists
					        	if(Utils.getBlockedFileByBlockList(blockList) != null) {
					        		bf = Utils.getBlockedFileByBlockList(blockList);
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
					        	resetTable();
					        }
					        it.remove();
					    }
					}
				}
			}
		});
		resLatch.countDown();
		setVisible(true);
	}
	
	private void clearTable(DefaultTableModel tableModel) {
		for(int i=0; i < tableModel.getRowCount(); i++) {
			tableModel.removeRow(i);
		}
	}
	
	private void removeColumnAndData(JTable table, int vColIndex) {
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
	
	public void out(String str) {
		if(!Settings.daemon) {
			try {
				resLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(searchMode) {
				removeColumnAndData(searchRes, 1);
				removeColumnAndData(searchRes, 0);
				searchModel.addColumn("Status");
				searchMode = false;
			}
			clearTable(searchModel);
			searchModel.addRow(new String[]{str});
		}
	}

	public void setDebugLatch(CountDownLatch debugLatch) {
		this.debugLatch = debugLatch;
	}
	
	public void updatePeerCount() {
		if(!Settings.daemon) {
			String peers = Utils.peersCount();
			lblPeers.setIcon(new ImageIcon(MainWindow.class.getResource("/res/imgres/" + peers + ".png")));
			lblPeers.setToolTipText(Utils.peerToolTip());
		}
	}
	
	public void updateProgress(String forFile, String progress) {
		if(!Settings.daemon) {
			int rowCount = downloadModel.getRowCount();
			for(int i=0; i < rowCount; i++) {
				if(downloadModel.getValueAt(i, 0).equals(forFile)) {
					downloadModel.setValueAt(progress, i, 1);
				}
			}
		}
	}
	
	public void updateLibrary() {
		if(!Settings.daemon) {
			clearTable(libraryModel);
			for(BlockedFile bf : Core.blockDex) {
				if(bf.completed) {
					String fileEstimateStr = "";
					long fileEstimateKb = bf.getFileSize() / 1000;
					if(fileEstimateKb > 1000) {
						double fileEstimateMb = (fileEstimateKb / 1000D);
						fileEstimateStr += fileEstimateMb + "MB";
					} else {
						fileEstimateStr += fileEstimateKb+ "KB";
					}
					libraryModel.addRow(new String[]{bf.getName(), fileEstimateStr, bf.getDateModified()});
				}
			}
		}
	}
	
	public void setSearchFocusable() {
		if(!Settings.daemon) {
			searchInput.setFocusable(true);
		}
	}
	
	public void setSearchEditable() {
		if(!Settings.daemon) {
			searchInput.setEditable(true);
		}
	}

	public void resetTable() {
		if(!Settings.daemon) {
			out("Enter your search query and press Enter.");
		}
	}
	
	public void addRowToSearchModel(String[] info) {
		if(!Settings.daemon) {
			searchModel.addRow(info);
		}
	}
}
