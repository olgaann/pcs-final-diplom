import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    protected Map<String, List<PageEntry>> map = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        //основное действие происходит в конструкторе класса
        // прочтите тут все pdf и сохраните нужные данные,
        // тк во время поиска сервер не должен уже читать файлы

        File[] pdfFiles = pdfsDir.listFiles();
        for (File pdfFile : pdfFiles) {                                  //пройдемся по каждому из файлов
            var doc = new PdfDocument(new PdfReader(pdfFile));           //это объект пдф-файла
            int pagesCount = doc.getNumberOfPages();
            for (int i = 0; i < pagesCount; i++) {                       //и по каждой странице каждого файла
                int currentPageNum = i + 1;
                var page = doc.getPage(currentPageNum);            //это объект страницы
                var text = PdfTextExtractor.getTextFromPage(page);   //это текст со страницы
                var words = text.split("\\P{IsAlphabetic}+"); //разбиваем текст на слова


                Map<String, Integer> frequency = new HashMap<>();           // мапа, где ключом будет слово, а значением - частота
                for (var word : words) {                                    // перебираем слова
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.toLowerCase();
                    frequency.put(word, frequency.getOrDefault(word, 0) + 1);
                }

                PageEntry pageEntry;
                List<PageEntry> currentList;
                for (Map.Entry<String, Integer> pair : frequency.entrySet()) { //перебираем пары
                    String key = pair.getKey();
                    Integer value = pair.getValue();
                    pageEntry = new PageEntry(pdfFile.getName(), currentPageNum, value); //cоздаем объект pageEntry для каждого уникального слова на странице

                    currentList = new ArrayList<>();
                    if (map.containsKey(key)) {
                        currentList = map.get(key);
                    }
                    currentList.add(pageEntry);                                         //добавляем этот объект в список
                    map.put(key, currentList);                                          //кладем в "общую" мапу

                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        // тут реализуйте поиск по слову
        List<PageEntry> result = map.get(word.toLowerCase());
        if (result != null) {
            Collections.sort(result);
            return result;
        }

        return new ArrayList<>();
    }
}
