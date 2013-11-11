package HTMLrenderer;

import java.util.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;


import javax.imageio.ImageIO;

class PictureCreator {
	
	private int[] coord; 
	private int picWidth, picHeight;
	
	private int leftmargin;
	private int topmargin;
	private int rightedge;
	private int bottomedge;
			
	private Color bgcolor;
	private String fontfamily;
		
	private Graphics2D g;
	private ContentCreator contentcreator;
	private CurrentPage cp;
	private TextContainer tc;
	
	private static LinkedList<FontStatus> fontstatuslist;
	
	
	PictureCreator(String filename, int w, int h) {
		picWidth=w;
		picHeight=h;
		
		bgcolor=Color.WHITE;
		coord = new int[2];
		
		fontstatuslist = new LinkedList<FontStatus>();
		FontStatus defaultFontStatus = new FontStatus();
		defaultFontStatus.setFontsize(12);
		defaultFontStatus.setTextcolor(Color.BLACK);
		fontstatuslist.add(defaultFontStatus);
		
		cp = new CurrentPage();
		rightedge=g.getClipBounds().width;
		bottomedge=g.getClipBounds().height;
		contentcreator = new ContentCreator();
		cp.setCurrentPageName(new File(filename).getName());
		tc = new TextContainer();
		
		File dir = new File(filename+".render");
		dir.mkdir();
		cp.setFilename(filename);
	}
	
	static class FontStatus implements Cloneable {
		private Color textcolor;
		private int fontsize;
		private boolean italic;
		private boolean underlined;
		private boolean bold;
			
		public FontStatus clone(){
			FontStatus copy=null;
			try {
				copy = (FontStatus) super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			copy.setTextcolor(textcolor);
			
			return copy;
			
		}
		
		boolean getI(){
			return italic;
		}
		
		boolean getU(){
			return underlined;
		}
		
		boolean getB(){
			return bold;
		}
		
		void setTextcolor(Color textcolor) {
			this.textcolor = textcolor;
		}
		void setFontsize(int fontsize) {
			this.fontsize = fontsize;
		}
		void setUnderlined(boolean underlined) {
			this.underlined = underlined;
		}
		void setBold(boolean bold) {
			this.bold = bold;
		}
		
		void setItalic(boolean italic) {
			this.italic = italic;
		}
		
		Color getTextcolor() {
			return textcolor;
		}
		int getFontsize() {
			return fontsize;
		}
		
		public void useStatus(Graphics2D g){
			if (textcolor!=null)
				g.setColor(textcolor);
			else {
				Iterator<FontStatus> li = fontstatuslist.descendingIterator();
				while (true) {
					Color c=li.next().getTextcolor();
					if (c != null) {
						g.setColor(c);
						break;
					}
				}

			}

			g.setFont(g.getFont().deriveFont((float) fontsize));
		}
	}
	
	
	class ContentCreator{
		class Content {
			private int page;
			private int type;
			private String header;
			
			Content(int page, int type, String header) {
				this.page = page;
				this.type = type;
				this.header = header;
			}

			int getPage() {
				return page;
			}

			int getType() {
				return type;
			}

			String getHeader() {
				return header;
			}
		}
		
		List<Content> contentlist;
		
		ContentCreator(){
			contentlist = new ArrayList<Content>();
		}
		
		void addHeader(String header, int type) {
			contentlist.add(new Content(cp.getPagenumber(), type, header));
		}
		
		void create(){
			int contentpagenumber=cp.getPagenumber();
			cp.setCurrentPageName("Content");
			for (Content c : contentlist) {
				//System.out.println(c);
				
				coord[0]+= c.getType()*g.getFontMetrics().stringWidth(" ");
				g.drawString(c.getHeader(), coord[0], coord[1]);
				coord[0]=rightedge-g.getFontMetrics().stringWidth(String.valueOf(c.getPage()));
				g.drawString(String.valueOf(c.getPage()), coord[0], coord[1]);
				if (coord[1] + g.getFont().getSize() > bottomedge) { 
					cp.closepage();
					cp.setCurrentPageName("Content");
				} else {
					coord[0]=leftmargin;
					coord[1] += g.getFont().getSize();
				}
			}
			
			g.drawString("Content ", coord[0], coord[1]);
			coord[0]=rightedge-g.getFontMetrics().stringWidth(String.valueOf(contentpagenumber));
			g.drawString(String.valueOf(contentpagenumber), coord[0], coord[1]);
			cp.closepage();
		}
	}
	
	
	class TextContainer{
		
