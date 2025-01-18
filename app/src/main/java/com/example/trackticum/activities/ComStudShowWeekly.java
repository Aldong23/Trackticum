package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.adapters.ComActivityAdapter;
import com.example.trackticum.models.Activities;
import com.example.trackticum.utils.Constants;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComStudShowWeekly extends AppCompatActivity {

    private Toolbar toolbar;

    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;

    private ArrayAdapter<String> adapterItems;
    TextInputEditText titleEt, commentsEt;
    AutoCompleteTextView evaluationEt;
    ExtendedFloatingActionButton saveBtn, saveSignBtn;
    Button downloadBtn;

    RecyclerView recyclerView;
    private ComActivityAdapter adapter;
    private List<Activities> activitiesList;

    String studID, weeklyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_com_stud_show_weekly);
        setupWindowInsets();
        //setting up the status bar
        getWindow().setStatusBarColor(getResources().getColor(R.color.deepTeal));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().getInsetsController().setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(0); // Clear the flag for light status bar
        }

        //Add code here
        initializeData();
        setupListeners();
    }

    private void initializeData() {
        //set up action bar
        toolbar = findViewById(R.id.stud_show_weekly_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Weekly Report Details");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        studID = getIntent().getStringExtra("stud_id");
        weeklyID = getIntent().getStringExtra("weekly_id");

        titleEt = findViewById(R.id.title_et);
        downloadBtn = findViewById(R.id.download_btn);
        evaluationEt = findViewById(R.id.evaluation_et);
        commentsEt = findViewById(R.id.comment_et);
        saveBtn = findViewById(R.id.save_btn);
        saveSignBtn = findViewById(R.id.save_sign_btn);

        titleEt.setEnabled(false);
        String[] evalItems = {"Excellent", "Very Good", "Good", "Fair", "Poor"};
        adapterItems = new ArrayAdapter<String>(this,R.layout.dropdown_layout, evalItems);
        evaluationEt.setAdapter(adapterItems);

        fetchWeeklyDetails();

        recyclerView = findViewById(R.id.activities_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        activitiesList = new ArrayList<>();
        adapter = new ComActivityAdapter(this, activitiesList);
        recyclerView.setAdapter(adapter);

        fetchActivities();
    }

    private void setupListeners() {
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchWeeklyReport();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String evaluation = evaluationEt.getText().toString().trim();
                String comments = commentsEt.getText().toString().trim();
                String action = "save";
                if(evaluation.isEmpty()){
                    Toast.makeText(ComStudShowWeekly.this, "Please put evaluation", Toast.LENGTH_SHORT).show();
                }else{
                    saveWeekly(evaluation, comments, action);
                }

            }
        });
        saveSignBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String evaluation = evaluationEt.getText().toString().trim();
                String comments = commentsEt.getText().toString().trim();
                String action = "save_sign";
                if(evaluation.isEmpty()){
                    Toast.makeText(ComStudShowWeekly.this, "Please put evaluation", Toast.LENGTH_SHORT).show();
                }else{
                    saveWeekly(evaluation, comments, action);
                }
            }
        });
    }

    private void saveWeekly(String evaluation, String comments, String action) {
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Saving");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/company/save-weekly-report";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        // Parse the JSON response to get the message
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        fetchWeeklyDetails();
                        //request to refresh weekly list
                        sharedPreferences.edit().putBoolean("refreshWeeklyList", true).apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("weekly_id", weeklyID);
                params.put("weekly_evaluation", evaluation);
                params.put("weekly_comments", comments);
                params.put("action", action);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    private void fetchWeeklyReport() {
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Generating");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/get-weekly-report-data/" + weeklyID + "/" + studID;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);

                String coordinatorName = jsonObject.getString("coordinator");
                String departmentLogo = jsonObject.getString("dep_logo_url");
                String studFname = jsonObject.getString("firstname");
                String studLname = jsonObject.getString("lastname");
                String studMinitial = jsonObject.getString("middle_initial");
                String studName = studFname + " " + studMinitial + " " + studLname;
                String studCourse = jsonObject.getString("course");
                String studYear = jsonObject.getString("year");
                String parentGuardian = jsonObject.getString("parent_guardian");
                String courseAndYear = studCourse + " " + studYear;
                String comName = jsonObject.getString("company_name");
                String supervisor = jsonObject.getString("supervisor");
                String comDepartment = jsonObject.getString("department_assigned");

                String studSchoolYear = jsonObject.getString("school_year");

                // Fetching the single weekly report with date
                String title = jsonObject.getString("title");
                String evaluation = jsonObject.getString("evaluation");
                String supervisorComment = jsonObject.getString("supervisor_comment");
                String isSigned = jsonObject.getString("is_signed");
                String signedAt = jsonObject.getString("signed_at");
                String weeklyReportDate = jsonObject.getString("date");

                // Fetch activities data (subquery) using the same array structure as DTR
                JSONArray activitiesArray = jsonObject.getJSONArray("activities");
                List<String[]> activitiesData = new ArrayList<>();  // Changed to a List of String arrays

                for (int j = 0; j < activitiesArray.length(); j++) {
                    JSONObject activity = activitiesArray.getJSONObject(j);
                    String activityDate = activity.getString("date");
                    String activityDescription = activity.getString("activity");

                    String[] lines = activityDescription.split("\\n");
                    StringBuilder bulletedActivities = new StringBuilder();
                    String bullet = "\u2022"; // Unicode bullet character

                    for (String line : lines) {
                        bulletedActivities.append(bullet).append(" ").append(line).append("\n");
                    }

                    activitiesData.add(new String[]{activityDate, bulletedActivities.toString()}); // Added each activity separately
                }

                createPDF(departmentLogo, studSchoolYear, studName, comName, comDepartment, activitiesData, evaluation, supervisorComment, isSigned, weeklyReportDate, courseAndYear, signedAt, supervisor, coordinatorName, parentGuardian, title);
            } catch (JSONException e) {
                Toast.makeText(this, "Error Fetching Details", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    private void createPDF(String departmentLogo, String studSchoolYear, String studName, String comName, String comDepartment, List<String[]> activitiesData, String evaluation, String supervisorComment, String isSigned, String weeklyReportDate, String courseAndYear, String signedAt, String supervisor, String coordinatorName, String parentGuardian, String weeklyTitle) {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);

        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String currentDate = currentDateFormat.format(new Date());
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmm");

        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/"+ weeklyTitle + "_" + dateFormat.format(Calendar.getInstance().getTime()) + ".pdf";

        new Thread(() -> {
            try {
                Document document = new Document(new Rectangle(612, 936));
                PdfWriter.getInstance(document, new FileOutputStream(filePath));
                document.open();

                // Header and Blue Line
                PdfPTable headerTable = new PdfPTable(2);
                headerTable.setWidthPercentage(100);
                float[] headerWidths = {1, 1};
                headerTable.setWidths(headerWidths);

                Drawable leftDrawable = getResources().getDrawable(R.drawable.ucu_logo);
                Bitmap leftBitmap = ((BitmapDrawable) leftDrawable).getBitmap();
                ByteArrayOutputStream leftStream = new ByteArrayOutputStream();
                leftBitmap.compress(Bitmap.CompressFormat.PNG, 100, leftStream);
                Image leftLogo = Image.getInstance(leftStream.toByteArray());
                leftLogo.scaleAbsolute(200, 50);
                PdfPCell leftCell = new PdfPCell(leftLogo);
                leftCell.setBorder(Rectangle.NO_BORDER);
                leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                headerTable.addCell(leftCell);

                // Right Logo (Loaded from the network or placeholder)
                Image rightLogo;
                if (departmentLogo == null || departmentLogo.trim().isEmpty() || departmentLogo.equals("null")) {
                    Drawable placeholderDrawable = getResources().getDrawable(R.drawable.img_placeholder);
                    Bitmap placeholderBitmap = ((BitmapDrawable) placeholderDrawable).getBitmap();
                    ByteArrayOutputStream placeholderStream = new ByteArrayOutputStream();
                    placeholderBitmap.compress(Bitmap.CompressFormat.PNG, 100, placeholderStream);
                    rightLogo = Image.getInstance(placeholderStream.toByteArray());
                } else {
                    URL url = new URL(departmentLogo);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    rightLogo = Image.getInstance(stream.toByteArray());
                }
                rightLogo.scaleAbsolute(50, 50);
                PdfPCell rightCell = new PdfPCell(rightLogo);
                rightCell.setBorder(Rectangle.NO_BORDER);
                rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                headerTable.addCell(rightCell);

                // Add header to the document
                document.add(headerTable);

                document.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 2)));
                LineSeparator separator = new LineSeparator();
                separator.setLineColor(BaseColor.BLUE);
                separator.setLineWidth(2f);
                document.add(new Chunk(separator));
                document.add(new Paragraph("\n"));

                // Title and Subtitle
                Paragraph title = new Paragraph("INTERNSHIP/PRACTICUM WEEKLY REPORT", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                Paragraph subtitle = new Paragraph("2nd Semester " + studSchoolYear, new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
                subtitle.setAlignment(Element.ALIGN_CENTER);
                document.add(subtitle);

                document.add(new Paragraph("\n"));

                // Two Column Borderless Table
                PdfPTable infoTable = new PdfPTable(3);
                infoTable.setWidthPercentage(100);
                infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                infoTable.addCell(new Phrase("Name"));
                infoTable.addCell(new Phrase(": " + studName, boldFont));
                infoTable.addCell(new Phrase(" "));
                infoTable.addCell(new Phrase("Course"));
                infoTable.addCell(new Phrase(": " + courseAndYear, boldFont));
                infoTable.addCell(new Phrase(" "));
                infoTable.addCell(new Phrase("Company:"));
                infoTable.addCell(new Phrase(": " + comName, boldFont));
                infoTable.addCell(new Phrase(" "));
                infoTable.addCell(new Phrase("Department:"));
                infoTable.addCell(new Phrase(": " + (!comDepartment.equals("null") ? comDepartment : " "), boldFont));
                infoTable.addCell(new Phrase(" "));

                document.add(infoTable);
                document.add(new Paragraph("\n"));

                // Three Column Table with Shaded Header
                PdfPTable activityTable = new PdfPTable(3);
                activityTable.setWidthPercentage(100);

                PdfPCell dateCell = new PdfPCell(new Phrase("Date", boldFont));
                dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                dateCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                activityTable.addCell(dateCell);

                PdfPCell activityCell = new PdfPCell(new Phrase("Activities", boldFont));
                activityCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                activityCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                activityCell.setColspan(2);
                activityTable.addCell(activityCell);

                for (String[] activity : activitiesData) {
                    activityTable.addCell(new Phrase(activity[0]));  // Date
                    PdfPCell mergedActivity = new PdfPCell(new Phrase(activity[1]));  // Bulleted Description
                    mergedActivity.setColspan(2);  // Span 2 columns for description
                    activityTable.addCell(mergedActivity);
                }

                document.add(activityTable);
                document.add(new Paragraph("\n"));

                // Borderless Declaration Table
                PdfPTable declarationTable = new PdfPTable(3);
                declarationTable.setWidthPercentage(100);

                PdfPCell declarationCell = new PdfPCell(new Phrase("I hereby certify that the above statements are true and correct.", new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC)));
                declarationCell.setColspan(3);
                declarationCell.setBorder(Rectangle.NO_BORDER);
                declarationTable.addCell(declarationCell);

                PdfPCell nameCell = new PdfPCell(new Phrase("Name of Student:", boldFont));
                nameCell.setBorder(Rectangle.NO_BORDER);
                nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                nameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                nameCell.setColspan(1);
                nameCell.setRowspan(3);
                declarationTable.addCell(nameCell);

                PdfPCell signatureCell = new PdfPCell();
                signatureCell.setBorder(Rectangle.NO_BORDER);
                signatureCell.setColspan(2);
                signatureCell.setRowspan(3);
                PdfPTable innerSignatureTable = new PdfPTable(1);
                innerSignatureTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                PdfPCell signedCell = new PdfPCell(new Phrase("signed " + currentDate, smallFont));
                signedCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                signedCell.setBorder(Rectangle.NO_BORDER);
                innerSignatureTable.addCell(signedCell);
                PdfPCell nameBorderCell = new PdfPCell(new Phrase(studName, boldFont));
                nameBorderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                nameBorderCell.setBorder(Rectangle.BOTTOM);
                innerSignatureTable.addCell(nameBorderCell);
                PdfPCell sigLabelCell = new PdfPCell(new Phrase("(Student Signature over printed name)", smallFont));
                sigLabelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                sigLabelCell.setBorder(Rectangle.NO_BORDER);
                innerSignatureTable.addCell(sigLabelCell);
                signatureCell.addElement(innerSignatureTable);

                declarationTable.addCell(signatureCell);

                document.add(declarationTable);

                // Black Line
                LineSeparator blackLine = new LineSeparator();
                blackLine.setLineColor(BaseColor.BLACK);
                blackLine.setLineWidth(2f);
                document.add(new Chunk(blackLine));

                // Create a 5-column table
                PdfPTable fiveColumnTable = new PdfPTable(5);
                fiveColumnTable.setWidthPercentage(100);

                // Merge 5 columns for 'Supervisor'
                PdfPCell supervisorCell = new PdfPCell(new Phrase("Supervisor’s Evaluation:", boldFont));
                supervisorCell.setColspan(5);
                supervisorCell.setBorder(Rectangle.NO_BORDER);
                fiveColumnTable.addCell(supervisorCell);

                // Merge 5 columns for 'Please check'
                PdfPCell pleaseCheckCell = new PdfPCell(new Phrase("(Please check [/] to rate the student based on his/her weekly performance.)", new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC)));
                pleaseCheckCell.setColspan(5);
                pleaseCheckCell.setBorder(Rectangle.NO_BORDER);
                fiveColumnTable.addCell(pleaseCheckCell);

                // Create shaded rating columns
                PdfPCell excellentCell = new PdfPCell(new Phrase("Excellent"));
                excellentCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                excellentCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                fiveColumnTable.addCell(excellentCell);

                PdfPCell veryGoodCell = new PdfPCell(new Phrase("Very Good"));
                veryGoodCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                veryGoodCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                fiveColumnTable.addCell(veryGoodCell);

                PdfPCell goodCell = new PdfPCell(new Phrase("Good"));
                goodCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                goodCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                fiveColumnTable.addCell(goodCell);

                PdfPCell fairCell = new PdfPCell(new Phrase("Fair"));
                fairCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                fairCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                fiveColumnTable.addCell(fairCell);

                PdfPCell poorCell = new PdfPCell(new Phrase("Poor"));
                poorCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                poorCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                fiveColumnTable.addCell(poorCell);

                String checkSymbol = "\u2713";
                String[] ratingCategories = {"Excellent", "Very Good", "Good", "Fair", "Poor"};

                for (String category : ratingCategories) {
                    PdfPCell checkCell;

                    // If the category matches the evaluation, add a checkmark; otherwise, leave blank
                    if (category.equals(evaluation)) {
                        checkCell = new PdfPCell(new Phrase("/"));
                    } else {
                        checkCell = new PdfPCell(new Phrase(" "));  // Leave blank
                    }

                    checkCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    fiveColumnTable.addCell(checkCell);
                }

                // Merge 5 columns for 'Comments'
                PdfPCell commentsCell = new PdfPCell(new Phrase("Comments and Suggestions (Optional):", boldFont));
                commentsCell.setColspan(5);
                commentsCell.setBorder(Rectangle.NO_BORDER);
                fiveColumnTable.addCell(commentsCell);

                // Merge 5 columns for empty space
                PdfPCell emptyCommentsCell = new PdfPCell(new Phrase(" "));
                emptyCommentsCell.setColspan(5);
                emptyCommentsCell.setBorder(Rectangle.NO_BORDER);
                fiveColumnTable.addCell(emptyCommentsCell);

                // Merge 5 columns with bottom border
                PdfPCell bottomBorderCell1 = new PdfPCell(new Phrase(" "));
                bottomBorderCell1.setColspan(5);
                bottomBorderCell1.setBorder(Rectangle.BOTTOM);
                fiveColumnTable.addCell(bottomBorderCell1);

                PdfPCell bottomBorderCell2 = new PdfPCell(new Phrase(" "));
                bottomBorderCell2.setColspan(5);
                bottomBorderCell2.setBorder(Rectangle.BOTTOM);
                fiveColumnTable.addCell(bottomBorderCell2);

                // Add the table to the document
                document.add(fiveColumnTable);

                document.add(new Paragraph("\n"));

                // Add a 3-column borderless table
                PdfPTable threeColumnTable = new PdfPTable(3);
                threeColumnTable.setWidthPercentage(100);

                // Name row
                PdfPCell nameLabelCell = new PdfPCell(new Phrase("Name of Supervisor: "));
                nameLabelCell.setBorder(Rectangle.NO_BORDER);
                threeColumnTable.addCell(nameLabelCell);

                // Nested table for "sign", "Lenard Cordial", and "(Signature over Printed name)"
                PdfPTable nestedTable = new PdfPTable(1);
                nestedTable.setWidthPercentage(100);

                PdfPCell signCell = new PdfPCell(new Phrase(isSigned.equals("1") ? "signed" : " ", smallFont));
                signCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                signCell.setBorder(Rectangle.NO_BORDER);
                nestedTable.addCell(signCell);

                PdfPCell supCell = new PdfPCell(new Phrase(supervisor.toUpperCase(), boldFont));
                supCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                supCell.setBorder(Rectangle.BOTTOM);
                nestedTable.addCell(supCell);

                PdfPCell signatureNoteCell = new PdfPCell(new Phrase("(Signature over Printed name)", smallFont));
                signatureNoteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                signatureNoteCell.setBorder(Rectangle.NO_BORDER);
                nestedTable.addCell(signatureNoteCell);

                PdfPCell nestedTableCell = new PdfPCell(nestedTable);
                nestedTableCell.setBorder(Rectangle.NO_BORDER);
                threeColumnTable.addCell(nestedTableCell);

                PdfPCell emptyCell1 = new PdfPCell(new Phrase(" "));
                emptyCell1.setBorder(Rectangle.NO_BORDER);
                threeColumnTable.addCell(emptyCell1);

                // Position row
                PdfPCell positionLabelCell = new PdfPCell(new Phrase("Position: "));
                positionLabelCell.setBorder(Rectangle.NO_BORDER);
                threeColumnTable.addCell(positionLabelCell);

                PdfPCell positionValueCell = new PdfPCell(new Phrase("Instructor / OJT Supervisor"));
                positionValueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                positionValueCell.setBorder(Rectangle.NO_BORDER);
                threeColumnTable.addCell(positionValueCell);

                PdfPCell emptyCell2 = new PdfPCell(new Phrase(" "));
                emptyCell2.setBorder(Rectangle.NO_BORDER);
                threeColumnTable.addCell(emptyCell2);

                // Date row
                PdfPCell dateLabelCell = new PdfPCell(new Phrase("Date: "));
                dateLabelCell.setBorder(Rectangle.NO_BORDER);
                threeColumnTable.addCell(dateLabelCell);

                PdfPCell dateValueCell = new PdfPCell(new Phrase(isSigned.equals("1") ? signedAt : " "));
                dateValueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                dateValueCell.setBorder(Rectangle.BOTTOM);
                threeColumnTable.addCell(dateValueCell);

                PdfPCell emptyCell3 = new PdfPCell(new Phrase(" "));
                emptyCell3.setBorder(Rectangle.NO_BORDER);
                threeColumnTable.addCell(emptyCell3);

                // Add table to the document
                document.add(threeColumnTable);


                document.add(new Paragraph("\n"));

                PdfPTable twoColumnTable = new PdfPTable(7);
                twoColumnTable.setWidthPercentage(100);

                // Row 1: "sign1" and "sign2"
                PdfPCell sign1Cell = new PdfPCell(new Phrase("signed " + currentDate, smallFont));
                sign1Cell.setColspan(3);
                sign1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                sign1Cell.setBorder(Rectangle.NO_BORDER);
                twoColumnTable.addCell(sign1Cell);

                PdfPCell blankCell1 = new PdfPCell(new Phrase(" "));
                blankCell1.setBorder(Rectangle.NO_BORDER); // Blank column to complete the row
                twoColumnTable.addCell(blankCell1);

                PdfPCell sign2Cell = new PdfPCell(new Phrase(" "));
                sign2Cell.setColspan(3);
                sign2Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                sign2Cell.setBorder(Rectangle.NO_BORDER);
                twoColumnTable.addCell(sign2Cell);

                // Row 2: "name1" and "name2" with bottom border
                PdfPCell name1Cell = new PdfPCell(new Phrase(parentGuardian.toUpperCase(), boldFont));
                name1Cell.setColspan(3);
                name1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                name1Cell.setBorder(Rectangle.BOTTOM);
                twoColumnTable.addCell(name1Cell);

                PdfPCell blankCell2 = new PdfPCell(new Phrase(" "));
                blankCell2.setBorder(Rectangle.NO_BORDER); // Blank column to complete the row
                twoColumnTable.addCell(blankCell2);

                PdfPCell name2Cell = new PdfPCell(new Phrase(coordinatorName.toUpperCase(), boldFont));
                name2Cell.setColspan(3);
                name2Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                name2Cell.setBorder(Rectangle.BOTTOM);
                twoColumnTable.addCell(name2Cell);

                // Row 3: "parent" and "coordinator"
                PdfPCell parentCell = new PdfPCell(new Phrase("Signature over Printed of Parent/Guardian", smallFont));
                parentCell.setColspan(3);
                parentCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                parentCell.setBorder(Rectangle.NO_BORDER);
                twoColumnTable.addCell(parentCell);

                PdfPCell blankCell3 = new PdfPCell(new Phrase(" "));
                blankCell3.setBorder(Rectangle.NO_BORDER); // Blank column to complete the row
                twoColumnTable.addCell(blankCell3);

                PdfPCell coordinatorCell = new PdfPCell(new Phrase("Internship/Practicum Adviser/Date Received", smallFont));
                coordinatorCell.setColspan(3);
                coordinatorCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                coordinatorCell.setBorder(Rectangle.NO_BORDER);
                twoColumnTable.addCell(coordinatorCell);

                // Add table to the document
                document.add(twoColumnTable);


                document.close();
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "PDF saved at " + filePath, Toast.LENGTH_LONG).show());
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();

    }


    private void fetchWeeklyDetails() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/show-weekly/" + weeklyID;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);

                String title = obj.getString("title");
                String evaluation = obj.optString("evaluation");
                String comment = obj.optString("supervisor_comment");

                titleEt.setText(title);
                commentsEt.setText(comment.equals("null") ? "" : comment);
                evaluationEt.setText(evaluation.equals("null") ? "" : evaluation, false);
                evaluationEt.setAdapter(adapterItems);

            } catch (JSONException e) {
                Toast.makeText(this, "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Weekly Report", error.toString());
        });

        queue.add(request);
    }

    private void fetchActivities() {
        String url = Constants.API_BASE_URL + "/student/get-activities/" + weeklyID;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    activitiesList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String activityId = obj.getString("id");
                            String weeklyId = obj.getString("weekly_report_id");
                            String date = obj.getString("date");
                            String activity = obj.getString("activity");

                            activitiesList.add(new Activities(activityId, weeklyId, date, activity));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Failed to fetch activities", Toast.LENGTH_SHORT).show()
        );

        queue.add(jsonArrayRequest);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Volley.newRequestQueue(this).cancelAll(request -> true);
    }
}