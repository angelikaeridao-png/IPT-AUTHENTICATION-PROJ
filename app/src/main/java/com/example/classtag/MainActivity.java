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
import androidx.core.widget.NestedScrollView;
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
    private List<ScheduleItem> allScheduleItems;
    private List<Announcement> allAnnouncements;

    private EditText etSearch;
    private RecyclerView rvSubjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthHelper.isLoggedIn(this)) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        allScheduleItems = getSampleSchedule();
        allAnnouncements = getSampleAnnouncements();

        setupGreeting();
        setupHeaderButtons();
        setupStats();
        renderSchedule("");
        setupSubjectGrid();
        setupSearchFilter();
        renderAnnouncements("");
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

    private void setupHeaderButtons() {
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        FrameLayout btnProfileHeader = findViewById(R.id.btnProfileHeader);

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> performLogout());
        }

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

    private void setupStats() {
        TextView tvAttendance = findViewById(R.id.tvStatAttendance);
        TextView tvGpa = findViewById(R.id.tvStatGpa);
        TextView tvDue = findViewById(R.id.tvStatDue);

        tvAttendance.setText(R.string.stat_attendance_value);
        tvGpa.setText(R.string.stat_gpa_value);
        tvDue.setText(R.string.stat_due_value);
    }

    private void renderSchedule(String filterQuery) {
        LinearLayout scheduleContainer = findViewById(R.id.scheduleContainer);
        if (scheduleContainer == null || allScheduleItems == null) return;
        scheduleContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        String clean = filterQuery != null ? filterQuery.trim().toLowerCase().replaceAll("^#", "") : "";

        for (ScheduleItem item : allScheduleItems) {
            if (item == null) continue;
            if (!clean.isEmpty()) {
                String subject = item.getSubject() != null ? item.getSubject().toLowerCase() : "";
                String room = item.getRoom() != null ? item.getRoom().toLowerCase() : "";
                String time = item.getTime() != null ? item.getTime().toLowerCase() : "";
                if (!subject.contains(clean) && !room.contains(clean) && !time.contains(clean)) {
                    continue;
                }
            }

            View cardView = inflater.inflate(R.layout.item_schedule_card, scheduleContainer, false);
            TextView tvTime = cardView.findViewById(R.id.tvScheduleTime);
            TextView tvSubject = cardView.findViewById(R.id.tvScheduleSubject);
            TextView tvRoom = cardView.findViewById(R.id.tvScheduleRoom);
            View vTimeBar = cardView.findViewById(R.id.vTimeBar);

            if (tvTime != null) tvTime.setText(item.getTime());
            if (tvSubject != null) tvSubject.setText(item.getSubject());
            if (tvRoom != null) tvRoom.setText(item.getRoom());
            if (vTimeBar != null) vTimeBar.setBackgroundColor(ContextCompat.getColor(this, item.getAccentColorRes()));

            cardView.setOnClickListener(v -> showScheduleItemOptions(item));

            scheduleContainer.addView(cardView);
        }
    }

    private List<ScheduleItem> getSampleSchedule() {
        List<ScheduleItem> list = new ArrayList<>();
        list.add(new ScheduleItem("8:00 – 8:45 AM", "Calculus I", "Room 204 · Mr. Alcaraz", R.color.brand_blue));
        list.add(new ScheduleItem("9:00 – 9:45 AM", "Bio-Chemistry", "Room 118 · Ms. Reyes", R.color.accent_green));
        list.add(new ScheduleItem("10:00 – 10:45 AM", "Literature II", "Room 302 · Mrs. Santos", R.color.brand_blue_dark));
        list.add(new ScheduleItem("11:00 – 11:45 AM", "World History", "Room 210 · Mr. Cruz", R.color.accent_coral));
        list.add(new ScheduleItem("1:00 – 1:45 PM", "Algorithms", "Lab 1 · Mr. Tan", R.color.brand_navy));
        list.add(new ScheduleItem("2:00 – 2:45 PM", "Robotics", "Lab 2 · Coach D.", R.color.brand_blue));
        return list;
    }

    private void setupSubjectGrid() {
        rvSubjects = findViewById(R.id.rvSubjects);
        rvSubjects.setLayoutManager(new GridLayoutManager(this, 2));

        allSubjects = getSampleSubjects();
        filteredSubjects = new ArrayList<>(allSubjects);

        subjectAdapter = new SubjectTagAdapter(filteredSubjects, subjectTag ->
                Toast.makeText(this, subjectTag.getName() + " selected", Toast.LENGTH_SHORT).show());
        rvSubjects.setAdapter(subjectAdapter);
    }

    private void setupSearchFilter() {
        etSearch = findViewById(R.id.etSearch);
        NestedScrollView mainScrollView = findViewById(R.id.mainScrollView);

        if (etSearch != null) {
            etSearch.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && mainScrollView != null) {
                    mainScrollView.post(() -> mainScrollView.scrollTo(0, 0));
                }
            });

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterDashboard(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filterDashboard(String query) {
        String clean = query != null ? query.trim().toLowerCase().replaceAll("^#", "") : "";

        if (allSubjects == null || filteredSubjects == null) return;

        filteredSubjects.clear();
        if (clean.isEmpty()) {
            filteredSubjects.addAll(allSubjects);
        } else {
            for (SubjectTag tag : allSubjects) {
                if (tag == null || tag.getName() == null) continue;
                String tagName = tag.getName().toLowerCase().replaceAll("^#", "");
                if (tagName.contains(clean) || tag.getName().toLowerCase().contains(clean)) {
                    filteredSubjects.add(tag);
                }
            }
        }
        if (subjectAdapter != null) {
            subjectAdapter.updateList(filteredSubjects);
        }

        renderSchedule(clean);
        renderAnnouncements(clean);
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

    private void renderAnnouncements(String filterQuery) {
        LinearLayout announcementContainer = findViewById(R.id.announcementContainer);
        if (announcementContainer == null || allAnnouncements == null) return;
        announcementContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        String clean = filterQuery != null ? filterQuery.trim().toLowerCase().replaceAll("^#", "") : "";

        for (Announcement announcement : allAnnouncements) {
            if (announcement == null) continue;
            if (!clean.isEmpty()) {
                String title = announcement.getTitle() != null ? announcement.getTitle().toLowerCase() : "";
                String meta = announcement.getMeta() != null ? announcement.getMeta().toLowerCase() : "";
                if (!title.contains(clean) && !meta.contains(clean)) {
                    continue;
                }
            }

            View rowView = inflater.inflate(R.layout.item_announcement, announcementContainer, false);
            TextView tvTitle = rowView.findViewById(R.id.tvAnnouncementTitle);
            TextView tvMeta = rowView.findViewById(R.id.tvAnnouncementMeta);

            if (tvTitle != null) tvTitle.setText(announcement.getTitle());
            if (tvMeta != null) tvMeta.setText(announcement.getMeta());

            rowView.setOnClickListener(v -> showAnnouncementOptions(announcement));

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

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(this, "Home Dashboard", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_schedule) {
                Toast.makeText(this, "Today's Schedule", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_add) {
                showAddChooserDialog();
                return false;
            } else if (id == R.id.nav_notices) {
                Toast.makeText(this, "Notices & Announcements", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                showProfileDialog();
                return true;
            }
            return false;
        });
    }

    private void showAddChooserDialog() {
        String[] options = {"Class Tag", "Schedule Note / Class", "Announcement / Notice"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("What would you like to add?")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddClassTagDialog();
                    } else if (which == 1) {
                        showAddScheduleNoteDialog();
                    } else if (which == 2) {
                        showAddAnnouncementDialog();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

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

                if (etSearch != null) etSearch.setText("");
                filterDashboard("");

                if (rvSubjects != null) rvSubjects.smoothScrollToPosition(0);
                Toast.makeText(MainActivity.this, "Added " + tagName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Tag name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddScheduleNoteDialog() {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Add Schedule Note");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(margin, margin, margin, margin);

        final EditText etSubject = new EditText(this);
        etSubject.setHint("Subject (e.g. Robotics)");
        layout.addView(etSubject);

        final EditText etTime = new EditText(this);
        etTime.setHint("Time (e.g. 3:00 – 3:45 PM)");
        layout.addView(etTime);

        final EditText etRoom = new EditText(this);
        etRoom.setHint("Room / Teacher (e.g. Lab 2 · Coach D.)");
        layout.addView(etRoom);

        builder.setView(layout);

        builder.setPositiveButton("Add Note", (dialog, which) -> {
            String subject = etSubject.getText().toString().trim();
            String time = etTime.getText().toString().trim();
            String room = etRoom.getText().toString().trim();

            if (!subject.isEmpty()) {
                if (time.isEmpty()) time = "TBD";
                if (room.isEmpty()) room = "General Room";

                ScheduleItem item = new ScheduleItem(time, subject, room, R.color.brand_blue);
                allScheduleItems.add(0, item);

                if (etSearch != null) etSearch.setText("");
                filterDashboard("");

                Toast.makeText(MainActivity.this, "Added schedule for " + subject, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Subject name is required", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddAnnouncementDialog() {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Add Announcement");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(margin, margin, margin, margin);

        final EditText etTitle = new EditText(this);
        etTitle.setHint("Announcement Title");
        layout.addView(etTitle);

        final EditText etMeta = new EditText(this);
        etMeta.setHint("Author / Info (e.g. Posted by Admin)");
        layout.addView(etMeta);

        builder.setView(layout);

        builder.setPositiveButton("Post", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String meta = etMeta.getText().toString().trim();

            if (!title.isEmpty()) {
                if (meta.isEmpty()) meta = "Posted just now";

                Announcement announcement = new Announcement(title, meta);
                allAnnouncements.add(0, announcement);

                if (etSearch != null) etSearch.setText("");
                filterDashboard("");

                Toast.makeText(MainActivity.this, "Posted announcement", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Title is required", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showScheduleItemOptions(ScheduleItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(item.getSubject())
                .setMessage("Time: " + item.getTime() + "\nLocation: " + item.getRoom())
                .setPositiveButton("Remove", (dialog, which) -> {
                    allScheduleItems.remove(item);
                    filterDashboard(etSearch != null ? etSearch.getText().toString() : "");
                    Toast.makeText(MainActivity.this, "Removed " + item.getSubject(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAnnouncementOptions(Announcement announcement) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(announcement.getTitle())
                .setMessage(announcement.getMeta())
                .setPositiveButton("Remove", (dialog, which) -> {
                    allAnnouncements.remove(announcement);
                    filterDashboard(etSearch != null ? etSearch.getText().toString() : "");
                    Toast.makeText(MainActivity.this, "Removed announcement", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }
}