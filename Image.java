import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class Image {

	public String imagePath;
	public String filePath;
	public String fileName;
	
	public ArrayList<String> tagList = new ArrayList<>();

	int id;
	int width;
	int height;


	/**
	 * Constructor for a website which holds the link to the image with id <em>id<em>
	 * @param lines
	 * @param id
	 */
	public Image(String[] lines, Integer id) {
		
		this.id = id;
		
		for(int i = 0; i < lines.length; ++i) {
			
			if(lines[i].startsWith("><a class=\"preview\" href=\"http://alpha.wallhaven.cc/wallpaper/"+id)) {
				
				imagePath = "http://alpha.wallhaven.cc/wallpaper/"+id;
				collectFurtherInformationForImage(imagePath);
				break;
			}
		}
		
	}


	private void collectFurtherInformationForImage(String imagePath) {
		// TODO Auto-generated method stub
		String[] content = null;
		try {
			content = Loader.getURLContentAsStringArray(new URL(imagePath));
			parseImagePage(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parseImagePage(String[] lines) throws Exception {
		
		
		// Gather meta information
		
		for(int i = 0; i < lines.length; ++i) {
			
			// TODO add robustness
			if(lines[i].contains("//wallpapers.wallhaven.cc/wallpapers/full/wallhaven")) {
				
				// Part at quotation marks
				String parts[] = lines[i].split("\"");
				
				filePath = "http:" + parts[1];
				// Set filename correctly
				this.fileName = this.filePath.split("-")[1]; 
				System.out.println("Fount download link: " + filePath);
				continue;
			}
			if(lines[i].contains("data-wallpaper-width=")) {
				
				// The part in quotation marks
				String parts[] = lines[i].split("\"");
				
				width = Integer.parseInt(parts[1]);
				continue;
			}
			if(lines[i].contains("data-wallpaper-height=")) {
				
				// The part in quotation marks
				String parts[] = lines[i].split("\"");
				
				height = Integer.parseInt(parts[1]);
				continue;
			}
			if(lines[i].contains("><a class=\"tagname\" rel=\"tag\" href=\"")) {
				
				// Part at closing
				String parts[] = lines[i].split(">");
				
				String tagWithSuffix = parts[2];
				
				tagList.add(tagWithSuffix.split("<")[0].toLowerCase());
				
				continue;
			}
		}
		

		
		

	}
	
	
	
}
