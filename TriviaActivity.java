package org.foxcroft.stem.raffle;

/**
 * TODO: Make a category unlock when all available questions have been answered.
 * TODO: Modify getCategory to get a RANDOM category, instead of the next one available.
 * Launch icon by Freepik from FlatIcon
 */

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class TriviaActivity extends AppCompatActivity {
    private final String TAG = "TriviaActivity";
    private final String KEY_TRIVIA_ENGINE = "org.foxcroft.stem.raffle.TRIVIA_ENGINE";
    private final String KEY_PIN = "org.foxcroft.stem.raffle.PIN";
    private final String KEY_TICKETS_REDEEMED = "org.foxcroft.stem.raffle.TICKETS_REDEEMED";
    private final String KEY_CURRENT_QUESTION = "org.foxcroft.stem.raffle.CURRENT_QUESTION";
    private final String MATH_LOGIN = "123456";
    private final String SCIENCE_LOGIN = "654321";
    private final String ADDRESS_LOGIN = "00000";
    private final String DATE_LOGIN = "2020";
    private final String NOT_LOGGED_IN = "No team is logged in at this time.";
    private final String ANSWERED_ALL_AVAILABLE_QUESTIONS = "You've answered all available trivia questions! A new category has been unlocked: ";
    public static final String MORNING_TEAM = "morning team";
    public static final String AFTERNOON_TEAM = "afternoon team";
    public static final String TEAM_NOT_FOUND = "team not found";
    private final int AM_PIN = 6;      // to identify pins of teams competing in the morning
    private final int PM_PIN = 4;    // to identify pins of teams competing in the afternoon
    private final int PIN_NOT_FOUND = -1;
    private TextView mMessageText;
    private RadioButton mChoice1Button;
    private RadioButton mChoice2Button;
    private RadioButton mChoice3Button;
    private RadioButton mChoice4Button;
    private RadioGroup mRadioGroup;
    private Button mSubmitButton;
    private Button mSkipButton;
    private Button mLogButton; // log in and log out
    private Button mCashButton; // to cash in tickets
    private EditText mLoginField;
    private TriviaEngine mTriviaEngine;
    private Question mCurrentQuestion;
    private String mPin; // if set to NOT_LOGGED_IN, no team is logged in.
    private List<String> mTeamNames = new ArrayList<String>();
    private List<String> mMorningPins = new ArrayList<String>();
    private List<String> mAfternoonPins = new ArrayList<String>();

    /**
     * Hide, disable, and clear mutable text from the following views:
     *  - mCashButton
     *  - mLogButton
     */
    private void disableCashInLogOutBar() {
        Log.d(TAG, "disableCashInLogOutBar() called");

        // disable the views
        mCashButton.setEnabled(false);
        mLogButton.setEnabled(false);

        // clear the text from the Log button
        mLogButton.setText("");

        // hide the views
        mCashButton.setVisibility(View.GONE);
        mLogButton.setVisibility(View.GONE);
    }

    /**
     * Hide, disable, and clear mutable text from the following views:
     *  - mLoginField
     *  - mLogButton
     */
    private void disableLoginUI() {
        Log.d(TAG, "disableLoginUI() called");

        // disable login field and log button
        mLoginField.setEnabled(false);
        mLogButton.setEnabled(false);

        // hide login field and login button
        mLoginField.setVisibility(View.GONE);
        mLogButton.setVisibility(View.GONE);

        // clear the text from the log button and the login field
        mLogButton.setText("");
        mLoginField.setText("");
    }

    /**
     * Hide, disable, and clear mutable text from the following views:
     *  - mMessageText
     *  - mChoice1Button
     *  - mChoice2Button
     *  - mChoice3Button
     *  - mChoice4Button
     *  - mSkipButton
     *  - mSubmitButton
     */
    private void disableQuestionUI() {
        Log.d(TAG, "disableQuestionUI() called");
        // disable the views
        mMessageText.setEnabled(false);
        mChoice1Button.setEnabled(false);
        mChoice2Button.setEnabled(false);
        mChoice3Button.setEnabled(false);
        mChoice4Button.setEnabled(false);
        mSkipButton.setEnabled(false);
        mSubmitButton.setEnabled(false);

        // hide the views
        mMessageText.setVisibility(View.GONE);
        mChoice1Button.setVisibility(View.GONE);
        mChoice2Button.setVisibility(View.GONE);
        mChoice3Button.setVisibility(View.GONE);
        mChoice4Button.setVisibility(View.GONE);
        mSkipButton.setVisibility(View.GONE);
        mSubmitButton.setVisibility(View.GONE);

        // clear the text from the mutable views
        mMessageText.setText("");
        mChoice1Button.setText("");
        mChoice2Button.setText("");
        mChoice3Button.setText("");
        mChoice4Button.setText("");
    }

    /**
     * Enable, show, and update mutable text in the following fields:
     *  - mCashButton
     *  - mLogButton
     *
     * Also resize text in mLogButton so "LOG OUT" is less prominent on the screen.
     */
    private void enableCashInLogOutBar() {
        // enable the views
        mCashButton.setEnabled(true);
        mLogButton.setEnabled(true);

        // show the views
        mCashButton.setVisibility(View.VISIBLE);
        mLogButton.setVisibility(View.VISIBLE);

        // change the text in the login button and cash button
        updateCashButton();
        mLogButton.setText(R.string.logout_button);
        mLogButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
    }

    /**
     * Enable, show, and update mutable text in the following fields:
     *      *  - mLogButton
     *      *  - mLoginField
     *
     * Also resize text in mLogButton so "LOG IN" is more prominent on the screen.
     */
    private void enableLoginUI() {
        Log.d(TAG, "enableLoginUI() called");
        // enable the login button and the login field
        mLogButton.setEnabled(true);
        mLoginField.setEnabled(true);

        // set the text in the login button and the login field
        mLogButton.setText(R.string.login_button);
        mLoginField.setHint(R.string.text_edit_hint);

        // make them visible
        mLogButton.setVisibility(View.VISIBLE);
        mLoginField.setVisibility(View.VISIBLE);

        // change text size for mLogButton
        mLogButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    }

    /**
     * Enable the following fields:
     *  - mMessageText
     *  - mChoice1Button
     *  - mChoice2Button
     *  - mChoice3Button
     *  - mChoice4Button
     *  - mSkipButton
     *  - mSubmitButton
     */
    private void enableQuestionUI() {
        Log.d(TAG, "enableQuestionUI() called");
        // enable them
        mMessageText.setEnabled(true);
        mChoice1Button.setEnabled(true);
        mChoice2Button.setEnabled(true);
        mChoice3Button.setEnabled(true);
        mChoice4Button.setEnabled(true);
        mSkipButton.setEnabled(true);
        mSubmitButton.setEnabled(true);

        // show them
        mMessageText.setVisibility(View.VISIBLE);
        mChoice1Button.setVisibility(View.VISIBLE);
        mChoice2Button.setVisibility(View.VISIBLE);
        mChoice3Button.setVisibility(View.VISIBLE);
        mChoice4Button.setVisibility(View.VISIBLE);
        mSkipButton.setVisibility(View.VISIBLE);
        mSubmitButton.setVisibility(View.VISIBLE);
    }

    private void importPins() {
        // Log.d(TAG, "importPins() called");
        InputStream is = getResources().openRawResource(R.raw.pins);
        BufferedReader inputFile = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";

        try {
            while ((line = inputFile.readLine()) != null) {
                // Log.d(TAG, "importPins(): imported line " + line);
                String[] tokens = line.split("\t");
                mTeamNames.add(tokens[0]);
                mMorningPins.add(tokens[1]); // this column c
                mAfternoonPins.add(tokens[2]);
            }
        } catch (IOException e) {
            Log.wtf(TAG, "importFile(Context): error reading file");
            e.printStackTrace();
        }

    }

    /**
     * inList() checks to see if the String is in one of the pin lists, and returns
     *
     * @param login
     * @return AM_PIN if found in high school list, PM_PIN if in middle school
     * list, or PIN_NOT_FOUND if the pin is not in either list.
     */
    private String inList(String login) {
        Log.d(TAG, "mLogButton.InList(" + login + ")");
        for (String pin : mMorningPins) {
            if (login.equals(pin)) {
                Log.d(TAG, "mLogButton.InList(): found in mMorningPins");
                return MORNING_TEAM;
            }
        }

        for (String pin : mAfternoonPins) {
            if (login.equals(pin)) {
                Log.d(TAG, "mLogButton.InList(): found in mAfternoonPins");
                return AFTERNOON_TEAM;
            }
        }
        Log.d(TAG, "mLogButton.InList(): PIN " + login + " not found in mMorningPins or mAfternoonPins");
        return TEAM_NOT_FOUND;
    }

    /**
     * Call the following methods, in the following order:
     *  - disableLoginUI()
     *  - enableCashInLogOutBar()
     *  - enableQuestionUI()
     *  - updateQuestion()
     */
    private void logIn() {
        // Log.d(TAG, "logIn() called");
        disableLoginUI();
        enableCashInLogOutBar();
        enableQuestionUI();
        updateQuestion();
    }

    /**
     * Call the following methods, in the following order:
     *  - disableCashInLogOutBar()
     *  - disableQuestionUI()
     *  - enableLoginUI()
     */
    private void logOut() {
        // Log.d(TAG, "logOut() called");
        disableCashInLogOutBar();
        disableQuestionUI();
        enableLoginUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setUpView();
        setListeners();
        importPins();
        logOut();

        if (savedInstanceState != null) {
            // Log.d(TAG, "onCreate(): savedInstanceState is not null; length of contents: " + savedInstanceState.toString().length());
            String triviaEngineState = savedInstanceState.getString(KEY_TRIVIA_ENGINE, "ERROR_ENGINE");
            String currentQuestionState = savedInstanceState.getString(KEY_CURRENT_QUESTION, Question.DEFAULT_QUESTION);
            mPin = savedInstanceState.getString(KEY_PIN, NOT_LOGGED_IN);
            mTriviaEngine.setTicketsRedeemed(savedInstanceState.getInt(KEY_TICKETS_REDEEMED, 0));

            if (!triviaEngineState.equals("ERROR_ENGINE")) {
                // Log.d(TAG, "onCreate(): triviaEngineState found");
                mTriviaEngine = new TriviaEngine(triviaEngineState);
            } else {
                // Log.d(TAG, "onCreate(): triviaEngineState not found");

                // try to retrieve the file
                mTriviaEngine = new TriviaEngine(mPin, TriviaActivity.this, inList(mPin));
            }

            if (!currentQuestionState.equals(Question.DEFAULT_QUESTION)) {
               //  Log.d(TAG, "onCreate(): currentQuestionState found: " + currentQuestionState);
                mCurrentQuestion = mTriviaEngine.findQuestion(new Question(currentQuestionState));
                // Log.d(TAG, "onCreate(): mCurrentQuestion = (" + mCurrentQuestion + ")");
            } else {
                Log.d(TAG, "onCreate(): currentQuestionState not found; getting another");
                updateQuestionValue();
            }

            // Log.d(TAG, "onCreate(): KEY_TRIVIA_ENGINE:\n" + savedInstanceState.getString(KEY_TRIVIA_ENGINE, "ERROR_ENGINE"));
            // Log.d(TAG, "onCreate(): KEY_CURRENT_QUESTION: " + savedInstanceState.getString(KEY_CURRENT_QUESTION, "ERROR_QUESTION"));
            // Log.d(TAG, "onCreate(): KEY_TICKETS_REDEEMED: " + savedInstanceState.getInt(KEY_TICKETS_REDEEMED, 0));

            updateQuestion();
            enableCashInLogOutBar();

        } else {
            Log.d(TAG, "onCreate(): savedInstanceState is null; hiding everything");
            logOut();
        }

        startAnimation();
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle inState) {
        super.onCreate(savedInstanceState, inState);
        Log.d(TAG, "onCreate(Bundle,PersistableBundle) called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart() called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState() called");

        // Log.d(TAG, "onSaveInstanceState(): saving ticketsRedeemed");
        savedInstanceState.putInt(KEY_TICKETS_REDEEMED, mTriviaEngine.getTicketsRedeemed());
        // outPersistableState.putInt(KEY_TICKETS_REDEEMED, mTicketsRedeemed);

        if (mTriviaEngine != null) {
            // Log.d(TAG, "onSaveInstanceState(): saving mTriviaEngine");
            savedInstanceState.putString(KEY_TRIVIA_ENGINE, mTriviaEngine.toString());
            // outPersistableState.putString(KEY_TRIVIA_ENGINE, mTriviaEngine.toString());
        } else {
            // Log.d(TAG, "onSaveInstanceState(): not saving mTriviaEngine");
        }

        if (mCurrentQuestion != null) {
            // Log.d(TAG, "onSaveInstanceState(): saving mCurrentQuestion");
            savedInstanceState.putString(KEY_CURRENT_QUESTION, mCurrentQuestion.toString());
            // outPersistableState.putString(KEY_TRIVIA_ENGINE, mCurrentQuestion.toString());
        } else {
            // Log.d(TAG, "onSaveInstanceState(): not saving mCurrentQuestion");
            savedInstanceState.putString(KEY_CURRENT_QUESTION, (new Question()).toString());
        }

        if (mPin != null) {
            // Log.d(TAG, "onSaveInstanceState(): saving mPin");
            savedInstanceState.putString(KEY_PIN, mPin);
        } else {
            // Log.d(TAG, "onSaveInstanceState(): not saving mPin");
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState, PersistableBundle outState) {
        Log.d(TAG, "onSaveInstanceState(Bundle,PersistableBundle) called");
        super.onSaveInstanceState(savedInstanceState, outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");

    }

    /**
     * Set listeners for the following Buttons:
     *  - mCashButton
     *  - mLogButton
     *  - mSkipButton
     *  - mSubmitButton
     */
    private void setListeners() {
        Log.d(TAG, "setUpListeners() called");

        mCashButton.setOnClickListener(new CashButtonListener());

        mLogButton.setOnClickListener(new LogButtonListener());

        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "mSkipButton.onClick() called");
                updateQuestion();
            }
        });

        mSubmitButton.setOnClickListener(new SubmitButtonListener());
    }

    /**
     * Sets the content view to activity_trivia.xml.
     * Finds all views as defined in activity_trivia.xml by their IDs.
     */
    private void setUpView() {
        Log.d(TAG, "setUpView() called");
        setContentView(R.layout.activity_trivia);
        mMessageText = (TextView) findViewById(R.id.text_view_question);
        mChoice1Button = (RadioButton) findViewById(R.id.radio_button_answer1);
        mChoice2Button = (RadioButton) findViewById(R.id.radio_button_answer2);
        mChoice3Button = (RadioButton) findViewById(R.id.radio_button_answer3);
        mChoice4Button = (RadioButton) findViewById(R.id.radio_button_answer4);
        mRadioGroup = (RadioGroup) findViewById(R.id.radio_group_answers);
        mSubmitButton = (Button) findViewById(R.id.button_submit);
        mLogButton = (Button) findViewById(R.id.button_log);
        mCashButton = (Button) findViewById(R.id.button_cash);
        mLoginField = (EditText) findViewById(R.id.login_field);
        mSkipButton = (Button) findViewById(R.id.button_skip);
    }

    /**
     * Starts the animated background for id main_layout
     */
    private void startAnimation() {
        Log.d(TAG, "startAnimation() called");
        ConstraintLayout constraintLayout = findViewById(R.id.main_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
    }

    /**
     * Update mCashButton to reflect the number of tickets that may be redeemed.
     */
    private void updateCashButton() {
        Log.d(TAG, "updateCashButton() called");

        if (mTriviaEngine != null) {
            if (mTriviaEngine.getRedeemableTickets() == 0) {
                mCashButton.setText("No tickets to redeem\nat this time");
            } else if (mTriviaEngine.getRedeemableTickets() == 1) {
                mCashButton.setText(String.format("Redeem 1 ticket\nat raffle table"));
            } else {
                mCashButton.setText(String.format("Redeem %d tickets\nat raffle table",
                        (mTriviaEngine.getRedeemableTickets())));
            }
        } else {
            // this will only show if something has gone terribly wrong
            mCashButton.setText("mTriviaEngine == null");
        }
    }

    /**
     * Call mTriviaEngine.unlockCategory(), which returns a String with the name of the
     * Category object which was just unlocked.
     * @param ranOutOfQuestions a boolean representing the reason why the method was called
     *      (true = ran out of questions, false = answered Starter question correctly)
     * @return the name of the unlocked category in a String (or a message, if this is the
     *      default value returned because no unlocked category exists)
     */
    private String unlockedCategoryMessage(boolean ranOutOfQuestions) {
        Log.d(TAG, "unlockedCategoryMessage(" + ranOutOfQuestions + "): called");
        String msg = mTriviaEngine.unlockCategory();
        Log.d(TAG, "unlockedCategoryMessage(): message: " + msg);

        if (msg.equals(TriviaEngine.ALL_UNLOCKED)) {
            Log.d(TAG, "default category returned; all categories unlocked");
            return "All categories unlocked! Great job.";
        } else {
            if (ranOutOfQuestions) {
                return "You ran out of questions! Another category has been unlocked: " + msg;
            } else {
                return "Congratulations! You've unlocked a new category: " + msg;
            }
        }
    }

    /**
     * Call updateQuestionValue then updateQuestionUI
     */
    private void updateQuestion() {
        updateQuestionValue();
        updateQuestionUI();
    }

    /**
     * Update the Question UI (mMessageText, mChoiceButton1 through mChoiceButton 4,
     * mSkipButton, and mSubmitButton), clear mRadioButtonGroup.
     * Disable UI if mCurrentQuestion is the default Question.
     * Enable UI if mCurrentQuestion is not the default Question.
     */
    private void updateQuestionUI() {
        Log.d(TAG, "updateQuestionUI() called; mCurrentQuestion = "
                + mCurrentQuestion.getQuestion());

        // clear the radioGroup
        mRadioGroup.clearCheck();

        if (mCurrentQuestion.equals(Question.DEFAULT_QUESTION)) {
            Log.d(TAG, "updateQuestionUI(): default question found");
            disableQuestionUI();
            mMessageText.setEnabled(true);
            mMessageText.setVisibility(View.VISIBLE);
            mMessageText.setText(R.string.all_questions_answered);
        } else {
            Log.d(TAG, "updateQuestionUI(): non-default question found");
            enableQuestionUI();
            String[] choices = mCurrentQuestion.getChoicesShuffled();
            mChoice1Button.setText(choices[0]);
            mChoice2Button.setText(choices[1]);
            mChoice3Button.setText(choices[2]);
            mChoice4Button.setText(choices[3]);
            mMessageText.setText(mCurrentQuestion.getQuestion());
        }
    }

    /**
     * Update the value in mCurrentQuestion.
     * Will get a question from mTriviaEngine.getQuestion(), or will create the default Question
     * if mTriviaEngine is null.
     */
    private void updateQuestionValue() {
        Log.d(TAG, "updateQuestionValue() called");

        if (mTriviaEngine != null) {
            Log.d(TAG, "updateQuestionValue(): mTriviaEngine is not null");

            // if there are no available categories, this will return the default question
            // and the UI will be updated in updateQuestionUI
            Log.d(TAG, "updateQuestionValue(): getting a random question");
            mCurrentQuestion = mTriviaEngine.getRandomQuestion(mCurrentQuestion);
            // Log.d(TAG, "updateQuestionValue(): mCurrentQuestion = " + mCurrentQuestion.getQuestion());

            // getRandomQuestion will return the default question if no others are available
            if (mCurrentQuestion.equals(Question.DEFAULT_QUESTION)) {
                Log.d(TAG, "updateQuestionValue(): default question found");

                if (mTriviaEngine.countLockedCategories() > 0) {
                    Log.d(TAG, "updateQuestionValue(): unlocking a category and calling updateQuestionValue() again");
                    Toast.makeText(TriviaActivity.this,
                            unlockedCategoryMessage(true),
                            Toast.LENGTH_SHORT).show();
                    updateQuestionValue();
                } else {
                    Log.d(TAG, "updateQuestionValue(): countLockedCategories returned 0");
                }
            }
        } else {
            Log.d(TAG, "updateQuestionValue(): mTriviaEngine is null");

            // if the trivia engine is null, this will fill out a Question object with
            // default values, which will display a "you've answered all the questions"
            // message when updateQuestionUI() is called.
            mCurrentQuestion = new Question();
        }

        Log.d(TAG, "updateQuestionValue(): mCurrentQuestion = " + mCurrentQuestion.getQuestion());
    }

    /**
     * CashButtonListener wires up mCashButton, which is used to redeem raffle tickets
     * at the raffle table. Clicking mCashButton brings up a login prompt, for admin login
     * with the codes listed in MATH_LOGIN, SCIENCE_LOGIN, DATE_LOGIN, and ADDRESS_LOGIN.
     * Calls mTriviaEngine.redeemTickets() to redeem tickets, then updates mCashButton view.
     */
    private class CashButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "mCashButton.setOnClickListener() called");
            AlertDialog.Builder builder = new AlertDialog.Builder(TriviaActivity.this);
            builder.setTitle(R.string.admin_prompt);

            final EditText input = new EditText(TriviaActivity.this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String admin_login = input.getText().toString();
                    if (admin_login.equals(MATH_LOGIN) ||
                            admin_login.equals(SCIENCE_LOGIN) ||
                            admin_login.equals(DATE_LOGIN) ||
                            admin_login.equals(ADDRESS_LOGIN)) {
                        int tickets_redeemed = mTriviaEngine.getRedeemableTickets();

                        if (tickets_redeemed == 1) {
                            Toast.makeText(TriviaActivity.this,
                                    String.format("Redeeming 1 ticket", tickets_redeemed),
                                    Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            Toast.makeText(TriviaActivity.this,
                                    String.format("Redeeming %d tickets", tickets_redeemed),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                        mTriviaEngine.redeemTickets();
                        mTriviaEngine.saveFile(TriviaActivity.this);
                        updateCashButton();
                    } else {
                        Toast.makeText(TriviaActivity.this,
                                "Invalid login",
                                Toast.LENGTH_SHORT).show();
                    }
                }

            });

            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
            mTriviaEngine.saveFile(TriviaActivity.this);
        }
    }

    /**
     * LogButtonListener wires up mLogButton, which is used as both the login and the logout
     * button.
     */
    private class LogButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "mLogButton.onClick() called");

            // hide the keyboard once someone submits their PIN
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            if (inputManager != null && getCurrentFocus() != null) {
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }

            if (mLogButton.getText().equals(getString(R.string.login_button))) {
                Log.d(TAG, "mLogButton.onClick(): Log In clicked");
                String login = mLoginField.getText().toString();

                // if the login is found
                if (inList(login).equals(MORNING_TEAM) || inList(login).equals(AFTERNOON_TEAM)) {
                    mPin = mLoginField.getText().toString();
                    mTriviaEngine = new TriviaEngine(mPin, TriviaActivity.this, inList(mPin));
                    mTriviaEngine.unlockStarterCategory();
                    mCurrentQuestion = mTriviaEngine.getRandomQuestion();

                    logIn();

                    Toast.makeText(TriviaActivity.this, "Logged in as "
                                    + getTeamName(mPin),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "mLogButton.onClick(): login not in list");
                    Toast.makeText(TriviaActivity.this, "Pin " + mLoginField.getText().toString() +
                            " is not valid.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "mLogButton.onClick(): text found in button: " + mLogButton.getText());
                logOut();
            }

            mLoginField.setText("");
        }

        /**
         * getTeam() method finds the team name corresponding to the passed pin
         *
         * @param pin the team's login pin
         * @return the team's name (as read from pins.tsv)
         */
        private String getTeamName(String pin) {
            for (int i = 0; i < mTeamNames.size(); i++) {
                if (mMorningPins.get(i).equals(pin) || mAfternoonPins.get(i).equals(pin)) {
                    return mTeamNames.get(i);
                }
            }
            return "TEAM NOT FOUND";
        }
    }

    /**
     * SubmitButtonListener wires up mSubmitButton, which checks to see if the chosen answer
     * is correct, then updates the related views.
     */
    private class SubmitButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "mSubmitButton.onClick() called");
            int selectedId = mRadioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) { // no answer choice is selected
                Toast.makeText(TriviaActivity.this,
                        R.string.nothing_selected_toast,
                        Toast.LENGTH_SHORT).show();
            } else { // an answer choice is selected
                if (mCurrentQuestion.checkAnswer((String) ((RadioButton) findViewById(selectedId))
                                                                                    .getText())) { // answer is correct
                    Log.d(TAG, "SubmitButtonListener().onClick(): question answered correctly");
                    if (mTriviaEngine.isStarterQuestion(mCurrentQuestion)
                            && (mTriviaEngine.countLockedCategories() > 0)) { // if this is a starter category and there are other questions to unlock
                        // give them a message stating they have unlocked a new category
                        Toast.makeText(TriviaActivity.this,
                                unlockedCategoryMessage(false),
                                Toast.LENGTH_SHORT).show();
                    /*} else if (mTriviaEngine.countUnlockedCategories() > 0) {
                        Toast.makeText(TriviaActivity.this,
                                unlockedCategoryMessage(true),
                                Toast.LENGTH_SHORT).show();*/
                    } else { // otherwise, just display a message congratulating them for getting it right
                        Toast.makeText(TriviaActivity.this,
                                correctMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TriviaActivity.this,
                            incorrectMessage(),
                            Toast.LENGTH_SHORT).show();
                }
                ((RadioButton) findViewById(selectedId)).setSelected(false);

                Log.d(TAG, "mSubmitButton.onClick(): updating question");
                updateQuestion();
            }
            mTriviaEngine.saveFile(TriviaActivity.this);
            updateCashButton();
        }

        private int correctMessage() {
            switch (1 + (int)(Math.random() * 5))
            {
                case 1: return R.string.correct_1;
                case 2: return R.string.correct_2;
                case 3: return R.string.correct_3;
                case 4: return R.string.correct_4;
                default: return R.string.correct_5;
            }
        }

        private int incorrectMessage() {
            switch (1 + (int)(Math.random() * 5))
            {
                case 1: return R.string.incorrect_1;
                case 2: return R.string.incorrect_2;
                case 3: return R.string.incorrect_3;
                case 4: return R.string.incorrect_4;
                default: return R.string.incorrect_5;
            }
        }
    }
}
