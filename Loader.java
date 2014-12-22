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

	/**
	 * An simple class for filtering jpg and png files
	 */
	private class JPGAndPNGFileFilter implements FileFilter {

		@Override
		public boolean accept(File arg0) {

			if (arg0.getName().endsWith(".jpg")
					|| arg0.getName().endsWith(".png"))
				return true;
			return false;
		}
	}

	private String mainSiteURL = new String("http://alpha.wallhaven.cc");
	private String searchSubString = new String("/random?");
	private String categoriesSubString = new String("categories=111");
	private String puritySubString = new String("purity=100");
	private String sortingSubString = new String("sorting=views");
	private String orderSubString = new String("order=desc");
	private String siteSubString = new String("page=");;
	private String concatSubString = new String("&");

	private File downloadPath = new File(System.getProperty("user.dir")
			+ File.separator + "Download" + File.separator + "");
	private ArrayList<Integer> idList = new ArrayList<>();

	private ArrayList<String> preferedToken = new ArrayList<>();
	private int minNumberOfTags;
	private int widthLimit = 1920;
	private int heightLimit = 1080;

	public Loader() {
		String[] preferedToken = { "abstract", "sunset", "mountains",
				"landscape", "skyscape", "cityscape", "landscapes",
				"cityscapes", "graph", "computer science", "stars",
				"outer space", "galaxies", "beach", "beaches", "water",
				"nature", "fields", "sunlight", "depth of field", "skyscape",
				"shadow", "clouds", "bokeh", "lenses", "wireframes",
				"wireframe", "science", "minimal", "minimalism", "space",
				"skies", "macro", "closeup" };

		setPreferedToken(preferedToken);
		startDownload();
	}

	/**
	 * Constructor taking an array of strings as tags. These tags are preferred
	 * and will be used to decide whether an image is "prefered" or not.
	 * 
	 * @param token
	 */
	public Loader(String[] token) {
		if (token != null) {
			setPreferedToken(token);
		}
		
		startDownload();
	}

	/**
	 * Constructor taking an array of strings as tags. These tags are preferred
	 * and will be used to decide whether an image is "prefered" or not. The
	 * threshhold is the number of prefered tags an image has to have to get
	 * downloaded.
	 * 
	 * @param token
	 * @param threshhold
	 */
	public Loader(String[] token, int threshhold) {
		if (threshhold >= 0) {
			this.minNumberOfTags = threshhold;
		} else {
			System.out
					.println("Ignoring negative number of prefered tags, downloading every image!");
			this.minNumberOfTags = 0;
		}

		if (token != null) {
			setPreferedToken(token);
		}

		startDownload();
	}

	private void startDownload() {
		if (!this.downloadPath.exists()) {
			this.downloadPath.mkdir();
		}
		
		findNewWallpaperOnHotPage();
	}

	/**
	 * Setter method for the prefered token list
	 * 
	 * @param preferedToken void
	 */
	public void setPreferedToken(String[] preferedToken) {
		for (String token : preferedToken) {
			this.preferedToken.add(token);
		}
	}

	public void setMinNumberOfTags(int threshold) {
		this.minNumberOfTags = threshold;
	}

	public void setDownloadPath(String downloadPath) {
		File newDownloadPath = new File(downloadPath);

		if (newDownloadPath.exists() && newDownloadPath.isDirectory()) {
			this.downloadPath = newDownloadPath;
		} else {
			try {
				newDownloadPath.mkdir();
			} catch (SecurityException e) {
				System.err.println("Can't create directory at " + downloadPath);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Starts parsing the "hotpage" of wallhaven.cc. This is the latest site
	 * sorted after SFW and views of the images.
	 */
	public void findNewWallpaperOnHotPage() {

		String parsingURL = mainSiteURL + searchSubString + categoriesSubString
				+ concatSubString + puritySubString + concatSubString
				+ sortingSubString + concatSubString + orderSubString;

		for (File f : this.downloadPath.listFiles(new JPGAndPNGFileFilter())) {
			// Add the names to the idList (the names are the ids of the images)
			String fileName = f.getName();
			this.idList.add(Integer.parseInt(fileName.substring(0,
					fileName.length() - 4)));
		}

		// try to get total number of pages
		int lastPage;
		try {
			Document doc = Jsoup
					.connect(parsingURL + concatSubString + siteSubString + 1)
					.timeout(1000).get();
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

	/**
	 * Check whether a given id has already been downloaded and return the the
	 * result.
	 * 
	 * @param id
	 * @return boolean
	 */
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

	/**
	 * Check the filePath and the priority and download the image if the
	 * priority is larger or equals the threshold.
	 * 
	 * @param img
	 * @return void
	 */
	private void loadImage(Image img) {
		int priority = 0;
		int id = 0;

		if (img.filePath != null) {
			// Decide whether a site contains correct token (image description)
			priority = calculatePriority(img.tagList);

			// Load the image, if it contains at least threshold token
			if (priority >= this.minNumberOfTags) {

				// check resolution
				if (img.width < widthLimit || img.height < heightLimit)
					return;

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

	/**
	 * Downloads the image to the download location
	 * 
	 * @param img
	 * @throws IOException
	 */
	private void downloadImage(Image img) throws IOException {
		URL url = new URL(img.filePath);

		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream(
				this.downloadPath.getAbsolutePath() + File.separator
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
