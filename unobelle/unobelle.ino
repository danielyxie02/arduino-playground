#include <Adafruit_MCP23017.h>
#include <LiquidCrystal.h>

#define REST 24
#define LONGNOTE 25

// music-related
int dt = 200;
unsigned long songDt = 400;

// main state control: 
// 0 - editing 
// 1 - scrolling through Isabelle text
// 2 - playing the song
int state = 0;

// input button setup
Adafruit_MCP23017 mcp;
int playButton = A0;

// LCD setup
int rs = 7;
int en = 8;
int d4 = 9;
int d5 = 10;
int d6 = 11;
int d7 = 12;
LiquidCrystal lcd(rs, en, d4, d5, d6, d7);
byte sharp[8] = {
  B00000,
  B01010,
  B11111,
  B01010,
  B01010,
  B11111,
  B01010,
  B00000
};

// stepper motor setup
int stepPin = 3;
int dirPin = 4;

// dialogue-related
int dialogueState = 0;
String phrases[] = {"Well, I do have ",
                    "a little musical ",
                    "knowledge... ",
                    "Allow me to ",
                    "preview the tune ",
                    "you created!",};

// musical things
int noteDelays[] = {1895, 1787, 1687, 1592, 1503, 1418, 1337, 1263, 1192, 1125, 1062, 1001,
                    945, 891, 842, 794, 750, 707, 667, 629, 595, 561, 529, 500};
String notes[] = {"c ", "c+", "d ", "d+", "e ", "f ", "f+", "g ", "g+", "a ", "a+", "b ", // lower octave
                  "C ", "C+", "D ", "D+", "E ", "F ", "F+", "G ", "G+", "A ", "A+", "B ", // higher octave
                  "--", "  "};  // rest, long note
int maxValidNote = 25;
int song[16] = {0};
int selectedNote = -1;

void printSong() {
  lcd.clear();
  for(int i = 0; i < 8; i++){
    lcd.setCursor(i * 2, 0);
    if (notes[song[i]][1] == '+') {
      lcd.print(notes[song[i]][0]);
      lcd.write(byte(0)); // sharp
    } else {
      lcd.print(notes[song[i]]);
    }
    
  }
  for(int i = 0; i < 8; i++){
    lcd.setCursor(i * 2, 1);
    if (notes[song[i+8]][1] == '+') {
      lcd.print(notes[song[i+8]][0]);
      lcd.write(byte(0)); // sharp
    } else {
      lcd.print(notes[song[i+8]]);
    }
  }
}

bool pollPlayButton() {
  return digitalRead(playButton) == LOW ? true : false;
}

int pollEditorButtons() {
  for (int i = 0; i < 16; i++) {
    if (mcp.digitalRead(i) == LOW) {
      return i;
    }
  }
  return -1;
}

void updateSong(int index) {
  song[index]++;
  if (song[index] > maxValidNote) {
    song[index] = 0; 
  }
  printSong();
}

void dialogue(int dState) {
  if (dState == 5) {  // if we're done reading all the dialogue
    state = 2;
    return;
  }
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(phrases[dState]);
  lcd.setCursor(0, 1);
  lcd.print(phrases[dState + 1]);
}

void playSong() {
  unsigned long startTime = millis();
  int curLen;
  int curNote;
  lcd.clear();
  printSong();
  lcd.cursor();
  for (int i = 0; i < 16; i++) {
    if (i < 8) {
      lcd.setCursor(i * 2, 0);
    } else {
      lcd.setCursor((i - 8) * 2, 1);
    }
    if (song[i] != LONGNOTE) {
      curNote = song[i];
    }
    if (i != 15 && song[i + 1] == LONGNOTE) {
      curLen = songDt;
    } else {
      curLen = songDt / 2;
    }
    while (millis() - startTime < songDt) {
      if (song[i] != REST && millis() - startTime < curLen) {
        digitalWrite(stepPin, HIGH);
        delayMicroseconds(noteDelays[curNote]);
        digitalWrite(stepPin, LOW);
        delayMicroseconds(noteDelays[curNote]);
      }
    } 
    startTime = millis();
  }
  lcd.noCursor();
  printSong();
  state = 0;
}

void setup() {
  // debug
  Serial.begin(9600);
  // input setups
  pinMode(playButton, INPUT_PULLUP);
  mcp.begin();
  for (int i = 0; i < 16; i++) {
    mcp.pinMode(i, INPUT);
    mcp.pullUp(i, HIGH);
  }
  // LCD setup
  lcd.begin(16, 2);
  lcd.createChar(0, sharp);
  // stepper motor setup
  pinMode(stepPin, OUTPUT);
  pinMode(dirPin, OUTPUT);
  digitalWrite(dirPin, LOW);
  // state machine initialization (just in case)
  state = 0;
  // print the default song to start
  printSong();
}
  
void loop() {
  switch (state){
    case 0:  // editor state
      // poll play button and if play button pressed, break
      if (pollPlayButton()) {
        dialogueState = 0;
        dialogue(dialogueState);
        state = 1;
        break;
      }
      // poll editor buttons, update song if needed
      selectedNote = pollEditorButtons();
      if (selectedNote != -1) {
        updateSong(selectedNote);
      }
      break;
    case 1:  // dialogue state
      // poll play button, advance dialogue if needed
      if (pollPlayButton()) {
        dialogueState++;
        dialogue(dialogueState);
      }
      break;
    case 2:  // play song
      playSong();
    default:
      break;
  }
  delay(dt);
}
