package com.broker.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConnectionFactory {


    public static Connection orderConn;
    public static Connection memberConn;
    public static Connection brokerConn;

    private static PreparedStatement queryMemberStatement;
    private static PreparedStatement countOrderStatement;
    private static PreparedStatement insertOrderStatement;
    private static PreparedStatement insertMemberStatement;
    private static PreparedStatement initBrokerStatement;
    private static PreparedStatement updateOrderStatement;

    private static String url = "jdbc:mysql://192.168.85.13:3306/brokers?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String pwd = "Caonima@123";

    public static void main(String[] args) {

        getCountOrderStatement();
    }

    static {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            orderConn = DriverManager.getConnection(url, user, pwd);
            memberConn = DriverManager.getConnection(url, user, pwd);
            brokerConn = DriverManager.getConnection(url, user, pwd);

            String queryMember = "select pid from member where member_id = ? ";
            queryMemberStatement = memberConn.prepareStatement(queryMember);

            String countOrder = "select count(*) from order where member_id = ? ";
            countOrderStatement = orderConn.prepareStatement(countOrder);

            String memberSql = "insert into member (card_vip,city,class_no,created_date,gender,get_week_card,grade,grade_id,head_icon,member_id," +
                    "name,nic,password,phone,pid,province,purchased_card,region,school_id,school_name,share_type,start_date,stop_date,union_id,user_id,vip_type) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            insertMemberStatement = memberConn.prepareStatement(memberSql);

            String orderSql = "insert into `order` (address,address_id,create_date,delivery_date,discount_amount,member_id,merchant_id,merchant_name," +
                    "order_amount_total,order_id,order_status,order_total_id,out_trade_no,pay_channel,payment_date,phone,product_amount_total," +
                    "recipient,recipient_phone,remark,tracking_no,transaction_date) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            insertOrderStatement = orderConn.prepareStatement(orderSql);

            String sql = "select id,name,mobile,account,parent_id,level,code,order_nums,sub_order_nums,income from broker";
            initBrokerStatement = orderConn.prepareStatement(sql);

            String updateOrder = "update broker set order_nums=?,level=?,income=? where id=?";
            updateOrderStatement = orderConn.prepareStatement(updateOrder);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    public static PreparedStatement getQueryMemberStatement() {
        try {
            queryMemberStatement.clearParameters();
            return queryMemberStatement;
        } catch (SQLException e) {
            e.printStackTrace();
            return queryMemberStatement;
        }
    }

    public static PreparedStatement getCountOrderStatement() {
        try {
            countOrderStatement.clearParameters();
            return countOrderStatement;
        } catch (SQLException e) {
            e.printStackTrace();
            return countOrderStatement;
        }
    }

    public static PreparedStatement getInsertOrderStatement() {
        try {
            insertOrderStatement.clearParameters();
            return insertOrderStatement;
        } catch (SQLException e) {
            e.printStackTrace();
            return insertOrderStatement;
        }
    }

    public static PreparedStatement getInsertMemberStatement() {
        try {
            insertMemberStatement.clearParameters();
            return insertMemberStatement;
        } catch (SQLException e) {
            e.printStackTrace();
            return insertMemberStatement;
        }
    }

    public static PreparedStatement getInitBrokerStatement() {
        try {
            initBrokerStatement.clearParameters();
            return initBrokerStatement;
        } catch (SQLException e) {
            e.printStackTrace();
            return initBrokerStatement;
        }
    }

    public static PreparedStatement getUpdateOrderStatement() {
        try {
            updateOrderStatement.clearParameters();
            return updateOrderStatement;
        } catch (SQLException e) {
            e.printStackTrace();
            return updateOrderStatement;
        }
    }
}
