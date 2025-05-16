package com.terra.framework.nova.tool.tools;

import com.terra.framework.nova.tool.AbstractTool;
import com.terra.framework.nova.tool.ToolExecutionException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 计算器工具，用于执行基本数学运算
 *
 * @author terra-nova
 */
@Slf4j
public class CalculatorTool extends AbstractTool {
    
    /**
     * 构造函数
     */
    public CalculatorTool() {
        super("calculator", "执行基本数学运算，支持加减乘除和基本函数", "math", false, false);
    }
    
    @Override
    protected Map<String, ParameterDescription> initializeParameterDescriptions() {
        Map<String, ParameterDescription> params = new HashMap<>();
        
        params.put("expression", new ParameterDescription(
                "expression",
                "要计算的数学表达式",
                "string",
                true
        ));
        
        return params;
    }
    
    @Override
    protected String doExecute(Map<String, String> parameters) throws ToolExecutionException {
        String expression = parameters.get("expression");
        
        try {
            // 这里使用一个简单的计算引擎，实际生产环境可以使用更安全和功能更强大的库
            double result = evaluateExpression(expression);
            return Double.toString(result);
        } catch (Exception e) {
            throw new ToolExecutionException(
                    getName(),
                    "CALCULATION_ERROR",
                    "计算表达式 '" + expression + "' 时出错: " + e.getMessage(),
                    e
            );
        }
    }
    
    /**
     * 评估数学表达式
     *
     * @param expression 数学表达式
     * @return 计算结果
     */
    private double evaluateExpression(String expression) {
        // 这里简化处理，只支持简单的加减乘除，格式为 "数字 运算符 数字"
        // 实际应用中应该使用专业的表达式计算库如 EvalEx
        String trimmedExpr = expression.trim();
        
        try {
            if (trimmedExpr.contains("+")) {
                String[] parts = trimmedExpr.split("\\+");
                return Double.parseDouble(parts[0].trim()) + Double.parseDouble(parts[1].trim());
            } else if (trimmedExpr.contains("-")) {
                String[] parts = trimmedExpr.split("-");
                return Double.parseDouble(parts[0].trim()) - Double.parseDouble(parts[1].trim());
            } else if (trimmedExpr.contains("*")) {
                String[] parts = trimmedExpr.split("\\*");
                return Double.parseDouble(parts[0].trim()) * Double.parseDouble(parts[1].trim());
            } else if (trimmedExpr.contains("/")) {
                String[] parts = trimmedExpr.split("/");
                double divisor = Double.parseDouble(parts[1].trim());
                if (divisor == 0) {
                    throw new ArithmeticException("除数不能为零");
                }
                return Double.parseDouble(parts[0].trim()) / divisor;
            } else {
                // 如果没有运算符，尝试直接解析为数字
                return Double.parseDouble(trimmedExpr);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的数字格式: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("表达式格式不正确: " + e.getMessage());
        }
    }
} 