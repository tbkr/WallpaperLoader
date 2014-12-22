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
		parser.addArgument("-p", "--purity").type(String.class)
				.setDefault("sfw");
		parser.addArgument("-c", "--category").type(String.class)
				.setDefault("all");

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

			switch (pargs.getString("purity")) {
			case "sfw":
				l.setPuritySubString("purity=100");
				break;
			case "sketchy":
				l.setPuritySubString("purity=010");
				break;
			case "both":
				l.setPuritySubString("purity=110");
				break;
			}

			switch (pargs.getString("category")) {
			case "all":
				l.setCategoriesSubString("categories=111");
				break;
			case "people":
				l.setCategoriesSubString("categories=001");
				break;
			case "anime":
				l.setCategoriesSubString("categories=010");
				break;
			case "general":
				l.setCategoriesSubString("categories=100");
				break;
			case "animegeneral":
				l.setCategoriesSubString("categories=110");
				break;
			case "animepeople":
				l.setCategoriesSubString("categories=011");
				break;
			case "generalpeople":
				l.setCategoriesSubString("categories=101");
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
