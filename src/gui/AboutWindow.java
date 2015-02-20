package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

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

public class AboutWindow extends JDialog {
	private static final long serialVersionUID = -6122131329793766296L;
	private final JPanel contentPanel = new JPanel();

	/**
	 * Create the dialog.
	 */
	public AboutWindow() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 445, 180);
		setVisible(true);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblAboutXnet = new JLabel("About XNet");
		lblAboutXnet.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblAboutXnet.setBounds(10, 0, 75, 26);
		contentPanel.add(lblAboutXnet);
		
		JSeparator headerSeparator = new JSeparator();
		headerSeparator.setBounds(0, 24, 391, 2);
		contentPanel.add(headerSeparator);
		
		JLabel lblNewLabel = new JLabel("Icons by Freepik / CC BY 3.0");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblNewLabel.setBounds(10, 37, 164, 14);
		contentPanel.add(lblNewLabel);
		
			JButton iconsVisitSource = new JButton("Visit source");
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
			iconsVisitSource.setBounds(279, 35, 100, 18);
			iconsVisitSource.setFocusable(false);
			contentPanel.add(iconsVisitSource);
		
		JLabel lblThemeByFreepik = new JLabel("Theme by JackJiang2011 / Apache 2.0");
		lblThemeByFreepik.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblThemeByFreepik.setBounds(10, 56, 220, 14);
		contentPanel.add(lblThemeByFreepik);
		
			JButton themeVisitSource = new JButton("Visit source");
			themeVisitSource.setFont(new Font("Dialog", Font.PLAIN, 11));
			themeVisitSource.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						Utils.openLink(new URI("https://github.com/JackJiang2011/beautyeye"));
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			});
			themeVisitSource.setBounds(279, 54, 100, 18);
			themeVisitSource.setFocusable(false);
			contentPanel.add(themeVisitSource);
		
		JLabel lblGsonByGoogle = new JLabel("GSON by Google / Apache 2.0");
		lblGsonByGoogle.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblGsonByGoogle.setBounds(10, 75, 151, 14);
		contentPanel.add(lblGsonByGoogle);
		
			JButton gsonVisitSource = new JButton("Visit source");
			gsonVisitSource.setFont(new Font("Dialog", Font.PLAIN, 11));
			gsonVisitSource.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						Utils.openLink(new URI("https://code.google.com/p/google-gson"));
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			});
			gsonVisitSource.setBounds(279, 73, 100, 18);
			gsonVisitSource.setFocusable(false);
			contentPanel.add(gsonVisitSource);
		
		Label footerLabel = new Label("Copyright 2015 - Kevin Cai");
		footerLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
		footerLabel.setBounds(111, 137, 142, 15);
		contentPanel.add(footerLabel);
	}
}
