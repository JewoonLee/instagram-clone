package kr.or.spring.instagram_clone.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.spring.instagram_clone.dto.Post;
import kr.or.spring.instagram_clone.service.PostService;


@Controller
public class PostController {
	@Autowired
	PostService postService;

	@GetMapping(path="/list")
	public String list(@RequestParam(name="start", required=false, defaultValue="0") int start,
					   ModelMap model, @CookieValue(value="count", defaultValue="1", required=true) String value,
					   HttpServletResponse response) {
		
		try {
			int i = Integer.parseInt(value);
			value = Integer.toString(++i);
		}catch(Exception ex){
			value = "1";
		}
		
		Cookie cookie = new Cookie("count", value);
		cookie.setMaxAge(60 * 60 * 24 * 365); // 1년 동안 유지.
		cookie.setPath("/"); // / 경로 이하에 모두 쿠키 적용. 
		response.addCookie(cookie);
		
		List<Post> list = postService.getPosts(start);
		
		int count = postService.getCount();
		int pageCount = count / PostService.LIMIT;
		if(count % PostService.LIMIT > 0)
			pageCount++;
		
		List<Integer> pageStartList = new ArrayList<>();
		for(int i = 0; i < pageCount; i++) {
			pageStartList.add(i * PostService.LIMIT);
		}
		
		model.addAttribute("list", list);
		model.addAttribute("count", count);
		model.addAttribute("pageStartList", pageStartList);
		model.addAttribute("cookieCount", value);
		
		return "list";
	}
	
	@GetMapping(path="/upload")
	public String upload() {
		return "upload";
	}
	
	@PostMapping(path="/write")
	public String write(@ModelAttribute Post post,
						HttpServletRequest request) {
		String clientIp = request.getRemoteAddr();
		System.out.println("clientIp : " + clientIp);
		postService.addPost(post, clientIp);
		return "redirect:list";
	}
	
   
	@GetMapping(path="/delete")
	public String delete(@RequestParam(name="id", required=true) Long id, 
			             @SessionAttribute("isAdmin") String isAdmin,
			             HttpServletRequest request,
			             RedirectAttributes redirectAttr) {
		if(isAdmin == null || !"true".equals(isAdmin)) { // 세션값이 true가 아닐 경우
			redirectAttr.addFlashAttribute("errorMessage", "로그인을 하지 않았습니다.");
			return "redirect:loginform";
		}
		String clientIp = request.getRemoteAddr();
		postService.deletePost(id, clientIp);
		return "redirect:list";		
	}
}