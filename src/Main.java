import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import Exceptions.*;


public class Main {
    public static void main(String[] args) {
        File input  = new File("Dollar rate.txt");
        try{
            Scanner file = new Scanner(input);
            rate = Double.parseDouble(file.nextLine());
            System.out.print("Enter expression: ");
            Scanner in = new Scanner(System.in);
            String text = in.nextLine();
            List<Lex> lexemes = lexAnalyze(text);
            Lex first = lexemes.get(0);
            char f = ' ';
            if (first.type == LexemeType.Rub || first.type == LexemeType.Tor){
                f = 'r';
            } else if (first.type == LexemeType.Doll || first.type == LexemeType.Tod){
                f = 'd';
            }
            LexemeBuf lexBuf = new LexemeBuf(lexemes);
            double result = expr(lexBuf);
            if (f == 'r'){
                System.out.println(result + "p");
            } else{
                System.out.println("$" + result);
            }

        }
        catch (FileNotFoundException e){
            System.out.println("The file is missing");
        }
        catch (NumberFormatException e){
            System.out.println("Invalid dollar rate");
        }
        catch (WrongWordsException e){
            System.out.println("Unexpected character in expression");
        }
        catch (IncorrectSyntaxException e){
            System.out.println("Incorrect Syntax");
        }

    }
    //toDollars(5.5p + toRubles($69))
    //5p + toRubles($56)
    /*   Tod Lbr Rub Plus Tor Lbr Doll Rbr Rbr Eof
         Rub Plus Tor Lbr Doll Rbr
    *  =  {0 .... infinity}
    expr : plusR Eof | plusD Eof
    plusR : rubF (( '+' | '-') rubF)* ;
    plusD : dollF (('+' | '-') dollF)* ;
    rubF : Rub | "toRubles" '(' plusD ')';
    dollF : Doll | "toDollars" '(' plusR ')' ;
     */

    public static double expr(LexemeBuf lexemes) throws IncorrectSyntaxException{
        Lex lex = lexemes.next();
        switch (lex.type){
            case Rub:
            case Tor:
                lexemes.back();
                return plusR(lexemes);
            case Doll:
            case Tod:
                lexemes.back();
                return plusD(lexemes);
            default:
                throw new IncorrectSyntaxException();

        }
    }
    public static double plusR(LexemeBuf lexemes) throws IncorrectSyntaxException{
        double val = rubF(lexemes);
        while (true){
            Lex lex = lexemes.next();
            switch (lex.type){
                case OpPlus:
                    val += rubF(lexemes);
                    break;
                case OpMinus:
                    val -= rubF(lexemes);
                    break;
                case Eof:
                    lexemes.back();
                    return val;
                case RBracket:
                    lexemes.back();
                    return val/rate;
                default:
                    throw new IncorrectSyntaxException();
            }

        }
    }
    public static double plusD(LexemeBuf lexemes) throws IncorrectSyntaxException{
        double val = dollF(lexemes);
        while (true){
            Lex lex = lexemes.next();
            switch (lex.type){
                case OpPlus:
                    val += dollF(lexemes);
                    break;
                case OpMinus:
                    val -= dollF(lexemes);
                    break;
                case Eof:
                    lexemes.back();
                    return val ;
                case RBracket:
                    lexemes.back();
                    return val*rate;
                default:
                    throw new IncorrectSyntaxException();
            }

        }
    }
    public static double rubF(LexemeBuf lexemes) throws IncorrectSyntaxException{
        Lex lex = lexemes.next();
        switch (lex.type){
            case Rub:
                return Double.parseDouble(lex.val);
            case Tor:
                lex = lexemes.next();
                if (lex.type == LexemeType.LBracket){
                    double val = plusD(lexemes);
                    lex = lexemes.next();
                    if (lex.type != LexemeType.RBracket){
                        throw new IncorrectSyntaxException();
                    }
                    return val;
                }else {
                    throw new IncorrectSyntaxException();
                }
            default:
                throw new IncorrectSyntaxException();
        }
    }
    public static double dollF(LexemeBuf lexemes) throws IncorrectSyntaxException{
        Lex lex = lexemes.next();
        switch (lex.type){
            case Doll:
                return Double.parseDouble(lex.val);
            case Tod:
                lex = lexemes.next();
                if (lex.type == LexemeType.LBracket){
                    double val = plusR(lexemes);
                    lex = lexemes.next();
                    if (lex.type != LexemeType.RBracket){
                        throw new IncorrectSyntaxException();
                    }
                    return val;
                }
            default:
                throw new IncorrectSyntaxException();
        }
    }
    public enum LexemeType {
        LBracket, RBracket, OpPlus, OpMinus, Eof, Doll, Rub, Tod, Tor;
    }
    public static class Lex {
        public LexemeType type;
        public String val;
        public Lex(LexemeType type, String val) {
            this.type = type;
            this.val = val;
        }
        public Lex(LexemeType type, Character val) {
            this.type = type;
            this.val = val.toString();
        }
    }
    public static class LexemeBuf {
        private int pos;

