/*  Student information for assignment:
 *
 *  On my honor, Sneha Dharmalingam, this programming assignment is my own work
 *  and I have not provided this code to any other student.
 *
 *  Name: Sneha Dharmalingam 
 *  email address: snehadharma@utexas.edu
 *  UTEID: sd39967
 *  Section 5 digit ID: 50220
 *  Grader name: Karnika
 *  Number of slip days used on this assignment: 2
 */

// add imports as necessary
import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

/**
 * Manages the details of EvilHangman. This class keeps
 * tracks of the possible words from a dictionary during
 * rounds of hangman, based on guesses so far.
 *
 */
public class HangmanManager {

    // instance variables / fields
    Set<String> words;
    boolean debug;
    int numGuesses;
    HangmanDifficulty diff;
    ArrayList<String> activeWords;
    ArrayList<Character> guessesMade;
    String currPattern;

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * pre: words != null, words.size() > 0
     * 
     * @param words   A set with the words for this instance of Hangman.
     * @param debugOn true if we should print out debugging to System.out.
     */
    public HangmanManager(Set<String> words, boolean debugOn) {
        this.words = words;
        this.debug = debugOn;
    }

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * Debugging is off.
     * pre: words != null, words.size() > 0
     * 
     * @param words A set with the words for this instance of Hangman.
     */
    public HangmanManager(Set<String> words) {
        this.words = words;
        this.debug = false;
    }

