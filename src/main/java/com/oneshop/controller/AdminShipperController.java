package com.oneshop.controller;

import com.oneshop.entity.Shipper;
import com.oneshop.entity.User;
import com.oneshop.service.ShipperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/shippers")
@RequiredArgsConstructor
public class AdminShipperController {

	private final ShipperService shipperService;

	@GetMapping
	public String listShippers(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
		List<Shipper> shippers = (keyword == null || keyword.isEmpty()) ? shipperService.getAllShippers()
				: shipperService.searchShippers(keyword);
		model.addAttribute("shippers", shippers);
		model.addAttribute("keyword", keyword);
		return "admin/shipper-list";
	}

	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("shipper", new Shipper());
		return "admin/shipper-form";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		Shipper shipper = shipperService.getShipperById(id);
		if (shipper == null)
			return "redirect:/admin/shippers";
		model.addAttribute("shipper", shipper);
		return "admin/shipper-form";
	}

	@PostMapping("/save")
	public String saveShipper(@ModelAttribute Shipper shipper) {
		shipperService.saveShipper(shipper);
		return "redirect:/admin/shippers";
	}

	@GetMapping("/delete/{id}")
	public String deleteShipper(@PathVariable Long id) {
		shipperService.deleteShipper(id);
		return "redirect:/admin/shippers";
	}
	
	@GetMapping("/shippers/form")
	public String showShipperForm(@RequestParam(required = false) Long id, Model model) {
	    Shipper shipper;
	    if (id != null) {
	        shipper = shipperService.getShipperById(id); 
	        if (shipper.getUser() == null) {
	            shipper.setUser(new User()); // phòng trường hợp thiếu
	        }
	    } else {
	        shipper = new Shipper();
	        shipper.setUser(new User()); // tạo user trống cho form hiển thị
	    }

	    model.addAttribute("shipper", shipper);
	    return "admin/shipper-form";
	}

}