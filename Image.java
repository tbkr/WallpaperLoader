import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Image {
	public String imagePath;
	public String filePath;
	public String fileName;

	public ArrayList<String> tagList = new ArrayList<>();

	int id;
	int width;
	int height;

	public Image(Integer id) {
		this.id = id;
		imagePath = "http://alpha.wallhaven.cc/wallpaper/" + id;
		collectFurtherInformationForImage(imagePath);
	}

	private void collectFurtherInformationForImage(String imagePath) {
		try {
			Document doc = Jsoup.connect(imagePath).get();
			parseImagePage(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parseImagePage(Document doc) throws Exception {
		// Gather meta information
		Element imgTag = doc.select("img[id=wallpaper]").first();

		filePath = "http:" + imgTag.attr("src");
		fileName = filePath.substring(filePath.lastIndexOf('-') + 1,
				filePath.length());
		width = Integer.parseInt(imgTag.attr("data-wallpaper-width"));
		height = Integer.parseInt(imgTag.attr("data-wallpaper-height"));

		for (Element el : doc.select("a[class=tagname]")) {
			tagList.add(el.text());
		}

		System.out.println("Found download link: " + filePath);
	}
}
