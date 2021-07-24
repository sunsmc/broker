package com.broker.service;

import com.alibaba.fastjson.JSON;
import com.broker.bo.OrderEvent;
import com.broker.dao.ConnectionFactory;
import com.broker.enums.Level;
import com.broker.enums.OrderType;
import com.broker.bo.Broker;
import org.assertj.core.util.Lists;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalculateService {

    private static Logger logger = LoggerFactory.getLogger(CalculateService.class);

    /**
     * 一级broker
     */
    List<Broker> brokers = new ArrayList<>();
    /**
     * temp list
     */
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
                broker.setLevel(Level.valueOf(resultSet.getString("level")));
                broker.setReferrerCode(resultSet.getString("referrer_code"));
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
        calculateIncomeAndUpgradeLevel(brokers, orderEvent);
        brokersNeedUpdate.forEach(broker -> {
            try {
                PreparedStatement preparedStatement = ConnectionFactory.getUpdateOrderStatement();
                preparedStatement.setInt(1, broker.getOrderNums());
                preparedStatement.setBigDecimal(2, broker.getLevel().getDirect());
                preparedStatement.setBigDecimal(3, broker.getIncome());
                preparedStatement.setLong(4, broker.getId());
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


    public static void main(String[] args) {
        Broker a = new Broker();
        a.setId(1L);
        a.setOrderNums(99);
        a.setSubOrderNums(0);
        a.setLevel(Level.ordinary);

        Broker b = new Broker();
        b.setId(2L);
        b.setOrderNums(0);
        b.setSubOrderNums(0);
        b.setLevel(Level.ordinary);

        Broker c = new Broker();
        c.setId(3L);
        c.setOrderNums(0);
        c.setSubOrderNums(0);
        c.setLevel(Level.ordinary);

        a.setChildren(Lists.newArrayList(b));
        b.setChildren(Lists.newArrayList(c));


        OrderEvent orderEvent = new OrderEvent("");
        orderEvent.setBrokerId(3L);
        orderEvent.setOrderAmountTotal(BigDecimal.valueOf(13232));
        orderEvent.setType(OrderType.HAIDUJIAOYU.name());
        orderEvent.setRenewal(false);
        ArrayList<Broker> brokers = Lists.newArrayList(a);

        new CalculateService().calculateIncomeAndUpgradeLevel(brokers, orderEvent);

        System.out.println(JSON.toJSONString(brokers));
    }

    private int calculateIncomeAndUpgradeLevel(List<Broker> brokers, OrderEvent orderEvent) {
        if (CollectionUtils.isEmpty(brokers)) {
            return 0;
        }
        for (Broker broker : brokers) {
            if (broker.getId().equals(orderEvent.getBrokerId())) {
                return updateBroker(orderEvent, broker, 0);
            } else {
                int orders = calculateIncomeAndUpgradeLevel(broker.getChildren(), orderEvent);
                if (orders == 0) {
                    return 0;
                } else {
                    return updateBroker(orderEvent, broker, orders);
                }
            }
        }
        return 0;
    }

    private int updateBroker(OrderEvent orderEvent, Broker broker, int orders) {
        if (orders == 0) {
            broker.setOrderNums(broker.getOrderNums() + 1);
        } else {
            broker.setSubOrderNums(broker.getSubOrderNums() + 1);
        }
        upgradeLevel(broker);
        incrementIncome(broker, orderEvent, orders);
        brokersNeedUpdate.add(broker);
        return ++orders;
    }

    private void upgradeLevel(Broker broker) {
        switch (broker.getLevel()) {
            case ordinary:
                if (broker.getOrderNums() >= 80 && broker.getChildren().stream().filter(cb -> cb.getSubOrderNums() >= 120).count() >= 3) {
                    broker.setLevel(Level.director);
                }
                break;
            case director:
                if (broker.getOrderNums() >= 160 && countLevel(broker, 0) >= 3) {
                    broker.setLevel(Level.manager);
                }
                break;
            case manager:
                if (broker.getOrderNums() >= 240 && countLevel(broker, 0) >= 3) {
                    broker.setLevel(Level.chairman);
                }
                break;
            case chairman:
                if (broker.getOrderNums() >= 300 && countLevel(broker, 0) >= 3) {
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


    private void buildTeamIncome(Broker broker, OrderEvent orderEvent, int orders) {

        if (orders <= 0) {
            return;
        }
        BigDecimal orderAmountTotal = orderEvent.getOrderAmountTotal();
        BigDecimal team = broker.getLevel().getTeam();
        List<Broker> res = Lists.newArrayList();
        createLine(res, broker, orderEvent.getBrokerId());
        // res至少2个元素：当前broker+订单归属broker，所以移除掉
        res = res.subList(1, res.size() - 1);
        // 计算下属中，最大的团队收益人
        BigDecimal subTeam = res.stream().map(Broker::getLevel).map(Level::getTeam).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        // 计算自己可得的差额收益
        BigDecimal income = orderAmountTotal.multiply(team.subtract(subTeam));
        broker.setTeamIncome(broker.getTeamIncome().add(income));
    }


    private boolean createLine(List<Broker> res, Broker broker, Long subBrokerId) {
        res.add(broker);
        boolean find = false;
        if (Objects.equals(broker.getId(), subBrokerId)) {
            find = true;
        } else {
            List<Broker> children = broker.getChildren();
            for (int i = 0; i < children.size() && !find; i++) {
                find = createLine(res, children.get(i), subBrokerId);
            }
        }
        if (!find) {
            res.remove(broker);
        }
        return find;
    }

    private void incrementIncome(Broker broker, OrderEvent orderEvent, int orders) {

        if (orders > 2) {
            return;
        }
        BigDecimal orderAmountTotal = orderEvent.getOrderAmountTotal();
        Level level = broker.getLevel();
        if (OrderType.HAIDUJIAOYU.name().equalsIgnoreCase(orderEvent.getType())) {
            // 海读书童首次订单
            if (!orderEvent.isRenewal()) {
                switch (orders) {
                    // 本人订单
                    case 0:
                        broker.setDirectIncome(broker.getDirectIncome().add(level.getHaiDuDirect()));
                        // 本人无团队收益
                        // 0,1,2才有收益，其他默认只有团队收益
                        buildTeamIncome(broker, orderEvent, 0);
                        break;
                    // 本人上1级
                    case 1:
                        broker.setFirstIncome(broker.getFirstIncome().add(level.getHaiDuFirst()));
                        // 0,1,2才有收益，其他默认只有团队收益
                        buildTeamIncome(broker, orderEvent, 1);
                        break;
                    // 本人上2级
                    case 2:
                        broker.setSecondIncome(broker.getSecondIncome().add(level.getHaiDuSecond()));
                        // 0,1,2才有收益，其他默认只有团队收益
                        buildTeamIncome(broker, orderEvent, 2);
                        break;
                    default:
                        // 0,1,2才有收益，其他默认只有团队收益
                        buildTeamIncome(broker, orderEvent, orders);
                        break;
                }
            }
            // 海读书童续约订单
            else {
                switch (orders) {
                    case 0:
                        broker.setDirectIncome(broker.getDirectIncome().add(level.getHaiDuRenewalDirect()));
                        break;
                    case 1:
                        broker.setFirstIncome(broker.getFirstIncome().add(level.getHaiDuRenewalFirst()));
                        break;
                    case 2:
                        broker.setSecondIncome(broker.getSecondIncome().add(level.getHaiDuRenewalSecond()));
                        break;
                }
            }
        }
        // 非海读书童业务
        else {
            switch (orders) {
                case 0:
                    broker.setDirectIncome(broker.getDirectIncome().add(level.getDirect().multiply(orderAmountTotal)));
                    break;
                case 1:
                    broker.setDirectIncome(broker.getDirectIncome().add(level.getFirst().multiply(orderAmountTotal)));
                    break;
                case 2:
                    broker.setDirectIncome(broker.getDirectIncome().add(level.getSecond().multiply(orderAmountTotal)));
                    break;
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
