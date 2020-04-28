package events_system;
import java.awt.Color;
import java.awt.Image;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import javax.swing.table.DefaultTableModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author benjamin
 */
public class Events_Home extends javax.swing.JFrame {

    double pdtPrice=0;
    double TotalCost=0;
    double subTotal=0;
    double Tax;
    double amountPaid=0;
    
    String itemName=""; 
    String salesNum="";
    String trans_type="";
    int counter=0;
    //public arrayList
    ArrayList<Data> itemList = new ArrayList<>();
    
    //public variable declarations
    String ImgPath=null;
    int pos=0;
    String catItem=null;
    //Admin admin = new Admin();
    /**
     * Creates new form SupermarketHome
     */
    public Events_Home() {
        initComponents();
        displayCustomerData();
        displayProductData();
        loadItems();
        displayCustomerSearchData(jTextFieldSearch.getText());
        //setComponentColours();
    }
    
    ArrayList<CustomerData> customerList;
    
   public ArrayList<CustomerData> getCustomerSearchList(String search){
            
            customerList = new ArrayList<>();
            Main_Window window = new Main_Window();
            java.sql.Connection con = window.dbConnect();
            
            String query = "SELECT * FROM customers WHERE refNum LIKE '"+search+"%'";
            Statement stmt;
            ResultSet rs;
        try {
                        
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            CustomerData customer;
            
            while(rs.next()){
                customer = new CustomerData(rs.getInt("Id"),rs.getString("custId"),rs.getString("custName"),rs.getString("Telephone"),rs.getString("transType"),rs.getDate("hireDate"),rs.getString("refNum"));
                customerList.add(customer);
              //System.out.println(rs.getString("custName"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
        return customerList;
    }    
    
   public ArrayList getCustomerPaymentInfo(String cust_id){
       //System.out.println("CUST ID "+cust_id);
        ArrayList<CustomerData> salesList = new ArrayList<CustomerData>();
        double amountDue=0;
        
        Main_Window win = new Main_Window();
        com.mysql.jdbc.Connection conn = (com.mysql.jdbc.Connection)win.dbConnect();
        String query ="SELECT id,custID,totalCost,amountPaid FROM salestable WHERE custID='"+cust_id+"'";
        Statement stmt;
        ResultSet rs;
        CustomerData data;
        
        try {
            stmt=conn.createStatement();
            rs=stmt.executeQuery(query);
            
            while(rs.next()){
                amountDue=(rs.getDouble("totalCost")-rs.getDouble("amountPaid"));
                //System.out.println("CUST COST "+amountDue);
                data=new CustomerData(rs.getInt("id"),rs.getString("custID"),rs.getDouble("totalCost"),rs.getDouble("amountPaid"),amountDue);
                salesList.add(data);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return salesList;
    }
    
    public void displayCustomerSearchData(String search){
        ArrayList<CustomerData> list = getCustomerSearchList(search);
        ArrayList<CustomerData> salesList;
        String cust_id ="";
        DefaultTableModel model = (DefaultTableModel)jTable_Customer2.getModel();
        
        //Clear the JTable 
        model.setRowCount(0);
        Object[] row = new Object[6];
        
        
        for(int i=0;i<list.size();i++){

            //row[0]=list.get(i).getId();
            row[0]=list.get(i).getCustId();
            cust_id=(String)row[0];
            row[1]=list.get(i).getCustName();
            row[2]=list.get(i).getTelephone();
            row[3]=list.get(i).getTransType();
            salesList=getCustomerPaymentInfo(cust_id);
            //System.out.println(salesList);
            for(int j=0;j<salesList.size();j++){
               // System.out.println("Here"+list.get(i).getCustName()+" "+salesList.get(j).getAmountPaid());
                row[4]=salesList.get(j).getAmountDue();
                row[5]=salesList.get(j).getAmountPaid();
            }
            
           model.addRow(row);
        }
        
    } 
    
    //Records the sales of each item
    public void recordItemSales(String item_name,int item_number,double cost,String custId){
            try {
                
                Main_Window window = new Main_Window();
                Connection conn= window.dbConnect();
                PreparedStatement ps = conn.prepareStatement("INSERT INTO hired_items (item_name,item_number,cost,custId) VALUES  (?,?,?,?)");
                ps.setString(1, item_name);
                ps.setInt(2, item_number);
                ps.setDouble(3, cost);
                ps.setString(4, custId);
                ps.executeUpdate();
               // JOptionPane.showMessageDialog(null, "Data Saved");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
                Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
            }    
    
    }
    //Retrieve products for every category
    public ArrayList<Products> getCategoryProducts(){
        ArrayList<Products> productList = new ArrayList<Products>();
        Main_Window newWindow = new Main_Window();
        newWindow.dbConnect();
        String query="SELECT * FROM items_in_stock";
        Statement stmt;
        ResultSet rs;
        
        try {
            stmt=newWindow.dbConnect().createStatement();
            rs=stmt.executeQuery(query);
            Products product;
            while(rs.next()){
                product= new Products(rs.getInt("item_id"),rs.getString("item_name"),rs.getInt("item_number"),rs.getDouble("item_cost"),rs.getString("date"),rs.getBytes("image"));
                productList.add(product);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }
        return productList;
    }
    
    //Display the retrieved data in a JTable
   /* public void displayCategoryProducts(){
        ArrayList<Products> list = getCategoryProducts();
        //DefaultTableModel model = (DefaultTableModel)jtblProducts.getModel();
        
        //Clear the JTable 
       // model.setRowCount(0);
        Object[] row = new Object[4];
        
        for(int i=0;i<list.size();i++){
            row[0]=list.get(i).getName();
            row[1]=list.get(i).getPrice();   
            //row[2]=list.get(i).getCategory();
            model.addRow(row);
        }
    } */
    //get subcategories of the selected product
    public void loadItems(){
            Main_Window window = new Main_Window();       
            Connection con = window.dbConnect();
            
            String query = "SELECT * FROM items_in_stock";
            Statement stmt;
            ResultSet rs;
        try {
                        
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
         
            
            while(rs.next()){
                //product = new Products(rs.getInt("item_id"),rs.getString("item_name"),rs.getInt("item_number"),rs.getDouble("item_cost"),rs.getString("date"),rs.getBytes("image"));
                //productList.add(product);
                
                jComboMainCat.addItem(rs.getString("item_name"));
                jComboStock.addItem(rs.getString("item_name"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
        //Retrieve products data into the inventory section of current stock
    public void getSelectedProductsData(String productName){
            
           // ArrayList<Products> productList = new ArrayList<Products>();
           int pdtQty=0;
           Main_Window dbConn= new Main_Window();
           Connection con=dbConn.dbConnect();
            
           String query = "SELECT item_number FROM items_in_stock WHERE item_name='"+productName+"'";
      
            PreparedStatement ps;
            ResultSet rs;
        try {
            
                    ps = con.prepareStatement(query);
                    rs=ps.executeQuery();             
            
          //  Products product;
            
           while(rs.next()){
               pdtQty=rs.getInt("item_number");
                } 
           jtxtStockQty.setText(Integer.toString(pdtQty));
       } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    //Display the retrieved data in a JTable
    public void displaySelectedProductsData(String productName){
        //ArrayList<Products> list = getSelectedProductsData();
        //Iterator itr = list.iterator();
        
        //while(itr.hasNext()){
        //System.out.println(itr.next());
        //}
        getSelectedProductsData(productName);
    }
    
    public void print(){
        
        try {
            jReceiptArea.print();
            //   int year=Calendar.getInstance().get(Calendar.YEAR);
            //   int rand=(int)((Math.random()*100)+1);
            //  System.out.println(rand);
            /*     String randomNum=Integer.toString(rand);
            String refNum=Integer.toString(year)+randomNum;
            
            Calendar timer = Calendar.getInstance();
            timer.getTime();
            SimpleDateFormat tTime = new SimpleDateFormat("HH:mm:ss");
            tTime.format(timer.getTime());
            SimpleDateFormat tDate = new SimpleDateFormat("dd-MM-yyyy");
            tDate.format(timer.getTime());
            
            jReceiptArea.append("\tSupermarket System \n "
            + "Reference:\t\t"+refNum+"\n"
            + "===========================\n"
            + "===========================\n"
            + "\nTax \t\t"+10
            + "\nSub Total \t\t"
            + "\nTotal \t\t"+TotalCost
            + "\n==========================="
            + "\nDate:\t\t"+tDate.format(timer.getTime())+""
            + "\nTime:\t\t"+tTime.format(timer.getTime())+""
            + "\n\nThank you for Shopping with us\n")*/;
            //jReceiptArea.setText(null);
        } catch (PrinterException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /*
    public void showItemInfo(String item_name){
           // ArrayList<Products> productList = new ArrayList<Products>();
           Main_Window dbConn= new Main_Window();
           Connection con=dbConn.dbConnect();
           String query = "SELECT * FROM items_in_stock WHERE item_name='"+item_name+"'";
      
            PreparedStatement ps;
            ResultSet rs;
        try {
            
                    ps = con.prepareStatement(query);
                    rs=ps.executeQuery();             
            
          while(rs.next()){
              //System.out.println(rs.getString("id")+" "+rs.getString("")+" "+rs.getFloat(2)+" "+rs.getString(3)+" "+rs.getString(4));
              String customer = jTxtCustomer.getText();
              jtxtStockQty.setText(Integer.toString(rs.getInt("item_number")));
              if(customer.isEmpty()){
                  jTxtCustomer.setEnabled(true);
                  jTxtCustId.setEnabled(true);
                  jTxtPhone.setEnabled(true);
              }else{
                  jTxtCustId.setEnabled(false);
                  jTxtCustomer.setEnabled(false);
                  jTxtPhone.setEnabled(false);
              }              
              jTxtCost.setText(Double.toString(rs.getDouble("item_cost")));
              jTxtNumber.setText(Integer.toString(rs.getInt("item_number")));
              jTxtDiscount.setText("0");
              //int quantity=Integer.parseInt(jTxtQty.getText());
          }
        } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }   
    } */
    //This function gets quantity and current cost of a selected item during customer order
    public void showSelectedItemInfo(String item_name){
        Main_Window main = new Main_Window();
        Connection con = (Connection) main.dbConnect();
        String query = "SELECT * FROM items_in_stock WHERE item_name ='"+item_name+"'";
        Statement stmt;
        ResultSet rs;
        
        try {
            stmt = con.createStatement();
            rs=stmt.executeQuery(query);
            
            while(rs.next()){
              jTxtNumber.setText(Integer.toString(rs.getInt("item_number")));
              jTxtCost.setText(Double.toString(rs.getDouble("item_cost")));
              jTxtDiscount.setText("0");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    //Totally different from the one above
    public void showItemInfo(String item_name){
        Main_Window main = new Main_Window();
        Connection con = (Connection) main.dbConnect();
        String query = "SELECT * FROM hired_items WHERE item_name ='"+item_name+"'";
        Statement stmt;
        ResultSet rs;
        
        try {
            stmt = con.createStatement();
            rs=stmt.executeQuery(query);
            
            while(rs.next()){
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
    
    public void saveCustomerDetails(String custName, String custId, String telephone, String email,String transType,String refNum){
        try {
            Main_Window window = new Main_Window();
            Connection conn= window.dbConnect();    
            PreparedStatement ps = conn.prepareStatement("INSERT INTO customers (custName,custId,telephone,emailAddress,transType,refNum) VALUES  (?,?,?,?,?,?)");
            ps.setString(1, custName);
            ps.setString(2, custId);
            ps.setString(3, telephone);
            ps.setString(4, email);
            ps.setString(5, transType);
            ps.setString(6, refNum);
            ps.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void recordSalesDetails(String custId,String salesNum,double total,String transType,String dueDate,String dateHired){
        try {
            /*    this.custId=custId;
            this.invoiceNum=invoiceNum;
            this.dueDate=dueDate*/;
            Main_Window window = new Main_Window();
            Connection conn= window.dbConnect();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO salestable (custId,sNumber,totalCost,transType,dueDate,dateHired) VALUES  (?,?,?,?,?,?)");
            ps.setString(1, custId);
            ps.setString(2, salesNum);
            ps.setDouble(3, total);
            ps.setString(4, transType);
            ps.setString(5, dueDate);
            ps.setString(6, dateHired);
            
            
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
   /*
    public void recordReceiptDetails(String custId,String receiptNum,double totalCost){
        try {
                this.custId=custId;
            this.invoiceNum=invoiceNum;
            this.dueDate=dueDate;
            Main_Window window = new Main_Window();
            Connection conn= window.dbConnect();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO receipt (custId,receiptNum,totalCost) VALUES  (?,?,?)");
            ps.setString(1, custId);
            ps.setString(2, receiptNum);
            ps.setDouble(3, totalCost);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }  
    } */

    public void generateInvoice(){
        int year=Calendar.getInstance().get(Calendar.YEAR);
        int rand=(int)((Math.random()*1000)+1);
      //  System.out.println(rand);
        String randomNum=Integer.toString(rand);
        salesNum=Integer.toString(year)+randomNum;

        Calendar timer = Calendar.getInstance();
        timer.getTime();
        SimpleDateFormat tTime = new SimpleDateFormat("HH:mm:ss");
        tTime.format(timer.getTime());
        SimpleDateFormat tDate = new SimpleDateFormat("dd-MM-yyyy");
        tDate.format(timer.getTime()); 
        
        jReceiptArea.setText("*******************************************\n"+
                            "*        EXCEL EVENTS SALES INVOICE       *\n"+
                            "*******************************************\n");
        jReceiptArea.setText(jReceiptArea.getText()+"Invoice Number   :                "+salesNum+"\n");
        jReceiptArea.setText(jReceiptArea.getText()+"Date                   :                "+tDate.format(timer.getTime())+"\n");
        jReceiptArea.setText(jReceiptArea.getText()+"Time                   :                "+tTime.format(timer.getTime())+"\n");
        jReceiptArea.setText(jReceiptArea.getText()+"To : "+jTxtCustomer.getText()+"\nFrom : Excel Events Limited\n");
        jReceiptArea.setText(jReceiptArea.getText()+"*******************************************\n\n");
        jReceiptArea.setText(jReceiptArea.getText()+"Item      Qty      Unit Price      Cost\n");
        jReceiptArea.setText(jReceiptArea.getText()+"----------------------------------------------------------------\n\n");
        for (Data data : itemList) {
            jReceiptArea.setText(jReceiptArea.getText()+""+data.item_name+"       "+data.item_number+"      "+data.item_cost+"   "+jTxtSubTotal.getText()+"\n");
        }
        jReceiptArea.setText(jReceiptArea.getText()+"\n                                  Sub Total : "+subTotal+"\n");
        jReceiptArea.setText(jReceiptArea.getText()+"                                  Discount  : "+jTxtDiscount.getText()+"\n");
        jReceiptArea.setText(jReceiptArea.getText()+"                                  Tax       : "+Tax+"\n");
        jReceiptArea.setText(jReceiptArea.getText()+"                                  Total Due : "+TotalCost+"\n\n");
        jReceiptArea.setText(jReceiptArea.getText()+"Payment Due By : "+invoiceDueDate()+"\n");
        jReceiptArea.setText(jReceiptArea.getText()+"Through : \n");
        jReceiptArea.setText(jReceiptArea.getText()+"    Bank Account Number : 3100046786\n");
        jReceiptArea.setText(jReceiptArea.getText()+"    Mobile Money : 070---------\n"
                                                   +"                             078---------\n\n");
        jReceiptArea.setText(jReceiptArea.getText()+"*******************************************\n");
        jReceiptArea.setText(jReceiptArea.getText()+"*     Thank You for Supporting Excel       *\n"
                                                   +"*                 Events Limited                     *\n");
        jReceiptArea.setText(jReceiptArea.getText()+"*******************************************\n");
        //print();
    }
    
    public String invoiceDueDate(){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date currentDate = new Date();
       // System.out.println(dateFormat.format(currentDate));

        // convert date to calendar
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);

        // manipulate date
        c.add(Calendar.MONTH, 1);
      //  c.add(Calendar.DATE, 1); //same with c.add(Calendar.DAY_OF_MONTH, 1);

        // convert calendar to date
        Date currentDatePlusOne = c.getTime();

        return dateFormat.format(currentDatePlusOne);
    }
    public void generateReceipt(){
        int year=Calendar.getInstance().get(Calendar.YEAR);
        int rand=(int)((Math.random()*100)+1);
      //  System.out.println(rand);
        String randomNum=Integer.toString(rand);
        salesNum=Integer.toString(year)+randomNum;

        Calendar timer = Calendar.getInstance();
        timer.getTime();
        SimpleDateFormat tTime = new SimpleDateFormat("HH:mm:ss");
        tTime.format(timer.getTime());
        SimpleDateFormat tDate = new SimpleDateFormat("dd-MM-yyyy");
        tDate.format(timer.getTime());
        
        jReceiptArea.setText("*******************************************\n"+
                            "*        EXCEL EVENTS SALES RECEIPT       *\n"+
                            "*******************************************\n\n");
        jReceiptArea.setText(jReceiptArea.getText()+"Receipt Number :                "+salesNum+"\n\n");
        jReceiptArea.setText(jReceiptArea.getText()+"Date    :                       "+tDate.format(timer.getTime())+"\n");
        jReceiptArea.setText(jReceiptArea.getText()+"Time    :                       "+tTime.format(timer.getTime())+"\n\n");
        jReceiptArea.setText(jReceiptArea.getText()+"Item Name      Quantity       Amount\n\n");
        for (Data data : itemList) {
            jReceiptArea.setText(jReceiptArea.getText()+""+data.item_name+"               "+data.item_number+"            "+data.item_cost+"\n");
        }
        jReceiptArea.setText(jReceiptArea.getText()+"\n");
        jReceiptArea.setText(jReceiptArea.getText()+"                          Total Cost   "+jTxtTotalCost.getText()+"\n");
        jReceiptArea.setText(jReceiptArea.getText()+"\n");
        jReceiptArea.setText(jReceiptArea.getText()+"*******************************************\n");
        jReceiptArea.setText(jReceiptArea.getText()+"*     Thank You for Supporting Excel       *\n"
                                                   +"*                 Events Limited                     *\n");
        jReceiptArea.setText(jReceiptArea.getText()+"*******************************************\n");
        print();
    }
    public void checkAddedItems(String item_name){
        String test = jReceiptArea.getText();
        if(test.contains(item_name) && !test.isEmpty()){
            //System.out.println(test);
            JOptionPane.showMessageDialog(null, "Item already added to the list");
        }else{
            showItemInfo(item_name);
            jReceiptArea.append(item_name+"\n");
        }   
    }
    
    /*
    public boolean checkSubTotalAdded(String item_name){
        String test = jReceiptArea.getText();
        return test.contains(itemName) && !test.isEmpty(); //System.out.println(test);
        // JOptionPane.showMessageDialog(null, "Item already added to the list");
        // showItemInfo(itemName);
        // jReceiptArea.append(itemName+"\n");
    } */
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup2 = new javax.swing.ButtonGroup();
        jdesktop = new javax.swing.JDesktopPane();
        jTabbedPaneHome = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jComboMainCat = new javax.swing.JComboBox<>();
        jPanel8 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jTxtCost = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTxtSubTotal = new javax.swing.JTextField();
        jBtnSubTotal = new javax.swing.JButton();
        jTxtNumber = new javax.swing.JTextField();
        jBtnTotalCost = new javax.swing.JButton();
        jTxtTotalCost = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jTxtDiscount = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jRadioInv = new javax.swing.JRadioButton();
        jRadioRec = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jTxtCustomer = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTxtCustId = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTxtPhone = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jTxtEmail = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jBtnPrint = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jReceiptArea = new javax.swing.JTextArea();
        jBtnSaveData = new javax.swing.JButton();
        jBtnReset = new javax.swing.JButton();
        jPanelStock = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        editItemsPanel = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jtxtName = new javax.swing.JTextField();
        jlblImage = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        JTable_Products = new javax.swing.JTable();
        Btn_Choose_Image = new javax.swing.JButton();
        Btn_Next = new javax.swing.JButton();
        Btn_insert = new javax.swing.JButton();
        Btn_Update = new javax.swing.JButton();
        Btn_delete = new javax.swing.JButton();
        Btn_Previous = new javax.swing.JButton();
        Btn_First = new javax.swing.JButton();
        Btn_Last = new javax.swing.JButton();
        jtxtNumber = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        jtxtDate = new com.toedter.calendar.JDateChooser();
        jLabel32 = new javax.swing.JLabel();
        jtxtCost = new javax.swing.JTextField();
        jComboMain = new javax.swing.JComboBox<>();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jComboSub = new javax.swing.JComboBox<>();
        jBtnReset2 = new javax.swing.JButton();
        jtxtId = new javax.swing.JTextField();
        jLabel35 = new javax.swing.JLabel();
        retItemsPanel = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        JTable_Products1 = new javax.swing.JTable();
        Btn_Next1 = new javax.swing.JButton();
        Btn_insert1 = new javax.swing.JButton();
        Btn_Previous1 = new javax.swing.JButton();
        Btn_First1 = new javax.swing.JButton();
        Btn_Last1 = new javax.swing.JButton();
        jtxtNumber1 = new javax.swing.JTextField();
        jtxtDate1 = new com.toedter.calendar.JDateChooser();
        jComboMain1 = new javax.swing.JComboBox<>();
        jLabel42 = new javax.swing.JLabel();
        jBtnReset3 = new javax.swing.JButton();
        jtxtId1 = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jComboSub2 = new javax.swing.JComboBox<>();
        trackItemsPanel = new javax.swing.JPanel();
        jComboStock = new javax.swing.JComboBox<>();
        jLabel43 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        JTable_Stock = new javax.swing.JTable();
        jLabel40 = new javax.swing.JLabel();
        jtxtStockQty = new javax.swing.JTextField();
        jLabel36 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jComboStockYear = new javax.swing.JComboBox<>();
        jLabel46 = new javax.swing.JLabel();
        jComboStockMonth = new javax.swing.JComboBox<>();
        jCheckBoxStock = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable_Customer2 = new javax.swing.JTable();
        jTextFieldSearch = new javax.swing.JTextField();
        jBtnSearch = new javax.swing.JButton();
        jPanelCustomer2 = new javax.swing.JPanel();
        jLabelName4 = new javax.swing.JLabel();
        jLabelPhone4 = new javax.swing.JLabel();
        jLabelId4 = new javax.swing.JLabel();
        jLabelId5 = new javax.swing.JLabel();
        jLabelName5 = new javax.swing.JLabel();
        jLabelPhone5 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jTextFieldPhone = new javax.swing.JTextField();
        jTextFieldId = new javax.swing.JTextField();
        jTextFieldName = new javax.swing.JTextField();
        jLabel52 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jTextFieldTotal = new javax.swing.JTextField();
        jLabel53 = new javax.swing.JLabel();
        jTextFieldTentQty = new javax.swing.JTextField();
        jTextFieldCost = new javax.swing.JTextField();
        jLabel54 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        jComboHiredItems = new javax.swing.JComboBox<>();
        jPanel15 = new javax.swing.JPanel();
        jLabel57 = new javax.swing.JLabel();
        jTextFieldDamaged = new javax.swing.JTextField();
        jLabel58 = new javax.swing.JLabel();
        jTextFieldMissing = new javax.swing.JTextField();
        jLabel59 = new javax.swing.JLabel();
        jTextFieldRet = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldAmount = new javax.swing.JTextField();
        jTextFieldRefNum = new javax.swing.JTextField();
        jBtnPayment = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel60 = new javax.swing.JLabel();
        jComboMore = new javax.swing.JComboBox<>();
        jLabel61 = new javax.swing.JLabel();
        jComboTrans = new javax.swing.JComboBox<>();
        jPanelExpense = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel62 = new javax.swing.JLabel();
        jComboExpName = new javax.swing.JComboBox<>();
        jLabel47 = new javax.swing.JLabel();
        jTextFieldExpCost = new javax.swing.JTextField();
        jLabel48 = new javax.swing.JLabel();
        jTextFieldExpDate = new com.toedter.calendar.JDateChooser();
        jLabel63 = new javax.swing.JLabel();
        jComboExpType = new javax.swing.JComboBox<>();
        jPanel16 = new javax.swing.JPanel();
        jTextFieldJobRef = new javax.swing.JTextField();
        jLabel49 = new javax.swing.JLabel();
        jComboJobName = new javax.swing.JComboBox<>();
        jLabel64 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jTextFieldJobCost = new javax.swing.JTextField();
        jLabel66 = new javax.swing.JLabel();
        jTextFieldJobDate = new com.toedter.calendar.JDateChooser();
        jLabel10 = new javax.swing.JLabel();
        jBtnResetExpense = new javax.swing.JButton();
        jBtnSaveExpense = new javax.swing.JButton();
        jCheckBoxNorm = new javax.swing.JCheckBox();
        jCheckBoxJob = new javax.swing.JCheckBox();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jexitOption = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTabbedPaneHome.setBackground(new java.awt.Color(41, 47, 51));
        jTabbedPaneHome.setFont(jTabbedPaneHome.getFont().deriveFont(jTabbedPaneHome.getFont().getStyle() | java.awt.Font.BOLD, jTabbedPaneHome.getFont().getSize()+3));
        jTabbedPaneHome.setPreferredSize(new java.awt.Dimension(1350, 675));
        jTabbedPaneHome.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTabbedPaneHomeFocusGained(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(41, 47, 51));

        jPanel11.setBackground(new java.awt.Color(85, 172, 238));
        jPanel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        jPanel11.setPreferredSize(new java.awt.Dimension(909, 500));

        jLabel1.setFont(new java.awt.Font("Ubuntu", 1, 24)); // NOI18N
        jLabel1.setText("ITEMS FOR HIRE");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        jLabel37.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel37.setText("Items");

        jComboMainCat.setFont(new java.awt.Font("Ubuntu", 0, 18)); // NOI18N
        jComboMainCat.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Choose--" }));
        jComboMainCat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboMainCatActionPerformed(evt);
            }
        });

        jPanel8.setBackground(new java.awt.Color(85, 172, 238));
        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Order Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18))); // NOI18N

        jLabel8.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel8.setText("Quantity");

        jTxtCost.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTxtCost.setPreferredSize(new java.awt.Dimension(10, 32));
        jTxtCost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTxtCostActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel9.setText("Unit Cost");

        jTxtSubTotal.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTxtSubTotal.setPreferredSize(new java.awt.Dimension(10, 32));
        jTxtSubTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTxtSubTotalActionPerformed(evt);
            }
        });

        jBtnSubTotal.setBackground(new java.awt.Color(151, 151, 129));
        jBtnSubTotal.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jBtnSubTotal.setText("Total");
        jBtnSubTotal.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jBtnSubTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSubTotalActionPerformed(evt);
            }
        });

        jTxtNumber.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTxtNumber.setPreferredSize(new java.awt.Dimension(10, 32));
        jTxtNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTxtNumberActionPerformed(evt);
            }
        });

        jBtnTotalCost.setBackground(new java.awt.Color(151, 151, 129));
        jBtnTotalCost.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jBtnTotalCost.setText("Total Cost");
        jBtnTotalCost.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jBtnTotalCost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnTotalCostActionPerformed(evt);
            }
        });

        jTxtTotalCost.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTxtTotalCost.setPreferredSize(new java.awt.Dimension(10, 32));
        jTxtTotalCost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTxtTotalCostActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel13.setText("Discount");

        jTxtDiscount.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTxtDiscount.setPreferredSize(new java.awt.Dimension(10, 32));
        jTxtDiscount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTxtDiscountActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel4.setText("GENERATE AND PRINT");

        buttonGroup2.add(jRadioInv);
        jRadioInv.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jRadioInv.setText("Invoice");
        jRadioInv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioInvActionPerformed(evt);
            }
        });

        buttonGroup2.add(jRadioRec);
        jRadioRec.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jRadioRec.setText("Receipt");
        jRadioRec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioRecActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jBtnSubTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(65, 65, 65)
                        .addComponent(jTxtSubTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jBtnTotalCost, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(53, 53, 53)
                        .addComponent(jTxtTotalCost, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 103, Short.MAX_VALUE))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(100, 100, 100)))
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTxtDiscount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTxtCost, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                            .addComponent(jTxtNumber, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioRec, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jRadioInv, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTxtNumber, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTxtCost, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTxtDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnSubTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTxtSubTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnTotalCost, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTxtTotalCost, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioInv, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioRec, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );

        jPanel1.setBackground(new java.awt.Color(85, 172, 238));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Customer Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18))); // NOI18N

        jLabel7.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel7.setText("Customer  Name");

        jTxtCustomer.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTxtCustomer.setPreferredSize(new java.awt.Dimension(10, 32));
        jTxtCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTxtCustomerActionPerformed(evt);
            }
        });
        jTxtCustomer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTxtCustomerKeyTyped(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel11.setText("Reference Number");

        jTxtCustId.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTxtCustId.setPreferredSize(new java.awt.Dimension(10, 32));
        jTxtCustId.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTxtCustIdFocusGained(evt);
            }
        });
        jTxtCustId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTxtCustIdActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel12.setText("Telephone");

        jTxtPhone.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTxtPhone.setPreferredSize(new java.awt.Dimension(10, 32));
        jTxtPhone.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTxtPhoneFocusGained(evt);
            }
        });
        jTxtPhone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTxtPhoneActionPerformed(evt);
            }
        });

        jLabel27.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel27.setText("Email Address");

        jTxtEmail.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTxtEmail.setPreferredSize(new java.awt.Dimension(10, 32));
        jTxtEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTxtEmailFocusGained(evt);
            }
        });
        jTxtEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTxtEmailActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jTxtPhone, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                        .addComponent(jTxtCustId, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTxtCustomer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jTxtEmail, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(162, 162, 162))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTxtCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                    .addComponent(jTxtCustId, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                    .addComponent(jTxtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                    .addComponent(jTxtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(35, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Ubuntu", 1, 24)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("PRICING");
        jLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jBtnPrint.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jBtnPrint.setText("PRINT");
        jBtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnPrintActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel11Layout.createSequentialGroup()
                            .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                            .addComponent(jComboMainCat, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(23, 23, 23))
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jBtnPrint, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jComboMainCat)
                    .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jBtnPrint, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel13.setBackground(new java.awt.Color(85, 172, 238));
        jPanel13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        jLabel5.setFont(new java.awt.Font("Ubuntu", 1, 24)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("INVOICE OR RECEIPT");
        jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        jReceiptArea.setColumns(20);
        jReceiptArea.setRows(5);
        jScrollPane3.setViewportView(jReceiptArea);

        jBtnSaveData.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jBtnSaveData.setText("Save");
        jBtnSaveData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSaveDataActionPerformed(evt);
            }
        });

        jBtnReset.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jBtnReset.setText("Reset");
        jBtnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnResetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jBtnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jBtnSaveData, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBtnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnSaveData, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, 844, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(44, 44, 44))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPaneHome.addTab("HOME", jPanel2);

        jPanelStock.setBackground(new java.awt.Color(41, 47, 51));

        jTabbedPane1.setBackground(new java.awt.Color(41, 47, 51));
        jTabbedPane1.setFont(new java.awt.Font("Ubuntu", 1, 14)); // NOI18N

        editItemsPanel.setBackground(new java.awt.Color(85, 172, 238));
        editItemsPanel.setPreferredSize(new java.awt.Dimension(1000, 595));

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel28.setText("Image");

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel29.setText("Quantity");

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel30.setText("Date");

        jtxtName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jtxtName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jtxtNameMouseEntered(evt);
            }
        });
        jtxtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtxtNameActionPerformed(evt);
            }
        });
        jtxtName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jtxtNameKeyTyped(evt);
            }
        });

        jlblImage.setBackground(new java.awt.Color(204, 255, 255));
        jlblImage.setOpaque(true);

        JTable_Products.setBackground(new java.awt.Color(85, 172, 238));
        JTable_Products.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "NAME", "COST PER ITEM", "QUANTITY IN STOCK", "DATE ADDED"
            }
        ));
        JTable_Products.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JTable_ProductsMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(JTable_Products);
        if (JTable_Products.getColumnModel().getColumnCount() > 0) {
            JTable_Products.getColumnModel().getColumn(1).setHeaderValue("COST PER ITEM");
        }

        Btn_Choose_Image.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        Btn_Choose_Image.setText("Choose Image");
        Btn_Choose_Image.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn_Choose_ImageActionPerformed(evt);
            }
        });

        Btn_Next.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Btn_Next.setText("Next");
        Btn_Next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn_NextActionPerformed(evt);
            }
        });

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

        Btn_Previous.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Btn_Previous.setText("Previous");
        Btn_Previous.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn_PreviousActionPerformed(evt);
            }
        });

        Btn_First.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Btn_First.setText("First");
        Btn_First.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn_FirstActionPerformed(evt);
            }
        });

        Btn_Last.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Btn_Last.setText("Last");
        Btn_Last.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn_LastActionPerformed(evt);
            }
        });

        jtxtNumber.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel31.setText("Name");

        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel32.setText("Cost");

        jtxtCost.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jComboMain.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Choose--", "Tables", "Chairs", "Tents", "Covers" }));
        jComboMain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboMainActionPerformed(evt);
            }
        });

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel33.setText("Main Category");

        jLabel34.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel34.setText("Sub Category");

        jComboSub.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboSubActionPerformed(evt);
            }
        });

        jBtnReset2.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jBtnReset2.setText("RESET");
        jBtnReset2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnReset2ActionPerformed(evt);
            }
        });

        jtxtId.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel35.setText("Item ID");

        javax.swing.GroupLayout editItemsPanelLayout = new javax.swing.GroupLayout(editItemsPanel);
        editItemsPanel.setLayout(editItemsPanelLayout);
        editItemsPanelLayout.setHorizontalGroup(
            editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editItemsPanelLayout.createSequentialGroup()
                .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, editItemsPanelLayout.createSequentialGroup()
                        .addGap(174, 174, 174)
                        .addComponent(Btn_insert, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Btn_Update, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(Btn_delete, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Btn_First, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Btn_Previous, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Btn_Next, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Btn_Last, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(editItemsPanelLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editItemsPanelLayout.createSequentialGroup()
                                .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(32, 32, 32)
                                .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jlblImage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jtxtDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jtxtCost, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(editItemsPanelLayout.createSequentialGroup()
                                        .addComponent(Btn_Choose_Image, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(editItemsPanelLayout.createSequentialGroup()
                                .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                                    .addComponent(jLabel31, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                                .addGap(32, 32, 32)
                                .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboSub, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboMain, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jtxtName)))
                            .addGroup(editItemsPanelLayout.createSequentialGroup()
                                .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32)
                                .addComponent(jtxtNumber))
                            .addGroup(editItemsPanelLayout.createSequentialGroup()
                                .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32)
                                .addComponent(jtxtId)))
                        .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(editItemsPanelLayout.createSequentialGroup()
                                .addGap(144, 144, 144)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 631, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(editItemsPanelLayout.createSequentialGroup()
                                .addGap(54, 54, 54)
                                .addComponent(jBtnReset2, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        editItemsPanelLayout.setVerticalGroup(
            editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editItemsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editItemsPanelLayout.createSequentialGroup()
                        .addGap(0, 26, Short.MAX_VALUE)
                        .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jtxtId, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboMain, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboSub, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtName, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jtxtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jtxtCost, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtDate, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(editItemsPanelLayout.createSequentialGroup()
                                .addGap(35, 35, 35)
                                .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jlblImage, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(Btn_Choose_Image, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(jBtnReset2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Btn_Update, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Btn_delete, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Btn_insert, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Btn_Next, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Btn_Last, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Btn_Previous, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Btn_First, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(editItemsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1167, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(editItemsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("EDIT ITEMS", jPanel6);

        retItemsPanel.setBackground(new java.awt.Color(85, 172, 238));
        retItemsPanel.setPreferredSize(new java.awt.Dimension(1000, 595));

        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel38.setText("Quantity");

        jLabel39.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel39.setText("Date");

        JTable_Products1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "NO.", "REFERENCE NO.", "CUSTOMER NAME", "HIRE DATE"
            }
        ));
        JTable_Products1.setGridColor(new java.awt.Color(83, 95, 203));
        JTable_Products1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JTable_Products1MouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(JTable_Products1);
        if (JTable_Products1.getColumnModel().getColumnCount() > 0) {
            JTable_Products1.getColumnModel().getColumn(0).setPreferredWidth(100);
        }

        Btn_Next1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Btn_Next1.setText("Next");
        Btn_Next1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn_Next1ActionPerformed(evt);
            }
        });

        Btn_insert1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Btn_insert1.setText("Save");
        Btn_insert1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn_insert1ActionPerformed(evt);
            }
        });

        Btn_Previous1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Btn_Previous1.setText("Previous");
        Btn_Previous1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn_Previous1ActionPerformed(evt);
            }
        });

        Btn_First1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Btn_First1.setText("First");
        Btn_First1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn_First1ActionPerformed(evt);
            }
        });

        Btn_Last1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Btn_Last1.setText("Last");
        Btn_Last1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn_Last1ActionPerformed(evt);
            }
        });

        jtxtNumber1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jComboMain1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Choose--" }));
        jComboMain1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboMain1ActionPerformed(evt);
            }
        });

        jLabel42.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel42.setText("Item Categories");

        jBtnReset3.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jBtnReset3.setText("RESET");
        jBtnReset3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnReset3ActionPerformed(evt);
            }
        });

        jtxtId1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jtxtId1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtxtId1ActionPerformed(evt);
            }
        });

        jLabel44.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel44.setText("Reference Number");

        jLabel45.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel45.setText("Item Condition");

        jComboSub2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Choose--", "Okay", "Damaged", "Missing" }));
        jComboSub2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboSub2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout retItemsPanelLayout = new javax.swing.GroupLayout(retItemsPanel);
        retItemsPanel.setLayout(retItemsPanelLayout);
        retItemsPanelLayout.setHorizontalGroup(
            retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(retItemsPanelLayout.createSequentialGroup()
                .addGroup(retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(retItemsPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Btn_First1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Btn_Previous1)
                        .addGap(18, 18, 18)
                        .addComponent(Btn_Next1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Btn_Last1, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(retItemsPanelLayout.createSequentialGroup()
                        .addGroup(retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(retItemsPanelLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel45, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                                        .addComponent(jLabel38, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel39, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel44)))
                            .addGroup(retItemsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jBtnReset3, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(60, 60, 60)
                        .addGroup(retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Btn_insert1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jtxtNumber1)
                            .addComponent(jtxtDate1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                            .addComponent(jComboSub2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboMain1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jtxtId1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 120, Short.MAX_VALUE)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 631, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        retItemsPanelLayout.setVerticalGroup(
            retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(retItemsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(retItemsPanelLayout.createSequentialGroup()
                        .addGroup(retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jtxtId1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel44, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboMain1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel45, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboSub2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jtxtNumber1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30)
                        .addGroup(retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jtxtDate1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(43, 43, 43)
                        .addGroup(retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Btn_insert1, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                            .addComponent(jBtnReset3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(retItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Btn_Next1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Btn_Last1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Btn_Previous1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Btn_First1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(82, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("RECORD RETURNED ITEMS", retItemsPanel);

        trackItemsPanel.setBackground(new java.awt.Color(85, 172, 238));

        jComboStock.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Choose--" }));
        jComboStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboStockActionPerformed(evt);
            }
        });

        jLabel43.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel43.setText("Select Item");

        JTable_Stock.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ITEM NAME", "DAMAGED ITEMS", "MISSING ITEMS"
            }
        ));
        JTable_Stock.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JTable_StockMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(JTable_Stock);

        jLabel40.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel40.setText("Quantity");

        jtxtStockQty.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jLabel36.setFont(new java.awt.Font("Ubuntu", 1, 24)); // NOI18N
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel36.setText("CURRENT STOCK");

        jLabel41.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel41.setText("SELECT YEAR");

        jComboStockYear.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2029", "2030" }));
        jComboStockYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboStockYearActionPerformed(evt);
            }
        });

        jLabel46.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel46.setText("SELECT MONTH");

        jComboStockMonth.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        jComboStockMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboStockMonthActionPerformed(evt);
            }
        });

        jCheckBoxStock.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jCheckBoxStock.setText("ANNUAL");
        jCheckBoxStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxStockActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jButton1.setText("EXPORT TO EXCEL");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jCheckBox1.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jCheckBox1.setText("MONTHLY");

        javax.swing.GroupLayout trackItemsPanelLayout = new javax.swing.GroupLayout(trackItemsPanel);
        trackItemsPanel.setLayout(trackItemsPanelLayout);
        trackItemsPanelLayout.setHorizontalGroup(
            trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, trackItemsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(trackItemsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addComponent(jtxtStockQty))
                    .addGroup(trackItemsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel43)
                        .addGap(34, 34, 34)
                        .addComponent(jComboStock, 0, 177, Short.MAX_VALUE)))
                .addGap(52, 52, 52)
                .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(trackItemsPanelLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(trackItemsPanelLayout.createSequentialGroup()
                                .addComponent(jCheckBoxStock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(2, 2, 2))
                            .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addComponent(jComboStockYear, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel46, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboStockMonth, 0, 155, Short.MAX_VALUE))
                        .addGap(157, 157, 157))
                    .addGroup(trackItemsPanelLayout.createSequentialGroup()
                        .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 777, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(24, 24, 24))))
            .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(trackItemsPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(857, Short.MAX_VALUE)))
        );
        trackItemsPanelLayout.setVerticalGroup(
            trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(trackItemsPanelLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxStock, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jComboStockYear)
                        .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel46, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jComboStockMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10)
                .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(trackItemsPanelLayout.createSequentialGroup()
                        .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboStock, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jtxtStockQty, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 358, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(trackItemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(trackItemsPanelLayout.createSequentialGroup()
                    .addGap(33, 33, 33)
                    .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(486, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("STOCK SUMMARY", trackItemsPanel);

        javax.swing.GroupLayout jPanelStockLayout = new javax.swing.GroupLayout(jPanelStock);
        jPanelStock.setLayout(jPanelStockLayout);
        jPanelStockLayout.setHorizontalGroup(
            jPanelStockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStockLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        jPanelStockLayout.setVerticalGroup(
            jPanelStockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStockLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE))
        );

        jTabbedPaneHome.addTab("INVENTORY SECTION", jPanelStock);

        jPanel4.setBackground(new java.awt.Color(41, 47, 51));

        jPanel10.setBackground(new java.awt.Color(85, 172, 238));
        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jTable_Customer2.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTable_Customer2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "REF NUMBER", "NAME", "TELEPHONE", "TRANSACTION TYPE", "AMOUNT DUE", "AMOUNT PAID"
            }
        ));
        jTable_Customer2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_Customer2MouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(jTable_Customer2);

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

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jTextFieldSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jBtnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 719, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnSearch))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelCustomer2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel12.setBackground(new java.awt.Color(85, 172, 238));
        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(null, "CUSTOMER DETAILS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18)), "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18))); // NOI18N

        jLabel50.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel50.setText("Name ");

        jLabel51.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel51.setText("Phone");

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

        jLabel52.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel52.setText("Customer ID");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel51, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel52, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldPhone, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                    .addComponent(jTextFieldName)
                    .addComponent(jTextFieldId))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel52, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldId))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel50, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(13, 13, 13))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jTextFieldName, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel51, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldPhone, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
                .addGap(6, 6, 6))
        );

        jPanel14.setBackground(new java.awt.Color(85, 172, 238));
        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "REVENUE ESTMATES", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18))); // NOI18N

        jTextFieldTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTotalActionPerformed(evt);
            }
        });

        jLabel53.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel53.setText("Total ");

        jLabel54.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel54.setText("Cost");

        jLabel55.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel55.setText("Item Name");

        jLabel56.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel56.setText("Quantity");

        jComboHiredItems.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboHiredItemsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel55, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel54, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel56, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE))
                    .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldTentQty)
                    .addComponent(jTextFieldCost)
                    .addComponent(jTextFieldTotal)
                    .addComponent(jComboHiredItems, 0, 156, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel55, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboHiredItems, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel56, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldTentQty))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel54, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldCost, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel53, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldTotal, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel15.setBackground(new java.awt.Color(85, 172, 238));
        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ITEM SUMMARY", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18))); // NOI18N

        jLabel57.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel57.setText("Damaged Items");

        jTextFieldDamaged.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDamagedActionPerformed(evt);
            }
        });

        jLabel58.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel58.setText("Missing Items");

        jTextFieldMissing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldMissingActionPerformed(evt);
            }
        });

        jLabel59.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel59.setText("Returned Items");

        jTextFieldRet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldRetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel59, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                    .addComponent(jLabel58, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel57, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jTextFieldDamaged, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                    .addComponent(jTextFieldMissing, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldRet, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap(68, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldDamaged)
                    .addComponent(jLabel57, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldMissing)
                    .addComponent(jLabel58, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldRet)
                    .addComponent(jLabel59, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelCustomer2Layout = new javax.swing.GroupLayout(jPanelCustomer2);
        jPanelCustomer2.setLayout(jPanelCustomer2Layout);
        jPanelCustomer2Layout.setHorizontalGroup(
            jPanelCustomer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCustomer2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelCustomer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(130, 130, 130)
                .addGroup(jPanelCustomer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabelId4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelPhone4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelName4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanelCustomer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelCustomer2Layout.createSequentialGroup()
                    .addContainerGap(536, Short.MAX_VALUE)
                    .addGroup(jPanelCustomer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabelId5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelPhone5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelName5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGap(24, 24, 24)))
        );
        jPanelCustomer2Layout.setVerticalGroup(
            jPanelCustomer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelCustomer2Layout.createSequentialGroup()
                .addGap(230, 230, 230)
                .addComponent(jLabelId4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelName4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelPhone4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(114, 114, 114))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelCustomer2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(73, 73, 73))
            .addGroup(jPanelCustomer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelCustomer2Layout.createSequentialGroup()
                    .addGap(70, 70, 70)
                    .addComponent(jLabelId5, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabelName5, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabelPhone5, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                    .addGap(323, 323, 323)))
        );

        jPanel5.setBackground(new java.awt.Color(85, 172, 238));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "RECORD PAYMENT", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18))); // NOI18N

        jLabel3.setText("REFERENCE NUMBER");

        jLabel6.setText("ENTER AMOUNT");

        jBtnPayment.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jBtnPayment.setText("SAVE");
        jBtnPayment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnPaymentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldRefNum, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jBtnPayment, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextFieldAmount, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldRefNum, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jBtnPayment, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel7.setBackground(new java.awt.Color(85, 172, 238));
        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "MORE OPTIONS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 18))); // NOI18N
        jPanel7.setFont(new java.awt.Font("Ubuntu", 0, 15)); // NOI18N

        jLabel60.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel60.setText("Change Ref No. Status");

        jComboMore.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select--", "Active", "Inactive" }));
        jComboMore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboMoreActionPerformed(evt);
            }
        });

        jLabel61.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel61.setText("Change Transaction Type");

        jComboTrans.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select--", "Receipt", "Invoice" }));
        jComboTrans.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboTransActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel61, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel60, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboTrans, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboMore, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel60, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboMore, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel61, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboTrans, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanelCustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, 434, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelCustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, 564, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(42, 42, 42))
        );

        jTabbedPaneHome.addTab("CLIENT DETAILS", jPanel4);

        jPanelExpense.setBackground(new java.awt.Color(85, 172, 238));

        jPanel9.setBackground(new java.awt.Color(85, 172, 238));
        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18))); // NOI18N

        jLabel62.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel62.setText("Expense Name");
        jLabel62.setPreferredSize(new java.awt.Dimension(118, 21));

        jComboExpName.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Rent", "Utilities", "Taxes", "Salaries", "License" }));
        jComboExpName.setPreferredSize(new java.awt.Dimension(118, 21));
        jComboExpName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboExpNameActionPerformed(evt);
            }
        });

        jLabel47.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel47.setText("Cost   ");

        jTextFieldExpCost.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTextFieldExpCost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldExpCostActionPerformed(evt);
            }
        });

        jLabel48.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel48.setText("Date");

        jLabel63.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel63.setText("Expense Type");
        jLabel63.setPreferredSize(new java.awt.Dimension(118, 21));

        jComboExpType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "- -Select --", "Fixed Expenses", "Recurrent Expenses" }));
        jComboExpType.setPreferredSize(new java.awt.Dimension(118, 21));
        jComboExpType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboExpTypeActionPerformed(evt);
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
                        .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jTextFieldExpDate, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel63, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel62, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel47, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextFieldExpCost, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                            .addComponent(jComboExpName, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboExpType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel63, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboExpType, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel62, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboExpName, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldExpCost, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldExpDate, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(77, 77, 77))
        );

        jPanel16.setBackground(new java.awt.Color(85, 172, 238));
        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 18))); // NOI18N

        jTextFieldJobRef.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTextFieldJobRef.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldJobRefActionPerformed(evt);
            }
        });

        jLabel49.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel49.setText("Enter Reference No.");

        jComboJobName.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "- -Select --", "Fuel", "Cleaning Tents", "Cleaning Chairs", "Cleaning Others", "Airtime", "Wages" }));
        jComboJobName.setPreferredSize(new java.awt.Dimension(118, 21));
        jComboJobName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboJobNameActionPerformed(evt);
            }
        });

        jLabel64.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel64.setText("Expense Name");
        jLabel64.setPreferredSize(new java.awt.Dimension(118, 21));

        jLabel65.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel65.setText("Cost   ");

        jTextFieldJobCost.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jTextFieldJobCost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldJobCostActionPerformed(evt);
            }
        });

        jLabel66.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel66.setText("Date");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel66, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel49))
                        .addGap(18, 18, 18))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel64, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(15, 15, 15))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel65, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(55, 55, 55)))
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldJobCost)
                    .addComponent(jComboJobName, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldJobRef)
                    .addComponent(jTextFieldJobDate, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel49, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldJobRef, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel64, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboJobName, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel65, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldJobCost, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel66, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldJobDate, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel10.setFont(new java.awt.Font("Ubuntu", 1, 24)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("SAVE ALL YOUR EXPENSES HERE");

        jBtnResetExpense.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jBtnResetExpense.setText("RESET");
        jBtnResetExpense.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnResetExpenseActionPerformed(evt);
            }
        });

        jBtnSaveExpense.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jBtnSaveExpense.setText("SAVE");
        jBtnSaveExpense.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSaveExpenseActionPerformed(evt);
            }
        });

        jCheckBoxNorm.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jCheckBoxNorm.setText("NORMAL EXPENSES");
        jCheckBoxNorm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNormActionPerformed(evt);
            }
        });

        jCheckBoxJob.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jCheckBoxJob.setText("EXPENSES PER JOB");
        jCheckBoxJob.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxJobActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelExpenseLayout = new javax.swing.GroupLayout(jPanelExpense);
        jPanelExpense.setLayout(jPanelExpenseLayout);
        jPanelExpenseLayout.setHorizontalGroup(
            jPanelExpenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelExpenseLayout.createSequentialGroup()
                .addGap(491, 491, 491)
                .addComponent(jBtnResetExpense, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)
                .addComponent(jBtnSaveExpense, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(391, 391, 391))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelExpenseLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelExpenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelExpenseLayout.createSequentialGroup()
                        .addGroup(jPanelExpenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelExpenseLayout.createSequentialGroup()
                                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(168, 168, 168))
                            .addGroup(jPanelExpenseLayout.createSequentialGroup()
                                .addComponent(jCheckBoxNorm, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(344, 344, 344)))
                        .addGroup(jPanelExpenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxJob, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(62, 62, 62))
        );
        jPanelExpenseLayout.setVerticalGroup(
            jPanelExpenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelExpenseLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanelExpenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxNorm)
                    .addComponent(jCheckBoxJob))
                .addGap(36, 36, 36)
                .addGroup(jPanelExpenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(42, 42, 42)
                .addGroup(jPanelExpenseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnResetExpense, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnSaveExpense, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(57, Short.MAX_VALUE))
        );

        jTabbedPaneHome.addTab("RECORD EXPENDITURE", jPanelExpense);

        jMenu3.setText("File");

        jMenuItem1.setText("Open");
        jMenu3.add(jMenuItem1);

        jMenuItem3.setText("Inventory");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem3);

        jMenuItem2.setText("Logout");
        jMenu3.add(jMenuItem2);

        jexitOption.setText("Exit");
        jexitOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jexitOptionActionPerformed(evt);
            }
        });
        jMenu3.add(jexitOption);

        jMenuBar2.add(jMenu3);

        jMenu4.setText("Edit");
        jMenuBar2.add(jMenu4);

        setJMenuBar(jMenuBar2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPaneHome, javax.swing.GroupLayout.PREFERRED_SIZE, 1231, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jdesktop, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jdesktop, javax.swing.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPaneHome, javax.swing.GroupLayout.PREFERRED_SIZE, 645, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jexitOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jexitOptionActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jexitOptionActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
        Main_Window inventory = new Main_Window();
        inventory.show(true);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jTabbedPaneHomeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTabbedPaneHomeFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jTabbedPaneHomeFocusGained

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
        showItemInfo(jComboHiredItems.getSelectedItem().toString());
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

    private void jCheckBoxStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxStockActionPerformed
        // TODO add your handling code here:
        if(jCheckBoxStock.isSelected()){
            jComboStockMonth.setEnabled(false);
        }else{
            jComboStockMonth.setEnabled(true);
        }
    }//GEN-LAST:event_jCheckBoxStockActionPerformed

    private void jComboStockMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboStockMonthActionPerformed
        try {
            // TODO add your handling code here:
            if(!jCheckBoxStock.isSelected()){
                getHiredItems(jComboStockYear.getSelectedItem().toString(),jComboStockMonth.getSelectedItem().toString());
            }

        } catch (ParseException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jComboStockMonthActionPerformed

    private void jComboStockYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboStockYearActionPerformed
        // TODO add your handling code here:
        if(jCheckBoxStock.isSelected()){
            getYearlyHiredItems(jComboStockYear.getSelectedItem().toString());
        }else{
            //Do nothing
        }
    }//GEN-LAST:event_jComboStockYearActionPerformed

    private void JTable_StockMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_JTable_StockMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_JTable_StockMouseClicked

    private void jComboStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboStockActionPerformed
        // TODO add your handling code here:
        getSelectedProductsData(jComboStock.getSelectedItem().toString());
    }//GEN-LAST:event_jComboStockActionPerformed

    private void jComboSub2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboSub2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboSub2ActionPerformed

    private void jtxtId1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtxtId1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jtxtId1ActionPerformed

    private void jBtnReset3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnReset3ActionPerformed
        // TODO add your handling code here:
        
        jtxtDate1.setDate(null);
        jtxtNumber1.setText(null);
        jtxtId1.setText(null);
        
    }//GEN-LAST:event_jBtnReset3ActionPerformed

    private void jComboMain1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboMain1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboMain1ActionPerformed

    private void Btn_Last1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_Last1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Btn_Last1ActionPerformed

    private void Btn_First1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_First1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Btn_First1ActionPerformed

    private void Btn_Previous1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_Previous1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Btn_Previous1ActionPerformed

    private void Btn_insert1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_insert1ActionPerformed
        // TODO add your handling code here:
        String refNum = jtxtId1.getText();
        String item_name = jComboMain1.getSelectedItem().toString();
        String condition = jComboSub2.getSelectedItem().toString();
        int qty = Integer.parseInt(jtxtNumber1.getText());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String date = dateFormat.format(jtxtDate1.getDate());
        if(condition.equals("Okay")){
            updateItemsInStock(item_name,qty,date);
        }else{}
        recordReturnedItems(refNum,item_name,condition,qty,date);
        displayCustomerData();
    }//GEN-LAST:event_Btn_insert1ActionPerformed

    private void Btn_Next1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_Next1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Btn_Next1ActionPerformed

    private void JTable_Products1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_JTable_Products1MouseClicked
        // TODO add your handling code here:
        int index = JTable_Products1.getSelectedRow();
        getCustomerDetails(index);
    }//GEN-LAST:event_JTable_Products1MouseClicked

    private void jBtnReset2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnReset2ActionPerformed
        // TODO add your handling code here:
        jtxtId.setText(null);
        jlblImage.setIcon(null);
        jComboSub.setEnabled(true);
        jComboMain.setEnabled(true);
        jtxtName.setText(null);
        jtxtName.setEnabled(true);
        jtxtNumber.setText(null);
        jtxtCost.setText(null);
        jtxtDate.setDate(null);
        jlblImage.setText(null);

    }//GEN-LAST:event_jBtnReset2ActionPerformed

    private void jComboSubActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboSubActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboSubActionPerformed

    private void jComboMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboMainActionPerformed
        // TODO add your handling code here:
        if(jComboMain.isFocusOwner()){
            jtxtName.setEnabled(false);
        }else{
            jtxtName.setEnabled(true);
        }

        if(jComboMain.getSelectedItem().toString().equals("Tents")){
            jComboSub.removeAllItems();
            jComboSub.addItem("50 Sitter Tents");
            jComboSub.addItem("100 sitter Tents");
            jComboSub.addItem("Marque Tents");
            jComboSub.addItem("Tent Flaps");
        }else if(jComboMain.getSelectedItem().toString().equals("Tables")){
            jComboSub.removeAllItems();
            jComboSub.addItem("Round Tables");
            jComboSub.addItem("Rectangular Tables");
            jComboSub.addItem("Serving Tables");
            jComboSub.addItem("Platform Tables");
        }else if(jComboMain.getSelectedItem().toString().equals("Chairs")){
            jComboSub.removeAllItems();
            jComboSub.addItem("Executive Chairs");
            jComboSub.addItem("Plastic Chairs");
            // jComboSub.addItem("Serving Tables");
            //jComboSub.addItem("Platform Tables");
        }else if(jComboMain.getSelectedItem().toString().equals("Covers")){
            jComboSub.removeAllItems();
            jComboSub.addItem("Table Covers");
            jComboSub.addItem("Chair Covers");
        }else{

        }
    }//GEN-LAST:event_jComboMainActionPerformed

    private void Btn_LastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_LastActionPerformed
        pos = getProductList().size()-1;
        showItem(pos);
    }//GEN-LAST:event_Btn_LastActionPerformed

    private void Btn_FirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_FirstActionPerformed
        pos = 0;
        showItem(pos);
    }//GEN-LAST:event_Btn_FirstActionPerformed

    private void Btn_PreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_PreviousActionPerformed
        pos--;
        if(pos<0){
            pos =0;
        }

        showItem(pos);
    }//GEN-LAST:event_Btn_PreviousActionPerformed

    private void Btn_deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_deleteActionPerformed
        Main_Window window = new Main_Window();

        if(!jtxtName.getText().equals("")){
            try {
                Connection con = window.dbConnect();
                String sql = "DELETE FROM items_in_stock WHERE item_id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                int id = Integer.parseInt(jtxtId.getText());
                ps.setInt(1, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(null, "Item deleted form the Database");
                displayProductData();
            } catch (SQLException ex) {
                Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Item Deletion Failed: Please Enter the Correct Product ID");
                displayProductData();
            }
        }else{
            JOptionPane.showMessageDialog(null,"Please enter an ID of the item you wish to Delete");
            displayProductData();
        }
    }//GEN-LAST:event_Btn_deleteActionPerformed

    private void Btn_UpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_UpdateActionPerformed
        Main_Window window = new Main_Window();
        if(checkInput() && jtxtName.getText() != null){
            String UpdateQuery = null;
            PreparedStatement ps = null;
            Connection con = window.dbConnect();

            //Update without image
            if(ImgPath == null){
                try {
                    UpdateQuery = "UPDATE items_in_stock SET item_name=?,item_number=?,item_cost=?,date=? WHERE item_id =? ";
                    ps = con.prepareStatement(UpdateQuery);

                    ps.setString(1,jtxtName.getText());
                    ps.setString(2,jtxtNumber.getText());

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    String addDate = dateFormat.format(jtxtDate.getDate()); //Remember

                    ps.setString(3,jtxtCost.getText());
                    ps.setString(4,addDate);
                    // ps.setInt(4,Integer.parseInt(txt_name.getText()));

                    ps.setInt(5,Integer.parseInt(jtxtId.getText()));

                    //Execute the query
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Data Update Successful");
                    displayProductData();
                } catch (SQLException ex) {
                    Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
                    displayProductData();
                }
            }
            //Update with image
            else{
                try{
                    InputStream img = new FileInputStream(new File(ImgPath));
                    UpdateQuery = "UPDATE items_in_stock SET item_name=?,item_number=?,item_cost=?,date=?,image=? WHERE item_id =? ";
                    ps = con.prepareStatement(UpdateQuery);

                    ps.setString(1,jtxtName.getText());
                    ps.setString(2,jtxtNumber.getText());

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    String addDate = dateFormat.format(jtxtDate.getDate()); //Remember

                    ps.setString(3,jtxtCost.getText());
                    ps.setString(4,addDate);
                    ps.setBlob(5,img);
                    ps.setInt(6,Integer.parseInt(jtxtId.getText()));

                    //Execute the query
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Data Update Successful");
                    displayProductData();

                }catch(Exception ex){
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                    displayProductData();
                }
            }
        }else{
            JOptionPane.showMessageDialog(null, "One or More Fields Are Empty");
            displayProductData();
        }
    }//GEN-LAST:event_Btn_UpdateActionPerformed

    private void Btn_insertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_insertActionPerformed
        String pdtName;
        if(jtxtName.getText().isEmpty()){
            pdtName= jComboSub.getSelectedItem().toString();
            // pdtName = jtxtName.getText();
        }else{
            pdtName= jtxtName.getText();
        }

        Main_Window window =  new Main_Window();

        if(checkInput() && ImgPath !=null){
            try {
                Connection con = window.dbConnect();
                PreparedStatement ps = con.prepareStatement("INSERT INTO items_in_stock (item_name,item_number,item_cost,date,image) VALUES  (?,?,?,?,?)");

                ps.setString(1, pdtName);
                ps.setString(2,jtxtNumber.getText());

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String addDate = dateFormat.format(jtxtDate.getDate());

                ps.setString(3,jtxtCost.getText());
                ps.setString(4, addDate);
                InputStream img = new FileInputStream(new File(ImgPath));
                ps.setBlob(5, img);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(null, "Data Inserted");
                displayProductData();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
                displayProductData();
            }
        }else{
            JOptionPane.showMessageDialog(null, "One or More Fields Are Empty!");
            displayProductData();
        }
    }//GEN-LAST:event_Btn_insertActionPerformed

    private void Btn_NextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_NextActionPerformed
        pos++;

        if(pos>=getProductList().size()){
            pos = getProductList().size()-1;
        }
        showItem(pos);
    }//GEN-LAST:event_Btn_NextActionPerformed

    private void Btn_Choose_ImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn_Choose_ImageActionPerformed
        JFileChooser file = new JFileChooser();
        file.setCurrentDirectory(new File(System.getProperty("user.home")));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.images","jpg","png");
        file.addChoosableFileFilter(filter);

        int result = file.showSaveDialog(null);

        if(result == JFileChooser.APPROVE_OPTION){
            File selectedFile = file.getSelectedFile();
            String path = selectedFile.getAbsolutePath();

            jlblImage.setIcon(ResizeImage(path,null));

            ImgPath = path;
        }else{
            System.out.println("No File Selected");
        }
    }//GEN-LAST:event_Btn_Choose_ImageActionPerformed

    private void JTable_ProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_JTable_ProductsMouseClicked

        jComboMain.setEnabled(false);
        jComboSub.setEnabled(false);
        int index = JTable_Products.getSelectedRow();
        showItem(index);
    }//GEN-LAST:event_JTable_ProductsMouseClicked

    private void jtxtNameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtxtNameKeyTyped
        // TODO add your handling code here:
        jComboMain.setEnabled(false);
        jComboSub.setEnabled(false);
    }//GEN-LAST:event_jtxtNameKeyTyped

    private void jtxtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtxtNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jtxtNameActionPerformed

    private void jtxtNameMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jtxtNameMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_jtxtNameMouseEntered

    private void jBtnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnResetActionPerformed
        // TODO add your handling code here:
        counter=0;
        subTotal=0;
        TotalCost=0;
        itemList.clear();
        jTxtCustomer.setText(null);
        jTxtEmail.setText(null);
        jReceiptArea.setText(null);
        /// jTxtCustId.setText(null);
        jTxtSubTotal.setText(null);
        jTxtNumber.setText(null);
        jTxtCost.setText(null);
        jTxtTotalCost.setText(null);
        TotalCost=0;
        jTxtDiscount.setText(null);
        //  jSelectedArea.setText(null);
        jTxtCustomer.setEnabled(true);
        //jComboMainCat.removeAllItems();
        jTxtPhone.setText(null);
        jTxtCustId.setText(null);
    }//GEN-LAST:event_jBtnResetActionPerformed

    private void jBtnSaveDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSaveDataActionPerformed
        // TODO add your handling code here:
        String name="";
        String id="";
        String telephone="";
        String email = "";
        String refNum="";
        int qty=0;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");  
        LocalDateTime now = LocalDateTime.now();  
        String dateHired=dtf.format(now);  
        String dueDate=invoiceDueDate();

        LinkedHashSet<Data> linkedSet = new LinkedHashSet<>(itemList);

        for (Data data : linkedSet) {
            name=data.customer_name;
            id=data.custId;
            telephone=data.telephone;
            email = data.email;
            refNum=data.refNum;

            // System.out.println(data.item_name+" "+data.item_number+" "+data.item_cost+" "+id);
            recordItemSales(data.item_name,data.item_number, data.item_cost,id);
            deductHiredItems(data.item_number,data.item_name);
            //JOptionPane.showMessageDialog(null, "Your transaction sales have been saved");
        }
        //generateInvoice();
        saveCustomerDetails(name, id, telephone, email,trans_type,refNum);
        recordSalesDetails(id, salesNum, Double.parseDouble(jTxtTotalCost.getText()),trans_type, dueDate,dateHired);
        JOptionPane.showMessageDialog(null, "Your transaction sales have been recorded");
        trans_type=null;
    }//GEN-LAST:event_jBtnSaveDataActionPerformed

    private void jBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnPrintActionPerformed
        try {
            // TODO add your handling code here:
            jReceiptArea.print();
        } catch (PrinterException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jBtnPrintActionPerformed

    private void jTxtEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTxtEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtEmailActionPerformed

    private void jTxtEmailFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTxtEmailFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtEmailFocusGained

    private void jTxtPhoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTxtPhoneActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtPhoneActionPerformed

    private void jTxtPhoneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTxtPhoneFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtPhoneFocusGained

    private void jTxtCustIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTxtCustIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtCustIdActionPerformed

    private void jTxtCustIdFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTxtCustIdFocusGained
        // TODO add your handling code here:
        customerId="CU"+Integer.toString(generateCustomerId());
    }//GEN-LAST:event_jTxtCustIdFocusGained

    private void jTxtCustomerKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtCustomerKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtCustomerKeyTyped

    private void jTxtCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTxtCustomerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtCustomerActionPerformed

    private void jRadioRecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioRecActionPerformed
        // TODO add your handling code here:
        trans_type = "receipt";
        generateReceipt();
    }//GEN-LAST:event_jRadioRecActionPerformed

    private void jRadioInvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioInvActionPerformed
        // TODO add your handling code here:
        trans_type="invoice";
        generateInvoice();
    }//GEN-LAST:event_jRadioInvActionPerformed

    private void jTxtDiscountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTxtDiscountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtDiscountActionPerformed

    private void jTxtTotalCostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTxtTotalCostActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtTotalCostActionPerformed

    private void jBtnTotalCostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnTotalCostActionPerformed
        // TODO add your handling code here:
        int discount=Integer.parseInt(jTxtDiscount.getText());
        double discountShs;
        if(discount<1){
            TotalCost=(subTotal-Tax);
            jTxtTotalCost.setText(Double.toString(TotalCost));
        }else{
            subTotal-=Tax;
            discountShs=(discount/100)*subTotal;
            TotalCost=subTotal-discountShs;
            jTxtTotalCost.setText(Double.toString(TotalCost));
        }
    }//GEN-LAST:event_jBtnTotalCostActionPerformed

    private void jTxtNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTxtNumberActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtNumberActionPerformed

    private void jBtnSubTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSubTotalActionPerformed
        // TODO add your handling code here:
        counter++;
        int quantity=Integer.parseInt(jTxtNumber.getText());
        double hirePrice=Double.parseDouble(jTxtCost.getText());
        String customer_name=jTxtCustomer.getText();
        String item_name=itemName;
        String custId=customerId;
        String refNum = jTxtCustId.getText();
        String telephone=jTxtPhone.getText();
        String email = jTxtEmail.getText();
        int stockQty=getStockItemQty(item_name);
        if(stockQty<quantity){
            JOptionPane.showMessageDialog(null, "You have entered more items than what is available in stock\nPlease "
                + "enter items less or equal to "+stockQty);
        }else{
            double cost=hirePrice*quantity;
            if(counter==1){

                subTotal+=cost;
                jTxtSubTotal.setText(Double.toString(cost));

                Data items = new Data(item_name,quantity,hirePrice,customer_name,custId,telephone,email,refNum);
                itemList.add(items);
            }else{
                //  JOptionPane.showMessageDialog(null, "Item has Already been Added");
            }
        }

        /*
        if(!itemList.isEmpty()){
            for (Data data : itemList) {
                try{
                    if(!data.item_name.equals(items.item_name)){
                        itemList.add(items);
                    }else{
                        JOptionPane.showMessageDialog(null,"Item Already Added to cart");
                    }
                }catch(Exception ex){
                    System.err.println(ex.getMessage());
                }

            }

        }else{
            itemList.add(items);
        } */

        /*
        Iterator itr = itemList.iterator();
        while(itr.hasNext()){
            Data data = (Data)itr.next();
            System.out.println("Name of Item: "+data.item_name);
            System.out.println("Quantity: "+data.item_number);
            System.out.println("Hire Price: "+data.item_cost);
            System.out.println("Name of Customer: "+data.customer_name);
            System.out.println("Id of the Customer: "+data.custId);
        } */
    }//GEN-LAST:event_jBtnSubTotalActionPerformed

    private void jTxtSubTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTxtSubTotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtSubTotalActionPerformed

    private void jTxtCostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTxtCostActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTxtCostActionPerformed

    private void jComboMainCatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboMainCatActionPerformed
        // TODO add your handling code here:
        // getSelectedItem(jComboMainCat.getSelectedItem().toString());
        counter=0; //Reset the counter for every option that is selected
        itemName=jComboMainCat.getSelectedItem().toString();
        showSelectedItemInfo(itemName);
        checkAddedItems(itemName);
    }//GEN-LAST:event_jComboMainCatActionPerformed

    private void jBtnPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnPaymentActionPerformed
        // TODO add your handling code here:
        
        recordInstallmentPayment(Double.parseDouble(jTextFieldAmount.getText()),jTextFieldId.getText());
        
    }//GEN-LAST:event_jBtnPaymentActionPerformed

    private void jComboMoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboMoreActionPerformed
        // TODO add your handling code here:
        try {
            changeRefNumStatus(jComboMore.getSelectedIndex(),jTextFieldId.getText(),jComboMore.getSelectedItem().toString());
        } catch (Exception e) {
        }
        
    }//GEN-LAST:event_jComboMoreActionPerformed

    public void changeRefNumStatus(int index, String cust_id,String status){
        Main_Window main = new Main_Window();
        Connection con = main.dbConnect();
        String query = "UPDATE customers SET refNumStatus =? WHERE custID = ?";
        PreparedStatement ps;
        
        if(!cust_id.isEmpty()){
            try {
                ps = con.prepareStatement(query);
                ps.setInt(1, index);
                ps.setString(2, cust_id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(null, "Status of Reference Number Changed to "+status);
            } catch (SQLException ex) {
                Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }else{
            JOptionPane.showMessageDialog(null, "Please Select a Customer from the table and try again");
        }

    }
    private void jComboTransActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboTransActionPerformed
        // TODO add your handling code here:
        try {
            changeTransType(jComboTrans.getSelectedItem().toString(),jTextFieldId.getText());
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jComboTransActionPerformed

    //Changes the transaction type in the customers table
    public void changeTransType2(String transType, String custID){
        try {
            Main_Window main = new Main_Window();
            Connection con = main.dbConnect();
            
            String query ="UPDATE customers SET transtype = ? WHERE custId =?";
            PreparedStatement ps;
            
            ps = con.prepareStatement(query);
            ps.setString(1, transType);
            ps.setString(2, custID);
            ps.executeUpdate();
            //JOptionPane.showMessageDialog(null, "Transaction status has been changed to "+transType+" type");
            displayCustomerSearchData(jTextFieldSearch.getText());
        } catch (SQLException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    //changes the transaction type in the salestable
    public void changeTransType(String transType,String custID){
        changeTransType2(transType, custID);
        try {
            Main_Window main = new Main_Window();
            Connection con = main.dbConnect();
            
            String query ="UPDATE salestable SET transType = ? WHERE custID =?";
            PreparedStatement ps;
            
            ps = con.prepareStatement(query);
            ps.setString(1, transType);
            ps.setString(2, custID);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Transaction status has been changed to "+transType+" type");
            displayCustomerSearchData(jTextFieldSearch.getText());
        } catch (SQLException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void jComboExpNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboExpNameActionPerformed
        // TODO add your handling code here:        
    }//GEN-LAST:event_jComboExpNameActionPerformed

    private void jTextFieldExpCostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldExpCostActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldExpCostActionPerformed

    private void jBtnResetExpenseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnResetExpenseActionPerformed
        // TODO add your handling code here:
        jTextFieldJobDate.setDate(null);
        jTextFieldJobCost.setText(null);
        jTextFieldJobRef.setText(null);
        jTextFieldExpDate.setDate(null);
        jTextFieldExpCost.setText(null);
    }//GEN-LAST:event_jBtnResetExpenseActionPerformed

    private void jBtnSaveExpenseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSaveExpenseActionPerformed
        // TODO add your handling code here:
        
        String tableName="";
        String date="";
        double cost =0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        if(jCheckBoxNorm.isSelected()){
            
            //String itemArray[jComboExpName.getSelectedIndex()]=jComboExpName.getSelectedItem().toString();
            tableName="normalExpenses";
            cost = Double.parseDouble(jTextFieldExpCost.getText());
            
            date = dateFormat.format(jTextFieldExpDate.getDate());
            
            recordExpense(jComboExpType.getSelectedItem().toString(), jComboExpName.getSelectedItem().toString(),cost, date,tableName);
          
            
        }else if(jCheckBoxJob.isSelected()){
          //  count++;
            tableName="jobExpenses";
            cost = Double.parseDouble(jTextFieldJobCost.getText());
            date = dateFormat.format(jTextFieldJobDate.getDate()); 
            
              recordExpense(jTextFieldJobRef.getText(), jComboJobName.getSelectedItem().toString(),cost, date,tableName);
               // JOptionPane.showMessageDialog(null, "Expense already Saved");
            
        }else{
            JOptionPane.showMessageDialog(null, "Please Select one of the expense types but not both");
        }
        
        
        
    }//GEN-LAST:event_jBtnSaveExpenseActionPerformed

    private void jComboExpTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboExpTypeActionPerformed
        // TODO add your handling code here:
        jComboExpName.removeAllItems();
        if(jComboExpType.getSelectedItem().toString().equals("Fixed Expenses")){
            jComboExpName.addItem("Rent");
            jComboExpName.addItem("Utilities");
            jComboExpName.addItem("Taxes");
            jComboExpName.addItem("Salaries");
            jComboExpName.addItem("License");
        }else if(jComboExpType.getSelectedItem().toString().equals("Recurrent Expenses")){
            jComboExpName.addItem("New Stock");
            jComboExpName.addItem("Cleaning Office");
            jComboExpName.addItem("Airtime");
            jComboExpName.addItem("Office Fuel");
            jComboExpName.addItem("Donations");
            jComboExpName.addItem("Stationery");
            jComboExpName.addItem("Internet Data");
            jComboExpName.addItem("Hiring Stock");
            jComboExpName.addItem("Other");
        }
        
      
       //jComboExpType.removeAllItems();
       //jComboJobName.removeAllItems();
    }//GEN-LAST:event_jComboExpTypeActionPerformed

    private void jTextFieldJobRefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldJobRefActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldJobRefActionPerformed

    private void jComboJobNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboJobNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboJobNameActionPerformed

    private void jTextFieldJobCostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldJobCostActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldJobCostActionPerformed

    private void jCheckBoxNormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNormActionPerformed
        // TODO add your handling code here:
        if(jCheckBoxNorm.isSelected()){
            jCheckBoxJob.setEnabled(false);
        }else{
            jCheckBoxJob.setEnabled(true);
        }
        
    }//GEN-LAST:event_jCheckBoxNormActionPerformed

    private void jCheckBoxJobActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxJobActionPerformed
        // TODO add your handling code here:
        if(jCheckBoxJob.isSelected()){
            jCheckBoxNorm.setEnabled(false);
        }else{
            jCheckBoxNorm.setEnabled(true);
        }
    }//GEN-LAST:event_jCheckBoxJobActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            // TODO add your handling code here:
            exportStockSummary();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    //This function gets sums of damaged items, missing items (Annual and monthly)
    public int[] getAnnualStockData(String itemName){

        int[] itemData = new int[2];
        Date dbDate=null;
        int damaged=0, missing=0;
        try {
            
            Main_Window window = new Main_Window();
            com.mysql.jdbc.Connection con = (com.mysql.jdbc.Connection)window.dbConnect();
            String query = "SELECT * FROM hired_items WHERE item_name='"+itemName+"' AND dateReturned IS NOT NULL";
            //String query2="SELECT totalCost,amountPaid FROM salestable";
            Statement stmt;
            ResultSet rs;
            int year=Integer.parseInt(jComboStockYear.getSelectedItem().toString());
            //Returns a summary of Damaged items, missing items
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            
            Date myDate;
            
            while(rs.next()){
                try {
                   myDate = new SimpleDateFormat("MMMM",Locale.ENGLISH).parse(jComboStockMonth.getSelectedItem().toString());
                   Calendar cal2 = Calendar.getInstance();
                   cal2.setTime(myDate);  
                   
                   dbDate=new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("dateReturned"));
                   
                    //System.out.println("Date DB "+dbDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dbDate);  

                    //System.out.println("DB MONTH "+cal.get(Calendar.MONTH)+"AND MY MONTH "+month);
                    if(cal.get(Calendar.YEAR)==year && jCheckBoxStock.isSelected()){
                        damaged+=rs.getInt("damaged_items");
                        missing+=rs.getInt("missing_items");

                    }else if(cal.get(Calendar.YEAR)==year && !jCheckBoxStock.isSelected() && cal2.get(Calendar.MONTH)==cal.get(Calendar.MONTH)){
                        damaged+=rs.getInt("damaged_items");
                        missing+=rs.getInt("missing_items");
                    }else{
                    //Do nothing 
                    }                
                   
                } catch (ParseException ex) {
                    Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
                }            
                
            }
            itemData[0]=damaged;
            itemData[1]=missing;
        } catch (SQLException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return itemData;
    }
    
    public void exportStockSummary() throws FileNotFoundException{
        Calendar now = Calendar.getInstance();
        int month=0;
        int year=0;

        //Create a blank workbook
        XSSFWorkbook workBook = new XSSFWorkbook();
        ArrayList<Data>list = new ArrayList<>();
        Main_Window window = new Main_Window();
        com.mysql.jdbc.Connection con = (com.mysql.jdbc.Connection)window.dbConnect();
        String query = "SELECT * FROM items_in_stock";
        //String query2="SELECT totalCost,amountPaid FROM salestable";
        Statement stmt;
        ResultSet rs;
        int[] itemData= new int[2];
        
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            Data stockInfo;
            Date dbDate;
            
            list.clear();
                year=Integer.parseInt(jComboStockYear.getSelectedItem().toString());
                while(rs.next()){
                    itemData=getAnnualStockData(rs.getString("item_name"));
                    
                    stockInfo=new Data(rs.getString("item_name"),rs.getInt("item_number"),itemData[0],itemData[1]);
                    list.add(stockInfo);
                   //System.out.println(rs.getString("item_name")+" "+rs.getInt("item_number")+" "+itemData[0]+" "+itemData[1]);
                }
            
            
        } catch (SQLException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }


        
            //Create a array of strings containing column names
            String[] columNames={"STOCK ITEM","QUANTITY AVAILABLE","DAMAGED ITEMS","MISSING ITEMS"};

            //Create a blank sheet
            XSSFSheet spreadsheet = workBook.createSheet( " Stock Summary Report");

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

            row.createCell(0).setCellValue(data.itemName);

            row.createCell(1).setCellValue(data.currStock);

            row.createCell(2).setCellValue(data.damagedItems);

            row.createCell(3).setCellValue(data.missingItems);
            }

            // Resize all columns to fit the content size
            for(int i = 0; i < columNames.length; i++) {
            spreadsheet.autoSizeColumn(i);
            }

            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(new File("/home/benjamin/Desktop/Stock Summary Report.xlsx"));
            
            try {
            workBook.write(out);
            out.close();
            workBook.close();
            JOptionPane.showMessageDialog(null, "Data has been exported Successfully");
            } catch (IOException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
            }       
        
        
    }
    public void recordInstallmentPayment(double payment, String cust_id){
        amountPaid+=payment;
       // System.out.println("Amount "+amountPaid);
        try {
            Main_Window main = new Main_Window();
            Connection conn = main.dbConnect();
            
            String query ="UPDATE salestable SET amountPaid = ? WHERE custID =?";
            PreparedStatement ps;
            
            ps = conn.prepareStatement(query);
            ps.setDouble(1, amountPaid);
            ps.setString(2, cust_id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Payment has been successfully Recorded");
            displayCustomerSearchData(jTextFieldSearch.getText());
        } catch (SQLException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
            displayCustomerSearchData(jTextFieldSearch.getText());
        }
        amountPaid=0;
    }
    //This function returns the quantity of each hired item from the inventory
    public int getStockItemQty(String item_name){
        int qty=0;
        Main_Window main = new Main_Window();
        Connection con = main.dbConnect();
        
        String query ="SELECT item_number FROM items_in_stock WHERE item_name ='"+item_name+"'";
        Statement stmt;
        ResultSet rs; 
        
        try {
            stmt = con.createStatement();
            rs=stmt.executeQuery(query);
            
            while(rs.next()){
                qty=rs.getInt("item_number");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return qty;
    }
    
    //This function deducts hired items from the items in stock
    public void deductHiredItems(int qty, String item_name){
        try {
            int qtyUpdate =getStockItemQty(item_name)-qty;
            Main_Window main = new Main_Window();
            Connection con = main.dbConnect();
            
            String query ="UPDATE items_in_stock set item_number = ? WHERE item_name =?";
            PreparedStatement ps;
            
            ps = con.prepareStatement(query);
            ps.setInt(1, qtyUpdate);
            ps.setString(2, item_name);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //load the JComboStock with Items
    public void loadJComboStockItems(){
        ArrayList<Products> list = getProductList();
        
        for(int i=0;i<list.size();i++){
            //row[0]=list.get(i).getId();

           // jComboStock.addItem(list.get(i).getName());
        }
        
    
    }
    public void recordExpense(String paramOne,String expenseName, double expenseCost,String date,String tableName){
            try {
                
                Main_Window window = new Main_Window();
                Connection conn= window.dbConnect();
                PreparedStatement ps= null;
                if(tableName.equals("normalExpenses")){
                    ps = conn.prepareStatement("INSERT INTO normalExpenses (expenseType,expenseName,expenseCost,dateRecorded) VALUES  (?,?,?,?)");
                }else{
                    ps = conn.prepareStatement("INSERT INTO jobExpenses (refNum,expenseName,expenseCost,dateRecorded) VALUES  (?,?,?,?)");
                }
                
                ps.setString(1, paramOne);
                ps.setString(2, expenseName);
                ps.setDouble(3, expenseCost);
                ps.setString(4, date);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(null, "Expenses Saved");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
                Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
            }    
    
    }
    
    String customerId="";    public int generateCustomerId(){
        int val=0;
        Main_Window main = new Main_Window();
        Connection con = main.dbConnect();
        String query = "SELECT COUNT(id)+1 FROM salestable";
        Statement stmt;
        ResultSet rs;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            
            while(rs.next()){
                val=Integer.parseInt(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return val;
    }    
//Retrieve products data
    public ArrayList<Products> getProductList(){
     Main_Window window = new Main_Window();       
            ArrayList<Products> productList = new ArrayList<>();
            Connection con = window.dbConnect();
            
            String query = "SELECT * FROM items_in_stock";
            Statement stmt;
            ResultSet rs;
        try {
                        
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            Products product;
            
            while(rs.next()){
                product = new Products(rs.getInt("item_id"),rs.getString("item_name"),rs.getInt("item_number"),rs.getDouble("item_cost"),rs.getString("date"),rs.getBytes("image"));
                productList.add(product);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
        return productList;
    }
    
    public void setComponentColours(){
        /*        JTable_Products.setBackground(Color.decode("#8B9DC3"));
        JTable_Products.setRowHeight(30);
        jtxtDate.setBackground(Color.decode("#8B9DC3"));
        jtxtCost.setBackground(Color.decode("#8B9DC3"));
        jtxtNumber.setBackground(Color.decode("#8B9DC3"));
        jtxtName.setBackground(Color.decode("#8B9DC3"));
        jComboSub.setBackground(Color.decode("#8B9DC3"));
        jComboMain.setBackground(Color.decode("#8B9DC3"));
        jtxtId.setBackground(Color.decode("#8B9DC3"));*/
    }
    
    //Display the retrieved data in a JTable
    public void displayProductData(){
        ArrayList<Products> list = getProductList();
        DefaultTableModel model = (DefaultTableModel)JTable_Products.getModel();
        
        
        //Clear the JTable 
        model.setRowCount(0);
        Object[] row = new Object[5];
        
        for(int i=0;i<list.size();i++){
            //row[0]=list.get(i).getId();
            row[0]=list.get(i).getName();
            row[1]=list.get(i).getPrice();
            row[3]=list.get(i).getAddDate();
            row[2]=list.get(i).getNumber();
            model.addRow(row);
        }
    }    
    
    public void showItem(int index){
        jtxtName.setText(getProductList().get(index).getName());
        jtxtCost.setText(Double.toString(getProductList().get(index).getPrice()));
        jtxtId.setText(Integer.toString(getProductList().get(index).getId()));
        jtxtNumber.setText(Integer.toString(getProductList().get(index).getNumber()));
       // jComboBox.setSelectedItem(getProductList().get(index).getCategory());
        
        
        Date addDate = null;
        try {
            addDate = new SimpleDateFormat("dd-MM-yyyy").parse(getProductList().get(index).getAddDate());
            jtxtDate.setDate(addDate);
        } catch (ParseException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
        jlblImage.setIcon(ResizeImage(null,getProductList().get(index).getImage()));
        
    }
    
    //Resize Image
    
    public ImageIcon ResizeImage(String imagePath, byte[] pic){
        ImageIcon myImage = null;
        
        if(imagePath !=null){
            myImage = new ImageIcon(imagePath);
        }else{
            myImage = new ImageIcon(pic);
        }
        
        Image img = myImage.getImage();
        Image img2 = img.getScaledInstance(jlblImage.getWidth(),jlblImage.getHeight(), Image.SCALE_SMOOTH);
        ImageIcon image = new ImageIcon(img2);
        return image;
    }
    
    public boolean checkInput(){
        if(
            jtxtCost.getText() == null ||
            jtxtNumber.getText() == null ||
            jtxtDate.getDate() == null      
          ){
            return false;
           }else{
            try{
                Double.parseDouble(jtxtCost.getText());
                return true;
            }catch(Exception ex){
                return false;
            }
        }
    }    
    
    //This updates the stock by adding the returned items to the new items
    public void updateItemsInStock(String item_name,int qty,String date){
          // Add the returned stock to the available stock
            int qtyUpdate=getStockItemQty(item_name)+qty;
            Main_Window window = new Main_Window();
            Connection con = window.dbConnect();
            String UpdateQuery = null;
            PreparedStatement ps = null;
 
                try { 
                    UpdateQuery = "UPDATE items_in_stock SET item_number=?,date=? WHERE item_name =?";
                    ps = con.prepareStatement(UpdateQuery);
                    
                    ps.setInt(1,qtyUpdate);
                    ps.setString(2,date);
                    
                    ps.setString(3,item_name);
                   // ps.setInt(4,Integer.parseInt(txt_name.getText()));
                    
                    //Execute the query
                    ps.executeUpdate(); 

                    JOptionPane.showMessageDialog(null, "Items in stock have been successfully updated");
                    displayProductData();
                } catch (SQLException ex) {
                    Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
                    displayProductData();
                }    
    }
    public void getYearlyHiredItems(String yr){
        Date dbDate;
        String item_name="";
        int damagedItems = 0,missingItems=0;
      
        Main_Window window = new Main_Window();
        Connection con = window.dbConnect();
        ArrayList<Products> list = getProductList();
        
        Statement stmt;
        ResultSet rs;
        SimpleDateFormat date=new SimpleDateFormat("dd-MM-yyyy",Locale.ENGLISH);  
        DefaultTableModel model3 = (DefaultTableModel) JTable_Stock.getModel();
        Object[] row = new Object[3];
        model3.setRowCount(0);
        for(int i=0;i<list.size();i++){
            item_name=list.get(i).getName();
            try {
            
            String query ="SELECT * FROM hired_items WHERE item_name='"+item_name+"' AND dateReturned IS NOT NULL";
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
              dbDate=date.parse(rs.getString("dateReturned"));
                Calendar cal = Calendar.getInstance();
                cal.setTime(dbDate);

                if(cal.get(Calendar.YEAR)==Integer.parseInt(yr)){
                    damagedItems+=rs.getInt("damaged_items");
                    missingItems+=rs.getInt("missing_items");
                } 
            // System.out.print(rs.getString("dateReturned"));
            }

            } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
            }
           
            row[0]=item_name;
            row[1]=damagedItems;
            row[2]=missingItems;
            
            model3.addRow(row);
            damagedItems=0;
            missingItems=0;
        }
            
}
    
    //This function gets data from the hireditems table
    public void getHiredItems(String year, String month) throws ParseException, ParseException{
        Date dbDate;
        String item_name="";
        int damagedItems = 0,missingItems=0;
      
        Main_Window window = new Main_Window();
        Connection con = window.dbConnect();
        ArrayList<Products> list = getProductList();
        
        Statement stmt;
        ResultSet rs;
        SimpleDateFormat date=new SimpleDateFormat("dd-MM-yyyy",Locale.ENGLISH); 
        Date myDate=null;
        try {
            myDate = new SimpleDateFormat("MMMM",Locale.ENGLISH).parse(month);
        } catch (ParseException ex) {
            Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
        }
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(myDate);
                 
        DefaultTableModel model3 = (DefaultTableModel) JTable_Stock.getModel();
        Object[] row = new Object[3];
        model3.setRowCount(0);
        for(int i=0;i<list.size();i++){
            item_name=list.get(i).getName();
            try {
            
            String query ="SELECT * FROM hired_items WHERE item_name='"+item_name+"' AND dateReturned IS NOT NULL";
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
              dbDate=date.parse(rs.getString("dateReturned"));
                Calendar cal = Calendar.getInstance();
                cal.setTime(dbDate);

                if(cal.get(Calendar.YEAR)==Integer.parseInt(year) && cal2.get(Calendar.MONTH)==cal.get(Calendar.MONTH)){
                    damagedItems+=rs.getInt("damaged_items");
                    missingItems+=rs.getInt("missing_items");
                } 
            // System.out.print(rs.getString("dateReturned"));
            }

            } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(Events_Home.class.getName()).log(Level.SEVERE, null, ex);
            }
           
            row[0]=item_name;
            row[1]=damagedItems;
            row[2]=missingItems;
            
            model3.addRow(row);
            damagedItems=0;
            missingItems=0;
        }        
    }
    public void recordReturnedItems(String refNum,String item,String condition,int qty,String date){
        
        String UpdateQuery = null;
            PreparedStatement ps = null;
            Main_Window window = new Main_Window();
            Connection con = window.dbConnect();
 
        
        if(condition.equals("Okay")){
                try { 
                    UpdateQuery = "UPDATE hired_items SET returned_items=?,dateReturned=? WHERE custId =? AND item_name=?";
                    ps = con.prepareStatement(UpdateQuery);
                    
                    ps.setInt(1,qty);
                    ps.setString(2,date);
                    
                    ps.setString(3,refNum);
                    ps.setString(4,item);
                   // ps.setInt(4,Integer.parseInt(txt_name.getText()));
                    
                    //Execute the query
                    ps.executeUpdate(); 

                    JOptionPane.showMessageDialog(null, "Data Update Successful");
                    displayProductData();
                } catch (SQLException ex) {
                    Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
                    displayProductData();
                }
        }else if(condition.equals("Damaged")){
                            try { 
                    UpdateQuery = "UPDATE hired_items SET damaged_items=?,dateReturned=? WHERE custId =? AND item_name=?";
                    ps = con.prepareStatement(UpdateQuery);
                    
                    ps.setInt(1,qty);
                    ps.setString(2,date);
                    
                    ps.setString(3,refNum);
                    ps.setString(4,item);
                   // ps.setInt(4,Integer.parseInt(txt_name.getText()));
                    
                    //Execute the query
                    ps.executeUpdate(); 

                    JOptionPane.showMessageDialog(null, "Data Update Successful");
                    displayProductData();
                } catch (SQLException ex) {
                    Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
                    displayProductData();
                }
        }else if(condition.equals("Missing")){
                try { 
                    UpdateQuery = "UPDATE hired_items SET missing_items=?,dateReturned=? WHERE custId =? AND item_name=?";
                    ps = con.prepareStatement(UpdateQuery);
                    
                    ps.setInt(1,qty);
                    ps.setString(2,date);
                    
                    ps.setString(3,refNum);
                    ps.setString(4,item);
                   // ps.setInt(4,Integer.parseInt(txt_name.getText()));
                    
                    //Execute the query
                    ps.executeUpdate(); 

                    JOptionPane.showMessageDialog(null, "Data Update Successful");
                    displayProductData();
                } catch (SQLException ex) {
                    Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
                    displayProductData();
                }        
        }
    }
    public ArrayList<CustomerData> getCustomerList(){
            
            ArrayList<CustomerData> customerList = new ArrayList<>();
            Main_Window window = new Main_Window();
            Connection con = window.dbConnect();
            double amountPaid=0,amountDue=0,totalCost=0;
            String query = "SELECT * FROM customers";
            Statement stmt;
            ResultSet rs;
        try {
                        
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            CustomerData customer;
            
            while(rs.next()){
                customer = new CustomerData(rs.getInt("Id"),rs.getString("custId"),rs.getString("custName"),rs.getString("Telephone"),rs.getString("transType"),rs.getDate("hireDate"),rs.getString("refNum"));
                customerList.add(customer);
               // System.out.println(rs.getString("custName"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
        return customerList;
    }    
    
    public void displayCustomerData(){
        ArrayList<CustomerData> list = getCustomerList();
        //DefaultTableModel model = (DefaultTableModel)jTable_Customer.getModel();
        DefaultTableModel model2= (DefaultTableModel)JTable_Products1.getModel();
        
        //Clear the JTables 
        //model.setRowCount(0);
        model2.setRowCount(0);
        
        //Object[] row = new Object[5];
        Object[] row2 = new Object[5];
        
        for(int i=0;i<list.size();i++){
            //row[0]=list.get(i).getId();
           // row[0]=list.get(i).getCustId();
          //  row[1]=list.get(i).getCustName();
          //  row[2]=list.get(i).getTelephone();
          //  row[3]=list.get(i).getTransType();
            //System.out.println("Here"+list.get(i).getCustName());
            
           // row2[0]=list.get(i).getId();
            row2[0]=list.get(i).getCustId();
            row2[1]=list.get(i).getRefNumber();
            row2[2]=list.get(i).getCustName();
            row2[3]=list.get(i).getHireDate();
            
           //model.addRow(row);
           model2.addRow(row2);
        }
    }  
    //This displays data in the Jtable in the inventory section{record returned items tab}
    public void getCustomerDetails(int index){
        try {
            jtxtId1.setText(getCustomerList().get(index).getCustId());
        } catch (Exception e) {
        }
        
        String custId = jtxtId1.getText();
        
        
        Main_Window window = new Main_Window();
        Connection con = window.dbConnect();
           String query = "SELECT item_name FROM hired_items WHERE custId='"+custId+"'";
      
            PreparedStatement ps;
            ResultSet rs;
        try {
            
            ps = con.prepareStatement(query);
            rs=ps.executeQuery();                         
            double total=0;
          while(rs.next()){
                //System.out.println(rs.getString("id")+" "+rs.getString("")+" "+rs.getFloat(2)+" "+rs.getString(3)+" "+rs.getString(4));
                jComboMain1.addItem(rs.getString("item_name"));
          }
         // jTextFieldTotal1.setText(Double.toString(total-Tax));
        } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }

   public void showCustomerDetails(int index){
        //Set initial values for the items
        jTextFieldCost.setText("0");
        jTextFieldTentQty.setText("0");
        
        jTextFieldId.setText(customerList.get(index).getCustId());
        jTextFieldName.setText(customerList.get(index).getCustName());
        jTextFieldPhone.setText(customerList.get(index).getTelephone());
        jTextFieldRefNum.setText(customerList.get(index).getRefNumber());
        //String transType=customerList.get(index).getTransType();
        String custId = jTextFieldId.getText();
        
        ArrayList<CustomerData> salesList=getCustomerPaymentInfo(custId);
        //System.out.println(salesList);
        for(int j=0;j<salesList.size();j++){
            amountPaid=salesList.get(j).getAmountPaid();
        }

        
        Main_Window main = new Main_Window();
        Connection con = (Connection)main.dbConnect();
           String query = "SELECT * FROM hired_items WHERE custId='"+custId+"'";
      
            PreparedStatement ps;
            ResultSet rs;
        try {
            
            ps = con.prepareStatement(query);
            rs=ps.executeQuery();                         
          while(rs.next()){
                //System.out.println(rs.getString("id")+" "+rs.getString("")+" "+rs.getFloat(2)+" "+rs.getString(3)+" "+rs.getString(4));
                jComboHiredItems.addItem(rs.getString("item_name"));
          }
        } catch (SQLException ex) {
            Logger.getLogger(Main_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            java.util.logging.Logger.getLogger(Events_Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Events_Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Events_Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Events_Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Events_Home().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Btn_Choose_Image;
    private javax.swing.JButton Btn_First;
    private javax.swing.JButton Btn_First1;
    private javax.swing.JButton Btn_Last;
    private javax.swing.JButton Btn_Last1;
    private javax.swing.JButton Btn_Next;
    private javax.swing.JButton Btn_Next1;
    private javax.swing.JButton Btn_Previous;
    private javax.swing.JButton Btn_Previous1;
    private javax.swing.JButton Btn_Update;
    private javax.swing.JButton Btn_delete;
    private javax.swing.JButton Btn_insert;
    private javax.swing.JButton Btn_insert1;
    private javax.swing.JTable JTable_Products;
    private javax.swing.JTable JTable_Products1;
    private javax.swing.JTable JTable_Stock;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JPanel editItemsPanel;
    private javax.swing.JButton jBtnPayment;
    private javax.swing.JButton jBtnPrint;
    private javax.swing.JButton jBtnReset;
    private javax.swing.JButton jBtnReset2;
    private javax.swing.JButton jBtnReset3;
    private javax.swing.JButton jBtnResetExpense;
    private javax.swing.JButton jBtnSaveData;
    private javax.swing.JButton jBtnSaveExpense;
    private javax.swing.JButton jBtnSearch;
    private javax.swing.JButton jBtnSubTotal;
    private javax.swing.JButton jBtnTotalCost;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBoxJob;
    private javax.swing.JCheckBox jCheckBoxNorm;
    private javax.swing.JCheckBox jCheckBoxStock;
    private javax.swing.JComboBox<String> jComboExpName;
    private javax.swing.JComboBox<String> jComboExpType;
    private javax.swing.JComboBox<String> jComboHiredItems;
    private javax.swing.JComboBox<String> jComboJobName;
    private javax.swing.JComboBox<String> jComboMain;
    private javax.swing.JComboBox<String> jComboMain1;
    private javax.swing.JComboBox<String> jComboMainCat;
    private javax.swing.JComboBox<String> jComboMore;
    private javax.swing.JComboBox<String> jComboStock;
    private javax.swing.JComboBox<String> jComboStockMonth;
    private javax.swing.JComboBox<String> jComboStockYear;
    private javax.swing.JComboBox<String> jComboSub;
    private javax.swing.JComboBox<String> jComboSub2;
    private javax.swing.JComboBox<String> jComboTrans;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
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
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelId4;
    private javax.swing.JLabel jLabelId5;
    private javax.swing.JLabel jLabelName4;
    private javax.swing.JLabel jLabelName5;
    private javax.swing.JLabel jLabelPhone4;
    private javax.swing.JLabel jLabelPhone5;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelCustomer2;
    private javax.swing.JPanel jPanelExpense;
    private javax.swing.JPanel jPanelStock;
    private javax.swing.JRadioButton jRadioInv;
    private javax.swing.JRadioButton jRadioRec;
    private javax.swing.JTextArea jReceiptArea;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPaneHome;
    private javax.swing.JTable jTable_Customer2;
    private javax.swing.JTextField jTextFieldAmount;
    private javax.swing.JTextField jTextFieldCost;
    private javax.swing.JTextField jTextFieldDamaged;
    private javax.swing.JTextField jTextFieldExpCost;
    private com.toedter.calendar.JDateChooser jTextFieldExpDate;
    private javax.swing.JTextField jTextFieldId;
    private javax.swing.JTextField jTextFieldJobCost;
    private com.toedter.calendar.JDateChooser jTextFieldJobDate;
    private javax.swing.JTextField jTextFieldJobRef;
    private javax.swing.JTextField jTextFieldMissing;
    private javax.swing.JTextField jTextFieldName;
    private javax.swing.JTextField jTextFieldPhone;
    private javax.swing.JTextField jTextFieldRefNum;
    private javax.swing.JTextField jTextFieldRet;
    private javax.swing.JTextField jTextFieldSearch;
    private javax.swing.JTextField jTextFieldTentQty;
    private javax.swing.JTextField jTextFieldTotal;
    private javax.swing.JTextField jTxtCost;
    private javax.swing.JTextField jTxtCustId;
    private javax.swing.JTextField jTxtCustomer;
    private javax.swing.JTextField jTxtDiscount;
    private javax.swing.JTextField jTxtEmail;
    private javax.swing.JTextField jTxtNumber;
    private javax.swing.JTextField jTxtPhone;
    private javax.swing.JTextField jTxtSubTotal;
    private javax.swing.JTextField jTxtTotalCost;
    private javax.swing.JDesktopPane jdesktop;
    private javax.swing.JMenuItem jexitOption;
    private javax.swing.JLabel jlblImage;
    private javax.swing.JTextField jtxtCost;
    private com.toedter.calendar.JDateChooser jtxtDate;
    private com.toedter.calendar.JDateChooser jtxtDate1;
    private javax.swing.JTextField jtxtId;
    private javax.swing.JTextField jtxtId1;
    private javax.swing.JTextField jtxtName;
    private javax.swing.JTextField jtxtNumber;
    private javax.swing.JTextField jtxtNumber1;
    private javax.swing.JTextField jtxtStockQty;
    private javax.swing.JPanel retItemsPanel;
    private javax.swing.JPanel trackItemsPanel;
    // End of variables declaration//GEN-END:variables
}
