package HTMLrenderer;

import java.awt.Color;

public interface HTMLObject {
	void implement (PictureCreator pc);

}

class BRObject implements HTMLObject {
	
	@Override
	public void implement(PictureCreator pc) {
		pc.operateBR();
		
	}

}

class TitleObject implements HTMLObject {
	String title;
	TitleObject(String title) {
		this.title=title;
	}
	
	@Override
	public void implement(PictureCreator pc) {
		pc.operateTitle(title);
		
	}
}

class FontStyleObject implements HTMLObject {
	private int type; 								// 1 for i, 2 for u, 3 for b
	private boolean status;							// true for start, false for end
	private Color color;
	
	FontStyleObject(int t, boolean st) {
		type = t;
		status = st;
	}
	
	public void addAttribute(Color a) {				//Builder
		color=a;
	}
	
	@Override
	public void implement(PictureCreator pc) {
		pc.updatestyle(type, status, color);
		
	}
	
}

class HeaderObject implements HTMLObject {
	private String header;
	private int type;
	private Color color;
	
	HeaderObject(int t, String header) {
		this.type=t;
		this.header=header;
	}
	
	public void addAttribute(Color a) {				//Builder 
		color=a;
	}
	
	@Override
	public void implement(PictureCreator pc) {
		pc.operateheader(type, header, color);
		
	}
}

class TextObject implements HTMLObject {
	protected String text;

	TextObject(String txt) {
		text = txt;
	}

	@Override
	public void implement(PictureCreator pc) {
		pc.operateText(text);
		
	}

}

class ParagraphObject implements HTMLObject {
	private int attribute; 									//0,1,2 for left, center, right 
	private boolean status;
	
	ParagraphObject(boolean m) {
		status=m;
	}
	
	public void setAttribute(int attr) {							//Builder 
		attribute=attr;
	}
	
	
	@Override
	public void implement(PictureCreator pc) {
		pc.operateParagraph(status, attribute);
		
	}
	

}

class ImgObject implements HTMLObject {
	private String source;
	private int width;
	private int height;

	ImgObject(String src) {
		source = src;
	}
	
	public void setWidthAndHeight(int w, int h) {
		width=w;
		height=h;
	}

	@Override
	public void implement(PictureCreator pc) {
		pc.operatePic(source, width, height);
	}
}


class BodyObject implements HTMLObject {
	private Color bgcolor;
	private Color textcolor;
	private int leftmargin;
	private int topmargin;
	private int rightmargin;
	private int bottommargin;
	private String fontfamily;
	private int fontsize;
	private boolean status;

	BodyObject(boolean status) {
		this.status = status;
	}

	public void setBgcolor(Color color) { 		// Builder
		bgcolor = color;
	}

	public void setTextcolor(Color color) {
		textcolor = color;
	}

	public void setLeftmargin(int a) {
		leftmargin = a;
	}

	public void setTopmargin(int a) {
		topmargin = a;
	}

	public void setRightmargin(int a) {
		rightmargin = a;
	}

	public void setBottommargin(int a) {
		bottommargin = a;
	}

	public void setFontsize(int a) {
		fontsize = a;
	}

	public void setFontFamily(String a) {
		fontfamily = a;
	}

	@Override
	public void implement(PictureCreator pc) {
		if (status) {
			pc.operateBody(bgcolor, textcolor, leftmargin, bottommargin, rightmargin, topmargin, fontfamily, fontsize);
		}
	}

}

class HTMLHeadObject implements HTMLObject {
	@SuppressWarnings("unused")
	private boolean status; // open/close
	@SuppressWarnings("unused")
	private int type; // 1 for HTML, 2 for Head

	HTMLHeadObject(int typ, boolean stat) {
		status = stat;
		type = typ;
	}

	@Override
	public void implement(PictureCreator pc) {
	}

}