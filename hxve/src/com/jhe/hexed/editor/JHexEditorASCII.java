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

public class JHexEditorASCII extends JComponent implements MouseListener, KeyListener, MouseMotionListener{
	private static final long serialVersionUID = -237929190394378428L;
	
	private JHexEditor hexEdit;

    public JHexEditorASCII(JHexEditor hexEdit)
    {
        this.hexEdit = hexEdit;
        addMouseListener(this);
        addKeyListener(this);
        addMouseMotionListener(this);
        addFocusListener(hexEdit);
    }

    public Dimension getPreferredSize()
    {
        debug("getPreferredSize()");
        return getMinimumSize();
    }

    public Dimension getMinimumSize()
    {
        debug("getMinimumSize()");

        Dimension d=new Dimension();
        FontMetrics fn=getFontMetrics(hexEdit.font);
        int h=fn.getHeight();
        int nl=hexEdit.getLines();
        d.setSize((fn.stringWidth(" ")+1)*(16)+(hexEdit.border*2)+1,h*nl+(hexEdit.border*2)+1);
        return d;
    }

    public void paint(Graphics g)
    {
        debug("paint("+g+")");
        debug("cursor=" + hexEdit.cursorStart + " buff.length=" + hexEdit.buff.length);
        Dimension dimens = getMinimumSize();
        g.setColor(Color.white);
        g.fillRect(0, 0, dimens.width, dimens.height);
        g.setColor(Color.black);

        g.setFont(hexEdit.font);

        //Swap selection start and end if needed
        if (hexEdit.cursorStart > hexEdit.cursorEnd){
        	int tmp = hexEdit.cursorStart;
        	hexEdit.cursorStart = hexEdit.cursorEnd;
        	hexEdit.cursorEnd = tmp;
        }
        
        //ASCII data
        int start = hexEdit.getStart()*16;
        int end = start + (hexEdit.getLines()*16);
        if(end > hexEdit.buff.length){
        	end = hexEdit.buff.length;
        }

        int x = 0;
        int y = 0;
        for(int n = start;n<end;n++)
        {
            if (n >= hexEdit.cursorStart && n<= hexEdit.cursorEnd){
                g.setColor(Color.blue);
                if(hasFocus()){
                	hexEdit.drawBackground(g,x,y,1);
                } else {
                	g.setColor(hexEdit.selBgColor1);
                	hexEdit.drawTable(g,x,y,1);
                }
                if(hasFocus()){
                	g.setColor(Color.white);
                } else {
                	g.setColor(Color.black);
                }
                
            } else {
                g.setColor(Color.black);
            }

            String s = "" + new Character((char)hexEdit.buff.getByte(n));
            if((hexEdit.buff.getByte(n) < 20) || (hexEdit.buff.getByte(n) > 126)){
            	s = "" + (char)16;
            }
            hexEdit.printString(g, s, (x++), y);
            if(x == 16)
            {
                x = 0;
                y++;
            }
        }

    }

    private void debug(String s)
    {
        if(hexEdit.DEBUG) System.out.println("JHexEditorASCII ==> "+s);
    }

    public int calculateCursorPosition(int x, int y){
        FontMetrics fn=getFontMetrics(hexEdit.font);
        x=x/(fn.stringWidth(" ")+1);
        y=y/fn.getHeight();
        debug("x="+x+" ,y="+y);
        return x+((y+hexEdit.getStart())*16);
    }

    public void mouseClicked(MouseEvent e){
    	hexEdit.cursorStart = calculateCursorPosition(e.getX(),e.getY());
    	hexEdit.cursorStart = hexEdit.cursorEnd;
    	this.requestFocus();
        hexEdit.repaint();
    }

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
	}
	
    public void mousePressed(MouseEvent e){
        hexEdit.cursorStart = calculateCursorPosition(e.getX(),e.getY());
        hexEdit.cursorEnd = hexEdit.cursorStart;
        this.requestFocus();
        hexEdit.repaint();
    }

	public void mouseMoved(MouseEvent e) {
	}
	
    public void mouseReleased(MouseEvent e){
    }

    public void mouseEntered(MouseEvent e){
    }

    public void mouseExited(MouseEvent e){
    }

    //KeyListener
    public void keyTyped(KeyEvent e)
    {
        debug("keyTyped("+e+")");

        if (hexEdit.writable){
        	hexEdit.buff.writeByte(hexEdit.cursorStart, (byte)e.getKeyChar());
        } else {
        	System.out.println("Read only enabled!");
        }
        
        if(hexEdit.cursorStart != (hexEdit.buff.length-1)){
        	hexEdit.cursorStart++;
        }
        hexEdit.repaint();
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
}
