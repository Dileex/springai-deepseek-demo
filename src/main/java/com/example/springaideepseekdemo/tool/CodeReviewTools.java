package com.example.springaideepseekdemo.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CodeReviewTools {

    private static final Logger log = LoggerFactory.getLogger(CodeReviewTools.class);

    @Tool(description = "按照 code-review-skill 的审查流程审查 Java 代码。参数 code 是待审查代码，返回 Markdown 格式审查报告。")
    public String reviewJavaCode(String code) {
        log.info("reviewJavaCode tool called");

        String source = code == null ? "" : code.strip();
        if (source.isBlank()) {
            return "请提供需要审查的 Java 代码。";
        }

        List<Finding> findings = new ArrayList<>();
        checkNullPointerRisk(source, findings);
        checkSwallowedException(source, findings);
        checkSystemOut(source, findings);
        checkHardcodedSecret(source, findings);
        checkMissingValidation(source, findings);

        StringBuilder report = new StringBuilder();
        report.append("""
                # 代码审查报告

                审查依据：code-review-skill / references/checklist.md

                ## 审查路径

                - Step 1：识别代码场景
                - Step 2：按风险优先级检查
                - Step 3：调用 reviewJavaCode 工具执行基础检查
                - Step 4：按固定结构输出报告

                ## 总体结论

                """);
        if (findings.isEmpty()) {
            report.append("这段代码没有命中当前内置的高风险规则。仍建议补充单元测试，并结合业务上下文继续人工确认。\n\n");
        } else {
            report.append("这段代码发现 ").append(findings.size()).append(" 个需要关注的问题，建议先处理高风险项。\n\n");
        }

        report.append("## 主要问题\n\n");
        if (findings.isEmpty()) {
            report.append("- 暂无明确问题。\n\n");
        } else {
            for (int i = 0; i < findings.size(); i++) {
                Finding finding = findings.get(i);
                report.append(i + 1).append(". **").append(finding.title()).append("**\n\n")
                        .append("   风险级别：").append(finding.level()).append("\n\n")
                        .append("   问题说明：").append(finding.detail()).append("\n\n")
                        .append("   修改建议：").append(finding.suggestion()).append("\n\n");
            }
        }

        report.append("""
                ## 建议补充的测试

                - 正常输入场景
                - 空值或非法输入场景
                - 异常分支场景
                - 关键业务规则的回归测试

                ## 下一步

                先修复高风险问题，再补测试，最后重新发起一次审查。
                """);
        return report.toString();
    }

    private void checkNullPointerRisk(String source, List<Finding> findings) {
        if (source.contains(".getName()") && !source.contains("null")) {
            findings.add(new Finding(
                    "存在空指针风险",
                    "高",
                    "代码直接调用对象方法，但没有看到空值保护。参数为 null 时可能触发 NullPointerException。",
                    "在进入业务逻辑前做参数校验，或者使用明确的异常提示，让调用方知道问题出在哪里。"
            ));
        }
    }

    private void checkSwallowedException(String source, List<Finding> findings) {
        if (source.contains("catch") && (source.contains("catch (Exception") || source.contains("catch(Exception"))
                && !source.contains("throw") && !source.contains("log.")) {
            findings.add(new Finding(
                    "异常被吞掉",
                    "高",
                    "代码捕获了通用异常，但没有重新抛出，也没有记录日志。线上排查时会丢失关键上下文。",
                    "不要静默吞异常。至少记录错误上下文，必要时转换成业务异常继续抛出。"
            ));
        }
    }

    private void checkSystemOut(String source, List<Finding> findings) {
        if (source.contains("System.out.println")) {
            findings.add(new Finding(
                    "使用 System.out.println 输出日志",
                    "中",
                    "业务代码里直接使用标准输出，不利于日志级别控制、链路追踪和线上检索。",
                    "改用项目统一日志框架，并补充必要的业务字段。"
            ));
        }
    }

    private void checkHardcodedSecret(String source, List<Finding> findings) {
        String lower = source.toLowerCase();
        if (lower.contains("password") || lower.contains("secret") || lower.contains("apikey") || lower.contains("api_key")) {
            findings.add(new Finding(
                    "疑似硬编码敏感信息",
                    "高",
                    "代码中出现 password、secret 或 api key 相关字段，可能存在敏感信息硬编码风险。",
                    "敏感信息应放到环境变量、配置中心或密钥管理系统，不要写死在代码里。"
            ));
        }
    }

    private void checkMissingValidation(String source, List<Finding> findings) {
        if ((source.contains("@RequestBody") || source.contains("@RequestParam")) && !source.contains("@Valid") && !source.contains("Assert.")) {
            findings.add(new Finding(
                    "缺少入参校验",
                    "中",
                    "接口层接收外部输入，但没有看到 Bean Validation 或显式参数校验。",
                    "为请求对象补充校验注解，或者在方法入口明确校验必填字段和取值范围。"
            ));
        }
    }

    private record Finding(String title, String level, String detail, String suggestion) {
    }
}
