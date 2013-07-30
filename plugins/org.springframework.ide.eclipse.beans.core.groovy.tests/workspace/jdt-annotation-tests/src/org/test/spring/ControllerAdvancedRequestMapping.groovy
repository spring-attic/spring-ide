package org.test.spring;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(["/index1.htm" , "/index2.htm"])
public class ControllerAdvancedRequestMapping {

    @RequestMapping(method = [RequestMethod.GET, RequestMethod.POST])
    public String setupForm(@RequestParam("petId") int petId, ModelMap model) {
        return "petForm";
    }

}
