import java.awt.Color;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

	private String categoriesSubString = "categories=111";
	private String concatSubString = "&";
	private File downloadPath;

	private int heightLimit = 0;

	private ArrayList<Integer> idList = new ArrayList<>();

	private String mainSiteURL = "http://alpha.wallhaven.cc";
	private int minNumberOfTags;
	private String orderSubString = "order=desc";
	private String puritySubString = "purity=100";

	private String searchSubString = "/search?";
	private String siteSubString = "page=";

	private String sortingSubString = "sorting=views";
	private int widthLimit = 0;

	private boolean tagsToSymlinks;

	private ArrayList<String> preferedToken = new ArrayList<>();
	private ArrayList<Color> preferedColor = new ArrayList<>();

	public Loader() {
		String[] preferedToken = {"abstract", "sunset", "mountains",
				"landscape", "skyscape", "cityscape", "landscapes",
				"cityscapes", "graph", "computer science", "stars",
				"outer space", "galaxies", "beach", "beaches", "water",
				"nature", "fields", "sunlight", "depth of field", "skyscape",
				"shadow", "clouds", "bokeh", "lenses", "wireframes",
				"wireframe", "science", "minimal", "minimalism", "space",
				"skies", "macro", "closeup"};

		setPreferedToken(preferedToken);
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
	}

	/**
	 * Starts parsing the "hotpage" of wallhaven.cc. This is the latest site *
	 * sorted after SFW and views of the images.
	 */
	public void findNewWallpaperOnHotPage() {
		String parsingURL = mainSiteURL + searchSubString + categoriesSubString
				+ concatSubString + puritySubString + concatSubString
				+ sortingSubString + concatSubString + orderSubString;

		System.out.println("Download started with the following URL: "
				+ parsingURL);

		// try to get total number of pages
		int lastPage;
		try {
			Document doc = Jsoup
					.connect(parsingURL + concatSubString + siteSubString + 1)
					.timeout(5000).get();
			lastPage = Integer.parseInt(doc
					.select("span[class=thumb-listing-page-total]").first()
					.text().substring(2));
		} catch (IOException e) {
			lastPage = 3000000;
		}

		for (int index = 1; index <= lastPage; ++index) {
			// get the ids of all wallpapers for the current page
			System.out.printf("Parsing page #%d%n", index);
			List<Integer> ids = findWallpapersOnPage(parsingURL, index);

			// download the found wallpapers
			for (Integer id : ids) {
				if (!isAlreadyDownloaded(id)) {
					loadImage(new Image(id));
				} else {
					System.out.printf("> Already loaded image #%d%n", id);
				}
			}
		}

	}

	/**
	 * Create download directory and start loading!
	 */
	public void startDownload() {
		if (!this.downloadPath.exists()) {
			this.downloadPath.mkdir();
		}

		findNewWallpaperOnHotPage();
	}

	/**
	 * Get all files that we already downloaded to downloadPath.
	 */
	private void loadAlreadyDownloadedFiles() {
		for (File f : this.downloadPath.listFiles(new JPGAndPNGFileFilter())) {
			// parse the id from the filename
			String file = f.getName();
			int from = 0;
			int to = (file.indexOf('_') != -1) ? file.indexOf('_') : file
					.indexOf('.');
			this.idList.add(Integer.parseInt(file.substring(from, to)));
		}
	}

	/**
	 * Get the ID's of all images on one specific page.
	 *
	 * @param url
	 * @param pageNumber
	 */
	private List<Integer> findWallpapersOnPage(String url, int pageNumber) {
		List<Integer> ids = new LinkedList<Integer>();
		try {
			String pageURL = url + concatSubString + siteSubString + pageNumber;
			Document doc = Jsoup.connect(pageURL).get();

			for (Integer id : getImageIdsFromContent(doc)) {
				ids.add(id);
			}
		} catch (IOException e) {
			System.out.printf("Couldn't get page content. Skipping page %d.%n",
					pageNumber);
		}
		return ids;
	}

	/**
	 * Check whether a given id has already been downloaded and return the the
	 * result.
	 *
	 * @param id
	 * @return boolean
	 */
	private boolean isAlreadyDownloaded(Integer id) {
		return idList.contains(id);
	}

	private double[] rgbToYUV(Color c) {

		double[] res = new double[3];

		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();

		double y = 0.2126 * r + 0.7152 * g + 0.0722 * b;
		double u = -0.09991 * r - 0.33609 * g + 0.436 * b;
		double v = 0.615 * r - 0.55861 * g - 0.05639 * b;

		res[0] = y;
		res[1] = u;
		res[2] = v;

		return res;
	}

	private double calculatePriority(Image img) {
		int feasibleToken = 0;

		for (Color c : this.preferedColor) {

			// Convert to YUV to use euclidean distance as measurement
			double[] vuyPrefered = rgbToYUV(c);

			for (Color d : img.colorList) {

				double[] vuyImage = rgbToYUV(d);

				double dist2 = (vuyImage[0] - vuyPrefered[0])
						* (vuyImage[0] - vuyPrefered[0]);
				dist2 += (vuyImage[1] - vuyPrefered[1])
						* (vuyImage[1] - vuyPrefered[1]);
				dist2 += (vuyImage[2] - vuyPrefered[2])
						* (vuyImage[2] - vuyPrefered[2]);

				// TODO: Get a good measure for similiar
				// and get a measurement between 0 and 1
				// If colors are similar
				if (Math.sqrt(dist2) < 0.25) {
					feasibleToken++;
				}

			}

		}

		// TODO: Replace with a better scheme
		for (String token : img.tagList) {
			if (this.preferedToken.contains(token)) {
				feasibleToken++;
			}
		}

		return feasibleToken;
	}

	/**
	 * Downloads the image to the download location
	 *
	 * @param img
	 * @throws IOException
	 */
	private void downloadImage(Image img) throws IOException {
		URL url = new URL(img.filePath);
		InputStream is = null;
		OutputStream os = null;

		try {
			is = url.openStream();
			os = new FileOutputStream(this.downloadPath.getAbsolutePath()
					+ File.separator + img.fileName);

			byte[] b = new byte[2048];
			int length;

			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (null != os)
					os.close();
				if (null != is)
					is.close();
			} catch (IOException e) {
				return;
			}
		}
	}

	private LinkedList<Integer> getImageIdsFromContent(Document doc) {
		LinkedList<Integer> ids = new LinkedList<>();

		for (Element el : doc.body().select("a[class=preview]")) {
			String url = el.attr("href");
			String id = url.substring(url.lastIndexOf('/') + 1, url.length());
			ids.push(Integer.valueOf(id));
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
		double priority = 0;

		if (img.filePath != null) {
			// Decide whether a site contains correct token (image description)
			priority = calculatePriority(img);

			// Skip the image, if it doesn't match threshold tokens
			if (priority < this.minNumberOfTags) {
				printImageInformation(img, priority,
						"Skipped: doesn't match priority");
				return;
			}
			// Skip the image, if it doesn't match the resolution
			if (img.width < widthLimit || img.height < heightLimit) {
				printImageInformation(img, priority,
						"Skipped: wrong resolution.");
				return;
			}

			// try to download the image
			try {
				downloadImage(img);

				if (tagsToSymlinks) {
					createSymlinks(img);
				}
			} catch (IOException e) {
				printImageInformation(img, priority,
						"Skipped: Couldn't load image!");
				return;
			}

			// Logging information to the current image
			printImageInformation(img, priority, "Got it!");
		} else {
			// Logging information to the current image
			printImageInformation(img, priority, "Skipped: Filepath is null!");
		}

	}

	private void printImageInformation(Image img, double priority,
			String message) {
		System.out.printf("> Found image #%d with priority %.1f. %s%n", img.id,
				priority, message);
	}

	private void createSymlinks(Image img) {
		String filepath = this.downloadPath.getAbsolutePath() + File.separator
				+ img.fileName;

		for (String tag : img.tagList) {
			if (preferedToken.contains(tag)) {
				File tagPath = createTagSubdirectory(tag);
				Path original = Paths.get(filepath).toAbsolutePath();
				Path link = Paths.get(
						tagPath.toString() + File.separator + img.fileName)
						.toAbsolutePath();
				try {
					Files.createSymbolicLink(link, original);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private File createTagSubdirectory(String tag) {
		File tagSubdirectory = new File(downloadPath, "by_tags");

		if (!tagSubdirectory.exists()) {
			tagSubdirectory.mkdir();
		}

		File tagPath = new File(tagSubdirectory, tag);

		if (!tagPath.exists()) {
			try {
				tagPath.mkdir();
			} catch (SecurityException e) {
				System.err.println("Can't create directory at " + tagPath);
				e.printStackTrace();
			}
		}

		return tagPath;
	}

	public void setDownloadPath(String downloadPath) {
		File newDownloadPath = new File(downloadPath);

		if (newDownloadPath.exists() && newDownloadPath.isDirectory()) {
			this.downloadPath = newDownloadPath;
		} else {
			try {
				newDownloadPath.mkdir();
				this.downloadPath = newDownloadPath;
			} catch (SecurityException e) {
				System.err.println("Can't create directory at " + downloadPath);
				e.printStackTrace();
			}
		}

		loadAlreadyDownloadedFiles();
	}

	public void setMinNumberOfTags(int threshold) {
		this.minNumberOfTags = threshold;
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

	/**
	 * @param searchSubString the searchSubString to set
	 */
	public void setSearchSubString(String searchSubString) {
		this.searchSubString = searchSubString;
	}

	/**
	 * @param sortingSubString the sortingSubString to set
	 */
	public void setSortingSubString(String sortingSubString) {
		this.sortingSubString = sortingSubString;
	}

	/**
	 * @param categoriesSubString the categoriesSubString to set
	 */
	public void setCategoriesSubString(String categoriesSubString) {
		this.categoriesSubString = categoriesSubString;
	}

	/**
	 * @param puritySubString the puritySubString to set
	 */
	public void setPuritySubString(String puritySubString) {
		this.puritySubString = puritySubString;
	}

	/**
	 * @param heightLimit the heightLimit to set
	 */
	public void setHeightLimit(int heightLimit) {
		this.heightLimit = heightLimit;
	}

	/**
	 * @return the heightLimit
	 */
	public int getHeightLimit() {
		return heightLimit;
	}

	/**
	 * @param widthLimit the widthLimit to set
	 */
	public void setWidthLimit(int widthLimit) {
		this.widthLimit = widthLimit;
	}

	/**
	 * @return the widthLimit
	 */
	public int getWidthLimit() {
		return widthLimit;
	}

	/**
	 * @param tagsToSymlinks
	 */
	public void setTagsToSymlinks(Boolean tagsToSymlinks) {
		this.tagsToSymlinks = tagsToSymlinks;
	}
}
