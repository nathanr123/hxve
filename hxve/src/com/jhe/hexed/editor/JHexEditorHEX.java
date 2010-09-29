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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

public class JHexEditorHEX extends JComponent implements MouseListener, KeyListener, MouseMotionListener{
	private static final long serialVersionUID = -238224296258897535L;
	
	private JHexEditor hexEdit;
    private int cursor = 0;

    public JHexEditorHEX(JHexEditor he)
    {
        this.hexEdit = he;
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        addFocusListener(he);
    }

    public Dimension getPreferredSize()
    {
        debug("getPreferredSize()");
        return getMinimumSize();
    }

    public Dimension getMaximumSize()
    {
        debug("getMaximumSize()");
        return getMinimumSize();
    }

    public Dimension getMinimumSize()
    {
        debug("getMinimumSize()");

        Dimension dimens = new Dimension();
        FontMetrics fn = getFontMetrics(hexEdit.font);
        int height = fn.getHeight();
        int numOfLines = hexEdit.getLines();
        dimens.setSize( ( (fn.stringWidth(" ") + 1) * ((16*3) - 1) ) + (hexEdit.border*2) + 1,
        					height * numOfLines + (hexEdit.border*2) + 1);
        return dimens;
    }

    public void paint(Graphics g)
    {
        debug("paint(" + g + ")");
        debug("cursor=" + hexEdit.cursorStart + " buff.length=" + hexEdit.fileLength);
        
        Dimension dimens = getMinimumSize();
        g.setColor(Color.white);
        g.fillRect(0,0,dimens.width,dimens.height);
        g.setColor(Color.black);

        g.setFont(hexEdit.font);

        int start = hexEdit.getStart() * 16;
        int end = start + (hexEdit.getLines() * 16);//Maximum "character" length of the HEX view component
        
        if (end > hexEdit.fileLength){
        	end = hexEdit.fileLength;
        }

        //hex data
        int x = 0;
        int y = 0;
        for(int n = start; n < end; n++){
        	
            if((n >= hexEdit.cursorStart && n<= hexEdit.cursorEnd)){
            	
                if(hasFocus()){
                    g.setColor(hexEdit.selBgColor1);
                    hexEdit.drawBackground(g, (x*3) ,y,2);
                    g.setColor(hexEdit.selFontColor);
                    hexEdit.drawBackground(g, (x*3) + cursor,y,1);
                    
                } else {
                    g.setColor(hexEdit.selBgColor1);
                    hexEdit.drawTable(g,(x*3),y,2);
                }

                if(hasFocus()){
                	//g.setColor(Color.white);
                	g.setColor(Color.DARK_GRAY);
                } else {
                	g.setColor(Color.black);
                }
                
            } else {
                g.setColor(Color.DARK_GRAY);
            }

            String s = ("0" + Integer.toHexString(hexEdit.buff.getByte(n)));
            s = s.substring(s.length() - 2);
            hexEdit.printString(g, s, ((x++)*3), y);
            if (x==16){
                x = 0;
                y++;
            }
        }
    }

    private void debug(String s)
    {
        if(hexEdit.DEBUG) System.out.println("JHexEditorHEX ==> "+s);
    }

    /**
     * Calculate the position of the cursor based on where the user clicked
     * @param x X-position of the mouse
     * @param y Y-position of the mouse
     * @return
     */
    public int calculateCursorPosition(int x,int y)
    {
        FontMetrics fn=getFontMetrics(hexEdit.font);
        x = x / ((fn.stringWidth(" ")+1)*3);
        y = y / fn.getHeight();
        debug("x="+x+" ,y="+y);
        return x + ((y + hexEdit.getStart()) * 16);
    }

    // mouselistener
    public void mouseClicked(MouseEvent e){
    	hexEdit.cursorStart = calculateCursorPosition(e.getX(),e.getY());
    	hexEdit.cursorStart = hexEdit.cursorEnd;
    	this.requestFocus();
        hexEdit.repaint();
    }
  
    public void mousePressed(MouseEvent e){
        hexEdit.cursorStart = calculateCursorPosition(e.getX(),e.getY());
        hexEdit.cursorEnd = hexEdit.cursorStart;
        this.requestFocus();
        hexEdit.repaint();
    }
 
    public void mouseReleased(MouseEvent e){
//    	hexEdit.updateSelectedTextArea();
    	hexEdit.hxve.visualisationPanel.setSelection(hexEdit.cursorStart, hexEdit.cursorEnd);
    }

    public void mouseEntered(MouseEvent e){
    }

    public void mouseExited(MouseEvent e){
    }

    //KeyListener
    public void keyTyped(KeyEvent e)
    {
        debug("keyTyped(" + e + ")");

        char c = e.getKeyChar();
        if (((c >= '0')&&(c <= '9')) || ((c >= 'A')&&(c <= 'F')) || ((c >= 'a')&&(c <= 'f'))) {

            char[] str = new char[2];
            String n = "00" + Integer.toHexString( (int)hexEdit.buff.getByte(hexEdit.cursorStart) );
            if(n.length() > 2){
            	n = n.substring(n.length() - 2);
            }
            str[1 - cursor] = n.charAt(1 - cursor);
            str[cursor] = e.getKeyChar();
            
            if (hexEdit.writable){
            	hexEdit.buff.writeByte(hexEdit.cursorStart, (byte)Integer.parseInt( new String(str), 16 ));
            } else {
            	System.out.println("Read only enabled!");
            }
            
            if (cursor != 1){
            	cursor = 1;
            } else if (hexEdit.cursorStart != (hexEdit.fileLength - 1)){
            	hexEdit.cursorStart++; hexEdit.cursorEnd++;
            	cursor = 0;
            }
            hexEdit.updateCursor();
        }
    }

    public void keyPressed(KeyEvent e)
    {
        debug("keyPressed("+e+")");
        hexEdit.keyPressed(e);
    }

    public void keyReleased(KeyEvent e)
    {
        debug("keyReleased("+e+")");
    }

    public boolean isFocusTraversable()
    {
        return true;
    }

	@Override
	public void mouseDragged(MouseEvent e) {
		hexEdit.cursorEnd = calculateCursorPosition(e.getX(),e.getY());
    	
        //Swap selection start and end if needed
        if (hexEdit.cursorStart > hexEdit.cursorEnd){
        	int tmp = hexEdit.cursorStart;
        	hexEdit.cursorStart = hexEdit.cursorEnd;
        	hexEdit.cursorEnd = tmp;
        }
    	
        this.requestFocus();
        hexEdit.repaint();
		
        //Now check if the mouse is at the bottom or top of the screen, in which case we will scroll down/up
        if (e.getY() > this.getHeight()){
        	hexEdit.scrollDown();
        }
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}
}
