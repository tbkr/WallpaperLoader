import java.util.Arrays;


public class Main {

	public static void main(String[] args) {
		
		// Handle user input
		if(args.length == 0){
			try {
				Loader l = new Loader();
				l.findNewWallpaperOnHotPage();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		


	}
}
