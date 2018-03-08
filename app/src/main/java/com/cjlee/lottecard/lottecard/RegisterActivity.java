package com.cjlee.lottecard.lottecard;

import android.content.Context;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String EXCEL_TAG = "ExelLog";

    EditText editName, editTel, editRegiPlace;
    DatePicker dateRegi;
    Button btnRegi, btnPrev, btnExcelSave, btnExcelRead;

    URL url;
    HttpURLConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        /* Main Thread에서 네트워크 접속 가능하도록 설정*/
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        editName = findViewById(R.id.name);
        editTel = findViewById(R.id.tel);
        editRegiPlace = findViewById(R.id.regiPlace);
        dateRegi = findViewById(R.id.regiDate);
        btnRegi = findViewById(R.id.btnRegi);
        btnPrev = findViewById(R.id.btnPrev);
        btnExcelSave = findViewById(R.id.btnExcelSave);
        btnExcelRead = findViewById(R.id.btnExcelRead);

        editTel.addTextChangedListener(new PhoneNumberFormattingTextWatcher("KR"));

        ButtonHandler buttonHandler = new ButtonHandler();
        btnRegi.setOnClickListener(buttonHandler);
        btnPrev.setOnClickListener(buttonHandler);
        btnExcelSave.setOnClickListener(buttonHandler);
        btnExcelRead.setOnClickListener(buttonHandler);
    }

    /**
     *
     */
    class ButtonHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnRegi:
                    registerMember();
                    break;
                case R.id.btnPrev:
                    finish();
                    break;
                case R.id.btnExcelSave:
                    saveExcelFile(RegisterActivity.this, "myExcel.xls");
                    break;
                case R.id.btnExcelRead:
                    readExcelFile(RegisterActivity.this, "myExcel.xls");
                    break;
            }
        }
    }

    /**
     * 고객 정보를 데이터베이스에 넘겨준다
     * @return true when the data is stored in database well, false otherwise
     */
    private void registerMember() {
        // Parameters for query
        // name, telephone, registeredDate, registeredPlace, issuedDate
        // hasTmoney, hasAutopay, etcInfo
        String name = editName.getText().toString();
        String telephone = editTel.getText().toString();
        String registeredPlace = editRegiPlace.getText().toString();
        String registeredDate = dateRegi.getYear() + "/" + (dateRegi.getMonth() + 1) + "/" + dateRegi.getDayOfMonth();

        //Toast.makeText(this, "name:" + name, Toast.LENGTH_SHORT).show();

        HashMap<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("telephone", telephone);
        params.put("registeredPlace", registeredPlace);
        params.put("registeredDate", registeredDate);

        // 요청시 보낼 쿼리스트림으로 변환
        String param = makeParams(params);

        try {
            //서버의 IP주소, PORT번호, Context root, Request Mapping경로
            url = new URL("http://" + HomeActivity.IP_ADDRESS + ":8888/lottecard/member/insertMember");
        } catch (MalformedURLException e) {
            Toast.makeText(this,"잘못된 URL입니다.", Toast.LENGTH_SHORT).show();
        }

        try {
            connection = (HttpURLConnection) url.openConnection();

            if (connection != null) {

                connection.setConnectTimeout(10000);	//연결제한시간. 0은 무한대기.
                connection.setUseCaches(false);		//캐쉬 사용여부
                connection.setRequestMethod("POST"); // URL 요청에 대한 메소드 설정 : POST.
                connection.setRequestProperty("Accept-Charset", "UTF-8"); // Accept-Charset 설정.
                connection.setRequestProperty("Context_Type", "application/x-www-form-urlencoded;cahrset=UTF-8");

                OutputStream os = connection.getOutputStream();
                os.write(param.getBytes("UTF-8"));
                os.flush();
                os.close();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

                    String line;
                    String page = "";

                    while ((line = reader.readLine()) != null) {
                        page += line;
                    }

                    Toast.makeText(this, page, Toast.LENGTH_SHORT).show();

                }

            }
        } catch (Exception e){
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

    /**
     * Import list of data from the Excel file.
     * @param context context to be shown on
     * @param filename filename to be read
     */
    private void readExcelFile(Context context, String filename) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e(EXCEL_TAG, "Storage not available or read only");
            return;
        }

        try {
            // Creating Input Stream
            File file = new File(context.getExternalFilesDir(null), filename);
            FileInputStream myInput = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            /** We now need something to iterate through the cells. **/
            Iterator rowIter = mySheet.rowIterator();

            while (rowIter.hasNext()) {
                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator cellIter = myRow.cellIterator();

                while (cellIter.hasNext()) {
                    HSSFCell myCell = (HSSFCell) cellIter.next();
                    Log.d(EXCEL_TAG, "Cell Value: " + myCell.toString());
                    Toast.makeText(context, "cell Value: " + myCell.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
