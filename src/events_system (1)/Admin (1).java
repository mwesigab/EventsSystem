/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package events_system;

import org.apache.poi.*;
import com.mysql.jdbc.Connection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toMap;
import javafx.util.converter.LocalDateTimeStringConverter;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

/**
 *
 * @author benjamin
 */
public class Admin extends javax.swing.JFrame {

    /**
     * Creates new form Admin
     */
    Events_Home home = new Events_Home();
    public Admin() {
        initComponents(); 
        displayAttendants();
        //displayCustomerData();
        displayCustomerSearchData(jTextFieldSearch.getText());
    }
    String custId=null;
    int pos=0;
    ArrayList<CustomerData> customerList;
    ArrayList<CustomerData> custList;
    
    public java.sql.Connection dbConnect(){
        java.sql.Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            try {
                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/inventory?useSSL=false", "root", "root");
            } catch (SQLException ex) {
                Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
            }
            return con;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
            return con;
        }
    }
    public void displayCustomerData(){
        ArrayList<CustomerData> list = home.getCustomerList();
        DefaultTableModel model = (DefaultTableModel)jTable_Customer2.getModel();
        
        //Clear the JTable 
        model.setRowCount(0);
        Object[] row = new Object[5];
        
        for(int i=0;i<list.size();i++){
            //row[0]=list.get(i).getId();
            row[0]=list.get(i).getCustId();
            row[1]=list.get(i).getCustName();
            row[2]=list.get(i).getTelephone();
            row[3]=list.get(i).getTransType();
            //System.out.println("Here"+list.get(i).getCustName());
            
           model.addRow(row);
        }
    }  
    
    public ArrayList getCustomerPaymentInfo(String cust_id){
        ArrayList<CustomerData> salesList = new ArrayList<CustomerData>();
        double amountDue=0;
        
        Main_Window win = new Main_Window();
        Connection conn = (Connection)win.dbConnect();
        String query ="SELECT id,custID,totalCost,amountPaid FROM salestable WHERE custID='"+cust_id+"'";
        Statement stmt;
        ResultSet rs;
        CustomerData data;
        
        try {
            stmt=conn.createStatement();
            rs=stmt.executeQuery(query);
            
            while(rs.next()){
                amountDue=(rs.getDouble("totalCost")-rs.getDouble("amountPaid"));
                data=new CustomerData(rs.getInt("id"),rs.getString("custID"),rs.getDouble("totalCost"),rs.getDouble("amountPaid"),amountDue);
                salesList.add(data);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return salesList;
    }
//Already used in Admin to display Customer List
    public ArrayList<CustomerData> getCustomerSearchList(String search){
            //double[] amountArray=null;
            //double amountDue=0;
            customerList = new ArrayList<>();
            Main_Window window = new Main_Window();
            Connection con = (Connection)window.dbConnect();
            
            String query = "SELECT * FROM customers WHERE refNum LIKE '"+search+"%' AND refNumStatus=0";
            //String query2="SELECT totalCost,amountPaid FROM salestable";
            Statement stmt;
            ResultSet rs;
            //ResultSet result;
        try {
                        
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            //result=stmt.executeQuery(query2);
            
            CustomerData customer;
            //amountArray=getCustomerPaymentInfo(custId);
           /* if(amountArray[0]>amountArray[1]){
                amountDue=amountArray[0]-amountArray[1];
            }else{
                amountDue=0;
            } */
            
            while(rs.next()){
                customer = new CustomerData(rs.getInt("id"),rs.getString("custId"),rs.getString("custName"),rs.getString("telephone"),rs.getString("transtype"),rs.getDate("hireDate"),rs.getString("refNum"));
                customerList.add(customer);
              //System.out.println(rs.getString("custName"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
        return customerList;
    }    
    
    public void displayCustomerSearchData(String search){
        ArrayList<CustomerData> list = getCustomerSearchList(search);
        ArrayList<CustomerData> salesList;
        String cust_id ="",refNum="";
        
        DefaultTableModel model = (DefaultTableModel)jTable_Customer2.getModel();
        
        //Clear the JTable 
        model.setRowCount(0);
        Object[] row = new Object[7];
        
        
        for(int i=0;i<list.size();i++){

            //row[0]=list.get(i).getId();
            row[0]=list.get(i).getCustId();
            cust_id=(String)row[0];
            row[1]=list.get(i).getRefNumber();
            refNum=(String)row[1];
            row[2]=list.get(i).getTransType();
            salesList=getCustomerPaymentInfo(cust_id);
            
            for(int j=0;j<salesList.size();j++){
                //System.out.println("Here"+list.get(i).getCustName()+" "+salesList.get(j).getAmountPaid());
                row[3]=salesList.get(j).getAmountDue();
                row[4]=salesList.get(j).getAmountPaid();
                row[5]=expenditurePerJob(refNum);
                row[6]=incomePerJob(refNum,cust_id);
            }
            
           model.addRow(row);
        }
        
    }

    
    //Check Input Fields
    
    public boolean checkInput(){
        if(
            jtxtFirstName.getText() == null ||
            jtxtLastName.getText() == null||
            jtxtPassword.getText() == null ||
            jtxtDate.getDate() == null ||
            jtxtUserName.getText()==null
          ){
            return false;
           }else{
            try{
               // Double.parseDouble(jtxtCost.getText());
                return true;
            }catch(Exception ex){
                return false;
            }
        }
    }
 
    
    public void showPerson(int index){
        jtxtFirstName.setText(getAttendantsList().get(index).firstName);
        jtxtLastName.setText(getAttendantsList().get(index).lastName);
        jtxtId.setText(Integer.toString(getAttendantsList().get(index).attendantID));
        jtxtUserName.setText(getAttendantsList().get(index).userName);
        jtxtPassword.setText(getAttendantsList().get(index).password);
       // jComboBox.setSelectedItem(getProductList().get(index).getCategory());
        
        
        Date addDate = null;
        try {
            addDate = new SimpleDateFormat("dd-MM-yyyy").parse(getAttendantsList().get(index).dateAdded);
            jtxtDate.setDate(addDate);
        } catch (ParseException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
        //jlblImage.setIcon(ResizeImage(null,getProductList().get(index).getImage()));
        
    }
    
    public double expenditurePerJob(String refNum){
        double expenditure=0;
       // jTextFieldTtlExp.setText("0");
        Connection con = (Connection)dbConnect();
        String query ="SELECT * FROM jobExpenses WHERE refNum='"+refNum+"' AND refNumStatus=0";
            Statement stmt;
            ResultSet rs;
        try {
           // SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
               /*
                data = new Data(rs.getDate("dateHired"),rs.getDouble("totalCost"));
                 if(date.equals(dateFormat.format(rs.getDate("date")))){
                    expenditure+=rs.getDouble("serviceCost");
                 }
                System.out.println(data.date+"  "+data.cost);
                */
               expenditure+=rs.getDouble("expenseCost");
            }
           // jTextFieldTtlExp.setText(Double.toString(expenditure));
            
        } catch (SQLException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return expenditure;
    }
    //this function returns all job expenses for each month
    public double getJobExpensesPerMonth(String month) throws ParseException{
        double expenditure=0;
        Date dbDate;
        LocalDate dateNow = LocalDate.now();        
            Connection con = (Connection)dbConnect();

            String query ="SELECT * FROM jobExpenses";
                Statement stmt;
                ResultSet rs;
            try {
                Date myDate = new SimpleDateFormat("MMMM",Locale.ENGLISH).parse(month);
                Calendar cal2 = Calendar.getInstance();
                cal2.setTime(myDate);
                
                stmt = con.createStatement();
                rs = stmt.executeQuery(query);
                while(rs.next()){
                    dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("dateRecorded"));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dbDate);
                    
                    if(cal.get(Calendar.YEAR)==dateNow.getYear() && cal.get(Calendar.MONTH)==cal2.get(Calendar.MONTH)){
                        expenditure+=rs.getDouble("expenseCost");
                       
                    }
                }
                
                // jTextFieldTtlExpMonthly.setText(Double.toString(expenditure));
            } catch (SQLException ex) {
                Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
            }    
        
               
        return expenditure;
    
    }
    
    //this returns the cost of all normal expenses per month
    public double getNormalExpensesPerMonth(String month) throws ParseException{
        double expenditure=0;
        Date dbDate;
        LocalDate dateNow = LocalDate.now();        
            Connection con = (Connection)dbConnect();

            String query ="SELECT * FROM normalExpenses";
                Statement stmt;
                ResultSet rs;
            try {
                Date myDate = new SimpleDateFormat("MMMM",Locale.ENGLISH).parse(month);
                Calendar cal2 = Calendar.getInstance();
                cal2.setTime(myDate);
                
                stmt = con.createStatement();
                rs = stmt.executeQuery(query);
                while(rs.next()){
                    dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("dateRecorded"));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dbDate);
                    
                    if(cal.get(Calendar.YEAR)==dateNow.getYear() && cal.get(Calendar.MONTH)==cal2.get(Calendar.MONTH)){
                        expenditure+=rs.getDouble("expenseCost");
                       
                    }
                }
                
                // jTextFieldTtlExpMonthly.setText(Double.toString(expenditure));
            } catch (SQLException ex) {
                Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
            }    
        
               
        return expenditure;   
    }
    public double expenditurePerMonth(String month) throws ParseException{
        double expenditure=0;

         jTextFieldTtlExpMonthly.setText("0");
         expenditure=getJobExpensesPerMonth(month)+getNormalExpensesPerMonth(month);
         jTextFieldTtlExpMonthly.setText(Double.toString(expenditure));
               
        return expenditure;
    }
    //Get JAnnual Job Expenditure for Selected Year
    public double getAnnualJobExpenditure(String year) throws ParseException{
        double expenditure=0;
          Date dbDate;
          
       // LocalDate dateNow = LocalDate.now();
            //jTextFieldTtlExpYear.setText("0");
            Connection con = (Connection) dbConnect();

            String query ="SELECT * FROM jobExpenses";
                Statement stmt;
                ResultSet rs;
            try {
                
                stmt = con.createStatement();
                rs = stmt.executeQuery(query);
                while(rs.next()){
                    dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("dateRecorded"));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dbDate);
                    
                    if(cal.get(Calendar.YEAR)==Integer.parseInt(year)){
                        expenditure+=rs.getDouble("expenseCost");
                        
                    }
                }
               // jTextFieldTtlExpYear.setText(Double.toString(expenditure));

            } catch (SQLException ex) {
                Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
            }        
               
        
        return expenditure;    
    }
    
    //Get Annual Normal Expenditure for Selected Year
    public double getAnnualNormalExpenditure(String year) throws ParseException{
        double expenditure=0;
          Date dbDate;
          
       // LocalDate dateNow = LocalDate.now();
            //jTextFieldTtlExpYear.setText("0");
            Connection con = (Connection) dbConnect();

            String query ="SELECT * FROM normalExpenses";
                Statement stmt;
                ResultSet rs;
            try {
                
                stmt = con.createStatement();
                rs = stmt.executeQuery(query);
                while(rs.next()){
                    dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("dateRecorded"));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dbDate);
                    
                    if(cal.get(Calendar.YEAR)==Integer.parseInt(year)){
                        expenditure+=rs.getDouble("expenseCost");
                        
                    }
                }
               // jTextFieldTtlExpYear.setText(Double.toString(expenditure));

            } catch (SQLException ex) {
                Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
            }        
               
        
        return expenditure;          
    }
    public double expenditurePerYear(String yr) throws ParseException{
        double expenditure=0;
          Date dbDate;
          
       // LocalDate dateNow = LocalDate.now();
            jTextFieldTtlExpYear.setText("0");
            expenditure=getAnnualJobExpenditure(yr)+getAnnualNormalExpenditure(yr);
            jTextFieldTtlExpYear.setText(Double.toString(expenditure));
            
        
        return expenditure;
    }
    public double revenueGeneratedPerYear(String yr){
        
        double revenue=0;
          Date dbDate;
          
       // LocalDate dateNow = LocalDate.now();
            jTextFieldTtlYearRev.setText("0");
            Connection con = (Connection) dbConnect();

            String query ="SELECT * FROM salestable";
                Statement stmt;
                ResultSet rs;
            try {
                
                stmt = con.createStatement();
                rs = stmt.executeQuery(query);
                while(rs.next()){
                    try {
                        dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("dateHired"));
                          Calendar cal = Calendar.getInstance();
                          cal.setTime(dbDate);
                    
                            if(cal.get(Calendar.YEAR)==Integer.parseInt(yr)){
                                revenue+=rs.getDouble("amountPaid");

                            }
                    } catch (ParseException ex) {
                        Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                jTextFieldTtlYearRev.setText(Double.toString(revenue));

            } catch (SQLException ex) {
                Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
            }        
               
        return revenue;
    }
    
    public double revenueGeneratedPerJob(String cust_id){
        //Data data=null;
        double revenue=0;
        //jTextFieldTtlJobRev.setText("0");
        Connection con = (Connection)dbConnect();
        
        String query ="SELECT amountPaid FROM salestable WHERE custID='"+cust_id+"'";
            Statement stmt;
            ResultSet rs;
        try {
           // SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                revenue=rs.getDouble("amountPaid");
            }
            //System.out.println("Amount "+revenue);
            //jTextFieldTtlJobRev.setText(Double.toString(revenue));
            
        } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
        
   return revenue;
    }
    
    public double revenueGeneratedPerMonth(String month) throws ParseException{
        
        Date dbDate;
        double revenue=0;
        LocalDate dateNow = LocalDate.now();
         jTextFieldTtlMonthlyRev.setText("0");
        
            Connection con = (Connection)dbConnect();

            String query ="SELECT amountPaid,dateHired FROM salestable";
                Statement stmt;
                ResultSet rs;
            try {
                Date myDate = new SimpleDateFormat("MMMM",Locale.ENGLISH).parse(month);
                Calendar cal2 = Calendar.getInstance();
                cal2.setTime(myDate);
                
                stmt = con.createStatement();
                rs = stmt.executeQuery(query);
                while(rs.next()){
                    dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("dateHired"));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dbDate);
                    
                    if(cal.get(Calendar.YEAR)==dateNow.getYear() && cal.get(Calendar.MONTH)==cal2.get(Calendar.MONTH)){
                        revenue+=rs.getDouble("amountPaid");
                       
                    }
                }
                
                 jTextFieldTtlMonthlyRev.setText(Double.toString(revenue));
            } catch (SQLException ex) {
                Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
            }    
        
        return revenue;
    }
    
    public double incomePerJob(String refNum, String cust_id){
        //int index=jCBJobNet.getSelectedIndex();
        double netIncome = revenueGeneratedPerJob(cust_id)-expenditurePerJob(refNum);
       // jTextFieldTtlNet.setText(Double.toString(netIncome));
       // System.out.println("Revenue "+revenueGeneratedPerJob(refNum));
        return netIncome;
    }
    
    public void incomePerMonth(String month) throws ParseException{
        double netIncome=revenueGeneratedPerMonth(month)-expenditurePerMonth(month);
        jTextFieldTtlMonthlyNet.setText(Double.toString(netIncome));
    
    }
    public void incomePerYear(String year) throws ParseException{
    double netIncome= revenueGeneratedPerYear(year)-expenditurePerYear(year);
    jTextFieldTtlYearNet.setText(Double.toString(netIncome));
    }

    
    public void showCustomerDetails(int index){
        //Set initial values for the items
        jTextFieldCost.setText("0");
        jTextFieldTentQty.setText("0");
        
        jTextFieldId.setText(customerList.get(index).getCustId());
        jTextFieldName.setText(customerList.get(index).getCustName());
        jTextFieldPhone.setText(customerList.get(index).getTelephone());
        //String transType=customerList.get(index).getTransType();

        String custId = jTextFieldId.getText();
        
        Connection con = (Connection)dbConnect();
           String query = "SELECT * FROM hired_items WHERE custId='"+custId+"'";
      
            PreparedStatement ps;
            ResultSet rs;
        try {
            
            ps = con.prepareStatement(query);
            rs=ps.executeQuery();   
            jComboHiredItems.removeAllItems();
          while(rs.next()){
                //System.out.println(rs.getString("id")+" "+rs.getString("")+" "+rs.getFloat(2)+" "+rs.getString(3)+" "+rs.getString(4));
                jComboHiredItems.addItem(rs.getString("item_name"));
          }
        } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    //Retrieve attendants data
    public ArrayList<Data> getAttendantsList(){
            
            ArrayList<Data> attendantsList = new ArrayList<>();
            java.sql.Connection con = dbConnect();
            
            String query = "SELECT * FROM attendants";
            Statement stmt;
            ResultSet rs;
        try {
                        
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            Data attendant;
            
            while(rs.next()){
                attendant = new Data(rs.getInt("attendantID"),rs.getString("userName"),rs.getString("password"),rs.getString("firstName"),rs.getString("lastName"),rs.getString("dateAdded"));
                attendantsList.add(attendant);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
        return attendantsList;
    }
    
//Display the retrieved data in a JTable
    public void displayAttendants(){
        ArrayList<Data> list = getAttendantsList();
        DefaultTableModel model = (DefaultTableModel)JTableAttendants.getModel();
        
        //Clear the JTable 
        model.setRowCount(0);
        Object[] row = new Object[5];
        
        for(int i=0;i<list.size();i++){
            //row[0]=list.get(i).getId();
            row[3]=list.get(i).lastName;
            row[2]=list.get(i).firstName;
            row[0]=list.get(i).userName;
            row[1]=list.get(i).password;
            row[4]=list.get(i).dateAdded;
            model.addRow(row);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldTtlJobRev = new javax.swing.JTextField();
        jTextFieldTtlMonthlyRev = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jCBMonthlyRev = new javax.swing.JComboBox<>();
        jBtnTtlJobSearch = new javax.swing.JButton();
        jTextFieldTtlYearRev = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jCBAnnualRev = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel45 = new javax.swing.JLabel();
        jCBRevReport = new javax.swing.JComboBox<>();
        jBtnReportRev = new javax.swing.JButton();
        jTextFieldJobRef = new javax.swing.JTextField();
        jCBJobRev = new javax.swing.JComboBox<>();
        jPanel5 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jCBMonthlyExp = new javax.swing.JComboBox<>();
        jLabel13 = new javax.swing.JLabel();
        jTextFieldTtlExpMonthly = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jCBAnnualExp = new javax.swing.JComboBox<>();
        jTextFieldTtlExpYear = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        jCBExpReport = new javax.swing.JComboBox<>();
        jBtnReportExp = new javax.swing.JButton();
        jChkJobExp = new javax.swing.JCheckBox();
        jChkNormExp = new javax.swing.JCheckBox();
        jCBJobExp = new javax.swing.JComboBox<>();
        jTextFieldTtlExp = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jTextFieldTtlMonthlyNet = new javax.swing.JTextField();
        jCBMonthlyNet = new javax.swing.JComboBox<>();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jCBAnnualNet = new javax.swing.JComboBox<>();
        jTextFieldTtlYearNet = new javax.swing.JTextField();
        jCBJobNet = new javax.swing.JComboBox<>();
        jTextFieldTtlNet = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        jtxtFirstName = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        jtxtLastName = new javax.swing.JTextField();
        jtxtUserName = new javax.swing.JTextField();
        jtxtPassword = new javax.swing.JTextField();
        jtxtDate = new com.toedter.calendar.JDateChooser();
        jLabel41 = new javax.swing.JLabel();
        Btn_insert = new javax.swing.JButton();
        Btn_Update = new javax.swing.JButton();
        Btn_delete = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        JTableAttendants = new javax.swing.JTable();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jtxtId = new javax.swing.JTextField();
        jLabel40 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable_Customer2 = new javax.swing.JTable();
        jTextFieldSearch = new javax.swing.JTextField();
        jBtnSearch = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jLabel51 = new javax.swing.JLabel();
        jTextFieldDamaged = new javax.swing.JTextField();
        jLabel52 = new javax.swing.JLabel();
        jTextFieldMissing = new javax.swing.JTextField();
        jLabel53 = new javax.swing.JLabel();
        jTextFieldRet = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jTextFieldTotal = new javax.swing.JTextField();
        jLabel35 = new javax.swing.JLabel();
        jTextFieldTentQty = new javax.swing.JTextField();
        jTextFieldCost = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jComboHiredItems = new javax.swing.JComboBox<>();
        jPanel7 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jTextFieldPhone = new javax.swing.JTextField();
        jTextFieldId = new javax.swing.JTextField();
        jTextFieldName = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(41, 47, 51));

        jTabbedPane1.setBackground(new java.awt.Color(41, 47, 57));
        jTabbedPane1.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N

        jPanel1.setBackground(new java.awt.Color(85, 172, 238));
        jPanel1.setBorder(new javax.swing.border.MatteBorder(null));
        jPanel1.setPreferredSize(new java.awt.Dimension(1331, 1000));

        jPanel4.setBackground(new java.awt.Color(85, 172, 238));
        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("REVENUE PER JOB");

        jLabel2.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel2.setText("Enter Referece No.");
        jLabel2.setPreferredSize(new java.awt.Dimension(110, 37));

        jLabel3.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel3.setText("MONTHLY REVENUE");

        jLabel5.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel5.setText("ANNUAL REVENUE");

        jTextFieldTtlJobRev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTtlJobRevActionPerformed(evt);
            }
        });

        jTextFieldTtlMonthlyRev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTtlMonthlyRevActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel7.setText("Select Month");
        jLabel7.setPreferredSize(new java.awt.Dimension(110, 37));

        jCBMonthlyRev.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        jCBMonthlyRev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBMonthlyRevActionPerformed(evt);
            }
        });

        jBtnTtlJobSearch.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jBtnTtlJobSearch.setText("Search");
        jBtnTtlJobSearch.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jBtnTtlJobSearch.setPreferredSize(new java.awt.Dimension(110, 37));
        jBtnTtlJobSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnTtlJobSearchActionPerformed(evt);
            }
        });

        jTextFieldTtlYearRev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTtlYearRevActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel8.setText("Select Year");

        jCBAnnualRev.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select--", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));
        jCBAnnualRev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBAnnualRevActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel4.setText("Total");

        jLabel6.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel6.setText("Total");
        jLabel6.setPreferredSize(new java.awt.Dimension(110, 37));

        jPanel12.setBackground(new java.awt.Color(85, 172, 238));
        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "REPORTS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18))); // NOI18N

        jLabel45.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel45.setText("Select Period");

        jCBRevReport.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Select--", "This Month", "Previous Month", "Last 5 months", "Annual Report" }));
        jCBRevReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBRevReportActionPerformed(evt);
            }
        });

        jBtnReportRev.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jBtnReportRev.setText("EXPORT TO EXCEL");
        jBtnReportRev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnReportRevActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel45, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jBtnReportRev, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCBRevReport, 0, 181, Short.MAX_VALUE))
                .addGap(41, 41, 41))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel45, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 7, Short.MAX_VALUE))
                    .addComponent(jCBRevReport))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jBtnReportRev, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jCBJobRev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBJobRevActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jTextFieldTtlJobRev, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCBJobRev, javax.swing.GroupLayout.Alignment.LEADING, 0, 181, Short.MAX_VALUE))
                                .addGap(0, 45, Short.MAX_VALUE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jTextFieldJobRef, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jBtnTtlJobSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jCBAnnualRev, 0, 185, Short.MAX_VALUE)
                            .addComponent(jTextFieldTtlYearRev))
                        .addGap(45, 45, 45))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jCBMonthlyRev, 0, 190, Short.MAX_VALUE)
                            .addComponent(jTextFieldTtlMonthlyRev))
                        .addGap(41, 41, 41)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jBtnTtlJobSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldJobRef)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addComponent(jCBJobRev, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldTtlJobRev, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCBMonthlyRev, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jTextFieldTtlMonthlyRev, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCBAnnualRev, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldTtlYearRev, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(85, 172, 238));
        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel9.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel9.setText("EXPENDITURE PER JOB");

        jLabel11.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel11.setText("MONTHLY EXPENDITURE");

        jLabel12.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel12.setText("Select Month");

        jCBMonthlyExp.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        jCBMonthlyExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBMonthlyExpActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel13.setText("Total");

        jTextFieldTtlExpMonthly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTtlExpMonthlyActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("ANNUAL EXPENDITURE");

        jLabel15.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel15.setText("Select Year");

        jCBAnnualExp.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select--", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));
        jCBAnnualExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBAnnualExpActionPerformed(evt);
            }
        });

        jTextFieldTtlExpYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTtlExpYearActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel16.setText("Total");

        jPanel13.setBackground(new java.awt.Color(85, 172, 238));
        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "REPORTS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18))); // NOI18N

        jLabel46.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel46.setText("Select Period");

        jCBExpReport.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Select--", "This Month", "Previous Month", "Last 5 months", "Annual Report" }));
        jCBExpReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBExpReportActionPerformed(evt);
            }
        });

        jBtnReportExp.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jBtnReportExp.setText("EXPORT TO EXCEL");
        jBtnReportExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnReportExpActionPerformed(evt);
            }
        });

        jChkJobExp.setText("Job Expenses");
        jChkJobExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChkJobExpActionPerformed(evt);
            }
        });

        jChkNormExp.setText("Normal Expenses");
        jChkNormExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChkNormExpActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel46, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jChkJobExp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jBtnReportExp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jCBExpReport, 0, 202, Short.MAX_VALUE))
                        .addGap(31, 31, 31))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jChkNormExp)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel46, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCBExpReport, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jChkJobExp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jChkNormExp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(jBtnReportExp, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jCBJobExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBJobExpActionPerformed(evt);
            }
        });

        jTextFieldTtlExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTtlExpActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(32, 32, 32))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jCBMonthlyExp, 0, 181, Short.MAX_VALUE)
                                    .addComponent(jTextFieldTtlExpMonthly))))
                        .addContainerGap(34, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jTextFieldTtlExp, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCBJobExp, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(71, 71, 71))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCBAnnualExp, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldTtlExpYear, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCBJobExp, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldTtlExp, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCBMonthlyExp)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldTtlExpMonthly, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCBAnnualExp, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldTtlExpYear, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel17.setFont(new java.awt.Font("Ubuntu", 1, 24)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("EXPENDITURE");
        jLabel17.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel18.setFont(new java.awt.Font("Ubuntu", 1, 24)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("REVENUE GENERATED");
        jLabel18.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel19.setFont(new java.awt.Font("Ubuntu", 1, 24)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("NET INCOME");
        jLabel19.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jPanel6.setBackground(new java.awt.Color(85, 172, 238));
        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel21.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel21.setText("NET INCOME PER JOB");

        jLabel22.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel22.setText("PER MONTH");

        jTextFieldTtlMonthlyNet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTtlMonthlyNetActionPerformed(evt);
            }
        });

        jCBMonthlyNet.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        jCBMonthlyNet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBMonthlyNetActionPerformed(evt);
            }
        });

        jLabel23.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel23.setText("Select Month");

        jLabel24.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel24.setText("Total");

        jLabel25.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel25.setText("PER YEAR");

        jCBAnnualNet.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select--", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));
        jCBAnnualNet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBAnnualNetActionPerformed(evt);
            }
        });

        jTextFieldTtlYearNet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTtlYearNetActionPerformed(evt);
            }
        });

        jCBJobNet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBJobNetActionPerformed(evt);
            }
        });

        jTextFieldTtlNet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTtlNetActionPerformed(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel20.setText("Total");

        jLabel10.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel10.setText("Select Year");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(140, 140, 140)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jTextFieldTtlNet, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCBJobNet, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jCBMonthlyNet, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextFieldTtlMonthlyNet, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCBAnnualNet, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldTtlYearNet, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCBJobNet, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldTtlNet, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCBMonthlyNet, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldTtlMonthlyNet, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(53, 53, 53)
                .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCBAnnualNet, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldTtlYearNet, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 26, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 18, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(674, 674, 674)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("INCOME AND EXPENDITURE SUMMARY", jPanel1);

        jPanel2.setBackground(new java.awt.Color(85, 172, 238));

        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel38.setText("First Name");

        jtxtFirstName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jLabel39.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel39.setText("Last Name");

        jtxtLastName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jtxtUserName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jtxtPassword.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jLabel41.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel41.setText("Date");

        Btn_insert.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Btn_insert.setText("Insert");
        Btn_insert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn_insertActionPerformed(evt);
            }
        });

        Btn_Update.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Btn_Update.setText("Update");
        Btn_Update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn_UpdateActionPerformed(evt);
            }
        });

        Btn_delete.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Btn_delete.setText("Delete");
        Btn_delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn_deleteActionPerformed(evt);
            }
        });

        JTableAttendants.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "LAST NAME", "FIRST NAME", "USERNAME", "PASSWORD", "DATE ADDED"
            }
        ));
        JTableAttendants.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JTableAttendantsMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(JTableAttendants);

        jLabel42.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel42.setText("Password");

        jLabel43.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel43.setText("UserName");

        jtxtId.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jLabel40.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel40.setText("Attendant ID");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(Btn_insert, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Btn_Update, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(Btn_delete, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(100, 100, 100)
                            .addComponent(jtxtId))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(100, 100, 100)
                            .addComponent(jtxtFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(102, 102, 102)
                            .addComponent(jtxtPassword))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(102, 102, 102)
                            .addComponent(jtxtDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(102, 102, 102)
                            .addComponent(jtxtUserName))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(100, 100, 100)
                            .addComponent(jtxtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 675, Short.MAX_VALUE)
                .addGap(61, 61, 61))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtId, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jtxtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtDate, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(61, 61, 61)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Btn_Update, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Btn_delete, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Btn_insert, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(179, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("ATTENDANT DETAILS", jPanel2);

        jPanel3.setBackground(new java.awt.Color(41, 47, 51));

        jPanel9.setBackground(new java.awt.Color(85, 172, 238));
        jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jTable_Customer2.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTable_Customer2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CUSTOMER ID", "REF NUMBER", "TRANS TYPE", "AMOUNT DUE", "AMOUNT PAID", "EXPENDITURE", "JOB INCOME"
            }
        ));
        jTable_Customer2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_Customer2MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable_Customer2);

        jTextFieldSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jTextFieldSearchMouseEntered(evt);
            }
        });
        jTextFieldSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldSearchActionPerformed(evt);
            }
        });

        jBtnSearch.setText("SEARCH");
        jBtnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jTextFieldSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jBtnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnSearch))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        jPanel10.setBackground(new java.awt.Color(85, 172, 238));
        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ITEM SUMMARY", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18))); // NOI18N

        jLabel51.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel51.setText("Damaged Items");

        jTextFieldDamaged.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDamagedActionPerformed(evt);
            }
        });

        jLabel52.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel52.setText("Missing Items");

        jTextFieldMissing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldMissingActionPerformed(evt);
            }
        });

        jLabel53.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel53.setText("Returned Items");

        jTextFieldRet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldRetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel53, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                    .addComponent(jLabel52, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel51, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jTextFieldDamaged, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                    .addComponent(jTextFieldMissing, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldRet, javax.swing.GroupLayout.Alignment.LEADING)))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldDamaged)
                    .addComponent(jLabel51, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldMissing)
                    .addComponent(jLabel52, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldRet)
                    .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel8.setBackground(new java.awt.Color(85, 172, 238));
        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "REVENUE ESTMATES", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18))); // NOI18N

        jTextFieldTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTotalActionPerformed(evt);
            }
        });

        jLabel35.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel35.setText("Total ");

        jLabel44.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel44.setText("Cost");

        jLabel48.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel48.setText("Item Name");

        jLabel49.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel49.setText("Quantity");

        jComboHiredItems.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboHiredItemsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel44, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel49, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE))
                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldTentQty)
                    .addComponent(jTextFieldCost)
                    .addComponent(jTextFieldTotal)
                    .addComponent(jComboHiredItems, 0, 156, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboHiredItems, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel49, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldTentQty))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel44, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldCost, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldTotal, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)))
        );

        jPanel7.setBackground(new java.awt.Color(85, 172, 238));
        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(null, "CUSTOMER DETAILS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18)), "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18))); // NOI18N

        jLabel29.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel29.setText("Name ");

        jLabel30.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel30.setText("Phone");

        jTextFieldPhone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldPhoneActionPerformed(evt);
            }
        });

        jTextFieldId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldIdActionPerformed(evt);
            }
        });

        jLabel32.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel32.setText("Customer ID");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldPhone, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                    .addComponent(jTextFieldName)
                    .addComponent(jTextFieldId))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldId))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(13, 13, 13))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jTextFieldName, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldPhone, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 344, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(46, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(71, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("JOB DETAILS", jPanel3);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1242, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 629, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("OTHERS", jPanel11);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 667, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldTtlJobRevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTtlJobRevActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTtlJobRevActionPerformed

    private void jTextFieldTtlMonthlyRevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTtlMonthlyRevActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTtlMonthlyRevActionPerformed

    private void jTextFieldTtlYearRevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTtlYearRevActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTtlYearRevActionPerformed

    private void jCBAnnualRevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBAnnualRevActionPerformed
        // TODO add your handling code here:
        revenueGeneratedPerYear(jCBAnnualRev.getSelectedItem().toString());
    }//GEN-LAST:event_jCBAnnualRevActionPerformed

    private void jCBMonthlyExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBMonthlyExpActionPerformed
        try {
            // TODO add your handling code here:
            expenditurePerMonth(jCBMonthlyExp.getSelectedItem().toString());
        } catch (ParseException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jCBMonthlyExpActionPerformed

    private void jTextFieldTtlExpMonthlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTtlExpMonthlyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTtlExpMonthlyActionPerformed

    private void jCBAnnualExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBAnnualExpActionPerformed
        try {
            // TODO add your handling code here:
            expenditurePerYear(jCBAnnualExp.getSelectedItem().toString());
        } catch (ParseException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jCBAnnualExpActionPerformed

    private void jTextFieldTtlExpYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTtlExpYearActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTtlExpYearActionPerformed

    private void jTextFieldTtlMonthlyNetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTtlMonthlyNetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTtlMonthlyNetActionPerformed

    private void jCBAnnualNetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBAnnualNetActionPerformed
        try {
            // TODO add your handling code here:
            incomePerYear(jCBAnnualNet.getSelectedItem().toString());
        } catch (ParseException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jCBAnnualNetActionPerformed

    private void jTextFieldTtlYearNetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTtlYearNetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTtlYearNetActionPerformed

    private void jCBMonthlyRevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBMonthlyRevActionPerformed
        try {
            // TODO add your handling code here:
            //SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            //String date = dateFormat.format(jTextFieldDateRev.getDate());
            revenueGeneratedPerMonth(jCBMonthlyRev.getSelectedItem().toString());
            //jTextFieldTtlMonthlyRev.setText("I am coming Hehe");
        } catch (ParseException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jCBMonthlyRevActionPerformed

    private void jCBMonthlyNetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBMonthlyNetActionPerformed
        try {
            // TODO add your handling code here:
            incomePerMonth(jCBMonthlyNet.getSelectedItem().toString());
        } catch (ParseException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jCBMonthlyNetActionPerformed

    private void Btn_insertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_insertActionPerformed
        if(checkInput() !=false){
            try {
                Connection con = (Connection) dbConnect();
                PreparedStatement ps = con.prepareStatement("INSERT INTO attendants (userName,password,firstName,LastName,dateAdded) VALUES  (?,?,?,?,?)");

                ps.setString(1, jtxtUserName.getText());
                ps.setString(2,jtxtPassword.getText());

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String addDate = dateFormat.format(jtxtDate.getDate());

                ps.setString(3,jtxtFirstName.getText());
                ps.setString(4, jtxtLastName.getText());
                ps.setString(5, addDate);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(null, "Attendant details have been recorded successfully");
                displayAttendants();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
                displayAttendants();
            }
        }else{
            JOptionPane.showMessageDialog(null, "One or More Fields Are Empty!");
            displayAttendants();
        }
    }//GEN-LAST:event_Btn_insertActionPerformed

    private void Btn_UpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_UpdateActionPerformed
        if(checkInput() != false){
            String UpdateQuery = null;
            PreparedStatement ps = null;
            Connection con = (Connection)dbConnect();

                try {
                    UpdateQuery = "UPDATE attendants SET userName=?,password=?,firstName=?,lastName=?,dateAdded=? WHERE attendantID =? ";
                    ps = con.prepareStatement(UpdateQuery);
                    
                    ps.setString(1,jtxtUserName.getText());
                    ps.setString(2,jtxtPassword.getText());
                    ps.setString(3,jtxtFirstName.getText());
                    ps.setString(4,jtxtLastName.getText());

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    String addDate = dateFormat.format(jtxtDate.getDate()); //Remember

                    ps.setString(5,addDate);
                    ps.setString(6,jtxtId.getText());
                    
                    // ps.setInt(4,Integer.parseInt(txt_name.getText()));

                    //Execute the query
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Data Update Successful");
                    displayAttendants();
                } catch (SQLException ex) {
                    Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
                    displayAttendants();
                }

        }else{
            JOptionPane.showMessageDialog(null, "One or More Fields Are Empty");
            displayAttendants();
        }
    }//GEN-LAST:event_Btn_UpdateActionPerformed

    private void Btn_deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_deleteActionPerformed
        if(!jtxtId.getText().equals("")){
            try {
                Connection con = (Connection)dbConnect();
                String sql = "DELETE FROM attendants WHERE attendantID=?";
                PreparedStatement ps = con.prepareStatement(sql);
                int id = Integer.parseInt(jtxtId.getText());
                ps.setInt(1, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(null, "Attendant deleted form the Database");
                displayAttendants();
            } catch (SQLException ex) {
                Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Attendant Deletion Failed: Please Enter the Correct Attendant ID");
                displayAttendants();
            }
        }else{
            JOptionPane.showMessageDialog(null,"Please enter an ID of the attendant you wish to Delete");
            displayAttendants();
        }
    }//GEN-LAST:event_Btn_deleteActionPerformed

    private void JTableAttendantsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_JTableAttendantsMouseClicked
        int index = JTableAttendants.getSelectedRow();
        showPerson(index);
    }//GEN-LAST:event_JTableAttendantsMouseClicked

    private void jCBRevReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBRevReportActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCBRevReportActionPerformed

    private void jCBExpReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBExpReportActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCBExpReportActionPerformed

    public void showItemInfo(String item_name,String cust_id){
        Connection con = (Connection) dbConnect();
        String query = "SELECT * FROM hired_items WHERE item_name ='"+item_name+"' AND custId='"+cust_id+"'";
        Statement stmt;
        ResultSet rs;
        
        try {
            stmt = con.createStatement();
            rs=stmt.executeQuery(query);
            
            while(rs.next()){
               // System.out.println("HEre "+Double.toString(rs.getInt("item_number")*rs.getDouble("cost")));
                jTextFieldTentQty.setText(Integer.toString(rs.getInt("item_number")));
                jTextFieldCost.setText(Double.toString(rs.getDouble("cost")));
                jTextFieldTotal.setText(Double.toString(rs.getInt("item_number")*rs.getDouble("cost")));
                jTextFieldRet.setText(Integer.toString(rs.getInt("returned_items")));
                jTextFieldDamaged.setText(Integer.toString(rs.getInt("damaged_items")));
                jTextFieldMissing.setText(Integer.toString(rs.getInt("missing_items")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void jCBJobRevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBJobRevActionPerformed
        // TODO add your handling code here:
        int index=jCBJobRev.getSelectedIndex();
        //System.out.println(index);
        //System.out.println(custList.get(index).getCustId());
        try {
            double temp = revenueGeneratedPerJob(custList.get(index).getCustId());
            jTextFieldTtlJobRev.setText(Double.toString(temp));            
        } catch (Exception e) {
        }
        
        
    }//GEN-LAST:event_jCBJobRevActionPerformed

    private void jBtnTtlJobSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnTtlJobSearchActionPerformed
        jCBJobRev.removeAllItems();
        jCBJobExp.removeAllItems();
        jCBJobNet.removeAllItems();
        searchByCustRefNum(jTextFieldJobRef.getText());
    }//GEN-LAST:event_jBtnTtlJobSearchActionPerformed

    private void jCBJobExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBJobExpActionPerformed
        // TODO add your handling code here:
        int index=jCBJobExp.getSelectedIndex();
        try {
            double exp = expenditurePerJob(custList.get(index).getRefNumber());
            jTextFieldTtlExp.setText(Double.toString(exp));            
        } catch (Exception e) {
        }
        
    }//GEN-LAST:event_jCBJobExpActionPerformed

    private void jTextFieldTtlExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTtlExpActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTtlExpActionPerformed

    private void jCBJobNetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBJobNetActionPerformed
        // TODO add your handling code here
        int index=jCBJobNet.getSelectedIndex();
        try {
            double income = incomePerJob(custList.get(index).getRefNumber(),custList.get(index).getCustId());
            jTextFieldTtlNet.setText(Double.toString(income));
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jCBJobNetActionPerformed

    private void jTextFieldTtlNetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTtlNetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTtlNetActionPerformed

    private void jTextFieldRetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldRetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldRetActionPerformed

    private void jTextFieldMissingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldMissingActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMissingActionPerformed

    private void jTextFieldDamagedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDamagedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldDamagedActionPerformed

    private void jComboHiredItemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboHiredItemsActionPerformed
        // TODO add your handling code here:
        try {
            //System.out.println(jComboHiredItems.getSelectedItem().toString()+"  "+jTextFieldId.getText());
           showItemInfo(jComboHiredItems.getSelectedItem().toString(),jTextFieldId.getText());            
        } catch (Exception e) {
            //System.out.println(e);
        }

    }//GEN-LAST:event_jComboHiredItemsActionPerformed

    private void jTextFieldTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTotalActionPerformed

    private void jTextFieldIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldIdActionPerformed

    private void jTextFieldPhoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldPhoneActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldPhoneActionPerformed

    private void jBtnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSearchActionPerformed
        // TODO add your handling code here:
        displayCustomerSearchData(jTextFieldSearch.getText());
    }//GEN-LAST:event_jBtnSearchActionPerformed

    private void jTextFieldSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldSearchActionPerformed

    private void jTable_Customer2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable_Customer2MouseClicked
        
        int index = jTable_Customer2.getSelectedRow();
        showCustomerDetails(index);
    }//GEN-LAST:event_jTable_Customer2MouseClicked

    private void jBtnReportRevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnReportRevActionPerformed
        try {
            // TODO add your handling code here:
            //ExportExcel excel = new ExportExcel();
            //excel.setVisible(true);
            exportRevenueReportToExcel(jCBRevReport.getSelectedIndex());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jBtnReportRevActionPerformed

    private void jBtnReportExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnReportExpActionPerformed
        try {
            // TODO add your handling code here:
            exportExpenditureReport(jCBExpReport.getSelectedIndex());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jBtnReportExpActionPerformed

    private void jChkJobExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChkJobExpActionPerformed
        // TODO add your handling code here:
        if(jChkJobExp.isSelected()){
            jChkNormExp.setEnabled(false);
        }else{
            jChkNormExp.setEnabled(true);
        }
    }//GEN-LAST:event_jChkJobExpActionPerformed

    private void jChkNormExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChkNormExpActionPerformed
        // TODO add your handling code here:
        if(jChkNormExp.isSelected()){
            jChkJobExp.setEnabled(false);
        }else{
            jChkJobExp.setEnabled(true);
        }
    }//GEN-LAST:event_jChkNormExpActionPerformed

    private void jTextFieldSearchMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextFieldSearchMouseEntered
        // TODO add your handling code here:
        jTextFieldSearch.setToolTipText("Search By Reference Number");
    }//GEN-LAST:event_jTextFieldSearchMouseEntered
    
    public void exportExpenditureReport(int period) throws FileNotFoundException{
    Calendar now = Calendar.getInstance();
        int month=0;
        int year=0;
        String query=null,tableIdf=null;
        
        
        //Create a blank workbook
        XSSFWorkbook workBook = new XSSFWorkbook();
        ArrayList<Data>list = new ArrayList<>();
        Main_Window window = new Main_Window();
        Connection con = (Connection)window.dbConnect();
        Data expenses;
        if(jChkJobExp.isSelected()){
            query = "SELECT * FROM jobExpenses";
            tableIdf="job";
        }else if(jChkNormExp.isSelected()){
            query = "SELECT * FROM normalExpenses";
            tableIdf="normal";
        }else{
        JOptionPane.showMessageDialog(null, "Please Select one of the Expense Types");
            return;
        }
        
        //String query2="SELECT totalCost,amountPaid FROM salestable";
        Statement stmt;
        ResultSet rs;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            Date dbDate;
      
                //System.out.println("Now "+period);
                if(period==1){
                   list.clear(); //Very imporant
                    month=now.get(Calendar.MONTH)+1;
                    year=now.get(Calendar.YEAR);
                    
                    //System.out.println("DB MONTH "+cal.get(Calendar.MONTH)+"AND MY MONTH "+month);
                    while(rs.next()){
                        dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("dateRecorded"));
                        //System.out.println("Date DB "+dbDate);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dbDate);
                        //System.out.println("DB MONTH "+cal.get(Calendar.MONTH)+"AND MY MONTH "+month);
                        if(cal.get(Calendar.MONTH)+1==month && cal.get(Calendar.YEAR)==year){
                            if(tableIdf.equals("job")){
                                expenses=new Data(rs.getString("refNum"),rs.getString("expenseName"),rs.getDouble("expenseCost"),rs.getString("dateRecorded"));
                                list.add(expenses);
                            }else if(tableIdf.equals("normal")){
                                expenses=new Data(rs.getString("expenseType"),rs.getString("expenseName"),rs.getDouble("expenseCost"),rs.getString("dateRecorded"));
                                list.add(expenses);                            
                            }else{
                                return;
                            }
                        }
                    }                                      
                    
                    
                }else if(period==2){
                    list.clear();
                    month=now.get(Calendar.MONTH);
                    year=now.get(Calendar.YEAR);
                    
                    while(rs.next()){
                        dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("dateRecorded"));
                        //System.out.println("Date DB "+dbDate);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dbDate);
                        //System.out.println("DB MONTH "+cal.get(Calendar.MONTH)+"AND MY MONTH "+month);
                        if(cal.get(Calendar.MONTH)+1==month && cal.get(Calendar.YEAR)==year){
                            if(tableIdf.equals("job")){
                                expenses=new Data(rs.getString("refNum"),rs.getString("expenseName"),rs.getDouble("expenseCost"),rs.getString("dateRecorded"));
                                list.add(expenses);
                            }else if(tableIdf.equals("normal")){
                                expenses=new Data(rs.getString("expenseType"),rs.getString("expenseName"),rs.getDouble("expenseCost"),rs.getString("dateRecorded"));
                                list.add(expenses);                            
                            }else{
                                return;
                            }
                        }
                    }
                    
                }else if(period==3){
                    list.clear();
                    //Fifth month from the indexed month
                    month=now.get(Calendar.MONTH)-4;
                    year=now.get(Calendar.YEAR);
                    int dbMonth=0;
                    int dbYear=0;
                    int currentMonth=now.get(Calendar.MONTH)+1;
                    
                    while(rs.next()){
                        dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("dateRecorded"));
                        //System.out.println("Date DB "+dbDate);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dbDate);
                        
                        dbMonth=cal.get(Calendar.MONTH)+1;
                        dbYear=cal.get(Calendar.YEAR);
                        
                        //System.out.println("DB MONTH "+cal.get(Calendar.MONTH)+"AND MY MONTH "+month);
                        if((month<=dbMonth && dbMonth<=currentMonth) && dbYear==year){
                            if(tableIdf.equals("job")){
                                expenses=new Data(rs.getString("refNum"),rs.getString("expenseName"),rs.getDouble("expenseCost"),rs.getString("dateRecorded"));
                                list.add(expenses);
                            }else if(tableIdf.equals("normal")){
                                expenses=new Data(rs.getString("expenseType"),rs.getString("expenseName"),rs.getDouble("expenseCost"),rs.getString("dateRecorded"));
                                list.add(expenses);                            
                            }else{
                                return;
                            }
                        }
                    }
                }else if(period==4){
                    list.clear();
                    year=now.get(Calendar.YEAR);
                    while(rs.next()){
                        dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("dateRecorded"));
                        //System.out.println("Date DB "+dbDate);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dbDate);
                        //System.out.println("DB MONTH "+cal.get(Calendar.MONTH)+"AND MY MONTH "+month);
                        if(cal.get(Calendar.YEAR)==year){
                            if(tableIdf.equals("job")){
                                expenses=new Data(rs.getString("refNum"),rs.getString("expenseName"),rs.getDouble("expenseCost"),rs.getString("dateRecorded"));
                                list.add(expenses);
                            }else if(tableIdf.equals("normal")){
                                expenses=new Data(rs.getString("expenseType"),rs.getString("expenseName"),rs.getDouble("expenseCost"),rs.getString("dateRecorded"));
                                list.add(expenses);                            
                            }else{
                                return;
                            }
                        }
                    }
                }else{
                    return;
                }
                          
            
            
        } catch (Exception e) {
        }
        
       
        if(tableIdf.equals("job")){
             //Create a array of strings containing column names
            String[] columNames={"REFERENCE NUMBER","EXPENSE NAME","EXPENSE COST","DATE RECORDED"};
                    //Create a blank sheet
               XSSFSheet spreadsheet = workBook.createSheet( " Job Expenses Report");

               // Create a Font for styling header cells
               Font headerFont = workBook.createFont();
               headerFont.setBold(true);
               headerFont.setFontHeightInPoints((short) 14);
               headerFont.setColor(IndexedColors.RED.getIndex());

               // Create a CellStyle with the font
               CellStyle headerCellStyle = workBook.createCellStyle();
               headerCellStyle.setFont(headerFont);

               // Create a Row
               Row headerRow = spreadsheet.createRow(0);

               // Create cells
               for(int i = 0; i < columNames.length; i++) {
               Cell cell = headerRow.createCell(i);
               cell.setCellValue(columNames[i]);
               cell.setCellStyle(headerCellStyle);
               }
               // Create Other rows and cells with employees data
               int rowNum = 1;
               for(Data data: list) {
               Row row = spreadsheet.createRow(rowNum++);

               row.createCell(0).setCellValue(data.refNum);

               row.createCell(1).setCellValue(data.expenseName);

               row.createCell(2).setCellValue(data.expenseCost);

               row.createCell(3).setCellValue(data.dateRecorded);
               }

               // Resize all columns to fit the content size
               for(int i = 0; i < columNames.length; i++) {
               spreadsheet.autoSizeColumn(i);
               }

               //Write the workbook in file system
               FileOutputStream out = new FileOutputStream(new File("/home/benjamin/Desktop/Job Expenses Report for " +jCBExpReport.getSelectedItem().toString()+".xlsx"));

               try {
                   workBook.write(out);
                   out.close();
                   workBook.close();
                   JOptionPane.showMessageDialog(null, "Data has been exported Successfully");
               } catch (IOException ex) {
                   Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
               }            
            
        }else if(tableIdf.equals("normal")){
            String[] columNames={"EXPENSE NAME","EXPENSE TYPE","EXPENSE COST","DATE RECORDED"};
                    //Create a blank sheet
               XSSFSheet spreadsheet = workBook.createSheet( " Normal Expenses Report");

               // Create a Font for styling header cells
               Font headerFont = workBook.createFont();
               headerFont.setBold(true);
               headerFont.setFontHeightInPoints((short) 14);
               headerFont.setColor(IndexedColors.RED.getIndex());

               // Create a CellStyle with the font
               CellStyle headerCellStyle = workBook.createCellStyle();
               headerCellStyle.setFont(headerFont);

               // Create a Row
               Row headerRow = spreadsheet.createRow(0);

               // Create cells
               for(int i = 0; i < columNames.length; i++) {
               Cell cell = headerRow.createCell(i);
               cell.setCellValue(columNames[i]);
               cell.setCellStyle(headerCellStyle);
               }
               // Create Other rows and cells with employees data
               int rowNum = 1;
               for(Data data: list) {
               Row row = spreadsheet.createRow(rowNum++);
               row.createCell(0).setCellValue(data.expenseName);
               
               row.createCell(1).setCellValue(data.refNum);//this is an expense type in disguise hihi

               row.createCell(2).setCellValue(data.expenseCost);

               row.createCell(3).setCellValue(data.dateRecorded);
               }

               // Resize all columns to fit the content size
               for(int i = 0; i < columNames.length; i++) {
               spreadsheet.autoSizeColumn(i);
               }

               //Write the workbook in file system
               FileOutputStream out = new FileOutputStream(new File("/home/benjamin/Desktop/Normal Expenses Report for " +jCBExpReport.getSelectedItem().toString()+".xlsx"));

               try {
                   workBook.write(out);
                   out.close();
                   workBook.close();
                   JOptionPane.showMessageDialog(null, "Data has been exported Successfully");
               } catch (IOException ex) {
                   Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
               }         
        }else{
            return;
        }
            
    }
    
    public void exportRevenueReportToExcel(int period) throws FileNotFoundException{
        Calendar now = Calendar.getInstance();
        int month=0;
        int year=0;
        
        //Create a blank workbook
        XSSFWorkbook workBook = new XSSFWorkbook();
        ArrayList<Data>list = new ArrayList<>();
        Main_Window window = new Main_Window();
        Connection con = (Connection)window.dbConnect();
        String query = "SELECT * FROM customers INNER JOIN salestable ON customers.custId=salestable.custID";
        //String query2="SELECT totalCost,amountPaid FROM salestable";
        Statement stmt;
        ResultSet rs;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            Data revInfo;
            Date dbDate;
      
                //System.out.println("Now "+period);
                if(period==1){
                   list.clear(); //Very imporant
                    month=now.get(Calendar.MONTH)+1;
                    year=now.get(Calendar.YEAR);
                    
                    //System.out.println("DB MONTH "+cal.get(Calendar.MONTH)+"AND MY MONTH "+month);
                    while(rs.next()){
                        dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("salestable.dateHired"));
                        //System.out.println("Date DB "+dbDate);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dbDate);
                        //System.out.println("DB MONTH "+cal.get(Calendar.MONTH)+"AND MY MONTH "+month);
                        if(cal.get(Calendar.MONTH)+1==month && cal.get(Calendar.YEAR)==year){
                            //System.out.println("Now "+rs.getString("customers.custName"));
                            //customer = new CustomerData(rs.getInt("id"),rs.getString("custId"),rs.getString("custName"),rs.getString("telephone"),rs.getString("transtype"),rs.getDate("hireDate"),rs.getString("refNum"));
                            revInfo=new Data(rs.getString("customers.refNum"), rs.getString("customers.custName"), rs.getDouble("salestable.amountPaid"), rs.getDouble("salestable.totalCost"), rs.getString("salestable.dateHired"));
                            //System.out.println(rs.getString("customers.custId")+" "+rs.getString("customers.custName")+" "+rs.getDouble("salestable.amountPaid"));
                            list.add(revInfo);
                        }
                    }                                      
                    
                    
                }else if(period==2){
                    list.clear();
                    month=now.get(Calendar.MONTH);
                    year=now.get(Calendar.YEAR);
                    
                    while(rs.next()){
                        dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("salestable.dateHired"));
                        //System.out.println("Date DB "+dbDate);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dbDate);
                        //System.out.println("DB MONTH "+cal.get(Calendar.MONTH)+"AND MY MONTH "+month);
                        if(cal.get(Calendar.MONTH)+1==month && cal.get(Calendar.YEAR)==year){
                            //System.out.println("Now "+rs.getString("customers.custName"));
                            //customer = new CustomerData(rs.getInt("id"),rs.getString("custId"),rs.getString("custName"),rs.getString("telephone"),rs.getString("transtype"),rs.getDate("hireDate"),rs.getString("refNum"));
                            revInfo=new Data(rs.getString("customers.refNum"), rs.getString("customers.custName"), rs.getDouble("salestable.amountPaid"), rs.getDouble("salestable.totalCost"), rs.getString("salestable.dateHired"));
                            //System.out.println(rs.getString("customers.custId")+" "+rs.getString("customers.custName")+" "+rs.getDouble("salestable.amountPaid"));
                            list.add(revInfo);
                        }
                    }
                    
                }else if(period==3){
                    list.clear();
                    //Fifth month from the indexed month
                    month=now.get(Calendar.MONTH)-4;
                    year=now.get(Calendar.YEAR);
                    int dbMonth=0;
                    int dbYear=0;
                    int currentMonth=now.get(Calendar.MONTH)+1;
                    
                    while(rs.next()){
                        dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("salestable.dateHired"));
                        //System.out.println("Date DB "+dbDate);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dbDate);
                        
                        dbMonth=cal.get(Calendar.MONTH)+1;
                        dbYear=cal.get(Calendar.YEAR);
                        
                        //System.out.println("DB MONTH "+cal.get(Calendar.MONTH)+"AND MY MONTH "+month);
                        if((month<=dbMonth && dbMonth<=currentMonth) && dbYear==year){
                            //System.out.println("Now "+cal.get(Calendar.MONTH)+" AND "+ (cal.get(Calendar.MONTH)-4));
                            //customer = new CustomerData(rs.getInt("id"),rs.getString("custId"),rs.getString("custName"),rs.getString("telephone"),rs.getString("transtype"),rs.getDate("hireDate"),rs.getString("refNum"));
                            revInfo=new Data(rs.getString("customers.refNum"), rs.getString("customers.custName"), rs.getDouble("salestable.amountPaid"), rs.getDouble("salestable.totalCost"), rs.getString("salestable.dateHired"));
                            //System.out.println(rs.getString("customers.custId")+" "+rs.getString("customers.custName")+" "+rs.getDouble("salestable.amountPaid"));
                            list.add(revInfo);
                        }
                    }
                }else if(period==4){
                    list.clear();
                    year=now.get(Calendar.YEAR);
                    while(rs.next()){
                        dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("salestable.dateHired"));
                        //System.out.println("Date DB "+dbDate);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dbDate);
                        //System.out.println("DB MONTH "+cal.get(Calendar.MONTH)+"AND MY MONTH "+month);
                        if(cal.get(Calendar.YEAR)==year){
                            //System.out.println("Now "+rs.getString("customers.custName"));
                            //customer = new CustomerData(rs.getInt("id"),rs.getString("custId"),rs.getString("custName"),rs.getString("telephone"),rs.getString("transtype"),rs.getDate("hireDate"),rs.getString("refNum"));
                            revInfo=new Data(rs.getString("customers.refNum"), rs.getString("customers.custName"), rs.getDouble("salestable.amountPaid"), rs.getDouble("salestable.totalCost"), rs.getString("salestable.dateHired"));
                            //System.out.println(rs.getString("customers.custId")+" "+rs.getString("customers.custName")+" "+rs.getDouble("salestable.amountPaid"));
                            list.add(revInfo);
                        }
                    }
                }else{
                    System.out.println("Now Here");
                }
                          
            
            
        } catch (Exception e) {
        }
        
        //Create a array of strings containing column names
        String[] columNames={"REFERENCE NUMBER","CUSTOMER NAME","AMOUNT PAID","INVOICED AMOUNT"};
        
        //Create a blank sheet
        XSSFSheet spreadsheet = workBook.createSheet( " Revenue Report");
        
        // Create a Font for styling header cells
        Font headerFont = workBook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());
        
        // Create a CellStyle with the font
        CellStyle headerCellStyle = workBook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        
        // Create a Row
        Row headerRow = spreadsheet.createRow(0);
        
        // Create cells
        for(int i = 0; i < columNames.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columNames[i]);
        cell.setCellStyle(headerCellStyle);
        }
        // Create Other rows and cells with employees data
        int rowNum = 1;
        for(Data data: list) {
        Row row = spreadsheet.createRow(rowNum++);
        
        row.createCell(0).setCellValue(data.refNum);
        
        row.createCell(1).setCellValue(data.custName);
        
        row.createCell(2).setCellValue(data.amountPaid);
        
        row.createCell(3).setCellValue(data.exAmount);
        }
        
        // Resize all columns to fit the content size
        for(int i = 0; i < columNames.length; i++) {
        spreadsheet.autoSizeColumn(i);
        }
        
        //Write the workbook in file system
        FileOutputStream out = new FileOutputStream(new File("/home/benjamin/Desktop/Revenue Reportfor " +jCBRevReport.getSelectedItem().toString()+".xlsx"));
        
        try {
            workBook.write(out);
            out.close();
            workBook.close();
            JOptionPane.showMessageDialog(null, "Data has been exported Successfully");
        } catch (IOException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public void searchByCustRefNum(String searchString){
//        custList.clear();
        custList = getCustomerSearchByRefNum(searchString);
        for(int i=0;i<custList.size();i++){
            jCBJobRev.addItem(custList.get(i).getCustName());
            jCBJobExp.addItem(custList.get(i).getCustName());
            jCBJobNet.addItem(custList.get(i).getCustName());
        }
    }
    
    // Searches the database using customer's reference number
    public ArrayList<CustomerData> getCustomerSearchByRefNum(String search){
        
            ArrayList<CustomerData>list = new ArrayList<>();
            Main_Window window = new Main_Window();
            Connection con = (Connection)window.dbConnect();
            
            String query = "SELECT * FROM customers WHERE refNum LIKE '"+search+"%' AND refNumStatus = 0";
            //String query2="SELECT totalCost,amountPaid FROM salestable";
            Statement stmt;
            ResultSet rs;

        try {
                        
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            
            CustomerData customer;
            
            while(rs.next()){
                customer = new CustomerData(rs.getInt("id"),rs.getString("custId"),rs.getString("custName"),rs.getString("telephone"),rs.getString("transtype"),rs.getDate("hireDate"),rs.getString("refNum"));
                list.add(customer);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(int i=0;i<list.size();i++){
            
            System.out.println(list.get(i).getId()+" "+list.get(i).getCustId()+""
                    + " "+list.get(i).getCustName()+" "+list.get(i).getTelephone()+" "+list.get(i).getTransType()+" "+list.get(i).getHireDate()+" "+list.get(i).getRefNumber());
        }
        
        return list;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Admin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Admin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Admin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Admin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Admin().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Btn_Update;
    private javax.swing.JButton Btn_delete;
    private javax.swing.JButton Btn_insert;
    private javax.swing.JTable JTableAttendants;
    private javax.swing.JButton jBtnReportExp;
    private javax.swing.JButton jBtnReportRev;
    private javax.swing.JButton jBtnSearch;
    private javax.swing.JButton jBtnTtlJobSearch;
    private javax.swing.JComboBox<String> jCBAnnualExp;
    private javax.swing.JComboBox<String> jCBAnnualNet;
    private javax.swing.JComboBox<String> jCBAnnualRev;
    private javax.swing.JComboBox<String> jCBExpReport;
    private javax.swing.JComboBox<String> jCBJobExp;
    private javax.swing.JComboBox<String> jCBJobNet;
    private javax.swing.JComboBox<String> jCBJobRev;
    private javax.swing.JComboBox<String> jCBMonthlyExp;
    private javax.swing.JComboBox<String> jCBMonthlyNet;
    private javax.swing.JComboBox<String> jCBMonthlyRev;
    private javax.swing.JComboBox<String> jCBRevReport;
    private javax.swing.JCheckBox jChkJobExp;
    private javax.swing.JCheckBox jChkNormExp;
    private javax.swing.JComboBox<String> jComboHiredItems;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable_Customer2;
    private javax.swing.JTextField jTextFieldCost;
    private javax.swing.JTextField jTextFieldDamaged;
    private javax.swing.JTextField jTextFieldId;
    private javax.swing.JTextField jTextFieldJobRef;
    private javax.swing.JTextField jTextFieldMissing;
    private javax.swing.JTextField jTextFieldName;
    private javax.swing.JTextField jTextFieldPhone;
    private javax.swing.JTextField jTextFieldRet;
    private javax.swing.JTextField jTextFieldSearch;
    private javax.swing.JTextField jTextFieldTentQty;
    private javax.swing.JTextField jTextFieldTotal;
    private javax.swing.JTextField jTextFieldTtlExp;
    private javax.swing.JTextField jTextFieldTtlExpMonthly;
    private javax.swing.JTextField jTextFieldTtlExpYear;
    private javax.swing.JTextField jTextFieldTtlJobRev;
    private javax.swing.JTextField jTextFieldTtlMonthlyNet;
    private javax.swing.JTextField jTextFieldTtlMonthlyRev;
    private javax.swing.JTextField jTextFieldTtlNet;
    private javax.swing.JTextField jTextFieldTtlYearNet;
    private javax.swing.JTextField jTextFieldTtlYearRev;
    private com.toedter.calendar.JDateChooser jtxtDate;
    private javax.swing.JTextField jtxtFirstName;
    private javax.swing.JTextField jtxtId;
    private javax.swing.JTextField jtxtLastName;
    private javax.swing.JTextField jtxtPassword;
    private javax.swing.JTextField jtxtUserName;
    // End of variables declaration//GEN-END:variables
}
