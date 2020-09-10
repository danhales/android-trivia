// For help: danhalesprogramming@gmail.com
// Creation date: 01-19-2020
// 
// Question class is used in the TriviaEngine, and models a multiple (4) choice trivia question.
// An ArrayList of Questions is a field in the Category class.
// An ArrayList of Categories is a field in the TriviaEngine class.
// The TriviaEngine will be included in an Android app where players earn raffle tickets based
// on how many trivia questions they answer correctly.
package org.foxcroft.stem.raffle;

import android.util.Log;

public class Question
{
    private final String TAG = "Question";
    public static final String DEFAULT_QUESTION = "DEFAULT_QUESTION";
    public static final String DEFAULT_ANSWER = "DEFAULT_ANSWER";
    public static final String DEFAULT_USER_ANSWER = "DEFAULT_USER_ANSWER";
    public static final String[] DEFAULT_CHOICES = { "DEFAULT_CHOICE_1", "DEFAULT_CHOICE_2", "DEFAULT_CHOICE_3", "DEFAULT_CHOICE_4"};
    private String mQuestion;
    private String mAnswer;
    private final String UNANSWERED = "UNANSWERED"; // so I can type the literal just once
    private String mUserAnswer;
    private String[] mChoices;

    /**
     * Question() initializes all fields to default values.
     * Primarily used for debugging the user interface, when a generic question is needed as filler.
     */
    public Question() {
        // Log.d(TAG, "Question() called");
        mQuestion = DEFAULT_QUESTION;
        mAnswer = DEFAULT_ANSWER;
        mChoices = DEFAULT_CHOICES;
        mUserAnswer = DEFAULT_USER_ANSWER;
    }

    /**
     * Constructor accepts an array of Strings obtained by splitting a row from a .tsv file using
     * '\t' as a delimiter, and the resulting array of Strings should have the following contents
     * (listed by index):
     * [0] category: processed in the Category class, skipped here
     * [1] question: the text of the question (should not contain tabs)
     * [2] answer: the correct answer
     * [3] distractor1: an incorrect answer
     * [4] distractor2: an incorrect answer
     * [5] distractor3: an incorrect answer
     * [6] userAnswer (will not contain a value on first load)
     * @param row an array of Strings containing question data
     */
    public Question(String[] row) {
        // Log.d(TAG, "Question(String[]) called");
        mQuestion = row[1];
        mAnswer = row[2];
        mChoices = new String[] { row[2], row[3], row[4], row[5] };

        if (row.length == 7)
        {
            mUserAnswer = row[6];
        }

        else
        {
            mUserAnswer = UNANSWERED;
        }
    }

    /**
     * Constructor accepts a row from a .tsv file and splits using '\t' as a delimiter, and
     * the resulting array of Strings should have the following contents.
     * (listed by index):
     * [0] category: processed in the Category class, skipped here
     * [1] question: the text of the question (should not contain tabs)
     * [2] answer: the correct answer
     * [3] distractor1: an incorrect answer
     * [4] distractor2: an incorrect answer
     * [5] distractor3: an incorrect answer
     * [6] userAnswer (will not contain a value on first load)
     * The difference between this and Question(String[]) is this splits in the constructor, while
     * Question(String[]) splits before the constructor.
     * This is my preferred constructor.
     * @param line an array of Strings containing question data
     */
    public Question(String line) {
        // Log.d(TAG, "Question(" + line + ") called");
        String[] row = line.split("\t");

        if (row.length == 6 || row.length == 7) {
            mQuestion = row[1];
            mAnswer = row[2];
            mChoices = new String[]{row[2], row[3], row[4], row[5]};

            if (row.length == 7) {
                mUserAnswer = row[6];
            } else {
                mUserAnswer = UNANSWERED;
            }
        } else {
            mQuestion = "QUESTION ERROR";
            mAnswer = "ANSWER ERROR";
            mChoices = new String[] {"ANSWER1", "ANSWER2", "ANSWER3", "ANSWER4"};
            mUserAnswer = UNANSWERED;
        }
    }

    /**
     * checkAnswer changes the value in mUserAnswer. The value passed should be one of the
     * Strings stored as fields in this object. This method will return true if the user chose
     * the correct answer, and false otherwise. This method should only be called once the
     * user has answered the question, because it modifies the value in mUserAnswer
     * @param userAnswer the user's answer
     * @return the value returned by isCorrect()
     */
    public boolean checkAnswer(String userAnswer) {
        mUserAnswer = userAnswer;
        return isCorrect();
    }

    /**
     * clearAnswer() sets mUserAnswer to UNANSWERED
     * This method will be called iteratively to clear the user's answers and reset the app.
     */
    public void clearAnswer() {
        mUserAnswer = UNANSWERED;
    }

