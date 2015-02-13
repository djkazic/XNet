package gui;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class TableModelSpec extends DefaultTableModel {

	public boolean isCellEditable(int rowIndex,
            int columnIndex) {
		return false;
	}
	
	public Vector<?> getColumnIdentifiers() {
        return columnIdentifiers;
    }
}
