package com.example.roberto.flagquizgame;

//Credits: Deitel and Deitel: "Android for programmers and app driven approach"
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> fileNameList; //flag file names

    private ArrayList<String>  quizCountryList; //names of the countries in the current quiz

    private Map<String,Boolean> regionsMap; //Hash map of enabled retgions

    private int correctAnswers; //Number of correct answer
    private int totalGuesses; //Total number of guesses made by a user

    private String correctAnswer; // name the correct country for the current flag

    private Random random; //Random number generator

    private TextView questionNumberTextView; //display current question number
    private ImageView flagImageView;//Image of the flag
    private TableLayout buttonTableLayout;// table of answer buttons


    private int guessRows; //Number of rows displaying choices


    private Animation animation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileNameList = new ArrayList<String>();
        quizCountryList = new ArrayList<String>();

        regionsMap = new HashMap<String,Boolean>();

        animation = AnimationUtils.loadAnimation(this,R.anim.animation);
        animation.setRepeatCount(4);

        random = new Random();

        guessRows = 1; //default is 1

        questionNumberTextView = (TextView) findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) findViewById(R.id.flagImageView);
        buttonTableLayout = (TableLayout) findViewById(R.id.buttonTableLayout);


        //get the array of world regions from strings.xml
        String  [] regionNames = getResources().getStringArray(R.array.regionsList);

        //By default all regions are enabled
        for (String region:regionNames)
            regionsMap.put(region,true);

        //set text in questionNumberTextView

        questionNumberTextView.setText(getResources().getString(R.string.question)+" 1 " + getResources().getString(R.string.of)+ " 10 ");


        resetQuiz();
    }

    //Set up and start the next quiz
    private void resetQuiz() {

        AssetManager assetManager = getAssets(); //get the app's AssetManager

        fileNameList.clear(); //clear the list

        try {

            Set<String> regions = regionsMap.keySet();

            //loop through each region

            for (String region: regions) {

                //if region is enabled

                if(regionsMap.get(region)){

                    //Get a list of all the names of the flag image files in the region

                    String  [] paths = assetManager.list(region);

                    ///Toast.makeText(this,region,Toast.LENGTH_LONG).show();
                        for(String path : paths)
                            fileNameList.add(path);
                }
            }
        }
        catch (IOException e){
        }




        correctAnswers = 0 ; //reset the number of correct answers
        totalGuesses = 0; //reset the number of users's guesses

        quizCountryList.clear();

        //add 10 random file names to the quizCountryList
        int flagCounter=1;
        int numberOfFlags = fileNameList.size();

        while (flagCounter<=10){

            int randonIndex = random.nextInt(numberOfFlags);
            String fileName = fileNameList.get(randonIndex);

            if (!quizCountryList.contains(fileName)){
                quizCountryList.add(fileName);
                flagCounter++;
            }//end if
        }//end while


        loadNextFlag();

    }

    private void loadNextFlag() {



        //get the name of the next flag and remove it from the list

        String nextImageName = quizCountryList.remove(0);
        correctAnswer = nextImageName;




        //display the current number of question in the view

        questionNumberTextView.setText(getResources().getString(R.string.question)+" "+
                        (correctAnswers+1) + " " +
                        getResources().getString(R.string.of)+ " 10 "
        );

        //Extract the region from the country name

        String region = nextImageName.substring(0,nextImageName.indexOf('-'));

        //Toast.makeText(getApplicationContext(),region,Toast.LENGTH_LONG).show();

        //load the image of the coutry from the asset
        AssetManager assetManager = getAssets();
        InputStream inputStream;


        //Toast.makeText(getApplicationContext(),region,Toast.LENGTH_LONG).show();

        try {
            String path = region + "/" + nextImageName;
            inputStream = assetManager.open(path);
            Drawable flag = Drawable.createFromStream(inputStream,nextImageName);
            flagImageView.setImageDrawable(flag);
        }

        catch (IOException e){
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }

        //Clear all prior buttons from table rows
        for(int row=0;row<buttonTableLayout.getChildCount();++row)
            ((TableRow) buttonTableLayout.getChildAt(row)).removeAllViews();

        //Shuffle the file names
        Collections.shuffle(fileNameList);

        //Put the correct answer to the end of the list
        //so that cannot be set as text in a a button
        //the right answer replaces one of the (3,6,9) buttons at random
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        //Put the correct answer to the end of fileNameList
        //
        //int correct = fileNameList.indexOf(correctAnswer);
        //fileNameList.add(fileNameList.remove(correct));

        //get a reference to the Layout Inflater Service

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Create a table of buttons

        for (int row=0;row<guessRows;row++){

            TableRow currentTableRow = getTableRow(row);
            //place button in the currentTableRow

            for (int i=0;i<3;i++){

                Button newGuessButton =
                        (Button) layoutInflater.inflate(R.layout.guess_button,null);

                //set the text of the button
                String fileName = fileNameList.get((3*row)+i);
                newGuessButton.setText(getCountryName(fileName));
                newGuessButton.setTextSize(10);
                newGuessButton.setOnClickListener(guessButtonListener);
                currentTableRow.addView(newGuessButton);

            }
        }


        //replace a button with the correct answer
        int row = random.nextInt(guessRows);//pick one row at random
        int column = random.nextInt(3);//pick one column at random
        TableRow tableRow = getTableRow(row); //get the random row
        String countryName = getCountryName(correctAnswer);
        ((Button)tableRow.getChildAt(column)).setText(countryName);


        //Toast.makeText(getApplicationContext(),correctAnswer,Toast.LENGTH_LONG).show();

    }

    private TableRow getTableRow(int row) {

       return (TableRow) buttonTableLayout.getChildAt(row);
    }

    private String getCountryName(String fileName) {
        String name= fileName.substring(fileName.indexOf('-')+1).replace('_',' ');
        return name.substring(0,name.indexOf('.'));
    }

    private void submitGuess(Button guessButton){

        String guess = guessButton.getText().toString();
        String answer = getCountryName(correctAnswer);
        totalGuesses++;


        //guess is correct
        if (guess.equals(answer)){

            correctAnswers++;


            if (correctAnswers==10){


                //Prepapare an Alert Dialog


                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.reset_quiz); //title bar string

                builder.setMessage(getResources().getString(R.string.guesses) + " " + Integer.toString(totalGuesses));

                builder.setPositiveButton(R.string.reset_quiz,new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog,int id){

                                resetQuiz();
                            }


                    }
                );

                //Create a dialog from the Builder
                AlertDialog resetDialog = builder.create();
                resetDialog.show();

            }
            //answer is correct, but the quiz is not over
            else {

                Toast.makeText(getApplicationContext(),"Correct! "+ totalGuesses,Toast.LENGTH_SHORT).show();

                loadNextFlag();

            }
        }

        else { //answer was not correct

            flagImageView.startAnimation(animation);
            Toast.makeText(getApplicationContext(),"Not correct!: " + totalGuesses,Toast.LENGTH_SHORT).show();

        }


    }



    private final View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            submitGuess((Button)v);
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.regions) {

            //get the name of world region
            final String [] regionNames =
                    regionsMap.keySet().toArray(new String[regionsMap.size()]);

            //boolean array representing whether each region is enabled or not
            boolean [] regionsEnabled = new boolean[regionsMap.size()];
            for(int i=0;i<regionsEnabled.length;i++)
                regionsEnabled [i] = regionsMap.get(regionNames[i]);


            //Create an Alert Dialog to set to region enabled...

            AlertDialog.Builder regionsBuilder = new AlertDialog.Builder(this);
            regionsBuilder.setTitle(R.string.regions);

            //create the name to display replacing '_' with a space (like in South_America)

            final String [] displayNames = new String[regionNames.length];
            for (int i=0;i<regionNames.length;i++)
                displayNames [i] = regionNames[i].replace('_',' ');

            //Add display names to the dialog and set the listener

            regionsBuilder.setMultiChoiceItems(displayNames, regionsEnabled, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                            //include or exclude the clicked region
                            regionsMap.put(regionNames[which].toString(),isChecked);
                        }
                    }
            );

            regionsBuilder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    resetQuiz();
                }
            });

            AlertDialog regionsDialog = regionsBuilder.create();
            regionsDialog.show(); //display the dialog

            return true;
        }

        if (id == R.id.choises) {

            //Set the number of choises, 3,6,9

            final String [] possibleChoices =
                    getResources().getStringArray(R.array.guessesList);

            AlertDialog.Builder choiseBuilder= new AlertDialog.Builder(this);

            choiseBuilder.setTitle(R.string.choices);

            //Add possible choise's and set tjhe action to take when
            //an item is selected

            choiseBuilder.setItems(R.array.guessesList,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            guessRows = Integer.parseInt(possibleChoices[which].toString())/3;
                            resetQuiz();
                        }
                    }

            );
            AlertDialog choiseDialog = choiseBuilder.create();
            choiseDialog.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
