package HTMLrenderer;

public class HTMLrenderer {

	public static void main(String[] args) {

		int width = 0, height = 0;
		String filename = null;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-i"))
				filename = args[i + 1];
			if (args[i].equals("-w"))
				width = Integer.valueOf(args[i + 1]);
			if (args[i].equals("-h"))
				height = Integer.valueOf(args[i + 1]);
		}

		if (filename == null || width <= 0 || height <= 0) {
			System.out.println("You should use parameters like "
							+ "\"java –Xmx16m –Xms16m –Xss16m -jar myhtml.jar -i input.html -w 800 -h 1280\" \nplease try again");

		} else
			new PictureCreator(filename, width, height).create(new Parser().parse(filename));
	}

}
