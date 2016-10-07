/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package albumappnew;

import java.awt.Color;
import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 *
 * @author tararamanan
 */
public class PhotoComponent extends JComponent implements MouseListener, MouseMotionListener, KeyListener{

public static boolean flipped;
private BufferedImage image;
private boolean drawingMode = false;
private boolean textMode = false;



private int prevX, prevY;     // The previous location of the mouse. 

ArrayList<ArrayList<ArrayList<Integer>>> drawingLines = new ArrayList(); //drawing lines is a list of eachLine's
ArrayList<ArrayList<Integer>> eachLine = new ArrayList(); 

ArrayList<Integer[]> rects = new ArrayList();
ArrayList<String> postItTexts = new ArrayList();

private int boundLeft;
private int boundTop;
private int boundRight;
private int boundBottom;

private int rectX,rectY, rectXEnd, rectYEnd;
private StringBuffer currentPostItText;
private int charInputX, charInputY;

private boolean isDrawing = false;
private boolean newline = true;

private boolean textRectAssociationPending = false;


//constructor
public PhotoComponent()
{ 
    //call superclass constructor
    super();
    
    flipped = false;
    setPreferredSize( new Dimension( 500, 350 ) );
    setBackground(Color.black);
    
    addMouseListener(this);
    addMouseMotionListener(this);
    addKeyListener(this);
    
    
}

public void paintComponent(Graphics g)
{
    super.paintComponent(g); 
    g.setColor(Color.black);

    if(flipped == true)
    {
        //draw white image
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int imageX = image.getMinX();
        int imageY = image.getMinY();
        g.setColor(Color.white);
        g.fillRect(imageX, imageY, imageWidth, imageHeight);
        
        boundLeft = imageX;
        boundRight = imageX + imageWidth;
        boundTop = imageY;
        boundBottom = imageY+imageHeight;
           
        //draw any old annotations/drawings
        redrawStoredDrawings(g);
        
        //draw any old postits?
        redrawStoredText(g);
    }
    else
    {
        g.drawImage(image, 0, 0, null);
    }
  
    
}

public void loadPhoto(File filename)
{
    try {                
          image = ImageIO.read(filename);
          System.out.println("read in image" + image.toString());
          
          setPreferredSize( new Dimension( image.getWidth(), image.getHeight() ) );
       } catch (IOException ex) {
            System.out.println("Error while trying to read in image!");
       }
    repaint();
    revalidate();
}

public void mouseClicked(MouseEvent event)
{
  if (event.getClickCount() == 2) 
  {
      flip();
  }
  
  
}

public void flip()
{
    if(flipped) //back is shown
    {
        flipped = false;
        currentPostItText = new StringBuffer("");   
    }
    else
        flipped = true;
    repaint();
}

    private void redrawStoredDrawings(Graphics graphics) 
    {
        setupDrawingGraphics(graphics);
        graphics.setColor(Color.black);

        int pX=0, pY=0;
        boolean isFirstElem;
        for(ArrayList<ArrayList<Integer>> innerList : drawingLines) 
        {
            isFirstElem = true;
            for(ArrayList<Integer> numberCoord : innerList) 
            {
                //for first element just set the prev vals n continue
                if(isFirstElem)
                { 
                    pX = numberCoord.get(0);
                    pY = numberCoord.get(1);
                    isFirstElem = false;
                }
                else
                {
                    graphics.drawLine(pX,pY,numberCoord.get(0),numberCoord.get(1));
                    pX = numberCoord.get(0);
                    pY = numberCoord.get(1);
                    
                }
            }
        }

    }

    private void redrawStoredText(Graphics graphicsForDrawing) 
    {
        
        for(int i =0; i<rects.size(); i++)
        {
            Integer[] coord = rects.get(i);
            String text = postItTexts.get(i);
            graphicsForDrawing.setColor(Color.green);
            graphicsForDrawing.fillRect(coord[0], coord[1], coord[2]-coord[0], coord[3]-coord[1]);
            
            charInputX = coord[0];
            charInputY = coord[1];
            newline = true;
            
            for(char c: text.toCharArray())
            {
                writeTextInRect(graphicsForDrawing, c,coord);
            }
        }
        
    }
    
 
    public void mousePressed(MouseEvent e)
    {
        int x = e.getX();
        int y = e.getY();

        prevX = x;
        prevY = y;
        
        //setup drawing graphics
        setupDrawingGraphics(getGraphics());

        if(drawingMode && flipped)
        {
            //add this to a new array list.
            ArrayList<Integer> tempList = new ArrayList();
            tempList.add(x);
            tempList.add(y);

        }
        
        if(textMode && flipped)
        {
           rectX = x;
           rectY = y;
        }
        //append prev text to string--trying out!
        if(currentPostItText != null)
        {
                //check if an association is pending with a rect if yes:
                if(textRectAssociationPending)
                {
                    postItTexts.add(currentPostItText.toString()); //exception!
                    textRectAssociationPending = false;
                }
        }

    }
    
