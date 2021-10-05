import Exceptions.IncorrectSyntaxException;
import java.util.List;

public class LexemeBuf {
    //Поля
    //Позиция
    private int pos;
    //Курс доллара для подсчета
    private double rate;
    //Коллекция лексем
    private List<Lex> lexemes;

    //Конструктор
    public LexemeBuf(List<Lex> lexemes,double rate) {
        this.lexemes = lexemes;
        this.rate = rate;
    }
    //Следующая лексема
    public Lex next() {
        return lexemes.get(pos++);
    }
    //Вернуть позицию назад
    public void back() {
        pos--;
    }
    //Получить текущую позицию
    public int getPos() {
        return pos;
    }
    //Синтаксический анализатор и нужные ему доп методы
    /*
    Синтаксис
    *  =  {0 .... infinity}
    expr : plusR Eof | plusD Eof
    plusR : rubF (( '+' | '-') rubF)* ;
    plusD : dollF (('+' | '-') dollF)* ;
    rubF : Rub | "toRubles" '(' plusD ')';
    dollF : Doll | "toDollars" '(' plusR ')' ;
    */
    public static double syntaxAnalyzer(LexemeBuf lexemes) throws IncorrectSyntaxException {
        Lex lex = lexemes.next();
        switch (lex.getType()){
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
            switch (lex.getType()){
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
                    return val/ lexemes.rate;
                default:
                    throw new IncorrectSyntaxException();
            }

        }
    }
    public static double plusD(LexemeBuf lexemes) throws IncorrectSyntaxException{
        double val = dollF(lexemes);
        while (true){
            Lex lex = lexemes.next();
            switch (lex.getType()){
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
                    return val*lexemes.rate;
                default:
                    throw new IncorrectSyntaxException();
            }

        }
    }
    public static double rubF(LexemeBuf lexemes) throws IncorrectSyntaxException{
        Lex lex = lexemes.next();
        switch (lex.getType()){
            case Rub:
                return Double.parseDouble(lex.getVal());
            case Tor:
                lex = lexemes.next();
                if (lex.getType() == Lex.LexemeType.LBracket){
                    double val = plusD(lexemes);
                    lex = lexemes.next();
                    if (lex.getType() != Lex.LexemeType.RBracket){
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
        switch (lex.getType()){
            case Doll:
                return Double.parseDouble(lex.getVal());
            case Tod:
                lex = lexemes.next();
                if (lex.getType() == Lex.LexemeType.LBracket){
                    double val = plusR(lexemes);
                    lex = lexemes.next();
                    if (lex.getType() != Lex.LexemeType.RBracket){
                        throw new IncorrectSyntaxException();
                    }
                    return val;
                }
            default:
                throw new IncorrectSyntaxException();
        }
    }
}