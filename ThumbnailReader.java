import java.io.BufferedReader;
import java.io.IOException;
import java.nio.CharBuffer;


public class ThumbnailReader implements Readable{

	private BufferedReader br = null;
	
	public ThumbnailReader(BufferedReader br) {
		this.br = br;
	}

	@Override
	public int read(CharBuffer cb) throws IOException {
		return this.br.read();
	}
	
	public String readThumbnail() throws IOException{
		
		String thumbnailblock = "";
		
		int counter = 1;
		String currentLine = "";
		
		while(this.br.ready()) {
			currentLine = this.br.readLine();
			if(currentLine.contains("thumbs")) {
				break;
			}
		}
		
		do{
			currentLine = this.br.readLine();
		
			if(currentLine.startsWith("<div id") || currentLine.startsWith("<div class") || currentLine.startsWith("<a ")) {
				counter++;
			} else {
				counter --;
			}
			
			thumbnailblock += currentLine + "\n";
			
		} while(this.br.ready() && counter != 0);
		
		
		return thumbnailblock;
	}
	
	
	
}
