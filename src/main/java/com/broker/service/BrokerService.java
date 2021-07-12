package com.broker.service;

import com.broker.jobs.Broker;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Service
public class BrokerService {

    public static Connection connection;

    public BrokerService() {

        try {
            Class.forName("mysql");
            connection = DriverManager.getConnection(",", "", "");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    public void register(String name, String mobile, String account) {
        Broker broker = new Broker();

        String sql = "insert into broker (name,mobile,account) values (?,?,?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, mobile);
            preparedStatement.setString(3, account);
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Broker> getBrokers() {

        Broker broker = new Broker();
//        brokers.add(broker);
        broker.setId(1L);
        broker.setSubOrderNums(111);
        broker.setName("sdfjdiofjioe");
        broker.setMobile("11122233311");
        return Lists.newArrayList();
    }

}
