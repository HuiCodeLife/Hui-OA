package com.h.wechat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.h.auth.service.SysUserService;
import com.h.model.process.Process;
import com.h.model.process.ProcessTemplate;
import com.h.model.system.SysUser;
import com.h.process.service.ProcessService;
import com.h.process.service.ProcessTemplateService;
import com.h.security.custom.LoginUserInfoHelper;
import com.h.wechat.service.MessageService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 消息推送实现类
 * @author: Lin
 * @since: 2023-03-15
 */
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Resource
    private WxMpService wxMpService;

    @Resource
    private ProcessService processService;

    @Resource
    private ProcessTemplateService processTemplateService;

    @Resource
    private SysUserService sysUserService;

    @SneakyThrows
    @Override
    public void pushPendingMessage(Long processId, Long userId, String taskId) {
        // 获取审核信息
        Process process = processService.getById(processId);
        // 获取审核模板
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        // 获取被通知人信息
        SysUser sysUser = sysUserService.getById(userId);
        // 获取任务提交者信息
        SysUser submitSysUser = sysUserService.getById(process.getUserId());
        // 获取openId
        String openid = sysUser.getOpenId();
        //TODO 测试，给默认值（开发者本人的openId）
        if(StringUtils.isEmpty(openid)) {
            openid = "oSN8I6imSpV4Jz1Jvy_QR4erm-p0";
        }
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                // 被推送者openId
                .toUser(openid)
                // 模板id
                .templateId("JgFPPnLbcuBW8iXjdD6VmtjQBWmpYuEd3RlekdbXr0s")
                // 点击模板消息要访问的网址
                .url("http://ggkt1.vipgz1.91tunnel.com/#/show/"+processId+"/"+taskId)
                .build();
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuilder content = new StringBuilder();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        templateMessage.addData(new WxMpTemplateData("first", submitSysUser.getName()+"提交了"+processTemplate.getName()+"审批申请，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        String msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        log.info("推送消息返回：{}", msg);
    }
    @SneakyThrows
    @Override
    public void pushProcessedMessage(Long processId, Long userId, Integer status) {
        Process process = processService.getById(processId);
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        SysUser sysUser = sysUserService.getById(userId);
        SysUser currentSysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        String openid = sysUser.getOpenId();
        if(StringUtils.isEmpty(openid)) {
            openid = "oSN8I6imSpV4Jz1Jvy_QR4erm-p0";
        }
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                // 被推送者openId
                .toUser(openid)
                // 模板id
                .templateId("S6EP-eMrwmwfoQIGDVpqilizbzQLhn31NMGcLeNsifk")
                // 点击模板消息要访问的网址
                .url("http://ggkt1.vipgz1.91tunnel.com/#/show/"+processId+"/0")
                .build();
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        templateMessage.addData(new WxMpTemplateData("first", "你发起的"+processTemplate.getName()+"审批申请已经被处理了，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword3", currentSysUser.getName(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword4", status == 1 ? "审批通过" : "审批拒绝", status == 1 ? "#009966" : "#FF0033"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        String msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        log.info("推送消息返回：{}", msg);
    }
}
