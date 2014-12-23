import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
		parser.addArgument("-c", "--category")
				// .action(Arguments.append())
				.choices(new String[] { "all", "anime", "people", "general" })
				.nargs("+").setDefault("all");

		parser.addArgument("--help").action(Arguments.help());

		try {
			Namespace pargs = parser.parseArgs(args);
			Loader l = new Loader();

			// set desired width and height
			l.setHeightLimit(pargs.getInt("height"));
			l.setWidthLimit(pargs.getInt("width"));

			// set desired type
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

			// set desired purity setting
			switch (pargs.getString("purity")) {
			case "sfw":
				// only sfw
				l.setPuritySubString("purity=100");
				break;
			case "sketchy":
				// only sketchy
				l.setPuritySubString("purity=010");
				break;
			default:
				// both
				l.setPuritySubString("purity=110");
				break;

			}

			// set desired category
			Map<String, Integer> categories = new HashMap<String, Integer>();
			categories.put("all", 0b111);
			categories.put("people", 0b001);
			categories.put("anime", 0b010);
			categories.put("general", 0b100);

			int cat = 0;
			if (pargs.get("category").getClass() == ArrayList.class) {
				for (Object o : pargs.getList("category")) {
					String cstr = (String) o;
					if (categories.containsKey(cstr)) {
						cat |= categories.get(cstr);
					}
				}
			} else {
				cat = categories.get("all");
			}
			l.setCategoriesSubString(String.format("categories=%3s",
					Integer.toBinaryString(cat)).replace(' ', '0'));

			// start loading!
			l.startDownload();
		} catch (ArgumentParserException e) {
			parser.handleError(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
