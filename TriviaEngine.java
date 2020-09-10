/**
 * For help: danhalesprogramming@gmail.com
 * Creation date: 01-19-2020
 *
 * TriviaEngine class manages Categories, which manage Questions.
 * TriviaEngine holds an ArrayList of categories. It can import questions from a .tsv file,
 * separate them into categories, lock and unlock categories, write all questions to a .tsv file
 * (for saving).
 */
package org.foxcroft.stem.raffle;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

class TriviaEngine
{
    private final String TAG = "TriviaEngine";
    private final String IMPORT_FILE = "questions";
    private final String SAVE_FILE = "save"; // to save by PIN
    private final String EXTENSION = ".tsv"; // in case another format works later
    private static final String EMPTY = "empty";
    public static final String STARTER_CATEGORY = "Starter";
    public static final String ALL_UNLOCKED = "All categories unlocked";
    private int mStarterQuestionsAnswered;
    private String mPin; // the team's identification number
    private List<Category> mCategories;
    private Context mContext;
    private int mTicketsRedeemed;

    /**
     * Constructor accepts a String containing the team's PIN.
     * It stores this value in mPin, creates an empty ArrayList of Categories,
     * then calls import(), which imports a save file (if it exists) or the questions file
     * (if there is no save file).
     * @param pin a unique identifier (4 digits) for each team in the competition
     */
    public TriviaEngine(String pin, Context context, String typeOfTeam) {
        Log.d(TAG, "TriviaEngine(" + pin + ", context) called");
        mPin = pin;
        mCategories = new ArrayList<Category>();
        mContext = context;
        importFile(mContext, typeOfTeam);

        // unlock the starter category
        Log.d(TAG, "Unlocking Starter category");
        unlockStarterCategory();
    }

    /**
     * Constructor is used for reloading the class after a savedInstanceState is loaded.
     * @param state a String containing the state of the Trivia engine
     */
    public TriviaEngine(String state) {
        Log.d(TAG, "TriviaEngine(" + state + ") called");
        String[] tokens = state.split("\n");

        mCategories = new ArrayList<Category>();

        for (String line : tokens) {
            Log.d(TAG, "TriviaEngine(state): " + line);
            if (line.split(":")[0].equals("TEAM")) {
                Log.d(TAG, "TriviaEngine(state): team found: " + line.split(":")[1]);
                mPin = line.split(":")[1];
            } else if (line.split(":")[0].equals(Category.CATEGORY)) {
                Log.d(TAG, "TriviaEngine(state): category found: " + line);
                addCategory(line.split(":")[1], line.split(":")[2].equals(Category.LOCKED));
            } else {
                if (line.split("\t").length != 1) {
                    Log.d(TAG, "TriviaEngine(state): question found: " + line);
                    addQuestion(line);
                } else {
                    Log.d(TAG, "TriviaEngine(state): blank line found: " + line);
                }
            }
        }
    }

    /**
     * addCategory adds a new category.
     * @param category the name of the category
     * @param locked true if locked, false otherwise
     */
    public void addCategory(String category, boolean locked) {
        Log.d(TAG, String.format("addCategory(%s,%b)", category, locked));
        mCategories.add(new Category(category, locked));
    }

    /**
     * addQuestion takes a line from the .tsv and adds a question to the bank. First, it splits the
     * line with the tab delimiter "\t". Then, it passes the first element of this array (the name
     * of the category) to the getCategory method, and then calls the returned Category's
     * addQuestion method.
     * @param line a line from a .tsv save file
     */
    public void addQuestion(String line) {
        Log.d(TAG, "addQuestion(" + line + ") called");
        Log.d(TAG, "addQuestion(" + line.split("\t")[1] + ") called");

        if (line.split("\t").length != 1) {
            findCategory(line.split("\t")[0]).addQuestion(line);
        }
    }

