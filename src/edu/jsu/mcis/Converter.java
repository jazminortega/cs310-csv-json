package edu.jsu.mcis;

import java.io.*;
import java.util.*;
import com.opencsv.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import java.sql.*;

public class Converter {
    
    /*
    
        Consider the following CSV data:
        
        "ID","Total","Assignment 1","Assignment 2","Exam 1"
        "111278","611","146","128","337"
        "111352","867","227","228","412"
        "111373","461","96","90","275"
        "111305","835","220","217","398"
        "111399","898","226","229","443"
        "111160","454","77","125","252"
        "111276","579","130","111","338"
        "111241","973","236","237","500"
        
        The corresponding JSON data would be similar to the following (tabs and
        other whitespace have been added for clarity).  Note the curly braces,
        square brackets, and double-quotes!  These indicate which values should
        be encoded as strings, and which values should be encoded as integers!
        
        {
            "colHeaders":["ID","Total","Assignment 1","Assignment 2","Exam 1"],
            "rowHeaders":["111278","111352","111373","111305","111399","111160",
            "111276","111241"],
            "data":[[611,146,128,337],
                    [867,227,228,412],
                    [461,96,90,275],
                    [835,220,217,398],
                    [898,226,229,443],
                    [454,77,125,252],
                    [579,130,111,338],
                    [973,236,237,500]
            ]
        }
    
        Your task for this program is to complete the two conversion methods in
        this class, "csvToJson()" and "jsonToCsv()", so that the CSV data shown
        above can be converted to JSON format, and vice-versa.  Both methods
        should return the converted data as strings, but the strings do not need
        to include the newlines and whitespace shown in the examples; again,
        this whitespace has been added only for clarity.
    
        NOTE: YOU SHOULD NOT WRITE ANY CODE WHICH MANUALLY COMPOSES THE OUTPUT
        STRINGS!!!  Leave ALL string conversion to the two data conversion
        libraries we have discussed, OpenCSV and json-simple.  See the "Data
        Exchange" lecture notes for more details, including example code.
    
    */
    
    @SuppressWarnings("unchecked")
    public static String csvToJson(String csvString) {
        
        String results = "";
        
        try {
            
            CSVReader reader = new CSVReader(new StringReader(csvString));
            List<String[]> full = reader.readAll();
            Iterator<String[]> iterator = full.iterator();
            String[] line;
            
            JSONObject json = new JSONObject();
            JSONArray colHeaders = new JSONArray();
            JSONArray rowHeaders = new JSONArray();
            JSONArray data = new JSONArray();
            JSONArray row;
            
            line = iterator.next();
            for(int i=0; i < line.length; i++)
                colHeaders.add(line[i]);
            
            while(iterator.hasNext())
            {
                line = iterator.next();
                row = new JSONArray();
                int score;
                for (int i=0; i<line.length; i++)
                {
                    if(i==0)
                        rowHeaders.add(line[i]);
                    else{
                        score = Integer.parseInt(line[i]);
                        row.add(score);
                    }
                }
                data.add(row);
            }
            
            json.put("colHeaders", colHeaders);
            json.put("rowHeaders", rowHeaders);
            json.put("data",data);
            
            results = json.toString();
            
        }        
        catch(Exception e) { return e.toString(); }
        
        return results.trim();
        
    }
    
    public static String jsonToCsv(String jsonString) {
        
        String results = "";
        
        try {

            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer, ',', '"', '\n');
            Iterator<String> iterator2;
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject)parser.parse(jsonString);
            JSONArray colHeaders = (JSONArray) json.get("colHeaders");
            JSONArray rowHeaders = (JSONArray) json.get("rowHeaders");
            JSONArray data = (JSONArray) json.get("data");
            
            String[] header = new String[colHeaders.size()];
            String[] rowHeader = new String[rowHeaders.size()];
            String[] num;
            
            iterator2 = colHeaders.iterator();
            for(int i=0; iterator2.hasNext(); i++)
                header[i] = iterator2.next();
            csvWriter.writeNext(header);
            
