package com.jjc.ui;

/**
 * @author sweeter
 * @date 2025/12/10 16:46
 * @description
 */
import org.springframework.web.bind.annotation.GetMapping;
public class RootRedirectController extends MarkController {

    /**
     *  根路径重定向
     * @return
     */
    @GetMapping("/")
    public String redirectToIndex() {
        return "redirect:/ui/index.html";
    }
}
