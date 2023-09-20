/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pos.mvc.assignment.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import pos.mvc.assignment.db.DBConnection;
import pos.mvc.assignment.model.OrderDetailModel;
import pos.mvc.assignment.model.OrderModel;

/**
 *
 * @author Harsha
 */
public class OrderController {

    public String placeOrder(OrderModel orderModel, ArrayList<OrderDetailModel> orderDetailModels) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
         try {
            connection.setAutoCommit(false);
            String orderQuery = "INSERT INTO orders VALUES(?,?,?)";
            PreparedStatement statementForOrder = connection.prepareStatement(orderQuery);
            statementForOrder.setString(1, orderModel.getOrderId());
            statementForOrder.setString(2, orderModel.getOrderDate());
            statementForOrder.setString(3, orderModel.getCustomerId());
            
            if(statementForOrder.executeUpdate()>0){
                String orderDetailQuery = "INSERT INTO orderdetail VALUES(?,?,?,?)";
                
                boolean isOrderdDetailSaved = true;
                for (OrderDetailModel orderDetailModel : orderDetailModels) {
                    PreparedStatement statementForOrderDetail = connection.prepareStatement(orderDetailQuery);
                    statementForOrderDetail.setString(1, orderModel.getOrderId());
                    statementForOrderDetail.setString(2, orderDetailModel.getItemCode());
                    statementForOrderDetail.setInt(3, orderDetailModel.getQty());
                    statementForOrderDetail.setDouble(4, orderDetailModel.getDiscount()); 
                    if(!(statementForOrderDetail.executeUpdate()>0)){
                        isOrderdDetailSaved = false;
                    }
                }
                if(isOrderdDetailSaved){
                    String itemQuery = "UPDATE item SET QtyOnHand = QtyOnHand - ? WHERE ItemCode=?";
                    
                    boolean isItemUpdated = true;
                    for (OrderDetailModel orderDetailModel : orderDetailModels) {
                        PreparedStatement statementForItem = connection.prepareStatement(itemQuery);
                        statementForItem.setInt(1, orderDetailModel.getQty());
                        statementForItem.setString(2, orderDetailModel.getItemCode());
                        
                        if(!(statementForItem.executeUpdate()>=0)){
                            isItemUpdated = false;
                        }
                    }
                    if(isItemUpdated){
                        connection.commit();
                        return "Succes";
                    }else{
                        connection.rollback();
                        return "Item Update Error";
                    }
                    
                    
                }else{
                    connection.rollback();
                    return "OrderDetail Save Error";
                }
                
            }else{
                connection.rollback();
                return "Order Save Error";
            }
            
        } catch (Exception e) {
            connection.rollback();
            e.printStackTrace();
            return e.getMessage();
        }finally{
            connection.setAutoCommit(true);
        }
    }
    
}