            iterator2 = rowHeaders.iterator();
            for(int i=0; iterator2.hasNext(); i++)
                rowHeader[i] = iterator2.next();
            
            String line[] = new String[header.length];
            
            for(int i=0; i<data.size(); i++)
            {
                JSONArray innerArray = (JSONArray) data.get(i);
                num = new String[innerArray.size()];
                for(int j=0; j<innerArray.size(); j++)
                {
                    String result = "";
                    result += innerArray.get(j).toString();
                    num[j] = result;
                }
                
                for(int m=0; m<line.length; m++)
                {
                    if(m == 0)
                        line[m] = rowHeader[i];
                    else{
                        int z;
                        for(z=1; z<num.length; z++)    
                            line[z] = num[z-1];
                        line[z] = num[z-1];
                    }
                        
                }
                csvWriter.writeNext(line);
            }
                
            results = writer.toString();
            
        }
        
        catch(Exception e) { return e.toString(); }
        
        return results.trim();
        
    }

    public static JSONArray getJSONData()throws ClassNotFoundException, InstantiationException {
        
        JSONArray results = null;
        Connection conn = null;
        PreparedStatement pstSelect = null, pstUpdate = null;
        ResultSet resultset = null;
        ResultSetMetaData metadata = null;
        
        String query, key, value;
        String[] headers;
        
        JSONArray records = new JSONArray();
        
        boolean hasresults;
        int resultCount, columnCount, updateCount = 0;
        
        try {
            
            /* Identify the Server */
            
            String server = ("jdbc:mysql://localhost/p2_test");
            String username = "root";
            String password = "CS310";
            System.out.println("Connecting to " + server + "...");
            
            /* Load the MySQL JDBC Driver */
            
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            
            /* Open Connection */

            conn = DriverManager.getConnection(server, username, password);

            /* Test Connection */
            
            if (conn.isValid(0)) {
                
                /* Prepare Select Query */
                
                query = "SELECT * FROM people";
                pstSelect = conn.prepareStatement(query);
                
                /* Execute Select Query */
           
                hasresults = pstSelect.execute();                
                
                /* Get Results */
                
                while ( hasresults || pstSelect.getUpdateCount() != -1 ) {

                    if ( hasresults ) {
                        
                        /* Get ResultSet Metadata */
                        
                        resultset = pstSelect.getResultSet();
                        metadata = resultset.getMetaData();
                        columnCount = metadata.getColumnCount();
                        
                        headers = new String[columnCount - 1];
  

                        for (int i = 0; i < headers.length; i++) {

                            headers[i] = metadata.getColumnLabel(i + 2);

                        }         
                        
                        /* Get Data; Print as Table Rows */
                       
                        LinkedHashMap data = new LinkedHashMap();
                       
                        while(resultset.next()) {
                            
                        
                            
                        /* Loop Through ResultSet Columns; Print Values */


                        data = new LinkedHashMap();

                            

                        for (int i = 0; i < headers.length; i++) {

                            value = resultset.getString(i + 2);
                            
                            if (resultset.wasNull()) {
                                 
                                data.put(headers[i], "");
                            }
                            else {

                                data.put(headers[i], value);

                            }
                        }    
                        
                        records.add(data);
                        
                        }
                    }    
                    else {

                        resultCount = pstSelect.getUpdateCount();  

                        if ( resultCount == -1 ) {
                            break;
                        }

                    }
                    
                    /* Check for More Data */

                    hasresults = pstSelect.getMoreResults();

                }
                
                results = records;
                
            }
            
            System.out.println();
            
            /* Close Database Connection */
            
            conn.close();
            
        }
        
        catch (Exception e) {
            System.err.println(e.toString());
        }
        
        /* Close Other Database Objects */
        
        finally {
            
            if (resultset != null) { try { resultset.close(); resultset = null; } catch (Exception e) {} }
            
            if (pstSelect != null) { try { pstSelect.close(); pstSelect = null; } catch (Exception e) {} }
            
            if (pstUpdate != null) { try { pstUpdate.close(); pstUpdate = null; } catch (Exception e) {} }
            
        }
        
        return results;
    }
    
}
