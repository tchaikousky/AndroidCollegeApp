package com.tchaikousky.abm1_tchaikousky.UI;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.tchaikousky.abm1_tchaikousky.Database.AssessmentRepository;
import com.tchaikousky.abm1_tchaikousky.Database.CourseRepository;
import com.tchaikousky.abm1_tchaikousky.Entities.Assessment;
import com.tchaikousky.abm1_tchaikousky.Entities.Course;
import com.tchaikousky.abm1_tchaikousky.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AddEditViewAssessment extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private int courseID;
    private DatePicker dueDate;
    private DatePicker goalDateDP;
    private String title;
    private int assessmentID;
    private EditText noteEditText;
    private EditText titleEditText;
    private Spinner assessmentTypeSpinner;
    private AssessmentRepository assessmentRepository;
    private String type;
    private String note;
    private TextView dateTextView;
    private TextView goalTextView;
    private Button editButton;
    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;
    private String dateDue;
    private CourseRepository courseRepository;
    private String courseMentor;
    private String mentorPhone;
    private String mentorEmail;
    private String courseNote;
    private String courseEndDate;
    private String courseStartDate;
    private String courseStatus;
    private int courseTermID;
    private String courseTitle;
    private boolean notifications;
    private boolean courseNotifications;
    private ToggleButton notificationToggle;
    private int assessmentNotificationNumber;
    private int courseNotificationNumber;
    private int goalNotificationNumber;
    private String goalDateString;
    private int courseEndNotificationNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_view_assessment);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Assessment");

        TextView typeTextView = new TextView(this);
        dateTextView = new TextView(this);
        goalTextView = new TextView(this);
        editButton = new Button(this);
        editButton.setText(R.string.edit);
        editButton.setOnClickListener(v -> setEditView());

        notificationToggle = findViewById(R.id.assessmentNotificationToggle);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        deleteButton = new Button(this);
        deleteButton.setText(R.string.delete);
        deleteButton.setOnClickListener(v -> delete());
        assessmentRepository = new AssessmentRepository(getApplication());
        assessmentTypeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        type = getIntent().getStringExtra("type");
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.assessmentType, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assessmentTypeSpinner.setAdapter(typeAdapter);
        courseID = getIntent().getIntExtra("courseID", 0);
        note = getIntent().getStringExtra("note");
        title = getIntent().getStringExtra("title");
        dateDue = getIntent().getStringExtra("dueDate");
        goalDateString = getIntent().getStringExtra("goalDate");
        notifications = getIntent().getBooleanExtra("notifications", false);
        assessmentNotificationNumber = getIntent().getIntExtra("notificationNumber", 0);
        goalNotificationNumber = getIntent().getIntExtra("goalNotificationNumber", 0);
        dueDate = findViewById(R.id.dueDatePicker);
        goalDateDP = findViewById(R.id.goalDatePicker);
        titleEditText = findViewById(R.id.titleEditText);
        assessmentID = getIntent().getIntExtra("assessmentID", 0);
        noteEditText = findViewById(R.id.noteEditText);
        assessmentTypeSpinner = findViewById(R.id.typeSpinner);
        assessmentTypeSpinner.setOnItemSelectedListener(this);
        assessmentTypeSpinner.setEnabled(true);
        courseRepository = new CourseRepository(getApplication());

        if(assessmentID != 0) {
            setNonEditView();
            assessmentTypeSpinner.setSelection(typeAdapter.getPosition(type));
        }

        if(notifications) {
            notificationToggle.setText(R.string.on);
        } else {
            notificationToggle.setText(R.string.off);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.homeScreen:
                Intent intent = new Intent(AddEditViewAssessment.this, MainActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_menu, menu);
        menu.removeItem(R.id.share);
        return true;
    }

    public void saveAssessment(View view) {
        String startDay = String.valueOf(dueDate.getDayOfMonth());
        String startMonth = String.valueOf(dueDate.getMonth() + 1);
        String startYear = String.valueOf(dueDate.getYear());
        String goalDay = String.valueOf(goalDateDP.getDayOfMonth());
        String goalMonth = String.valueOf(goalDateDP.getMonth() + 1);
        String goalYear = String.valueOf(goalDateDP.getYear());

        notifications = notificationToggle.getText().toString().equals("ON");
        dateDue = startMonth.concat("-").concat(startDay).concat("-").concat(startYear);
        goalDateString = goalMonth.concat("-").concat(goalDay).concat("-").concat(goalYear);
        if(titleEditText.getText().toString().trim().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("All fields except 'Notes' is required.");
            builder.setPositiveButton("Ok", (dialog, id) -> {
            });
            builder.show();
        } else {
            if ((assessmentNotificationNumber != 0) && notifications) {
                notification("Assessment Due Today", dateDue);
            } else if((assessmentNotificationNumber == 0) && notifications) {
                assessmentNotificationNumber = MainActivity.notificationNumber;
                notification("Assessment Due Today", dateDue);
                MainActivity.notificationNumber++;
            }

            if ((goalNotificationNumber != 0) && notifications) {
                goalNotification("Assessment Goal Today", goalDateString);
            } else if((goalNotificationNumber == 0) && notifications) {
                goalNotificationNumber = MainActivity.notificationNumber;
                goalNotification("Assessment Goal Today", goalDateString);
                MainActivity.notificationNumber++;
            }

            Assessment assessment = new Assessment(assessmentID, titleEditText.getText().toString(),
                    dateDue, type, noteEditText.getText().toString(), courseID, notifications,
                    assessmentNotificationNumber, goalNotificationNumber, goalDateString);
            if (assessmentID == 0) {
                assessmentRepository.insert(assessment);
            } else {
                assessmentRepository.update(assessment);
            }

            setView();
        }
    }

    public void cancel(View view) {
        setView();
    }

    public void delete() {
        Assessment thisAssessment = new Assessment(assessmentID, title,
                dateDue, type, note, courseID, notifications, assessmentNotificationNumber,
                goalNotificationNumber, goalDateString);
        assessmentRepository.delete(thisAssessment);
        setView();
    }

    public void setView() {
        ArrayList<Course> course = (ArrayList<Course>) courseRepository.getSingleCourse(courseID);
        for(Course c : course) {
            courseID = c.getCourseID();
            courseMentor = c.getCourseMentor();
            mentorPhone = c.getPhone();
            mentorEmail = c.getEmail();
            courseNote = c.getNote();
            courseEndDate = c.getEndDate();
            courseStartDate = c.getStartDate();
            courseStatus = c.getStatus();
            courseTermID = c.getTermID();
            courseTitle = c.getTitle();
            courseNotifications = c.getNotifications();
            courseNotificationNumber = c.getNotificationNumber();
        }
        Intent intent = new Intent(AddEditViewAssessment.this, AddEditViewCourse.class);
        intent.putExtra("courseID", courseID);
        intent.putExtra("courseMentor", courseMentor);
        intent.putExtra("mentorPhone", mentorPhone);
        intent.putExtra("mentorEmail", mentorEmail);
        intent.putExtra("endDateTextView", courseEndDate);
        intent.putExtra("startDateTextView", courseStartDate);
        intent.putExtra("title", courseTitle);
        intent.putExtra("note", courseNote);
        intent.putExtra("courseStatus", courseStatus);
        intent.putExtra("termID", courseTermID);
        intent.putExtra("courseNotifications", courseNotifications);
        intent.putExtra("courseNotificationNumber", courseNotificationNumber);
        intent.putExtra("courseEndNotificationNumber", courseEndNotificationNumber);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        type = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void setNonEditView() {
        Objects.requireNonNull(getSupportActionBar()).setTitle("Assessment - " + title);
        titleEditText.setEnabled(false);
        titleEditText.setText(title);
        dateTextView.setText(dateDue);
        goalTextView.setText(goalDateString);
        noteEditText.setText(note);
        noteEditText.setEnabled(false);
        assessmentTypeSpinner.setEnabled(false);
        notificationToggle.setEnabled(false);

        ((ViewGroup) dueDate.getParent()).addView(dateTextView);
        ((ViewGroup) goalDateDP.getParent()).addView(goalTextView);
        ((ViewGroup) dateTextView.getParent()).removeView(dueDate);
        ((ViewGroup) goalTextView.getParent()).removeView(goalDateDP);
        ((ViewGroup) saveButton.getParent()).addView(editButton);
        ((ViewGroup) editButton.getParent()).removeView(saveButton);
        ((ViewGroup) editButton.getParent()).removeView(cancelButton);
        ((ViewGroup) editButton.getParent()).addView(deleteButton);

    }

    public void setEditView() {
        Objects.requireNonNull(getSupportActionBar()).setTitle("Edit Assessment - " + title);
        String[] date = dateDue.split("-", 0);
        String[] goalDate = goalDateString.split("-", 0);
        dueDate.updateDate(Integer.parseInt(date[2]), Integer.parseInt(date[0]) - 1,
                Integer.parseInt(date[1]));
        goalDateDP.updateDate(Integer.parseInt(goalDate[2]), Integer.parseInt(goalDate[0]) - 1,
                Integer.parseInt(goalDate[1]));
        titleEditText.setEnabled(true);
        noteEditText.setEnabled(true);
        assessmentTypeSpinner.setEnabled(true);
        notificationToggle.setEnabled(true);

        ((ViewGroup) dateTextView.getParent()).addView(dueDate);
        ((ViewGroup) goalTextView.getParent()).addView(goalDateDP);
        ((ViewGroup) dueDate.getParent()).removeView(dateTextView);
        ((ViewGroup) goalDateDP.getParent()).removeView(goalTextView);
        ((ViewGroup) editButton.getParent()).addView(saveButton);
        ((ViewGroup) saveButton.getParent()).removeView(editButton);
        ((ViewGroup) saveButton.getParent()).removeView(deleteButton);
        ((ViewGroup) saveButton.getParent()).removeView(cancelButton);
        ((ViewGroup) saveButton.getParent()).addView(cancelButton);
    }

    public void toggleSwitch(View view) {
        if(notificationToggle.getText().equals("ON")) {
            notificationToggle.setText(R.string.off);
            notifications = false;
        } else {
            notificationToggle.setText(R.string.on);
            notifications = true;
        }
    }

    public void notification(String assessmentNotification, String dateDue) {
        Date testDate = null;
        String format = "MM-dd-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        try {
            testDate = sdf.parse(dateDue);
        } catch (ParseException e){
            e.printStackTrace();
        }
        Long trigger = testDate.getTime();

        Intent intent = new Intent(AddEditViewAssessment.this, MyReceiver.class);

        if(notificationToggle.getText().toString().equals("ON")) {
            intent.putExtra("key", titleEditText.getText().toString() + " is due today!");
            intent.putExtra("contentTitle", assessmentNotification);
            intent.putExtra("notificationNumber", assessmentNotificationNumber);
            intent.putExtra("notifications", notifications);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(AddEditViewAssessment.this,
                    MainActivity.notificationNumber, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, trigger, pendingIntent);
        }
    }

    public void goalNotification(String assessmentNotification, String goalDateString) {
        Date testDate = null;
        String format = "MM-dd-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        try {
            testDate = sdf.parse(goalDateString);
        } catch (ParseException e){
            e.printStackTrace();
        }
        Long trigger = testDate.getTime();

        Intent intent = new Intent(AddEditViewAssessment.this, MyReceiver.class);

        if(notificationToggle.getText().toString().equals("ON")) {
            intent.putExtra("key", titleEditText.getText().toString() + " has a goal completion " +
                    "date of today!");
            intent.putExtra("contentTitle", assessmentNotification);
            intent.putExtra("notificationNumber", goalNotificationNumber);
            intent.putExtra("notifications", notifications);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(AddEditViewAssessment.this,
                    MainActivity.notificationNumber, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, trigger, pendingIntent);
        }
    }
}