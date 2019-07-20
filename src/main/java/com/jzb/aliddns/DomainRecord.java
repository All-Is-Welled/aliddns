package com.jzb.aliddns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.AddDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.AddDomainRecordResponse;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse.Record;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class DomainRecord {

    private static Log logging = LogFactory.getLog(DomainRecord.class);

    private static DomainRecord domainRecord = null;
    private DefaultProfile profile;
    private String domainName;
    private String[] RRs;
    private String url = "http://members.3322.org/dyndns/getip/";
    private String ip = "";

    private DomainRecord(String accessKeyId, String accessKeySecret, String domainName, String[] RRs) {
        this.domainName = domainName;
        this.RRs = RRs;
        profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);

    }

    public synchronized static void init(String accessKeyId, String accessKeySecret, String domainName, String[] RRs,
            int timeout) {
        if (domainRecord == null) {
            domainRecord = new DomainRecord(accessKeyId, accessKeySecret, domainName, RRs);
        }
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        logging.info("启动解析服务");
        service.scheduleAtFixedRate(() -> domainRecord.start(), 0, timeout, TimeUnit.MINUTES);
        // domainRecord.start();

    }

    // @Scheduled(10 * 1000l)
    private void start() {
        try {
            String newIp = getIp();
            logging.info("IP：" + ip + "  新IP：" + newIp);
            List<Record> records = getRecords();
            List<String> addRRList = new ArrayList(Arrays.asList(RRs));
            List<Record> updateRRList = new ArrayList<>();
            logging.info("输入解析的子域名：" + new ArrayList(Arrays.asList(RRs)).toString());
            for (Record record : records) {
                if (Arrays.asList(RRs).contains(record.getRR())) {
                    updateRRList.add(record);
                    addRRList.remove(record.getRR());
                }
            }

            if ((!ip.equals(newIp) && !newIp.equals(updateRRList.get(0).getValue())) && !updateRRList.isEmpty()) {
                logging.info("需要更新解析的子域名：" + updateRRList.stream().map(record -> record.getRR())
                        .collect(Collectors.toCollection(TreeSet::new)).toString());
                for (Record record : updateRRList) {
                    record.setValue(newIp);
                    updateAnalysis(record);
                }
            }
            ip = newIp;
            logging.info("需要增加解析子域名" + addRRList.toString());
            for (String rr : addRRList) {
                addAnalysis(rr);
            }
            logging.info("解析完成");
        } catch (Throwable e) {
            logging.error("运行出错", e);
        }

    }

    private List<Record> getRecords() {

        IAcsClient client = new DefaultAcsClient(this.profile);

        DescribeDomainRecordsRequest request = new DescribeDomainRecordsRequest();

        request.setDomainName(domainName);

        try {
            DescribeDomainRecordsResponse response = client.getAcsResponse(request);
            String json = new Gson().toJson(response);
            JsonObject object = new JsonParser().parse(json).getAsJsonObject();
            JsonArray str = object.getAsJsonArray("domainRecords");

            List<Record> records = new Gson().fromJson(str, new TypeToken<List<Record>>() {
            }.getType());
            logging.info("查询子域名列表成功 ");
            return records;
        } catch (ServerException e) {
            logging.error("查询子域名列表失败：", e);
        } catch (ClientException e) {
            logging.error("ErrCode:" + e.getErrCode());
            logging.error("ErrCode:" + e.getErrCode());
            logging.error("ErrMsg:" + e.getErrMsg());
            logging.error("RequestId:" + e.getRequestId());
        }

        return null;
    }

    private boolean updateAnalysis(Record record) {
        IAcsClient client = new DefaultAcsClient(profile);

        UpdateDomainRecordRequest request = new UpdateDomainRecordRequest();

        request.setRecordId(record.getRecordId());
        request.setRR(record.getRR());
        request.setValue(record.getValue());
        request.setType("A");

        try {
            UpdateDomainRecordResponse response = client.getAcsResponse(request);
            logging.info("更新子域名成功：" + record.getRR() + " 结果：" + new Gson().toJson(response));
        } catch (ServerException e) {
            logging.error("更新子域名失败：", e);
        } catch (ClientException e) {
            logging.error("ErrCode:" + e.getErrCode());
            logging.error("ErrMsg:" + e.getErrMsg());
            logging.error("RequestId:" + e.getRequestId());
        }
        return false;
    }

    private boolean addAnalysis(String rr) {
        IAcsClient client = new DefaultAcsClient(profile);

        AddDomainRecordRequest request = new AddDomainRecordRequest();
        request.setDomainName(domainName);
        request.setRR(rr);
        request.setValue(this.ip);
        request.setType("A");

        try {
            AddDomainRecordResponse response = client.getAcsResponse(request);
            logging.info("增加子域名成功：" + rr + " 结果：" + new Gson().toJson(response));
        } catch (ServerException e) {
            logging.error("增加子域名失败：", e);
        } catch (ClientException e) {
            logging.error("ErrCode:" + e.getErrCode());
            logging.error("ErrMsg:" + e.getErrMsg());
            logging.error("RequestId:" + e.getRequestId());
        }
        return false;
    }

    public String getIp() {
        String result = "";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                result = EntityUtils.toString(response.getEntity()).replace("\n", "").trim();
                logging.info("IP获取成功" + result);
            }
            return result;
        } catch (Exception e) {
            logging.error("IP获取失败");
            return null;
        }

    }
}
