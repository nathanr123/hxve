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
package com.jhe.hexed.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class VisualisationPanel extends JPanel{
	private static final long serialVersionUID = 7317616250037774534L;

	private final FileReader fileReader;

	private final JPanel jpnlCanvas;//Drawing canvas
	private final JLabel jlblInfo;//Info on position in file of the mouse

	private int startOffset = 0;
	private int endOffset = 999999999;
	
	private int selStart;
	private int selEnd;
	
	@SuppressWarnings("serial")
	public VisualisationPanel(final FileReader fileReader){
		this.fileReader = fileReader;
		
		jlblInfo = new JLabel("Info");
		jpnlCanvas = new JPanel(){
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				BufferedImage img = null;
				if (VisualisationPanel.this.fileReader.isValid() && (startOffset < endOffset)){
					//Check to where we should read bytes
					int end = 0;
					
					//Limit the endoffset to what can be fitted on the screen
					if (endOffset < (startOffset + (width * getSize().height))){
						end = endOffset;
					} else {
						end = (startOffset + (width * getSize().height));
					}
					
					byte[] fromFile;
					if ((end-startOffset) < getSize().width){
						fromFile = VisualisationPanel.this.fileReader.getBytesPadded(startOffset, end, getSize().width, (byte)255);
					} else {
						fromFile = VisualisationPanel.this.fileReader.getBytes(startOffset, end);
					}
					img = ImageGenerator.getGrayscale(width, fromFile);
					g.drawImage(img, 0, 0, null);
					
					//Now draw transparant color over selected pixels
					colorBytes((Graphics2D) g, selStart, selEnd);
				}
			}
			
			public void colorBytes(Graphics2D g2, int selStart, int selEnd){
				Color color = new Color(1, 0, 0, 0.3f); //Red
				g2.setPaint(color);
				
				//Selection checks
				if (selStart < startOffset)
					selStart = startOffset;
				if (selEnd > endOffset)
					selEnd = endOffset;
				
				while (selStart < selEnd){
					int x1 = selStart % width;
					int y = (selStart - (selStart % width))/width;
					int x2 = (selEnd - selStart) >= width ? width : (selEnd % width);

					g2.drawLine(x1, y, x2, y);
					
					selStart += (width - x1);
				}
			}
		};

		this.setLayout(new BorderLayout());
		this.add(jpnlCanvas, BorderLayout.CENTER);
		this.add(jlblInfo, BorderLayout.SOUTH);

		jpnlCanvas.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				int location = (e.getY() * jpnlCanvas.getWidth()) + e.getX();
				
				if (location <= fileReader.length){
					jlblInfo.setText("Location: " + Integer.toHexString(location)
							+ " Value: " + Integer.toHexString(fileReader.getByte(location) & 0xFF));
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});
	}

	/**
	 * Sets the boundaries that will be used when querying the fileReader.
	 * @param startOffset
	 * @param endOffset
	 */
	public void setBoundaries(int startOffset, int endOffset){
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}
	
	public void setSelection(int selStart, int selEnd){
		this.selStart = selStart;
		this.selEnd = selEnd;
		this.repaint();
	}
	
	/**
	 * Sets a new width for the drawing
	 * @param width
	 */
	private int width = 400;
	public void setCanvasWidth(int width){
		this.width = width;
	}
	
	@Override
	public Dimension getMinimumSize(){
		return new Dimension(410,600);
	}
}
