package boundless.boundlesskitexample;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

import boundless.boundlesskitexample.db.TaskContract;
import boundless.boundlesskitexample.db.TaskDbHelper;
import boundless.kit.BoundlessKit;
import boundless.kit.rewards.animation.attention.CustomSheenView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;

    private View rootView;
    private ImageView logoView;
    private CustomSheenView sheen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logoView = (ImageView) findViewById(R.id.header_icon);
        sheen = findViewById(R.id.sheen);


        mHelper = new TaskDbHelper(MainActivity.this);
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        updateUI();
        BoundlessKit.debugMode = true;
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

//                                        View contentView = findViewById(android.R.id.content);
//
//                                        ViewGroup parent = (ViewGroup) logoView.getParent();
//
//                                        ImageView sheenView = new ImageView(MainActivity.this);
//                                        sheenView.setImageResource(boundless.kit.R.drawable.sheen);
//
////                                        FrameLayout relativeLayout = new FrameLayout(MainActivity.this);
////                                        relativeLayout.addView(sheenView);
////                                        relativeLayout.forceLayout();
////                                        parent.addView(relativeLayout);
//
//
////                                        Log.v("Test", "Type:" + parent);
////                                        ((ViewGroup)parent).addView(sheenView);


//                                        Bitmap returnedBitmap = Bitmap.createBitmap(logoView.getWidth(), logoView.getHeight(),Bitmap.Config.ARGB_8888);
//                                        Canvas canvas = new Canvas(returnedBitmap);
//                                        Drawable bgDrawable = logoView.getBackground();
//                                        if (bgDrawable!=null)
//                                            bgDrawable.draw(canvas);
//                                        logoView.draw(canvas);
//                                        sheen.mMask = returnedBitmap;

                                        sheen.setMask(logoView);
//                                        sheen.setImage(MainActivity.this.getResources(), R.drawable.sheen);
                                        sheen.start();

                                        sheen.setVisibility(View.VISIBLE);



//                                        Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shimmy);
//                                        rotate.setRepeatCount(1);
//                                        logoView.startAnimation(rotate);

////                                        new Emojisplosion().setContent(MainActivity.this.getResources().getDrawable(R.drawable.red_balloon))
//                                        new Emojisplosion().setContent(MainActivity.this, "\uD83D\uDE00\n")
//                                                .setX(contentView.getWidth() / 2)
//                                                .setY(contentView.getHeight() * 2 / 3)
//                                                .setScale(2f)
//                                                .setVelocity(-0.1f)
//                                                .setTarget(findViewById(android.R.id.content))
//                                                .start();

//                                        new VibrationAnimator().animate(contentView);


////                                        RotateAnimation rotateAnimation = new RotateAnimation(getApplicationContext(), Atrbu);
//                                        BoundlessKit.reinforce(getApplicationContext(), "taskCompleted", null, new BoundlessKit.ReinforcementCallback() {
//
//                                            @Override
//                                            public void onReinforcement(String reinforcement) {
//                                                // Show some candy and make them feel good!
//                                                CandyBar candyBar = null;
//                                                switch (reinforcement) {
//                                                    case "stars":
//                                                        candyBar = new CandyBar(findViewById(android.R.id.content).getRootView(), CandyBar.Candy.STARS, "Out of this world!", "We knew you could do it", Color.parseColor("#ffcc00"), CandyBar.LENGTH_SHORT);
//                                                        break;
//                                                    case "medalStar":
//                                                        candyBar = new CandyBar(findViewById(android.R.id.content).getRootView(), CandyBar.Candy.MEDALSTAR, "Great job!", "Run finished", Color.parseColor("#339933"), CandyBar.LENGTH_SHORT);
//                                                        break;
//                                                    case "thumbsUp":
//                                                        candyBar = new CandyBar(findViewById(android.R.id.content).getRootView(), CandyBar.Candy.THUMBSUP, "You go!", Color.parseColor("#336699"), CandyBar.LENGTH_SHORT);
//                                                        break;
//                                                    default:
//                                                        // Show nothing! This is called a neutral response,
//                                                        // and builds up the good feelings for the next surprise!
//                                                        break;
//                                                }
//                                                if (candyBar != null) {
//                                                    candyBar.show();
//                                                }
//                                            }
//
//                                        });


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
            cursor.close();
            db.close();
        }
    }
}
