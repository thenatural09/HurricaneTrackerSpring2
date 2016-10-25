package com.theironyard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by zach on 10/21/16.
 */
@Controller
public class HurricaneTrackerController {
    @Autowired
    HurricaneRepository hurricanes;

    @Autowired
    UserRepository users;

    @PostConstruct
    public void init() throws PasswordStorage.CannotPerformOperationException {
        User defaultUser = new User("Zach", PasswordStorage.createHash("hunter2"));
        if (users.findFirstByName(defaultUser.name) == null) {
            users.save(defaultUser);
        }
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home(Model model, Hurricane.Category category, String search, HttpSession session) {
        String name = (String) session.getAttribute("username");
        User user = users.findFirstByName(name);

        List<Hurricane> hlist;
        if (category != null) {
            hlist = hurricanes.findByCategory(category);
        }
        else if (search != null) {
            hlist = hurricanes.findByNameContainingIgnoreCaseOrLocationContainingIgnoreCase(search, search);
        }
        else {
            hlist = (List<Hurricane>) hurricanes.findAll();
        }

        for (Hurricane h : hlist) {
            h.isMe = h.user.name.equals(name);
        }

        model.addAttribute("hurricanes", hlist);
        model.addAttribute("user", user);
        return "home";
    }

    @RequestMapping(path = "/hurricane", method = RequestMethod.POST)
    public String addHurricane(String hname, String hlocation, Hurricane.Category hcategory, String himage, HttpSession session) throws Exception {
        String name = (String) session.getAttribute("username");
        User user = users.findFirstByName(name);
        if (user == null) {
            throw new Exception("Not logged in.");
        }
        Hurricane h = new Hurricane(hname, hlocation, hcategory, himage, user);
        hurricanes.save(h);
        return "redirect:/";
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, HttpSession session) throws Exception {
        User user = users.findFirstByName(username);
        if (user == null) {
            user = new User(username, PasswordStorage.createHash(password));
            users.save(user);
        }
        else if (!PasswordStorage.verifyPassword(password,user.password)) {
            throw new Exception("Wrong password!");
        }
        session.setAttribute("username", username);
        return "redirect:/";
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @RequestMapping(path = "/delete-hurricane", method = RequestMethod.POST)
    public String delete(HttpSession session, int id) throws Exception {
        if (!validateUser(session,id)) {
            throw new Exception("Not allowed!");
        }
        hurricanes.delete(id);
        return "redirect:/";
    }

    @RequestMapping(path = "/edit-hurricane",method = RequestMethod.GET)
    public String edit(Model model, int id) {
        Hurricane hurricane = hurricanes.findOne(id);
        model.addAttribute("hurricane",hurricane);
        return "edit";
    }

    @RequestMapping(path = "/edit-hurricane",method = RequestMethod.POST)
    public String editHurricane(int id,String hname,String hlocation,Hurricane.Category hcategory,String himage,HttpSession session) throws Exception {
        if(!validateUser(session,id)) {
            throw new Exception("Not allowed!!");
        }
        Hurricane hurricane = hurricanes.findOne(id);
        hurricane.name = hname;
        hurricane.location = hlocation;
        hurricane.category = hcategory;
        hurricane.image = himage;
        hurricanes.save(hurricane);
        return "redirect:/";
    }

    public boolean validateUser(HttpSession session,int id) {
        String name = (String) session.getAttribute("username");
        User user = users.findFirstByName(name);
        Hurricane h = hurricanes.findOne(id);
        return user != null && h != null && user.name.equals(h.user.name);
    }

}
