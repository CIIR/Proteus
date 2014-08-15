/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//package ciir.proteus.parse;

import java.util.regex.Pattern;

/**
 *
 * @author bzifkin
 */
public class RomanNumeral {
    
    public RomanNumeral(){}
    
    
   

    /*this method doesnt work quite right
     it will appropriately recognizes well formatted
     roman numerals, but will also recognize 
     incorrectly formatted numerals
     for example it interperts "iix" as 10.
     However, in the context it's being used I
     don't think this will cause problems
     */
    
    public static int romanToDecimal(String number) {
        assert (isRomanNum(number));
        int decimal = 0;
        int lastNumber = 0;
        String romanNumeral = number.toUpperCase();
        /* operation to be performed on upper cases even if user enters roman values in lower case chars */
        for (int x = romanNumeral.length() - 1; x >= 0; x--) {
            char convertToDecimal = romanNumeral.charAt(x);

            switch (convertToDecimal) {
                case 'M':
                    decimal = processDecimal(1000, lastNumber, decimal);
                    lastNumber = 1000;
                    break;

                case 'D':
                    decimal = processDecimal(500, lastNumber, decimal);
                    lastNumber = 500;
                    break;

                case 'C':
                    decimal = processDecimal(100, lastNumber, decimal);
                    lastNumber = 100;
                    break;

                case 'L':
                    decimal = processDecimal(50, lastNumber, decimal);
                    lastNumber = 50;
                    break;

                case 'X':
                    decimal = processDecimal(10, lastNumber, decimal);
                    lastNumber = 10;
                    break;

                case 'V':
                    decimal = processDecimal(5, lastNumber, decimal);
                    lastNumber = 5;
                    break;

                case 'I':
                    decimal = processDecimal(1, lastNumber, decimal);
                    lastNumber = 1;
                    break;
            }
        }
        return decimal;
    }

    public static int processDecimal(int decimal, int lastNumber, int lastDecimal) {
        if (lastNumber > decimal) {
            return lastDecimal - decimal;
        } else {
            return lastDecimal + decimal;
        }
    }

    public static boolean isRomanNum(String text) {
        text = text.toUpperCase();
        return Pattern.matches("^M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$", text);
    }
    
}
