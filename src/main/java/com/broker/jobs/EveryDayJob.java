package com.broker.jobs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.broker.bo.MemberDO;
import com.broker.bo.OrderDO;
import com.broker.bo.OrderEvent;
import com.broker.bo.Result;
import com.broker.dao.ConnectionFactory;
import com.broker.enums.OrderType;
import com.broker.utils.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;

@Component
public class EveryDayJob implements ApplicationContextAware {

    private static Logger logger = LoggerFactory.getLogger(EveryDayJob.class);

    //    @Autowired
    private RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private ConnectionFactory connectionFactory;

    @PostConstruct
    private void setRestTemplate() {
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    @Scheduled(cron = "0 * * * * ?")
    public void syncData() {

        syncMember();

        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            logger.error("sync Data temp error", e);
        }

        syncOrder();
    }


    public void syncMember() {
        String url = "http://api.haidushutong.com/api-web/member/list?sign=" + Secret.sign();
        ResponseEntity<String> getResult = restTemplate.getForEntity(url, String.class, new HashMap<>());
        Result<MemberDO> result = JSON.parseObject(getResult.getBody(), new TypeReference<Result<MemberDO>>() {
        });
        logger.info("sync member:{}", JSON.toJSONString(result));
        try {
            for (MemberDO memberDO : result.getData()) {
                insertMember(memberDO);
            }

        } catch (SQLException e) {
            logger.error("sync member error", e);
        }
    }

    public static void main(String[] args) {
        String url = "http://api.haidushutong.com/api-web/member/list?sign=" + Secret.sign();
        ResponseEntity<String> getResult = new RestTemplate().getForEntity(url, String.class, new HashMap<>());
        Result<MemberDO> result = JSON.parseObject(getResult.getBody(), new TypeReference<Result<MemberDO>>() {
        });
        logger.info("sync member:{}", JSON.toJSONString(result));
         url = "http://api.haidushutong.com/api-web/order/list?sign=" + Secret.sign();
         getResult = new RestTemplate().getForEntity(url, String.class, new HashMap<>());
        Result<OrderDO> result1 = JSON.parseObject(getResult.getBody(), new TypeReference<Result<OrderDO>>() {
        });
        logger.info("sync order:{}", JSON.toJSONString(result1));
    }

    public void syncOrder() {

        String url = "http://api.haidushutong.com/api-web/order/list?sign=" + Secret.sign();
        ResponseEntity<String> getResult = restTemplate.getForEntity(url, String.class, new HashMap<>());
        Result<OrderDO> result = JSON.parseObject(getResult.getBody(), new TypeReference<Result<OrderDO>>() {
        });
        logger.info("sync order:{}", JSON.toJSONString(result));
        try {
            for (OrderDO orderDO : result.getData()) {

                PreparedStatement getOrder = connectionFactory.getGetOrder();
                getOrder.setString(1, orderDO.getOrderId());
                ResultSet resultSet = getOrder.executeQuery();
                if (resultSet.next()) {
                    logger.error("orderDO already exists:{}", JSON.toJSONString(orderDO));
                    return;
                }

                insertOrder(orderDO);

                // order -> member -> broker
                PreparedStatement queryMemberStatement = connectionFactory.getQueryMemberStatement();
                queryMemberStatement.setString(1, orderDO.getMemberId());
                resultSet = queryMemberStatement.executeQuery();
                if (!resultSet.next()) {
                    logger.error("order not belong anyone:{}", JSON.toJSONString(orderDO));
                    continue;
                }

                long brokerId = resultSet.getLong("pid");
                OrderEvent orderEvent = new OrderEvent(this);
                orderEvent.setId(Long.valueOf(orderDO.getOrderId()));
                orderEvent.setBrokerId(brokerId);
                orderEvent.setOrderAmountTotal(new BigDecimal(orderDO.getOrderAmountTotal()));
                orderEvent.setType(OrderType.HAIDUJIAOYU.name());

                PreparedStatement countStatement = connectionFactory.getCountOrderStatement();
                countStatement.setString(1, orderDO.getMemberId());
                resultSet = countStatement.executeQuery();
                if (resultSet.next()) {
                    orderEvent.setRenewal(resultSet.getInt(1) > 1);
                }
                applicationEventPublisher.publishEvent(orderEvent);
            }

        } catch (SQLException e) {
            logger.error("sync order error", e);
        }
    }


