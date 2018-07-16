package kit.boundlesskitexample;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import kit.boundlesskitexample.db.TaskContract;
import kit.boundlesskitexample.db.TaskDbHelper;
import kit.boundless.BoundlessKit;
import kit.boundless.reward.attention.PulseAnimator;
import kit.boundless.reward.attention.RotationAnimator;
import kit.boundless.reward.attention.ShimmyAnimator;
import kit.boundless.reward.attention.VibrationAnimator;
import kit.boundless.reward.overlay.Confetti;
import kit.boundless.reward.overlay.Emojisplosion;
import kit.boundless.reward.overlay.SheenView;
import kit.boundless.reward.overlay.candybar.Candybar;
import kit.boundless.reward.overlay.particle.ConfettoDrawable;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;

    private View contentView;
    private ImageView logoView;

    /*
    An enum made for demonstartion of out-of-the-box rewards provided in BoundlessKit.
    In your app, you could make an enum consisting of your reward decisions that were configured on the developer dashboard.
     */
    enum RewardSample { shimmy, pulse, vibrate, rotate, sheen, emojisplosion, confetti, candybar}
    RewardSample rewardSample = RewardSample.confetti;

    /*
    Create a method like this in your app. It has 2 purposes
    1) Request a reinforcement decision from BoundlessKit
    2) Depending on the decision, show a reward
     */
    private void reinforcementCall() {
        BoundlessKit.reinforce(getApplicationContext(), "taskCompleted", null, new BoundlessKit.ReinforcementCallback() {
            @Override
            public void onReinforcement(String reinforcement) {
                switch (reinforcement) {
                    case "stars":
                        rewardSample = RewardSample.confetti;
                        break;
                    case "medalStar":
                        rewardSample = RewardSample.emojisplosion;
                        break;
                    case "thumbsUp":
                        rewardSample = RewardSample.vibrate;
                        break;
                    default:
                        // Show nothing! This is called a neutral response,
                        // and builds up the good feelings for the next surprise!
                        return;
                }

                // Show some reward and make them feel good!
                showReward();

            }
        });
    }

    /*
    This method is just for demonstration, and does not need to be in your app.
    This method cycles through a few of the out-of-the-box rewards. Change the values inside the reinforcementCall() switch statement to sample the others.
     */
    private void showReward() {
        switch (rewardSample) {
            case shimmy:
                //// Shimmy Sample
                //
                // Create an animator instance, set values, and animate()
                //
                new ShimmyAnimator()
                        .setCount(3)
                        .setHorizontally(true)
                        .animate(findViewById(R.id.list_title));


                break;
            case pulse:
                //// Pulse Sample
                //
                // Create an animator instance, set values, and animate()
                //
                new PulseAnimator()
                        .setCount(3)
                        .animate(findViewById(R.id.list_title));


                break;
            case vibrate:
                //// Vibration Sample
                //
                // Create an animator instance, set values, and animate()
                //
                new VibrationAnimator()
                        .setScale(0.8f)
                        .animate(logoView);


                break;
            case rotate:
                //// Rotation Sample
                //
                // Create an animator instance, set values, and animate()
                //
                new RotationAnimator()
                        .setCount(2)
                        .animate(findViewById(R.id.list_title));


                break;
            case sheen:
                //// Sheen Sample
                //
                // add SheenView to activity layout, after view to animate over
                //
                ((SheenView) findViewById(R.id.sheen)).start();


                break;
            case emojisplosion:
                //// Emojisplosion Sample
                //
                // get reference to view to animate over, set values, and animate()
                //
                new Emojisplosion()
                        .setContent(MainActivity.this, "\uD83D\uDE00\n")
                        .setxPosition(contentView.getWidth() / 2)
                        .setyPosition(contentView.getMeasuredHeight())
                        .setScale(2f)
                        .setLifetime(4000)
                        .animate(contentView);


                break;
            case confetti:
                //// Confetti Sample
                //
                // Create confetti animation object in `onCreate()` for better memory usage
                //
                confetti.start();


                break;
            case candybar:
                //// CandyBar Sample
                //
                // Create a Candybar view, similar to the Snackbar view in Android, over the main content view.
                // Customize text, color, or add an icon
                //
                Candybar candybar = new Candybar(contentView,
                        Candybar.DIRECTION_TOP,
                        "Knocked it out of the park! " + ("\u26be\ufe0f") + ("\ud83d\ude80"),
                        3200);
                candybar.setBackgroundColor(Color.parseColor("#336633"))
                        .setDismissOnTap(true)
                        .setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.stars), 48, true)
                        .show();
        }
    }

    Confetti confetti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contentView = findViewById(android.R.id.content);
        logoView = findViewById(R.id.header_icon);

        mHelper = new TaskDbHelper(MainActivity.this);
        mTaskListView = findViewById(R.id.list_todo);
        updateUI();
        BoundlessKit.debugMode = true;


        // convenience function to create confetti demo. Done here to avoid lag on UI thread
        confetti = new Confetti().addConfetti(
                50,
                50,
                Arrays.asList(
                        ConfettoDrawable.Shape.RECTANGLE,
                        ConfettoDrawable.Shape.RECTANGLE,
                        ConfettoDrawable.Shape.SPIRAL,
                        ConfettoDrawable.Shape.CIRCLE
                ),
                Arrays.asList(
                        ColorUtils.setAlphaComponent(Color.parseColor("#4d81fb"), 204),
                        ColorUtils.setAlphaComponent(Color.parseColor("#4ac4fb"), 204),
                        ColorUtils.setAlphaComponent(Color.parseColor("#9243f9"), 204),
                        ColorUtils.setAlphaComponent(Color.parseColor("#fdc33b"), 204),
                        ColorUtils.setAlphaComponent(Color.parseColor("#f7332f"), 204)
                )
        ).setTarget(contentView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                final EditText taskEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Add a new task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());
                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                                db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                        null,
                                        values,
                                        SQLiteDatabase.CONFLICT_REPLACE);
                                db.close();
                                updateUI();
                                BoundlessKit.reinforce(getApplicationContext(), "action1", null, new BoundlessKit.ReinforcementCallback() {
                                    @Override
                                    public void onReinforcement(String reinforcementDecision) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
                return true;

            case R.id.action_add_demo_tasks:
                addDemoTasks();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addDemoTasks(){
        BoundlessKit.track(getApplicationContext(), "addedDemoTasks", null);

        String demoTasks[] = {"Feed the kitties", "Feed the mice", "Feed the snakes the mice", "Feed the mongoose the snakes", "a", "b", "c"};
        SQLiteDatabase db = mHelper.getWritableDatabase();
        for(String taskText : demoTasks){
            String task = String.valueOf(taskText);
            ContentValues values = new ContentValues();
            values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
            db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
        printDB(db);
        db.close();
        updateUI();

    }

    private void printDB(SQLiteDatabase db){
        Cursor rows = null;
        try {
            rows = db.rawQuery("select * from " + TaskContract.TaskEntry.TABLE, null);
            rows.moveToFirst();
            for (int i = 0; i < rows.getCount(); i++) {
//                Log.v("DB row " + i, rows.getString(1));
                rows.moveToNext();
            }
        } finally {
            if(rows != null) { rows.close(); }
        }
    }

    public void deleteTask(View view) {
        TextView taskTextView = (TextView) view.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        Log.v(TAG, "Starting to delete " + task + "...");
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});

        printDB(db);
        db.close();
        updateUI();

    }


    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = mHelper.getReadableDatabase();
            cursor = db.query(TaskContract.TaskEntry.TABLE,
                    new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                    null, null, null, null, null);

            while (cursor.moveToNext()) {
                int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
                taskList.add(cursor.getString(idx));
            }

            if (mAdapter == null) {
                mAdapter = new ArrayAdapter<>(this,
                        R.layout.item_todo,
                        R.id.task_title,
                        taskList);
                mTaskListView.setAdapter(mAdapter);

                final SwipeToDismissTouchListener<ListViewAdapter> touchListener =
                        new SwipeToDismissTouchListener<>(
                                new ListViewAdapter(mTaskListView),
                                new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
                                    @Override
                                    public boolean canDismiss(int position) {
                                        return true;
                                    }

                                    @Override
                                    public void onPendingDismiss(ListViewAdapter recyclerView, int position) {

                                    }

                                    @Override
                                    public void onDismiss(ListViewAdapter view, int position) {
                                        deleteTask(mTaskListView.getChildAt(position));
//                                    // The completed task has been deleted
//                                    // Let's give em some positive reinforcement!

                                        reinforcementCall();

//                                    HashMap<String, String> metaData = new HashMap<String, String>();
//                                    metaData.put("calories", "400");
//                                    BoundlessKit.track(getApplicationContext(), "foodItemAdded", null);

                                    }
                                });
// Dismiss the item automatically after 3 secondsj
                touchListener.setDismissDelay(0);

                mTaskListView.setOnTouchListener(touchListener);
                mTaskListView.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
                mTaskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (touchListener.existPendingDismisses()) {
                            touchListener.undoPendingDismiss();
                        } else {
                            Toast.makeText(MainActivity.this, "Position " + position, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else {
                mAdapter.clear();
                mAdapter.addAll(taskList);
                mAdapter.notifyDataSetChanged();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }
}
