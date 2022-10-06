package pl434;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Scanner implements Iterator<Token> {

    private BufferedReader input;   // buffered reader to read file
    private boolean closed; // flag for whether reader is closed or not

    private int lineNum;    // current line number
    private int charPos;    // character offset on current line

    private String scan;    // current lexeme being scanned in
    private int nextChar;   // contains the next char (-1 == EOF)

    // reader will be a FileReader over the source file
    public Scanner (Reader reader) {
        // TODO: initialize scanner
        input = new BufferedReader(reader);
        closed = false; 
        scan = ""; 
        nextChar = readChar();
        lineNum = 1;
        charPos = 1; 
    }

    // signal an error message
    public void Error (String msg, Exception e) {
        System.err.println("Scanner: Line - " + lineNum + ", Char - " + charPos);
        if (e != null) {
            e.printStackTrace();
        }
        System.err.println(msg);
    }

    /*
     * helper function for reading a single char from input
     * can be used to catch and handle any IOExceptions,
     * advance the charPos or lineNum, etc.
     */
    private int readChar () {
        int prevChar = nextChar;
        //System.out.println("prevChar " + (char) prevChar);

        int inputChar = 0; 

        try { 
            inputChar = input.read(); 
        }catch (IOException e) {
            Error("Error reading char from input", e);
        }

        if (prevChar == '\n' || prevChar == '\r'){
            lineNum++;
            charPos = 0; 
        }
        else if (prevChar != ' '){
            charPos++; 
        }
        return inputChar;
    }

    /*
     * function to query whether or not more characters can be read
     * depends on closed and nextChar
     */
    @Override
    public boolean hasNext () {
        // TODO: implement

        if (!closed){
            return true; 
        }
        return false;
    }

    /*
     *	returns next Token from input
     *
     *  invariants:
     *  1. call assumes that nextChar is already holding an unread character
     *  2. return leaves nextChar containing an untokenized character
     *  3. closes reader when emitting EOF
     */
    @Override
    public Token next () {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        Token tok = null;
        if (hasNext()){

            while((char) nextChar == ' ' || (char) nextChar == '\t' || (char) nextChar == '\r' || (char) nextChar == '\n'){

                if (hasNext()){
                    nextChar = readChar();
                    /*if (nextChar == '\n'){
                        lineNum++; 
                        charPos = 0;
                    }*/
                    //charPos++;
                }
            }
        
            if (nextChar == -1){
                closed = true;
                return Token.EOF(lineNum, charPos);
            }
            // Keep going until reach whitespace or end of file
            while (nextChar != ' ' && nextChar != '\t' && nextChar != '\n' && nextChar != '\r' && nextChar != -1){
                // Read in Char

                if (nextChar == -1){
                    closed = true;
                    return Token.EOF(lineNum, charPos);
                }

                scan += (char) nextChar; 
                nextChar = readChar();
                //charPos++;

                // Generate token with Char
                tok = generateToken(scan, lineNum, charPos);
            
                // if token is valid, try adding more chars to it for maximal munch 
                while (tok.kind() != Token.Kind.ERROR){
                    scan += (char) nextChar;

                    if (scan.contains("//")){
                        handleComments();
                        scan = "";
                        break;
                    }
                    tok = generateToken(scan, lineNum, charPos);

                    if(tok.kind() == Token.Kind.ERROR){
                        tok = generateToken(scan.substring(0, scan.length() - 1), lineNum, charPos);
                        scan = "";
                        return tok;
                    }

                    if (hasNext()){
                        nextChar = readChar();
                        //charPos++; 
                    }
                }
            }
            //return tok; // this should fix the private test case that I missed
        }
       
        
        /*if (nextChar == '\n'){
            lineNum++; 
            charPos = 0;
        }*/

        if (nextChar == -1){
            closed = true;
            return Token.EOF(lineNum, charPos);
        }
        scan = "";
        return tok;
    }

    // OPTIONAL: add any additional helper or convenience methods
    //           that you find make for a cleaner design
    //           (useful for handling special case Tokens)

    public void handleComments(){
        while (nextChar != '\n' && nextChar != -1){
            nextChar = readChar();
            //charPos++; 
        }

        // pass the newline char
        nextChar = readChar();
        //lineNum++; 
        //charPos = 0; 

        while((char) nextChar == ' ' || (char) nextChar == '\t' || (char) nextChar == '\r' || (char) nextChar == '\n'){

            /*if (nextChar == '\n'){
                lineNum++; 
                charPos = 0;
            }*/
            if (hasNext()){
                nextChar = readChar();
                //charPos++;
            }
        }

    }

    public Token generateToken(String scan, int lineNum, int charPos){
        Token tok = new Token(scan, lineNum, charPos); 

        if (tok.kind() == Token.Kind.ERROR){
            Token specialTok = checkSpecialCases(scan); 
            if (specialTok != null){
                return specialTok; 
            }
        }

        return tok; 
    }

    public Token checkSpecialCases(String newScan){
        // See if the token is an IDENT
        if ((newScan.charAt(0) >= 'a' && newScan.charAt(0) <= 'z') || (newScan.charAt(0) >= 'A' && newScan.charAt(0) <= 'Z')){
            if (checkIDENT(newScan)){
                Token identTok = Token.IDENT(lineNum, charPos, newScan);
                return identTok;
            }
        }
        // See if the token is numerical
        else if (newScan.charAt(0) >= '0' && newScan.charAt(0) <= '9' || newScan.charAt(0) == '-'){
            if (checkNUM(newScan)){
                if (newScan.contains(".")){
                    Token floatTok = Token.FLOAT_VAL(lineNum, charPos, newScan);
                    return floatTok;
                }
                else{ 
                    Token intTok = Token.INT_VAL(lineNum, charPos, newScan);
                    return intTok;
                }
            }
            
        }
        return null;
    }

    public Boolean checkIDENT(String identifier){
        Boolean ident = true; 
        for(int j = 1; j < identifier.length(); j++){ 
            if (((identifier.charAt(j) >= 'a' && identifier.charAt(j) <= 'z') || (identifier.charAt(j) >= 'A' && identifier.charAt(j) <= 'Z') || identifier.charAt(j) == '_' || identifier.charAt(j) >= '0' && identifier.charAt(j) <= '9')){
                ident = true;
            }
            else{ 
                ident = false; 
                break;
            }
        } 
        return ident;
    }

    public Boolean checkNUM(String identifier){
        Boolean ident = true; 
        for(int j = 1; j < identifier.length(); j++){ 
            if (identifier.charAt(j) >= '0' && identifier.charAt(j) <= '9' || identifier.charAt(j) == '.'){
                ident = true;
            }
            else{ 
                ident = false; 
                break;
            }
        } 
        return ident;
    }
}
