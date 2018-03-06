package com.cjlee.lottecard.lottecard;

import android.content.Context;
import android.os.Environment;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    EditText editName, editTel;
    DatePicker dateRegi;
    Button btnRegi, btnPrev, btnExcelSave, btnExcelRead;

    static String TAG = "ExelLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editName = findViewById(R.id.name);
        editTel = findViewById(R.id.tel);
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

    class ButtonHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnRegi:
                    String msg = "";
                    msg += "이름: " + editName.getText();
                    msg += ", 전화번호: " + editTel.getText();
                    msg += ", 접수일: " + dateRegi.getYear() + "/" + dateRegi.getMonth()+1 + "/" + dateRegi.getDayOfMonth();

                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btnPrev:
                    break;
                case R.id.btnExcelSave:
                    saveExcelFile(MainActivity.this, "myExcel.xls");
                    break;
                case R.id.btnExcelRead:
                    readExcelFile(MainActivity.this, "myExcel.xls");
                    break;
            }
        }
    }

    private boolean saveExcelFile(Context context, String fileName)
    {

        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
        {
            Log.e(TAG, "Storage not available or read only");
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

        try
        {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w("FileUtils", "Writing file" + file);
            success = true;
        }
        catch (IOException e)
        {
            Log.w("FileUtils", "Error writing " + file, e);
        }
        catch (Exception e)
        {
            Log.w("FileUtils", "Failed to save file", e);
        }
        finally
        {
            try
            {
                if (null != os)
                    os.close();
            }
            catch (Exception ex)
            {
            }
        }
        return success;
    }

    private void readExcelFile(Context context, String filename)
    {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
        {
            Log.e(TAG, "Storage not available or read only");
            return;
        }

        try
        {
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

            while (rowIter.hasNext())
            {
                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator cellIter = myRow.cellIterator();
                while (cellIter.hasNext())
                {
                    HSSFCell myCell = (HSSFCell) cellIter.next();
                    Log.d(TAG, "Cell Value: " + myCell.toString());
                    Toast.makeText(context, "cell Value: " + myCell.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return;
    }

    public boolean isExternalStorageReadOnly()
    {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState))
        {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageAvailable()
    {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState))
        {
            return true;
        }
        return false;
    }
}