    /**
     * This method is used to determine whether
     * @return true if all categories are unlocked, false otherwise
     */
    public boolean alLCategoriesUnlocked() {
        for (Category c : mCategories) {
            if (c.isLocked()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if all questions have been answered.
     * @return true if all questions have been answered, false otherwise.
     */
    public boolean allQuestionsAnswered() {
        return countAnswered() == countQuestions();
    }

    /**
     * countAnswered() calculates the number of questions that have been answered.
     * return the number of trivia questions that have already been answered.
     */
    public int countAnswered() {
        Log.d(TAG, "countAnswered() called");
        int numAnswered = 0;

        for (Category c : mCategories)
        {
            numAnswered += c.countAnswered();
        }

        return numAnswered;
    }

    /**
     * countAvailable() calculates the total number of Questions in the bank that
     * have not been answered yet.
     * @return the number of available trivia questions
     */
    public int countAvailable() {
        Log.d(TAG, "countAvailable() called");
        int numAvailable = 0;

        for (Category c : mCategories)
        {
            numAvailable += c.countAvailable();
        }

        return numAvailable;
    }

    /**
     * categoriesAvailable() returns true if countUnlockedCategories() > 0.
     * @return true if categories are unlocked, false otherwise.
     */
    public boolean categoriesAvailable() {
        Log.d(TAG, "categoriesAvailable() called");
        return (countUnlockedCategories() > 0);
    }

    /**
     * countCategories() returns the size of mCategories.
     * @return the number of categories available.
     */
    public int countCategories() {
        Log.d(TAG, "countCategories() called");
        Log.d(TAG, "countCategories(): number of categories: " + mCategories.size());
        return mCategories.size();
    }

    /**
     * countLockedCategories returns the number of categories that are locked.
     * @return the number of categories that are locked.
     */
    public int countLockedCategories() {
        Log.d(TAG, "countLockedCategories() called");
        int count = 0;

        for (Category cat: mCategories) {
            Log.d(TAG, "countLockedCategories(): " + cat.getCategory() + " " + cat.isLocked());
            count += cat.isLocked() ? 1 : 0;
        }

        Log.d(TAG, "countLockedCategories() called: returning " + count);
        return count;
    }

    /**
     * Counts the questions that are "available" - in an unlocked category and
     * unanswered.
     * @return the number of available questions
     */
    public int countAvailableQuestions() {
        Log.d(TAG, "countAvailableQuestions() called");
        int numAvailable = 0;

        for (Category c : mCategories) {
            if (c.isUnlocked()) {
                numAvailable += c.countAvailable();
            }
        }

        Log.d(TAG, "countAvailableQuestions called: returning " + numAvailable);
        return numAvailable;
    }

    /**
     * countCorrect() calculates the number of questions that have been answered correctly.
     * @return the number of correct answers (corresponds to raffle tickets at STEM Challenge)
     */
    public int countCorrect() {
        Log.d(TAG, "countCorrect() called");
        int correct = 0;
        for (Category c : mCategories)
        {
            correct += c.countCorrect();
        }

        return correct;
    }

    /**
     * countQuestions calculates the total number of Questions in the bank.
     * @return the number of trivia questions available.
     */
    public int countQuestions() {
        Log.d(TAG, "countQuestions() called");
        int numQuestions = 0;
        for (Category c : mCategories)
        {
            numQuestions += c.countQuestions();
        }

        return numQuestions;
    }

    /**
     * countUnlockedCategories() returns the number of categories that are unlocked.
     * @return the number of available categories
     */
    public int countUnlockedCategories() {
        Log.d(TAG, "countUnlockedCategories() called");
        int count = 0;

        for (Category cat : mCategories)
        {
            Log.d(TAG, "countUnlockedCategories(): " + cat.getCategory() + " " + cat.isLocked());
            count += cat.isUnlocked() ? 1 : 0;
        }
        Log.d(TAG, "countUnlockedCategories(): Number of unlocked categories: " + count);
        return count;
    }

    /**
     * getScores() returns an array containing the name, score, and max score for each category.
     * Each row represents a category of trivia questions.
     * The first column is the name of the category.
     * The second column is the number of points earned for that category (a String).
     * The third column is the number of points possible for that category (a String).
     * ints will need to be parsed before using in operations
     * @return a [mCategories.size()] by 3 array of Strings containing scores for categories
     */
    public String[][] getScores() {
        Log.d(TAG, "getScores() called");
        String[][] scores = new String[mCategories.size()][3];

        for (int i = 0; i < mCategories.size(); i++)
        {
            scores[i][0] = mCategories.get(i).getCategory();
            scores[i][1] = Integer.toString(mCategories.get(i).countCorrect());
            scores[i][2] = Integer.toString(mCategories.get(i).countQuestions());
        }

        return scores;
    }

    /**
     * findCategory(String) searches the ArrayList mCategories to find the Category with this name.
     * If the Category is found, it returns a reference to it. If the Category is not found, a new
     * one is added to mCategories.
     * This method is only called when adding trivia questions to the bank.
     * @param name the name of the Category to search for
     * @return the Category object in mCategories with the matching name (may be created)
     */
    public Category findCategory(String name) {
        Log.d(TAG, "findCategory(" + name + ") called");
        for (Category category : mCategories)
        {
            if (category.getCategory().equals(name))
                return category;
        }

        // if the category is not found,
        mCategories.add(new Category(name, true));
        return mCategories.get(mCategories.size() - 1);
    }

    /**
     * findQuestion(Question) accepts a Question object, and finds it in the bank.
     * @param q the text of the question to search for
     * @return a reference to the Question object in the bank
     */
    public Question findQuestion(Question q) {
        for (Category c : mCategories) {
            if (!c.findQuestion(q.getQuestion()).equals(new Question())) {
                return q;
            }
        }
        return new Question();
    }

    /**
     * getLockedCategory returns the first unlocked category, or an empty category if none are
     * unlocked.
     * @return an unlocked Category from mCategories.
     */
    public Category getLockedCategory() {
        Log.d(TAG, "getLockedCategory() called");
        if (countUnlockedCategories() < countCategories())
        {
            Log.d(TAG, "getLockedCategory(): countUnlockedCategories() < countCategories()");
            for (Category c : mCategories)
            {
                Log.d(TAG, "getLockedCategory(): Checking category: " + c.getCategory() + ": " + c.isLocked());

                if (c.isLocked())
                {
                    Log.d(TAG, "getLockedCategory(): Returning category: " + c.getCategory());
                    return c;
                }
            }
        }
        Log.d(TAG, "getLockedCategory(): Returning empty category.");
        return new Category(); // if there are no locked categories
    }

    /**
     * getQuestion returns a reference to an unanswered Question in an unlocked Category object.
     * @return a Question that has not been answered yet
     */
    public Question getQuestion() {
        Log.d(TAG, "getQuestion() called");
        Category c = getUnlockedCategory();

        if (!c.getCategory().equals(Category.DEFAULT_CATEGORY))
        {
            Log.d(TAG, "getQuestion(): unlocked category found: " + c.getCategory());
            return c.getRandomQuestion();
        }

        Log.d(TAG, "getQuestion(): unlocked category not found. returning default");
        return new Question();
    }

    public Question getRandomQuestion() {
        Log.d(TAG, "getRandomQuestion() called");
        Category c = getRandomUnlockedCategory();

        if (!c.getCategory().equals(Category.DEFAULT_CATEGORY)) {
            return c.getRandomQuestion();
        }

        // if all else fails
        return getQuestion();
    }

    public Question getRandomQuestion(Question current) {
        Log.d(TAG, "getRandomQuestion() called");
        Category c = getRandomUnlockedCategory();
        Question q;

        if (!c.getCategory().equals(Category.DEFAULT_CATEGORY)) {
            do {
                Log.d(TAG, "Getting a random question.");
                q = getRandomQuestion();
                Log.d(TAG, "Random question: " + q.getQuestion());
                Log.d(TAG, "Current question: " + current.getQuestion());
            } while (q.equals(current.getQuestion()) && countAvailableQuestions() > 1);    // to avoid loading the same question

            return q;
        }

        // if all else fails
        return getQuestion();
    }

    /**
     * getRandomUnlockedCategory returns a random unlocked category.
     * @return an unlocked Category object with questions available
     */
    public Category getRandomUnlockedCategory() {
        Log.d(TAG, "getRandomUnlockedCategory() called");
        int     count = 0,
                numSkip = 1 + (int)(Math.random() * countUnlockedCategories());

        if (countUnlockedCategories() > 0 && countAvailableQuestions() > 0)
        {
            int index = 0;
            do {
                index = (int)(Math.random() * mCategories.size());

                if (mCategories.get(index).isUnlocked() && mCategories.get(index).hasQuestionsAvailable()) {
                    return mCategories.get(index);
                }
            } while (!mCategories.get(index).hasQuestionsAvailable() || mCategories.get(index).isLocked());
        }

        // if that fails for some reason
        Log.d(TAG, "getRandomUnlockedCategory(): calling getUnlockedCategory()");
        return getUnlockedCategory();
    }

    /**
     * getRedeemableTickets returns the number of tickets that can be redeemed.
     * @return the number of questions answered minus the number of tickets redeemed
     */
    public int getRedeemableTickets() {
        return countCorrect() - mTicketsRedeemed;
    }

    /**
     * getStarterQuestionsAnswered returns the number of starter questions that have been
     * answered.
     * @return the number of questions that have been answered in STARTER_CATEGORY.
     */
    public int countStarterQuestionsCorrect() {
        return findCategory(STARTER_CATEGORY).countCorrect();
    }

    /**
     * @return the number of tickets that have been redeemed
     */
    public int getTicketsRedeemed() {
        return mTicketsRedeemed;
    }

    /**
     * getUnlockedCategory returns an unlocked category that has not had all of its questions
     * answered, if there is one. If there is no unlocked category, it returns an empty
     * category (created with default static values in the the no-arg Category constructor).
     * @return an unlocked Category with questions available.
     */
    public Category getUnlockedCategory() {
        Log.d(TAG, "getUnlockedCategory() called");
        if (countUnlockedCategories() > 0)
        {
            int skip = mCategories.size() + 1; // coprime to the size of the array (cyclic group)
            int startIndex = (int)(Math.random() * mCategories.size()); // start somewhere random
            for (int i = 0; i < mCategories.size(); i++)
            {
                Category c = mCategories.get((startIndex + skip*i) % mCategories.size());
                if (c.isUnlocked() && c.hasQuestionsAvailable())
                {
                    Log.d(TAG, "getUnlockedCategory(): returning " + c.getCategory());
                    return c;
                }
            }
        }

        Log.d(TAG, "getUnlockedCategory(): no suitable category found; returning default");
        return new Category(); // if there are no unlocked categories with questions available
    }

    /**
     * importFile() imports file from saved data.
     * Import filename is hard-coded above in final fields.
     * Save files will have the form "save####.tsv", where #### is the team's PIN.
     * Constructor takes the team's PIN (from the login screen), and constructs the name
     * of the expected save file (SAVE_FILE + mPin + EXTENSION). It checks for an existing
     * save file, and if one is not found, it uses the import file (questions.tsv).

     public void importFile(ApplicationInfo applicationInfo, Context context) throws IOException {
     Log.d(TAG, "importFile(ApplicationInfo) called");
     String saveFilename = applicationInfo.dataDir + File.separatorChar + SAVE_FILE + mPin + EXTENSION;
     String readFilename = applicationInfo.dataDir + File.separatorChar + IMPORT_FILE + EXTENSION;
     Log.d(TAG, "saveFilename: " + saveFilename);
     Log.d(TAG, "readFilename: " + readFilename);

     File file = new File(saveFilename);
     Scanner inputFile;

     if (file.exists()) // read from the save file
     {
     // System.out.println("Found file " + saveFilename);
     // read from the file
     inputFile = new Scanner(file);
     }

     else // read from the file with the questions
     {
     Log.d(TAG, "importFile() did not find save file");
     importFile(TriviaEngine.this);
     }

     while (inputFile.hasNext())
     {
     String line = inputFile.nextLine();
     // System.out.println("The line read was: " + line); // for debugging

     // line contains a category name and locked status
     if (line.split(":").length == 2)
     {
     // add the category and the locked status
     mCategories.add(new Category(line.split(":")[0],
     line.split(":")[1].equals("UNLOCKED")));
     // System.out.println("Adding a Category");
     }

     // line contains a trivia question
     else if (line.split("\t").length == 6 || line.split("\t").length == 7)
     {
     addQuestion(line);
     // System.out.println("Adding a Question");
     }

     else
     {
     // System.out.println("Line: " + line + " was not recognized.");
     }
     }

     inputFile.close();
     } */

    /**
     * importFile(Context) is used to import from questions.tsv.
     * @param context the instance of the object calling this method
     */
    public void importFile(Context context, String typeOfTeam) {
        Log.d(TAG, "importFile(Context) called");

        try {
            String saveFilename = SAVE_FILE + mPin + EXTENSION;
            BufferedReader saveFile = new BufferedReader(new FileReader(new File(context.getFilesDir(), saveFilename)));
            Log.d(TAG, "importFile(Context): save file found!");
            String line = "";

            while ((line = saveFile.readLine()) != null) {
                Log.d(TAG, "importFile(Context): Line read: " + line);

                if (line.split(":")[0].equals("TEAM") && line.split(":")[1].equals(mPin)) {
                    Log.d(TAG, "importFile(Context): correct save file located!");
                    mTicketsRedeemed = Integer.parseInt(line.split(":")[2]);
                } else if (line.split(":")[0].equals(Category.CATEGORY)) {
                    Log.d(TAG, "importFile(Context): found Category: " + line.split(":")[1].equals(Category.LOCKED));
                    String[] tokens = line.split(":");
                    for (String t : tokens) {
                        Log.d(TAG, "importFile(Context): token: " + t + "(" + t.length() + ")");
                    }
                    addCategory(line.split(":")[1], line.split(":")[2].equals(Category.LOCKED));
                } else if (line.split("\t").length == 6 || line.split("\t").length == 7) {
                    Log.d(TAG, "importFile(Context): found Question: " + line.split("\t")[0]);
                    addQuestion(line);
                } else {
                    Log.d(TAG, "importFile(Context): empty line found: " + line);
                }
            }

            saveFile.close();

        } catch (FileNotFoundException e) {

            InputStream isImport;

            if (typeOfTeam.equals(TriviaActivity.MORNING_TEAM)) {
                isImport = context.getResources().openRawResource(R.raw.questions_morning);
            } else if (typeOfTeam.equals(TriviaActivity.AFTERNOON_TEAM)) {
                isImport = context.getResources().openRawResource(R.raw.questions_afternoon);
            } else {
                isImport = context.getResources().openRawResource(R.raw.questions);
            }

            BufferedReader inputFile = new BufferedReader(
                    new InputStreamReader(isImport, Charset.forName("UTF-8"))
            );

            String line = "";

            try {
                while ((line = inputFile.readLine()) != null) {
                    addQuestion(line);
                }
            } catch (IOException ex) {
                Log.wtf(TAG, "importFile(Context): error reading file" + ex);
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.d(TAG, "importFile(context): Exception thrown: " + e);
        }
    }

    /**
     * isStarterQuestion accepts a question and checks to see if it is a question in
     * STARTER_CATEGORY
     * @param q a Question object
     * @return true if this Question is in the Starter category, false otherwise
     */
    public boolean isStarterQuestion(Question q) {
        return !findCategory(STARTER_CATEGORY).findQuestion(q.getQuestion()).equals(new Question());
    }

    /**
     * printEverything() just prints the value returned by toString()
     * This method was primarily used for debugging.
     */
    public void printEverything() {
        Log.d(TAG, "printEverything() called");
        System.out.println(this.toString());
    }

    /**
     * redeemTickets sets mTicketsRedeemed to the number of correctly-answered questions.
     */
    public void redeemTickets() {
        mTicketsRedeemed = countCorrect();
    }

    /**
     * saveFile saves the team's data to a file with the name SAVE_FILE + mPin + EXTENSION
     * that represents the current set of categories that are unlocked and the current set of
     * trivia questions that have been answered (along with their answers).
     */
    public void saveFile() {
        Log.d(TAG, "saveFile() called");
        String saveFilename = SAVE_FILE + mPin + EXTENSION;
        File file = new File(saveFilename);

        try
        {
            PrintWriter fileOut = new PrintWriter(file);
            fileOut.print(this.toString());
            fileOut.close();
        }
        catch (IOException e)
        {
            e.printStackTrace(); // will debug later (sorry if you're reading this and it isn't done yet)
        }
    }

    /**
     * saveFile saves the team's data to a file with the name SAVE_FILE + mPin + EXTENSION
     * that represents the current set of categories that are unlocked and the current set of
     * trivia questions that have been answered (along with their answers).
     * @param context the context in which this is called (will be TriviaActivity.this)
     */
    public void saveFile(Context context) {
        Log.d(TAG, "saveFile(Context) called");
        String saveFilename = SAVE_FILE + mPin + EXTENSION;
        File file = new File(context.getFilesDir(), saveFilename);

        try
        {
            Log.d(TAG, "saveFile(Context): in try block");
            PrintWriter fileOut = new PrintWriter(file);
            fileOut.print(this.toString());
            fileOut.close();
        }
        catch (IOException e)
        {
            Log.d(TAG, e.getMessage()); // will debug later (sorry if you're reading this and it isn't done yet)
        }
    }

    /**
     * setTicketsRedeemed sets the number of tickets that have beenredeemed
     * @param ticketsRedeemed the number of tickets that have been redeemed
     */
    public void setTicketsRedeemed(int ticketsRedeemed) {
        mTicketsRedeemed = ticketsRedeemed;
    }

    /**
     * toString outputs all questions and answers, and the team's PIN at the bottom.
     * Return value is formatted to fit in a .tsv file.
     * @return a String that can be printed directly to a .tsv file
     */
    public String toString() {
        Log.d(TAG, "toString() called");
        String str = String.format("TEAM:%s:%d", mPin, mTicketsRedeemed) + "\n";
        for (Category cat : mCategories)
        {
            str += cat + "\n";
        }

        return str;
    }

    /**
     * unlockCategory() unlocks a locked category.
     */
    public String unlockCategory() {
        Log.d(TAG, "unlockCategory() called");
        Category c = getLockedCategory();
        if (!c.equals(new Category())) {
            c.unlock();
            Log.d(TAG, "unlockCategory(): " + c.getCategory() + " has been unlocked");
            return c.getCategory();
        }
        else {
            Log.d(TAG, "unlockCategory(): default category returned");
            return ALL_UNLOCKED;
        }
    }

    /**
     * unlockStarterCategory() unlocks a category called Starter.
     * Starter category is the first one to be unlocked. It contains questions that must be
     * answered by mingling with the other teams (i.e. "What is Fake Middle School's mascot?")
     * When questions in the Starter category are answered correctly, a STEM category unlocks.
     */
    public void unlockStarterCategory() {
        Log.d(TAG, "unlockStarterCategory() called");
        findCategory(STARTER_CATEGORY).unlock();
    }
}