import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

import javax.imageio.ImageIO;

public class Loader {

	private String preImageRozneString = new String("http://wallpapers.wallbase.cc/rozne/wallpaper-");
	private String preImageHighString = new String("http://wallpapers.wallbase.cc/high-resolution/wallpaper-");

	private String preSiteString = new String("http://wallbase.cc/wallpaper/");

	private File downloadLocation = new File(System.getProperty("user.dir")+ File.separator + "Download" + File.separator+ "");
	private ArrayList<Integer> idList = new ArrayList<>();

	
	private ArrayList<String> preferedToken = new ArrayList<>();
	private int threshold;
	private boolean isInterruped;
	
	public Loader() {

		this(new String[] {"abstract", "sunset", "mountains", "landscape", "skyscape",
				"cityscape", "landscapes", "cityscapes", "graph", "computer science",
				"stars", "outer space", "galaxies", "beach", "beaches", "water", "nature", "fields",
				"sunlight", "depth of field", "skyscape", "shadow", "clouds"
				
		});

	}
	
	public Loader(String[] token) {

		this(token, 1);
		
	}
	
	public Loader(String[] token, int threshhold) {
		
		this.threshold = threshhold;
		
		if(token != null) {
			
			for(String t : token) {
				this.preferedToken.add(t.toLowerCase());
			}	
			
		}
		
		if (!this.downloadLocation.exists()){
			this.downloadLocation.mkdir();
			startLoading();
		} else {
			int maxId = getMaxId(this.downloadLocation);
			
			System.out.println("Starting toplist search...");
			startHotPage(maxId);
		}
	}