    private void insertOrder(OrderDO orderDO) throws SQLException {

        PreparedStatement preparedStatement = connectionFactory.getInsertOrderStatement();
        preparedStatement.setString(1, orderDO.getAddress());
        preparedStatement.setString(2, orderDO.getAddressId());
        preparedStatement.setString(3, orderDO.getCreateDate());
        preparedStatement.setString(4, orderDO.getDeliveryDate());
        preparedStatement.setString(5, orderDO.getDiscountAmount());
        preparedStatement.setString(6, orderDO.getMemberId());
        preparedStatement.setString(7, orderDO.getMerchantId());
        preparedStatement.setString(8, orderDO.getMerchantName());
        preparedStatement.setString(9, orderDO.getOrderAmountTotal());
        preparedStatement.setString(10, orderDO.getOrderId());
        preparedStatement.setString(11, orderDO.getOrderStatus());
        preparedStatement.setString(12, orderDO.getOrderTotalId());
        preparedStatement.setString(13, orderDO.getOutTradeNo());
        preparedStatement.setString(14, orderDO.getPayChannel());
        preparedStatement.setString(15, orderDO.getPaymentDate());
        preparedStatement.setString(16, orderDO.getPhone());
        preparedStatement.setString(17, orderDO.getProductAmountTotal());
        preparedStatement.setString(18, orderDO.getRecipient());
        preparedStatement.setString(19, orderDO.getRecipientPhone());
        preparedStatement.setString(20, orderDO.getRemark());
        preparedStatement.setString(21, orderDO.getTrackingNo());
        preparedStatement.setString(22, orderDO.getTransactionDate());
        preparedStatement.execute();
    }

    private void insertMember(MemberDO memberDO) throws SQLException {
        PreparedStatement queryMemberStatement = connectionFactory.getQueryMemberStatement();
        queryMemberStatement.setString(1, memberDO.getMemberId());
        ResultSet resultSet = queryMemberStatement.executeQuery();
        if (resultSet.next()) {
            logger.error("memberDO already exists:{}", JSON.toJSONString(memberDO));
            return;
        }
        PreparedStatement preparedStatement = connectionFactory.getInsertMemberStatement();
        preparedStatement.setString(1, memberDO.getCardVip());
        preparedStatement.setString(2, memberDO.getCity());
        preparedStatement.setString(3, memberDO.getClassNo());
        preparedStatement.setString(4, memberDO.getCreatedDate());
        preparedStatement.setString(5, memberDO.getGender());
        preparedStatement.setString(6, memberDO.getGetWeekCard());
        preparedStatement.setString(7, memberDO.getGrade());
        preparedStatement.setString(8, memberDO.getGradeId());
        preparedStatement.setString(9, memberDO.getHeadIcon());
        preparedStatement.setString(10, memberDO.getMemberId());
        preparedStatement.setString(11, memberDO.getName());
        preparedStatement.setString(12, memberDO.getNic());
        preparedStatement.setString(13, memberDO.getPassword());
        preparedStatement.setString(14, memberDO.getPhone());
        preparedStatement.setString(15, memberDO.getPid());
        preparedStatement.setString(16, memberDO.getProvince());
        preparedStatement.setString(17, memberDO.getPurchasedCard());
        preparedStatement.setString(18, memberDO.getRegion());
        preparedStatement.setString(19, memberDO.getSchoolId());
        preparedStatement.setString(20, memberDO.getSchoolName());
        preparedStatement.setString(21, memberDO.getShareType());
        preparedStatement.setString(22, memberDO.getStartDate());
        preparedStatement.setString(23, memberDO.getStopDate());
        preparedStatement.setString(24, memberDO.getUnionId());
        preparedStatement.setString(25, memberDO.getUserId());
        preparedStatement.setString(26, memberDO.getVipType());
        preparedStatement.execute();
    }

    ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
