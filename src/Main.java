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
            Scanner file = new Scanner(input);              //Получение курса доллара из файла
            double rate = Double.parseDouble(file.nextLine());
            System.out.print("Enter expression: ");
            Scanner in = new Scanner(System.in);
            //Получение выражения
            String text = in.nextLine();
            //Запуск лексического анализатора
            List<Lex> lexemes = Lex.lexAnalyze(text);
            //Проверка валюты для ответа
            Lex first = lexemes.get(0);
            char f = ' ';
            if (first.getType() == Lex.LexemeType.Rub || first.getType() == Lex.LexemeType.Tor){
                f = 'r';
            } else if (first.getType() == Lex.LexemeType.Doll || first.getType() == Lex.LexemeType.Tod){
                f = 'd';
            }
            //Создание буфера лексем
            LexemeBuf lexBuf = new LexemeBuf(lexemes, rate);
            //Запуск синтаксического анализатора
            double result = LexemeBuf.syntaxAnalyzer(lexBuf);
            //Вывод результата
            String res = String.format("%.2f",result);
            if (f == 'r'){
                System.out.print(res+"p");
            } else{
                System.out.print("$"+res);
            }

        }
        //Обработка исключений
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
}
