import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Ankit on 2016-05-21.
 */
public class Search {
    private static final int SEARCH_RADIUS = 1;

    //TODO check for case that a word/phrase starts in one page and finishes in the next
    public static List<WordTextLocation> findWords(String[] words) {
        List<WordTextLocation> wordTextLocations = new ArrayList<>(words.length);
        for(int i = 0; i < words.length; i++) {
            String word = words[i];
            WordTextLocation wordTextLocation = new WordTextLocation(word);
            for (int j = 0; j < Main._textBook.length; j++) {
                String page = Main._textBook[j];

                if (page.toLowerCase().contains(word.toLowerCase())) {
                    List<Integer> occurrencesArray = Utils.getAllIndexOccurrencesInString(word.toLowerCase(),page.toLowerCase());
                    for(int occurrenceIndex : occurrencesArray) {
                        wordTextLocation.addEntry(new TextLocation(j,occurrenceIndex));
                    }
                }

                if(page.toLowerCase().contains(Utils.addSpacesInBetweenEachCharacter(word.toLowerCase()))) {
                    List<Integer> occurrencesArray = Utils.getAllIndexOccurrencesInString(Utils.addSpacesInBetweenEachCharacter(word.toLowerCase()),page.toLowerCase());
                    for(int occurrenceIndex : occurrencesArray) {
                        wordTextLocation.addEntry(new TextLocation(j,occurrenceIndex));
                    }
                }
            }
            wordTextLocations.add(wordTextLocation);
        }
        return wordTextLocations;
    }

    public static List<Result> search(List<WordTextLocation> wordTextLocations) {
        WordTextLocation firstWord = wordTextLocations.get(0);
        List<Integer> pageList = firstWord.getPageList();
        List<WordTextLocation> otherWords = Utils.removeFirstElement(wordTextLocations);
        List<Result> results = new ArrayList<>();
        for(int pageNum : pageList) {
            List<List<WordOnePage>> allPossibleResults = new ArrayList<>(otherWords.size());
            for(WordTextLocation otherWord : otherWords) {
                List<WordOnePage> possibleResultsForWord = new ArrayList<>(3);
                for(int pageCheck = pageNum - SEARCH_RADIUS; pageCheck < pageNum + SEARCH_RADIUS + 1; pageCheck++ ) {
                    if(otherWord.hasResultsOnPage(pageCheck)) {
                        possibleResultsForWord.add(
                                new WordOnePage(otherWord.getWord(),pageCheck,otherWord.getTextLocationList(pageCheck)));
                    }
                }
                allPossibleResults.add(possibleResultsForWord);
            }
            results.addAll(getAllTextLocations(new WordOnePage(firstWord.getWord(),
                    pageNum,firstWord.getTextLocationList(pageNum)),allPossibleResults));
        }
        results.sort(Comparator.naturalOrder());
        return results;
    }

    private static List<Result> getAllTextLocations(WordOnePage firstWord, List<List<WordOnePage>> otherWords) {
        List<List<WordOnePage>> allWords = new ArrayList<>();
        List<WordOnePage> firstWordList = new ArrayList<>();
        firstWordList.add(firstWord);
        allWords.add(firstWordList);
        allWords.addAll(otherWords);
        return getAllTextLocationsRecursive(allWords).getResultList();
    }

    private static ResultList getAllTextLocationsRecursive(List<List<WordOnePage>> allWords) {
        List<WordOnePage> firstWordPages = allWords.get(0);
        if(allWords.size() == 2) {
            ResultList results = new ResultList();
            for(WordOnePage firstWordPage : firstWordPages) {
                List<TextLocation> firstWordPageIndexes = firstWordPage.getIndexesOnPage();
                List<WordOnePage> lastWordPages = allWords.get(1);
                for(TextLocation firstWordIndex: firstWordPageIndexes) {
                    for(WordOnePage lastWordPage : lastWordPages) {
                        List<TextLocation> lastWordPageIndexes = lastWordPage.getIndexesOnPage();
                        for(TextLocation lastWordIndex : lastWordPageIndexes) {
                            Result result = new Result(firstWordIndex);
                            result.addResultPart(lastWordIndex);
                            results.addResult(result);
                        }
                    }
                }
            }
            return results;
        } else {
            List<ResultList> allResults = new ArrayList<>();
            ResultList results = getAllTextLocationsRecursive(allWords.subList(1,allWords.size()));
            for(WordOnePage firstWord : firstWordPages) {
                List<TextLocation> firstWordIndexes = firstWord.getIndexesOnPage();
                for(TextLocation firstWordIndex : firstWordIndexes) {
                    ResultList resultsCopy = new ResultList(results);
                    resultsCopy.addEntryToAllResults(firstWordIndex);
                    allResults.add(resultsCopy);
                }
            }
            return ResultList.mergeAll(allResults);
        }
    }
}