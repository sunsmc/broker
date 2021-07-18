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
    private static PreparedStatement queryBrokerStatement;
    private static PreparedStatement insertBrokerStatement;
    private static PreparedStatement queryBrokerLimitStatement;
    private static PreparedStatement countBrokerStatement;
    private static PreparedStatement referrerStatement;
    private static PreparedStatement queryBrokerByMobileLimitStatement;
    private static PreparedStatement countBrokerByMobileStatement;

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

            //member
            queryMemberStatement = memberConn.prepareStatement("select pid from member where member_id = ? ");
            insertMemberStatement = memberConn.prepareStatement("insert into member (card_vip,city,class_no,created_date,gender,get_week_card,grade,grade_id,head_icon,member_id," +
                    "name,nic,password,phone,pid,province,purchased_card,region,school_id,school_name,share_type,start_date,stop_date,union_id,user_id,vip_type) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            //order
            insertOrderStatement = orderConn.prepareStatement("insert into `order` (address,address_id,create_date,delivery_date,discount_amount,member_id,merchant_id,merchant_name," +
                    "order_amount_total,order_id,order_status,order_total_id,out_trade_no,pay_channel,payment_date,phone,product_amount_total," +
                    "recipient,recipient_phone,remark,tracking_no,transaction_date) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            countOrderStatement = orderConn.prepareStatement("select count(*) from order where member_id = ? ");
            updateOrderStatement = orderConn.prepareStatement("update broker set order_nums=?,level=?,income=? where id=?");

            //broker
            initBrokerStatement = brokerConn.prepareStatement("select * from broker");
            queryBrokerStatement = brokerConn.prepareStatement("select * from broker where mobile=? ");
            insertBrokerStatement = brokerConn.prepareStatement("insert into broker (name,mobile,account,password,referrer_code,parent_id) values (?,?,?,?,?,?)");
            queryBrokerLimitStatement = brokerConn.prepareStatement("select * from broker limit ?,?");
            queryBrokerByMobileLimitStatement = brokerConn.prepareStatement("select * from broker where mobile like concat('%',?,'%') limit ?,?");
            countBrokerStatement = brokerConn.prepareStatement("select count(*) from broker");
            countBrokerByMobileStatement = brokerConn.prepareStatement("select count(*) from broker where mobile like concat('%',?,'%') ");
            referrerStatement = brokerConn.prepareStatement("select * from broker where referrer_code = ?");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    public static PreparedStatement getQueryBrokerByMobileLimitStatement() {
        return getPreparedStatement(queryBrokerByMobileLimitStatement);
    }

    public static PreparedStatement getCountBrokerByMobileStatement() {
        return getPreparedStatement(countBrokerByMobileStatement);
    }

    public static PreparedStatement getReferrerStatement() {
        return getPreparedStatement(referrerStatement);
    }

    public static PreparedStatement getCountBrokerStatement() {
        return getPreparedStatement(countBrokerStatement);
    }

    public static PreparedStatement getQueryBrokerLimitStatement() {
        return getPreparedStatement(queryBrokerLimitStatement);
    }

    public static PreparedStatement getQueryMemberStatement() {
        return getPreparedStatement(queryMemberStatement);
    }

    public static PreparedStatement getCountOrderStatement() {
        return getPreparedStatement(countOrderStatement);
    }

    public static PreparedStatement getInsertOrderStatement() {
        return getPreparedStatement(insertOrderStatement);
    }

    public static PreparedStatement getInsertMemberStatement() {
        return getPreparedStatement(insertMemberStatement);
    }

    public static PreparedStatement getInitBrokerStatement() {
        return getPreparedStatement(initBrokerStatement);
    }

    public static PreparedStatement getUpdateOrderStatement() {
        return getPreparedStatement(updateOrderStatement);
    }

    public static PreparedStatement getQueryBrokerStatement() {
        return getPreparedStatement(queryBrokerStatement);
    }

    public static PreparedStatement getInsertBrokerStatement() {
        return getPreparedStatement(insertBrokerStatement);
    }

    private static PreparedStatement getPreparedStatement(PreparedStatement preparedStatement) {
        try {
            preparedStatement.clearParameters();
            return preparedStatement;
        } catch (SQLException e) {
            e.printStackTrace();
            return preparedStatement;
        }
    }
}
