package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.example.trackticum.adapters.StudDtrAdapter;
import com.example.trackticum.models.StudDtr;
import com.example.trackticum.utils.Constants;
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
import com.itextpdf.text.html.WebColors;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ComShowDtr extends AppCompatActivity implements StudDtrAdapter.StudDtrVAction {

    private Toolbar toolbar;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;

    AutoCompleteTextView monthFilter;
    Button donwloadBTN;

    RecyclerView recyclerView;

    private StudDtrAdapter adapter;
    private List<StudDtr> dtrList;

    String studID, actionFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_com_show_dtr);
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
        toolbar = findViewById(R.id.com_show_dtr_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Daily Time Record");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        studID = getIntent().getStringExtra("stud_id");
        monthFilter = findViewById(R.id.month_filter);
        donwloadBTN = findViewById(R.id.download_btn);

        recyclerView = findViewById(R.id.dtr_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        dtrList = new ArrayList<>();
        adapter = new StudDtrAdapter(this, dtrList, this);
        recyclerView.setAdapter(adapter);

        fetchMonthFilter();

    }

    private void setupListeners() {
        donwloadBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(actionFilter != null){
                    fetchDetails();
                }else{
                    Toast.makeText(ComShowDtr.this, "Please Select Month", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void fetchDetails() {
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Generating");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/get-dtr-data/" + formatDate(actionFilter) + "/" + studID;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);

                String coordinatorName = jsonObject.getString("coordinator");
                String departmentLogo = jsonObject.getString("dep_logo_url");
                String studFname = jsonObject.getString("firstname");
                String studLname = jsonObject.getString("lastname");
                String studMinitial = jsonObject.getString("middle_initial") + ".";
                String studName = studFname + " " + studMinitial + " " + studLname;
                String studYear = jsonObject.getString("year");
                String comName = jsonObject.getString("company_name");
                String comAddress = jsonObject.getString("company_address");
                String comNameAndAddress = comName + "/" + comAddress;
                String supervisor = jsonObject.getString("supervisor");
                String comDepartment = jsonObject.getString("department_assigned");

                JSONArray dtrArray = jsonObject.getJSONArray("dtr_data");
                List<String[]> dtrData = new ArrayList<>();

                for (int i = 0; i < dtrArray.length(); i++) {
                    JSONObject dtr = dtrArray.getJSONObject(i);
                    String date = dtr.getString("formatted_date");
                    String amTimeIn = dtr.getString("am_time_in");
                    String amTimeOut = dtr.getString("am_time_out");
                    String pmTimeIn = dtr.getString("pm_time_in");
                    String pmTimeOut = dtr.getString("pm_time_out");
                    String totalHours = dtr.getString("total_hours");
                    String isSigned = dtr.getString("is_signed");

                    dtrData.add(new String[]{date, amTimeIn, amTimeOut, pmTimeIn, pmTimeOut, totalHours, isSigned});
                }

                createPDF(coordinatorName, departmentLogo, studName, studYear, comNameAndAddress, supervisor, comDepartment, dtrData);
            } catch (JSONException e) {
                Toast.makeText(this, "Error Fetching Details", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    public void createPDF(String coordinatorName, String departmentLogo, String studName, String studYear, String comNameAndAddress, String supervisor, String comDepartment, List<String[]> dtrData) {

        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);

        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String currentDate = currentDateFormat.format(new Date());
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmm");

        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/DTR_" + dateFormat.format(Calendar.getInstance().getTime()) + ".pdf";

        new Thread(() -> {
            try {
                // Prepare document and writer
                Document document = new Document(new Rectangle(612, 936));
                PdfWriter.getInstance(document, new FileOutputStream(filePath));
                document.open();

                // Prepare the header table
                PdfPTable headerTable = new PdfPTable(2);
                headerTable.setWidthPercentage(100);
                float[] headerWidths = {1, 1};
                headerTable.setWidths(headerWidths);

                // Left Logo (Loaded immediately)
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

                // === Continue PDF Generation ===
                document.add(new Paragraph(" ",  new Font(Font.FontFamily.HELVETICA, 2)));
                LineSeparator separator = new LineSeparator();
                separator.setLineColor(BaseColor.BLUE);
                separator.setLineWidth(2f);
                document.add(new Chunk(separator));
                document.add(new Paragraph("\n"));

                Paragraph title = new Paragraph("Daily Time Record/Internship Time Frame",
                        new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK));
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph("\n"));

                // Create Main Table
                PdfPTable table = new PdfPTable(9); // 9 columns
                table.setWidthPercentage(100);

                // Row 1: Name
                PdfPCell cell = new PdfPCell(new Phrase("NAME OF STUDENT-TRAINEE: " + studName));
                cell.setColspan(9);
                table.addCell(cell);

                // Row 2: Year
                cell = new PdfPCell(new Phrase("COURSE & YEAR: " + studYear));
                cell.setColspan(9);
                table.addCell(cell);

                // Row 3: Company
                cell = new PdfPCell(new Phrase("NAME OF COMPANY/ADDRESS: " + comNameAndAddress));
                cell.setColspan(9);
                table.addCell(cell);

                // Row 4: Department
                cell = new PdfPCell(new Phrase("DEPARTMENT ASSIGNED: " + comDepartment));
                cell.setColspan(9);
                table.addCell(cell);

                // Row 5: Month
                cell = new PdfPCell(new Phrase("MONTH OF: " + actionFilter, boldFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setColspan(9);
                table.addCell(cell);

                // Row 6: Headers (Blank, AM, PM, etc.)
                cell = new PdfPCell(new Phrase(""));
                cell.setColspan(2); // Blank
                table.addCell(cell);

                cell = new PdfPCell(new Phrase("AM"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setColspan(2);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase("PM"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setColspan(2);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase("NO. OF HOURS"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setColspan(1);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase("SIGNATURE OF SUPERVISOR"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setColspan(2);
                table.addCell(cell);

                // Row 7: Sub-Headers (Date/Day, Time In, Time Out, etc.)
                cell = new PdfPCell(new Phrase("DATE/DAY"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setColspan(2);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase("TIME-\nIN"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
                cell = new PdfPCell(new Phrase("TIME-\nOUT"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
                cell = new PdfPCell(new Phrase("TIME-\nIN"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
                cell = new PdfPCell(new Phrase("TIME-\nOUT"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(""));
                cell.setColspan(1); // Blank
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(""));
                cell.setColspan(2); // Blank
                table.addCell(cell);

                // Content Rows (Repeat Pattern)
                boolean allSigned = true;
                int totalNoHours = 0;
                for (String[] dtrRow : dtrData) {
                    // Date with colspan 2
                    cell = new PdfPCell(new Phrase(dtrRow[0])); // Date is the first element
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setColspan(2);
                    table.addCell(cell);

                    // AM Time In
                    cell = new PdfPCell(new Phrase(!dtrRow[1].equals("null") ? dtrRow[1] : ""));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);

                    // AM Time Out
                    cell = new PdfPCell(new Phrase(!dtrRow[2].equals("null") ? dtrRow[2] : ""));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);

                    // PM Time In
                    cell = new PdfPCell(new Phrase(!dtrRow[3].equals("null") ? dtrRow[3] : ""));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);

                    // PM Time Out
                    cell = new PdfPCell(new Phrase(!dtrRow[4].equals("null") ? dtrRow[4] : ""));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);

                    // Total Hours (already calculated)
                    totalNoHours += Integer.parseInt(dtrRow[5]);

                    cell = new PdfPCell(new Phrase(dtrRow[5]));  // Total hours is the last element
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);

                    if (!dtrRow[6].equals("1")) {
                        allSigned = false;
                    }
                    cell = new PdfPCell(new Phrase(!dtrRow[6].equals("0") ? "signed" : ""));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setColspan(2);
                    table.addCell(cell);
                }


                // Last Row: Totals
                cell = new PdfPCell(new Phrase("TOTAL NUMBER OF HOURS"));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setColspan(6);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(String.valueOf(totalNoHours)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setColspan(1);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(""));
                cell.setColspan(2);
                table.addCell(cell);

                // Add Table to Document
                document.add(table);

                PdfPTable footerTable = new PdfPTable(9); // 9 columns
                footerTable.setWidthPercentage(100);

                // Upper Left Section: Merge 4 rows and 4 columns
                PdfPCell upperLeftCell = new PdfPCell();
                upperLeftCell.setColspan(4);
                upperLeftCell.setRowspan(4);

                // Add a borderless 4-row, 1-column table inside this cell
                PdfPTable upperLeftTable = new PdfPTable(1);
                upperLeftTable.setWidthPercentage(100);

                // Add content for upperLeftCell
                PdfPCell innerCell = new PdfPCell(new Phrase("Prepared by:", boldFont));
                innerCell.setBorder(Rectangle.NO_BORDER);
                upperLeftTable.addCell(innerCell);

                innerCell = new PdfPCell(new Phrase(allSigned ? "signed " + currentDate : " ", smallFont));
                innerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerCell.setBorder(Rectangle.NO_BORDER);
                upperLeftTable.addCell(innerCell);

                innerCell = new PdfPCell(new Phrase(studName.toUpperCase()));
                innerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerCell.setBorder(Rectangle.BOTTOM);
                upperLeftTable.addCell(innerCell);

                innerCell = new PdfPCell(new Phrase("SIGNATURE OVER PRINTED NAME OF\nSTUDENT-TRAINEE", smallFont));
                innerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerCell.setBorder(Rectangle.NO_BORDER);
                upperLeftTable.addCell(innerCell);

                // Add inner table to upperLeftCell
                upperLeftCell.addElement(upperLeftTable);
                footerTable.addCell(upperLeftCell);

                // Right Section: Merge 8 rows and 5 columns
                PdfPCell fRightCell = new PdfPCell();
                fRightCell.setColspan(5);
                fRightCell.setRowspan(8);

                // Add a borderless table with data inside this cell
                PdfPTable innerRightTable = new PdfPTable(1);
                innerRightTable.setWidthPercentage(100);

                // Add content for fRightCell
                innerCell = new PdfPCell(new Phrase("Approved by:", boldFont));
                innerCell.setBorder(Rectangle.NO_BORDER);
                innerRightTable.addCell(innerCell);

                innerCell = new PdfPCell(new Phrase(" "));
                innerCell.setBorder(Rectangle.NO_BORDER);
                innerRightTable.addCell(innerCell);

                innerCell = new PdfPCell(new Phrase(allSigned ? "signed " + currentDate : " ", smallFont));
                innerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerCell.setBorder(Rectangle.NO_BORDER);
                innerRightTable.addCell(innerCell);

                innerCell = new PdfPCell(new Phrase(supervisor.toUpperCase()));
                innerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerCell.setBorder(Rectangle.BOTTOM);
                innerRightTable.addCell(innerCell);

                innerCell = new PdfPCell(new Phrase("SIGNATURE OVER PRINTED NAME OF SUPERVISOR/\nCOMPANY’S AUTHORIZED REPRESENTATIVE", smallFont));
                innerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerCell.setBorder(Rectangle.NO_BORDER);
                innerRightTable.addCell(innerCell);

                // Add inner table to fRightCell
                fRightCell.addElement(innerRightTable);
                footerTable.addCell(fRightCell);

                // Bottom Left Section: Merge 4 rows and 4 columns
                PdfPCell bottomLeftCell = new PdfPCell();
                bottomLeftCell.setColspan(4);
                bottomLeftCell.setRowspan(4);

                // Add a borderless 4-row, 1-column table inside this cell
                PdfPTable bottomLeftTable = new PdfPTable(1);
                bottomLeftTable.setWidthPercentage(100);

                // Add content for bottomLeftCell
                innerCell = new PdfPCell(new Phrase("Reviewed by:", boldFont));
                innerCell.setBorder(Rectangle.NO_BORDER);
                bottomLeftTable.addCell(innerCell);

                innerCell = new PdfPCell(new Phrase(" ", smallFont));
                innerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerCell.setBorder(Rectangle.NO_BORDER);
                bottomLeftTable.addCell(innerCell);

                innerCell = new PdfPCell(new Phrase(coordinatorName.toUpperCase()));
                innerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerCell.setBorder(Rectangle.BOTTOM); // Underline
                bottomLeftTable.addCell(innerCell);

                innerCell = new PdfPCell(new Phrase("SIGNATURE OVER PRINTED NAME OF COLLEGE \nOJT COORDINATOR", smallFont)); // Same footer text
                innerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerCell.setBorder(Rectangle.NO_BORDER);
                bottomLeftTable.addCell(innerCell);

                // Add inner table to bottomLeftCell
                bottomLeftCell.addElement(bottomLeftTable);
                footerTable.addCell(bottomLeftCell);

                // Add the continuation table to the document
                document.add(footerTable);

                // Close Document
                document.close();

                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "PDF saved at " + filePath, Toast.LENGTH_LONG).show());
                        progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        progressDialog.dismiss();
            }
        }).start();
    }

    private void fetchMonthFilter() {
        String url = Constants.API_BASE_URL + "/student/get-dtr-filter/" + studID;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response != null && response.length() > 0) {
                        List<String> monthsList = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                String recordMonth = obj.getString("record_month");
                                monthsList.add(recordMonth); // Add record_month to the list
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                            }
                        }

                        // Populate the AutoCompleteTextView
                        populateAutoCompleteTextView(monthsList);
                    } else {
                        Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonArrayRequest);
    }

    private void populateAutoCompleteTextView(List<String> monthsList) {
        monthFilter = findViewById(R.id.month_filter);

        // Set up the ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, monthsList);
        monthFilter.setAdapter(adapter);
        monthFilter.setThreshold(1); // Start showing suggestions after typing 1 character

        // Handle user selection
        monthFilter.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMonth = formatDate(parent.getItemAtPosition(position).toString());

            actionFilter = parent.getItemAtPosition(position).toString();
            fetchDtrList(selectedMonth);
        });
    }

    private void fetchDtrList(String selectedMonth) {
        String url = Constants.API_BASE_URL + "/student/get-dtr-list/" + selectedMonth + "/" + studID;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response != null && response.length() > 0) {
                        dtrList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);

                                String dtrId = obj.getString("id");
                                String studID = obj.getString("stud_id");
                                String date = obj.getString("date");
                                String amTimeIn = obj.getString("am_time_in");
                                String amTimeout = obj.getString("am_time_out");
                                String pmTimeIn = obj.getString("pm_time_in");
                                String pmTimeout = obj.getString("pm_time_out");
                                String isSigned = obj.getString("is_signed");

                                dtrList.add(new StudDtr(dtrId, studID, date, amTimeIn, amTimeout, pmTimeIn, pmTimeout, isSigned));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch interns", Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonArrayRequest);
    }

    @Override
    public void onViewDtr(String dtrId, String amTimeIn, String amTimeOut, String pmTimeIn, String pmTimeOut, String isSigned) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit DTR Times");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_dtr, null);
        builder.setView(dialogView);

        // Initialize Material TextInput fields
        TextInputEditText amIn = dialogView.findViewById(R.id.amTimeInInput);
        TextInputEditText amOut = dialogView.findViewById(R.id.amTimeOutInput);
        TextInputEditText pmIn = dialogView.findViewById(R.id.pmTimeInInput);
        TextInputEditText pmOut = dialogView.findViewById(R.id.pmTimeOutInput);

        // Pre-fill the fields with existing values
        amIn.setText(amTimeIn);
        amOut.setText(amTimeOut);
        pmIn.setText(pmTimeIn);
        pmOut.setText(pmTimeOut);

        // Set up TimePickerDialog with AM/PM selection in the dialog itself
        View.OnClickListener timeClickListener = v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);  // 24-hour format for initialization
            int minute = calendar.get(Calendar.MINUTE);

            // Using TimePickerDialog in 12-hour format with AM/PM selection
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfDay) -> {
                // Convert and display time in 12-hour format with AM/PM
                String amPm = (hourOfDay < 12) ? "AM" : "PM";
                int displayHour = (hourOfDay == 0) ? 12 : (hourOfDay > 12 ? hourOfDay - 12 : hourOfDay);

                // Set the formatted time back to the input field
                String time = String.format("%02d:%02d %s", displayHour, minuteOfDay, amPm);
                ((TextInputEditText) v).setText(time);
            }, hour, minute, false); // 'false' enables the AM/PM selection in the dialog

            timePickerDialog.show();
        };


        amIn.setOnClickListener(timeClickListener);
        amOut.setOnClickListener(timeClickListener);
        pmIn.setOnClickListener(timeClickListener);
        pmOut.setOnClickListener(timeClickListener);

        // Positive button to save
        builder.setPositiveButton("Save", (dialog, which) -> {
            String am_in = amIn.getText().toString().trim();
            String am_out = amOut.getText().toString().trim();
            String pm_in = pmIn.getText().toString().trim();
            String pm_out = pmOut.getText().toString().trim();
            saveDtr(dtrId, am_in, am_out, pm_in, pm_out, isSigned);
        });

        // Neutral button to save and sign
        builder.setNegativeButton("Save and Sign", (dialog, which) -> {
            String am_in = amIn.getText().toString().trim();
            String am_out = amOut.getText().toString().trim();
            String pm_in = pmIn.getText().toString().trim();
            String pm_out = pmOut.getText().toString().trim();
            saveDtr(dtrId, am_in, am_out, pm_in, pm_out, "1");
        });

        builder.setNeutralButton("Delete", (dialog, which) -> {
            deleteDtr(dtrId);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveDtr(String dtrId, String amIn, String amOut, String pmIn, String pmOut, String isSigned) {
        if (amIn.isEmpty() || amIn.equals("null") || amOut.isEmpty() || amOut.equals("null") ||
                pmIn.isEmpty() || pmIn.equals("null") || pmOut.isEmpty() || pmOut.equals("null")) {
            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setMessage("Please wait...");
            progressDialog.setTitle("Saving");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Constants.API_BASE_URL + "/student/update-dtr";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        progressDialog.dismiss();
                        try {
                            // Parse the JSON response to get the message
                            JSONObject jsonResponse = new JSONObject(response);
                            String message = jsonResponse.getString("message");
                            Toast.makeText(ComShowDtr.this, message, Toast.LENGTH_LONG).show();
                            fetchDtrList(formatDate(actionFilter));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ComShowDtr.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }, error -> {
                progressDialog.dismiss();
                Toast.makeText(ComShowDtr.this, "Failed to update", Toast.LENGTH_SHORT).show();
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("dtr_id", dtrId);
                    params.put("am_in", amIn);
                    params.put("am_out", amOut);
                    params.put("pm_in", pmIn);
                    params.put("pm_out", pmOut);
                    params.put("is_signed", isSigned);
                    return params;
                }
            };
            queue.add(stringRequest);
        }
    }

    private void deleteDtr(String dtrId) {
        progressDialog.setMessage("Deleting DTR...");
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/delete-dtr/" + dtrId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.getString("message");
                        Toast.makeText(ComShowDtr.this, message, Toast.LENGTH_LONG).show();
                        fetchDtrList(formatDate(actionFilter));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ComShowDtr.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(ComShowDtr.this, "Failed to delete DTR", Toast.LENGTH_SHORT).show();
                });

        queue.add(stringRequest);
    }


    private String formatDate(String inputDate) {
        // Define the input and output date formats
        SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH); // "January 2025"
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM-yyyy", Locale.ENGLISH); // "01-2025"

        try {
            // Parse the input string into a Date object
            Date date = inputFormat.parse(inputDate);
            // Format the Date object into the desired output string
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            // Return the input string in case of an error
            return inputDate;
        }
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

}