		class Container{
			private String text;
			private FontStatus fontstatus;
			
			Container(String t, FontStatus fs) {
				text=t;
				fontstatus=fs;
			}
			
			String getText() {
				return text;
			}

			FontStatus getFontstatus() {
				return fontstatus;
			}
		}
		
		List<Container> containerlist;
		private int currentlength;
		private int currentmax = rightedge-leftmargin;
		private int currentAlignstyle; 					//0,1,2 for left, center, right
		
		TextContainer(){
			containerlist = new ArrayList<Container>();
		}
		
		
		
		void add(String s, FontStatus fs) {
			fontstatuslist.peekLast().useStatus(g);
			if (s.equals(" ")) {
				containerlist.add(new Container(s, fs));
				return;
			}
			if (currentlength+g.getFontMetrics().stringWidth(s) <= currentmax) {
				currentlength+=g.getFontMetrics().stringWidth(s);
				containerlist.add(new Container(s, fs));
			} else if (s.lastIndexOf(" ") != -1) {					//if there are spacebars
					String temptext = new String(s);
					while (true) {
						int i = temptext.lastIndexOf(" ");
						if (i != -1) {								//trying to put in max words till spacebar 
							temptext = temptext.substring(0, i);
							if (currentlength+g.getFontMetrics(g.getFont()).stringWidth(temptext) <= currentmax) {
								String text2 = s.substring(i+1, s.length());
								add(temptext, fs);
								add(" ", fs);
								add(text2, fs);
								break;
							} else
								continue;
						} else if(currentlength!=0) {				//if first word can't be added and not new line, try to put it on a new line
							drop();
							add(s, fs);
							break;
						} else {									//if it was a new line, try to put in max symbols and then change line
							for (int i2 = s.indexOf(" "); i2 >0; i2--) {
								if (g.getFontMetrics(g.getFont()).stringWidth(s.substring(0, i2)) <= currentmax) {
									String text1 = s.substring(0, i2);
									String text2 = s.substring(i2, s.length());
									add(text1, fs);
									drop();
									add(text2, fs);
									break;
								}
							}
						break;	
						}

					}
			
			//if there are no spacebars		
			// and new word can be added on next line
			} else if (currentlength!=0 && g.getFontMetrics(g.getFont()).stringWidth(s) <= currentmax) {				
				drop();
				add(s, fs);
			} else {
				for (int i = s.length(); i > 0; i--) {
					if (currentlength + g.getFontMetrics(g.getFont()).stringWidth(s.substring(0, i)) <= currentmax) {
						String text1 = s.substring(0, i);
						String text2 = s.substring(i, s.length());
						add(text1, fs);
						drop();
						add(text2, fs);
						break;
					}
				}
			}
			
		}
		
		
		void drop() {
			
			if (coord[1] + g.getFont().getSize() > bottomedge) 
				cp.closepage();
				
			switch (currentAlignstyle) {
			case 1:
				coord[0]=leftmargin+(rightedge-leftmargin)/2-currentlength/2;
				break;
			case 2:
				coord[0]=rightedge-currentlength;
				break;
			
			}
			
			for (Container c : containerlist) {
				c.getFontstatus().useStatus(g);
				drawCustomString(c.getText(), c.getFontstatus().getI(), c.getFontstatus().getU(), c.getFontstatus().getB());
			}
			
			coord[0] = leftmargin;
			coord[1] += g.getFont().getSize();
			containerlist.clear();
			currentlength=0;
			fontstatuslist.peekLast().useStatus(g);
			
		}

