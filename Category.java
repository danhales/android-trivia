// For help: danhalesprogramming@gmail.com
// Creation date: 01-19-2020
// 
// Category class is used in the TriviaEngine, and holds a collection of 4-choice trivia questions.
// The Category object holds the name of the trivia question, and a boolean value representing
// whether or not the category is locked.
// An ArrayList of Questions is a field in the Category class.
// An ArrayList of Categories is a field in the TriviaEngine class.
// The TriviaEngine will be included in an app where players earn raffle tickets based
// on how many trivia questions they answer correctly.
package org.foxcroft.stem.raffle;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Category
{
    private final String TAG = "Category";
    public static final String DEFAULT_CATEGORY = "Empty";
    public static final String LOCKED = "LOCKED";
    public static final String UNLOCKED = "UNLOCKED";
    public static final String CATEGORY = "CATEGORY";
    private String mCategory;
    private boolean mLocked;
    private List<Question> mQuestions;

    /**
     * No-arg constructor initializes the name of the category to EMPTY and locks it.
     * This is to be returned by get() methods if the desired category is not found.
     */
    public Category() {
        Log.d(TAG, "Category() called");
        mCategory = DEFAULT_CATEGORY;
        mLocked = true;
        mQuestions = new ArrayList<Question>();
    }

    /**
     * Category constructor takes a String for the category.
     * Question objects must be added to mQuestions field by iterating over the contents of a file.
     * Categories are all locked by default, and must be unlocked by interacting with another team,
     * entering a code, or running out of available questions in other Categories.
     * @param line to be parsed (category name, or line from .tsv)
     */
    public Category(String line) {
        // Log.d(TAG, "Category(" + line + ") called");
        String[] tokens = line.split("\t");
        mLocked = true; // locked by default
        mQuestions = new ArrayList<Question>();

        // if the calling object only passes the name of the category
        if (tokens.length == 1)
        {
            mCategory = line;
        }
        // if the calling object passes a line from the .tsv
        else if (tokens.length == 6 || tokens.length == 7)
        {
            mCategory = tokens[0];
            addQuestion(line);
        }

        else
        {
            System.out.println("Error: Could not parse text in Category constructor.");
        }
    }

    /**
     * Category(String,boolean) is called when reading categories from the save file.
     */
    public Category(String category, boolean locked) {
        // Log.d(TAG, "Category(" + category + "," + locked + ") called");
        mCategory = category;
        mLocked = locked;
        mQuestions = new ArrayList<Question>();
    }

    /**
     * addQuestion takes a line from questions.tsv and uses it to create and add a Question object
     * to mQuestions. addQuestion also validates the line of text to make sure the Question
     * constructor will be successful. The line must be in the following format:
     * "0\t1\t2\t3\t4\t5\t6\t" where:
     * 0 is the trivia category (should match value in mCategory)
     * 1 is the text of the question
     * 2 is the correct answer
     * 3 is an incorrect answer choice
     * 4 is an incorrect answer choice
     * 5 is an incorrect answer choice
     * 6 is an optional field that may contain the user's answer (if it has been answered), a default
     *   value (if the question has been retrieved from a save file but has not been answered yet)
     *   or nothing (if it has not been answered yet and the question has not been saved).
     * If the Question is already in mQuestions, it is not added.
     * @param line a line from a .tsv file to pass to the Question constructor
     */
    public void addQuestion(String line) {
        // Log.d(TAG, "addQuestion(" + line + ") called on line 105");
        String[] tokens = line.split("\t");

        if (tokens[0].equals(mCategory) && (tokens.length == 6 || tokens.length == 7)) {
            Question q = new Question(line);
            boolean found = false;

            // don't add duplicate questions
            for (Question question : mQuestions) {
                if (question.toString().equals(q.toString())) {
                    // Log.d(TAG, "addQuestion(): Question " + q.toString() + " found");
                    found = true;
                }
            }

            if (!found) {
                // Log.d(TAG, "addQuestion(): Question (" + q.toString() + ") not found");
                mQuestions.add(q);
                // Log.d(TAG, "addQuestion(): Question (" + q.toString() + ") added to mQuestions");
            }
        } else {
            // Log.d(TAG, "addQuestion(): line (" + line + ") not added");
        }
    }

    /**
     * countAnswered() iterates through mQuestions and iterates a counter to determine how
     * many questions have been answered (correctly or incorrectly). this will be used to determine
     * whether or not a new category should be unlocked, and how many questions are remaining.
     * @return an int between 0 and the value returned by countQuestions()
     */
    public int countAnswered() {
        // Log.d(TAG, "countAnswered() called");
        int numAnswered = 0;

        for (Question q : mQuestions)
        {
            if (q.isAnswered())
            {
                numAnswered++;
            }
        }

        // Log.d(TAG, "countAnswered(): returning " + numAnswered);
        return numAnswered;
    }

    /**
     * countAvailable() counts the number of unanswered questions available.
     * @return the number of available questions in this category
     */
    public int countAvailable() {
        Log.d(TAG, "countAvailable(" + mCategory + ") called");

        int available = 0;
        for (Question q : mQuestions) {
            available += q.isAnswered() ? 0 : 1;
        }

        Log.d(TAG, "countAvailable(" + mCategory + ") returning: " + available);
        return available;
    }

    /**
     * countCorrect iterates through mQuestions and increments a counter to determine how
     * many questions have been answered correctly. this will be used to determine how many raffle
     * tickets the team has earned for this category.
     * @ return an int between 0 and the value returned by countAnswered()
     */
    public int countCorrect() {
        // Log.d(TAG, "countCorrect(" + mCategory + ") called");
        int numCorrect = 0;

        for (Question q : mQuestions)
        {
            if (q.isCorrect())
            {
                numCorrect++;
            }
        }

        Log.d(TAG, "countCorrect(" + mCategory + ") called; returning " + numCorrect);
        return numCorrect;
    }

    /**
     * countQuestions returns the number of questions in this category. useful for debugging,
     * calculating scores, and determining when the category has been exhausted.
     * @return the size of mQuestions
     */
    public int countQuestions() {
        // Log.d(TAG, "countQuestions() called; returning " + mQuestions.size());
        return mQuestions.size();  }

    /**
     * findQuestion accepts the text of a question, and returns a reference to that Question.
     * @param q the text of the question
     * @return a reference to the Question object
     */
    public Question findQuestion(String q) {
        // Log.d(TAG, "findQuestion(" + q + ") called");
        for (Question question : mQuestions) {
            if (question.equals(q)) {
                // Log.d(TAG, "findQuestion(" + q + ") called; question found");
                return question;
            }
        }
        // return a question with default values that can be checked
        // Log.d(TAG, "findQuestion(): returning default Question");
        return new Question();
    }

    /**
     * The mCategory field holds the name of the trivia category
     * @return the name of the trivia category
     */
    public String getCategory() {
        // Log.d(TAG, "getCategory() called. Returning " + mCategory);
        return mCategory; }

    /**
     * getNextQuestion returns the next unanswered Question object from the bank.
     * @return a Question that has not been answered yet, and null if none is available
     */
    public Question getNextQuestion() {
        Log.d(TAG, "getNextQuestion() called");
        if (countAnswered() < countQuestions()) {
            int index = 0;

            while (mQuestions.get(index).isAnswered() && index < mQuestions.size())
            {
                index++;
            }

            if (index < mQuestions.size())
            {
                Log.d(TAG, "getNextQuestion(): returning " + mQuestions.get(index).getQuestion());
                return mQuestions.get(index);
            }
        }

        Log.d(TAG, "getNextQuestion(): returning default Question");
        return new Question();
    }

    /**
     * getRandomQuestion will iterate through the number of questions in a pseudorandom fashion,
     * and will return the first question that is unanswered.
     * @return an unanswered Question object, or null if none is available
     */
    public Question getRandomQuestion()
    {
        Log.d(TAG, "getRandomQuestion() called");

        if (hasQuestionsAvailable())
        {
            int index = 0;
            do {
                index = (int)(Math.random() * mQuestions.size());

                if (!mQuestions.get(index).isAnswered()) {
                    return mQuestions.get(index);
                }
            } while (mQuestions.get(index).isAnswered());
        }

        Log.d(TAG, "getRandomQuestion(): returning default Question");
        return new Question(); // if there are no unanswered questions
    }

    /**
     * hasQuestionsAvailable() returns a boolean indicating whether or not all of the questions
     * in this category have been answered.
     * @return true if there are unanswered questions, false otherwise
     */
    public boolean hasQuestionsAvailable() {
        Log.d(TAG, "hasQuestionsAvailable(" + mCategory + ") called; returning " + (countAnswered() < countQuestions()));
        return (countAnswered() < countQuestions());
    }

    /**
     * isLocked checks to see if this Category's Questions can be accessed.
     * @return true if the category is locked
     */
    public boolean isLocked() {
        // Log.d(TAG, "isLocked() called");
        return mLocked;   }

    /**
     * isUnlocked checks to see if this Category's Questions can be accessed.
     * this method is for plain language convenience, and returns the negation of the value
     * returned by isLocked.
     * @return true if the category is unlocked
     */
    public boolean isUnlocked() {
        // Log.d(TAG, "isUnlocked() called");
        return !isLocked();
    }

    /**
     * lock method sets mLocked to true, which makes this category's Question objects
     * inaccessible to the user.
     */
    public void lock() {
        // Log.d(TAG, "lock() called");
        mLocked = true;
    }

    /**
     * Category's print() calls the print() method for each Question in mQuestions
     */
    public void print()
    {
        // Log.d(TAG, "print() called");
        for (Question q : mQuestions)
        {
            q.print();
            System.out.println();
        }
    }

    /**
     * toString method formats each question for writing to a .tsv. It appends the category name
     * to the front of each question (delimited by "\t") and a newline character to the end.
     */
    @Override
    public String toString()
    {
        // Log.d(TAG, "Category.toString(" + mCategory + ") called");

        String str = CATEGORY + ":" + mCategory + ":" + (mLocked ? LOCKED : UNLOCKED) + "\n";

        for (Question q : mQuestions)
        {
            str += mCategory + "\t" + q + "\n";
        }
        return str;
    }

    /**
     * unlock method sets mLocked to false, which makes this category's Question objects
     * accessible to the user.
     */
    public void unlock()
    {
        Log.d(TAG, "unlock() called on category: " + mCategory);
        mLocked = false;   }

    /**
     * mCategory contains the name of the category. This should be found in the first column
     * of questions.tsv.
     * @param category the name of this Category
     */
    public void setCategory(String category) {
        // Log.d(TAG, "setCategory(" + category + ") called");
        mCategory = category;   }
}