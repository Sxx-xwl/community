package sxx.xwl.community.community.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sxx.xwl.community.community.entity.Message;
import sxx.xwl.community.community.entity.Page;
import sxx.xwl.community.community.entity.User;
import sxx.xwl.community.community.service.MessageService;
import sxx.xwl.community.community.service.UserService;
import sxx.xwl.community.community.util.HostHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sxx_27
 * @create 2022-05-05 11:20
 */
@Controller
public class MessageController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

    //私信列表
    @RequestMapping(value = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {

        User user = hostHolder.getUser();

        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        //会话列表
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        //查询未读消息数
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";
    }

    //私信详情
    @RequestMapping(value = "/letter-detail/list/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetailList(Model model, Page page, @PathVariable("conversationId") String conversationId) {

        User user = hostHolder.getUser();

        //分页信息
        page.setLimit(5);
        page.setPath("/letter-detail/list/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        //会话详情
        List<Message> detailList = messageService.findLetters(
                conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> details = new ArrayList<>();
        if (detailList != null) {
            for (Message detail : detailList) {
                Map<String, Object> map = new HashMap<>();
                map.put("detail", detail);
                map.put("user", userService.findUserById(detail.getFromId()));

                details.add(map);
            }
        }
        model.addAttribute("details", details);
        model.addAttribute("fromUser", getLetterTarget(conversationId));

        return "/site/letter-detail";

    }

    private User getLetterTarget(String conversationId) {
        String[] s = conversationId.split("_");
        int num0 = Integer.parseInt(s[0]);
        int num1 = Integer.parseInt(s[1]);
        User user = hostHolder.getUser();
        if (num0 != user.getId()) {
            return userService.findUserById(num0);
        }else {
            return userService.findUserById(num1);
        }
    }
}
