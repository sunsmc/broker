package com.broker.service;

import com.broker.bo.OrderEvent;
import com.broker.dao.ConnectionFactory;
import com.broker.enums.Level;
import com.broker.jobs.Broker;
import com.broker.jobs.EveryDayJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CalculateService {

    private static Logger logger = LoggerFactory.getLogger(CalculateService.class);

    /**
     * 一级broker
     */
    List<Broker> brokers = new ArrayList<>();
    List<Broker> brokersNeedUpdate = new ArrayList<>();


    @PostConstruct
    public void initBrokers() {
        try {
            PreparedStatement preparedStatement = ConnectionFactory.getInitBrokerStatement();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Broker broker = new Broker();
                broker.setId(resultSet.getLong("id"));
                broker.setName(resultSet.getString("name"));
                broker.setMobile(resultSet.getString("mobile"));
                broker.setAccount(resultSet.getString("account"));
                broker.setParentId(resultSet.getLong("parent_id"));
                broker.setLevel(Level.valueOf(resultSet.getString("levels")));
                broker.setCode(resultSet.getString("code"));
                broker.setOrderNums(resultSet.getInt("order_nums"));
                broker.setSubOrderNums(resultSet.getInt("sub_order_nums"));
                broker.setIncome(resultSet.getBigDecimal("income"));
                brokers.add(broker);
            }
            List<Broker> parents = brokers.stream().filter(broker -> broker.getParentId() == null).collect(Collectors.toList());
            brokers.removeAll(parents);
            Map<Long, List<Broker>> subs = brokers.stream().collect(Collectors.groupingBy(Broker::getParentId));
            parents.forEach(parent -> buildChildren(parent, subs));
            brokers = parents;

        } catch (SQLException e) {
            logger.error("", e);
        }
    }

    private void buildChildren(Broker broker, Map<Long, List<Broker>> source) {
        broker.setChildren(source.get(broker.getId()));
        if (CollectionUtils.isEmpty(broker.getChildren())) {
            return;
        }
        broker.getChildren().forEach(c -> buildChildren(c, source));
    }

    @EventListener
    public synchronized void addOrder(OrderEvent orderEvent) {

        brokersNeedUpdate.clear();
        recursionBroker(brokers, orderEvent);
        brokersNeedUpdate.forEach(broker -> {
            try {
                PreparedStatement preparedStatement = ConnectionFactory.getUpdateOrderStatement();
                preparedStatement.setInt(1, broker.getOrderNums());
                preparedStatement.setBigDecimal(2, broker.getLevel().getDirect());
                preparedStatement.setBigDecimal(3, broker.getIncome());
                preparedStatement.setLong(4, broker.getId());
                preparedStatement.execute();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


    private int recursionBroker(List<Broker> brokers, OrderEvent orderEvent) {
        if (CollectionUtils.isEmpty(brokers)) {
            return 0;
        }
        for (Broker broker : brokers) {
            if (broker.getId().equals(orderEvent.getBrokerId())) {
                broker.setOrderNums(broker.getOrderNums() + 1);
                upgradeLevel(broker);
                broker.setIncome(broker.getIncome().add(getIncrement(broker.getLevel(), orderEvent, 0)));
                brokersNeedUpdate.add(broker);
                return 1;
            } else {
                int res = recursionBroker(broker.getChildren(), orderEvent);
                if (res > 0) {
                    broker.setSubOrderNums(broker.getSubOrderNums() + 1);
                    upgradeLevel(broker);
                    broker.setIncome(broker.getIncome().add(getIncrement(broker.getLevel(), orderEvent, res++)));
                    brokersNeedUpdate.add(broker);
                }
                return res;
            }
        }
        return 0;
    }

    private void upgradeLevel(Broker broker) {
        switch (broker.getLevel()) {
            case ordinary:
                if (broker.getOrderNums() >= 100 && broker.getChildren().stream().filter(cb -> cb.getSubOrderNums() >= 120).count() >= 3) {
                    broker.setLevel(Level.director);
                }
                break;
            case director:
                if (broker.getOrderNums() >= 200 && countLevel(broker, 0) >= 3) {
                    broker.setLevel(Level.manager);
                }
                break;
            case manager:
                if (broker.getOrderNums() >= 300 && countLevel(broker, 0) >= 3) {
                    broker.setLevel(Level.chairman);
                }
                break;
            case chairman:
                if (broker.getOrderNums() >= 600 && countLevel(broker, 0) >= 3) {
                    broker.setLevel(Level.partner);
                }
                break;
            case partner:
                break;
        }
    }

    private long countLevel(Broker broker, long count) {

        if (CollectionUtils.isEmpty(broker.getChildren())) {
            return 0;
        }
        Level level = broker.getLevel();
        count += broker.getChildren().stream().filter(cb -> cb.getLevel().getDirect().compareTo(level.getDirect()) >= 0).count();
        for (Broker cb : broker.getChildren()) {
            countLevel(cb, count);
        }
        return count;
    }


    private BigDecimal getIncrement(Level level, OrderEvent orderEvent, int superior) {

        if ("haidujiaoyu".equalsIgnoreCase(orderEvent.getType())) {
            if (orderEvent.isRenewal()) {
                switch (superior) {
                    case 0:
                        return level.getHaiDuRenewalDirect();
                    case 1:
                        return level.getHaiDuRenewalFirst();
                    case 2:
                        return level.getHaiDuRenewalSecond();
                    default:
                        return BigDecimal.valueOf(0);
                }
            } else {
                switch (superior) {
                    case 0:
                        return level.getHaiDuDirect();
                    case 1:
                        return level.getHaiDuFirst();
                    case 2:
                        return level.getHaiDuSecond();
                    default:
                        return BigDecimal.valueOf(0);
                }
            }
        } else {
            switch (superior) {
                case 0:
                    return orderEvent.getOrderAmountTotal().multiply(level.getDirect());
                case 1:
                    return orderEvent.getOrderAmountTotal().multiply(level.getFirst());
                case 2:
                    return orderEvent.getOrderAmountTotal().multiply(level.getSecond());
                default:
                    return BigDecimal.valueOf(0);
            }
        }
    }


    public void addBroker(Broker broker) {

        if (broker.getParentId() == null) {
            brokers.add(broker);
        } else {
            brokers.forEach(b -> {
                if (Objects.equals(b.getId(), broker.getParentId())) {
                    b.getChildren().add(broker);
                }
            });
        }
    }
}
