package com.born2go.lazzybee.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.born2go.lazzybee.R;
import com.born2go.lazzybee.algorithms.CardSched;
import com.born2go.lazzybee.db.Card;
import com.born2go.lazzybee.db.impl.LearnApiImplements;
import com.born2go.lazzybee.fragment.FragmentStudy;
import com.born2go.lazzybee.shared.LazzyBeeShare;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class StudyActivity extends ActionBarActivity implements FragmentStudy.FragmentStudyListener {

    private static final String TAG = "StudyActivity";
    FragmentStudy fragmentStudy;
    boolean learn_more;

    LearnApiImplements dataBaseHelper;
    TextToSpeech textToSpeech;
    WebView mWebViewLeadDetails;
    Button btnShowAnswer;
    LinearLayout mLayoutButton;
    Button btnAgain0, btnHard1, btnGood2, btnEasy3;

    TextView lbCountNew;

    TextView lbCountAgain;

    TextView lbCountDue;

    List<Card> todayList = new ArrayList<Card>();
    List<Card> againList = new ArrayList<Card>();
    List<Card> dueList = new ArrayList<Card>();
    List<Card> cardListAddDueToDay = new ArrayList<Card>();

    CardSched cardSched;

    //Current Card
    Card currentCard = new Card();
    //init position
    int position = 0;
    int position_again = 0;
    int position_due = 0;

    String mime = "text/html";
    String encoding = "utf-8";
    String ASSETS = "file:///android_asset/";

    boolean complete_new_learn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        _initView();

        //db
        _initDatabase();

        //init cardSched
        cardSched = new CardSched();

        _initTextToSpeech();

        //get lean_more form intern
        learn_more = getIntent().getBooleanExtra(LazzyBeeShare.LEARN_MORE, false);

        //get card due today & agin
        againList = dataBaseHelper._getListCardByQueue(Card.QUEUE_LNR1);
        dueList = dataBaseHelper._getListCardByQueue(Card.QUEUE_REV2);

        //get new random card list to day
        //TODO: only take new cards if total learn today not exceed MAX_LEARN_PER_DAY
        //int newCount = 10 - (againList.size() + dueList.size);
        //if (newCount > 0)
        //  todayList = dataBaseHelper._getRandomCard(newCount);
        todayList = dataBaseHelper._getRandomCard(LazzyBeeShare.MAX_LEARN_PER_DAY, learn_more);

        int dueCount = dueList.size();
        int againCount = againList.size();
        int todayCount = todayList.size();

        Log.i(TAG, "againCount:" + againCount);
        Log.i(TAG, "dueCount:" + dueCount);
        Log.i(TAG, "todayCount:" + todayCount);

        //set data
//        if (againCount > 0 || dueCount > 0 || todayCount > 0) {
        if (todayCount > 0) {
            _setDataforWebView();

            final int list_card_again_in_today_size = againList.size();
            //set total vocabilary
            lbCountAgain.setText("" + list_card_again_in_today_size);
            lbCountAgain.setTag(list_card_again_in_today_size);

            int list_card_new_size = todayList.size();
            lbCountNew.setText("" + list_card_new_size);
            lbCountNew.setTag(list_card_new_size);


            lbCountDue.setText("" + dueList.size());
            lbCountDue.setTag(dueList.size());
        } else {
            Log.i(TAG, "_completeLean");
            _completeLean();
        }


        //Add AdView
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Log.i(TAG, LazzyBeeShare.LEARN_MORE + ":" + learn_more);
    }

    private void _completeLean() {
        onBackPressed();
    }

    /**
     * Init db sqlite
     */
    private void _initDatabase() {
        dataBaseHelper = new LearnApiImplements(this);
    }

    private void _initTextToSpeech() {
        //Todo:init TextToSpeech
        textToSpeech = new TextToSpeech(this.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });
    }

    private void _initView() {
        mWebViewLeadDetails = (WebView) findViewById(R.id.mWebViewLeadDetaisl);

        //init button

        btnShowAnswer = (Button) findViewById(R.id.btnShowAnswer);
        mLayoutButton = (LinearLayout) findViewById(R.id.mLayoutButton);

        btnAgain0 = (Button) findViewById(R.id.btnAgain0);
        btnHard1 = (Button) findViewById(R.id.btnHard1);
        btnGood2 = (Button) findViewById(R.id.btnGood2);
        btnEasy3 = (Button) findViewById(R.id.btnEasy3);

        //init lbCount
        lbCountNew = (TextView) findViewById(R.id.lbCountTotalVocabulary);
        lbCountAgain = (TextView) findViewById(R.id.lbCountAgainInday);
        lbCountDue = (TextView) findViewById(R.id.lbAgainDue);
    }

    public boolean isLearn_more() {
        return learn_more;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_study, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Hoan thanh khoa hoc rui quay tro lai DetailCourse
     */
    @Override
    public void completeCourse() {
        try {
            onBackPressed();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Set data for webview.
     * <p/>
     * <p>Difine onclick btnAnswer and btnAgain0.</p>
     * <p/>
     * Define JavaScrip to Speek Text.
     */
    private void _setDataforWebView() {
        //Todo: Set  JavaScripEnabled for webview
        WebSettings ws = mWebViewLeadDetails.getSettings();
        ws.setJavaScriptEnabled(true);

        try {
            //Todo: Load first card
            if (againList.size() > 0) {
                Log.i(TAG, "Load first again card ");
                currentCard = againList.get(position_again);

                lbCountDue.setBackgroundResource(R.color.white);
                lbCountAgain.setBackgroundResource(R.color.teal_200);
                lbCountNew.setBackgroundResource(R.color.white);
            } else if (dueList.size() > 0) {
                //Todo: get next Card
                Log.i(TAG, "Load first duecard ");
                currentCard = dueList.get(position_due);


                lbCountDue.setBackgroundResource(R.color.teal_200);
                lbCountAgain.setBackgroundResource(R.color.white);
                lbCountNew.setBackgroundResource(R.color.white);
            } else if (todayList.size() > 0) {

                Log.i(TAG, "Load first new card ");
                currentCard = todayList.get(position);

                lbCountDue.setBackgroundResource(R.color.white);
                lbCountAgain.setBackgroundResource(R.color.white);
                lbCountNew.setBackgroundResource(R.color.teal_200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mWebViewLeadDetails.loadDataWithBaseURL(ASSETS, LazzyBeeShare._getQuestionDisplay(currentCard.getQuestion()), mime, encoding, null);

        _addJavascriptInterfaceQuestionAndAnswer();


        //btnShowAnswer onCLick
        btnShowAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //hide btnShowAnswer and show mLayoutButton
                btnShowAnswer.setVisibility(View.GONE);
                mLayoutButton.setVisibility(View.VISIBLE);
                try {
                    //get card
                    Card card = currentCard;
                    //Show answer question
                    //mWebViewLeadDetails.loadDataWithBaseURL(ASSETS, getAnswerHTML(card), mime, encoding, null);
                    // Log.i(TAG, "HTML init:" + getAnswerHTML(card));

                    //Load Answer
                    _loadWebView(LazzyBeeShare.getAnswerHTML(card));

//                    //set current card
//                    currentCard[0] = card;

                    //get  next Ivl String List
                    String[] ivlStrList = cardSched.nextIvlStrLst(card);

                    //set text btn
                    btnAgain0.setText(Html.fromHtml(ivlStrList[Card.EASE_AGAIN] + "<br/>" + getString(R.string.EASE_AGAIN)));
                    btnHard1.setText(Html.fromHtml(ivlStrList[Card.EASE_HARD] + "<br/>" + getString(R.string.EASE_HARD)));
                    btnGood2.setText(Html.fromHtml(ivlStrList[Card.EASE_GOOD] + "<br/>" + getString(R.string.EASE_GOOD)));
                    btnEasy3.setText(Html.fromHtml(ivlStrList[Card.EASE_EASY] + "<br/>" + getString(R.string.EASE_EASY)));

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        //Todo:btnAgain on click
        btnAgain0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set display card queue==2
                //update due
                //display card next if cardQueuesize>0 else priority cardQueue>cardDue>cardNew
                //if end card else complete
                //update rev_count
                //show btnShowAnswer and hide btnAgain0
                _answerAgainCard();
            }


        });
        //

        btnHard1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _answerDueCard(Card.EASE_HARD);

            }
        });

        //btnGood2 onClick
        //set display card queue==2
        //update due
        //display card next if cardQueuesize>0 else priority cardQueue>cardDue>cardNew
        //if end card else complete
        btnGood2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _answerDueCard(Card.EASE_GOOD);
            }
        });


        btnEasy3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _answerDueCard(Card.EASE_EASY);
            }
        });

    }

    private void _answerAgainCard() {
        final int curren_time = (int) (new Date().getTime() / 1000);/*curent time*/
        btnShowAnswer.setVisibility(View.VISIBLE);
        mLayoutButton.setVisibility(View.GONE);

        int currentQueue = currentCard.getQueue();//Get current Queue

        _checkContainsAndRemove(againList);

        //TODO:Reset count list again,new,due
        if (currentQueue == Card.QUEUE_NEW_CRAM0) {
            //reset new card count
            todayList.remove(currentCard);
            int countNew = todayList.size();
            lbCountNew.setText("" + countNew);
        }
//        if (currentQueue == Card.QUEUE_LNR1) {
////            reset new card again
//            againList.remove(currentCard);
//            int countAgain = againList.size();
//            lbCountAgain.setText("" + countAgain);
//
//        }
        if (currentQueue == Card.QUEUE_REV2) {
            //reset new card due
            dueList.remove(currentCard);
            int countDue = dueList.size();
            lbCountDue.setText("" + countDue);

        }


        //TODO:Set queue,due using cardShed
        cardSched.answerCard(currentCard, Card.EASE_AGAIN);
        currentCard.setDue(curren_time + 60);
        againList.add(currentCard);

        int countAgain = againList.size();
        lbCountAgain.setText("" + countAgain);

        //TODO:update card
        dataBaseHelper._updateCard(currentCard);

        try {
            if (currentQueue == Card.QUEUE_NEW_CRAM0) {
                Log.i(TAG, "_answerDueCard:Card.QUEUE_NEW_CRAM0");
                _nextAgainCard();

            }
            if (currentQueue == Card.QUEUE_LNR1) {
                Log.i(TAG, "_answerDueCard:Card.QUEUE_LNR1");
                _nextDueCard();

            }
            if (currentQueue == Card.QUEUE_REV2) {
                Log.i(TAG, "_answerAgainCard:Card.QUEUE_REV2");
                _nextNewCard();
            }
        } catch (Exception e) {
            e.printStackTrace();
            _completeLean();
        }


//        int flag_queue = 1;
//        int due_time = (int) (curren_time + 600);
//
//        try {
//            //getCurrent Card Queue
//            int currentCardQueue = currentCard.getQueue();
//
//            //Check Contains
//            if (againList.contains(currentCard)) {
//                Log.i(TAG, "Card Contains againList");
//                //remove current Card
//                againList.remove(currentCard);
//            }
//            if (cardListAgainToday.contains(currentCard)) {
//                Log.i(TAG, "Card Contains in cardListAgainToday");
//                //remove current Card
//                cardListAgainToday.remove(currentCard);
//            }
//
//
//            cardSched.answerCard(currentCard, Card.EASE_AGAIN);
//
//
//            againList.add(currentCard);
//            cardListAgainToday.add(currentCard);
//
//
//            dataBaseHelper._updateCard(currentCard);//update card
//
//            int _count = todayList.size();
//            //reset total vocabilary
//            int current_count = _count - cardListAgainToday.size();
//            lbCountNew.setText("" + current_count);
//
//            //Update Count Again
//            int count_card_list_again = againList.size();
//            //reset total vocabilary
//            lbCountAgain.setText("" + count_card_list_again);
//
//            if (position < todayList.size()) {
//                if (currentCardQueue == Card.QUEUE_NEW_CRAM0) {
//                    //if currentCardQueue == Card.QUEUE_NEW_CRAM0 next card again if againList.size>0
//                    Log.i(TAG, "Card.QUEUE_NEW_CRAM0");
//                    _nextAgainCard();
//
//
//                } else if (currentCardQueue == Card.QUEUE_LNR1) {
//                    Log.i(TAG, "Card.QUEUE_LNR1");
//                    _nextDueCard();
//
//
//                } else if (currentCardQueue == Card.QUEUE_REV2) {
//                    Log.i(TAG, "Card.QUEUE_REV2");
//                    _nextNewCard();
//
//                }
//            } else {
//                _completeLean();
//
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    /**
     * Answer card by easy
     * get next card
     */
    private void _answerDueCard(int easy) {
        //TODO:show btnShowAnswer and hide btnAgain0
        btnShowAnswer.setVisibility(View.VISIBLE);
        mLayoutButton.setVisibility(View.GONE);

        int currentQueue = currentCard.getQueue();//Get current Queue

        Log.i(TAG, "_answerDueCard:Currrent Card Queue:" + currentQueue);

        //Check Contains and Remove
        // _checkContainsAndRemove(dueList);
        _checkContainsAndRemove(cardListAddDueToDay);

        //TODO:Reset count list again,new,due
        if (currentQueue == Card.QUEUE_NEW_CRAM0) {
            //reset new card count
            todayList.remove(currentCard);
            int countNew = todayList.size();
            Log.i(TAG, "_answerDueCard:Curren new count:" + countNew);
            lbCountNew.setText("" + countNew);
        }
        if (currentQueue == Card.QUEUE_LNR1) {
            //reset new card again
            int countAgain = againList.size() - 1;
            lbCountAgain.setText("" + countAgain);
            againList.remove(currentCard);
        }
        if (currentQueue == Card.QUEUE_REV2) {
            //reset new card due
            int countDue = dueList.size() - 1;
            lbCountDue.setText("" + countDue);
            dueList.remove(currentCard);

        }


        //TODO:Set queue,due using cardShed
        cardSched.answerCard(currentCard, easy);
        // dueList.add(currentCard);
        cardListAddDueToDay.add(currentCard);

        //TODO:update card
        dataBaseHelper._updateCard(currentCard);

        //Todo:get next card by currentQueue
        try {
//            if (position <= todayList.size()) {
            if (currentQueue == Card.QUEUE_NEW_CRAM0) {
                Log.i(TAG, "_answerDueCard:Card.QUEUE_NEW_CRAM0");
                _nextAgainCard();

            }
            if (currentQueue == Card.QUEUE_LNR1) {
                Log.i(TAG, "_answerDueCard:Card.QUEUE_LNR1");
                _nextDueCard();

            }
            if (currentQueue == Card.QUEUE_REV2) {
                Log.i(TAG, "_answerDueCard:Card.QUEUE_REV2");
                _nextNewCard();
            }
//            } else {
//                _completeLean();
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void _addJavascriptInterfaceQuestionAndAnswer() {
        //Todo: addJavascriptInterface play question
        mWebViewLeadDetails.addJavascriptInterface(new JsObjectQuestion() {
            @JavascriptInterface
            public void playQuestion() {
                //get text to Speak
                String toSpeak = currentCard.getQuestion();

                //Toast Text Speak
                //Toast.makeText(this.getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();

                //Speak text
                _speakText(toSpeak);

                //textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        }, "question");
        mWebViewLeadDetails.addJavascriptInterface(new JsObjectExplain() {
            @JavascriptInterface
            public void speechExplain() {
                //get answer json
                String answer = currentCard.getAnswers();
                String toSpeech=LazzyBeeShare._getValueFromKey(answer,"explain");

                //Speak text
                _speakText(toSpeech);
            }
        }, "explain");
        mWebViewLeadDetails.addJavascriptInterface(new JsObjectExample() {
            @JavascriptInterface
            public void speechExample() {
                //get answer json
                String answer = currentCard.getAnswers();
                String toSpeech=LazzyBeeShare._getValueFromKey(answer,"example");

                //Speak text
                _speakText(toSpeech);            }
        }, "example");

//        //Todo: addJavascriptInterface play answer
//        mWebViewLeadDetails.addJavascriptInterface(new JsObjectAnswers() {
//            @JavascriptInterface
//            public void playAnswers() {
//                //get text to Speak
//                String toSpeak = currentCard.getAnswers();
//
//                //Toast Text Speak
//                //Toast.makeText(this, toSpeak, Toast.LENGTH_SHORT).show();
//
//                //Speak text
//                _speakText(toSpeak);
//
//                //textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
//            }
//        }, "answers");



    }

    private void _checkContainsAndRemove(List<Card> cardLis) {
        if (dueList.contains(currentCard)) {
            Log.i(TAG, "Card Contains cardList");
            //remove current Card
            cardLis.remove(currentCard);
        }
    }

    private void _updateCardQueueAndCardDue(int card_id, int queue, int due) {
        dataBaseHelper._updateCardQueueAndCardDue(/*cast in to string*/String.valueOf(card_id), queue, due);

    }

    private void _nextNewCard() {
        Log.i(TAG, "Curent new card:" + currentCard.toString());
        if (todayList.size() > 0) {
            position = todayList.size() - 1;
            try {
                //get next card again
                Log.i(TAG, "_nextNewCard Position=" + position + " today:" + todayList.size());
                currentCard = todayList.get(position);

                //set BackBackground color
                lbCountDue.setBackgroundResource(R.color.white);
                lbCountAgain.setBackgroundResource(R.color.white);
                lbCountNew.setBackgroundResource(R.color.teal_200);


                //TODO:Display next card
                _loadWebView(LazzyBeeShare._getQuestionDisplay(currentCard.getQuestion()));

            } catch (Exception e) {
                e.printStackTrace();
                _completeLean();
            }
        } else if (againList.size() > 0) {
            Log.i(TAG, "_nextNewCard:Next card is Again card");
            //complete_new_learn = true;
            _nextAgainCard();
            //_completeLean();

        } else if (dueList.size() > 0) {
            Log.i(TAG, "_nextNewCard:Next card is Due card");
            _nextDueCard();
            //_completeLean();
        } else {
            if (todayList.size() == 0) {
                Log.i(TAG, "_nextNewCard:_completeLean");
                _completeLean();
            } else {
                Log.i(TAG, "_nextNewCard:Next card is New card");
                _nextNewCard();
            }
        }
    }

    boolean flag_due = true;

    private void _nextDueCard() {
        Log.i(TAG, "_nextDueCard:Current Card:" + currentCard.toString());
        if (dueList.size() > 0) {//Check dueList.size()>0
            if (flag_due) {
                position_due = 0;
            } else {
                position_due++;
            }
            Log.i(TAG, "_nextDueCard:Next card is due card " + dueList.size());

            // position_due = (dueList.size() - 1);
            currentCard = dueList.get(position_due);

            lbCountDue.setBackgroundResource(R.color.teal_200);
            lbCountAgain.setBackgroundResource(R.color.white);
            lbCountNew.setBackgroundResource(R.color.white);

            //TODO:Display next card
            _loadWebView(LazzyBeeShare._getQuestionDisplay(currentCard.getQuestion()));

        } else if (againList.size() > 0) {//Check againList.size()>0
            Log.i(TAG, "_nextDueCard:Next card is again card");
            _nextAgainCard();
        } else {
            if (todayList.size() == 0) {
                Log.i(TAG, "_nextDueCard:_completeLean");
                _completeLean();
            } else {
                Log.i(TAG, "_nextDueCard:Next card is new card");
                _nextNewCard();
            }
        }


    }

    boolean flag_one = true;

    private void _nextAgainCard() {
        Log.i(TAG, "_nextAgainCard:Current Card:" + currentCard.toString());


        if (againList.size() > 0) {//Check againList.size()>0

            try {
                if (flag_one) {
                    position_again = 0;
                } else {
                    position_again++;
                }

                if (position_again < (againList.size() - 1)) {
                    currentCard = againList.get(position_again);

                    //get current time and du card
                    int current_time = (int) (new Date().getTime() / 1000);
                    int due = (int) currentCard.getDue();

                    Log.i(TAG, "_nextAgainCard:" + current_time + ":" + due);
                    if (current_time - due >= 600 || todayList.size() == 0) {

                        Log.i(TAG, "_nextAgainCard:Next card is again card 1");

                        lbCountDue.setBackgroundResource(R.color.white);
                        lbCountAgain.setBackgroundResource(R.color.teal_200);
                        lbCountNew.setBackgroundResource(R.color.white);

                        flag_one = false;
                        //TODO:Display next card
                        _loadWebView(LazzyBeeShare._getQuestionDisplay(currentCard.getQuestion()));


                    } else {
                        Log.i(TAG, "_nextAgainCard:Next card is new card 1");
                        _nextNewCard();
                    }
                } else {
                    if (todayList.size() > 0) {
                        Log.i(TAG, "_nextAgainCard:Next card is new card 2");
                        _nextNewCard();
                    } else {
                        _completeLean();
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "_nextAgainCard:AAAAA");
//                position_again++;
//                if (position_again < (againList.size() - 1)) {
//
//                    currentCard = againList.get(position_again);
//
//                    //get current time and du card
//                    int current_time = (int) (new Date().getTime() / 1000);
//                    int due = (int) currentCard.getDue();
//
//                    if (current_time - due >= 600 || todayList.size() == 0) {
//                        Log.i(TAG, "_nextAgainCard:Next card is again card 2");
//
//                        lbCountDue.setBackgroundResource(R.color.white);
//                        lbCountAgain.setBackgroundResource(R.color.teal_200);
//                        lbCountNew.setBackgroundResource(R.color.white);
//
//                        //TODO:Display next card
//                        _loadWebView(_getQuestionDisplay(currentCard.getQuestion()));
//                    } else {
//                        Log.i(TAG, "_nextAgainCard:Next card is new card 2");
//                        _nextNewCard();
//                    }
//                } else {
//                    Log.i(TAG, "_nextAgainCard:Next card is new card 3");
//                    _nextNewCard();
//                }
            }


        } else if (dueList.size() > 0) {//Check dueList.size()>0
            Log.i(TAG, "_nextAgainCard:Next card is due card");
            _nextDueCard();
        } else {
            Log.i(TAG, "_nextAgainCard:Next card is new card");
            _nextNewCard();
        }

    }

    /**
     * Load string Html
     */
    private void _loadWebView(String questionDisplay) {
        //Clear View
        if (Build.VERSION.SDK_INT < 18) {
            mWebViewLeadDetails.clearView();
        } else {
            mWebViewLeadDetails.loadUrl("about:blank");
        }
        //
        //  Log.i(TAG, "HTML FROM:" + questionDisplay.toString());
        //Set Data
        mWebViewLeadDetails.loadDataWithBaseURL(ASSETS, questionDisplay, mime, encoding, null);

    }

//    /**
//     * init HTML question
//     */
//    private String _getQuestionDisplay(String s) {
//        String html =
//                "<!DOCTYPE html>\n" +
//                        "<html>\n" +
//                        "<head>\n" +
//                        "<style>\n" +
//                        " figure {" +
//                        "   text-align: center;" +
//                        "   margin: auto;" +
//                        "}" +
//                        "figure.image img {" +
//                        "   width: 100% !important;" +
//                        "   height: auto !important;" +
//                        "}" +
//                        "figcaption {" +
//                        "   font-size: 10px;" +
//                        "}" +
//                        "a {" +
//                        " margin-top:5px;" +
//                        "}" +
//                        "</style>\n" +
//                        "</head>\n" +
//                        "<body>\n" +
//                        "<h1 >" + s + "<a onclick='question.playQuestion();'><img src='ic_play_black.png'/></a></h1>"
//                        + "</body>\n" +
//                        "</html>";
//        return html;
//    }

    /*
    *Java Scrip Object Question
    * */
    public class JsObjectQuestion {
        @JavascriptInterface
        public String toString() {
            return "question";
        }
    }

    /*
   *Java Scrip Object explain
   * */
    public class JsObjectExplain {
        @JavascriptInterface
        public String toString() {
            return "explain";
        }

    }
    /*
  *Java Scrip Object example
  * */
    public class JsObjectExample {
        @JavascriptInterface
        public String toString() {
            return "example";
        }

    }

    /**
     * Speak text theo version andorid
     */
    public void _speakText(String toSpeak) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            _textToSpeechGreater21(toSpeak);
        } else {
            _textToSpeechUnder20(toSpeak);
        }
    }

    @SuppressWarnings("deprecation")
    private void _textToSpeechUnder20(String text) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void _textToSpeechGreater21(String text) {
        String utteranceId = this.hashCode() + "";
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
}
