package com.cjlee.lottecard.lottecard;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MemberListActivity extends AppCompatActivity {

    private static final String TAG = MemberListActivity.class.getSimpleName();
    private static final String EXCEL_TAG = "Excel Log";

    TextView text;
    ListView listView;
    Button btnExcel, btnPrev, btnLastMonth, btnNextMonth;

    URL url;
    HttpURLConnection connection;

    ListViewAdapter adapter;

    Calendar calendar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memberlist);

        /* Main Thread에서 네트워크 접속 가능하도록 설정 */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        adapter = new ListViewAdapter() ;

        // 리스트뷰 참조 및 Adapter달기
        listView = (ListView) findViewById(R.id.memberlist_listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new ListItemHandler());

        /*// 첫 번째 아이템 추가.
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_account_box_black_36dp),
                "Box", "Account Box Black 36dp") ;*/

        // 현재 날짜 읽어서 명단을 보여준다
        calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        getMemberListByMonth(year, month);

        text = findViewById(R.id.memberlist_text);
        listView = findViewById(R.id.memberlist_listview);
        btnExcel = findViewById(R.id.memberlist_btn_excel);
        btnPrev = findViewById(R.id.memberlist_btn_prev);
        btnLastMonth = findViewById(R.id.memberlist_btn_lastmonth);
        btnNextMonth = findViewById(R.id.memberlist_btn_nextmonth);

        ButtonHandler buttonHandler = new ButtonHandler();
        btnExcel.setOnClickListener(buttonHandler);
        btnPrev.setOnClickListener(buttonHandler);
        btnLastMonth.setOnClickListener(buttonHandler);
        btnNextMonth.setOnClickListener(buttonHandler);
    }

    /**
     * ClickListener for buttons.
     */
    class ButtonHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.memberlist_btn_lastmonth:
                    calendar.add(Calendar.MONTH, -1);
                    String prevMonth = calendar.get(Calendar.YEAR) + "년" +
                            (calendar.get(Calendar.MONTH) + 1) + "월";
                    //SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                    //String from = format.format(calendar.getTime());
                    text.setText(prevMonth);
                    getMemberListByMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
                    adapter.notifyDataSetChanged();
                    break;

                case R.id.memberlist_btn_nextmonth:
                    calendar.add(Calendar.MONTH, 1);
                    String nextMonth = calendar.get(Calendar.YEAR) + "년" +
                            (calendar.get(Calendar.MONTH) + 1) + "월";
                    text.setText(nextMonth);
                    getMemberListByMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
                    adapter.notifyDataSetChanged();
                    break;

                case R.id.memberlist_btn_excel:
                    String fileName = "";
                    // TODO: filename
                    saveExcelFile(MemberListActivity.this, fileName);
                    break;

                case R.id.memberlist_btn_prev:
                    finish();
                    break;
            }
        }
    }

    /**
     * ClickListener for items in ListView.
     */
    class ListItemHandler implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        }
    }

    /**
     * Read list of member
     */
    private void getMemberListByMonth(int year, int month) {
        HashMap<String, String> params = new HashMap<>();
        params.put("year", "" + year);
        params.put("month", "" + month);

        String param = makeParams(params);

        try {
            url = new URL("http://" + HomeActivity.IP_ADDRESS + ":8888/lottecard/member/selectMemberByMonth?" + param);
        } catch (MalformedURLException e) {
            Toast.makeText(this,"잘못된 URL입니다.", Toast.LENGTH_SHORT).show();
        }

        try {
            connection = (HttpURLConnection) url.openConnection();

            if(connection != null){
                connection.setConnectTimeout(10000);	//연결제한시간. 0은 무한대기.
                connection.setUseCaches(false);		//캐쉬 사용여부
                connection.setRequestProperty("Accept-Charset", "UTF-8"); // Accept-Charset 설정.
                connection.setRequestProperty("Context_Type", "application/x-www-form-urlencoded;cahrset=UTF-8");

                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){

                    //Log.e(TAG, "contentType: " + connection.getContentType());
                    //Log.e(TAG, "resMsg: " + connection.getResponseMessage());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

                    String line;
                    String page = "";

                    while ((line = reader.readLine()) != null){
                        page += line;
                    }

                    //Log.e(TAG, "response: " + page);

                    jsonParse(page);
                } else {
                    Toast.makeText(this, "통신 실패", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "" + e.toString(), Toast.LENGTH_SHORT).show();
        } finally {
            if(connection != null){
                connection.disconnect();
            }
        }
    }

    /**
     * Change data to be sent to query string.
     * @param params HashMap data to be sent
     * @return query string formatted ?name1=value1&name2=value2
     */
    private String makeParams(HashMap<String,String> params){
        StringBuffer sbParam = new StringBuffer();
        String key = "";
        String value = "";
        boolean isAnd = false;

        for(Map.Entry<String,String> elem : params.entrySet()){
            key = elem.getKey();
            value = elem.getValue();

            if(isAnd){
                sbParam.append("&");
            }

            sbParam.append(key).append("=").append(value);

            if(!isAnd){
                if(params.size() >= 2){
                    isAnd = true;
                }
            }
        }

        return sbParam.toString();
    }

    /**
     * Parsing json data and then Add items into ListViewAdapter
     * @param page json data representing members' info
     */
    private void jsonParse(String page){
        JSONArray jarray = null;
        JSONObject item = null;

        try {
            jarray = new JSONArray(page);

            adapter.clearItems();

            for (int i = 0; i < jarray.length(); i++) {
                item = jarray.getJSONObject(i);
                adapter.addItem(
                        item.getString("name"), item.getString("telephone"), item.getString("registeredDate"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Export list of date to the Excel file.
     * @param context context to be shown on
     * @param fileName filename to be saved
     * @return true when the data is stored in database well, false otherwise
     */
    private boolean saveExcelFile(Context context, String fileName) {

        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e(EXCEL_TAG, "Storage not available or read only");
            return false;
        }

        boolean success = false;

        // New Workbook
        Workbook wb = new HSSFWorkbook();

        Cell c = null;

        // Cell style for header row
        CellStyle cs = wb.createCellStyle();

        // New Sheet
        Sheet sheet1 = null;
        // TODO: 고객의 접수일을 기준으로 시트 이름을 생성
        sheet1 = wb.createSheet("myOrder");

        // Generate column headings
        Row row = sheet1.createRow(0);

        c = row.createCell(0);
        c.setCellValue("이름");

        c = row.createCell(1);
        c.setCellValue("전화번호");

        c = row.createCell(2);
        c.setCellValue("접수일");

        // TODO: 고객 명단을 반복하면서 Row를 생성해 나간다.

        sheet1.setColumnWidth(0, (15 * 500));
        sheet1.setColumnWidth(1, (15 * 500));
        sheet1.setColumnWidth(2, (15 * 500));

        // Create a path where we will place our List of objects on external storage
        File file = new File(context.getExternalFilesDir(null), fileName);
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w("FileUtils", "Writing file" + file);
            success = true;
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return success;
    }

    private boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

}
