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
package com.jhe.hexed.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

import com.jhe.hexed.HexViewEditor;
import com.jhe.hexed.util.FileReader;

public class JHexEditor extends JPanel implements FocusListener, AdjustmentListener, MouseWheelListener{
	private static final long serialVersionUID = 3953672354857567284L;

	public FileReader buff;
	public int fileLength;
	
	public int cursorStart;
	public int cursorEnd;
	
	/*
	 * Whether we should allow writing to the file (can be enabled in menu system)
	 */
	public boolean writable = false;
	
	/*
	 * Gui settings
	 */
	public final Font font = new Font("Monospaced",0,14);
	public final Color selFontColor = Color.LIGHT_GRAY;
	public final Color selBgColor1 = new Color(153,204,255);
	public final Color selBgColor2 = new Color(204,204,255);
	public final int border = 8;
	
	public boolean DEBUG = false;
	private final JPanel panel;
	private final JScrollBar scrollbar;
	
	private final JHexEditorHEX hexEditHEX;
	private final JHexEditorASCII hexEditASCII;

	private int start = 0;
	private int lines = 20;

	public final HexViewEditor hxve; 
	/**
	 * Instantiates a new Hex editor component.
	 * @param jtxtSelectedBox The text area that will display the current selection
	 */
	public JHexEditor(HexViewEditor hxve){
		super();
		this.hxve = hxve;
		
		buff = new FileReader();
		fileLength = buff.getLengthInt();

		this.addMouseWheelListener(this);

		scrollbar = new JScrollBar(JScrollBar.VERTICAL);
		scrollbar.addAdjustmentListener(this);
		scrollbar.setMinimum(0);
		scrollbar.setMaximum(fileLength/getLines());

		JPanel p1,p2,p3;
		
		//center
		p1 = new JPanel(new BorderLayout(1,1));
		
		hexEditHEX = new JHexEditorHEX(this);
		p1.add(hexEditHEX, BorderLayout.CENTER);
		
		p1.add(new Columns(),BorderLayout.NORTH);

		// leftside (index)
		p2 = new JPanel(new BorderLayout(1,1));
		p2.add(new Rows(),BorderLayout.CENTER);
		p2.add(new Box(),BorderLayout.NORTH);

		// rightside (ascii)
		p3 = new JPanel(new BorderLayout(1,1));
		p3.add(scrollbar,BorderLayout.EAST);
		
		hexEditASCII = new JHexEditorASCII(this);
		p3.add(hexEditASCII, BorderLayout.CENTER);
		
		p3.add(new Box(),BorderLayout.NORTH);

		panel = new JPanel();
		panel.setLayout(new BorderLayout(1,1));
		panel.add(p1,BorderLayout.CENTER);
		panel.add(p2,BorderLayout.WEST);
		panel.add(p3,BorderLayout.EAST);

		this.setLayout(new BorderLayout(1,1));
		this.add(panel,BorderLayout.CENTER);
	}
	
	public void readFile(File file){
		if (file != null){
			buff.openFile(file);
		} else {
			System.out.println("File does not exist!");
		}
		fileLength = buff.getLengthInt();
	}

	/**
	 * Close current file
	 */
	public void close(){
		buff.closeFile();
	}

	public void paint(Graphics g){
		FontMetrics fn = getFontMetrics(font);
		Rectangle rec = this.getBounds();
		lines = ( rec.height / fn.getHeight() -1);
		
		int n = ((int) Math.ceil( (fileLength)/16 )); //Determine the needed height of hex view
		
		if (lines > n) {//Check if the contents is smaller then the preset number of lines to display
			lines = n+1;
			start = 0;
		}

		scrollbar.setValues(getStart(), getLines(), 0, ((int) Math.ceil( (fileLength)/16 ))+1 );
		scrollbar.setValueIsAdjusting(true);
		super.paint(g);
	}

	protected void updateCursor(){
		int n = (cursorStart/16);

		System.out.print("- " + start + "<" + n + "<" + (lines + start) + "(" + lines + ")");

		if(n < start){
			start = n;
		} else if(n >= start + lines){
			start = n - (lines - 1);
		}

		System.out.println(" - " + start + "<" + n + "<" + (lines + start) + "(" + lines + ")");

		repaint();
	}

	protected int getStart(){
		return start;
	}

	protected int getLines(){
		return lines;
	}

	protected void drawBackground(Graphics g,int x,int y,int s){
		FontMetrics fn = getFontMetrics(font);
		g.fillRect(((fn.stringWidth(" ")+1)*x) + border,
				(fn.getHeight()*y) + border,
				((fn.stringWidth(" ") + 1)*s),
				fn.getHeight() + 1);
	}

	protected void drawTable(Graphics g,int x,int y,int s){
		FontMetrics fn = getFontMetrics(font);
		g.setColor(Color.BLUE);
		g.drawRect(((fn.stringWidth(" ") + 1) *x) + border,
				(fn.getHeight()*y) + border,
				((fn.stringWidth(" ") + 1) *s),
				fn.getHeight() + 1);
		g.setColor(Color.BLACK);
	}

