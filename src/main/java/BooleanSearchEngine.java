import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {
    protected Map<String, List<PageEntry>> map = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        //основное действие происходит в конструкторе класса
        // прочтите тут все pdf и сохраните нужные данные,
        // тк во время поиска сервер не должен уже читать файлы

        File[] pdfFiles = pdfsDir.listFiles();
        for (File pdfFile : pdfFiles) {                                         //пройдемся по каждому из файлов
            var doc = new PdfDocument(new PdfReader(pdfFile));           //это объект пдф-файла
            int pagesCount = doc.getNumberOfPages();
            for (int i = 0; i < pagesCount; i++) {                              //и по каждой странице каждого файла
                int currentPageNum = i + 1;
                var page = doc.getPage(currentPageNum);                //это объект страницы
                var text = PdfTextExtractor.getTextFromPage(page);       //это текст со страницы
                var words = text.split("\\P{IsAlphabetic}+");    //разбиваем текст на слова


                Map<String, Integer> frequency = new HashMap<>();              // мапа, где ключом будет слово, а значением - частота
                for (var word : words) {                                       // перебираем слова
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
                    currentList.add(pageEntry);                                  //добавляем этот объект в список
                    map.put(key, currentList);                                   //кладем в "общую" мапу

                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String words) throws IOException {


        List<String> wordsList = Arrays.asList(words.toLowerCase().split(" ")); //формируем список слов, которые будем искать
        wordsList = deleteStopWords(wordsList); //удаляем из этого списка стоп-слова, которые не должны влиять на результат


        //нам нужно выделить из мапы map значения List<PageEntry>>, которые соотвествуют словам из wordsList
        //при совпадении у PageEntry параметров pdfName и page нам нужно сложить параметр count

        //c помощью стрима сформируем промежуточную мапу:
        Map<String, PageEntry> localMap = wordsList.stream()                  //это стрим слов
                .distinct()                                                   //удаляем из него дубликаты
                .map(word -> map.get(word))                           //из стрима слов получаем стрим List<PageEntry>
                .filter(Objects::nonNull)                            //удаляем null-списки для слов, которых нет в файлах
                .flatMap(Collection::stream)                          //стрим List<PageEntry> превращаем в стрим PageEntry
                .collect(Collectors.toMap(                           //собираем в мапу:
                        pageEntry -> pageEntry.getPdfName() + "@" + pageEntry.getPage(), // ключ по двум полям - название и номер страницы
                        pageEntry -> pageEntry,                                          // копируем текущей элемент pageEntry
                        (pageEntry1, pageEntry2) -> {                                    // при совпадении ключей складываем поле count
                            return new PageEntry(pageEntry1.getPdfName(), pageEntry1.getPage(), pageEntry1.getCount() + pageEntry2.getCount());
                        }

                ));

        List<PageEntry> resultList = new ArrayList<>();
        localMap.entrySet().stream()                                          //из мапы берем только значения и формируем из них итоговый список List<PageEntry>
                .forEach(pair -> resultList.add(pair.getValue()));

        Collections.sort(resultList);
        return resultList;

    }

    public static List<String> deleteStopWords(List<String> words) throws IOException {
        File stopFile = new File("stop-ru.txt");
        List<String> stopList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(stopFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopList.add(line.toLowerCase());
            }
        }

        return words.stream()
                .filter(word -> !stopList.contains(word))
                .collect(Collectors.toList());
    }
}
