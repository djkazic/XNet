package gui;

@SuppressWarnings("serial")
public class MacMainWindow extends MainWindow {

	/**
	 * Create the frame.
	 */
	public MacMainWindow() {
		super();
		setBounds(100, 100, 545, 470);
		searchInput.setBounds(10, 39, 519, 25);
		menuBar.setBounds(0, 0, 539, 22);
		searchResScrollPane.setBounds(10, 75, 519, 220);
		downloadScrollPane.setBounds(10, 306, 519, 102);
		separator.setBounds(0, 419, 546, 2);
		lblPeers.setBounds(479, 422, 60, 20);
	}

	public void aboutWindow() {
		new MacAboutWindow();
	}
}