package com.terra.framework.nova.llm.model.wenxin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.llm.exception.ErrorType;
import com.terra.framework.nova.llm.exception.ModelException;
import com.terra.framework.nova.llm.model.AbstractVendorAdapter;
import com.terra.framework.nova.llm.model.AuthProvider;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.MessageRole;
import com.terra.framework.nova.llm.model.ModelRequest;
import com.terra.framework.nova.llm.model.ModelResponse;
import com.terra.framework.nova.llm.model.TokenUsage;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * 文心一言模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class WenxinAdapter extends AbstractVendorAdapter {

    /**
     * 构造函数
     *
     * @param requestMappingStrategy 请求参数映射策略
     * @param authProvider 认证提供者
     */
    public WenxinAdapter(WenxinRequestMappingStrategy requestMappingStrategy, AuthProvider authProvider) {
        super(requestMappingStrategy, authProvider);
    }

    @Override
    protected void customizeMessage(JSONObject vendorMessage, Message originalMessage) {
        // 文心一言特殊处理角色
        switch (originalMessage.getRole()) {
            case SYSTEM:
                // 文心一言不支持system角色，将其转换为user角色并添加标记
                vendorMessage.put("role", "user");
                vendorMessage.put("content", "[系统指令]" + originalMessage.getContent());
                break;
            case FUNCTION:
            case TOOL:
                // 文心一言不直接支持function或tool角色，将其转换为附加信息
                vendorMessage.put("role", "assistant");
                vendorMessage.put("content", "工具调用结果: " + originalMessage.getContent());
                break;
        }
    }

    @Override
    protected ErrorType mapErrorType(String errorCode) {
        if (errorCode == null) {
            return ErrorType.UNKNOWN_ERROR;
        }

        // 文心一言的错误码参考
        switch (errorCode) {
            case "2": // 请求参数错误
                return ErrorType.INVALID_REQUEST_ERROR;
            case "4":  // 授权错误
            case "6":  // 身份认证失败
            case "14": // Access Token过期
            case "15": // Access Token不存在
            case "17": // Open API 请求数达到上限
            case "18": // Open API 调用频率达到上限
                return ErrorType.AUTHENTICATION_ERROR;
            case "19": // 模型调用超限
            case "336100": // QPS 限流
                return ErrorType.RATE_LIMIT_ERROR;
            case "336101": // 文本安全
                return ErrorType.CONTENT_FILTER_ERROR;
            case "336102": // prompt 尺寸超限
                return ErrorType.CONTEXT_LENGTH_ERROR;
            case "336103": // 模型服务化预估内部错误
            case "336104": // 模型服务化预估内部错误
                return ErrorType.SERVER_ERROR;
            default:
                return ErrorType.UNKNOWN_ERROR;
        }
    }

    @Override
    protected String getVendorName() {
        return "文心一言";
    }

    @Override
    protected String getDefaultModelName() {
        return "ernie-bot";
    }
}
