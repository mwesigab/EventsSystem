/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package events_system;

/**
 *
 * @author Ben
 */
public class Products {
    private int id;
    private String name;
    private int number;
    private double price;
    private String addDate;
    private byte[] picture;
    
    public Products(int id,String name,int number,double price,String addDate,byte[] img){
        this.id=id;
        this.name=name;
        this.price=price;
        this.addDate=addDate;
        this.number=number;
        this.picture=img;
    }
    
    public int getId(){
        return id;
    }
    
    public String getName(){
        return name;
    }
    
    public double getPrice(){
        return price;
    }
    
    public int getNumber(){
        return number;
    }
    public String getAddDate(){
        return addDate;
    }
    
    public byte[] getImage(){
        return picture;
    }
}