    public void mouseDragged(MouseEvent e)
    {
        isDrawing = true;
        int x = e.getX();
        int y = e.getY();
        
        Graphics graphics = getGraphics();

        if(drawingMode && flipped)
        {
            if(Math.abs(x-prevX)>0 || Math.abs(y-prevY)>0)
            {
                graphics.setColor(Color.black);
                if(x<= boundRight && x >= boundLeft && y>= boundTop && y<= boundBottom)
                    graphics.drawLine(prevX, prevY, x, y);

                //add this to a new array list.
                ArrayList<Integer> tempList = new ArrayList();
                tempList.add(x);
                tempList.add(y);
                eachLine.add(tempList);
            }
            prevX = x;
            prevY = y;
        }
        
        if(textMode && flipped)
        {
            //clear previous rectangle
            //graphics.clearRect(rectX, rectY, WIDTH, HEIGHT);
            if(x<= boundRight && x >= boundLeft && y>= boundTop && y<= boundBottom)
            {
                if(x<prevX)
                    x = prevX;
                if(y<prevY)
                    y = prevY;
                graphics.setColor(Color.green);
                graphics.fillRect(rectX, rectY, x-rectX, y-rectY);

                prevX = x;
                prevY =y;
            }
        }

    }

    public void mouseReleased(MouseEvent e)
    {

        if(drawingMode && flipped)
        {         
            if(isDrawing)
            {
                drawingLines.add((ArrayList<ArrayList<Integer>>) eachLine.clone());
                //eachLine.
                eachLine.clear();
                isDrawing = false;
            }
        }
        
        if(textMode && flipped)
        {
            rectXEnd = prevX;
            rectYEnd = prevY;
            if(isDrawing)
            {
                rects.add(new Integer[]{rectX, rectY, rectXEnd, rectYEnd});
                isDrawing = false;
                
                //create stringbuffer for text
                currentPostItText = new StringBuffer("");
                charInputX = rectX;
                charInputY = rectY;

                newline = true;
                
                textRectAssociationPending = true;
            }   
        }

    }

    private void setupDrawingGraphics(Graphics graphics) 
    {
        graphics = (Graphics2D) getGraphics();
        if(drawingMode)
            graphics.setColor(Color.black);
        if(textMode)
            graphics.setColor(Color.green);
    }
    
    private void writeTextInRect(Graphics graphics, char inputChar, Integer[] lastRectDim)
    {
        int x1 = lastRectDim[0];
        int y1 = lastRectDim[1];
        int x2 = lastRectDim[2];
        int y2 = lastRectDim[3];
        
        graphics.setColor(Color.black);
        
        FontMetrics metrics = graphics.getFontMetrics();
        int fontHeight = metrics.getMaxAscent();
        int fontLeading = metrics.getLeading();
        int fontDescent = metrics.getMaxDescent();
        int charWidth = metrics.charWidth(inputChar);
        
        if(newline) //to start, check if newline is true and reposition
        {
            charInputX = x1 + 3;
            charInputY = charInputY + fontHeight + fontLeading+fontDescent;
            
            if(charInputY + fontDescent + fontLeading > y2)
            {   
                if(charInputY + fontDescent+fontLeading<=boundBottom)
                {
                    //increasePostItHeight(x1,y2,x2,y2+3+maxascent+maxdescent
                    graphics.setColor(Color.green);
                    graphics.fillRect(x1, y2, x2-x1, fontHeight+fontDescent+fontLeading);

                    //modify rect value in array
                    rects.remove(rects.size()-1);
                    lastRectDim[3] = fontHeight+fontDescent+fontLeading + y2;
                    rects.add(lastRectDim);
                }
            }
            newline = false;
        }
        
        //output the character
        graphics.setColor(Color.black);
        if(charInputY+fontDescent<=boundBottom)
            graphics.drawString(String.valueOf(inputChar) , charInputX, charInputY);
        
        //set position for next input character
        charInputX = charInputX + charWidth;
        
        
        //see where next input char will go. if it is too close to the edge, set newline to true
        if(charInputX+charWidth>=x2)
            newline = true;

        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        return;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        return;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        return;
    }

    void setDrawingMode(boolean b) 
    {
        drawingMode = b;
        textMode = !b;
        this.setFocusable(false);
    }
    
    void setTextMode(boolean b) 
    {
        textMode = b;
        drawingMode = !b;
        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char lastChar = e.getKeyChar();
        currentPostItText.append(lastChar);
        
        writeTextInRect(getGraphics(), lastChar,rects.get(rects.size()-1));
    
    }

    @Override
    public void keyPressed(KeyEvent e) {
        return;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        return;
    }
    
    public boolean getFlipped()
    {
        return flipped;
    }
    


}



