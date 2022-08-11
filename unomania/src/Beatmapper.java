import java.util.ArrayList;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class Beatmapper extends JComponent implements Runnable {
	/*****************************************
     * Misc.
     *****************************************/
	Thread synthThread = new Thread();
	/*****************************************
     * Piano roll data fields
     *****************************************/
	String songFile = null;
	String mapFile = null;
	int keys;
	int bpm = 120;
	ArrayList<Beat> map;
	/*****************************************
     * GUI data fields
     *****************************************/
	int mainAppWidth = 500;
	int mainAppHeight = 650;
	int sidePanelWidth = 200;
	int sidePanelHeight = mainAppHeight;
	int mapNoteWidth = 50;
	int mapNoteHeight = 30;
	/*****************************************
     * GUI fields
     *****************************************/
	JFrame appFrame;
	BorderLayout appFrameLayout;
	// top panel
	JPanel sidePanel;
	JPanel exportPanel;
	JButton exportButton;
	JTextField exportFilename;
	JPanel playStopPanel;
	JButton playButton;
	JButton stopButton;
	JPanel addPanel;
	JButton addButton;
	JTextField addMeasures;
	JPanel bpmPanel;
	JLabel bpmLabel;
	JButton bpmButton;
	// the "piano"
	JScrollPane noteScroller;
	JPanel notePanel;
	// piano roll
	JScrollPane mapScroller;
	JPanel mapPanel;

	/*****************************************
     * Class Constructors
     *****************************************/
	public Beatmapper(String songFile, int keys) {
		this.songFile = songFile;
		this.keys = keys;
		map = new ArrayList<Beat>();
	}

	public Beatmapper(String mapFile) {
		this.songFile = songFile;
		this.keys = keys;
		this.mapFile = mapFile;
		map = new ArrayList<Beat>();
	}

	/*****************************************
     * Generators
     *****************************************/
	// new beats always appended to map; never delete beats or messes up counting
	private void generateBlankBeat() {
		// default 16 notes, I guess
		JPanel beatPanel = new JPanel();
		beatPanel.setLayout(new BoxLayout(beatPanel, BoxLayout.X_AXIS));
		for (int j = 0; j < keys; j++) {
			JPanel note = new JPanel();
			note.setLayout(new BorderLayout(0, 0));
			note.setPreferredSize(new Dimension(mapNoteWidth, mapNoteHeight));
			JButton noteButton = new JButton();
			noteButton.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0xdddddd)));
			note.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.black));
			final int tmp = map.size() * keys + j;
			// action listener for the button starts here
			noteButton.addActionListener(new ActionListener() {
				private boolean on = false;
				private int id = tmp;
				private Color onColor = Color.red;
				private Color offColor = new Color(0xe6e6e6);
				public void actionPerformed(ActionEvent e) {
					if (on) { // if previously on, remove from data
						System.out.println("Button " + id + " turned off");
						map.get(id / keys).setData(id % (keys), false);
						((JButton)e.getSource()).setBackground(offColor);
						this.on = false;
					} else { // if previously off, add to data
						System.out.println("Button " + id + " turned on");
						map.get(id / keys).setData(id % (keys), true);
						((JButton)e.getSource()).setBackground(onColor);
						this.on = true;
					}
				}
			});
			// action listener for the button ends here
			noteButton.setBackground(new Color(0xe6e6e6));
			note.add(noteButton, BorderLayout.CENTER);
			beatPanel.add(note);
		}
		mapPanel.add(beatPanel, 0);
		mapPanel.revalidate();
		mapPanel.repaint();
		mapScroller.revalidate();
		mapScroller.repaint();
		map.add(new Beat(new boolean[keys]));
	}

	// slightly different from generateBlankBeat
	private void generateBeat(Beat beat) {
		JPanel beatPanel = new JPanel();
		beatPanel.setLayout(new BoxLayout(beatPanel, BoxLayout.X_AXIS));
		for (int j = 0; j < keys; j++) {
			JPanel note = new JPanel();
			note.setLayout(new BorderLayout(0, 0));
			note.setPreferredSize(new Dimension(mapNoteWidth, mapNoteHeight));
			JButton noteButton = new JButton();
			noteButton.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0xdddddd)));
			note.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.black));
			final int tmp = map.size() * keys + j;
			final int curIndex = j;
			// action listener for the button starts here
			noteButton.addActionListener(new ActionListener() {
				private boolean on = beat.getData(curIndex);
				private int id = tmp;
				private Color onColor = Color.red;
				private Color offColor = new Color(0xe6e6e6);
				public void actionPerformed(ActionEvent e) {
					if (on) { // if previously on, remove from data
						map.get(id / keys).setData(id % (keys), false);
						((JButton)e.getSource()).setBackground(offColor);
						System.out.println("Button " + id + " turned off");
						this.on = false;
					} else { // if previously off, add to data
						map.get(id / keys).setData(id % (keys), true);
						((JButton)e.getSource()).setBackground(onColor);
						System.out.println("Button " + id + " turned on");
						this.on = true;
					}
				}
			});
			// action listener for the button ends here
			if (beat.getData(j)) {
				noteButton.setBackground(Color.red);
			} else {
				noteButton.setBackground(new Color(0xe6e6e6));
			}
			note.add(noteButton, BorderLayout.CENTER);
			beatPanel.add(note);
		}
		mapPanel.add(beatPanel, 0);
		mapPanel.revalidate();
		mapPanel.repaint();
		mapScroller.revalidate();
		mapScroller.repaint();
		map.add(beat);
	}

	private void generateAppFrame() {
		// frame
		appFrame = new JFrame("Piano Roll");
		appFrame.setSize(mainAppWidth, mainAppHeight);
		appFrameLayout = new BorderLayout();
		appFrame.setLayout(appFrameLayout);
		appFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		appFrame.setLocationRelativeTo(null);
		appFrame.setVisible(true);

		// SIDE PANEL
		sidePanel = new JPanel();
		sidePanel.setPreferredSize(new Dimension(sidePanelWidth, sidePanelHeight));
		sidePanel.setBackground(new Color(0xc4faff));
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
		// play/pause panel
		playStopPanel = new JPanel();
		playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playRollFromSynth();
			} 
		});
		playStopPanel.add(playButton);
		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (synthThread != null)
					synthThread.interrupt();
			} 
		});
		playStopPanel.add(stopButton);
		sidePanel.add(playStopPanel);
		// export panel
		exportPanel = new JPanel();
		exportButton = new JButton("Export");
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				export(exportFilename.getText());
			} 
		});
		exportPanel.add(exportButton);
		exportFilename = new JTextField(10);
		exportPanel.add(exportFilename);
		sidePanel.add(exportPanel);
		// add measure panel
		addPanel = new JPanel();
		addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int toAdd;
				try {
					toAdd = Integer.parseInt(addMeasures.getText());
					if (toAdd <= 0) throw new Exception();
					for (int i = 0; i < toAdd; i++) {
						generateBlankBeat();
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "Invalid measures to add", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		addPanel.add(addButton);
		addMeasures = new JTextField(2);
		addPanel.add(addMeasures);
		sidePanel.add(addPanel);
		// BPM panel
		bpmPanel = new JPanel();
		bpmLabel = new JLabel("BPM ???");
		bpmPanel.add(bpmLabel);
		sidePanel.add(bpmPanel);
		appFrame.add(sidePanel, BorderLayout.EAST);
		

		// map
		/* ex. ids for 4 keys
		. . . .
		. . . .
		4 5 6 7
		0 1 2 3
		*/
		mapScroller = new JScrollPane();
		mapPanel = new JPanel();
		mapPanel.setBackground(Color.blue);
		mapPanel.setLayout(new BoxLayout(mapPanel, BoxLayout.Y_AXIS));
		mapScroller.setViewportView(mapPanel);
		mapScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		appFrame.add(mapScroller, BorderLayout.WEST);
	}

	private void generateBlank() {
		for (int i = 0; i < 32; i++) {
			generateBlankBeat();	
		}
	}

	
	private void generateFromFile(String filename) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
			br.readLine();  // skip the header
			String line = br.readLine();
			while (line != null) {
				//System.out.println(line);
				boolean[] beatData = new boolean[line.length()];
				for (int i = 0; i < line.length(); i++) {
					beatData[i] = line.charAt(i) == '1';
				}
				Beat b = new Beat(beatData);
				generateBeat(b);
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*****************************************
     * Functionality
     *****************************************/
	private void export(String filename) {
		try {
			File f = new File(filename);
			PrintWriter pw = new PrintWriter(f);
			pw.printf("%s %d\n", songFile, keys);
			for (Beat b : map) {
				pw.println(b.toString());
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void playRollFromSynth() {

	}

	// only pulls song's BPM for now, can add more if needed
	private void parseSongFile() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(songFile)));
			String headerInfo = br.readLine();
			this.bpm = Integer.parseInt(headerInfo.split("\\ ")[2]);
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// gets songFile and keys
	private void parseMapFile() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(mapFile)));
			String headerInfo = br.readLine();
			this.songFile = headerInfo.split("\\ ")[0];
			this.keys = Integer.parseInt(headerInfo.split("\\ ")[1]);
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*****************************************
     * Drivers
     *****************************************/
	public void run() {
		generateAppFrame();
		if (mapFile == null) {
			parseSongFile();
			generateBlank();
		} else {
			parseMapFile();
			parseSongFile();
			generateFromFile(mapFile);
		}
		bpmLabel.setText("BPM " + bpm);
		// little bit of magic number cheese for padding out the key panel
		appFrame.setSize(new Dimension(mapNoteWidth * keys + 50 + sidePanelWidth, mainAppHeight));
	}

	public static void main(String[] args) {
		try {
			if (args.length == 1) {
				SwingUtilities.invokeLater(new Beatmapper(args[0]));
			} else if (args.length == 2) {
				SwingUtilities.invokeLater(new Beatmapper(args[0], Integer.parseInt(args[1])));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}