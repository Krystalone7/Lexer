import Exceptions.WrongWordsException;
import java.util.ArrayList;
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
        //лючевые слова
        String toD = "toDollars";
        String toR = "toRubles";
        //прохождение по строке
        while (pos< text.length()) {
            //получение символа
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
                    //Проверка на доллар
                    if (c == '$'){
                        f = 'd';
                        pos++;
                        c = text.charAt(pos);
                    }
                    //Получение числа
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
                            //Проверка на максимум одну точку в числе
                            if (c == '.'){
                                flag ++;
                            }
                            if (flag>1){
                                throw new RuntimeException("2 ,,");
                            }
                        } while ((c <= '9' && c >= '0') || c == '.');
                        //Запись доллара
                        if (f == 'd') {
                            lexemes.add(new Lex(LexemeType.Doll, sb.toString()));
                            f = '0';
                            continue;
                        }
                        //Проверка на рубль
                        char a = text.charAt(pos);
                        pos++;
                        if (a == 'p' || a == 'р'){
                            lexemes.add(new Lex(LexemeType.Rub, sb.toString()));
                            continue;
                        }
                        //Ключевые слова
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
                        //Если не пробел - ошибка
                        if (c != ' ') {
                            throw new WrongWordsException();
                        }
                        pos++;
                    }
            }
        }
        //Конец строки
        lexemes.add(new Lex(LexemeType.Eof, ""));
        return lexemes;
    }
}