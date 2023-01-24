package io.thalesdigital.rfc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class MainController {

    private static String STRICT_COOKIE_NAME = "StrictCookie";
    private static String LAX_COOKIE_NAME = "LaxCookie";
    private static String DEFAULT_COOKIE_NAME = "DefaultCookie";
    private static String AGE_COOKIE_NAME = "NotASessionCookie";

    private static String[] WL_PARAM = new String[]{"code", "state"};

    @RequestMapping("/")
    public String index(Model model, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        model.addAttribute("cookies", cookies);
        return "index";
    }

    @RequestMapping("/helpers")
    public String helpers(Model model, HttpServletRequest request) {
        return "helpers";
    }


    @RequestMapping("/strict")
    public String strictCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie", STRICT_COOKIE_NAME + "=One; SameSite=Strict;");
        response.addHeader("Set-Cookie", LAX_COOKIE_NAME + "=Two; SameSite=Lax;");
        response.addHeader("Set-Cookie", AGE_COOKIE_NAME + "=Three; Max-Age=2592000");
        response.addHeader("Set-Cookie", DEFAULT_COOKIE_NAME + "=Four;");


        return "redirect:/";
    }

    @RequestMapping("/callback")
    public String callback(Model model, HttpServletRequest request) {
        // We can't get a cookie with sameSite strict if the callback is called from an external website
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return doBounce(model, request);
        }

        // We only check the strict cookie, as other should work.
        Optional<Cookie> cookie = Arrays.stream(cookies).filter(t -> STRICT_COOKIE_NAME.equals(t.getName())).findFirst();
        if (!cookie.isPresent()) {
            return doBounce(model, request);
        }

        return internalCallback(model, request);
    }

    @RequestMapping(value = "/callback", params = {"bounced"})
    public String bouncedCallback(Model model, HttpServletRequest request) {
        // We have bounced, so even if we don't have the cookie we can't really do more.
        return internalCallback(model, request);
    }


    private String internalCallback(Model model, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            Optional<Cookie> cookie = Arrays.stream(cookies).filter(t -> STRICT_COOKIE_NAME.equals(t.getName())).findFirst();
            if (cookie.isPresent()) {
                String foundCookie = cookie.get().getName() + "=" + cookie.get().getValue();
                System.out.println("Strict cookie found: " + foundCookie);
                model.addAttribute("cookie", foundCookie);
            }
            return "callback";
        }

        model.addAttribute("cookie", "");
        model.addAttribute("error", "Still no cookies :'(");
        return "callback";

    }

    private String doBounce(Model model, HttpServletRequest request) {
        String qs = whiteListRequestParam(request);
        // Here we should still have no cookies.
        String refresh = "0;URL=/callback" + qs + "&bounced";
        model.addAttribute("refresh", refresh);
        return "bounce";
    }

    private String whiteListRequestParam(HttpServletRequest request) {
        Map<String, String> wtParam = new HashMap<>();
        for (String param : WL_PARAM) {
            if (request.getParameter(param) != null) {
                wtParam.put(param, request.getParameter(param));
            }
        }

        String whiteListedParamQueryString = wtParam.keySet().stream()
                .map(key -> key + "=" + wtParam.get(key))
                .collect(Collectors.joining("&", "?", ""));

        return whiteListedParamQueryString;
    }

}
