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

	/**
	 * Constructor for a website which holds the link to the image with id
	 * <em>id<em>
	 * 
	 * @param id
	 */
	public Image(Integer id) {
		this.id = id;
		imagePath = "http://alpha.wallhaven.cc/wallpaper/" + id;
		collectFurtherInformationForImage(imagePath);
	}

	private void collectFurtherInformationForImage(String imagePath) {
		// TODO Auto-generated method stub
		try {
			Document page = Jsoup.connect(imagePath).timeout(1000).get();
			parseImagePage(page);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseImagePage(Document doc) throws Exception {

		// Gather meta information
		Element imageTag = doc.select("img[id=wallpaper]").first();

		filePath = "http:" + imageTag.attr("src");
		fileName = filePath.substring(filePath.lastIndexOf('-') + 1,
				filePath.length());
		width = Integer.parseInt(imageTag.attr("data-wallpaper-width"));
		height = Integer.parseInt(imageTag.attr("data-wallpaper-height"));
		for (Element el : doc.select("a[class=tagname]")) {
			tagList.add(el.text());
		}
		System.out.println("Found download link: " + filePath);
	}
}
