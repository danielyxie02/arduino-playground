import java.util.ArrayList;

/**
 * Beat
 *
 * Typically, a Beat will have data[] as the set of 
 * all possible values, then the true values are what
 * "happens" on the beat. For example, in a piano roll, 
 * each index of data[] represents a possible note (C,
 * C#, D, ...) and a true value means that on this beat,
 * that note "happens". No handling for tied/long notes;
 * the UNO won't handle long notes/sliders for the game
 * anyway. 
 */

public class Beat {
	private boolean[] data;

	public Beat(boolean[] data) {
		this.data = data;
	}

	public void setData(boolean[] newData) {
		this.data = newData;
	}

	public void setData(int index, boolean data) {
		this.data[index] = data;
	}

	public boolean getData(int index) {
		return this.data[index];
	}

	public boolean[] getData() {
		return this.data;
	}

	@Override
	public String toString() {
		String ret = "";
		for (int i = 0; i < data.length; i++) {
			if (data[i]) {
				ret += "1";
			} else {
				ret += "0";
			}
		}
		return ret;
	}
}