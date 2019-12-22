package com.mycomany.community.controller;

import com.mycomany.community.services.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {


    @Autowired
    private AlphaService alphaService;


    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }



// Get Method
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {

        System.out.println(current);
        System.out.println(limit);

        return "some students";
    }


    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "a student";
    }


  //Post method

    @RequestMapping(path="/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age){

        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    // Response data
    @RequestMapping(path="/teacher",method = RequestMethod.GET)
    public ModelAndView getTeacher(){

          ModelAndView mav = new ModelAndView();
          mav.addObject("name","huang xin");
          mav.addObject("age",26);
          mav.setViewName("/demo/view");

          return mav;
    }

    @RequestMapping(path="/school", method=RequestMethod.GET)
   public String getSchool(Model model){

      model.addAttribute("name","NUS");
      model.addAttribute("age","80");
      return   "/demo/view";
    }

    //Response JSON object
    @RequestMapping(path="/emp",method= RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getEmp(){
        Map<String,Object> emp = new HashMap<>();
        emp.put("name","Huang Xin");
        emp.put("age",23);
        emp.put("salary",8000);
        return emp;
    }

    @RequestMapping(path="/emps",method= RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmps(){

        List<Map<String, Object>> list = new ArrayList<>();


        Map<String,Object> emp = new HashMap<>();
        emp.put("name","Huang Xin");
        emp.put("age",23);
        emp.put("salary",8000);
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name","Liu");
        emp.put("age",24);
        emp.put("salary",9000);
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name","Wu");
        emp.put("age",25);
        emp.put("salary",9999);
        list.add(emp);



        return list;
    }






}