    /**
     * equals checks the values in all fields.
     * @param obj a Question to check for equality
     * @return true if the Questions have the same content
     */
    public boolean equals(Question obj) {
        if (!this.mQuestion.equals(obj.mQuestion)) {
            return false;
        }

        if (!this.mAnswer.equals(obj.mAnswer)) {
            return false;
        }

        if (!this.mUserAnswer.equals(obj.mUserAnswer)) {
            return false;
        }

        for (int i = 0; i < mChoices.length; i++) {
            if (!this.mChoices[i].equals(obj.mChoices[i]))
                return false;
        }

        return true;
    }

    /**
     * overloaded equals method checks to see if the text of this question matches the
     * String passed.
     * @param q the text to check
     * @return true if the text matches this question's text, false otherwise
     */
    public boolean equals(String q) {
        return q.equals(this.mQuestion);
    }

    /**
     * getAnswer() returns the correct answer to this Question.
     * @return the correct answer to this Question
     */
    public String getAnswer() {
        return mAnswer;
    }

    /**
     * getChoices() returns a COPY of the mChoices array, not a reference to the original.
     * this method returns choices in the order mAnswer, distractor1, distractor2, distractor3.
     * @return a copy of the answer choices.
     */
    public String[] getChoices() {
        return new String[] { mChoices[0], mChoices[1], mChoices[2], mChoices[3] };
    }

    /**
     * getChoicesShuffled returns an array containing the answer choices in a randomized order.
     * This is the preferred method for populating the UI's interactive components.
     * @return an array of Strings containing the answer choices
     */
    public String[] getChoicesShuffled() {
        String[] shuffled = new String[mChoices.length];
        int start = (int)(Math.random()*4);

        for (int i = 0; i < shuffled.length; i++)
        {
            shuffled[(start + i + 5)%4] = mChoices[i];
        }

        return shuffled;
    }

    /**
     * getQuestion() returns the text of the question
     * @return the question
     */
    public String getQuestion() {
        return mQuestion;
    }

    /**
     * getUserAnswer() returns the user's answer to this question, or a default value.
     * @return the user's answer, or UNANSWERED
     */
    public String getUserAnswer() {
        return mUserAnswer;
    }

    /**
     * isAnswered checks to see if the value in mUserAnswer is still equal to the value in
     * UNANSWERED. This value will be changed when the user answers the question.
     * @return true if the value in mAnswered equals the value in UNANSWERED.
     */
    public boolean isAnswered() {
        return !mUserAnswer.equals(UNANSWERED);
    }

    /**
     * isCorrect checks to see if the value in mUserAnswer is equal to the value in mAnswer.
     * @return true if the value in mAnswered equals the value in mAnswer.
     */
    public boolean isCorrect() {
        return mUserAnswer.equals(mAnswer);
    }

    /**
     * print() just outputs the question for testing purposes.
     */
    public void print() {
        System.out.println(mQuestion + "\n\t" +
                mAnswer + "\n\t" +
                mChoices[1] + "\n\t" +
                mChoices[2] + "\n\t" +
                mChoices[3] + "\n" +
                "User answer: " + mUserAnswer);
    }

    /**
     * setAnswer() is a mutator for the mAnswer field
     * The answer field will be read from (and written to) a .tsv file, so its text should
     * not have any instances of the character '\t'. This data is duplicated in the choices[]
     * array, in order to facilitate scrambling the answers.
     * @param answer the answer to the trivia question
     */
    public void setAnswer(String answer) {
        mAnswer = answer;
    }

    /**
     * setChoices() is a mutator for the mChoices field
     * The question choices will be read from (and written to) a .tsv file, so their text should
     * not have any instances of the character '\t'. The question has four answer choices.
     * The first element in this list will be
     * @param choices an array of four Strings containing all answer choices.
     */
    public void setChoices(String[] choices) {
        mChoices = new String[choices.length];
        for (int i = 0; i < mChoices.length; i++)
        {
            mChoices[i] = choices[i];
        }
    }

    /**
     * setQuestion() parses a String read from a tsv file.
     * The Trivia question will be read from (and written to) a .tsv file, so its text should
     * not have any instances of the character '\t'. The question has four answer choices.
     * @param question a trivia question that can have four answer choices
     */
    public void setQuestion(String question) {
        mQuestion = question;
    }

    /**
     * toString returns the question and its answer choices formatted for saving to a .tsv file.
     * this .tsv file is intended to be used as a save file during runtime. The Category class
     * will append this return value to a String containing the trivia category.
     * @return a String formatted for writing to a .tsv save file.
     */
    @Override
    public String toString() {
        return mQuestion + "\t" + mAnswer
                + "\t" + mChoices[1]
                + "\t" + mChoices[2]
                + "\t" + mChoices[3]
                + "\t" + mUserAnswer;
    }

}