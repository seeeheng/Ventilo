package sg.gov.dsta.mobileC3.ventilo.util.constant;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VoiceCommands {
    public static final String ROGER = "Roger";
    public static final String THAT = "That";
    public static final String AFFIRMATIVE = "Affirmative";
    public static final String VESSEL = "Vessel";
    public static final String WILCO = "Wilco";
    public static final String SITREP = "Sitrep";

    public static final Map<String, String> ROGER_POSSIBLE_WORDS;
    public static final Map<String, String> THAT_POSSIBLE_WORDS;
    public static final Map<String, String> AFFIRMATIVE_POSSIBLE_WORDS;
    public static final Map<String, String> VESSEL_POSSIBLE_WORDS;
    public static final Map<String, String> WILCO_POSSIBLE_WORDS;
    public static final Map<String, String> SITREP_POSSIBLE_WORDS;

    static
    {
        ROGER_POSSIBLE_WORDS = new HashMap<String, String>();
        ROGER_POSSIBLE_WORDS.put("rodger", "Roger");
        ROGER_POSSIBLE_WORDS.put("georgia", "Roger");
        ROGER_POSSIBLE_WORDS.put("russia", "Roger");
        ROGER_POSSIBLE_WORDS.put("raja", "Roger");

        THAT_POSSIBLE_WORDS = new HashMap<String, String>();
        THAT_POSSIBLE_WORDS.put("debt", "That");

        AFFIRMATIVE_POSSIBLE_WORDS = new HashMap<String, String>();
        AFFIRMATIVE_POSSIBLE_WORDS.put("formative", "Affirmative");
        AFFIRMATIVE_POSSIBLE_WORDS.put("iphone", "Affirmative");
        AFFIRMATIVE_POSSIBLE_WORDS.put("automotive", "Affirmative");

        VESSEL_POSSIBLE_WORDS = new HashMap<String, String>();
        VESSEL_POSSIBLE_WORDS.put("vezel", "Vessel");
        VESSEL_POSSIBLE_WORDS.put("wrestle", "Vessel");
        VESSEL_POSSIBLE_WORDS.put("pestle", "Vessel");
        VESSEL_POSSIBLE_WORDS.put("pretzel", "Vessel");

        WILCO_POSSIBLE_WORDS = new HashMap<String, String>();
        WILCO_POSSIBLE_WORDS.put("local", "Wilco");
        WILCO_POSSIBLE_WORDS.put("vocal", "Wilco");
        WILCO_POSSIBLE_WORDS.put("welchol", "Wilco");
        WILCO_POSSIBLE_WORDS.put("lokal", "Wilco");
        WILCO_POSSIBLE_WORDS.put("will call", "Wilco");

        SITREP_POSSIBLE_WORDS = new HashMap<String, String>();
        SITREP_POSSIBLE_WORDS.put("cigarette", "Sitrep");
        SITREP_POSSIBLE_WORDS.put("siri rap", "Sitrep");
        SITREP_POSSIBLE_WORDS.put("sid rap", "Sitrep");
        SITREP_POSSIBLE_WORDS.put("sit wrap", "Sitrep");
    }

    private static ArrayList<Map<String, String>> getAllPossibleWords() {
        ArrayList<Map<String, String>> allPossibleWords = new ArrayList<>();
        allPossibleWords.add(ROGER_POSSIBLE_WORDS);
        allPossibleWords.add(THAT_POSSIBLE_WORDS);
        allPossibleWords.add(AFFIRMATIVE_POSSIBLE_WORDS);
        allPossibleWords.add(VESSEL_POSSIBLE_WORDS);
        allPossibleWords.add(WILCO_POSSIBLE_WORDS);
        allPossibleWords.add(SITREP_POSSIBLE_WORDS);

//        allPossibleWords.add(setWordMapCheckToLowerCase(ROGER_POSSIBLE_WORDS));
//        allPossibleWords.add(setWordMapCheckToLowerCase(THAT_POSSIBLE_WORDS));
//        allPossibleWords.add(setWordMapCheckToLowerCase(AFFIRMATIVE_POSSIBLE_WORDS));
//        allPossibleWords.add(setWordMapCheckToLowerCase(VESSEL_POSSIBLE_WORDS));
//        allPossibleWords.add(setWordMapCheckToLowerCase(WILCO_POSSIBLE_WORDS));
//        allPossibleWords.add(setWordMapCheckToLowerCase(SITREP_POSSIBLE_WORDS));

        return allPossibleWords;
    }

    private static ArrayList<String> getAllPrimaryWords() {
        ArrayList<String> allPrimaryWords = new ArrayList<>();
        allPrimaryWords.add(ROGER.toLowerCase());
        allPrimaryWords.add(THAT.toLowerCase());
        allPrimaryWords.add(AFFIRMATIVE.toLowerCase());
        allPrimaryWords.add(VESSEL.toLowerCase());
        allPrimaryWords.add(WILCO.toLowerCase());
        allPrimaryWords.add(SITREP.toLowerCase());

        return allPrimaryWords;
    }

//    private static Map<String, String> setWordMapCheckToLowerCase(Map<String, String> wordMap) {
//        for (String key : wordMap.keySet()) {
//            wordMap.replace(key, key.toLowerCase());
//        }
//
//        return wordMap;
//    }

    private static String doPrimaryCheck(String wordToCheck) {
        String foundWord = "";
        ArrayList<String> allPrimaryWords = getAllPrimaryWords();

        Log.d("doPrimaryCheck", "doPrimaryCheck is " + wordToCheck);

        for (int i = 0; i < allPrimaryWords.size(); i++) {
            if(wordToCheck.equalsIgnoreCase(allPrimaryWords.get(i))) {
                foundWord = wordToCheck;
                Log.d("FOUND - doPrimaryCheck", "Word Found is " + foundWord);
                break;
            }
        }

        return foundWord;
    }

    private static String doInDepthCheck(String wordToCheck) {
        String foundWord = "";
        ArrayList<Map<String, String>> allPossibleWords = getAllPossibleWords();

        Log.d("doInDepthCheck", "doInDepthCheck is " + wordToCheck);

        for (int i = 0; i < allPossibleWords.size(); i++) {
            for (int j = 0; j < allPossibleWords.get(i).size(); j++) {
                if(allPossibleWords.get(i).containsKey(wordToCheck)) {
                    foundWord = allPossibleWords.get(i).get(wordToCheck);
                    Log.d("FOUND - doInDepthCheck", "Word Found is " + foundWord);
                    break;
                }
            }

            if (!foundWord.equalsIgnoreCase("")) {
                break;
            }
        }

        return foundWord;
    }

    /*
     *  Steps of Word Check:
     *  1) For each possible list of words in the obtained command, iterate through each set of words
     *      a) Find primary word. If found, increase counter of primary words found count by 1, and store primary word.
     *      b) If primary word is not found, search for other possible words.
     *      c) If possible word is found, increase counter of possible words found count by 1, store possible word, .
     *      d) If both primary and possible words are not found, store obtained word from Google Recogniser.
     *  2) Compare and get the most reliable set of words based on the following:
     *      a) Primary words
     *      b) Possible words
     *      c) Obtained words
     */
    public static String getPredictedWords(ArrayList<String> command) {
        StringBuilder interpretedCommand = new StringBuilder();
//        String[] firstPossibleString = command.get(0).split(" ");
        ArrayList<Integer> listOfPrimaryWordsFoundCount = new ArrayList<>();
        ArrayList<Integer> listOfPossibleWordsFoundCount = new ArrayList<>();
        ArrayList<String> listOfPossibleWords = new ArrayList<>();

        for (int i = 0; i < command.size(); i++) {
            listOfPrimaryWordsFoundCount.add(0);
            listOfPossibleWordsFoundCount.add(0);
        }

        String foundWord = "";

        for (int i = 0; i < command.size(); i++) {
            String[] stringInCheck = command.get(i).split(" ");
            for (int j = 0; j < stringInCheck.length; j++) {
                foundWord = doPrimaryCheck(stringInCheck[j]);

                if (!"".equalsIgnoreCase(foundWord)) {
                    interpretedCommand.append(foundWord);
                    int currentPrimaryWordFoundCount = listOfPrimaryWordsFoundCount.get(i);
                    listOfPrimaryWordsFoundCount.set(i, currentPrimaryWordFoundCount++);
                } else {
                    foundWord = doInDepthCheck(stringInCheck[j]);
                    if (!"".equalsIgnoreCase(foundWord)) {
                        interpretedCommand.append(foundWord);
                        int currentPossibleWordFoundCount = listOfPossibleWordsFoundCount.get(i);
                        listOfPossibleWordsFoundCount.set(i, currentPossibleWordFoundCount++);
                    }
                }

                if ("".equalsIgnoreCase(foundWord)) {
                    interpretedCommand.append(stringInCheck[j]);
                }

                foundWord = "";

                if (j != stringInCheck.length - 1) {
                    interpretedCommand.append(" ");
                }
            }

            listOfPossibleWords.add(interpretedCommand.toString().trim());
        }

        // Get best index based on following priorities:
        // 1) Highest number of Primary words found
        // 2) If highest number of Primary words found are more than 1,
        //      find for highest number of Possible words found based among them
        // 3) If there are no Primary words found, find highest number of Possible words
        //      on every index
        // 4) If there are no Possible words found, simple take the first obtained index
        String finalPossibleWords = "";
        ArrayList<Integer> listOfBestSetOfWordsIndices = new ArrayList<>();
        int bestSetOfWordsIndex = 0;
        int highestPrimaryWordsCount = 0;
        int highestPossibleWordsCount = 0;
        for (int i = 0; i < listOfPrimaryWordsFoundCount.size(); i++) {
            if (listOfPrimaryWordsFoundCount.get(i) > highestPrimaryWordsCount) {
                highestPrimaryWordsCount = listOfPrimaryWordsFoundCount.get(i);
                listOfBestSetOfWordsIndices.clear();
                listOfBestSetOfWordsIndices.add(i);
            } else if (highestPrimaryWordsCount != 0 &&
                    listOfPrimaryWordsFoundCount.get(i) == highestPrimaryWordsCount) {
                listOfBestSetOfWordsIndices.add(i);
            }
        }

        if (listOfBestSetOfWordsIndices.size() == 1) {
            bestSetOfWordsIndex = listOfBestSetOfWordsIndices.get(0);
        } else if (listOfBestSetOfWordsIndices.size() > 1) {
            bestSetOfWordsIndex = listOfBestSetOfWordsIndices.get(0);
            for (int i = 0; i < listOfBestSetOfWordsIndices.size(); i++) {
                if (listOfPossibleWordsFoundCount.get(listOfBestSetOfWordsIndices.get(i))
                        > highestPossibleWordsCount) {
                    highestPossibleWordsCount =
                            listOfPossibleWordsFoundCount.get(listOfBestSetOfWordsIndices.get(i)) ;
                    bestSetOfWordsIndex = i;
                }
            }
        } else {
            for (int i = 0; i < listOfPossibleWordsFoundCount.size(); i++) {
                if (listOfPossibleWordsFoundCount.get(i) > highestPossibleWordsCount) {
                    bestSetOfWordsIndex = listOfPossibleWordsFoundCount.get(i);
                }
            }
        }

        finalPossibleWords = listOfPossibleWords.get(bestSetOfWordsIndex);
        return finalPossibleWords;
//        return interpretedCommand.toString().trim();
    }
}