		void setCurrentAlignstyle(int currentAlignstyle) {
			this.currentAlignstyle = currentAlignstyle;
		}

		void update() {
			currentmax = rightedge - leftmargin;
		}

		int getCurrentlength() {
			return currentlength;
		}



		void setCurrentlength(int currentlength) {
			this.currentlength = currentlength;
		}
	}
	
	class CurrentPage{
		BufferedImage bf;
		private String currentPageName;
		private String lastHeaderOnPage;
		private String firstHeaderOnPage;
		private String filename;
		private int pagenumber;
		
		CurrentPage() {
			pagenumber=1;
			bf = new BufferedImage(picWidth, picHeight, 1);
			g=bf.createGraphics();
			g.setRenderingHint(
			        RenderingHints.KEY_TEXT_ANTIALIASING,
			        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			g.setClip(0, 0, bf.getWidth(), bf.getHeight());
			updateAllparameters();
		}
		
		int getPagenumber() {
			return pagenumber;
		}

		void setCurrentPageName(String currentPageName) {
			this.currentPageName = currentPageName;
		}
		
		void setLastHeader(String lastHeader) {
			if (lastHeaderOnPage==null) {
				firstHeaderOnPage = lastHeader;
			}
			lastHeaderOnPage = lastHeader;
		}

		
		void setFilename(String filename) {
			this.filename = filename;
		}

		void closepage(){
			if(firstHeaderOnPage!=null) {  
				currentPageName=firstHeaderOnPage;
				firstHeaderOnPage=null;
				}
			
			fontstatuslist.peekFirst().useStatus(g);				//use default font status for page title
			g.drawString(currentPageName, leftmargin+(rightedge-leftmargin)/2-g.getFontMetrics(g.getFont()).stringWidth(currentPageName)/2, topmargin/3+g.getFont().getSize());
			g.drawString(String.valueOf(pagenumber), g.getClipBounds().width/2, g.getClipBounds().height-2*g.getFont().getSize());
			
			try{
				File outputfile = new File(filename+".render/"+(pagenumber++)+".png");
				ImageIO.write(bf, "png", outputfile);
				
				} catch (Exception e) {
					System.out.println("Error while opening and writing to the file: " + e);
				}
			
			bf.flush();
			g.clearRect(0, 0, picWidth, picHeight);
			updateAllparameters();
			if(lastHeaderOnPage!=null)
				currentPageName=lastHeaderOnPage;
		}
		
	}
		
	void operateBody(Color bgcolor2, Color textcolor2, int leftmargin2,
			int bottommargin2, int rightmargin2, int topmargin2,
			String fontfamily2, int fontsize2) {
		
		FontStatus setByBodyFontStatus = fontstatuslist.getFirst();
		if(fontsize2>0)
			setByBodyFontStatus.setFontsize(fontsize2);
		if (textcolor2!=null)
			setByBodyFontStatus.setTextcolor(textcolor2);
		fontstatuslist.add(setByBodyFontStatus);
		
		leftmargin=leftmargin2;
		topmargin=topmargin2;
		rightedge=g.getClipBounds().width - rightmargin2;
		bottomedge=g.getClipBounds().height - bottommargin2;

		if (bgcolor2!=null)
			bgcolor = bgcolor2;
		if (fontfamily2 !=null)
			fontfamily = fontfamily2;
		
		tc.update();
		updateAllparameters();

	}
	


	
	
	void updateAllparameters() {
		if (bgcolor != null) {
			Color temp = g.getColor();
			g.setColor(bgcolor);
			g.fillRect(0, 0, picWidth, picHeight);
			g.setColor(temp);
		}
		
		FontStatus fs = fontstatuslist.peekLast(); 
		fs.useStatus(g);
		
		coord[0]=leftmargin;
		coord[1]=topmargin+g.getFont().getSize();
		
		if (fontfamily!=null) {
			
			g.setFont(new Font(fontfamily, g.getFont().getStyle(), g.getFont().getSize()));
			
		}
	}
	
	
	
