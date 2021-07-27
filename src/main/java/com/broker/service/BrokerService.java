package com.broker.service;

import com.alibaba.fastjson.JSON;
import com.broker.bo.HttpResult;
import com.broker.bo.Broker;
import com.broker.dao.ConnectionFactory;
import com.broker.enums.Level;
import com.broker.utils.ExcelUtil;
import com.broker.utils.QrCodeUtils;
import com.broker.vo.BrokerVO;
import com.google.zxing.WriterException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class BrokerService {

    private static Logger logger = LoggerFactory.getLogger(BrokerService.class);

    @Autowired
    private CalculateService calculateService;
    @Autowired
    private ConnectionFactory connectionFactory;

    private static final String nullParentReferrerCode = "1qaz@WSX";

    public HttpResult<Void> register(BrokerVO brokerVO) {
        try {
            if (StringUtils.isBlank(brokerVO.getReferrerCode())) {
                return HttpResult.failure("请输入邀请码");
            }
            Broker broker = new Broker();
            BeanUtils.copyProperties(brokerVO, broker);
            if (!Objects.equals(nullParentReferrerCode, brokerVO.getReferrerCode())) {
                PreparedStatement referrerStatement = connectionFactory.getReferrerStatement();
                referrerStatement.setString(1, brokerVO.getReferrerCode());
                ResultSet resultSet = referrerStatement.executeQuery();
                if (!resultSet.next()) {
                    return HttpResult.failure("没找到推荐人");
                }
                if (StringUtils.isBlank(brokerVO.getPassword())) {
                    return HttpResult.failure("密码为空");
                }
                broker.setParentId(resultSet.getLong("id"));
            }

            PreparedStatement queryBrokerStatement = connectionFactory.getQueryBrokerStatement();
            queryBrokerStatement.setString(1, brokerVO.getMobile());
            ResultSet resultSet = queryBrokerStatement.executeQuery();
            if (resultSet.next()) {
                return HttpResult.failure("该手机号已注册");
            }
            if (broker.getParentId() == null || broker.getParentId() <= 0) {
                PreparedStatement insertBrokerStatement = connectionFactory.getInsertBrokerNoParentStatement();
                insertBrokerStatement.setString(1, broker.getName());
                insertBrokerStatement.setString(2, broker.getMobile());
                insertBrokerStatement.setString(3, broker.getAccount());
                insertBrokerStatement.setString(4, broker.getPassword());
                insertBrokerStatement.setString(5, UUID.randomUUID().toString().substring(0, 8));
                insertBrokerStatement.execute();
                ResultSet generatedKeys = insertBrokerStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    PreparedStatement queryBrokerById = connectionFactory.getQueryBrokerById();
                    queryBrokerById.setLong(1, id);
                    ResultSet query = queryBrokerById.executeQuery();
                    if (query.next()) {
                        buildBroker(query, broker);
                    }
                }
                calculateService.brokers.add(broker);
            } else {
                PreparedStatement insertBrokerStatement = connectionFactory.getInsertBrokerStatement();
                insertBrokerStatement.setString(1, broker.getName());
                insertBrokerStatement.setString(2, broker.getMobile());
                insertBrokerStatement.setString(3, broker.getAccount());
                insertBrokerStatement.setString(4, broker.getPassword());
                insertBrokerStatement.setString(5, UUID.randomUUID().toString().substring(0, 8));
                insertBrokerStatement.setLong(6, broker.getParentId());
                insertBrokerStatement.execute();
                ResultSet generatedKeys = insertBrokerStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    PreparedStatement queryBrokerById = connectionFactory.getQueryBrokerById();
                    queryBrokerById.setLong(1, id);
                    ResultSet query = queryBrokerById.executeQuery();
                    if (query.next()) {
                        buildBroker(query, broker);
                    }
                }
                for (Broker b : calculateService.brokers) {
                    if (b.getId().equals(broker.getParentId())) {
                        b.getChildren().add(broker);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return HttpResult.failure(e.getMessage());
        }
        return HttpResult.success();
    }

    public Pair<Integer, List<Broker>> searchBrokers(Integer limit, Integer offset, String mobile) {

        try {
            PreparedStatement countBrokerStatement = connectionFactory.getCountBrokerByMobileStatement();
            countBrokerStatement.setString(1, mobile);
            ResultSet countRes = countBrokerStatement.executeQuery();
            if (!countRes.next()) {
                return Pair.of(0, null);
            }
            PreparedStatement queryBrokerStatement = connectionFactory.getQueryBrokerByMobileLimitStatement();
            List<Broker> brokers = new ArrayList<>(limit * 2);
            queryBrokerStatement.setString(1, mobile);
            queryBrokerStatement.setInt(2, offset);
            queryBrokerStatement.setInt(3, limit);
            ResultSet resultSet = queryBrokerStatement.executeQuery();
            while (resultSet.next()) {
                Broker broker = new Broker();
                brokers.add(broker);
                buildBroker(resultSet, broker);
            }
            return Pair.of(countRes.getInt(1), brokers);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Pair.of(0, null);
    }

    public static void buildBroker(ResultSet resultSet, Broker broker) throws SQLException {
        broker.setId(resultSet.getLong("id"));
        broker.setName(resultSet.getString("name"));
        broker.setMobile(resultSet.getString("mobile"));
        broker.setAccount(resultSet.getString("account"));
        broker.setParentId(resultSet.getLong("parent_id"));
        broker.setLevel(Level.valueOf(resultSet.getString("level")));
        broker.setLevelStr(broker.getLevel().getName());
        broker.setReferrerCode(resultSet.getString("referrer_code"));
        broker.setOrderNums(resultSet.getInt("order_nums"));
        broker.setSubOrderNums(resultSet.getInt("sub_order_nums"));
        broker.setReferrerCode(resultSet.getString("referrer_code"));
        broker.setIncome(resultSet.getBigDecimal("income"));
        broker.setDirectIncome(resultSet.getBigDecimal("direct_income"));
        broker.setFirstIncome(resultSet.getBigDecimal("first_income"));
        broker.setSecondIncome(resultSet.getBigDecimal("second_income"));
        broker.setTeamIncome(resultSet.getBigDecimal("team_income"));
        broker.setShopIncome(resultSet.getBigDecimal("shop_income"));
        broker.setResearchIncome(resultSet.getBigDecimal("research_income"));
        broker.setCreateDate(resultSet.getDate("create_date"));
        broker.setChildren(Lists.newArrayList());
    }

    public Pair<Integer, List<Broker>> getBrokers(Integer limit, Integer offset) {

        try {
            PreparedStatement countBrokerStatement = connectionFactory.getCountBrokerStatement();
            List<Broker> brokers = new ArrayList<>(limit * 2);
            PreparedStatement queryBrokerStatement = connectionFactory.getQueryBrokerLimitStatement();
            ResultSet countRes = countBrokerStatement.executeQuery();
            if (!countRes.next()) {
                return Pair.of(0, null);
            }
            queryBrokerStatement.setInt(1, offset);
            queryBrokerStatement.setInt(2, limit);
            ResultSet resultSet = queryBrokerStatement.executeQuery();
            while (resultSet.next()) {
                Broker broker = new Broker();
                brokers.add(broker);
                buildBroker(resultSet, broker);
            }
            return Pair.of(countRes.getInt(1), brokers);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Pair.of(0, null);
    }

    public HttpResult<Void> export(HttpServletResponse httpServletResponse) {

        List<Broker> brokers = new ArrayList<>();
        PreparedStatement brokerStatement = connectionFactory.getInitBrokerStatement();
        try {
            ResultSet resultSet = brokerStatement.executeQuery();
            while (resultSet.next()) {
                Broker broker = new Broker();
                brokers.add(broker);
                buildBroker(resultSet, broker);
                broker.setPassword(null);
            }
            //excel标题
            String[] title = {"用户", "电话", "银行账户", "直接收入", "一代收入", "二代收入", "团队收入", "商场收入", "乐研收入", "总收入", "级别", "注册时间"};

            //excel文件名
            String fileName = "推广者" + System.currentTimeMillis() + ".xls";

            //sheet名
            String sheetName = "推广者信息表";
            String[][] content = new String[brokers.size()][title.length];
            for (int i = 0; i < brokers.size(); i++) {
                content[i] = new String[title.length];
                Broker obj = brokers.get(i);
                content[i][0] = obj.getName();
                content[i][1] = obj.getMobile();
                content[i][2] = obj.getAccount();
                content[i][3] = String.valueOf(obj.getDirectIncome());
                content[i][4] = String.valueOf(obj.getFirstIncome());
                content[i][5] = String.valueOf(obj.getSecondIncome());
                content[i][6] = String.valueOf(obj.getTeamIncome());
                content[i][7] = String.valueOf(obj.getShopIncome());
                content[i][8] = String.valueOf(obj.getResearchIncome());
                content[i][9] = String.valueOf(obj.getIncome());
                content[i][10] = obj.getLevel().getName();
                content[i][11] = obj.getCreateDate().toString();
            }

            //创建HSSFWorkbook
            HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook(sheetName, title, content, null);

            //响应到客户端
            try {
                this.setResponseHeader(httpServletResponse, fileName);
                OutputStream os = httpServletResponse.getOutputStream();
                wb.write(os);
                os.flush();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
                return HttpResult.failure(e.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return HttpResult.failure(e.getMessage());
        }
        return HttpResult.success();
    }

    //发送响应流方法
    public void setResponseHeader(HttpServletResponse response, String fileName) {
        try {
            try {
                fileName = new String(fileName.getBytes(), "ISO8859-1");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            response.setContentType("application/octet-stream;charset=ISO8859-1");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.addHeader("Pargam", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public HttpResult<Broker> login(String phone, String password) {

        PreparedStatement queryBrokerStatement = connectionFactory.getQueryBrokerStatement();
        try {
            queryBrokerStatement.setString(1, phone);
            ResultSet resultSet = queryBrokerStatement.executeQuery();
            if (resultSet.next()) {
                String pwd = resultSet.getString("password");
                if (!Objects.equals(password, pwd)) {
                    return HttpResult.failure("密码错误");
                }
                Broker broker = new Broker();
                broker.setId(resultSet.getLong("id"));
                broker.setName(resultSet.getString("name"));
                broker.setMobile(resultSet.getString("mobile"));
                broker.setAccount(resultSet.getString("account"));
                broker.setParentId(resultSet.getLong("parent_id"));
                broker.setLevel(Level.valueOf(resultSet.getString("level")));
                broker.setLevelStr(broker.getLevel().getName());
                broker.setReferrerCode(resultSet.getString("referrer_code"));
                broker.setOrderNums(resultSet.getInt("order_nums"));
                broker.setSubOrderNums(resultSet.getInt("sub_order_nums"));
                broker.setReferrerCode(resultSet.getString("referrer_code"));
                broker.setIncome(resultSet.getBigDecimal("income"));
                return HttpResult.success(broker);
            } else {
                return HttpResult.failure("用户不存在");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return HttpResult.failure(e.getMessage());
        }
    }

    public HttpResult<Void> qrcode(String phone, HttpServletResponse httpResponse) {
        try {
            PreparedStatement queryBrokerStatement = connectionFactory.getQueryBrokerStatement();
            queryBrokerStatement.setString(1, phone);
            ResultSet resultSet = queryBrokerStatement.executeQuery();
            if (!resultSet.next()) {
                return HttpResult.failure("broker not exists");
            }
            long id = resultSet.getLong("id");
            ServletOutputStream outputStream = httpResponse.getOutputStream();
            QrCodeUtils.generateQRCodeImage("http://m.haidushutong.com/?agentTag=Ge&pid=" + id, outputStream);
            outputStream.flush();
            outputStream.close();
            return HttpResult.success();
        } catch (WriterException | IOException | SQLException e) {
            e.printStackTrace();
            return HttpResult.failure(e.getLocalizedMessage());
        }
    }

    public HttpResult<BrokerVO> getUserInfo(String phone, HttpServletResponse httpResponse) {
        try {
            PreparedStatement queryBrokerStatement = connectionFactory.getQueryBrokerStatement();
            queryBrokerStatement.setString(1, phone);
            ResultSet resultSet = queryBrokerStatement.executeQuery();
            if (!resultSet.next()) {
                return HttpResult.failure("broker not exists");
            }
            BrokerVO broker = new BrokerVO();
            broker.setName(resultSet.getString("name"));
            broker.setMobile(resultSet.getString("mobile"));
            broker.setAccount(resultSet.getString("account"));
            broker.setReferrerCode(resultSet.getString("referrer_code"));
            return HttpResult.success(broker);
        } catch (SQLException e) {
            e.printStackTrace();
            return HttpResult.failure(e.getLocalizedMessage());
        }
    }

    public HttpResult<List<Broker>> getTreeBroker(String phone, HttpServletResponse httpResponse) {
        List<Broker> brokers = calculateService.brokers;
        logger.info(JSON.toJSONString(brokers));
        Broker target = find(phone, brokers);
        if (target != null) {
            return HttpResult.success(Lists.newArrayList(target));
        }
        return HttpResult.success(Lists.newArrayList());
    }

    private Broker find(String phone, List<Broker> brokers) {

        if (CollectionUtils.isEmpty(brokers)) {
            return null;
        }
        for (Broker broker : brokers) {
            if (Objects.equals(broker.getMobile(), phone)) {
                return broker;
            }
        }
        for (Broker broker : brokers) {
            Broker target = find(phone, broker.getChildren());
            if (target != null) {
                return target;
            }
        }
        return null;
    }
}