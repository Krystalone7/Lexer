import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;



public class Main {
    public static void main(String[] args) {
        String line = "";
        try
        {
            File file = new File("Dollar rate.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            line  = reader .readLine();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        rate = Integer.parseInt(line);
        System.out.println("Enter expression :");
        Scanner in = new Scanner(System.in);
        String text = in.nextLine();
        List<Lex> lexemes = lexAnalyze(text);

        LexemeBuf lexBuf = new LexemeBuf(lexemes);
        double result = expr(lexBuf);
        System.out.println(result);
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

    public static double expr(LexemeBuf lexemes){
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
                throw new RuntimeException("expr Unexpected token " + lex.val + " at " + lexemes.getPos());

        }
    }
    public static double plusR(LexemeBuf lexemes){
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
                    throw new RuntimeException("plusR Unexpected token " + lex.val + " at " + lexemes.getPos());
            }

        }
    }
    public static double plusD(LexemeBuf lexemes){
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
                    throw new RuntimeException("plusD Unexpected token " + lex.val + " at " + lexemes.getPos());
            }

        }
    }
    public static double rubF(LexemeBuf lexemes){
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
                        throw new RuntimeException("Braaaackeets!");
                    }
                    return val;
                }else {
                    throw new RuntimeException("rubF1");
                }
            default:
                throw new RuntimeException("rubF2");
        }
    }
    public static double dollF(LexemeBuf lexemes){
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
                        throw new RuntimeException("Braaaackeets!");
                    }
                    return val;
                }
            default:
                throw new RuntimeException("dollF");
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
    public static List<Lex> lexAnalyze(String text) {
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
                            throw new RuntimeException("Baaad words");
                        }
                    } else{
                        if (c != ' ') {
                            throw new RuntimeException("Unexpected character: " + c);
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
