package gui;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Label;
import java.awt.Font;

import javax.swing.JLabel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JSeparator;

import main.Utils;

import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("serial")
public class AboutWindow extends JDialog {
	protected final JPanel contentPanel = new JPanel();
	protected JLabel lblAboutXnet;
	protected JSeparator headerSeparator;
	protected JLabel lblNewLabel;
	protected JButton iconsVisitSource;
	protected JLabel lblBoon;
	protected JButton boonVisitSource;
	protected JLabel lblLogging;
	protected JButton loggingVisitSource;
	protected Label footerLabel;

	/**
	 * Create the dialog.
	 */
	public AboutWindow() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 400, 180);
		setVisible(true);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		lblAboutXnet = new JLabel("About XNet");
		lblAboutXnet.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblAboutXnet.setBounds(10, 0, 75, 26);
		contentPanel.add(lblAboutXnet);
		
		headerSeparator = new JSeparator();
		headerSeparator.setBounds(0, 24, 391, 2);
		contentPanel.add(headerSeparator);
		
		lblNewLabel = new JLabel("Icons by Freepik / CC BY 3.0");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblNewLabel.setBounds(10, 40, 164, 14);
		contentPanel.add(lblNewLabel);
		
			iconsVisitSource = new JButton("Visit source");
			iconsVisitSource.setFont(new Font("Dialog", Font.PLAIN, 11));
			iconsVisitSource.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						Utils.openLink(new URI("http://www.flaticon.com/packs/startup"));
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			});
			iconsVisitSource.setBounds(279, 38, 100, 18);
			iconsVisitSource.setFocusable(false);
			contentPanel.add(iconsVisitSource);
		
		lblBoon = new JLabel("Boon by Richard Hightower / Apache 2.0");
		lblBoon.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblBoon.setBounds(10, 67, 195, 14);
		contentPanel.add(lblBoon);
		
			boonVisitSource = new JButton("Visit source");
			boonVisitSource.setFont(new Font("Dialog", Font.PLAIN, 11));
			boonVisitSource.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						Utils.openLink(new URI("https://github.com/boonproject/boon"));
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			});
			boonVisitSource.setBounds(279, 65, 100, 18);
			boonVisitSource.setFocusable(false);
			contentPanel.add(boonVisitSource);
	
		lblLogging = new JLabel("Apache Commons Logging / Apache 2.0");
		lblLogging.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblLogging.setBounds(10, 95, 226, 14);
		contentPanel.add(lblLogging);
		
			loggingVisitSource = new JButton("Visit source");
			loggingVisitSource.setFont(new Font("Dialog", Font.PLAIN, 11));
			loggingVisitSource.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						Utils.openLink(new URI("http://commons.apache.org/proper/commons-logging/"));
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			});
			loggingVisitSource.setBounds(279, 92, 100, 18);
			loggingVisitSource.setFocusable(false);
			contentPanel.add(loggingVisitSource);
			
		//Footer
			
		footerLabel = new Label("Copyright 2015 - Kevin Cai");
		footerLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
		footerLabel.setBounds(122, 129, 142, 15);
		contentPanel.add(footerLabel);
	}
}
