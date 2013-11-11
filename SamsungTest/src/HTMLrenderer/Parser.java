package HTMLrenderer;

import java.awt.Color;
import java.io.*;
import java.util.*;


public class Parser {
	
	public List<HTMLObject> parse(String filename) {
		List<HTMLObject> objectlist = new ArrayList<HTMLObject>();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if (strLine.length()==0) {
					continue;
				}
				if (!strLine.contains("<") && strLine.length()>0) {
					objectlist.add(new TextObject(strLine+" "));
					continue;
				}
				
				if(!strLine.startsWith("<")) {
					objectlist.add(new TextObject(strLine.split("<")[0]));
					strLine = strLine.substring(strLine.split("<")[0].length());
				}
				
				while (true) {
					if (!strLine.contains(">")) {
						strLine += br.readLine();
					} else
						break;
				}
				
				if (strLine.contains("<h1") || strLine.contains("<h2") || strLine.contains("<h3")
						|| strLine.contains("<h4") || strLine.contains("<h5") || strLine.contains("<h6") ) {
					while (true) {
						if (strLine.contains("</h1") || strLine.contains("</h2") || strLine.contains("</h3")
								|| strLine.contains("</h4") || strLine.contains("</h5") || strLine.contains("</h6") ) {
							break;
						} else
							strLine += br.readLine();;
					}
				}
				
				if (strLine.contains("<title")) {
					while (true) {
						if (!strLine.contains("</title")) {
							strLine += br.readLine();
						} else
							break;
					}
				}

				for (String str : strLine.split("<"))
					parseTag(str, objectlist);
			}

