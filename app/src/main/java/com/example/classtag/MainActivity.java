package com.example.classtag;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SubjectTagAdapter subjectAdapter;
    private List<SubjectTag> allSubjects;
    private List<SubjectTag> filteredSubjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check authentication
        if (!AuthHelper.isLoggedIn(this)) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        setupGreeting();
        setupHeaderButtons();
        setupStats();
        setupSchedule();
        setupSubjectGrid();
        setupSearchFilter();
        setupAnnouncements();
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!AuthHelper.isLoggedIn(this)) {
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /** Sets a time-of-day aware greeting with user name. */
    private void setupGreeting() {
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour < 12) {
            greeting = getString(R.string.greeting_morning);
        } else if (hour < 18) {
            greeting = getString(R.string.greeting_afternoon);
        } else {
            greeting = getString(R.string.greeting_evening);
        }

        String userName = AuthHelper.getUserName(this);
        tvGreeting.setText(getString(R.string.greeting_template, greeting, userName));
    }

    /** Wires up Logout and Profile buttons in the header. */
    private void setupHeaderButtons() {
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        FrameLayout btnProfileHeader = findViewById(R.id.btnProfileHeader);

        // Working Button: Logout from Header
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> performLogout());
        }

        // Working Button: Profile Click
        if (btnProfileHeader != null) {
            btnProfileHeader.setOnClickListener(v -> showProfileDialog());
        }
    }

    private void performLogout() {
        AuthHelper.logout(this);
        Toast.makeText(this, R.string.msg_logout_success, Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void showProfileDialog() {
        String email = AuthHelper.getUserEmail(this);
        String name = AuthHelper.getUserName(this);

        String message = "Name: " + name + "\nEmail: " + email + "\nStatus: Authenticated";

        new MaterialAlertDialogBuilder(this)
                .setTitle("User Profile")
                .setMessage(message)
                .setPositiveButton(R.string.btn_logout, (dialog, which) -> performLogout())
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /** Populates attendance / GPA / assignments-due stat cards. */
    private void setupStats() {
        TextView tvAttendance = findViewById(R.id.tvStatAttendance);
        TextView tvGpa = findViewById(R.id.tvStatGpa);
        TextView tvDue = findViewById(R.id.tvStatDue);

        tvAttendance.setText(R.string.stat_attendance_value);
        tvGpa.setText(R.string.stat_gpa_value);
        tvDue.setText(R.string.stat_due_value);
    }

    /** Builds the horizontally scrolling "Today schedule" strip. */
    private void setupSchedule() {
        LinearLayout scheduleContainer = findViewById(R.id.scheduleContainer);
        LayoutInflater inflater = LayoutInflater.from(this);

        List<ScheduleItem> schedule = getTodaysSchedule();
        for (ScheduleItem item : schedule) {
            View cardView = inflater.inflate(R.layout.item_schedule_card, scheduleContainer, false);

            TextView tvTime = cardView.findViewById(R.id.tvScheduleTime);
            TextView tvSubject = cardView.findViewById(R.id.tvScheduleSubject);
            TextView tvRoom = cardView.findViewById(R.id.tvScheduleRoom);
            View vTimeBar = cardView.findViewById(R.id.vTimeBar);

            tvTime.setText(item.getTime());
            tvSubject.setText(item.getSubject());
            tvRoom.setText(item.getRoom());
            vTimeBar.setBackgroundColor(ContextCompat.getColor(this, item.getAccentColorRes()));

            cardView.setOnClickListener(v ->
                    Toast.makeText(this, item.getSubject() + " · " + item.getTime(), Toast.LENGTH_SHORT).show());

            scheduleContainer.addView(cardView);
        }
    }

    private List<ScheduleItem> getTodaysSchedule() {
        List<ScheduleItem> list = new ArrayList<>();
        list.add(new ScheduleItem("8:00 – 8:45 AM", "Calculus I", "Room 204 · Mr. Alcaraz", R.color.brand_blue));
        list.add(new ScheduleItem("9:00 – 9:45 AM", "Bio-Chemistry", "Room 118 · Ms. Reyes", R.color.accent_green));
        list.add(new ScheduleItem("10:00 – 10:45 AM", "Literature II", "Room 302 · Mrs. Santos", R.color.brand_blue_dark));
        list.add(new ScheduleItem("11:00 – 11:45 AM", "World History", "Room 210 · Mr. Cruz", R.color.accent_coral));
        list.add(new ScheduleItem("1:00 – 1:45 PM", "Algorithms", "Lab 1 · Mr. Tan", R.color.brand_navy));
        list.add(new ScheduleItem("2:00 – 2:45 PM", "Robotics", "Lab 2 · Coach D.", R.color.brand_blue));
        return list;
    }

    /** Builds the 2-column grid of class tags. */
    private void setupSubjectGrid() {
        RecyclerView rvSubjects = findViewById(R.id.rvSubjects);
        rvSubjects.setLayoutManager(new GridLayoutManager(this, 2));

        allSubjects = getSampleSubjects();
        filteredSubjects = new ArrayList<>(allSubjects);

        subjectAdapter = new SubjectTagAdapter(filteredSubjects, subjectTag ->
                Toast.makeText(this, subjectTag.getName() + " selected", Toast.LENGTH_SHORT).show());
        rvSubjects.setAdapter(subjectAdapter);
    }

    /** Enables real-time search filtering. */
    private void setupSearchFilter() {
        EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterSubjects(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filterSubjects(String query) {
        filteredSubjects.clear();
        if (query.trim().isEmpty()) {
            filteredSubjects.addAll(allSubjects);
        } else {
            String lower = query.toLowerCase().trim();
            for (SubjectTag tag : allSubjects) {
                if (tag.getName().toLowerCase().contains(lower)) {
                    filteredSubjects.add(tag);
                }
            }
        }
        subjectAdapter.updateList(filteredSubjects);
    }

    private List<SubjectTag> getSampleSubjects() {
        List<SubjectTag> list = new ArrayList<>();
        list.add(new SubjectTag("#DataStructures", "45 topics", R.color.brand_blue));
        list.add(new SubjectTag("#Algorithms", "62 topics", R.color.accent_green));
        list.add(new SubjectTag("#Robotics", "28 topics", R.color.brand_blue_dark));
        list.add(new SubjectTag("#Architecture", "34 topics", R.color.accent_coral));
        list.add(new SubjectTag("#Psychology", "51 topics", R.color.brand_navy));
        list.add(new SubjectTag("#Economics", "42 topics", R.color.brand_blue));
        list.add(new SubjectTag("#Calculus", "78 topics", R.color.brand_blue_light));
        list.add(new SubjectTag("#Trigonometry", "39 topics", R.color.brand_blue_dark));
        list.add(new SubjectTag("#DigitalMarketing", "22 topics", R.color.accent_green));
        list.add(new SubjectTag("#Philosophy", "18 topics", R.color.accent_coral));
        list.add(new SubjectTag("#Physics", "58 topics", R.color.brand_navy));
        list.add(new SubjectTag("#Chemistry", "64 topics", R.color.brand_blue));
        return list;
    }

    /** Builds the announcements list. */
    private void setupAnnouncements() {
        LinearLayout announcementContainer = findViewById(R.id.announcementContainer);
        LayoutInflater inflater = LayoutInflater.from(this);

        List<Announcement> announcements = getSampleAnnouncements();
        for (Announcement announcement : announcements) {
            View rowView = inflater.inflate(R.layout.item_announcement, announcementContainer, false);

            TextView tvTitle = rowView.findViewById(R.id.tvAnnouncementTitle);
            TextView tvMeta = rowView.findViewById(R.id.tvAnnouncementMeta);

            tvTitle.setText(announcement.getTitle());
            tvMeta.setText(announcement.getMeta());

            rowView.setOnClickListener(v ->
                    Toast.makeText(this, announcement.getTitle(), Toast.LENGTH_SHORT).show());

            announcementContainer.addView(rowView);
        }
    }

    private List<Announcement> getSampleAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        list.add(new Announcement("Science fair submissions due Friday", "Posted by Ms. Reyes · 2h ago"));
        list.add(new Announcement("Parent-teacher conference next Tuesday", "Posted by Admin Office · 5h ago"));
        list.add(new Announcement("Library extended hours during finals week", "Posted by Mr. Cruz · 1d ago"));
        return list;
    }

    /** Wires up bottom navigation, including Working Add Class Tag Button. */
    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_schedule) {
                Toast.makeText(this, "Schedule", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_add) {
                showAddClassTagDialog();
                return true;
            } else if (id == R.id.nav_notices) {
                Toast.makeText(this, "Notices", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                showProfileDialog();
                return true;
            }
            return false;
        });
    }

    /** Working Button Feature: Show Add Class Tag Dialog */
    private void showAddClassTagDialog() {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.dialog_add_tag_title);

        final EditText input = new EditText(this);
        input.setHint(R.string.dialog_add_tag_hint);
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        FrameLayout container = new FrameLayout(this);
        container.setPadding(margin, margin, margin, margin);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton(R.string.btn_add, (dialog, which) -> {
            String tagName = input.getText().toString().trim();
            if (!tagName.isEmpty()) {
                if (!tagName.startsWith("#")) {
                    tagName = "#" + tagName;
                }
                SubjectTag newTag = new SubjectTag(tagName, "1 topic", R.color.brand_blue);
                allSubjects.add(0, newTag);
                filterSubjects("");
                Toast.makeText(MainActivity.this, "Added " + tagName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Tag name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