        public List<Lex> lexemes;

        public LexemeBuf(List<Lex> lexemes) {
            this.lexemes = lexemes;
        }

        public Lex next() {
            return lexemes.get(pos++);
        }

        public void back() {
            pos--;
        }

        public int getPos() {
            return pos;
        }
    }
    public static List<Lex> lexAnalyze(String text) throws WrongWordsException {
        ArrayList<Lex> lexemes = new ArrayList<>();
        int pos = 0;
        HashSet<Character> keys = new HashSet<>();
        keys.add('t'); keys.add('o');keys.add('D');keys.add('l');keys.add('a');
        keys.add('r');keys.add('s');keys.add('R');keys.add('u');keys.add('b');keys.add('e');
        String toD = "toDollars";
        String toR = "toRubles";
        while (pos< text.length()) {
            char c = text.charAt(pos);
            switch (c) {
                case '(':
                    lexemes.add(new Lex(LexemeType.LBracket, c));
                    pos++;
                    continue;
                case ')':
                    lexemes.add(new Lex(LexemeType.RBracket, c));
                    pos++;
                    continue;
                case '+':
                    lexemes.add(new Lex(LexemeType.OpPlus, c));
                    pos++;
                    continue;
                case '-':
                    lexemes.add(new Lex(LexemeType.OpMinus, c));
                    pos++;
                    continue;
                default:
                    char f = '0';
                    if (c == '$'){
                        f = 'd';
                        pos++;
                        c = text.charAt(pos);
                    }
                    if (c <= '9' && c >= '0') {
                        StringBuilder sb = new StringBuilder();
                        int flag = 0;
                        do {
                            sb.append(c);
                            pos++;
                            if (pos >= text.length()) {
                                break;
                            }
                            c = text.charAt(pos);
                            if (c == ','){
                                flag ++;
                            }
                            if (flag>1){
                                throw new RuntimeException("2 ,,");
                            }
                        } while ((c <= '9' && c >= '0') || c == ',');
                        if (f == 'd'){
                            lexemes.add(new Lex(LexemeType.Doll, sb.toString()));
                            f = '0';
                            continue;
                        }
                        char a = text.charAt(pos);
                        pos++;
                        if (a == 'p' || a == 'р'){
                            lexemes.add(new Lex(LexemeType.Rub, sb.toString()));
                            continue;
                        }
                    }else if(keys.contains(c)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(c);
                        while (toD.contains(sb.toString()) || toR.contains(sb.toString())){
                            pos++;
                            if (pos >= text.length()) {
                                break;
                            }
                            c = text.charAt(pos);
                            sb.append(c);
                        }
                        if (sb.substring(0,sb.length()-1).toString().equals(toD)){
                            lexemes.add(new Lex(LexemeType.Tod, sb.substring(0,sb.length()-1).toString()));
                        }
                        else if (sb.substring(0,sb.length()-1).toString().equals(toR)){
                            lexemes.add(new Lex(LexemeType.Tor, sb.substring(0,sb.length()-1).toString()));
                        }
                        else{
                            throw new WrongWordsException();
                        }
                    } else{
                        if (c != ' ') {
                            throw new WrongWordsException();
                        }
                        pos++;
                    }
            }
        }
        lexemes.add(new Lex(LexemeType.Eof, ""));
        return lexemes;
    }
    public static double rate;
}