			br.close();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		return objectlist;
	}
	
	private void parseTag(String str, List<HTMLObject> objectlist) {
		{
			boolean needadd = false;
			String second = null;
			if (str.split(">").length != 1) {
				if (str.startsWith("title")) {
					parceTitle(str.split(">")[1], objectlist);
					return;
				}
				else if (str.startsWith("h") && !str.startsWith("html") && !str.startsWith("head")){
					parceHeader(str, objectlist);
					return;
				}
				else{
					second = str.split(">")[1];
					needadd = true;
					str = str.split(">")[0]+">";
				}
			}
			
			if (str.startsWith("body") || str.startsWith("/body"))
				parceBody(str, objectlist);
			else if (str.startsWith("!--"))
				return;
			else if (str.contains("html") || str.contains("head"))
				parceHTMLTitleHead(str, objectlist);
			else if(str.startsWith("/h") || str.startsWith("/title"))
				return;
			else if (str.startsWith("img"))
				parceImg(str, objectlist);
			else if (str.toLowerCase().startsWith("br"))
				objectlist.add(new BRObject());
			else if (str.startsWith("h") || str.startsWith("/h"))
				parceHeader(str, objectlist);
			else if (str.startsWith("i") || str.startsWith("u")	|| str.startsWith("b"))
				parceFontStyle(str, objectlist, true);
			else if (str.startsWith("/i") || str.startsWith("/u") || str.startsWith("/b"))
				parceFontStyle(str, objectlist, false);
			else if (str.startsWith("p") || str.startsWith("/p"))
				parceParagraph(str, objectlist);
			else if (str.length()!=0)
				objectlist.add(new TextObject("<" + str));
		 
			if (needadd && second.length()>0)
				objectlist.add(new TextObject(second));

		}
		
	}



	private void parceTitle(String string, List<HTMLObject> objectlist) {
		objectlist.add(new TitleObject(string));
	}

	private void parceImg(String str, List<HTMLObject> objectlist) {
		
		ImgObject img = new ImgObject(str.split("\"")[1]);
		
		
		if (str.split("\"").length > 3) {
			int w=0, h=0;
			String [] temp = str.split("\"");
			for (int i =0; i< temp.length; i++){
				if (temp[i].contains("width"))
					w=Integer.parseInt(temp[i+1]);
				if (temp[i].contains("height"))
					h=Integer.parseInt(temp[i+1]);
			img.setWidthAndHeight(w, h);
			}
			

		}
		
		
		objectlist.add(img);
		
		
	}

	private void parceHTMLTitleHead(String str, List<HTMLObject> objectlist) {
		boolean m =!str.startsWith("/");
		if (!m)
			str = str.substring(1);
		switch(str.toLowerCase()) {
		case "html>" :
			objectlist.add(new HTMLHeadObject(1 , m));
			break;
		case "head>" :
			objectlist.add(new HTMLHeadObject(2 , m));
			break;
		}
		
	}

	private void parceBody(String str, List<HTMLObject> objectlist) {
		if (str.startsWith("body")) {
			BodyObject bo = new BodyObject(true);
			if (str.split("body").length > 1) {
				str = str.split("body")[1];
				String substr[] = str.split("\"");
				for (int i = 0; i < substr.length; i++) {
					switch(substr[i].replaceAll("\\s","").toLowerCase()) {
					
					case "bgcolor=" :
						bo.setBgcolor(parceColor(substr[i + 1].split("#")[1]));
						break;
					case "text=" :
						bo.setTextcolor(parceColor(substr[i + 1].split("#")[1]));
						break;
					case "leftmargin=" :
						bo.setLeftmargin(Integer.parseInt(substr[i + 1]));
						break;
					case "topmargin=" :
						bo.setTopmargin(Integer.parseInt(substr[i + 1]));
						break;
						
					case "rightmargin=" :
						bo.setRightmargin(Integer.parseInt(substr[i + 1]));
						break;
					
					
					case "bottommargin=" :
						bo.setBottommargin(Integer.parseInt(substr[i + 1]));
						break;
						
					case  "font-size=" :
						bo.setFontsize(Integer.parseInt(substr[i + 1]));
						break;
						
					case "font-family=" :
						bo.setFontFamily(substr[i+1]);
						break;
						
					}
										
				}
			}		
						
			objectlist.add(bo);
		}
		
		else
			objectlist.add(new BodyObject(false));
		
	}

	private void parceHeader(String str, List<HTMLObject> objectlist) {
		HeaderObject ho = new HeaderObject(Integer.parseInt(String.valueOf(str.charAt(1))), str.split(">")[1]);

		if (str.split("#").length != 1)
			ho.addAttribute(parceColor(str.split("#")[1].split("\"")[0]));

		objectlist.add(ho);

	}
	
	private Color parceColor(String temp){
		int[] result = new int[3];
		result[0] = Integer.parseInt(temp.substring(0, 2), 16);
		result[1] = Integer.parseInt(temp.substring(2, 4), 16);
		result[2] = Integer.parseInt(temp.substring(4, 6), 16);
		return new Color(result[0], result[1], result[2]);
		
	}

	private void parceParagraph(String str, List<HTMLObject> objectlist) {
		ParagraphObject po = null;
		if (str.startsWith("p")) {
			po = new ParagraphObject(true);
			if (str.split("\"").length != 1) {
				switch (str.split("\"")[1]) {
				case "center":
					po.setAttribute(1);
					break;
				case "left":
					po.setAttribute(0);
					break;
				case "right":
					po.setAttribute(2);
					break;

				}
			}
		} else
			po = new ParagraphObject(false);

		objectlist.add(po);

	}

	private void parceFontStyle(String str, List<HTMLObject> objectlist,
			boolean status) {
		if (status) {
			FontStyleObject fso=null;
			if (str.startsWith("i"))
				fso = new FontStyleObject(1, true);
			if (str.startsWith("u"))
				fso = new FontStyleObject(2, true);
			if (str.startsWith("b"))
				fso = new FontStyleObject(3, true);
			
			if (str.split("#").length != 1) 
				fso.addAttribute(parceColor(str.split("#")[1].split("\"")[0]));
			
			
			objectlist.add(fso);
			
		} else {
			switch (str.replaceAll("\\s", "").toLowerCase()) {
			case "/i>":
				objectlist.add(new FontStyleObject(1, false));
				break;
			case "/u>":
				objectlist.add(new FontStyleObject(2, false));
				break;
			case "/b>":
				objectlist.add(new FontStyleObject(3, false));
				break;

			}

		}

	}
}	
