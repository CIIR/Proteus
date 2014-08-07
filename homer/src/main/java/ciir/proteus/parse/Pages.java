package ciir.proteus.parse;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bzifkin
 */
public class Pages {

    int pageNum = -1;
    int acmPageNum;
    List<Word> wordsOnPage = new ArrayList<Word>();

    public Pages() {
        
    }

    

    public List<Word> getWords() {
        return wordsOnPage;
    }

    public String toString() {
        String stuff = "";
        stuff = "---------------ACM PAGE------------------- " + acmPageNum + "\n";
        for (Word w : wordsOnPage) {
            stuff = stuff + w.toString();
        }
        return stuff;
    }
    /* consider adding a counter that counts
     number of words and assigns the value to each
     word, to help with referencing later/
     */

    public static class Word {

        int acmNum= -1;
        String text;
        boolean isBlank= false;
        int xOne; //coordinates of the words
        int yOne;
        int xTwo;
        int yTwo;

        public Word() {
        }

        public Word(String text) {
            this.text = text;
        }

        public String toString() {
            String stuff = "";

            stuff = acmNum + " WORD: " + text + "   (" + xOne + " , " + yOne + ")  (" + xTwo + " , " + yTwo + ")\n";
            return stuff;
        }
    }
}
