import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;

public class Main {
	public static void main(String[] args) {
		try {
//			// Handle User Input
//			ArgumentParser parser = ArgumentParsers
//					.newArgumentParser("WallpaperLoader")
//					.defaultHelp(true)
//					.description(
//							"Automatically download Wallapers from wallhaven.cc");
			Loader l = new Loader();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
