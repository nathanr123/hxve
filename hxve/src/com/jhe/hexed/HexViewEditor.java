/* This file is part of HxVe.
 * 
 * HxVe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jhe.hexed;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jhe.hexed.editor.JHexEditor;
import com.jhe.hexed.util.VisualisationPanel;

public class HexViewEditor extends JFrame{
	
	private static final long serialVersionUID = -8086381669829672701L;

	/*
	 * Selection display
	 */
	final JTextArea jtxtSelectedBox;
	
	/*
	 * Hexedit component
	 */
	final JHexEditor hexEdit;
	
	/*
	 * Menu system
	 */
	final JMenu mnuFile;
	final JMenuItem mniOpenFile;
	final JMenuItem mniExit;
	
	final JMenu mnuEdit;
	final JCheckBoxMenuItem mniReadOnly;
	
	final JMenu mnuHelp;
	final JMenuItem mniAbout;
	
	/*
	 * Visualisation gui components
	 */
	final JPanel jpnlDrawingControls;
	public final VisualisationPanel visualisationPanel;
	final JScrollPane jscrlVisPanel;
	final JPanel jpnlDrawing;
	
	final JSpinner jspnStart;
	final SpinnerNumberModel spnmodStart;
	final JSpinner jspnEnd;
	final SpinnerNumberModel spnmodEnd;
	
	final SpinnerNumberModel spnmodCanvasWidth;
	final JSpinner jspnCanvasWidth;
	
	final JLabel jlblStartOffset;
	final JLabel jlblEndOffset;
	
	final JLabel jlblZoom;
	final JButton jbtnZoomIn;
	final JButton jbtnZoomOut;
	
	final JLabel jlblWidth;
	
	/**
	 * Main window
	 */
	public HexViewEditor(){
		
    	jtxtSelectedBox = new JTextArea();
    	jtxtSelectedBox.setLineWrap(true);
    	jtxtSelectedBox.setFont(new Font("Courier", Font.PLAIN, 10));
    	
    	hexEdit = new JHexEditor(this);

    	//Spinner for selection
    	spnmodStart = new SpinnerNumberModel(0, 0, 0, 1);
    	spnmodEnd = new SpinnerNumberModel(0, 0, 0, 1);
    	jspnStart = new JSpinner(spnmodStart);
    	jspnEnd = new JSpinner(spnmodEnd);
    	spnmodCanvasWidth = new SpinnerNumberModel(400,0,10000,1);
    	jspnCanvasWidth = new JSpinner(spnmodCanvasWidth);
    	
    	//File menu
    	mnuFile = new JMenu("File");
    	mnuFile.setMnemonic('F');
    	mniOpenFile = new JMenuItem("Open File...");
    	mniOpenFile.setMnemonic('O');
    	mnuFile.add(mniOpenFile);
    	mniExit = new JMenuItem("Exit");
    	mniExit.setMnemonic('x');
    	mnuFile.add(mniExit);
    	//Edit menu
    	mnuEdit = new JMenu("Edit");
    	mnuFile.setMnemonic('E');
    	mniReadOnly = new JCheckBoxMenuItem("Read Only", true);
    	mniReadOnly.setMnemonic('R');
    	mnuEdit.add(mniReadOnly);
    	//Help menu
    	mnuHelp = new JMenu("Help");
    	mnuHelp.setMnemonic('H');
    	mniAbout = new JMenuItem("About...");
    	mniAbout.setMnemonic('A');
    	mnuHelp.add(mniAbout);
    	
    	// Menu actions
    	mniReadOnly.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				hexEdit.writable = !mniReadOnly.isSelected();
			}
    	});
    	mniExit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
            	hexEdit.close();
            	System.out.println("Closing file");
            	System.exit(0);
			}
		});
    	mniOpenFile.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				if (fc.showOpenDialog(HexViewEditor.this) == JFileChooser.APPROVE_OPTION){
					hexEdit.readFile(fc.getSelectedFile());
					spnmodStart.setMaximum(hexEdit.fileLength);
					spnmodEnd.setMaximum(hexEdit.fileLength);
					spnmodEnd.setValue(hexEdit.fileLength);
					visualisationPanel.repaint();
				}
			}
		});
    	mniAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HexViewEditor.this.visualisationPanel.setSelection(100, 500);