    /**
     * Get the number of words in this HangmanManager of the given length.
     * pre: none
     * 
     * @param length The given length to check.
     * @return the number of words in the original Dictionary
     *         with the given length
     */
    public int numWords(int length) {
        int count = 0;
        for (String s : words) {
            if (s.length() == length) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get for a new round of Hangman. Think of a round as a
     * complete game of Hangman.
     * 
     * @param wordLen    the length of the word to pick this time.
     *                   numWords(wordLen) > 0
     * @param numGuesses the number of wrong guesses before the
     *                   player loses the round. numGuesses >= 1
     * @param diff       The difficulty for this round.
     */
    public void prepForRound(int wordLen, int numGuesses, HangmanDifficulty diff) {
        // creates an empty pattern based on the word length user chose
        currPattern = "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordLen; i++) {
            sb.append("-");
        }
        currPattern = sb.toString();

        // repopulates active word list based of word length user chose
        activeWords = new ArrayList<>();
        for (String s : words) {
            if (s.length() == wordLen) {
                activeWords.add(s);
            }
        }

        this.numGuesses = numGuesses;
        this.diff = diff;
        this.guessesMade = new ArrayList<>();
    }

    /**
     * The number of words still possible (live) based on the guesses so far.
     * Guesses will eliminate possible words.
     * 
     * @return the number of words that are still possibilities based on the
     *         original dictionary and the guesses so far.
     */
    public int numWordsCurrent() {
        return activeWords.size();
    }

    /**
     * Get the number of wrong guesses the user has left in
     * this round (game) of Hangman.
     * 
     * @return the number of wrong guesses the user has left
     *         in this round (game) of Hangman.
     */
    public int getGuessesLeft() {
        return numGuesses;
    }

    /**
     * Return a String that contains the letters the user has guessed
     * so far during this round.
     * The characters in the String are in alphabetical order.
     * The String is in the form [let1, let2, let3, ... letN].
     * For example [a, c, e, s, t, z]
     * 
     * @return a String that contains the letters the user
     *         has guessed so far during this round.
     */
    public String getGuessesMade() {
        Collections.sort(guessesMade);
        return guessesMade.toString();
    }

    /**
     * Check the status of a character.
     * 
     * @param guess The characater to check.
     * @return true if guess has been used or guessed this round of Hangman,
     *         false otherwise.
     */
    public boolean alreadyGuessed(char guess) {
        for (char c : guessesMade) {
            if (c == guess) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the current pattern. The pattern contains '-''s for
     * unrevealed (or guessed) characters and the actual character
     * for "correctly guessed" characters.
     * 
     * @return the current pattern.
     */
    public String getPattern() {
        return currPattern;
    }

    /**
     * Update the game status (pattern, wrong guesses, word list),
     * based on the give guess.
     * 
     * @param guess pre: !alreadyGuessed(ch), the current guessed character
     * @return return a tree map with the resulting patterns and the number of
     *         words in each of the new patterns.
     *         The return value is for testing and debugging purposes.
     */
    public TreeMap<String, Integer> makeGuess(char guess) {

        // checks if user has already guessed this letter
        if (alreadyGuessed(guess)) {
            throw new IllegalStateException("Violation of precondition: user has already"
                    + " guessed this character.");
        }

        guessesMade.add(guess);
        TreeMap<String, ArrayList<String>> patternsMap = new TreeMap<String, ArrayList<String>>();
        TreeMap<String, Integer> result = new TreeMap<String, Integer>();

        for (int i = 0; i < activeWords.size(); i++) {
            // retrieves the pattern for the current word
            String patternString = getCurrPattern(activeWords.get(i), guess);

            // adding the pattern to the map of patterns
            addToPatternsMap(patternsMap, patternString, i);

            // adding the pattern to the map of the counts
            addToResultMap(result, patternString);
        }

        // sorts the patterns based on difficulty level
        ArrayList<Pattern> findHardest = new ArrayList<>();
        for (String key : result.keySet()) {
            findHardest.add(new Pattern(key, result.get(key).intValue()));
        }
        Collections.sort(findHardest);

        // adjusts the list of active words and current pattern based upon difficulty
        String patternOfHardest = adjustDifficulty(patternsMap, findHardest);
        changeCurrPattern(patternOfHardest, guess);

        if (debug) {
            System.out.print("DEBUGGING: New pattern is: " + currPattern + ". New family has "
                    + result.get(currPattern) + " words.\n\n");
        }

        return result;
    }

    /*
     * updates the map of patterns based on the current pattern of active word and
     * user's guess
     */
    private void addToPatternsMap(TreeMap<String, ArrayList<String>> patternsMap,
            String patternString, int i) {
        if (patternsMap.containsKey(patternString)) {
            ArrayList<String> temp = patternsMap.get(patternString);
            temp.add(activeWords.get(i));
            patternsMap.put(patternString, temp);
        } else {
            ArrayList<String> temp = new ArrayList<>();
            temp.add(activeWords.get(i));
            patternsMap.put(patternString, temp);
        }
    }

    /*
     * updates and increments the result map based on the current pattern of active
     * word and user's guess
     */
    private void addToResultMap(TreeMap<String, Integer> result, String patternString) {
        if (result.containsKey(patternString)) {
            result.put(patternString, result.get(patternString) + 1);
        } else {
            result.put(patternString, 1);
        }
    }

    /*
     * updates the current pattern of the hangman word based on the pattern of
     * hardest/second hardest word calculated and if the user has guessed the
     * pattern correctly. decrements the number of guesses user has left if user 
     * guessed incorrectly.
     * 
     * @param the pattern of the hardest word set and the user's guess
     */
    private void changeCurrPattern(String patternOfHardest, char guess) {
        // changes the current pattern based on the pattern of hardest word decided
        // above and user's guess
        boolean correctGuess = false;
        for (int i = 0; i < patternOfHardest.length(); i++) {
            if (patternOfHardest.substring(i, i + 1).equals(String.valueOf(guess))) {
                correctGuess = true;
                currPattern = currPattern.substring(0, i) + String.valueOf(guess)
                        + currPattern.substring(i + 1, currPattern.length());
            }
        }

        // decrement number of guesses user has left if user guessed incorrectly
        if (!correctGuess) {
            numGuesses--;
        }
    }

    /*
     * prints out what list is being chosen based on difficulty level and elements
     * in the pattern map.
     * 
     * @param a boolean variable of if the list of patterns is short (less than or
     * equal to 1) and the index of difficulty calculated
     */
    private void printDifficulty(boolean shortList, int indexOfDifficulty) {
        System.out.println();
        if (debug && diff.equals(HangmanDifficulty.HARD)) {
            System.out.println("DEBUGGING: Picking hardest list.");
        } else if (debug && shortList && indexOfDifficulty == 0) {
            System.out.println("DEBUGGING: Should pick second hardest pattern this turn, but " +
                    "only one pattern available.\n");
            System.out.println("DEBUGGING: Picking hardest list.");
        } else if (debug && !shortList && indexOfDifficulty == 1) {
            System.out.println("DEBUGGING: Difficulty second hardest pattern and list.");
        } else if (debug && indexOfDifficulty == 0) {
            System.out.println("DEBUGGING: Picking hardest list.");
        }
    }

    /*
     * creates a pattern based on the current word parameter and the user's guess
     * 
     * @param the current word iteration in active words list and the user's guess
     */
    private String getCurrPattern(String currWord, char guess) {
        StringBuilder pattern = new StringBuilder(currPattern);

        // creates a pattern based on how many occurences of guess there are in current
        // string
        for (int j = 0; j < currWord.length(); j++) {
            if (currWord.charAt(j) == (guess)) {
                pattern.setCharAt(j, guess);
            }
        }

        return pattern.toString();
    }

    /*
     * adjusts the active words based on the difficulty level the user chose and
     * returns the string based on difficulty level
     * 
     * @param a map of all the possible patterns based of the user's guess to the
     * words that belong to that pattern and a sorted arraylist of all possible
     * patterns
     * 
     * @return the string that is the pattern based on the user's chose difficulty
     */
    private String adjustDifficulty(TreeMap<String, ArrayList<String>> patternsMap,
            ArrayList<Pattern> findHardest) {

        // gets the index of the easier list of words if it exists
        int easierDifficulty = (findHardest.size() >= 2) ? 1 : 0;
        boolean shortList = (findHardest.size() <= 1) ? true : false;

        // finds the index of the pattern based on user's chosen difficulty and
        // available patterns
        int indexOfDifficulty = findIndexDifficulty(easierDifficulty);

        // prints the chosen difficulty pattern based on debug value
        printDifficulty(shortList, indexOfDifficulty);

        // adjusts list of active words based on difficulty
        activeWords = patternsMap.get(findHardest.get(indexOfDifficulty).patternKey);
        return findHardest.get(indexOfDifficulty).patternKey;
    }

    /*
     * returns the index of the hardest or second hardest pattern based on if the
     * user chose HARD, EASY, or MEDIUM difficulty levels.
     * 
     * @param the index of the second hardest difficulty level if it exists
     * @return the index of the pattern based off the user's choice of difficulty
     */
    private int findIndexDifficulty(int easierDifficulty) {
        if (diff.equals(HangmanDifficulty.HARD)) {
            return 0;
        } else if (diff.equals(HangmanDifficulty.MEDIUM)) {
            if (guessesMade.size() % 4 == 0) {
                return easierDifficulty;
            } else {
                return 0;
            }
        } else if (diff.equals(HangmanDifficulty.EASY)) {
            if (guessesMade.size() % 2 == 0) {
                return easierDifficulty;
            } else {
                return 0;
            }
        }

        return 0;
    }

    /**
     * Return the secret word this HangmanManager finally ended up
     * picking for this round.
     * If there are multiple possible words left one is selected at random.
     * <br>
     * pre: numWordsCurrent() > 0
     * 
     * @return return the secret word the manager picked.
     */
    public String getSecretWord() {
        if (numWordsCurrent() == 0) {
            throw new IllegalStateException("Violation of precondition: there must be " +
                    "at least one active word.");
        }
        if (activeWords.size() == 1) {
            return activeWords.get(0);
        }

        int random = (int) (Math.random() * activeWords.size());
        return activeWords.get(random);
    }

    /*
     * private class to sort patterns 
     */
    private class Pattern implements Comparable<Pattern> {
        private String patternKey;
        private int numWords = 0;

        public Pattern(String patternKey, int numWords) {
            this.patternKey = patternKey;
            this.numWords = numWords;
        }

        // overrides comparable's compare to method
        public int compareTo(Pattern other) {
            int diffSize = other.numWords - numWords;
            if (diffSize == 0) {
                diffSize = numRevealedChars(patternKey) - numRevealedChars(other.patternKey);
                if (diffSize == 0) {
                    diffSize = patternKey.compareTo(other.patternKey);
                }
            }

            return diffSize;
        }

        /*
         * @return the number of characters that are not equal to "-" in the parameter
         * string
         */
        private int numRevealedChars(String s) {
            int count = 0;
            for (int i = 0; i < s.length(); i++) {
                if (!(s.substring(i, i + 1).equals("-"))) {
                    count += 1;
                }
            }
            return count;
        }
    }
}
