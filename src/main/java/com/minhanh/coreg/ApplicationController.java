package com.minhanh.coreg;

import java.util.*;

import com.minhanh.coreg.transfactor.Transfactor;
import com.minhanh.coreg.transfactor.TransfactorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class ApplicationController {
	@Autowired
	private TransfactorService transfactorService;

	@GetMapping("/")
	public ModelAndView rootView() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("index");
		return mav;
	}

	@GetMapping("/search")
	public ModelAndView createGraph(@RequestParam Map<String,String> all_params, ModelMap model) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("search");
		return mav;
	}

	@GetMapping(value = "/graph", produces = "application/json")
	public Map<String, List<? extends Map<String, ?>>> getCoregulation(@RequestParam Map<String,String> allParams, ModelMap model) {
		String[] genes_list = allParams.get("genes").split("\\R");
		byte min = Byte.parseByte(allParams.get("coregulation"));

		return coregulatingGraph(genes_list, min);
	}

	private Map<String, List<? extends Map<String, ?>>> coregulatingGraph(String[] genes_list, byte min) {
		List<Transfactor> all_regulations = transfactorService.filterByGenes(genes_list);
		Map<String, Set<String>> regulating_map = new HashMap<>();

		for (Transfactor regulation : all_regulations) {
			String reg_factor = regulation.getRegulatoryFactor();
			String tar_gene = regulation.getTargetGene();
			if (regulating_map.get(reg_factor) == null) {
				regulating_map.put(reg_factor, new HashSet<>(Arrays.asList(tar_gene)));
			} else regulating_map.get(reg_factor).add(tar_gene);
		}

		Set<String> nodes_set = new HashSet<>();
		List<Map<String,Object>> links_list = new ArrayList<>();

		List<String> key_list = new ArrayList<>(regulating_map.keySet());
		ListIterator<String> itr = key_list.listIterator(0);
		while (itr.hasNext()) {
			String tf = itr.next();
			Set<String> genes_1 = regulating_map.get(tf);
			ListIterator<String> itr_next = key_list.listIterator(itr.nextIndex());
			itr_next.forEachRemaining(other_tf -> {
				Set<String> genes_2 = regulating_map.get(other_tf);
				Set<String> intersect = new HashSet<>(genes_1);
				intersect.retainAll(genes_2);
				int inter_count = intersect.size();
				if (inter_count >= min) {
					nodes_set.add(tf);
					nodes_set.add(other_tf);
					links_list.add(Map.of("source", tf, "target", other_tf, "value", inter_count));
				}
			});
		}

		if (nodes_set.isEmpty() || links_list.isEmpty()) return new HashMap<>();
		else {
			List<Map<String,String>> nodes_list = new ArrayList<>();
			for (String tf : nodes_set) {
				nodes_list.add(Map.of("id", tf));
			}
			return Map.of("nodes", nodes_list, "links", links_list);
		}
	}
}
