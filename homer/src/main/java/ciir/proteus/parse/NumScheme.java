/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ciir.proteus.parse;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bzifkin
 */
  public  class NumScheme {

        boolean foundElementOnCurrPage = false;
        //int blanks;
        //int pages;

        boolean inParkLot = false;
        List<Pages.Word> sequence = new ArrayList<>();

        public NumScheme() {
        }

        public Pages.Word getLast() {
            return sequence.get(sequence.size() - 1);
        }

        public void addBlank() {

            Pages.Word blank = new Pages.Word();
            blank.isBlank = true;
            blank.text = "blank";
            sequence.add(blank);

        }

        public String toString() {
            String result = "";
            String divider = "-------------------------------------------------\n";
            for (Pages.Word w : this.sequence) {
                result = result + w.toString();
            }
            return result + divider;
        }

//use this function to count backwards to last non-blank number...useful for extrapolating missing #s
        public int getNumOfBlanks() {
            int numOfBlanks = 0;
            if (this.sequence.size() > 0) {

                for (int i = this.sequence.size() - 1; i >= 0; i--) {

                    if (this.sequence.get(i).isBlank) {

                        numOfBlanks++;
                    } else {
                        break;
                    }

                }

            }
            return numOfBlanks;
        }

        //use this function to determine when to place scheme in parking lot
        //if ratio below 50% we no longer consider adding new elements to it
        public void setParkingLot() {
            int totalPagesLookedAt = this.sequence.size();
            int totalNumBlanks = 0;
            for (int i = 0; i <= this.sequence.size() - 1; i++) {
                if (this.sequence.get(i).isBlank) {
                    totalNumBlanks++;
                }

            }
            double ratio = (double) (totalPagesLookedAt - totalNumBlanks) / (double) totalPagesLookedAt;
            if (ratio <= .50) {
                //System.out.println("setting p-lot to true. ratio: " + ratio);
                this.inParkLot = true;
            } else {
              //  System.out.println("p-lot still false. ratio " + ratio);
                this.inParkLot = false;
            }
        }

    }