	void updatestyle(int type, boolean status, Color color) {		// 1 for i, 2 for u, 3 for b
		switch (type) {
		case 1:
			if (status) {
				FontStatus fs = fontstatuslist.peekLast().clone();
				fs.setItalic(true);
				if(color!=null)
					fs.setTextcolor(color);
				fontstatuslist.add(fs);
			} else {
				fontstatuslist.pollLast();
			}
			break;

		case 2:
			if (status) {
				FontStatus fs = fontstatuslist.peekLast().clone();
				fs.setUnderlined(true);
				if(color!=null)
					fs.setTextcolor(color);
				fontstatuslist.add(fs);
			} else {
				fontstatuslist.pollLast();
			}
			break;

		case 3:
			if (status) {
				FontStatus fs = fontstatuslist.peekLast().clone();
				fs.setBold(true);
				if(color!=null)
					fs.setTextcolor(color);
				fontstatuslist.add(fs);
			} else {
				fontstatuslist.pollLast();
			}
			break;

		}
		
	}


	void operateheader(int type, String header, Color color) {
		operateBR();
		FontStatus fs = fontstatuslist.peekLast().clone();
		if(color!=null)
			fs.setTextcolor(color);
		fs.setFontsize(fontstatuslist.peekFirst().getFontsize() + (7-type)*3);
		coord[1] += g.getFont().getSize();
		g.setFont(g.getFont().deriveFont(g.getFont().getStyle() | Font.BOLD));
		fontstatuslist.add(fs);
		tc.add(header, fontstatuslist.peekLast());
		operateBR();
		fontstatuslist.pollLast();
		g.setFont(g.getFont().deriveFont(g.getFont().getStyle() & ~Font.BOLD));
		

		contentcreator.addHeader(header, type);
		cp.setLastHeader(header);
		
			}
	
	void operateBR() {
		tc.drop();
	}


	void operateText(String text) {
		tc.add(text, fontstatuslist.peekLast());
	}
	
	
	private void drawCustomString(String text, boolean i, boolean u, boolean b) {
		
		if(i) {
			g.setFont(g.getFont().deriveFont(g.getFont().getStyle() | Font.ITALIC));
		}
		
		if (b) {
			g.setFont(g.getFont().deriveFont(g.getFont().getStyle() | Font.BOLD));
			createshadow(text, coord);
		}
		
		if(u) {
			createunderline(text, coord);
		}
		g.drawString(text, coord[0], coord[1]);
		coord[0] += g.getFontMetrics(g.getFont()).stringWidth(text);
		g.setFont(g.getFont().deriveFont(g.getFont().getStyle() & ~Font.ITALIC));
		g.setFont(g.getFont().deriveFont(g.getFont().getStyle() & ~Font.BOLD));
		
	}
	
	
	private void createshadow(String text, final int[] localcoord) {
		int step = g.getFontMetrics().getHeight()/10;
		Color col=g.getColor();
		g.setColor(new Color(150, 150, 150));
		
		g.drawString(text, localcoord[0]+step, localcoord[1]+step);
		g.setColor(col);

	}


	private void createunderline(String text, final int [] localcoord) {
		int[] i=new int[2];
		i[0]=localcoord[0];
		i[1]=localcoord[1];
		int finish = i[0] + g.getFontMetrics(g.getFont()).stringWidth(text);
		int step = g.getFontMetrics().getHeight()/10;
		i[1]+=g.getFontMetrics().getHeight()/10;
		while(i[0]+2*step < finish) {
			g.drawLine(i[0], i[1], i[0]+step, i[1]+step);
			g.drawLine(i[0]+step, i[1]+step, i[0]+2*step, i[1]);
			i[0] +=2*step;
		}
		
		
	}




