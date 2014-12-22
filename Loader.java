import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Loader {

	private String mainSiteURL = new String("http://alpha.wallhaven.cc");
	private String searchSubString = new String("/search?");
	private String categoriesSubString = new String("categories=111");
	private String puritySubString = new String("purity=100");
	private String sortingSubString = new String("sorting=views");
	private String orderSubString = new String("order=desc");
	private String siteSubString = new String("page=");;
	private String concatSubString = new String("&");

	private File downloadLocation = new File(System.getProperty("user.dir")
			+ File.separator + "Download" + File.separator + "");
	private ArrayList<Integer> idList = new ArrayList<>();

	private ArrayList<String> preferedToken = new ArrayList<>();
	private int threshold;

	public Loader() {
		this(new String[] { "abstract", "sunset", "mountains", "landscape",
				"skyscape", "cityscape", "landscapes", "cityscapes", "graph",
				"computer science", "stars", "outer space", "galaxies",
				"beach", "beaches", "water", "nature", "fields", "sunlight",
				"depth of field", "skyscape", "shadow", "clouds", "bokeh",
				"lenses", "wireframes", "wireframe", "science", "minimal",
				"minimalism", "space", "skies", "macro", "closeup" });
	}

	public Loader(String[] token) {
		this(token, 1);
	}

	public Loader(String[] token, int threshhold) {
		this.threshold = threshhold;

		if (token != null) {
			for (String t : token) {
				this.preferedToken.add(t.toLowerCase());
			}
		}

		// If there isn't a history, make the directory
		if (!this.downloadLocation.exists()) {
			this.downloadLocation.mkdir();
		}

		System.out.println("Starting toplist search... Saving to "
				+ downloadLocation);
		findNewWallpaperOnHotPage();
	}

	/**
	 * Starts parsing the "hotpage" of wallhaven.cc. This is the latest site
	 * sorted after SFW and Views of the images.
	 * 
	 * @param maxId void
	 */
	private void findNewWallpaperOnHotPage() {
		String parsingURL = mainSiteURL + searchSubString + categoriesSubString
				+ concatSubString + puritySubString + concatSubString
				+ sortingSubString + concatSubString + orderSubString;

		// Create a new FileFilter for jpg and png files
		for (File f : this.downloadLocation.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return (file.getName().endsWith(".jpg") || file.getName()
						.endsWith(".png"));
			}
		})) {
			// Add the names to the idList (the names are the ids of the images)
			// TODO: Better solution: Ignore the last four characters for
			// ending...
			this.idList.add(Integer.parseInt(f.getName().substring(0,
					f.getName().length() - 4)));
		}

		// try to get total number of pages
		int lastPage;
		try {
			Document doc = Jsoup.connect(
					parsingURL + concatSubString + siteSubString + 1).get();
			lastPage = Integer.parseInt(doc
					.select("span[class=thumb-listing-page-total]").first()
					.text().substring(2));
		} catch (Exception e) {
			lastPage = 3000000;
		}

		for (int index = 1; index < lastPage; ++index) {
			try {
				Document doc = Jsoup.connect(
						parsingURL + concatSubString + siteSubString + index)
						.get();

				for (Integer id : getImageIdsFromContent(doc)) {
					if (!alreadyDownloaded(id)) {
						loadImage(new Image(id));
					} else {
						System.out
								.println("Already owning Image with Id:" + id);
					}
				}

				System.out.println("Current Page:" + index);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private boolean alreadyDownloaded(Integer id) {
		for (int i : this.idList) {
			if (i == id) {
				return true;
			}
		}

		return false;
	}

	private LinkedList<Integer> getImageIdsFromContent(Document doc) {

		LinkedList<Integer> ids = new LinkedList<>();

		for (Element el : doc.body().select("a[class=preview]")) {
			String url = el.attr("href");
			String id = url.substring(url.lastIndexOf('/') + 1, url.length());
			ids.push(new Integer(id));
		}

		return ids;
	}

	private void loadImage(Image img) {
		int priority = 0;
		int id = 0;
		String filetype = "unknown (not calculated because under t) ";

		if (img.filePath != null) {
			// Decide whether a site contains correct token (image description)
			priority = calculatePriority(img.tagList);

			// Load the image, if it contains at least threshold token
			if (priority >= this.threshold) {

				try {
					downloadImage(img);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Logging information to the current image
				printInformation(priority, img.id, true);
			} else {
				// Logging information to the current image
				printInformation(priority, img.id, true);
			}

		} else {
			// Logging information to the current image
			printInformation(priority, id, false);
		}

	}

	private void downloadImage(Image img) throws IOException {
		URL url = new URL(img.filePath);

		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream(
				this.downloadLocation.getAbsolutePath() + File.separator
						+ img.fileName);

		byte[] b = new byte[200048];
		int length;

		while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}

		is.close();
		os.close();
	}

	private int calculatePriority(ArrayList<String> tagList) {
		int feasibleToken = 0;

		for (String token : tagList) {
			if (this.preferedToken.contains(token)) {
				feasibleToken++;
			}
		}

		return feasibleToken;
	}

	private void printInformation(int priority, long id, boolean b) {

		System.out.print("Current Image has ID:" + id + ", Priority: "
				+ priority);

		System.out
				.println(" "
						+ new Timestamp(Calendar.getInstance()
								.getTimeInMillis()) + " ");

	}

}
