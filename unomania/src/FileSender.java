import com.fazecast.jSerialComm.*;
import java.io.*;
import java.util.Scanner;
public class FileSender {

	public static SerialPort uno;
	public static InputStream reader;
	public static OutputStream writer;
	public static Scanner s;

	public static void portSetup() {
		try {
			uno = SerialPort.getCommPorts()[0]; // only COM4 is open for me so I just take the first choice, might be diff for others
			uno.openPort();
			uno.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
			reader = uno.getInputStream();
			writer = uno.getOutputStream();
			System.out.println(readline());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void portClose() {
		try {
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Hacky things coming in. Explanations by example because I'm dumb.
	 * Song file example: read 0000000100000000000000 -> send index of "1" (0 <= index <= 24 since we should only be using 2 octaves anyway)
	 * Since there is only one note at a time on the stepper motor, sending over an index that corresponds to a note will be fine. Use -1 to indicate a rest.
	 * Map file example: read 0101 from file -> send 5 to arduino. 
	 * Arduino parses 5 like this: AND each bit with 1 to see which keys are supposed to be turned on (can be more than one!). In this case, keys 2 and 4 turn on.
	 */
	public void sendFile(String filename, String mode) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
			// sending
			String header = br.readLine(); // for both music files and map files, 1st line is "header"
			String songFile = header.split("\\ ")[0];
			String line = br.readLine();
			while (line != null) {
				if (mode.equals("map")) {
					String lineAsString = Integer.parseInt(line, 2) + "\n";
					writer.write(lineAsString.getBytes());
					writer.flush();
					System.out.print("Sent " + lineAsString);
					System.out.println("UNO Received " + readline());
				} else if (mode.equals("song")) {
					String lineAsString = Integer.toString(line.indexOf("1")) + "\n";
					writer.write(lineAsString.getBytes());
					writer.flush();
					System.out.print("Sent " + lineAsString);
					System.out.println("UNO Received " + readline());
				}
				line = br.readLine();
			}
			writer.write("end\n".getBytes());
			writer.flush();
			// exit
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String readline() throws IOException {
		String ret = "";
		char a = (char) reader.read();
		while (a != '\n') {
			ret += a;
			a = (char) reader.read();
		}
		return ret;
	}

	public static void main(String[] args) throws IOException {
		System.out.print("Enter map name: ");
		s = new Scanner(System.in);
		String name = s.nextLine();
		FileSender fs = new FileSender();
		portSetup();
		fs.sendFile(name + ".map", "map");
		System.out.println("===================================");
		fs.sendFile(name + ".song", "song");
		portClose();
	}
}