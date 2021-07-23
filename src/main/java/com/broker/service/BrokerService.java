package com.broker.service;

import com.broker.bo.HttpResult;
import com.broker.dao.ConnectionFactory;
import com.broker.bo.Broker;
import com.broker.enums.Level;
import com.broker.utils.ExcelUtil;
import com.broker.utils.QrCodeUtils;
import com.broker.vo.BrokerVO;
import com.google.zxing.WriterException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private CalculateService calculateService;

    private static final String nullParentReferrerCode = "1qaz@WSX";

    public HttpResult<Void> register(BrokerVO brokerVO) {
        try {
            if (StringUtils.isBlank(brokerVO.getReferrerCode())) {
                return HttpResult.failure("请输入邀请码");
            }
            Broker broker = new Broker();
            BeanUtils.copyProperties(brokerVO, broker);
            if (!Objects.equals(nullParentReferrerCode, brokerVO.getReferrerCode())) {
                PreparedStatement referrerStatement = ConnectionFactory.getReferrerStatement();
                referrerStatement.setString(1, brokerVO.getReferrerCode());
                ResultSet resultSet = referrerStatement.executeQuery();
                if (!resultSet.next()) {
                    return HttpResult.failure("没找到推荐人");
                }
                if (StringUtils.isBlank(brokerVO.getPassword())) {
                    return HttpResult.failure("密码为空");
                }
                broker.setParentId(resultSet.getLong("parent_id"));
            }
            PreparedStatement insertBrokerStatement = ConnectionFactory.getInsertBrokerStatement();
            insertBrokerStatement.setString(1, broker.getName());
            insertBrokerStatement.setString(2, broker.getMobile());
            insertBrokerStatement.setString(3, broker.getAccount());
            insertBrokerStatement.setString(4, broker.getPassword());
            insertBrokerStatement.setString(5, UUID.randomUUID().toString().substring(0, 8));
            insertBrokerStatement.setLong(6, broker.getParentId());
            insertBrokerStatement.execute();

            if (broker.getParentId() == null) {
                calculateService.brokers.add(broker);
            } else {
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
            PreparedStatement countBrokerStatement = ConnectionFactory.getCountBrokerByMobileStatement();
            countBrokerStatement.setString(1, mobile);
            ResultSet countRes = countBrokerStatement.executeQuery();
            if (!countRes.next()) {
                return Pair.of(0, null);
            }
            PreparedStatement queryBrokerStatement = ConnectionFactory.getQueryBrokerByMobileLimitStatement();
            List<Broker> brokers = new ArrayList<>(limit * 2);
            queryBrokerStatement.setString(1, mobile);
            queryBrokerStatement.setInt(2, offset);
            queryBrokerStatement.setInt(3, limit);
            ResultSet resultSet = queryBrokerStatement.executeQuery();
            while (resultSet.next()) {
                Broker broker = new Broker();
                brokers.add(broker);
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
                broker.setCreateDate(resultSet.getDate("create_date"));
            }
            return Pair.of(countRes.getInt(1), brokers);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Pair.of(0, null);
    }

    public Pair<Integer, List<Broker>> getBrokers(Integer limit, Integer offset) {

        try {
            PreparedStatement countBrokerStatement = ConnectionFactory.getCountBrokerStatement();
            List<Broker> brokers = new ArrayList<>(limit * 2);
            PreparedStatement queryBrokerStatement = ConnectionFactory.getQueryBrokerLimitStatement();
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
                broker.setCreateDate(resultSet.getDate("create_date"));
            }
            return Pair.of(countRes.getInt(1), brokers);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Pair.of(0, null);
    }

    public HttpResult<Void> export(HttpServletResponse httpServletResponse) {

        List<Broker> brokers = new ArrayList<>();
        PreparedStatement brokerStatement = ConnectionFactory.getInitBrokerStatement();
        try {
            ResultSet resultSet = brokerStatement.executeQuery();
            while (resultSet.next()) {
                Broker broker = new Broker();
                brokers.add(broker);
                broker.setId(resultSet.getLong("id"));
                broker.setName(resultSet.getString("name"));
                broker.setMobile(resultSet.getString("mobile"));
                broker.setAccount(resultSet.getString("account"));
                broker.setParentId(resultSet.getLong("parent_id"));
                broker.setLevel(Level.valueOf(resultSet.getString("level")));
                broker.setReferrerCode(resultSet.getString("referrer_code"));
                broker.setOrderNums(resultSet.getInt("order_nums"));
                broker.setSubOrderNums(resultSet.getInt("sub_order_nums"));
                broker.setReferrerCode(resultSet.getString("referrer_code"));
                broker.setIncome(resultSet.getBigDecimal("income"));
                broker.setCreateDate(resultSet.getDate("create_date"));
            }
            //excel标题
            String[] title = {"用户", "电话", "银行账户", "收入", "级别", "注册时间"};

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
                content[i][3] = String.valueOf(obj.getIncome());
                content[i][4] = obj.getLevel().getName();
                content[i][4] = obj.getCreateDate().toString();
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

        PreparedStatement queryBrokerStatement = ConnectionFactory.getQueryBrokerStatement();
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return HttpResult.failure(e.getMessage());
        }
        return HttpResult.success();
    }

    public HttpResult<Void> qrcode(String phone, HttpServletResponse httpResponse) {
        try {
            PreparedStatement queryBrokerStatement = ConnectionFactory.getQueryBrokerStatement();
            queryBrokerStatement.setString(1, phone);
            ResultSet resultSet = queryBrokerStatement.executeQuery();
            if (!resultSet.next()) {
                return HttpResult.failure("broker not exists");
            }
            long id = resultSet.getLong("id");
            ServletOutputStream outputStream = httpResponse.getOutputStream();
            QrCodeUtils.generateQRCodeImage("www.baidu.com?pid=" + id, outputStream);
            outputStream.flush();
            outputStream.close();
            return HttpResult.success();
        } catch (WriterException | IOException | SQLException e) {
            e.printStackTrace();
            return HttpResult.failure(e.getLocalizedMessage());
        }
    }
}