	protected void printString(Graphics g,String s,int x,int y){
		FontMetrics fn = getFontMetrics(font);
		g.drawString(s,((fn.stringWidth(" ")+1)*x)+border,
				((fn.getHeight()*(y+1))-fn.getMaxDescent())+border);
	}

	public void focusGained(FocusEvent e){
		this.repaint();
	}

	public void focusLost(FocusEvent e){
		this.repaint();
	}

	public void adjustmentValueChanged(AdjustmentEvent e){
		start = e.getValue();
		if(start < 0){
			start = 0;
		}
		repaint();
	}

	public void mouseWheelMoved(MouseWheelEvent e){
		start += (e.getUnitsToScroll());
		if ((start+lines) >= fileLength/16){
			start = (fileLength/16) - lines;
		}
		if (start < 0){
			start = 0;
		}
		repaint();
	}

	public void keyPressed(KeyEvent e){
		switch(e.getKeyCode())
		{
		case 33:    // rep
		if(cursorStart >= (16 * lines)){
			cursorStart -= (16 * lines);
		}
		updateCursor();
		break;
		case 34:    // fin
			if(cursorStart < (fileLength - (16 * lines))){
				cursorStart += (16 * lines);
			}
			updateCursor();
			break;
		case 35:    // fin
			cursorStart = fileLength - 1;
			updateCursor();
			break;
		case 36:    // ini
			cursorStart = 0;
			updateCursor();
			break;
		case 37:    // <--
			if(cursorStart != 0){
				cursorStart--;
			}
			updateCursor();
			break;
		case 38:    // <--
			if(cursorStart > 15){
				cursorStart -= 16;
			}
			updateCursor();
			break;
		case 39:    // -->
			if(cursorStart != (fileLength - 1)){
				cursorStart++;
			}
			updateCursor();
			break;
		case 40:    // -->
			if(cursorStart < (fileLength - 16)){
				cursorStart += 16;
			}
			updateCursor();
			break;
		}
	}

	private class Columns extends JPanel{
		private static final long serialVersionUID = -5618032261346192686L;

		public Columns(){
			this.setLayout(new BorderLayout(1,1));
		}
		public Dimension getPreferredSize(){
			return getMinimumSize();
		}

		public Dimension getMinimumSize(){
			Dimension d=new Dimension();
			FontMetrics fn = getFontMetrics(font);
			int h = fn.getHeight();
			int nl = 1;
			d.setSize(( (fn.stringWidth(" ")+1) * ((16*3)-1)) + (border * 2) + 1,
					h*nl + (border*2) + 1);
			return d;
		}

		public void paint(Graphics g){
			Dimension d=getMinimumSize();
			g.setColor(Color.white);
			g.fillRect(0,0,d.width,d.height);
			g.setColor(Color.black);
			g.setFont(font);

			for(int n=0;n<16;n++){
				if (n == (cursorStart%16)){
					drawTable(g,n*3,0,2);
				}
				String s = "00"+Integer.toHexString(n);
				s = s.substring(s.length()-2);
				printString(g,s,n*3,0);
			}
		}
	}

	private class Box extends JPanel{
		private static final long serialVersionUID = 9041295974168164450L;

		public Dimension getPreferredSize(){
			return getMinimumSize();
		}

		public Dimension getMinimumSize(){
			Dimension d=new Dimension();
			FontMetrics fn=getFontMetrics(font);
			int h=fn.getHeight();
			d.setSize((fn.stringWidth(" ")+1)+(border*2)+1,h+(border*2)+1);
			return d;
		}

	}

	private class Rows extends JPanel{
		private static final long serialVersionUID = -1151041768761982943L;

		public Rows(){
			this.setLayout(new BorderLayout(1,1));
		}
		public Dimension getPreferredSize(){
			return getMinimumSize();
		}

		public Dimension getMinimumSize(){
			Dimension dimens = new Dimension();
			FontMetrics fn = getFontMetrics(font);
			int h = fn.getHeight();
			int numLines = getLines();
			dimens.setSize((fn.stringWidth(" ") + 1)*8 + (border*2) + 1,
					h*numLines + (border*2) + 1);
			return dimens;
		}

		public void paint(Graphics g){
			Dimension dimens = getMinimumSize();
			g.setColor(Color.white);
			g.fillRect(0, 0, dimens.width, dimens.height);
			g.setColor(Color.black);
			g.setFont(font);

			int start = getStart();
			int end = start + getLines();
			int y = 0;
			for(int n = start; n < end; n++){
				
				if( (n >= (cursorStart/16)) && (n <= (cursorEnd/16))){
					drawTable(g, 0, y, 8);
				}
				String s = "00000000000" + Integer.toHexString(n) + "0";
				s = s.substring(s.length() - 9);
				printString(g, s, 0, y++);
			}
		}
	}
	
	public void scrollDown(){
		this.scrollbar.setValue(scrollbar.getValue() +1);
	}

}
