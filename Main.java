import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {
	public static void main(String[] args) {
		// Handle User Input
		ArgumentParser parser = ArgumentParsers.newArgumentParser(
				"WallpaperLoader", false).description(
				"Automatically download Wallapers from wallhaven.cc");
		parser.addArgument("-w", "--width").type(Integer.class).setDefault(0);
		parser.addArgument("-h", "--height").type(Integer.class).setDefault(0);
		parser.addArgument("-t", "--type")
				.choices(new String[] { "hot", "random", "latest" })
				.setDefault("hot");
		parser.addArgument("--help").action(Arguments.help());

		try {
			Namespace pargs = parser.parseArgs(args);
			Loader l = new Loader();
			l.setHeightLimit(pargs.getInt("height"));
			l.setWidthLimit(pargs.getInt("width"));

			switch (pargs.getString("type")) {
			case "random":
				l.setSearchSubString("/random?");
				l.setSortingSubString("");
				break;
			case "latest":
				l.setSearchSubString("/latest?");
				l.setSortingSubString("");
				break;
			default:
				l.setSearchSubString("/search?");
				break;
			}

			l.startDownload();
		} catch (ArgumentParserException e) {
			parser.handleError(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