	private int getMaxId(File downloadLocation) {
		
		System.out.println("Processing current database!...");
		
		int max = 0;
		
		try {
			BufferedReader temp = new BufferedReader(new FileReader(this.downloadLocation + File.separator + "lastIndex.ep"));
			
			String line = temp.readLine();
			max = Integer.parseInt(line);
			
			temp.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return max;
	}

	private void startHotPage(int maxId) {
		
		String hotPageUrl = new String("http://wallbase.cc/toplist/index");
		URL hotPage = null;
		BufferedReader bf = null;
		BufferedWriter bw = null;
		int lastIndex = maxId;		
		
		for(File f: this.downloadLocation.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File arg0) {
				
				if(arg0.getName().endsWith(".jpg") || arg0.getName().endsWith(".png"))
					return true;
				return false;
			}
			}))
		{		
			this.idList.add(Integer.parseInt(f.getName().substring(0, f.getName().length()-4)));		
		}
		
		for(int index = lastIndex; index < 3000000; index++) {
			try {
				
				// Writer for list the last inex of hotpage
				try {
					bw = new BufferedWriter(new FileWriter(this.downloadLocation + File.separator + "lastIndex.ep"));
					bw.write(index+"");
					bw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				boolean loaded = false;
				boolean alreadyOwned = false;
				hotPage = new URL(hotPageUrl + "/" + index);
				bf = new BufferedReader(new InputStreamReader(hotPage.openStream()));
				String currentLine = null;
				Integer id = 0;
				
				System.out.println("Current at Index:"+ index);
				
				
				while(bf.ready() && !loaded) {
					
					currentLine = bf.readLine();
					currentLine = currentLine.trim();
					
					if(currentLine.startsWith("<div id=\"thumb")) {

						while(bf.ready()) {
							
							if(currentLine.contains("wallbase.cc/wallpaper")) {
								String[] token = currentLine.split("\"");
								String linkToPage = token[1];
								String idString = linkToPage.replace("http://wallbase.cc/wallpaper/", "");
								idString = idString.replace("/", "");

								id = Integer.parseInt(idString);
								
								if(!this.idList.contains(id)) {
									loadPictureFromPage(linkToPage, id);
									loaded = true;
								} else
									alreadyOwned = true;
								break;
							}
							
							currentLine = bf.readLine();
							currentLine = currentLine.trim();
							
						}
		
					}

				}
				
				if(alreadyOwned){
					System.out.println("Already own this image: " + id);
				}
				
				
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
	}
	
	public boolean isInterruped() {
		return isInterruped;
	}

	public void setInterruped(boolean isInterruped) {
		this.isInterruped = isInterruped;
	}

	private void loadPictureFromPage(String linkToPage, Integer id) {
		
		int priority = 0;
		String siteContent = "";
		String imagetype = "";
		String filetype = "unkown (not calculated because under t) ";

		// Loads the siteContent from the given URL 
		siteContent = loadSourceCode(this.preSiteString + id);

		if(siteContent != null) {
		
			// Decide whether a site contains correct token (image description)
			priority = calculatePriority(siteContent);
			
			// Load the image, if it contains at least threshold token
			if(priority >= this.threshold) {
				
				imagetype = getFileType(siteContent);
				filetype = imagetype.substring(imagetype.length() - 3);
				
				if(imagetype.equals("rozne-jpg"))
					downloadImage(this.preImageRozneString + id + ".jpg", id, filetype);
				else if(imagetype.equals("rozne-png"))
					downloadImage(this.preImageRozneString + id + ".png", id, filetype);
				else if(imagetype.equals("high-resolution-jpg"))
					downloadImage(this.preImageHighString + id + ".jpg", id, filetype);
				else if(imagetype.equals("high-resolution-png"))
					downloadImage(this.preImageHighString + id + ".png", id, filetype);
				else if(imagetype.equals("manga-anime-jpg"))
					downloadImage(this.preImageHighString + id + ".png", id, filetype);
				else if(imagetype.equals("manga-anime-png"))
					downloadImage(this.preImageHighString + id + ".png", id, filetype);
				
				// Logging information to the current image
				printInformation(priority, filetype, id, true);
			} else {
				// Logging information to the current image
				printInformation(priority, filetype, id, true);
			}
			
			
		} else {
			// Logging information to the current image
			printInformation(priority, filetype, id, false);
		}
		
	}

	private void continueLoading(int maxId) {
		
		// Iterator for the number of pictures
				
				for(int id = maxId; id < 4000000; id++ ) {
					
					int priority = 0;
					String siteContent = "";
					String fileFormat = "";

					// Loads the siteContent from the given URL 
					siteContent = loadSourceCode(this.preSiteString + id);

					if(siteContent != null) {
					
						// Decide whether a site contains correct token (image description)
						priority = calculatePriority(siteContent);
						
						// Load the image, if it contains at least threshold token
						if(priority >= this.threshold) {
							
							String imagetype = getFileType(siteContent);
							String filetype = imagetype.substring(imagetype.length() - 3);
							
							if(imagetype.equals("rozne-jpg"))
								downloadImage(this.preImageRozneString + id + ".jpg", id, filetype);
							else if(imagetype.equals("rozne-png"))
								downloadImage(this.preImageRozneString + id + ".png", id, filetype);
							else if(imagetype.equals("high-resolution-jpg"))
								downloadImage(this.preImageHighString + id + ".jpg", id, filetype);
							else if(imagetype.equals("high-resolution-png"))
								downloadImage(this.preImageHighString + id + ".png", id, filetype);
							else if(imagetype.equals("manga-anime-jpg"))
								downloadImage(this.preImageHighString + id + ".png", id, filetype);
							else if(imagetype.equals("manga-anime-png"))
								downloadImage(this.preImageHighString + id + ".png", id, filetype);
							
							// Logging information to the current image
							printInformation(priority, filetype, id, true);
						} else {
							// Logging information to the current image
							printInformation(priority, fileFormat, id, true);
						}
						
						
					} else {
						// Logging information to the current image
						printInformation(priority, fileFormat, id, false);
					}
				
				}
		
	}

	private void startLoading() {

		continueLoading(1000000);

	}
	
	private void printInformation(int priority, String fileFormat, long id, boolean b) {
		
		if(b)
			System.out.print("Current Image has ID:" + id + ", FileFormat: " + fileFormat + ", Priority: "+ priority);
		else
			System.out.print("Current Image has ID:" + id + " and doesn't exist!");
		
		System.out.println(" "+ new Timestamp(Calendar.getInstance().getTimeInMillis())+" ");
		
	}

	private String getFileType(String siteContent) {
		
		// The more token the higher the priority
		BufferedReader bf = null;
		String fileFormat = "";
		
		
		try {
			bf = new BufferedReader(new StringReader(siteContent));

			while (bf.ready()) {
				
				
				String line = bf.readLine();				
				line = line.trim();
				
				if(line.contains("rozne") && line.contains(".jpg")) {
					return "rozne-jpg";
				}
				else if(line.contains("rozne") && line.contains(".png")) {
					return "rozne-png";
				}
				else if(line.contains("high-resolution") && line.contains(".jpg")) {
					return "high-resolution-jpg";
				}
				else if(line.contains("high-resolution") && line.contains(".png")) {
					return "high-resolution-png";
				}
				else if(line.contains("manga-anime") && line.contains(".jpg")) {
					return "manga-anime-jpg";
				}
				else if(line.contains("manga-anime") && line.contains(".png")) {
					return "manga-anime-png";
				}
				
				if(!bf.ready()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.getMessage();
		} 
		
		return "error";
		
	}

	private void downloadImage(String string, int id, String filename) {

		BufferedImage im = null;
		URL loadImageFrom = null;
		
		try {
			loadImageFrom = new URL(string);
			
			im = ImageIO.read(loadImageFrom);
			ImageIO.write(im, filename , new File(this.downloadLocation.toString() + File.separator + id + "." +filename));
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private String loadSourceCode(String string) {
	
		BufferedReader bf = null;
		String siteContent = "";

		
		try {
			URL url = new URL(string);
			bf = new BufferedReader(new InputStreamReader(url.openStream()));
			String line = "";

			while ((line = bf.readLine()) != null) {
				siteContent += line + System.getProperty("line.separator");

				if(!bf.ready()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			bf.close();

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
		
		
		return siteContent;
	}

	private int calculatePriority(String siteContent) {
		
		// The more token the higher the priority
		int priority = 0;
		BufferedReader bf = null;
		String metaToken = "";
		
		
		try {
			bf = new BufferedReader(new StringReader(siteContent));

			while (bf.ready()) {
				String line = bf.readLine();
				line = line.trim();
				
				if(line.startsWith("<meta name=\"description\" content=\"") && line.endsWith("/>")) {
					metaToken = line;
					break;
				}
			}

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Trims the TokenList accordingly
		metaToken = metaToken.replace("<meta name=\"description\" content=\"", "");
		metaToken = metaToken.replace("\" />", "");
		
		// Creates the array containing the token of the current image
		String[] imageToken = metaToken.split(" ");
		ArrayList<String> currentToken = new ArrayList<>();
		
		// Creates the ArrayList with lowercase token
		for(String s : imageToken) {
			currentToken.add(s.toLowerCase());
		}
		
		for(String token : this.preferedToken) {
			if(currentToken.contains(token)) {
				priority++;
			}
		}
		
		
		return priority;
		
		
	}

}
