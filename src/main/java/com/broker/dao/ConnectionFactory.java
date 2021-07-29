package com.broker.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConnectionFactory {

    public Connection orderConn;
    public Connection memberConn;
    public Connection brokerConn;

    private PreparedStatement queryMemberStatement;
    private PreparedStatement countOrderStatement;
    private PreparedStatement insertOrderStatement;
    private PreparedStatement insertMemberStatement;
    private PreparedStatement initBrokerStatement;
    private PreparedStatement updateOrderStatement;
    private PreparedStatement queryBrokerStatement;
    private PreparedStatement insertBrokerStatement;
    private PreparedStatement queryBrokerLimitStatement;
    private PreparedStatement countBrokerStatement;
    private PreparedStatement referrerStatement;
    private PreparedStatement queryBrokerByMobileLimitStatement;
    private PreparedStatement countBrokerByMobileStatement;
    private PreparedStatement insertBrokerNoParentStatement;
    private PreparedStatement queryBrokerById;
    private PreparedStatement getOrder;

    @Value("${mysql.user}")
    private String user;
    @Value("${mysql.url}")
    private String url;
    @Value("${mysql.pwd}")
    private String pwd;

    private Map<String, PreparedStatement> statementMap = new HashMap<>();

    @PostConstruct
    public void init() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            orderConn = DriverManager.getConnection(url, user, pwd);
            memberConn = DriverManager.getConnection(url, user, pwd);
            brokerConn = DriverManager.getConnection(url, user, pwd);

            //member
            queryMemberStatement = memberConn.prepareStatement("select pid from member where member_id = ? ");
            insertMemberStatement = memberConn.prepareStatement("insert into member (card_vip,city,class_no,created_date,gender,get_week_card,grade,grade_id,head_icon,member_id," +
                    "name,nic,password,phone,pid,province,purchased_card,region,school_id,school_name,share_type,start_date,stop_date,union_id,user_id,vip_type) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

            //order
            insertOrderStatement = orderConn.prepareStatement("insert into `order` (address,address_id,create_date,delivery_date,discount_amount,member_id,merchant_id,merchant_name," +
                    "order_amount_total,order_id,order_status,order_total_id,out_trade_no,pay_channel,payment_date,phone,product_amount_total," +
                    "recipient,recipient_phone,remark,tracking_no,transaction_date) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            countOrderStatement = orderConn.prepareStatement("select count(*) from `order` where member_id = ? ");
            updateOrderStatement = orderConn.prepareStatement("update broker set order_nums=?,`level`=?,income=? where id=?");
            getOrder = orderConn.prepareStatement("select * from `order` where order_id = ?");

            //broker
            initBrokerStatement = brokerConn.prepareStatement("select * from broker");
            queryBrokerStatement = brokerConn.prepareStatement("select * from broker where mobile=? ");
            queryBrokerById = brokerConn.prepareStatement("select * from broker where id=? ");
            insertBrokerStatement = brokerConn.prepareStatement("insert into broker (name,mobile,account,password,referrer_code,parent_id) values (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            insertBrokerNoParentStatement = brokerConn.prepareStatement("insert into broker (name,mobile,account,password,referrer_code) values (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            queryBrokerLimitStatement = brokerConn.prepareStatement("select * from broker limit ?,?");
            queryBrokerByMobileLimitStatement = brokerConn.prepareStatement("select * from broker where mobile like concat('%',?,'%') limit ?,?");
            countBrokerStatement = brokerConn.prepareStatement("select count(*) from broker");
            countBrokerByMobileStatement = brokerConn.prepareStatement("select count(*) from broker where mobile like concat('%',?,'%') ");
            referrerStatement = brokerConn.prepareStatement("select * from broker where referrer_code = ?");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    public PreparedStatement getGetOrder() {
        return getPreparedStatement(getOrder);
    }

    public PreparedStatement getQueryBrokerById() {
        return getPreparedStatement(queryBrokerById);
    }

    public PreparedStatement getInsertBrokerNoParentStatement() {
        return getPreparedStatement(insertBrokerNoParentStatement);
    }

    public PreparedStatement getQueryBrokerByMobileLimitStatement() {
        return getPreparedStatement(queryBrokerByMobileLimitStatement);
    }

    public PreparedStatement getCountBrokerByMobileStatement() {
        return getPreparedStatement(countBrokerByMobileStatement);
    }

    public PreparedStatement getReferrerStatement() {
        return getPreparedStatement(referrerStatement);
    }

    public PreparedStatement getCountBrokerStatement() {
        return getPreparedStatement(countBrokerStatement);
    }

    public PreparedStatement getQueryBrokerLimitStatement() {
        return getPreparedStatement(queryBrokerLimitStatement);
    }

    public PreparedStatement getQueryMemberStatement() {
        return getPreparedStatement(queryMemberStatement);
    }

    public PreparedStatement getCountOrderStatement() {
        return getPreparedStatement(countOrderStatement);
    }

    public PreparedStatement getInsertOrderStatement() {
        return getPreparedStatement(insertOrderStatement);
    }

    public PreparedStatement getInsertMemberStatement() {
        return getPreparedStatement(insertMemberStatement);
    }

    public PreparedStatement getInitBrokerStatement() {
        return getPreparedStatement(initBrokerStatement);
    }

    public PreparedStatement getUpdateOrderStatement() {
        return getPreparedStatement(updateOrderStatement);
    }

    public PreparedStatement getQueryBrokerStatement() {
        return getPreparedStatement(queryBrokerStatement);
    }

    public PreparedStatement getInsertBrokerStatement() {
        return getPreparedStatement(insertBrokerStatement);
    }

    private PreparedStatement getPreparedStatement(PreparedStatement preparedStatement) {
        try {
            preparedStatement.clearParameters();
            return preparedStatement;
        } catch (SQLException e) {
            e.printStackTrace();
            return preparedStatement;
        }
    }
}