	void operateParagraph(boolean status, int attribute) {
		if (status) {
			if(tc.getCurrentlength()!=0)
				tc.drop();
			tc.setCurrentAlignstyle(attribute);
		}
		else {
			tc.drop();
			tc.setCurrentAlignstyle(0);	
		}
	}	
	
	
	void operatePic(String source, int width, int height) {
		try {
			BufferedImage bufferedImage = ImageIO.read(new File(source));
			
			if (width == 0 || height == 0) {						//if pic size is not set
				operatePicDefault(bufferedImage);
			} else {	
				operatePicSet(bufferedImage, width, height); //if pic size is set 
				}

		} catch (IOException e) {
			System.out.println("Error while opening a picture " + e);
		}
	}
	
	
	private void operatePicSet(BufferedImage bufferedImage, int width, int height) {
		if(width > rightedge-leftmargin || height>bottomedge-topmargin) {
			operatePicDefault(bufferedImage);
			return;
		}
			
						
		if (coord[0]+width>rightedge) {
			tc.drop();
		}
		
		if(coord[1]+height>bottomedge) {
			if(tc.getCurrentlength()!=0)
				tc.drop();
			cp.closepage();
		}
		
		
		if (coord[0] + tc.getCurrentlength() + width <= rightedge && coord[1] + height <= bottomedge) {
			if(tc.getCurrentlength()!=0)  {
				int t = tc.getCurrentlength();
				tc.drop();
				coord[1]-=g.getFont().getSize();
				coord[0]=leftmargin+t;
				tc.setCurrentlength(t+width);
			}
			else {
				tc.setCurrentlength(width);
			}
			g.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY));
			
			g.drawImage(bufferedImage, coord[0], coord[1], width, height, null);
			coord[0] += width;
			coord[1] += height;
			bufferedImage.flush();
		} else {
			tc.drop();
			if(coord[1] + height > bottomedge) 
				cp.closepage();
			
			g.addRenderingHints(new RenderingHints(
					RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY));
			
			g.drawImage(bufferedImage, coord[0], coord[1], width, height, null);
			coord[0] += width;
			coord[1] += height;
			tc.setCurrentlength(width);
			bufferedImage.flush();
		}

		
	}





	private void operatePicDefault(BufferedImage bufferedImage){
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();

		if (coord[0]+tc.getCurrentlength()+width>rightedge && width<= rightedge-leftmargin) {
			tc.drop();
		}
		
		if(coord[1]+height>bottomedge && height<=bottomedge-topmargin) {
			if(tc.getCurrentlength()!=0)
				tc.drop();
			cp.closepage();
		}
		
		
		if (coord[0] + tc.getCurrentlength() + width <= rightedge && coord[1] + height <= bottomedge) {
			if(tc.getCurrentlength()!=0)  {
				int t = tc.getCurrentlength();
				tc.drop();
				coord[1]-=g.getFont().getSize();
				coord[0]=leftmargin+t;
				tc.setCurrentlength(t+width);
			} else {
				tc.setCurrentlength(width);
			}
			g.drawImage(bufferedImage, null, coord[0], coord[1]);
			coord[0] += width;
			coord[1] += height;
			bufferedImage.flush();
		} else {
			if(tc.getCurrentlength()!=0 || coord[0]!=leftmargin || coord[1]!=topmargin+g.getFont().getSize()) {
				tc.drop();
				cp.closepage();
			}
			coord[1]=topmargin;
			double a= (double) (rightedge-leftmargin) / width ;
			double b= (double) (bottomedge - topmargin) / height ;
			double k =  (b>=a) ? a : b ;
			
			g.addRenderingHints(new RenderingHints(
					RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY));
			
			g.drawImage(bufferedImage, coord[0], coord[1], (int)(width*k), (int)(height*k), null);
			coord[0] += (int)(width*k);
			coord[1] += (int)(height*k);
			tc.setCurrentlength((int)(width*k));
			bufferedImage.flush();
		}

	}
	
	
	
	

	void operateTitle(String title) {
		cp.setCurrentPageName(title);
	}
	
		
	public void create(List<HTMLObject> htmlobjectlist){
		for (HTMLObject htmlobj : htmlobjectlist) {
			htmlobj.implement(this);
		}
		if(tc.getCurrentlength()!=0)
			tc.drop();
		cp.closepage();
		contentcreator.create();
		
	}

}
