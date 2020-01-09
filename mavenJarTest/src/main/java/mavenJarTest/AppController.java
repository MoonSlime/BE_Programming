package mavenJarTest;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class AppController {
	  private static final String template = "Hello, %s!";
	  private final AtomicLong counter = new AtomicLong();

	  
	  @RequestMapping("/app")
	  public App app(@RequestParam(value="name", defaultValue="World") String name) {
	    return new App(counter.incrementAndGet(),
	              String.format(template, name));
	  }
	  
	  @RequestMapping("/html")
	  public String html() {
		  ModelAndView mav = new ModelAndView("src/main/resources/html/view.html");
		  return new String("HELLO");
	  }

	  @RequestMapping("/html2")
	  public ModelAndView html2() {
		  System.out.println("Request \"/html\"");
		  ModelAndView mav = new ModelAndView("/src/main/html/view.html");
		  return mav;
	  }
}
