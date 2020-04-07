package com.infosys.filedownload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path = "/demo")
public class MainController {

	@Value("${KEY_ONE}")
	private String keyOne;
	
	@GetMapping(path = "/keyone")
	public @ResponseBody String addNewUser() {
		return "Saved->" + keyOne;
	}

}
