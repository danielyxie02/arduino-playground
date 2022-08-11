// credit to https://gist.github.com/pbloem/d29bf80e69d333415622, I just made some modifications here to fit my own project
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;

/**
 * A little example showing how to play a tune in Java.
 * 
 * Inputs are not sanitized or checked, this is just to show how simple it is.
 * 
 * @author Peter
 */
public class Synth implements Runnable {
	
	private static List<String> notes = Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
	private static MidiChannel[] channels;
	private static int INSTRUMENT = 0; // 0 is a piano, 9 is percussion, other channels are for other instruments
	private static int VOLUME = 100; // between 0 et 127
	private int bpm;
	private int noteLength;
	private ArrayList<ArrayList<String>> rollNotes;

	public Synth(int bpm, ArrayList<ArrayList<String>> rollNotes) {
		this.bpm = bpm;
		this.noteLength = 60000 / (bpm * 2); // 8th notes for now
		this.rollNotes = rollNotes;
	}

	public void run() {
		try {
			// * Open a synthesizer
			Synthesizer synth = MidiSystem.getSynthesizer();
			synth.open();
			channels = synth.getChannels();
			
			for (ArrayList<String> beat : rollNotes) {
				if (beat == null) {
					rest(noteLength);
				} else {
					play(beat, noteLength);
				}
			}
			// * finish up
			synth.close();
		}
		catch (Exception e) {
			
		}
	}
	
	/**
	 * Plays the given note for the given duration
	 */
	private void play(ArrayList<String> notes, int duration) throws InterruptedException
	{
			// * start playing a note
			for (int i = 0; i < notes.size(); i++) {
				channels[INSTRUMENT].noteOn(id(notes.get(i)), VOLUME);
			}
			// * wait
			Thread.sleep( duration );
			// * stop playing a note
			for (int i = 0; i < notes.size(); i++) {
				channels[INSTRUMENT].noteOff(id(notes.get(i)));
			}
	}
	
	/**
	 * Plays nothing for the given duration
	 */
	private void rest(int duration) throws InterruptedException
	{
		Thread.sleep(duration);
	}
	
	/**
	 * Returns the MIDI id for a given note: eg. 4C -> 60
	 * @return
	 */
	private static int id(String note)
	{
		int octave = Integer.parseInt(note.substring(0, 1));
		return notes.indexOf(note.substring(1)) + 12 * octave + 12;	
	}

	public void setBpm(int bpm) {
		this.bpm = bpm;
	}

	public int getBpm() {
		return this.bpm;
	}
}