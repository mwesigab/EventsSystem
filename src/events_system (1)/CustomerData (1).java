/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package events_system;

import java.util.Date;

/**
 *
 * @author benjamin
 */
public class CustomerData {
    private String custId,name,telephone,trans_type,refNum,customerId;
    private int id;
    private Date hireDate;
    private double amountPaid,amountDue,totalCost;
    
    public CustomerData(int id,String custId,String name, String telephone, String trans_type,Date hireDate,String refNum){
        this.custId=custId;
        this.id=id;
        this.name=name;
        this.telephone=telephone;
        this.trans_type=trans_type;
        this.hireDate = hireDate;
        this.refNum = refNum;
    }
    
    public CustomerData(int id,String customerId, double totalCost,double amountPaid, double amountDue){
        this.id=id;
        this.customerId = customerId;
        this.amountDue=amountDue;
        this.amountPaid=amountPaid;
        this.totalCost=totalCost;
    }

    public String getCustomerId() {
        return customerId;
    }
    
    public Double getTotalCost(){
        return totalCost;
    }
    public Double getAmountPaid(){
        return amountPaid;
    }
    public Double getAmountDue(){
  
        return amountDue;
    }
    public String getRefNumber(){
        return refNum;
    }
    public int getId(){
        return id;
    }
    public Date getHireDate(){
        return hireDate;
    }
    public String getCustId(){
        return custId;
    }
    
    public String getCustName(){
        return name;
    }
    public String getTelephone(){
        return telephone;
    }
    
    public String getTransType(){
        return trans_type;
    }
}
