import Exceptions.WrongWordsException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;



public class Lex {
    //Все типы лексем
    public static enum LexemeType {
        LBracket, RBracket, OpPlus, OpMinus, Eof, Doll, Rub, Tod, Tor;
    }

    //Поля
    //Тип лексемы
    private LexemeType type;
    //Содержание лексемы
    private String val;
    //Геттеры
    public LexemeType getType(){
        return type;
    }
    public String getVal(){
        return val;
    }
    //Конструкторы
    public Lex(LexemeType type, String val) {
        this.type = type;
        this.val = val;
    }
    public Lex(LexemeType type, Character val) {
        this.type = type;
        this.val = val.toString();
    }
    //Лексический анализ
    public static List<Lex> lexAnalyze(String text) throws WrongWordsException {
        ArrayList<Lex> lexemes = new ArrayList<>();
        int pos = 0;
        //Сет для букв в ключевых словах
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
                            if (c == '.'){
                                flag ++;
                            }
                            if (flag>1){
                                throw new RuntimeException("2 ,,");
                            }
                        } while ((c <= '9' && c >= '0') || c == '.');
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
                    }else if(toD.contains(Character.toString(c)) || toR.contains(Character.toString(c))) {
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
}