package gui;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class TableModelSpec extends DefaultTableModel {

	public boolean isCellEditable(int rowIndex,
            int columnIndex) {
		return false;
	}
}