//				JOptionPane.showMessageDialog(null,
//					    "HxVe\nJava HexViewEditor\n" +
//					    "by Klaas De Craemer (2010)\n\n" +
//					    "Based on HEX component code by German Laullon (2003)");

			}
		});
    	
    	/*
    	 * Drawing panel
    	 */
    	jpnlDrawingControls = new JPanel(new FlowLayout());
    	visualisationPanel = new VisualisationPanel(hexEdit.buff);//Drawing area for visualisation
    	jscrlVisPanel = new JScrollPane(visualisationPanel);
    	jpnlDrawing = new JPanel(new GridLayout(2, 1));
    	
    	jlblStartOffset = new JLabel("Start:");
    	jpnlDrawingControls.add(jlblStartOffset);
    	jspnStart.setPreferredSize(new Dimension(100,21));
    	jpnlDrawingControls.add(jspnStart);
    	jlblEndOffset = new JLabel("End:");
    	jpnlDrawingControls.add(jlblEndOffset);
    	jspnEnd.setPreferredSize(new Dimension(100,21));
    	jpnlDrawingControls.add(jspnEnd);

    	jlblZoom = new JLabel("Zoom:");
    	jpnlDrawingControls.add(jlblZoom);
    	jbtnZoomIn = new JButton("+");
    	jpnlDrawingControls.add(jbtnZoomIn);
    	jbtnZoomOut = new JButton("-");
    	jpnlDrawingControls.add(jbtnZoomOut);
    	
    	jlblWidth = new JLabel("Image Width:");
    	jpnlDrawingControls.add(jlblWidth);
    	jpnlDrawingControls.add(jspnCanvasWidth);
    	
    	jpnlDrawing.add(jpnlDrawingControls, BorderLayout.NORTH);
    	jpnlDrawing.add(jscrlVisPanel, BorderLayout.CENTER);
    	
    	/*
    	 * Spinner change handlers
    	 */
    	jspnStart.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int start = (Integer) jspnStart.getValue();
				int end = (Integer) jspnEnd.getValue();
				
				//Check that we are not running over the jspnEnd value
				if (start > (end-1)){
					jspnEnd.setValue(start);
				}
				
				//"Upload" values to the visualisation component
				visualisationPanel.setBoundaries((Integer) jspnStart.getValue(), (Integer) jspnEnd.getValue());
				visualisationPanel.repaint();
			}
		});
    	
    	jspnEnd.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int start = (Integer) jspnStart.getValue();
				int end = (Integer) jspnEnd.getValue();
				
				//Check that we are not running under the jspnStart value
				if (end < (start+1)){
					jspnStart.setValue(end);
				}
				
				//"Upload" values to the visualisation component
				visualisationPanel.setBoundaries((Integer) jspnStart.getValue(), (Integer) jspnEnd.getValue());
				visualisationPanel.repaint();
			}
		});
    	jspnCanvasWidth.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				
				
				//"Upload" values to the visualisation component
				visualisationPanel.setCanvasWidth((Integer) jspnCanvasWidth.getValue());
				visualisationPanel.repaint();
			}
		});
    	
        /*
         * Window closing handler
         */
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	setVisible(false);
            	hexEdit.close();
            	System.exit(0);
            }
          });
        
        /*
         * Add all components to the window
         */
        JSplitPane jspltMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, hexEdit, jpnlDrawing);
        jspltMain.setEnabled(false);
        getContentPane().add(jspltMain);
        
    	JMenuBar menubar = new JMenuBar();
    	setJMenuBar(menubar);
    	menubar.add(mnuFile);
    	menubar.add(mnuEdit);
    	menubar.add(mnuHelp);

        /*
         * Show window
         */
        pack();
        setTitle("HxVe");
        setSize(1100, 600);
        setVisible(true);        

    }

    public static void main(String arg[]) throws IOException {
    	new HexViewEditor();
    }
}
