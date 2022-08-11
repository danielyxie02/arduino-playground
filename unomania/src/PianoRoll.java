import java.util.ArrayList;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class PianoRoll extends JComponent implements Runnable {
	/*****************************************
     * Misc.
     *****************************************/
	Thread synthThread = new Thread();
	/*****************************************
     * Piano roll data fields
     *****************************************/
	String sourceFile = null;
	String[] notes = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
	int octaves;
	int lowestOctave;
	int bpm = 120;
	ArrayList<Beat> roll;
	/*****************************************
     * GUI data fields
     *****************************************/
	int mainAppWidth = 900;
	int mainAppHeight = 650;
	int topPanelWidth = mainAppWidth;
	int topPanelHeight = 50;
	int noteWidth = 50;
	int noteHeight = 30;
	int rollNoteWidth = 25;
	int rollNoteHeight = noteHeight;
	/*****************************************
     * GUI fields
     *****************************************/
	JFrame appFrame;
	BorderLayout appFrameLayout;
	// top panel
	JPanel topPanel;
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
	JTextField bpmTextField;
	JButton bpmButton;
	// the "piano"
	JScrollPane noteScroller;
	JPanel notePanel;
	// piano roll
	JScrollPane rollScroller;
	JPanel rollPanel;

	/*****************************************
     * Class Constructors
     *****************************************/
	public PianoRoll(int octaves, int lowestOctave, int bpm) {
		this.octaves = octaves;
		this.lowestOctave = lowestOctave;
		this.roll = new ArrayList<Beat>();
	}

	public PianoRoll(String sourceFile) {
		this.sourceFile = sourceFile;
		this.roll = new ArrayList<Beat>();
	}

	/*****************************************
     * Generators
     *****************************************/
	// new beats always appended to roll; never delete beats or messes up counting
	private void generateBlankBeat() {
		// default 16 notes, I guess
		JPanel beatPanel = new JPanel();
		beatPanel.setLayout(new BoxLayout(beatPanel, BoxLayout.Y_AXIS));
		for (int j = 0; j < 12 * octaves; j++) {
			JPanel note = new JPanel();
			note.setLayout(new BorderLayout(0, 0));
			note.setPreferredSize(new Dimension(rollNoteWidth, rollNoteHeight));
			JButton noteButton = new JButton();
			noteButton.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0xdddddd)));
			note.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.black));
			final int tmp = roll.size() * octaves * 12 + j;
			// action listener for the button starts here
			noteButton.addActionListener(new ActionListener() {
				private boolean on = false;
				private int id = tmp;
				private Color onColor = Color.red;
				private Color offColor = notes[id % 12].indexOf("#") == -1 ? new Color(0xf2f2f2) : new Color(0xe6e6e6);
				public void actionPerformed(ActionEvent e) {
					if (on) { // if previously on, remove from data
						roll.get(id / (octaves * 12)).setData(id % (octaves * 12), false);
						((JButton)e.getSource()).setBackground(offColor);
						System.out.println("Button " + id + " turned off");
						this.on = false;
					} else { // if previously off, add to data
						roll.get(id / (octaves * 12)).setData(id % (octaves * 12), true);
						((JButton)e.getSource()).setBackground(onColor);
						System.out.println("Button " + id + " turned on");
						this.on = true;
					}
				}
			});
			// action listener for the button ends here
			if (notes[j % 12].indexOf("#") == -1) {
				noteButton.setBackground(new Color(0xf2f2f2));
			} else {
				noteButton.setBackground(new Color(0xe6e6e6));
			}
			note.add(noteButton, BorderLayout.CENTER);
			beatPanel.add(note, 0);
		}
		rollPanel.add(beatPanel);
		rollPanel.revalidate();
		rollPanel.repaint();
		rollScroller.revalidate();
		rollScroller.repaint();
		roll.add(new Beat(new boolean[octaves * 12]));
	}

	// slightly different from generateBlankBeat
	private void generateBeat(Beat beat) {
		JPanel beatPanel = new JPanel();
		beatPanel.setLayout(new BoxLayout(beatPanel, BoxLayout.Y_AXIS));
		for (int j = 0; j < 12 * octaves; j++) {
			JPanel note = new JPanel();
			note.setLayout(new BorderLayout(0, 0));
			note.setPreferredSize(new Dimension(rollNoteWidth, rollNoteHeight));
			JButton noteButton = new JButton();
			noteButton.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0xdddddd)));
			note.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.black));
			final int tmp = roll.size() * octaves * 12 + j;
			final int curIndex = j;
			// action listener for the button starts here
			noteButton.addActionListener(new ActionListener() {
				private boolean on = beat.getData(curIndex);
				private int id = tmp;
				private Color onColor = Color.red;
				private Color offColor = notes[id % 12].indexOf("#") == -1 ? new Color(0xf2f2f2) : new Color(0xe6e6e6);
				public void actionPerformed(ActionEvent e) {
					if (on) { // if previously on, remove from data
						roll.get(id / (octaves * 12)).setData(id % (octaves * 12), false);
						((JButton)e.getSource()).setBackground(offColor);
						this.on = false;
					} else { // if previously off, add to data
						roll.get(id / (octaves * 12)).setData(id % (octaves * 12), true);
						((JButton)e.getSource()).setBackground(onColor);
						this.on = true;
					}
				}
			});
			// action listener for the button ends here
			if (beat.getData(curIndex)) {
				noteButton.setBackground(Color.red);
			} else {
				if (notes[j % 12].indexOf("#") == -1) {
					noteButton.setBackground(new Color(0xf2f2f2));
				} else {
					noteButton.setBackground(new Color(0xe6e6e6));
				}
			}
			note.add(noteButton, BorderLayout.CENTER);
			beatPanel.add(note, 0);
		}
		rollPanel.add(beatPanel);
		rollPanel.revalidate();
		rollPanel.repaint();
		rollScroller.revalidate();
		rollScroller.repaint();
		roll.add(beat);
	}

	private void generateOctave(int octave) {
		for (String n : notes) {
			JPanel note = new JPanel();
			note.setLayout(new GridBagLayout());
			note.setBorder(BorderFactory.createLineBorder(Color.black));
			note.setPreferredSize(new Dimension(noteWidth - noteScroller.getVerticalScrollBar().getSize().width, noteHeight));
			JLabel noteLabel = new JLabel(String.format(n + "%d", octave));
			if (n.indexOf("#") == -1) {
				note.setBackground(Color.white);
				noteLabel.setForeground(Color.black);
				note.add(noteLabel);
			} else {
				note.setBackground(Color.black);
				noteLabel.setForeground(Color.white);
				note.add(noteLabel);
			}
			notePanel.add(note, 0);
		}
		notePanel.revalidate();
		notePanel.repaint();
		noteScroller.revalidate();
		noteScroller.repaint();
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

		// TOP PANEL
		topPanel = new JPanel();
		topPanel.setPreferredSize(new Dimension(topPanelWidth, topPanelHeight));
		topPanel.setBackground(new Color(0xc4faff));
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
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
		topPanel.add(playStopPanel);
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
		topPanel.add(exportPanel);
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
		topPanel.add(addPanel);
		// BPM panel
		bpmPanel = new JPanel();
		bpmPanel.add(new JLabel("BPM"));
		bpmTextField = new JTextField(3);
		bpmPanel.add(bpmTextField);
		bpmButton = new JButton("Set");
		bpmButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int toAdd;
				try {
					toAdd = Integer.parseInt(bpmTextField.getText());
					if (toAdd <= 0) throw new Exception();
					bpm = toAdd;
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "Invalid BPM", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		bpmPanel.add(bpmButton);
		topPanel.add(bpmPanel);
		appFrame.add(topPanel, BorderLayout.NORTH);
		

		// piano roll
		/*
		. . . .
		. . . .
		. . . .
		3 3 3 3 . . .
		2 2 2 2 . . .
		1 1 1 1 . . .
		0 0 0 0 . . .
		*/
		rollScroller = new JScrollPane();
		rollPanel = new JPanel();
		rollPanel.setBackground(Color.blue);
		rollPanel.setLayout(new BoxLayout(rollPanel, BoxLayout.X_AXIS));
		rollScroller.setViewportView(rollPanel);
		rollScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		appFrame.add(rollScroller, BorderLayout.CENTER);

		// note guidelines
		noteScroller = new JScrollPane();
		noteScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		noteScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		notePanel = new JPanel();
		notePanel.setLayout(new BoxLayout(notePanel, BoxLayout.Y_AXIS));
		notePanel.setBackground(Color.red);
		noteScroller.setViewportView(notePanel);
		noteScroller.setVerticalScrollBar(rollScroller.getVerticalScrollBar());
		noteScroller.getVerticalScrollBar().setVisible(false);
		appFrame.add(noteScroller, BorderLayout.WEST);
	}

	private void generateBlank() {
		for (int i = 0; i < octaves; i++) {
			generateOctave(lowestOctave + i);
		}
		for (int i = 0; i < 32; i++) {
			generateBlankBeat();	
		}
	}

	private void generateFromFile(String filename) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
			// generate note helpers
			String headerInfo = br.readLine();
			this.lowestOctave = Integer.parseInt(headerInfo.split("\\ ")[0]);
			this.octaves = Integer.parseInt(headerInfo.split("\\ ")[1]);
			this.bpm = Integer.parseInt(headerInfo.split("\\ ")[2]);
			for (int i = 0; i < octaves; i++) {
				generateOctave(lowestOctave + i);
			}
			// load the song into piano roll
			String line = br.readLine();
			while (line != null) {
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
			pw.printf("%d %d %d\n", lowestOctave, octaves, bpm); // important to keep track of
			for (Beat b : roll) {
				pw.println(b.toString());
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void playRollFromSynth() {
		synthThread.interrupt();
		// parse the Beats into note names first. 
		ArrayList<ArrayList<String>> rollAsNotes = new ArrayList<ArrayList<String>>();
		for (Beat b : roll) {
			ArrayList<String> cur = new ArrayList<String>();
			for (int i = 0; i < b.getData().length; i++) {
				if (b.getData()[i]) {
					cur.add(Integer.toString(lowestOctave + i / 12) + notes[i % 12]);
				}
			}
			if (cur.size() == 0) {
				rollAsNotes.add(null);
			} else {
				rollAsNotes.add(cur);
			}
		}
		//synth.playRoll(rollAsNotes);
		synthThread = new Thread(new Synth(bpm, rollAsNotes));
		synthThread.start();
	}
	/*****************************************
     * Drivers
     *****************************************/
	public void run() {
		generateAppFrame();
		if (sourceFile == null) {
			generateBlank();
		} else {
			generateFromFile(sourceFile);
		}
		bpmTextField.setText(Integer.toString(bpm));
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			SwingUtilities.invokeLater(new PianoRoll(args[0]));
		} else {
			SwingUtilities.invokeLater(new PianoRoll(2, 4, 120));
		}
	}
}