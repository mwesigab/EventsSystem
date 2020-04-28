/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package events_system;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benjamin
 */
public class Data {
    String item_name;
    int item_number,attendantID,currStock,damagedItems,missingItems;
    double item_cost,cost,amountPaid,exAmount, expenseCost;
    String customer_name,expenseName,dateRecorded,custId,telephone,invoiceNum,dueDate,itemName;
    Date date;
    String userName,password,firstName,lastName,dateAdded,email,refNum,custName,dateHired;
    public Data(String item_name, int item_number, double item_cost, String customer_name, String custId,String telephone,String email,String refNum){
        this.item_name=item_name;
        this.item_number=item_number;
        this.item_cost=item_cost;
        this.customer_name=customer_name;
        this.custId=custId;
        this.telephone=telephone;
        this.email = email;
        this.refNum = refNum;
    }
    
    public Data (Date date, double cost){
        this.date=date;
        this.cost=cost;
    }
    
    public Data (int attendantID,String userName,String password, String firstName, String lastName, String dateAdded){
        this.attendantID=attendantID;
        this.userName=userName;
        this.password=password;
        this.firstName=firstName;
        this.lastName=lastName;
        this.dateAdded=dateAdded;
    }
    //holds revenue data that is exported to Excel
    public Data (String refNum, String custName, double amountPaid, double exAmount,String dateHired){
        this.exAmount=exAmount;
        this.amountPaid=amountPaid;
        this.custName=custName;
        this.refNum=refNum;
        this.dateHired=dateHired;
}    
    //holds job expenses and exports them to excel
    public Data (String refNum, String expenseName,double expenseCost, String dateRecorded){
        this.refNum=refNum;
        this.expenseName=expenseName;
        this.expenseCost=expenseCost;
        this.dateRecorded=dateRecorded;
    }
    
    //holds normal expenses and exports them to excel
  /*  public Data (String expenseType,String expenseName,double expenseCost, String dateRecorded){
    
    }*/
    
    public Data(String itemName,int currStock, int damagedItems,int missingItems){
        this.itemName=itemName;
        this.currStock=currStock;
        this.damagedItems=damagedItems;
        this.missingItems=missingItems;
    }
    
}
